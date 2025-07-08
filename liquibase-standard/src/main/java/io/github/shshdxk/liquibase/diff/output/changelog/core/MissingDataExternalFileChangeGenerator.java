package io.github.shshdxk.liquibase.diff.output.changelog.core;

import io.github.shshdxk.liquibase.Scope;
import io.github.shshdxk.liquibase.change.Change;
import io.github.shshdxk.liquibase.change.core.LoadDataChange;
import io.github.shshdxk.liquibase.change.core.LoadDataColumnConfig;
import io.github.shshdxk.liquibase.GlobalConfiguration;
import io.github.shshdxk.liquibase.database.Database;
import io.github.shshdxk.liquibase.database.jvm.JdbcConnection;
import io.github.shshdxk.liquibase.diff.output.DiffOutputControl;
import io.github.shshdxk.liquibase.diff.output.changelog.ChangeGeneratorChain;
import io.github.shshdxk.liquibase.exception.UnexpectedLiquibaseException;
import io.github.shshdxk.liquibase.resource.OpenOptions;
import io.github.shshdxk.liquibase.resource.PathHandlerFactory;
import io.github.shshdxk.liquibase.resource.Resource;
import io.github.shshdxk.liquibase.servicelocator.LiquibaseService;
import io.github.shshdxk.liquibase.structure.DatabaseObject;
import io.github.shshdxk.liquibase.structure.core.Data;
import io.github.shshdxk.liquibase.structure.core.Table;
import io.github.shshdxk.liquibase.util.ISODateFormat;
import io.github.shshdxk.liquibase.util.JdbcUtil;
import io.github.shshdxk.liquibase.util.csv.CSVWriter;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@LiquibaseService(skip = true)
public class MissingDataExternalFileChangeGenerator extends MissingDataChangeGenerator {

    private String dataDir;

    public MissingDataExternalFileChangeGenerator(String dataDir) {
        this.dataDir = dataDir;
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Data.class.isAssignableFrom(objectType)) {
            return PRIORITY_ADDITIONAL;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl outputControl, Database referenceDatabase, Database comparisionDatabase, ChangeGeneratorChain chain) {
    
        ResultSet rs = null;
        try (
            Statement stmt = ((JdbcConnection) referenceDatabase.getConnection()).createStatement(
                ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)
        )
        {
            Data data = (Data) missingObject;

            Table table = data.getTable();
            if (referenceDatabase.isLiquibaseObject(table)) {
                return null;
            }

            String sql = "SELECT * FROM " + referenceDatabase.escapeTableName(table.getSchema().getCatalogName(), table.getSchema().getName(), table.getName());

            
            stmt.setFetchSize(100);
            rs = stmt.executeQuery(sql);

            if (rs.isBeforeFirst()) {
                List<String> columnNames = new ArrayList<>();
                for (int i = 0; i < rs.getMetaData().getColumnCount(); i++) {
                    columnNames.add(rs.getMetaData().getColumnName(i + 1));
                }

                final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
                String fileName = table.getName().toLowerCase() + ".csv";
                Resource externalFileResource = pathHandlerFactory.getResource(fileName);
                if (dataDir != null) {
                    Resource dataDirResource = pathHandlerFactory.getResource(dataDir);
                    externalFileResource = dataDirResource.resolve(fileName);
                }

                String[] dataTypes = new String[0];
                try (
                        OutputStream fileOutputStream = externalFileResource.openOutputStream(new OpenOptions());
                        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
                                fileOutputStream, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());
                        CSVWriter outputFile = new CSVWriter(new BufferedWriter(outputStreamWriter))
                ) {

                    dataTypes = new String[columnNames.size()];
                    String[] line = new String[columnNames.size()];
                    for (int i = 0; i < columnNames.size(); i++) {
                        line[i] = columnNames.get(i);
                    }
                    outputFile.writeNext(line);

                    int rowNum = 0;
                    while (rs.next()) {
                        line = new String[columnNames.size()];

                        for (int i = 0; i < columnNames.size(); i++) {
                            Object value = JdbcUtil.getResultSetValue(rs, i + 1);
                            if ((dataTypes[i] == null) && (value != null)) {
                                if (value instanceof Number) {
                                    dataTypes[i] = "NUMERIC";
                                } else if (value instanceof Boolean) {
                                    dataTypes[i] = "BOOLEAN";
                                } else if (value instanceof Date) {
                                    dataTypes[i] = "DATE";
                                } else if (value instanceof byte[]) {
                                    dataTypes[i] = "BLOB";
                                } else {
                                    dataTypes[i] = "STRING";
                                }
                            }
                            if (value == null) {
                                line[i] = "NULL";
                            } else {
                                if (value instanceof Date) {
                                    line[i] = new ISODateFormat().format(((Date) value));
                                } else if (value instanceof byte[]) {
                                    // extract the value as a Base64 string, to safely store the
                                    // binary data
                                    line[i] = Base64.getEncoder().encodeToString((byte[])value);
                                } else {
                                    line[i] = value.toString();
                                }
                            }
                        }
                        outputFile.writeNext(line);
                        rowNum++;
                        if ((rowNum % 5000) == 0) {
                            outputFile.flush();
                        }
                    }
                }

                LoadDataChange change = new LoadDataChange();
                change.setFile(externalFileResource.getPath());
                change.setEncoding(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue());
                if (outputControl.getIncludeCatalog()) {
                    change.setCatalogName(table.getSchema().getCatalogName());
                }
                if (outputControl.getIncludeSchema()) {
                    change.setSchemaName(table.getSchema().getName());
                }
                change.setTableName(table.getName());

                for (int i = 0; i < columnNames.size(); i++) {
                    String colName = columnNames.get(i);
                    LoadDataColumnConfig columnConfig = new LoadDataColumnConfig();
                    columnConfig.setHeader(colName);
                    columnConfig.setName(colName);
                    columnConfig.setType(dataTypes[i] != null ? dataTypes[i] : "skip");

                    change.addColumn(columnConfig);
                }
                return new Change[]{
                        change
                };
            }
            return Change.EMPTY_CHANGE;
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignore) {
                    // nothing can be done
                } // try...
            } // rs == null?
        } // try... finally
    } // method fixMissing
} // class MissingDataExternalFileChangeGenerator
