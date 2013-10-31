/**
 * JPaaS
 * Copyright 2012 Bull S.A.S.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id:$
 */ 
package org.ow2.jonas.jpaas.application.bean;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Instantiate;
import org.apache.felix.ipojo.annotations.Provides;

import org.apache.felix.ipojo.annotations.Validate;
import org.ow2.jonas.jpaas.application.api.ApplicationManager;
import org.ow2.jonas.jpaas.application.api.ApplicationManagerBeanException;
import org.ow2.jonas.jpaas.manager.api.*;

import org.ow2.jonas.jpaas.util.clouddescriptors.cloudapplication.CloudApplicationDesc;
import org.ow2.jonas.jpaas.util.clouddescriptors.cloudapplication.artefact.v1.generated.ArtefactDeployableType;
import org.ow2.jonas.jpaas.util.clouddescriptors.cloudapplication.v1.generated.CloudApplicationType;

import org.ow2.jonas.jpaas.util.clouddescriptors.cloudapplication.xml.v1.generated.XmlDeployableType;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component(immediate=true)
@Instantiate
@Provides
public class ApplicationManagerMock implements ApplicationManager {

    //WARNING !!!! This attribute was added only for dummy tests (it allows the developer to add or remove Applications like in database).
//It has to be removed after the real implementation of all classes.
    private Map<String, Application> listApplication ;

    public final static String UPLOAD_ROOT_DIR = "/tmp/paas-upload-dir";

    /**
     * The logger
     */
    private Log logger = LogFactory.getLog(ApplicationManagerMock.class);

    @Validate
    public void init() {
        listApplication = new HashMap<String, Application>();

        Application app = new Application();
        app.setName("myapp");
        app.setAppId("1");

        ApplicationVersion version = new ApplicationVersion();
        version.setAppId("1");
        version.setVersionId("1");
        version.setVersionLabel("myversion");

        List<ApplicationVersion> versions =  new ArrayList<ApplicationVersion>();
        versions.add(version);

        app.setListApplicationVersion(versions);

        ApplicationVersionInstance instance = new ApplicationVersionInstance();
        instance.setAppId("1");
        instance.setVersionId("1");
        instance.setInstanceId("1");
        instance.setInstanceName("myinstance");

        List<ApplicationVersionInstance> instances =  new ArrayList<ApplicationVersionInstance>();
        instances.add(instance);

        version.setListApplicationVersionInstance(instances);


        listApplication.put(app.getAppId(), app);


    }

    public Application createApplication(String cloudApplicationDescriptor) throws ApplicationManagerBeanException {
        logger.info("Desc=" + cloudApplicationDescriptor);
        dump();

        CloudApplicationDesc cloudApplicationDesc =null;
        CloudApplicationType cloudApplication = null;
        try {
            cloudApplicationDesc = new CloudApplicationDesc(cloudApplicationDescriptor);
            cloudApplication = (CloudApplicationType) cloudApplicationDesc.getCloudApplication();
        } catch (Exception e) {
            throw new ApplicationManagerBeanException("Error:" + e.getMessage());
        }

        Application app = new Application();
        app.setName(cloudApplication.getName());
        app.setAppId(UUID.randomUUID().toString());

        listApplication.put(app.getAppId(), app);

        dump();

        return app;

    }

