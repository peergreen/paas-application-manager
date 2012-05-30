package org.ow2.jonas.jpaas.application;


import java.util.List;

public interface ApplicationManagerLocal {
  public void createApplication(String cloudApplicationDescritor);
  public void createApplicationVersion(String cloudApplicationVersionDescriptor);
  public void notifyArtefactUploades(String appId, String versionId, String artefactId);
  public void createApplicationVersionInstance(String cloudApplicationVersionInstanceDescriptor, String deploymentDescriptor);
  public void startApplicationVersionInstance(String appId, String instanceId);
  public void stopApplicationVersionInstance();
  public List findApplications();
  public List findApplicationVersion(String appId);
  public List findApplicationVersionsInstances(String appId, String versionId);
  public void getApplication(String appId);
  public void getApplicationVersion(String appId, String versionId);
  public void getApplicationVersionInstance(String appId, String versionId, String instanceId);
  public void deleteApplication(String appId);
  public void deleteApplicationVersion(String appId, String versionId);
  public void deleteApplicationVersionInstance(String appId, String versionId, String instanceId);
  public void getEnvironment(String appId, String versionId, String instanceId);
}
