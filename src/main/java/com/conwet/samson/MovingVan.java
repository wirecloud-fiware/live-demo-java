package com.conwet.samson;

import java.util.List;
import java.util.Objects;

import com.conwet.samson.jaxb.ContextAttribute;
import com.conwet.samson.jaxb.ContextAttributeList;
import com.conwet.samson.jaxb.ContextElement;
import com.conwet.samson.jaxb.ObjectFactory;
import com.conwet.samson.jaxb.UpdateActionType;

/**
 * Moves the Van position according to the coordinates found in the relative
 * file
 * 
 * @author sergio
 */
public class MovingVan implements Runnable {
	
	private String vanID;
	private List<String> coordinates;  // latitude and longitude
	private long wait;
	
	private ObjectFactory factory;
	private QueryBroker broker;
	
	
	public MovingVan(String vanID, List<String> coordinates, long wait, QueryBroker broker) {
		
		this.vanID = Objects.requireNonNull(vanID);
		this.coordinates = Objects.requireNonNull(coordinates);
		this.broker = Objects.requireNonNull(broker);
		this.wait = wait;
		
		this.factory = new ObjectFactory();
	}
	
	@Override
	public void run() {
		
		for (String coordinate : coordinates) {
			
			// construct URL to send
			// contextBroker + vanID + coordinates
			updateCoordinates(coordinate.trim());
			
			try {
				Thread.sleep(wait);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void updateCoordinates(String coordinates) {
		
		System.out.println("MOVE " + coordinates);
		// populate the attribute list
		ContextAttributeList attrList = factory.createContextAttributeList();
		ContextAttribute coord = factory.createContextAttribute();
		coord.setName("coordinates");
		coord.setContextValue(coordinates);
		attrList.getContextAttribute().add(coord);
		
		// create the contextElement
		ContextElement cxtElem = factory.createContextElement();
		cxtElem.setEntityId(broker.newEntityId("Van", vanID, false));
		cxtElem.setContextAttributeList(attrList);
		
		// send it
		try {
			broker.updateContext(cxtElem, UpdateActionType.UPDATE);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
