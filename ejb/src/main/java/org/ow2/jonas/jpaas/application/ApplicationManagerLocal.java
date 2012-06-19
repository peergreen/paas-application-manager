/**
 * JPaaS Util
 * Copyright (C) 2012 Bull S.A.S.
 * Contact: jasmine@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id$
 * --------------------------------------------------------------------------
 */
package org.ow2.jonas.jpaas.application;

import org.ow2.jonas.jpaas.manager.api.Application;
import org.ow2.jonas.jpaas.manager.api.ApplicationVersion;
import org.ow2.jonas.jpaas.manager.api.ApplicationVersionInstance;
import org.ow2.jonas.jpaas.manager.api.Environment;

import java.util.List;
import java.util.concurrent.Future;

public interface ApplicationManagerLocal {

  public Application createApplication(String cloudApplicationDescritor);
  public ApplicationVersion createApplicationVersion(String cloudApplicationVersionDescriptor);
  public void notifyArtefactUploades(String appId, String versionId, String artefactId);
  public ApplicationVersionInstance createApplicationVersionInstance(String cloudApplicationVersionInstanceDescriptor, String deploymentDescriptor);
  public Future<ApplicationVersionInstance> startApplicationVersionInstance(String appId, String versionId, String instanceId);
  public void stopApplicationVersionInstance();
  public List<Application> findApplications();
  public List<ApplicationVersion> findApplicationVersion(String appId);
  public List<ApplicationVersionInstance> findApplicationVersionsInstances(String appId, String versionId);
  public Application getApplication(String appId);
  public ApplicationVersion getApplicationVersion(String appId, String versionId);
  public ApplicationVersionInstance getApplicationVersionInstance(String appId, String versionId, String instanceId);
  public void deleteApplication(String appId);
  public void deleteApplicationVersion(String appId, String versionId);
  public void deleteApplicationVersionInstance(String appId, String versionId, String instanceId);
  public Environment getEnvironment(String appId, String versionId, String instanceId);
}
