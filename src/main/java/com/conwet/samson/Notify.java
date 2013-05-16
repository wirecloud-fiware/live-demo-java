package com.conwet.samson;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.Node;

import com.conwet.samson.jaxb.ContextAttribute;
import com.conwet.samson.jaxb.ContextElementResponse;
import com.conwet.samson.jaxb.NotifyContext;


public class Notify extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private JAXBContext context;
	private Runner runner;
	
	@Override
	public void init() throws ServletException {
		
		try {
			context = JAXBContext.newInstance("com.conwet.samson.jaxb");
			runner = new Runner();
		} catch (JAXBException e) {
			throw new ServletException(e);
		}
	}
		
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
										throws ServletException, IOException {
		
		try {
			Unmarshaller unmarsh = context.createUnmarshaller();
			@SuppressWarnings("unchecked")
			JAXBElement<NotifyContext> elem = (JAXBElement<NotifyContext>) unmarsh
													.unmarshal(req.getReader());
			NotifyContext context = elem.getValue();
			System.out.println("NotifyContext=" + context);
			
			for (ContextElementResponse cxtElemRes : context.getContextResponseList()
													.getContextElementResponse()) {
				
				for (ContextAttribute cxtAttr : cxtElemRes.getContextElement()
													.getContextAttributeList()
													.getContextAttribute()) {
					
					if (cxtAttr.getName().equals("technician")) {
						
						Node node = (Node) cxtAttr.getContextValue();
						runner.moveVanOfTech(node.getTextContent());
						
						// should be present only one technician
						return;
					}
				}
			}
		} catch (JAXBException e) {
			throw new ServletException(e);
		}
	}
}
