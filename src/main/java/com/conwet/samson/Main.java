package com.conwet.samson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class Main {
	
	private static void printUsage() {
		
		System.out.println("Usage: java -jar file.jar <notifHost> <notifPort> <cxtBroker> <cxtPort>");
		System.out.println("");
		System.out.println("\t<notifHost> the IP/hostname to receive notifications, default is local IP");
		System.out.println("\t<notifPort> the port to receive notifications, default is a random port");
		System.out.println("\t<cxtBroker> the context broker IP/hostname, default is 130.206.80.254");
		System.out.println("\t<cxtPort>   the context broker port, default is 1026");
		System.out.println("");
	}

	public static void main(String[] args) throws Exception {
		
		// default values
		String tomcatHost = InetAddress.getLocalHost().getCanonicalHostName();
		String brokerHost = "130.206.80.254";
		int tomcatPort = 0;
		int brokerPort = 1026;
		
		switch (args.length) {
			// breaks are missing deliberately
			case 4: brokerPort = Integer.parseInt(args[3]);
			case 3: brokerHost = args[2];
			case 2: tomcatPort = Integer.parseInt(args[1]);
			case 1: tomcatHost = args[0];
			case 0: break; // all defaults
			default: printUsage();
				break;
		}
		
		// set up TomCat so the servlet can move the van's location
		Tomcat tomcat = new Tomcat();
		tomcat.setHostname(tomcatHost);
		tomcat.setPort(tomcatPort);
		Context cxt = tomcat.addWebapp("/samson", "/tmp");
		tomcat.addServlet("/samson", "notify", Notify.class.getName());
		cxt.addServletMapping("/notify", "notify");
		tomcat.start();
		
		// register entities
		Util util = new Util(brokerHost, brokerPort); 
		util.loadJSON("data.json");
		util.registerEntities("vendingmachines", 0L, 0L);
		util.registerEntities("technicians", 0L, 0L);
		util.registerEntities("vans", 0L, 0L);
				
		// start creating issues every 2 sec with a delay of 5 sec.
		util.registerEntities("issues", 5000L, 2000L);
		
		// receive the technician-issue mapping from subs and moves the van
		tomcatPort = tomcat.getConnector().getLocalPort();
		String url = "http://" + tomcatHost + ":" + tomcatPort + "/samson/notify";
		System.out.println("Subscription URL=" + url);
		String subID = util.subscribeIssues(url, "technician");
		System.out.println("SUB_ID=" + subID);
		System.out.println("press enter to exit");
		
		try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in))) {
			
			input.readLine();
			
		} finally {
			tomcat.stop();
		}
	}
}