    public ApplicationVersion createApplicationVersion(String appId, String cloudApplicationVersionDescriptor) throws ApplicationManagerBeanException {

        logger.info("appId=" + appId + ", Desc=" + cloudApplicationVersionDescriptor);
        dump();

        CloudApplicationDesc cloudApplicationDesc =null;
        CloudApplicationType cloudApplication = null;
        try {
            cloudApplicationDesc = new CloudApplicationDesc(cloudApplicationVersionDescriptor);
            cloudApplication = (CloudApplicationType) cloudApplicationDesc.getCloudApplication();
        } catch (Exception e) {
            throw new ApplicationManagerBeanException("Error:" + e.getMessage());
        }

        Application app = getApplication(appId);
        if (app == null) {
            throw new ApplicationManagerBeanException("Application '" + appId + "' doesn't exist");
        }

        // Check if the version exist
        List<ApplicationVersion> versions = app.getListApplicationVersion();

        if (versions != null) {
            for (ApplicationVersion v:versions) {
                if (v.getVersionLabel().equals(cloudApplication.getVersion())) {
                    throw new ApplicationManagerBeanException("Version already exists for app=" + appId + " and version=" + cloudApplication.getVersion());
                }
            }
        } else {
            versions = new ArrayList<ApplicationVersion>();
        }

        ApplicationVersion appVersion = new ApplicationVersion();
        appVersion.setAppId(appId);
        appVersion.setVersionId(UUID.randomUUID().toString());
        appVersion.setVersionLabel(cloudApplication.getVersion());

        //appVersion.setRequirements(cloudApplication.getRequirements().getRequirement());
        //appVersion.setCapabilities(cloudApplication.getCapabilities().getApplication());

        // deployables
        List<Deployable> deployables = new ArrayList<Deployable>();
        List<Object> deployablesType = cloudApplication.getDeployables().getDeployables();
        for (Object deployableType:deployablesType) {
            if (deployableType instanceof ArtefactDeployableType) {
                ArtefactDeployableType artefactDeployable = (ArtefactDeployableType) deployableType;
                Deployable deployable = new Deployable();
                deployable.setDeployabledId(UUID.randomUUID().toString());
                deployable.setDeployableName(artefactDeployable.getName());

                deployable.setLocationUrl(buildLocationUrl(appId,appVersion.getVersionId(),deployable.getDeployableName()));

                //deployable.setRequirements();
                deployable.setUploaded(false);
                //deployable.setSlaEnforcement();
                deployables.add(deployable);
            } else if (deployableType instanceof XmlDeployableType) {
                XmlDeployableType xmlDeployable = (XmlDeployableType) deployableType;
                Deployable deployable = new Deployable();
                deployable.setDeployabledId(UUID.randomUUID().toString());
                deployable.setDeployableName(xmlDeployable.getName());
                deployable.setLocationUrl(buildLocationUrl(appId,appVersion.getVersionId(),deployable.getDeployableName()));
                //deployable.setRequirements();
                deployable.setUploaded(false);
                //deployable.setSlaEnforcement();
                deployables.add(deployable);
            } else {
                logger.error("Deployable type unknown:" + deployableType.getClass().toString());
            }
        }


        appVersion.setSortedDeployablesList(deployables);

        versions.add(appVersion);

        app.setListApplicationVersion(versions);

        dump();

        return appVersion;

    }

    private String buildLocationUrl(String appId, String versionId, String deployableName) {
        return UPLOAD_ROOT_DIR + File.separator +
                "app" + File.separator +
                appId + File.separator +
                "version" + File.separator +
                versionId + File.separator +
                deployableName;
    }

    public Deployable notifyArtefactUploades(String appId, String versionId, String deployableId) {
        logger.info("appId=" + appId + ", versionId=" + versionId + ", deployableId=" + deployableId);
        Deployable deployable = null;
        ApplicationVersion version = getApplicationVersion(appId,versionId);
        for (Deployable d:version.getSortedDeployablesList()) {
            if (d.getDeployabledId().equals(deployableId)) {
                deployable = d;
                deployable.setUploaded(true);
                logger.info("Deployable " + deployableId + " updated");
                break;
            }
        }
        return deployable;

    }

    public ApplicationVersionInstance createApplicationVersionInstance(String appId, String versionId, String cloudApplicationVersionInstanceDescriptor, String deploymentDescriptor) throws ApplicationManagerBeanException {
        logger.info("appId=" + appId + ", versionId=" + versionId + ", Desc=" + cloudApplicationVersionInstanceDescriptor + ", DeployDesc=" + deploymentDescriptor);
        dump();

        CloudApplicationDesc cloudApplicationDesc =null;
        CloudApplicationType cloudApplication = null;

        try {
            cloudApplicationDesc = new CloudApplicationDesc(cloudApplicationVersionInstanceDescriptor);
            cloudApplication = (CloudApplicationType) cloudApplicationDesc.getCloudApplication();
        } catch (Exception e) {
            throw new ApplicationManagerBeanException("Error:" + e.getMessage());
        }

        Application app = getApplication(appId);
        if (app == null) {
            throw new ApplicationManagerBeanException("Application '" + appId + "' doesn't exist");
        }

        ApplicationVersion version = getApplicationVersion(appId,versionId);
        if (version == null) {
            throw new ApplicationManagerBeanException("Version doesn't exist for appId=" + appId + ", versionId=" + versionId);
        }

        // Check if the instance exist
        List<ApplicationVersionInstance> instances = version.getListApplicationVersionInstance();

        if (instances != null) {
            for (ApplicationVersionInstance i:instances) {
                if (i.getInstanceName().equals(cloudApplication.getInstance())) {
                    throw new ApplicationManagerBeanException("Instance already exists for app=" + appId + ", version=" + versionId + ", instance=" + cloudApplication.getInstance());
                }
            }
        } else {
            instances = new ArrayList<ApplicationVersionInstance>();
        }

        ApplicationVersionInstance appVersionInstance = new ApplicationVersionInstance();
        appVersionInstance.setAppId(appId);
        appVersionInstance.setVersionId(versionId);
        appVersionInstance.setInstanceId(UUID.randomUUID().toString());
        appVersionInstance.setInstanceName(cloudApplication.getInstance());

        instances.add(appVersionInstance);
        version.setListApplicationVersionInstance(instances);

        dump();

        return  appVersionInstance;

    }

