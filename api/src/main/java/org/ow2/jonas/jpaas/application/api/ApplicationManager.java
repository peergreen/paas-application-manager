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
package org.ow2.jonas.jpaas.application.api;

import org.ow2.jonas.jpaas.manager.api.Application;
import org.ow2.jonas.jpaas.manager.api.ApplicationVersion;
import org.ow2.jonas.jpaas.manager.api.ApplicationVersionInstance;
import org.ow2.jonas.jpaas.manager.api.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public interface ApplicationManager {

  public Application createApplication(String cloudApplicationDescritor) throws ApplicationManagerBeanException;
  public ApplicationVersion createApplicationVersion(String appId, String cloudApplicationVersionDescriptor) throws ApplicationManagerBeanException;
  public void notifyArtefactUploades(String appId, String versionId, String artefactId);
  public ApplicationVersionInstance createApplicationVersionInstance(String appId, String versionId, String cloudApplicationVersionInstanceDescriptor, String deploymentDescriptor) throws ApplicationManagerBeanException;

  public Future<ApplicationVersionInstance> startApplicationVersionInstance(String appId, String versionId, String instanceId) throws ApplicationManagerBeanException;
  public Future<ApplicationVersionInstance> stopApplicationVersionInstance(String appId, String versionId, String instanceId) throws ApplicationManagerBeanException;

  public List<Application> findApplications();
  public List<ApplicationVersion> findApplicationVersion(String appId);
  public List<ApplicationVersionInstance> findApplicationVersionsInstances(String appId, String versionId);

  public Application getApplication(String appId);
  public ApplicationVersion getApplicationVersion(String appId, String versionId);
  public ApplicationVersionInstance getApplicationVersionInstance(String appId, String versionId, String instanceId);

  public void deleteApplication(String appId) throws ApplicationManagerBeanException;
  public void deleteApplicationVersion(String appId, String versionId) throws ApplicationManagerBeanException;
  public void deleteApplicationVersionInstance(String appId, String versionId, String instanceId) throws ApplicationManagerBeanException;

  public Environment getEnvironment(String appId, String versionId, String instanceId);

   //Temporary to test ScaleUp
   public Future<ApplicationVersionInstance> scaleUp(String appId, String versionId, String instanceId) throws ApplicationManagerBeanException;
   public Future<ApplicationVersionInstance> scaleDown(String appId, String versionId, String instanceId) throws ApplicationManagerBeanException;
}
