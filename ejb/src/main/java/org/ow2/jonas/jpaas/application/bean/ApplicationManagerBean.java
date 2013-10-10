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

import org.ow2.bonita.facade.ManagementAPI;
import org.ow2.bonita.facade.QueryDefinitionAPI;
import org.ow2.bonita.facade.QueryRuntimeAPI;
import org.ow2.bonita.facade.RuntimeAPI;
import org.ow2.bonita.facade.def.element.BusinessArchive;
import org.ow2.bonita.facade.def.majorElement.ProcessDefinition;
import org.ow2.bonita.facade.exception.InstanceNotFoundException;
import org.ow2.bonita.facade.exception.ProcessNotFoundException;
import org.ow2.bonita.facade.uuid.ProcessDefinitionUUID;
import org.ow2.bonita.facade.uuid.ProcessInstanceUUID;
import org.ow2.bonita.light.LightProcessInstance;
import org.ow2.bonita.util.AccessorUtil;
import org.ow2.bonita.util.BonitaConstants;
import org.ow2.bonita.util.BusinessArchiveFactory;
import org.ow2.bonita.util.SimpleCallbackHandler;
import org.ow2.easybeans.osgi.annotation.OSGiResource;
import org.ow2.jonas.jpaas.application.api.ApplicationManager;
import org.ow2.jonas.jpaas.application.api.ApplicationManagerBeanException;
import org.ow2.jonas.jpaas.manager.api.Application;
import org.ow2.jonas.jpaas.manager.api.ApplicationVersion;
import org.ow2.jonas.jpaas.manager.api.ApplicationVersionInstance;
import org.ow2.jonas.jpaas.manager.api.Deployable;
import org.ow2.jonas.jpaas.manager.api.Environment;
import org.ow2.jonas.jpaas.sr.facade.api.ISrApplicationFacade;
import org.ow2.jonas.jpaas.sr.facade.api.ISrApplicationVersionFacade;
import org.ow2.jonas.jpaas.sr.facade.api.ISrApplicationVersionInstanceFacade;

import org.ow2.jonas.jpaas.sr.facade.vo.ApplicationVO;
import org.ow2.jonas.jpaas.sr.facade.vo.ApplicationVersionInstanceVO;
import org.ow2.jonas.jpaas.sr.facade.vo.ApplicationVersionVO;
import org.ow2.jonas.jpaas.sr.facade.vo.DeployableVO;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.naming.Context;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;




@Stateless(mappedName="ApplicationManagerBean")
@Local(ApplicationManager.class)
@Remote(ApplicationManager.class)
public class ApplicationManagerBean implements ApplicationManager {

    //WARNING !!!! This attribute was added only for dummy tests (it allows the developer to add or remove Applications like in database).
//It has to be removed after the real implementation of all classes.
    private ArrayList<Application> listApplication ;

    /**
     * The logger
     */
    private Log logger = LogFactory.getLog(ApplicationManagerBean.class);

    private QueryDefinitionAPI queryDefinitionAPI;
    private RuntimeAPI runtimeAPI;
    private ManagementAPI managementAPI;
    private QueryRuntimeAPI queryRuntimeAPI;
    private QueryRuntimeAPI queryRuntimeAPIHistory;
    private LoginContext loginContext = null;


    private String processCreateApplication = "CreateApplication--1.0.bar";
    private String processCreateApplicationVersion = "CreateApplicationVersion--1.0.bar";
    private String processCreateApplicationVersionInstance = "CreateApplicationVersionInstance--1.0.bar";
    private String processStartApplicationVersionInstance = "StartApplicationVersionInstance--1.0.bar";
    private String subProcessDeployOnContainer = "DeployOnContainer--1.0.bar";
    private String subProcessCreateLoadBalancer = "CreateLoadBalancer--1.0.bar";
    private String processScaleUp = "ScaleUp--1.0.bar";
    private String subProcessInstanciateConnector = "InstanciateConnector--1.0.bar";
    private String subProcessInstanciateConnectors = "InstanciateConnectors--1.0.bar";
    private String subProcessInstanciateContainer = "InstanciateContainer--1.0.bar";
    private String processScaleDown = "ScaleDown--1.0.bar";