    public Future<ApplicationVersionInstance> startApplicationVersionInstance(final String appId, final String versionId, final String instanceId) throws ApplicationManagerBeanException {
        logger.info("appId=" + appId + ", versionId=" + versionId + ", instanceId=" + instanceId);

        ExecutorService es = Executors.newFixedThreadPool(3);
        final Future<ApplicationVersionInstance> future = es.submit(new Callable<ApplicationVersionInstance>() {
            public ApplicationVersionInstance call() throws Exception {

                ApplicationVersionInstance instance = getApplicationVersionInstance(appId,versionId,instanceId);
                if (instance == null) {
                    throw new ApplicationManagerBeanException("Application version instance '" + appId + "/" + versionId + "/" + instanceId + "' doesn't exist");

                }

                instance.setState(ApplicationVersionInstance.INSTANCE_RUNNING);

                return instance;
            }
        });
        return future;

    }

    public Future<ApplicationVersionInstance> stopApplicationVersionInstance(final String appId, final String versionId, final String instanceId) throws ApplicationManagerBeanException {
        logger.info("appId=" + appId + ", versionId=" + versionId + ", instanceId=" + instanceId);

        ExecutorService es = Executors.newFixedThreadPool(3);
        final Future<ApplicationVersionInstance> future = es.submit(new Callable<ApplicationVersionInstance>() {
            public ApplicationVersionInstance call() throws Exception {
                ApplicationVersionInstance instance = getApplicationVersionInstance(appId, versionId, instanceId);
                if (instance == null) {
                    throw new ApplicationManagerBeanException("Application version instance '" + appId + "/" + versionId + "/" + instanceId + "' doesn't exist");

                }
                instance.setState(ApplicationVersionInstance.INSTANCE_STOPPED);

                return instance;
            }
        });
        return future;

    }

    public List<Application> findApplications() {

        logger.info("Get all applications");
        return new ArrayList<>(listApplication.values());
    }

    public List<ApplicationVersion> findApplicationVersion(String appId) {

        logger.info("Get versions for app:" + appId);

        Application app = getApplication(appId);
        if (app == null) {
            return null;
        } else {
            return app.getListApplicationVersion();
        }

    }

    public List<ApplicationVersionInstance> findApplicationVersionsInstances(String appId, String versionId) {
        logger.info("Get instances for app:" + appId + ", version:" + versionId);

        ApplicationVersion version = getApplicationVersion(appId,versionId);
        if (version == null) {
            return null;
        } else {
            return version.getListApplicationVersionInstance();
        }
    }

    public Application getApplication(String appId) {
        logger.info("Get application with appId=" + appId);
        return listApplication.get(appId);
    }


    public ApplicationVersion getApplicationVersion(String appId, String versionId) {
        logger.info("Get application version with appId=" + appId + ", versionId=" + versionId);

        Application app = getApplication(appId);
        if (app == null) {
            return null;
        }

        ApplicationVersion version = null;
        List<ApplicationVersion> versions = app.getListApplicationVersion();
        for (ApplicationVersion v : versions) {
            if (v.getVersionId().equals(versionId)) {
                version = v;
                break;
            }
        }

        return version;
    }

    public ApplicationVersionInstance getApplicationVersionInstance(String appId, String versionId, String instanceId) {
        logger.info("Get application instance with appId=" + appId + ", versionId=" + versionId + ", instanceId=" + instanceId);

        ApplicationVersion version = getApplicationVersion(appId, versionId);
        if (version == null) {
            return null;
        }

        ApplicationVersionInstance instance = null;
        List<ApplicationVersionInstance> instances = version.getListApplicationVersionInstance();
        for (ApplicationVersionInstance i : instances) {
            if (i.getInstanceId().equals(instanceId)) {
                instance = i;
                break;
            }
        }

        return instance;

    }

