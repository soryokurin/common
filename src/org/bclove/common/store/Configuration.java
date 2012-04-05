package org.bclove.common.store;

import java.io.IOException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class Configuration {
	
	/**
	 * logger
	 */
	protected Log logger = LogFactory.getLog(this.getClass());
	private static final String CONFIG_FILE = "config.properties";
	private static ResourceBundle bundles = null;
	private static Configuration instance = null;

	private Configuration() {
		initialize();
	}

	/**
	 * Singleton
	 * 
	 * @return a singleton instance
	 */
	public static Configuration getInstance() {
		if (instance == null) {
			instance = new Configuration();
		}
		return instance;
	}

	public String getString(String name) {

		return getString(name, null);
	}

	public String getString(String name, String defaultVal) {
		String value = null;
		try {
			value = bundles.getString(name);
		} catch (Exception ex) {
		}
		return (value != null) ? value : defaultVal;
	}

	private void initialize() {
		try {
			bundles = new PropertyResourceBundle(Configuration.class
					.getResourceAsStream("/" + CONFIG_FILE));
		} catch (IOException ioe) {
			logger.error("Fail to load the configuration file: " + CONFIG_FILE);
		}
	}

}
