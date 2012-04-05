package org.bclove.common.store;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface OperateDataSource {
	
	public int insertReturnId(OpUpdate op) throws SQLException;
	
	/**
	 * 
	 * 先数据库中插入数据，如果成功则返回true，不成功返回false或者抛出异常
	 * 
	 * @param op
	 * @return
	 * @throws SQLException
	 */
	public boolean insert(OpUpdate op) throws SQLException;
	
	public boolean queryExist(OpUniq op) throws SQLException;
	
	public int queryId(OpUniq op) throws SQLException;
	
	public long getQueryId(OpUniq op) throws SQLException;
	
	public int queryInt(OpUniq op) throws SQLException ;
	
	public int queryInt(OpUniq op, boolean master) throws SQLException ;
	
	public long queryLong(OpUniq op) throws SQLException ;
	
	public List<?> queryList(OpList op) throws SQLException ;
	
	public Object queryUnique(OpUniq op) throws SQLException;
	
	public Object queryUnique(OpUniq op, boolean master) throws SQLException;
	
	public int update(OpUpdate op) throws SQLException;
	/**
	 * 执行批量的sql更新，返回第一条sql执行后的id
	 * @param ops 必须是OpBatchUpdate类型的列表
	 * @param bizName 业务名，必须输入 
	 * @throws SQLException
	 */
	public void insertBatchReturnFirstSqlId(List<OpBatchUpdate> ops) throws SQLException;

	/**
	 * 将得到的查询结果集封装在map中返回
	 * @param op
	 * @param keyFieldName 用那个字段的值作为map的key
	 * @return 
	 * @throws SQLException
	 */
	public Map<String ,Object> queryMap(OpMap op,String keyFieldName) throws SQLException;
	/**
	 * 将得到的查询结果集封装在map中返回
	 * @param op
	 * @param keyFieldNames 用多个字段的值联合作为map的key，字段名之间用_分隔
	 * @return
	 * @throws SQLException
	 */
	public Map<String ,Object> queryMap(OpMap op,String[] keyFieldNames) throws SQLException;
}