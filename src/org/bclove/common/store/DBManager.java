package org.bclove.common.store;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DBManager {
	
	/**
	 * logger
	 */
	protected Log log = LogFactory.getLog(this.getClass());
	
	private static DBManager instance = null;
	private static BasicDataSource dataSource = null;

	private DBManager() {
		init();
	}

	public static DBManager getInstance() {
		if (instance == null) {
			synchronized (DBManager.class) {
				if (instance == null)
					instance = new DBManager();
			}
		}

		return instance;
	}

	public Connection getConnection() {
		if (dataSource == null) {
			init();
		}
		Connection conn = null;
		if (dataSource != null) {
			try {
				conn = dataSource.getConnection();
			} catch (SQLException sqlEx) {
				log.error("Fail to get DB connection.", sqlEx);
			}
		}
		return conn;
	}

	public void release() {
		if (dataSource != null) {
			try {
				dataSource.close();
			} catch (SQLException sqlEx) {
				log.error("Fail to close data source.", sqlEx);
			}
		}
	}

	private void init() {
		release();
		Configuration config = Configuration.getInstance();
		Properties dbpoolProps = new Properties();
		dbpoolProps.setProperty("driverClassName", config
				.getString(Constants.PARAM_DB_DRIVER));
		dbpoolProps.setProperty("url", config.getString(Constants.PARAM_DB_URL));
		dbpoolProps.setProperty("username", config
				.getString(Constants.PARAM_DB_USER));
		dbpoolProps.setProperty("password", config
				.getString(Constants.PARAM_DB_PASSWD));
		dbpoolProps.setProperty("maxActive", config
				.getString(Constants.PARAM_DBPOOL_MAXACTIVE));
		dbpoolProps.setProperty("maxIdle", config
				.getString(Constants.PARAM_DBPOOL_MAXIDLE));
		dbpoolProps.setProperty("maxWait", config
				.getString(Constants.PARAM_DBPOOL_MAXWAIT));
		dbpoolProps.setProperty("removeAbandoned", config
				.getString(Constants.PARAM_DBPOOL_RM_ABANDON));
		dbpoolProps.setProperty("removeAbandonedTimeout", config
				.getString(Constants.PARAM_DBPOOL_RM_ABANDON_TIMEOUT));
		dbpoolProps.setProperty("testWhileIdle", config
				.getString(Constants.PARAM_DBPOOL_TEST_WHILEIDLE));
//		dbpoolProps.setProperty("validationQuery", config
//				.getString(Const.PARAM_DBPOOL_VALIDATION_QUERY));
		try {
			dataSource = (BasicDataSource) BasicDataSourceFactory
					.createDataSource(dbpoolProps);
		} catch (Exception ex) {
			log.error("Fail to create data source.", ex);
		}
	}

}
