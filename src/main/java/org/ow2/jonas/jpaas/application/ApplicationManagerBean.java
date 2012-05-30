package org.ow2.jonas.jpaas.application;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateful;
import java.util.List;

@Stateful
@Local(ApplicationManagerLocal.class)
@Remote(ApplicationManagerRemote.class)
public class ApplicationManagerBean {

  public ApplicationManagerBean() {
  }

  public void createApplication(String cloudApplicationDescritor) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / createApplication called");
  }

  public void createApplicationVersion(String cloudApplicationVersionDescriptor) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / createApplicationVersion called");
  }

  public void notifyArtefactUploades(String appId, String versionId, String artefactId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / notifyArtefactUploades called");
  }

  public void createApplicationVersionInstance(String cloudApplicationVersionInstanceDescriptor, String deploymentDescriptor) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / createApplicationVersionInstance called");
  }

  public void startApplicationVersionInstance(String appId, String instanceId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / startApplicationVersionInstance called");
  }

  public void stopApplicationVersionInstance() {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / stopApplicationVersionInstance called");
  }

  public List findApplications() {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / findApplications called");
    return null;
  }

  public List findApplicationVersion(String appId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / findApplicationVersion called");
    return null;
  }

  public List findApplicationVersionsInstances(String appId, String versionId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / findApplicationVersionsInstances called");
    return null;
  }

  public void getApplication(String appId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / getApplication called");
  }

  public void getApplicationVersion(String appId, String versionId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / getApplicationVersion called");
  }

  public void getApplicationVersionInstance(String appId, String versionId, String instanceId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / getApplicationVersionInstance called");
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

  public void getEnvironment(String appId, String versionId, String instanceId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / getEnvironment called");
  }

}