    @OSGiResource
    private ISrApplicationFacade appSR;
    @OSGiResource
    private ISrApplicationVersionFacade appVersionSR;
    @OSGiResource
    private ISrApplicationVersionInstanceFacade appVersionInstanceSR;


    public ApplicationManagerBean() throws ApplicationManagerBeanException {
        login();
        initEnv();
        logout();
    }

    public Application createApplication(String cloudApplicationDescriptor) throws ApplicationManagerBeanException {
        logger.info("JPAAS-APPLICATION-MANAGER / createApplication called : " + cloudApplicationDescriptor);
        final Map param = new HashMap();
        param.put("cloudApplicationDescriptor", cloudApplicationDescriptor);
        // deploy process if necessary
        login();
        ProcessDefinitionUUID uuidProcessCreateApplication = deployProcess(processCreateApplication);
        Application app;
        if (uuidProcessCreateApplication != null) {

            try {
                login();
                ProcessInstanceUUID uuidInstance = runtimeAPI.instantiateProcess(uuidProcessCreateApplication, param);

                // wait until processInstance is finished
                Set<LightProcessInstance> lightProcessInstances = queryRuntimeAPIHistory.getLightProcessInstances();
                waitProcessInstanceUUIDIsFinished(uuidInstance);


                /*
                // read Variable process instance to detect errors
                String variableErrorRouteur =
                        (String) queryRuntimeAPIHistory.getProcessInstanceVariable(uuidInstance, "errorCode");

                if (!variableErrorRouteur.equals("")) {
                    throw new ApplicationManagerBeanException("Error during the process CreateApplication : "
                            + variableErrorRouteur);
                } else {
                    return app;
                }*/

                ApplicationVO appVO = (ApplicationVO)
                        queryRuntimeAPIHistory.getProcessInstanceVariable(uuidInstance, "applicationVO");
                app = new Application();
                app.setName(appVO.getName());
                app.setAppId(appVO.getId());
                app.setDescription(appVO.getDescription());
                //app.setCapabilities(appVO.getCapabilities());
                app.setRequirements(appVO.getRequirements());
                return app;

            } catch (ProcessNotFoundException e) {
                e.printStackTrace();
                throw new ApplicationManagerBeanException("Error during intantiation of the process CreateApplication" +
                        ", process not found");
            } catch (org.ow2.bonita.facade.exception.VariableNotFoundException e) {
                e.printStackTrace();
                throw new ApplicationManagerBeanException("Error during intantiation of the process CreateApplication" +
                        ", variable not found");
            } catch (InstanceNotFoundException e) {
                throw new ApplicationManagerBeanException("Error during intantiation of the process CreateApplication" +
                        ", instance not found");
            } finally {
                logger.info("JPAAS-APPLICATION-MANAGER / createApplication finished.");
                logout();
            }

        } else {
            throw (new ApplicationManagerBeanException("process createApplication can't be deploy on server..."));
        }
    }

