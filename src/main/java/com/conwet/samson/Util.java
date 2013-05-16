package com.conwet.samson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.conwet.samson.jaxb.ContextAttribute;
import com.conwet.samson.jaxb.ContextAttributeList;
import com.conwet.samson.jaxb.ContextElement;
import com.conwet.samson.jaxb.ContextRegistration;
import com.conwet.samson.jaxb.ContextResponse;
import com.conwet.samson.jaxb.EntityId;
import com.conwet.samson.jaxb.EntityIdList;
import com.conwet.samson.jaxb.NotifyConditionType;
import com.conwet.samson.jaxb.ObjectFactory;
import com.conwet.samson.jaxb.RegisterContextResponse;
import com.conwet.samson.jaxb.SubscribeResponse;
import com.conwet.samson.jaxb.UpdateActionType;

public class Util {
	
	private static String ENTITY_URL = "http://wirecloud.conwet.fi.upm.es";
	
	private JSONObject json;
	private QueryBroker querier;
	private ObjectFactory factory;
	
	public Util(String host, int port) {
		
		this.factory = new ObjectFactory();
		this.querier = QueryFactory.newQuerier(host, port);
	}
	
	public void loadJSON(String filename) throws Exception {
		
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
			
			json = (JSONObject) new JSONParser().parse(new InputStreamReader(is));
		}
	}
	
	public String subscribeIssues(String url, String attrName) throws Exception {
		
		EntityIdList entityList = factory.createEntityIdList();
		entityList.getEntityId().add(querier.newEntityId("Issue", "iss.*", true));
		List<String> condList = new ArrayList<>();
		condList.add(attrName);
		Duration duration = DatatypeFactory.newInstance().newDuration("PT24H");
		
		SubscribeResponse resp = querier.subscribe(entityList.getEntityId(),
												condList, url, duration,
												NotifyConditionType.ONCHANGE);
		
		System.out.println("subscribe for issues: " + resp);
		return resp.getSubscriptionId();
	}
	
	private void createEntity(JSONObject entity) throws Exception {
		
		String id = (String) entity.get("id");
		String type = (String) entity.get("type");
		EntityId entityID = querier.newEntityId(type, id, false);
		
		ContextRegistration cxtReg = factory.createContextRegistration();
		EntityIdList entityList = factory.createEntityIdList();
		entityList.getEntityId().add(entityID);
		cxtReg.setEntityIdList(entityList);
		cxtReg.setProvidingApplication(ENTITY_URL);
		
		Duration duration = DatatypeFactory.newInstance().newDuration("PT24H");
		
		RegisterContextResponse resp = querier.registerContext(cxtReg, duration);
		System.out.println("Entity " + entityID.getId() + " reg id=" + resp.getRegistrationId());
	}
	
	/**
	 * Updates the registration with attributes and values
	 * 
	 * @param entity the entity JSON to read value for update
	 * @throws Exception
	 */
	private void updateEntity(JSONObject entity) throws Exception  {
		
		@SuppressWarnings("unchecked")
		Set<String> attrKey = new HashSet<>(entity.keySet());
		attrKey.remove("id");
		
		ContextAttributeList attrList = factory.createContextAttributeList();
		
		for (String attrName : attrKey) {
			
			ContextAttribute cxtAttr = factory.createContextAttribute();
			cxtAttr.setName(attrName);
			cxtAttr.setContextValue(entity.get(attrName));
			
			attrList.getContextAttribute().add(cxtAttr);
		}
		
		String id = (String) entity.get("id");
		String type = (String) entity.get("type");
		ContextElement cxtElem = factory.createContextElement();
		cxtElem.setEntityId(querier.newEntityId(type, id, false));
		cxtElem.setContextAttributeList(attrList);
		
		ContextResponse cxtResp = querier.updateContext(cxtElem, UpdateActionType.APPEND);
		System.out.println("UPDATE Status Code: " + cxtResp.getContextResponseList()
											.getContextElementResponse().get(0)
											.getStatusCode());
	}
	
	@SuppressWarnings("unchecked")
	public void registerEntities(String name, long delay, long period) throws Exception {
		
		JSONObject values = (JSONObject) json.get(name);
		List<JSONObject> entities = (List<JSONObject>) values.get("values");
		Thread.sleep(delay);
		
		for (JSONObject entity : entities) {
			
			createEntity(entity);
			updateEntity(entity);
			
			Thread.sleep(period);
		}
	}
}