    public void deleteApplication(String appId) throws ApplicationManagerBeanException {
        logger.info("Delete application with appId=" + appId);

        Application app = getApplication(appId);
        if (app == null) {
            throw new ApplicationManagerBeanException("Application '" + appId + "' doesn't exist");
        }

        listApplication.remove(appId);

    }

    public void deleteApplicationVersion(String appId, String versionId) throws ApplicationManagerBeanException {
        logger.info("Delete application with appId=" + appId + ", versionId=" + versionId);

        Application app = getApplication(appId);
        if (app == null) {
            throw new ApplicationManagerBeanException("Application '" + appId + "' doesn't exist");
        }

        ApplicationVersion version = getApplicationVersion(appId, versionId);
        if (version == null) {
            throw new ApplicationManagerBeanException("Application version '" + appId + "/" + versionId + "' doesn't exist");
        }

        List<ApplicationVersion> versions = findApplicationVersion(appId);
        versions.remove(version);
        app.setListApplicationVersion(versions);

    }

    public void deleteApplicationVersionInstance(String appId, String versionId, String instanceId) throws ApplicationManagerBeanException {
        logger.info("Delete application with appId=" + appId + ", versionId=" + versionId + ", instanceId=" + instanceId);

        Application app = getApplication(appId);
        if (app == null) {
            throw new ApplicationManagerBeanException("Application '" + appId + "' doesn't exist");
        }

        ApplicationVersion version = getApplicationVersion(appId, versionId);
        if (version == null) {
            throw new ApplicationManagerBeanException("Application version '" + appId + "/" + versionId + "' doesn't exist");
        }

        ApplicationVersionInstance instance = getApplicationVersionInstance(appId, versionId, instanceId);
        if (instance == null) {
            throw new ApplicationManagerBeanException("Application version instance '" + appId + "/" + versionId + "/" + instanceId + "' doesn't exist");
        }

        List<ApplicationVersionInstance> instances = findApplicationVersionsInstances(appId, versionId);
        instances.remove(instance);
        version.setListApplicationVersionInstance(instances);
    }

    public Environment getEnvironment(String appId, String versionId, String instanceId)  {
        //TODO
        System.out.println("JPAAS-APPLICATION-MANAGER / getEnvironment called");
        return null;
    }

    public Future<ApplicationVersionInstance> scaleUp(String appId, String versionId, String instanceId)
            throws ApplicationManagerBeanException {
        System.out.println("JPAAS-APPLICATION-MANAGER / ScaleUp called : " + appId + ", "
                + versionId + ", " + instanceId);

        ExecutorService es = Executors.newFixedThreadPool(3);
        final Future<ApplicationVersionInstance> future = es.submit(new Callable<ApplicationVersionInstance>() {
            public ApplicationVersionInstance call() throws Exception {

                return null;


            }
        });
        return future;
    }

    public Future<ApplicationVersionInstance> scaleDown(String appId, String versionId, String instanceId)
            throws ApplicationManagerBeanException {
        System.out.println("JPAAS-APPLICATION-MANAGER / ScaleDown called : " + appId + ", "
                + versionId + ", " + instanceId);
        ExecutorService es = Executors.newFixedThreadPool(3);
        final Future<ApplicationVersionInstance> future = es.submit(new Callable<ApplicationVersionInstance>() {
            public ApplicationVersionInstance call() throws Exception {

                return null;


            }
        });
        return future;
    }

    private void dump() {
        List<Application> apps = findApplications();
        for (Application app:apps) {
            logger.info("App - " + app.getAppId() + "/" + app.getName());

            List<ApplicationVersion> versions = findApplicationVersion(app.getAppId());
            for (ApplicationVersion version:versions)  {
                logger.info("   Version - " + version.getVersionId() + "/" + version.getVersionLabel());
                List <Deployable> deployables = version.getSortedDeployablesList();
                for (Deployable deployable:deployables) {
                    logger.info("    Deployable - " + deployable.getDeployabledId() + "/" + deployable.getDeployableName());
                }

                List<ApplicationVersionInstance> instances = findApplicationVersionsInstances(app.getAppId(),version.getVersionId());
                for (ApplicationVersionInstance instance:instances) {
                    logger.info("      Instance - " + instance.getInstanceId() + "/" + instance.getInstanceName());
                }
            }
        }

    }
}
