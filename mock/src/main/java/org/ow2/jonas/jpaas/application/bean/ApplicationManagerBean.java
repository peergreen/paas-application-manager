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

import org.ow2.jonas.jpaas.application.api.ApplicationManager;
import org.ow2.jonas.jpaas.application.api.ApplicationManagerBeanException;
import org.ow2.jonas.jpaas.manager.api.Application;
import org.ow2.jonas.jpaas.manager.api.ApplicationVersion;
import org.ow2.jonas.jpaas.manager.api.ApplicationVersionInstance;
import org.ow2.jonas.jpaas.manager.api.Deployable;
import org.ow2.jonas.jpaas.manager.api.Environment;

import org.ow2.jonas.jpaas.util.clouddescriptors.cloudapplication.CloudApplicationDesc;
import org.ow2.jonas.jpaas.util.clouddescriptors.cloudapplication.CloudApplicationVersion;
import org.ow2.jonas.jpaas.util.clouddescriptors.cloudapplication.v1.generated.CloudApplicationType;
import org.ow2.jonas.jpaas.util.clouddescriptors.environmenttemplate.EnvironmentTemplateDesc;
import org.ow2.jonas.jpaas.util.clouddescriptors.environmenttemplate.v1.generated.EnvironmentTemplateType;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import javax.naming.Context;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component(immediate=true)
@Instantiate
@Provides
public class ApplicationManagerBean implements ApplicationManager {

    //WARNING !!!! This attribute was added only for dummy tests (it allows the developer to add or remove Applications like in database).
//It has to be removed after the real implementation of all classes.
    private ArrayList<Application> listApplication ;

    /**
     * The logger
     */
    private Log logger = LogFactory.getLog(ApplicationManagerBean.class);

