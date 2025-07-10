package io.github.shshdxk.liquibase.database.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.database.DatabaseConnection;
import io.github.shshdxk.liquibase.database.OfflineConnection;
import io.github.shshdxk.liquibase.exception.DatabaseException;
import io.github.shshdxk.liquibase.executor.ExecutorService;
import io.github.shshdxk.liquibase.statement.core.RawParameterizedSqlStatement;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Table;
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
	 * Checks if the database supports boolean data type columns. This is true for versions >= 11.1.1.1.
	 *
	 * @return true if boolean data type columns are supported, false otherwise
	 */
	public boolean supportsBooleanDataType() {
		if (getConnection() == null)
			return false; /// assume not;
		try {

			final Integer fixPack = getDb2FixPack();

			if (fixPack == null)
				throw new DatabaseException("Error getting fix pack number");

			return getDatabaseMajorVersion() > 11
                                || getDatabaseMajorVersion() == 11 && ( getDatabaseMinorVersion() == 1 && fixPack >= 1 || getDatabaseMinorVersion() > 1 );

		} catch (final DatabaseException e) {
			return false; // assume not
		}
	}

	private Integer getDb2FixPack() {
		if (getConnection() == null || getConnection() instanceof OfflineConnection)
			return null;
		try {
			return Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).queryForObject(
					new RawParameterizedSqlStatement("SELECT fixpack_num FROM TABLE (sysproc.env_get_inst_info()) as INSTANCEINFO"),
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

	@Override
	public boolean supportsCreateIfNotExists(Class<? extends DatabaseObject> type) {
		return type.isAssignableFrom(Table.class);
	}

	@Override
	public boolean supportsDatabaseChangeLogHistory() {
		return true;
	}
}
