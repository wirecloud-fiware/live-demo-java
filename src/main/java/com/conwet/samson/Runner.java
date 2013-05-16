package com.conwet.samson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;


public class Runner {

	private Map<String, String> techToVan;
	private Map<String, List<String>> vanToCoord;
	private QueryBroker broker;
	
	public Runner() {
		
		// TODO broker's parameters
		broker = QueryFactory.newQuerier("130.206.80.254", 1026);
		techToVan = new HashMap<>();
		vanToCoord = new HashMap<>();
		
		loadData("data.json");
	}
	
	private void loadData(String filename) {
		
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
						
			JSONObject json = (JSONObject) new JSONParser().parse(new InputStreamReader(is));
			
			// loads techToVan
			JSONObject values = (JSONObject) json.get("technicians");
			@SuppressWarnings("unchecked")
			List<JSONObject> entities = (List<JSONObject>) values.get("values");
			
			for (JSONObject jsonObject : entities) {
				
				String tech = (String) jsonObject.get("id");
				String van = (String) jsonObject.get("van");
				
				techToVan.put(tech, van);
				vanToCoord.put(van, loadVanFile(van + ".route"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<String> loadVanFile(String vanFile) throws URISyntaxException, IOException {
		
		Path path = null;
		URI route = getClass().getClassLoader().getResource(vanFile).toURI();
		
		try {
			path = Paths.get(route);
		} catch(FileSystemNotFoundException fnfe) {
			// usually happens with jar files
			FileSystems.newFileSystem(route, new HashMap<String, Object>());
			path = Paths.get(route);
		}
		
		return Files.readAllLines(path, StandardCharsets.UTF_8);
	}
	
	public void moveVanOfTech(String technician) {
		
		String vanID = techToVan.get(technician);
		List<String> coordinates = vanToCoord.get(vanID);
		
		if (vanID != null && coordinates != null) {
			
			new Thread(new MovingVan(vanID, coordinates, 100L, broker)).start();
		}
	}
}
