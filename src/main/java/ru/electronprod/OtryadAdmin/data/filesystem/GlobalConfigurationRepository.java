//package ru.electronprod.OtryadAdmin.data.filesystem;
//
//import java.io.File;
//
//import org.json.simple.JSONObject;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.stereotype.Repository;
//
//import jakarta.annotation.PreDestroy;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//@Repository
//public class GlobalConfigurationRepository implements InitializingBean {
//	private File confFile = new File("global_config.json");
//	private JSONObject config;
//
//	/**
//	 * Loads config on startup
//	 */
//	@Override
//	public void afterPropertiesSet() throws Exception {
//		if (FileOptions.loadFile(confFile) || FileOptions.getFileLines(confFile.getPath()).isEmpty()) {
//			config = writeDefaults();
//		} else {
//			config = (JSONObject) FileOptions.ParseJs(FileOptions.getFileLine(confFile));
//		}
//	}
//
//	/**
//	 * Saves config from virtual memory to hard drive on application shutdown
//	 */
//	@PreDestroy
//	public void cleanup() {
//		FileOptions.writeFile(config.toJSONString(), confFile);
//		log.info("Wrote data to " + confFile.getName());
//	}
//
//	/**
//	 * Fills default values to config
//	 * 
//	 * @return JSONObject wrote to file
//	 */
//	private JSONObject writeDefaults() {
//		JSONObject main = new JSONObject();
//		// Links settings
//		main.put("links", new JSONObject());
//		FileOptions.writeFile(main.toJSONString(), confFile);
//		log.info("Wrote defaults to " + confFile.getName());
//		return main;
//	}
//}