    public ApplicationVersion createApplicationVersion(String cloudApplicationVersionDescriptor) throws ApplicationManagerBeanException {
        logger.info("JPAAS-APPLICATION-MANAGER / createApplicationVersion called : " + cloudApplicationVersionDescriptor);
        final Map param = new HashMap();
        param.put("cloudApplicationVersionDescriptor", cloudApplicationVersionDescriptor);
        // deploy process if necessary
        login();
        ProcessDefinitionUUID uuidProcessCreateApplicationVersion = deployProcess(processCreateApplicationVersion);
        ApplicationVersion appVersion;
        if (uuidProcessCreateApplicationVersion != null) {

            try {
                login();
                ProcessInstanceUUID uuidInstance = runtimeAPI.instantiateProcess(uuidProcessCreateApplicationVersion, param);

                // wait until processInstance is finished
                Set<LightProcessInstance> lightProcessInstances = queryRuntimeAPIHistory.getLightProcessInstances();
                waitProcessInstanceUUIDIsFinished(uuidInstance);


                /*
                // read Variable process instance to detect errors
                String variableErrorRouteur =
                        (String) queryRuntimeAPIHistory.getProcessInstanceVariable(uuidInstance, "errorCode");

                if (!variableErrorRouteur.equals("")) {
                    throw new ApplicationManagerBeanException("Error during the process CreateApplicationVersion : "
                            + variableErrorRouteur);
                } else {

                }*/

                ApplicationVersionVO appVersionVO = (ApplicationVersionVO)
                        queryRuntimeAPIHistory.getProcessInstanceVariable(uuidInstance, "applicationVersionVO");
                appVersion = new ApplicationVersion();
                appVersion.setAppId(appVersionVO.getAppId());
                //appVersion.setCapabilities(appVersionVO.getCapabilities());
                appVersion.setRequirements(appVersionVO.getRequirements());
/*                appVersion.setListApplicationVersionInstance(
                        appVersionInstanceVOListToAppVersionInstanceList(
                                appVersionVO.getApplicationVersionInstanceList()));*/
                appVersion.setSortedDeployablesList(deployableVOListToDeployableList(appVersionVO.getDeployableList()));
                appVersion.setVersionId(appVersionVO.getVersionId());
                appVersion.setVersionLabel(appVersionVO.getLabel());
                return appVersion;

            } catch (ProcessNotFoundException e) {
                e.printStackTrace();
                throw new ApplicationManagerBeanException("Error during intantiation of the process" +
                        " CreateApplicationVersion, process not found");
            } catch (org.ow2.bonita.facade.exception.VariableNotFoundException e) {
                e.printStackTrace();
                throw new ApplicationManagerBeanException("Error during intantiation of the process" +
                        " CreateApplicationVersion, variable not found");
            } catch (InstanceNotFoundException e) {
                throw new ApplicationManagerBeanException("Error during intantiation of the process" +
                        " CreateApplicationVersion, instance not found");
            } finally {
                logger.info("JPAAS-APPLICATION-MANAGER / createApplicationVersion finished.");
                logout();
            }

        } else {
            throw (new ApplicationManagerBeanException("process createApplicationVersion can't be deploy on server..."));
        }
    }

    public void notifyArtefactUploades(String appId, String versionId, String artefactId) {
        //TODO
        System.out.println("JPAAS-APPLICATION-MANAGER / notifyArtefactUploades called");
    }

    public ApplicationVersionInstance createApplicationVersionInstance(String cloudApplicationVersionInstanceDescriptor, String deploymentDescriptor) throws ApplicationManagerBeanException {
        //TODO
        System.out.println("JPAAS-APPLICATION-MANAGER / createApplicationVersionInstance called : " +
                cloudApplicationVersionInstanceDescriptor + "," + deploymentDescriptor);
        final Map param = new HashMap();
        param.put("cloudApplicationVersionInstanceDescriptor", cloudApplicationVersionInstanceDescriptor);
        param.put("deploymentDescriptor", deploymentDescriptor);
        // deploy process if necessary
        login();
        ProcessDefinitionUUID uuidProcessCreateApplicationVersionInstance = deployProcess(processCreateApplicationVersionInstance);
        ApplicationVersionInstance appVersionInstance;
        if (uuidProcessCreateApplicationVersionInstance != null) {

            try {
                login();
                ProcessInstanceUUID uuidInstance = runtimeAPI.instantiateProcess(uuidProcessCreateApplicationVersionInstance, param);

                // wait until processInstance is finished
                Set<LightProcessInstance> lightProcessInstances = queryRuntimeAPIHistory.getLightProcessInstances();
                waitProcessInstanceUUIDIsFinished(uuidInstance);


                /*
                // read Variable process instance to detect errors
                String variableErrorRouteur =
                        (String) queryRuntimeAPIHistory.getProcessInstanceVariable(uuidInstance, "errorCode");

                if (!variableErrorRouteur.equals("")) {
                    throw new ApplicationManagerBeanException("Error during the process CreateApplicationVersionInstance : "
                            + variableErrorRouteur);
                } else {

                }*/

                ApplicationVersionInstanceVO appVersionInstanceVO = (ApplicationVersionInstanceVO)
                        queryRuntimeAPIHistory.getProcessInstanceVariable(uuidInstance, "applicationVersionInstanceVO");
                appVersionInstance = new ApplicationVersionInstance();
                appVersionInstance.setInstanceId(appVersionInstanceVO.getId());
                appVersionInstance.setAppId(appVersionInstanceVO.getAppId());
                appVersionInstance.setVersionId(appVersionInstanceVO.getVersionId());
                appVersionInstance.setInstanceName(appVersionInstanceVO.getName());
                appVersionInstance.setSortedDeployablesList(deployableVOListToDeployableList(appVersionInstanceVO.getDeployableList()));
                //appVersionInstance.setState(appVersionInstanceVO.getState());
                return appVersionInstance;

            } catch (ProcessNotFoundException e) {
                e.printStackTrace();
                throw new ApplicationManagerBeanException("Error during intantiation of the process" +
                        " CreateApplicationVersionInstance, process not found");
            } catch (org.ow2.bonita.facade.exception.VariableNotFoundException e) {
                e.printStackTrace();
                throw new ApplicationManagerBeanException("Error during intantiation of the process" +
                        " CreateApplicationVersionInstance, variable not found");
            } catch (InstanceNotFoundException e) {
                throw new ApplicationManagerBeanException("Error during intantiation of the process" +
                        " CreateApplicationVersionInstance, instance not found");
            } finally {
                logger.info("JPAAS-APPLICATION-MANAGER / createApplicationVersionInstance finished.");
                logout();
            }

        } else {
            throw (new ApplicationManagerBeanException("process createApplicationVersionInstance can't be " +
                    "deploy on server..."));
        }
    }

