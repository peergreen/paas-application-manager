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
package org.ow2.jonas.jpaas.application.impl;

import org.ow2.jonas.jpaas.application.api.ApplicationManager;
import org.ow2.jonas.jpaas.manager.api.Application;
import org.ow2.jonas.jpaas.manager.api.ApplicationVersion;
import org.ow2.jonas.jpaas.manager.api.ApplicationVersionInstance;
import org.ow2.jonas.jpaas.manager.api.Deployable;
import org.ow2.jonas.jpaas.manager.api.Environment;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

@Stateless(mappedName="jpaas/application-manager")
@Local(ApplicationManager.class)
@Remote(ApplicationManager.class)
public class ApplicationManagerBean implements ApplicationManager {

//WARNING !!!! This attribute was added only for dummy tests (it allows the developer to add or remove Applications like in database).
//It has to be removed after the real implementation of all classes.
private ArrayList<Application> listApplication ;



public ApplicationManagerBean() {
  }

  public Application createApplication(String cloudApplicationDescritor) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / createApplication called : " +  cloudApplicationDescritor);
    Application app = new Application();
    app.setDescription("JPAAS-APPLICATION-MANAGER during createApplication");
    try {
      Thread . currentThread (). sleep (( int) Math . random ()*2000);
    } catch (InterruptedException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    finally {
       System.out.println("JPAAS-APPLICATION-MANAGER / createApplication finished : ");
       return app;
    }
  }

  public ApplicationVersion createApplicationVersion(String cloudApplicationVersionDescriptor) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / createApplicationVersion called : " + cloudApplicationVersionDescriptor);
 try {
      Thread . currentThread (). sleep (( int) Math . random ()*2000);
    } catch (InterruptedException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    finally {
       System.out.println("JPAAS-APPLICATION-MANAGER / createApplicationVersion finished : ");
       return null;
    }
  }

  public void notifyArtefactUploades(String appId, String versionId, String artefactId) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / notifyArtefactUploades called");
  }

  public ApplicationVersionInstance createApplicationVersionInstance(String cloudApplicationVersionInstanceDescriptor, String deploymentDescriptor) {
    //TODO
    System.out.println("JPAAS-APPLICATION-MANAGER / createApplicationVersionInstance called : " + cloudApplicationVersionInstanceDescriptor + deploymentDescriptor);
     try {
      Thread . currentThread (). sleep (( int) Math . random ()*2000);
    } catch (InterruptedException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    finally {
       System.out.println("JPAAS-APPLICATION-MANAGER / createApplicationVersionInstance finished : ");
       return null;
    }
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
	
	//Test code that returns a list of Applications instead null
	
	this.listApplication= new ArrayList<Application>();
	//Application 1
	Application app1=new Application();
	app1.setAppId("23645");
	app1.setName("My first application");
	ArrayList<String> requirements1= new ArrayList<String>();
	requirements1.add("requirement1");
	requirements1.add("requirement2");
	app1.setRequirements(requirements1);
	
		//Version 1 of Application 1
		ApplicationVersion version1 = new ApplicationVersion();
		version1.setVersionId("23462");
		version1.setVersionLabel("Version 1 of Application  1");
		version1.setAppId("23645");
	
	        //Instance 1 of Version 1
			ApplicationVersionInstance instance1=new ApplicationVersionInstance();
			instance1.setInstanceId("196");
			instance1.setAppId("23645");
			instance1.setVersionId("23462");
			instance1.setInstanceName("1rst instance of the 1rst version of the 1rst application");
			instance1.setState(ApplicationVersionInstance.INSTANCE_STARTED);
	
		version1.getListApplicationVersionInstance().add(instance1);
	        
		    //Deployable 1 of Version 1
		    Deployable dep1= new Deployable();
		    dep1.setDeployabledId("5024657");
		    dep1.setDeployableName("my war");
		    dep1.setUploaded(true);
		    
		    
		    //Deployable 2 of Version 1
		    Deployable dep2= new Deployable();
		    dep2.setDeployabledId("631256");
		    dep2.setDeployableName("my ear");
		    dep2.setUploaded(true);
		
		version1.getSortedDeployablesList().add(dep1);
		version1.getSortedDeployablesList().add(dep2);
		
		
		
		//Version 2 of Application 1
		ApplicationVersion version2 = new ApplicationVersion();
		version2.setVersionId("635478");
		version2.setVersionLabel("Version 2 of Application  1");
			
			//Instance 1 of Version 2
			ApplicationVersionInstance instance2=new ApplicationVersionInstance();
			instance2.setInstanceId("638");
			instance2.setAppId("23645");
			instance2.setVersionId("635478");
			instance2.setInstanceName("2nd instance of the 2 nd version of the 1srt application");
			instance2.setState(ApplicationVersionInstance.INSTANCE_RUNNING);
	    
		version2.getListApplicationVersionInstance().add(instance2);
		
			 //Deployable 1 of Version 2
		    Deployable dep3= new Deployable();
		    dep3.setDeployabledId("656547");
		    dep3.setDeployableName("my bundle");
		    dep3.setUploaded(true);
		    
		    
		    //Deployable 2 of Version 2
		    Deployable dep4= new Deployable();
		    dep4.setDeployabledId("9875212");
		    dep4.setDeployableName("my ear");
		    dep4.setUploaded(true);
	
	    version2.getSortedDeployablesList().add(dep3);
		version2.getSortedDeployablesList().add(dep4);
	
	    
		app1.getListApplicationVersion().add(version1);
		app1.getListApplicationVersion().add(version2);
		
		//-----------------------------------------------------------------------------------
		
		//Application 2
		Application app2=new Application();
		app2.setAppId("23645");
		app2.setName("My second application");
		ArrayList<String> requirements2= new ArrayList<String>();
		requirements2.add("requirement1");
		requirements2.add("requirement2");
		app2.setRequirements(requirements2);
		
			//Version 1 of Application 2
			ApplicationVersion version3 = new ApplicationVersion();
			version3.setVersionId("853214");
			version3.setAppId("23645");
			version3.setVersionLabel("Version 1 of Application  2");
		
		      //Instance 1 of Version 2
			  ApplicationVersionInstance instance3=new ApplicationVersionInstance();
			  instance3.setInstanceId("789");
			  instance3.setVersionId("853214");
			  instance3.setAppId("23645");
			  instance3.setInstanceName("1rst instance of the 1rst version of 2nd application");
			  instance3.setState(ApplicationVersionInstance.INSTANCE_STOPPED);
			
			version3.getListApplicationVersionInstance().add(instance3);
		  
			  
			app2.getListApplicationVersion().add(version3);

			
			
			listApplication.add(app1);
			listApplication.add(app2);

			

   
    return listApplication;
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
  public ArrayList<Application> getListApplication() {
	return listApplication;
  }

  public void setListApplication(ArrayList<Application> listApplication) {
	this.listApplication = listApplication;
  }
}
