package io.github.shshdxk.liquibase.database.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.database.DatabaseConnection;
import io.github.shshdxk.liquibase.database.OfflineConnection;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.executor.ExecutorService;
import io.github.shshdxk.liquibase.statement.core.RawSqlStatement;
import io.github.shshdxk.liquibase.util.StringUtil;

public class DB2Database extends AbstractDb2Database {

	@Override
	public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
		return conn.getDatabaseProductName().startsWith("DB2")
				&& !StringUtil.startsWith(conn.getDatabaseProductVersion(), "DSN");
	}

	@Override
	public String getShortName() {
		return "db2";
	}

	/**
	 * boolean data type column are allowed for versions >= 11.1.1.1
	 * @return
	 */
	public boolean supportsBooleanDataType() {
		if (getConnection() == null)
			return false; /// assume not;
		try {

			final Integer fixPack = getDb2FixPack();

			if (fixPack == null)
				throw new DatabaseException("Error getting fix pack number");

			return getDatabaseMajorVersion() > 11
					|| getDatabaseMajorVersion() == 11 && getDatabaseMinorVersion() >= 1 && fixPack.intValue() >= 1;

		} catch (final DatabaseException e) {
			return false; // assume not
		}
	}

	private Integer getDb2FixPack() {
		if (getConnection() == null || getConnection() instanceof OfflineConnection)
			return null;
		try {
			return Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).queryForObject(
					new RawSqlStatement("SELECT fixpack_num FROM TABLE (sysproc.env_get_inst_info()) as INSTANCEINFO"),
					Integer.class);
		} catch (final Exception e) {
			Scope.getCurrentScope().getLog(getClass()).info("Error getting fix pack number", e);
		}
		return null;
	}

	@Override
	protected String getDefaultDatabaseProductName() {
		return "DB2/LUW";
	}

}