    public Future<ApplicationVersionInstance> startApplicationVersionInstance(String appId, String versionId, String instanceId) throws ApplicationManagerBeanException {
        System.out.println("JPAAS-APPLICATION-MANAGER / startApplicationVersionInstance called : " + appId + ", "
                + versionId + ", " + instanceId);
        final Map param = new HashMap();
        param.put("appId", appId);
        param.put("versionId", versionId);
        param.put("instanceId", instanceId);
        // deploy process if necessary
        login();
        deployProcess(subProcessDeployOnContainer);
        deployProcess(subProcessCreateLoadBalancer);
        final ProcessDefinitionUUID uuidProcessprocessStartApplicationVersionInstance =
                deployProcess(processStartApplicationVersionInstance);

        if (uuidProcessprocessStartApplicationVersionInstance != null) {
            ExecutorService es = Executors.newFixedThreadPool(3);
            final Future<ApplicationVersionInstance> future = es.submit(new Callable<ApplicationVersionInstance>() {
                public ApplicationVersionInstance call() throws Exception {
                    try {
                        login();
                        ProcessInstanceUUID uuidInstance =
                                runtimeAPI.instantiateProcess(uuidProcessprocessStartApplicationVersionInstance, param);

                        // wait until processInstance is finished
                        Set<LightProcessInstance> lightProcessInstances =
                                queryRuntimeAPIHistory.getLightProcessInstances();
                        waitProcessInstanceUUIDIsFinished(uuidInstance);


                        /*
                        // read Variable process instance to detect errors
                        String variableErrorRouteur =
                                (String) queryRuntimeAPIHistory.getProcessInstanceVariable(uuidInstance, "errorCode");

                        if (!variableErrorRouteur.equals("")) {
                            throw new ApplicationManagerBeanException("Error during the process CreateApplicationVersionInstance : "
                                    + variableErrorRouteur);
                        } else {

                        }*/

                        return null;

                    } catch (ProcessNotFoundException e) {
                        e.printStackTrace();
                        throw new ApplicationManagerBeanException("Error during intantiation of the process" +
                                " startApplicationVersionInstance, process not found");
                    } catch (org.ow2.bonita.facade.exception.VariableNotFoundException e) {
                        e.printStackTrace();
                        throw new ApplicationManagerBeanException("Error during intantiation of the process" +
                                " startApplicationVersionInstance, variable not found");
                    } finally {
                        logger.info("JPAAS-APPLICATION-MANAGER / startApplicationVersionInstance finished.");
                        logout();
                    }
                }
            });
            return future;
        } else {
            throw (new ApplicationManagerBeanException("process startApplicationVersionInstance can't be " +
                    "deploy on server..."));
        }
    }

    public void stopApplicationVersionInstance() {
        //TODO
        System.out.println("JPAAS-APPLICATION-MANAGER / stopApplicationVersionInstance called");
    }

