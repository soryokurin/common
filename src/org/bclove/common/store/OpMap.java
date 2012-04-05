package org.bclove.common.store;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class OpMap  extends Op{
	
	private Map<String, Object> collect;

	/**
	 * 此构造器用于业务不散表的情况<br>
	 * @param sql sql语句
	 * @param bizName 业务名
	 */
	public OpMap(String sql,String bizName) {
		this.sql = sql;
		collect = new HashMap<String, Object>();
		this.bizName = bizName;
	}

	/**
	 * 此构造器用于业务散表的情况<br>
	 * @param sql sql语句
	 * @param bizName 业务名
	 * @param tableSuffix 散表表名的后缀，如gossip_2,应该传入 2
	 */
	public OpMap(String sql,String bizName,int tableSuffix) {
		this.sql = sql;
		collect = new HashMap<String, Object>();
		this.bizName = bizName;
		this.tableSuffix = tableSuffix;
	}
	
	public final Map<String, Object> getResult() {
		return collect;
	}

	public void setParam(PreparedStatement ps) throws SQLException {
	}

	public Object parse(ResultSet rs) throws SQLException {
		return null;
	}
	
	public final void add(String keyFieldName ,Object ob) {
		collect.put(keyFieldName,ob);
	}

}
