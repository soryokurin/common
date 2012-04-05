package org.bclove.common.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;



import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



public class DataAccessMgr implements OperateDataSource {
	

	private static DataAccessMgr instance = new DataAccessMgr();
	
	/**
	 * logger
	 */
	protected Log logger = LogFactory.getLog(this.getClass());

	public static DataAccessMgr getInstance() {
		if (instance == null)
			synchronized (DataAccessMgr.class) {
				if (instance == null){
					instance = new DataAccessMgr();
				}
			}
		return instance;
	}

	// public static Log logger = LogFactory.getLog(DataAccessMgr.class);
	

	private DataAccessMgr() {
		super();
	}

	public void closeConnection(final Connection con) {
		
		try {
			if (con != null)
				con.close();
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	public void closeResultSet(final ResultSet rs) {
		try {
			if (rs != null)
				rs.close();
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param rs
	 * @param st
	 * @param conn
	 */
	public void closeRSC(final ResultSet rs, final Statement st,
			final Connection conn) {
		if (rs != null)
			try {
				rs.close();
			} catch (SQLException e) {
				this.logger.error("res close", e);
			}
		if (st != null)
			try {
				st.close();
			} catch (SQLException e) {
				this.logger.error("prestatement  close", e);
			}
		if (conn != null)
			try {
				conn.close();
			} catch (SQLException e) {
				//e.printStackTrace();
			}
	}

	public void closeStatement(final Statement st) {
		try {
			if (st != null)
				st.close();
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}

	private Connection getConn(final char o, final Op op) throws SQLException {
		if (op.getBizName() == null || op.getBizName().length() == 0)
			throw new SQLException("Op中 BizName 不能为null");
		Connection conn = null;
		if (o == 'r') {
			//下面2行是新加的代码,依赖ConfigServer
//			SQLThreadLocal.set(SQLType.READ, op.getSql(), null, null);
//			conn = dataSourceFactory.getDataSource(op.getBizName()).getConnection();
			
			//下面4行是旧代码，依赖ICE的DBDescriptor服务
			if (op.getTableSuffix() < 0)
				conn = getConnection(op.getBizName(), true);
			else
				conn = getConnection(op.getBizName(), op.getTableSuffix(), true);
		} else if (o == 'w') {
			
			//下面4行是旧代码，依赖ICE的DBDescriptor服务
			if (op.getTableSuffix() < 0)
				conn = getConnection(op.getBizName(), false);
			else
				conn = getConnection(op.getBizName(), op.getTableSuffix(),
						false);
		} else
			throw new SQLException("没有指定是 r 还是 w");
		return conn;
	}

	/**
	 * 没有散表的情况下，要得到一个连接，可以调用此方法
	 * 
	 * @param bizName
	 *            业务名
	 * @param isReadConnection
	 *            是否获取读连接
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection(final String bizName,
			final boolean isReadConnection) throws SQLException {
		if (bizName == null || bizName.length() == 0)
			throw new SQLException("BizName 不能为null");
		Connection conn = null;
		if (isReadConnection){
			
			//下面4行是旧代码，依赖ICE的DBDescriptor服务
			try {
				conn = DBManager.getInstance().getConnection();
			} catch (Exception e) {
				e.printStackTrace(System.err);
				throw new SQLException();
			}
		}else{
			
			//下面4行是旧代码，依赖ICE的DBDescriptor服务
			try {
				conn = DBManager.getInstance().getConnection();
			} catch (Exception e) {
				e.printStackTrace(System.err);
				throw new SQLException();
			}
		}
		return conn;
	}

	/**
	 * 散表的情况下，要得到一个连接，可以调用此方法
	 * 
	 * @param bizName
	 *            业务名
	 * @param tableSuffix
	 *            散表名的后缀 ，如gossip_3，传入 3
	 * @param isReadConnection
	 *            是否获取读连接
	 * @return
	 * @throws SQLException
	 */
	public Connection getConnection(final String bizName,
			final int tableSuffix, final boolean isReadConnection)
			throws SQLException {
		if (bizName == null || bizName.length() == 0)
			throw new SQLException("BizName 不能为null");
		if (tableSuffix < 0)
			if (!bizName.equals("db_sequence"))
				throw new SQLException("tableSuffix 不能为null");
		Connection conn = null;
		//String tablePattern = getTablePattern(bizName, tableSuffix);
		if (isReadConnection){
			
			conn = DBManager.getInstance().getConnection();
		} else {
			
			conn = DBManager.getInstance().getConnection();
		}
		return conn;
	}

	/**
	 * 获得id，这个方法比较特殊，其数据源是固定的，jdbc/id
	 */
	public long getQueryId(final OpUniq op) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		long result = 0;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			conn = getConn('r', op);
			ps = conn.prepareStatement(op.getSql());
			op.setParam(ps);
			// op.log(logger);
			rs = ps.executeQuery();
			if (rs.next())
				result = rs.getLong(1);
			if (rs.next())
				this.logger
						.error("Non Unique Result Error: wrong sql syntax or database not consistence!");
			return result;
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	public boolean insert(final OpUpdate op) throws SQLException {
		// 数据源
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			conn = getConn('w', op);
			ps = conn.prepareStatement(op.getSql());
			op.setParam(ps);
			int count = ps.executeUpdate();
			if (count > 0) {
				if (ps != null)
					ps.close();
				return true;
			} else
				return false;
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	/**
	 * 慎用此方法
	 */
	public int[] batchUpdate(final OpUpdate op) throws SQLException {
		// 数据源
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			conn = getConn('w', op);
			ps = conn.prepareStatement(op.getSql());
			op.setParam(ps);
			return ps.executeBatch();
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}
	
	/**
	 * 慎用此方法
	 */
	public void insertBatchReturnFirstSqlId(final List<OpBatchUpdate> ops)
			throws SQLException {
		if (ops == null || ops.size() == 0)
			throw new SQLException(
					" ----- the opbatchupdate list can't null -------------");
		OpBatchUpdate firstOp = ops.get(0);
		if (firstOp.bizName == null || firstOp.bizName.trim().length() == 0)
			throw new SQLException(
					" ----- the bizName of the first opbatchupdate object can't null -------------");
		// 数据源
		PreparedStatement ps = null;
		Connection conn = null;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			conn = getConn('w', firstOp);
			for (OpBatchUpdate opb : ops) {
				ps = conn.prepareStatement(opb.getSql());
				opb.setParam(ps);
				// opb.log(logger);
				ps.executeUpdate();
				if (ps != null)
					ps.close();
			}
		} finally {
			closeRSC(null, ps, conn);
			sqlEnd("excutebatch sql,", begin);
		}
	}

	public int insertReturnId(final OpUpdate op) throws SQLException {
		// 数据源
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			conn = getConn('w', op);
			ps = conn.prepareStatement(op.getSql());
			op.setParam(ps);
			// op.log(logger);
			int count = ps.executeUpdate();
			if (count > 0) {
				if (ps != null)
					ps.close();
				ps = conn.prepareStatement("select last_insert_id();");
				rs = ps.executeQuery();
				if (rs.next())
					return rs.getInt(1);
				else
					return 0;
			} else
				return 0;
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	public boolean queryExist(final OpUniq op) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			// 从不同的数据源获得connection
			conn = getConn('r', op);
			ps = conn.prepareStatement(op.getSql());
			op.setParam(ps);
			// op.log(logger);
			rs = ps.executeQuery();
			if (rs.next())
				return true;
			return false;
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	/**
	 * 获得id，这个方法比较特殊，其数据源是固定的，jdbc/id
	 */
	public int queryId(final OpUniq op) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		int result = 0;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			conn = getConn('r', op);
			ps = conn.prepareStatement(op.getSql());
			op.setParam(ps);
			// op.log(logger);
			rs = ps.executeQuery();
			if (rs.next())
				result = rs.getInt(1);
			if (rs.next())
				this.logger
						.error("Non Unique Result Error: wrong sql syntax or database not consistence!");
			return result;
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	public int queryInt(final OpUniq op) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		int result = 0;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			// 从不同的数据源获得connection
			conn = getConn('r', op);
			ps = conn.prepareStatement(op.getSql());

			op.setParam(ps);
			// op.log(logger);
			rs = ps.executeQuery();
			if (rs.next())
				result = rs.getInt(1);
			else
				return 0;
			if (rs.next())
				this.logger
						.error("Non Unique Result Error: wrong sql syntax or database not consistence!");
			return result;
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	public int queryInt(final OpUniq op, final boolean master)
			throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		int result = 0;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			// 从不同的数据源获得connection
			if (master)
				conn = getConn('w', op);
			else
				conn = getConn('r', op);
			ps = conn.prepareStatement(op.getSql());

			op.setParam(ps);
			// op.log(logger);
			rs = ps.executeQuery();
			if (rs.next())
				result = rs.getInt(1);
			else
				return 0;
			if (rs.next())
				this.logger
						.error("Non Unique Result Error: wrong sql syntax or database not consistence!");
			return result;
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	public List<?> queryList(final OpList op) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			// 从不同的数据源获得connection
			conn = getConn('r', op);
			ps = conn.prepareStatement(op.getSql());

			op.setParam(ps);
			// op.log(logger);
			rs = ps.executeQuery();
			while (rs.next())
				op.add(op.parse(rs));
			return op.getResult();
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	public List<?> queryList(final OpList op, final boolean master)
			throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			// 从不同的数据源获得connection
			if (master)
				conn = getConn('w', op);
			else
				conn = getConn('r', op);
			ps = conn.prepareStatement(op.getSql());

			op.setParam(ps);
			// op.log(logger);
			rs = ps.executeQuery();
			while (rs.next())
				op.add(op.parse(rs));
			return op.getResult();
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	public long queryLong(final OpUniq op) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		long result = 0;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			// 从不同的数据源获得connection
			conn = getConn('r', op);
			ps = conn.prepareStatement(op.getSql());

			op.setParam(ps);
			// op.log(logger);
			rs = ps.executeQuery();
			if (rs.next())
				result = rs.getLong(1);
			else
				return 0;
			if (rs.next())
				this.logger
						.error("Non Unique Result Error: wrong sql syntax or database not consistence!");
			return result;
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	public Map<String, Object> queryMap(final OpMap op,
			final String keyFieldName) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			// 从不同的数据源获得connection
			conn = getConn('r', op);
			ps = conn.prepareStatement(op.getSql());

			op.setParam(ps);
			// op.log(logger);
			rs = ps.executeQuery();
			while (rs.next())
				op.add(rs.getString(keyFieldName), op.parse(rs));
			return op.getResult();
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	public Map<String, Object> queryMap(final OpMap op,
			final String[] keyFieldNames) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			// 从不同的数据源获得connection
			conn = getConn('r', op);
			ps = conn.prepareStatement(op.getSql());

			op.setParam(ps);
			// op.log(logger);
			rs = ps.executeQuery();
			int len = keyFieldNames.length;
			String key = "";
			while (rs.next()) {
				for (int i = 0; i < len - 1; i++)
					if (i == len - 1)
						key += rs.getString(keyFieldNames[i]);
					else
						key += rs.getString(keyFieldNames[i]) + ",";
				op.add(key, op.parse(rs));
			}
			return op.getResult();
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	public Object queryUnique(final OpUniq op) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			// 从不同的数据源获得connection
			conn = getConn('r', op);
			ps = conn.prepareStatement(op.getSql());
			op.setParam(ps);
			// op.log(logger);
			rs = ps.executeQuery();
			if (rs.next())
				op.add(op.parse(rs));
			if (rs.next()) {
				this.logger.error("----------【error sql】----------------");
				this.logger
						.error("Non Unique Result Error: wrong sql syntax or database not consistence!");
				this.logger.error("wrong sql is:" + op.getSql());
				this.logger.error("wrong ps is:" + ps);
			}
			return op.getResult();
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	public Object queryUnique(final OpUniq op, final boolean master)
			throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			// 从不同的数据源获得connection
			if (master)
				conn = getConn('w', op);
			else
				conn = getConn('r', op);
			ps = conn.prepareStatement(op.getSql());
			op.setParam(ps);
			// op.log(logger);
			rs = ps.executeQuery();
			if (rs.next())
				op.add(op.parse(rs));
			if (rs.next()) {
				this.logger.error("----------【error sql】----------------");
				this.logger
						.error("Non Unique Result Error: wrong sql syntax or database not consistence!");
				this.logger.error("wrong sql is:" + op.getSql());
				this.logger.error("wrong ps is:" + ps);
			}
			return op.getResult();
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	private long sqlBegin() {
		// 记录sql执行的开始时间
		if (this.logger.isDebugEnabled())
			return System.currentTimeMillis();
		return 0;
	}

	private void sqlEnd(final String sql, final long begin) {
		if (begin > 0 && this.logger.isDebugEnabled()) {
			long end = System.currentTimeMillis();
			this.logger.debug("|" + sql + "|" + (end - begin));
		}
	}

	public int update(final OpUpdate op) throws SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;
		long begin = 0L;
		try {
			// 记录sql执行的开始时间
			begin = sqlBegin();
			// 从不同的数据源获得connection
			conn = getConn('w', op);
			ps = conn.prepareStatement(op.getSql());
			op.setParam(ps);
			// op.log(logger);
			return ps.executeUpdate();
		} finally {
			closeRSC(rs, ps, conn);
			sqlEnd(op.getSql(), begin);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		OpUniq op = new OpUniq("select now()", "buddy"){
			public Object parse(ResultSet rs) throws SQLException {
				return rs.getObject(1);
			}
		};
		try {
			Object obj = DataAccessMgr.getInstance().queryUnique(op,true);
			System.out.println("result:" + obj);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		System.exit(0);
	}
}