    public List<Application> findApplications() {

        System.out.println("JPAAS-APPLICATION-MANAGER / findApplications called");

        List<ApplicationVO> appVOList = appSR.findApplications("1");

        List <Application> appList = createApplicationList(appVOList);

        return appList;

    }

    public List<ApplicationVersion> findApplicationVersion(String appId) {
        System.out.println("JPAAS-APPLICATION-MANAGER / findApplicationVersion called");
        List<ApplicationVersionVO> appVersionVOList = appSR.getApplication(appId).getApplicationVersionList();

        List <ApplicationVersion> appVersionList = createApplicationVersionList(appVersionVOList);

        return appVersionList;
    }

    public List<ApplicationVersionInstance> findApplicationVersionsInstances(String appId, String versionId) {
        System.out.println("JPAAS-APPLICATION-MANAGER / findApplicationVersionsInstances called");

        List<ApplicationVersionInstance> appVersionInstanceList = null;

        List<ApplicationVersionVO> appVersionVOList = appSR.getApplication(appId).getApplicationVersionList();

        for (ApplicationVersionVO appVersionVO:appVersionVOList) {

            if (appVersionVO.getVersionId().equals(versionId)) {
                appVersionInstanceList = createApplicationVersionInstanceList(appVersionVO.getApplicationVersionInstanceList());

            }
        }

        return appVersionInstanceList;
    }

    public Application getApplication(String appId) {
        System.out.println("JPAAS-APPLICATION-MANAGER / getApplication called");

        ApplicationVO appVO = appSR.getApplication(appId);

        return createApplication(appVO);
    }


    public ApplicationVersion getApplicationVersion(String appId, String versionId) {
        System.out.println("JPAAS-APPLICATION-MANAGER / getApplicationVersion called");
        List<ApplicationVersionVO> appVersionVOList = appSR.getApplication(appId).getApplicationVersionList();

        for (ApplicationVersionVO appVersionVO:appVersionVOList) {

            if (appVersionVO.getVersionId().equals(versionId)) {
                return createApplicationVersion(appVersionVO);

            }
        }

        return null;
    }

    public ApplicationVersionInstance getApplicationVersionInstance(String appId, String versionId, String instanceId) {
        System.out.println("JPAAS-APPLICATION-MANAGER / getApplicationVersionInstance called");
        List<ApplicationVersionVO> appVersionVOList = appSR.getApplication(appId).getApplicationVersionList();

        for (ApplicationVersionVO appVersionVO:appVersionVOList) {

            if (appVersionVO.getVersionId().equals(versionId)) {

                for (ApplicationVersionInstanceVO appVersionInstanceVO:appVersionVO.getApplicationVersionInstanceList()) {

                    if (appVersionInstanceVO.getId().equals(instanceId)) {
                        return createApplicationVersionInstance(appVersionInstanceVO);
                    }
                }

            }
        }
        return null;

    }

    private List<Application> createApplicationList(List <ApplicationVO> appVOList) {
        List<Application> appList = new ArrayList<Application>();

        for (ApplicationVO appVO:appVOList) {
            Application app = createApplication(appVO);
            appList.add(app);
        }

        return appList;
    }

    private Application createApplication(ApplicationVO appVO) {
        Application app = new Application();
        app.setAppId(appVO.getId());
        app.setDescription(appVO.getDescription());
        app.setName(appVO.getName());
        app.setRequirements(appVO.getRequirements());

        app.setListApplicationVersion(createApplicationVersionList(appVO.getApplicationVersionList()));
        return app;
    }


    private List<ApplicationVersion> createApplicationVersionList(List <ApplicationVersionVO> appVersionVOList) {

        List<ApplicationVersion> appVersionList = new ArrayList<ApplicationVersion>();

        for (ApplicationVersionVO appVersionVO : appVersionVOList) {

            ApplicationVersion appVersion = createApplicationVersion(appVersionVO);
            appVersionList.add(appVersion);

        }
        return appVersionList;
    }