    public Application createApplication(String cloudApplicationDescriptor) throws ApplicationManagerBeanException {
        logger.info("JPAAS-APPLICATION-MANAGER / createApplication called : " + cloudApplicationDescriptor);

        CloudApplicationDesc cloudApplicationDesc =null;
        CloudApplicationType cloudApplication = null;
        try {
            cloudApplicationDesc = new CloudApplicationDesc(cloudApplicationDescriptor);
            cloudApplication = (CloudApplicationType) cloudApplicationDesc.getCloudApplication();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Application app = new Application();
        app.setName(cloudApplication.getName());
        app.setAppId(UUID.randomUUID().toString());
        return app;

    }

    public ApplicationVersion createApplicationVersion(String cloudApplicationVersionDescriptor) throws ApplicationManagerBeanException {
        logger.info("JPAAS-APPLICATION-MANAGER / createApplicationVersion called : " + cloudApplicationVersionDescriptor);


        CloudApplicationDesc cloudApplicationDesc =null;
        CloudApplicationType cloudApplication = null;
        try {
            cloudApplicationDesc = new CloudApplicationDesc(cloudApplicationVersionDescriptor);
            cloudApplication = (CloudApplicationType) cloudApplicationDesc.getCloudApplication();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        ApplicationVersion appVersion = new ApplicationVersion();
        appVersion.setAppId(cloudApplication.getName());
        appVersion.setVersionId(cloudApplication.getVersion());
        appVersion.setVersionLabel(cloudApplication.getVersion());
        return appVersion;

    }

    public void notifyArtefactUploades(String appId, String versionId, String artefactId) {
        //TODO
        System.out.println("JPAAS-APPLICATION-MANAGER / notifyArtefactUploades called");
    }

    public ApplicationVersionInstance createApplicationVersionInstance(String cloudApplicationVersionInstanceDescriptor, String deploymentDescriptor) throws ApplicationManagerBeanException {
        //TODO
        System.out.println("JPAAS-APPLICATION-MANAGER / createApplicationVersionInstance called : " +
                cloudApplicationVersionInstanceDescriptor + "," + deploymentDescriptor);
        CloudApplicationDesc cloudApplicationDesc =null;
        CloudApplicationType cloudApplication = null;

        try {
            cloudApplicationDesc = new CloudApplicationDesc(cloudApplicationVersionInstanceDescriptor);
            cloudApplication = (CloudApplicationType) cloudApplicationDesc.getCloudApplication();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        ApplicationVersionInstance appVersionInstance = new ApplicationVersionInstance();
        appVersionInstance.setAppId(cloudApplication.getName());
        appVersionInstance.setVersionId(cloudApplication.getVersion());
        appVersionInstance.setInstanceId(cloudApplication.getInstance());
        appVersionInstance.setInstanceName(cloudApplication.getInstance());

        return  appVersionInstance;

    }

    public Future<ApplicationVersionInstance> startApplicationVersionInstance(final String appId, final String versionId, final String instanceId) throws ApplicationManagerBeanException {
        System.out.println("JPAAS-APPLICATION-MANAGER / startApplicationVersionInstance called : " + appId + ", "
                + versionId + ", " + instanceId);

        ExecutorService es = Executors.newFixedThreadPool(3);
        final Future<ApplicationVersionInstance> future = es.submit(new Callable<ApplicationVersionInstance>() {
            public ApplicationVersionInstance call() throws Exception {
                ApplicationVersionInstance appVersionInstance = new ApplicationVersionInstance();
                appVersionInstance.setAppId(appId);
                appVersionInstance.setVersionId(versionId);
                appVersionInstance.setInstanceName(instanceId);
                appVersionInstance.setState(ApplicationVersionInstance.INSTANCE_STARTED);

                return appVersionInstance;


            }
        });
        return future;


    }

    public void stopApplicationVersionInstance() {
        //TODO
        System.out.println("JPAAS-APPLICATION-MANAGER / stopApplicationVersionInstance called");
    }

    public List<Application> findApplications() {

        System.out.println("JPAAS-APPLICATION-MANAGER / findApplications called");

        Application app = new Application();
        app.setName("myapp");
        app.setAppId("1");

        List<Application> apps = new ArrayList<Application>();
        apps.add(app);
        return apps;

    }

    public List<ApplicationVersion> findApplicationVersion(String appId) {
        System.out.println("JPAAS-APPLICATION-MANAGER / findApplicationVersion called");

        ApplicationVersion appVersion = new ApplicationVersion();
        appVersion.setAppId("myapp");
        appVersion.setVersionId("V1");
        appVersion.setVersionLabel("V1");

        List<ApplicationVersion> appVersionList = new ArrayList<ApplicationVersion>();
        appVersionList.add(appVersion);

        return appVersionList;
    }

    public List<ApplicationVersionInstance> findApplicationVersionsInstances(String appId, String versionId) {
        System.out.println("JPAAS-APPLICATION-MANAGER / findApplicationVersionsInstances called");

        ApplicationVersionInstance appVersionInstance = new ApplicationVersionInstance();
        appVersionInstance.setAppId("myapp");
        appVersionInstance.setVersionId("V1");
        appVersionInstance.setInstanceName("myinstance");

        List<ApplicationVersionInstance> appVersionInstanceList = new ArrayList<ApplicationVersionInstance>();
        appVersionInstanceList.add(appVersionInstance);

        return appVersionInstanceList;
    }

    public Application getApplication(String appId) {
        System.out.println("JPAAS-APPLICATION-MANAGER / getApplication called");

        Application app = new Application();
        app.setName("myapp");
        app.setAppId("1");

        return app;
    }


    public ApplicationVersion getApplicationVersion(String appId, String versionId) {
        ApplicationVersion appVersion = new ApplicationVersion();
        appVersion.setAppId("myapp");
        appVersion.setVersionId("V1");
        appVersion.setVersionLabel("V1");

        return appVersion;
    }

    public ApplicationVersionInstance getApplicationVersionInstance(String appId, String versionId, String instanceId) {
        System.out.println("JPAAS-APPLICATION-MANAGER / getApplicationVersionInstance called");
        ApplicationVersionInstance appVersionInstance = new ApplicationVersionInstance();
        appVersionInstance.setAppId("myapp");
        appVersionInstance.setVersionId("V1");
        appVersionInstance.setInstanceName("myinstance");

        return appVersionInstance;

    }

    public void deleteApplication(String appId) {
        //TODO
        System.out.println("JPAAS-APPLICATION-MANAGER / deleteApplication called");
    }

    public void deleteApplicationVersion(String appId, String versionId) {
        //TODO
        System.out.println("JPAAS-APPLICATION-MANAGER / deleteApplicationVersion called");
    }

    public void deleteApplicationVersionInstance(String appId, String versionId, String instanceId) {
        //TODO
        System.out.println("JPAAS-APPLICATION-MANAGER / deleteApplicationVersionInstance called");
    }

    public Environment getEnvironment(String appId, String versionId, String instanceId) {
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

    public ArrayList<Application> getListApplication() {
        Application app = new Application();
        app.setName("myapp");
        app.setAppId("1");

        ArrayList<Application> apps = new ArrayList<Application>();
        apps.add(app);
        return apps;
    }
}
