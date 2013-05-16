package com.conwet.samson;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class Main {

	public static void main(String[] args) throws Exception {
				
		// set up TomCat so servet can move the van's location
		String host = "piccolo.ls.fi.upm.es";
		Tomcat tomcat = new Tomcat();
		tomcat.setHostname(host);
		tomcat.setPort(0);
		Context cxt = tomcat.addWebapp("/samson", "/tmp");
		tomcat.addServlet("/samson", "notify", Notify.class.getName());
		cxt.addServletMapping("/notify", "notify");
		tomcat.start();
		
		// register entities
		Util util = new Util("130.206.80.254", 1026); 
		util.loadJSON("data.json");
		util.registerEntities("vendingmachines", 0L, 0L);
		util.registerEntities("technicians", 0L, 0L);
		util.registerEntities("vans", 0L, 0L);
				
		// start creating issues every 2 sec with a delay of 5 sec.
		util.registerEntities("issues", 5000L, 2000L);
		
		// receive the technician-issue mapping from subs and moves the van
		int port = tomcat.getConnector().getLocalPort();
		System.out.println("Tomcat listening on port " + port);
		String url = "http://" + host + ":" + port + "/samson/notify";
		String subID = util.subscribeIssues(url, "technician");
		System.out.println("SUB_ID=" + subID);
		System.out.println("press enter to exit");
		new BufferedReader(new InputStreamReader(System.in)).readLine();
		
		tomcat.stop();
	}
}