    private ApplicationVersion createApplicationVersion(ApplicationVersionVO appVersionVO) {

        ApplicationVersion appVersion = new ApplicationVersion();
        appVersion.setAppId(appVersionVO.getAppId());
        appVersion.setVersionId(appVersionVO.getVersionId());
        appVersion.setVersionLabel(appVersionVO.getLabel());
        appVersion.setRequirements(appVersionVO.getRequirements());

        for  (DeployableVO deployableVO : appVersionVO.getDeployableList()) {

            Deployable deployable = new Deployable();
            deployable.setDeployabledId(deployableVO.getId());
            deployable.setDeployableName(deployableVO.getName());
            deployable.setLocationUrl(deployableVO.getUrl());
            deployable.setRequirements(deployableVO.getRequirements());

            appVersion.getSortedDeployablesList().add(deployable);
        }

        appVersion.setListApplicationVersionInstance(createApplicationVersionInstanceList(appVersionVO.getApplicationVersionInstanceList()));

        return appVersion;

    }


    private List<ApplicationVersionInstance> createApplicationVersionInstanceList(List <ApplicationVersionInstanceVO> appVersionInstanceVOList) {
        List<ApplicationVersionInstance> appVersionInstanceList = new ArrayList<ApplicationVersionInstance>();

        for (ApplicationVersionInstanceVO appVersionInstanceVO : appVersionInstanceVOList) {
            ApplicationVersionInstance appVersionInstance = createApplicationVersionInstance(appVersionInstanceVO);
            appVersionInstanceList.add(appVersionInstance);

        }
        return appVersionInstanceList;
    }


    private ApplicationVersionInstance createApplicationVersionInstance(ApplicationVersionInstanceVO appVersionInstanceVO) {

        ApplicationVersionInstance appVersionInstance = new ApplicationVersionInstance();

        appVersionInstance.setAppId(appVersionInstanceVO.getAppId());
        appVersionInstance.setInstanceId(appVersionInstanceVO.getId());
        appVersionInstance.setVersionId(appVersionInstanceVO.getVersionId());
        appVersionInstance.setStateStr(appVersionInstanceVO.getState());

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
        final Map param = new HashMap();
        param.put("appId", appId);
        param.put("versionId", versionId);
        param.put("instanceId", instanceId);
        // deploy process if necessary
        login();
        deployProcess(subProcessDeployOnContainer);
        deployProcess(subProcessCreateLoadBalancer);
        deployProcess(subProcessInstanciateConnector);
        deployProcess(subProcessInstanciateConnectors);
        deployProcess(subProcessInstanciateContainer);

        final ProcessDefinitionUUID uuidProcessScaleUp = deployProcess(processScaleUp);

        if (uuidProcessScaleUp != null) {
            ExecutorService es = Executors.newFixedThreadPool(3);
            final Future<ApplicationVersionInstance> future = es.submit(new Callable<ApplicationVersionInstance>() {
                public ApplicationVersionInstance call() throws Exception {
                    try {
                        login();
                        ProcessInstanceUUID uuidInstance =
                                runtimeAPI.instantiateProcess(uuidProcessScaleUp, param);

                        // wait until processInstance is finished
                        Set<LightProcessInstance> lightProcessInstances =
                                queryRuntimeAPIHistory.getLightProcessInstances();
                        waitProcessInstanceUUIDIsFinished(uuidInstance);


                        /*
                        // read Variable process instance to detect errors
                        String variableErrorRouteur =
                                (String) queryRuntimeAPIHistory.getProcessInstanceVariable(uuidInstance, "errorCode");

                        if (!variableErrorRouteur.equals("")) {
                            throw new ApplicationManagerBeanException("Error during the process CreateApplicationVersionInstance : "
                                    + variableErrorRouteur);
                        } else {

                        }*/

                        return null;

                    } catch (ProcessNotFoundException e) {
                        e.printStackTrace();
                        throw new ApplicationManagerBeanException("Error during intantiation of the process" +
                                " ScaleUp, process not found");
                    } catch (org.ow2.bonita.facade.exception.VariableNotFoundException e) {
                        e.printStackTrace();
                        throw new ApplicationManagerBeanException("Error during intantiation of the process" +
                                " ScaleUp, variable not found");
                    } finally {
                        logger.info("JPAAS-APPLICATION-MANAGER / ScaleUp finished.");
                        logout();
                    }
                }
            });
            return future;
        } else {
            throw (new ApplicationManagerBeanException("process ScaleUp can't be " +
                    "deploy on server..."));
        }
    }

