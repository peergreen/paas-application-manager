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

import org.ow2.jonas.jpaas.api.Application;
import org.ow2.jonas.jpaas.api.ApplicationVersion;
import org.ow2.jonas.jpaas.api.ApplicationVersionInstance;
import org.ow2.jonas.jpaas.api.Environment;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import java.util.List;
import java.util.concurrent.Future;

@Stateful
@Local(ApplicationManagerLocal.class)
@Remote(ApplicationManagerRemote.class)
public class ApplicationManagerBean {

  public ApplicationManagerBean() {
  }

  public Application createApplication(String cloudApplicationDescritor) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / createApplication called");
    return null;
  }

  public ApplicationVersion createApplicationVersion(String cloudApplicationVersionDescriptor) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / createApplicationVersion called");
    return null;
  }

  public void notifyArtefactUploades(String appId, String versionId, String artefactId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / notifyArtefactUploades called");
  }

  public ApplicationVersionInstance createApplicationVersionInstance(String cloudApplicationVersionInstanceDescriptor, String deploymentDescriptor) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / createApplicationVersionInstance called");
    return null;
  }

  public Future<ApplicationVersionInstance> startApplicationVersionInstance(String appId, String versionId, String instanceId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / startApplicationVersionInstance called");
    return null;
  }

  public void stopApplicationVersionInstance() {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / stopApplicationVersionInstance called");
  }

  public List<Application> findApplications() {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / findApplications called");
    return null;
  }

  public List<ApplicationVersion> findApplicationVersion(String appId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / findApplicationVersion called");
    return null;
  }

  public List<ApplicationVersionInstance> findApplicationVersionsInstances(String appId, String versionId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / findApplicationVersionsInstances called");
    return null;
  }

  public Application getApplication(String appId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / getApplication called");
    return null;
  }

  public ApplicationVersion getApplicationVersion(String appId, String versionId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / getApplicationVersion called");
    return null;
  }

  public ApplicationVersionInstance getApplicationVersionInstance(String appId, String versionId, String instanceId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / getApplicationVersionInstance called");
    return null;
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

}