    public Future<ApplicationVersionInstance> scaleDown(String appId, String versionId, String instanceId)
            throws ApplicationManagerBeanException {
        System.out.println("JPAAS-APPLICATION-MANAGER / ScaleDown called : " + appId + ", "
                + versionId + ", " + instanceId);
        final Map param = new HashMap();
        param.put("appId", appId);
        param.put("versionId", versionId);
        param.put("instanceId", instanceId);
        // deploy process if necessary
        login();
        final ProcessDefinitionUUID uuidProcessScaleDown = deployProcess(processScaleDown);

        if (uuidProcessScaleDown != null) {
            ExecutorService es = Executors.newFixedThreadPool(3);
            final Future<ApplicationVersionInstance> future = es.submit(new Callable<ApplicationVersionInstance>() {
                public ApplicationVersionInstance call() throws Exception {
                    try {
                        login();
                        ProcessInstanceUUID uuidInstance =
                                runtimeAPI.instantiateProcess(uuidProcessScaleDown, param);

                        // wait until processInstance is finished
                        Set<LightProcessInstance> lightProcessInstances =
                                queryRuntimeAPIHistory.getLightProcessInstances();
                        waitProcessInstanceUUIDIsFinished(uuidInstance);


                        /*
                        // read Variable process instance to detect errors
                        String variableErrorRouteur =
                                (String) queryRuntimeAPIHistory.getProcessInstanceVariable(uuidInstance, "errorCode");

                        if (!variableErrorRouteur.equals("")) {
                            throw new ApplicationManagerBeanException("Error during the process CreateApplicationVersionInstance : "
                                    + variableErrorRouteur);
                        } else {

                        }*/

                        return null;

                    } catch (ProcessNotFoundException e) {
                        e.printStackTrace();
                        throw new ApplicationManagerBeanException("Error during intantiation of the process" +
                                " ScaleDown, process not found");
                    } catch (org.ow2.bonita.facade.exception.VariableNotFoundException e) {
                        e.printStackTrace();
                        throw new ApplicationManagerBeanException("Error during intantiation of the process" +
                                " ScaleDown, variable not found");
                    } finally {
                        logger.info("JPAAS-APPLICATION-MANAGER / ScaleDown finished.");
                        logout();
                    }
                }
            });
            return future;
        } else {
            throw (new ApplicationManagerBeanException("process ScaleDown can't be " +
                    "deploy on server..."));
        }
    }

    public ArrayList<Application> getListApplication() {
        return listApplication;
    }

    public void setListApplication(ArrayList<Application> listApplication) {
        this.listApplication = listApplication;
    }

    private ProcessDefinitionUUID deployProcess(String processName) {
        final File tempFileBarProcess;
        ProcessDefinitionUUID result = null;
        try {
            URL processBar = ApplicationManagerBean.class.getClassLoader().getResource(processName);
            tempFileBarProcess = createTempFileBar(processBar);
            BusinessArchive businessArchive = BusinessArchiveFactory.getBusinessArchive(tempFileBarProcess);
            result = deployBarFile(businessArchive);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ApplicationManagerBeanException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private File createTempFileBar(URL processBar) throws IOException {
        File tempFile = null;
        tempFile = File.createTempFile("MyProcess", ".bar");   // empty file
        tempFile.deleteOnExit();
        java.io.FileOutputStream destinationFile = new java.io.FileOutputStream(tempFile);
        InputStream processBarStream = processBar.openStream();
        try {
            // 0.5Mo
            byte buffer[] = new byte[512 * 1024];
            int nbLecture;
            while ((nbLecture = processBarStream.read(buffer)) != -1) {
                destinationFile.write(buffer, 0, nbLecture);
            }
        } finally {
            destinationFile.close();
        }
        return (tempFile);
    }

    private ProcessDefinitionUUID deployBarFile(BusinessArchive businessArchive) throws Exception {
        try {
            ProcessDefinition p = queryDefinitionAPI.getProcess(businessArchive.getProcessDefinition().getUUID());
            return p.getUUID();
        } catch (ProcessNotFoundException e) {
            logger.info("Deploy the process " + businessArchive.getProcessDefinition().getName());
            final ProcessDefinition process = managementAPI.deploy(businessArchive); //deployJar
            return process.getUUID();
        }
    }

    private void initEnv() throws ApplicationManagerBeanException {
        String DEFAULT_INITIAL_CONTEXT_FACTORY = "org.objectweb.carol.jndi.spi.MultiOrbInitialContextFactory";

        System.setProperty(BonitaConstants.API_TYPE_PROPERTY, "EJB3");
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_INITIAL_CONTEXT_FACTORY);
        queryDefinitionAPI = AccessorUtil.getQueryDefinitionAPI();
        runtimeAPI = AccessorUtil.getRuntimeAPI();
        managementAPI = AccessorUtil.getManagementAPI();
        queryRuntimeAPI = AccessorUtil.getQueryRuntimeAPI();
        queryRuntimeAPIHistory = AccessorUtil.getQueryRuntimeAPI(AccessorUtil.QUERYLIST_HISTORY_KEY);
    }

    private void login() throws ApplicationManagerBeanException {
        String login = "admin";
        String password = "bpm";
        try {
            if (loginContext == null) {
                loginContext = new LoginContext("BonitaStore", new SimpleCallbackHandler(login, password));
            }
            if (loginContext == null) {
                throw (new ApplicationManagerBeanException("Error during login with login:" + login + "and password:" + password));
            } else {
                loginContext.login();
            }
        } catch (LoginException e) {
            e.printStackTrace();
            throw (new ApplicationManagerBeanException("Error during login with login:" + login + "and password:" + password));
        }
    }

    private void logout() throws ApplicationManagerBeanException {
        try {
            if (loginContext != null) {
                loginContext.logout();
                loginContext = null;
            } else
                throw (new ApplicationManagerBeanException("Error during logout. The loginContext is null"));
        } catch (LoginException e) {
            e.printStackTrace();
            throw (new ApplicationManagerBeanException("Error during logout : loginException"));
        }
    }

    private void waitProcessInstanceUUIDIsFinished(ProcessInstanceUUID uuidInstance) {
        Set<LightProcessInstance> lightProcessInstances = queryRuntimeAPIHistory.getLightProcessInstances();
        Iterator iter = lightProcessInstances.iterator();
        LightProcessInstance processInstanceCurrent = null;
        boolean processExist = false;
        while (!processExist) {
            if (!iter.hasNext()) {
                lightProcessInstances = queryRuntimeAPIHistory.getLightProcessInstances();
                iter = lightProcessInstances.iterator();
            }
            if (iter.hasNext()) {
                processInstanceCurrent = (LightProcessInstance) iter.next();

                if (processInstanceCurrent.getProcessInstanceUUID().equals(uuidInstance))
                    processExist = true;
            }
        }

    }

    private List<Deployable> deployableVOListToDeployableList(List<DeployableVO> deployableVOList) {
        List<Deployable> resultList = new ArrayList<Deployable>();
        for (DeployableVO tmp : deployableVOList) {
            Deployable deployable = new Deployable();
            deployable.setDeployabledId(tmp.getId());
            deployable.setDeployableName(tmp.getName());
            deployable.setLocationUrl(tmp.getUrl());
            deployable.setRequirements(tmp.getRequirements());
            //deployable.setSlaEnforcement(tmp.getSlaEnforcement());
            deployable.setUploaded(tmp.isUploaded());
            resultList.add(deployable);
        }
        return resultList;
    }

    private List<ApplicationVersionInstance> appVersionInstanceVOListToAppVersionInstanceList(
            List<ApplicationVersionInstanceVO> applicationVersionInstanceVOList) {
        List<ApplicationVersionInstance> resultList = new ArrayList<ApplicationVersionInstance>();
        for (ApplicationVersionInstanceVO tmp : applicationVersionInstanceVOList) {
            ApplicationVersionInstance applicationVersionInstance = new ApplicationVersionInstance();
            applicationVersionInstance.setInstanceId(tmp.getId());
            applicationVersionInstance.setInstanceName(tmp.getName());
            applicationVersionInstance.setAppId(tmp.getAppId());
            applicationVersionInstance.setVersionId(tmp.getVersionId());
            //applicationVersionInstance.setState(tmp.getState());
            resultList.add(applicationVersionInstance);
        }
        return resultList;
    }

}
