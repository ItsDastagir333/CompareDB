// ...existing code...
package ae.cbd.compareDB.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ae.cbd.compareDB.config.DatabaseConfig;

@Service
public class DbCompareService {

    @Autowired
    private DatabaseConfig dbConf;

    private final DataSource dataSourceA;
    private final DataSource dataSourceB;
    private final DataCompareService dataCompareService;

    public DbCompareService(@Qualifier("dataSourceA") DataSource dataSourceA,
                            @Qualifier("dataSourceB") DataSource dataSourceB,
                            DataCompareService dataCompareService) {
        this.dataSourceA = dataSourceA;
        this.dataSourceB = dataSourceB;
        this.dataCompareService = dataCompareService;
    }

    public void compareSchemasAndWriteToFile(String outputPath) throws Exception {
        File outputFile = new File(outputPath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write("===== DATABASE COMPARISON REPORT =====\n\n");

            try (Connection connA = dataSourceA.getConnection();
                 Connection connB = dataSourceB.getConnection()) {

                DatabaseMetaData metaA = connA.getMetaData();
                DatabaseMetaData metaB = connB.getMetaData();

                String dbAName = connA.getCatalog();
                String dbBName = connB.getCatalog();

                writer.write("Database A: " + dbAName + "\n");
                writer.write("Database B: " + dbBName + "\n\n");

                String compareMode = dbConf.getCompareMode(); // expected "ALL" or "TABLE"
                if (compareMode != null && "TABLE".equalsIgnoreCase(compareMode)) {
                    String tableA = dbConf.getDbATable();
                    String tableB = dbConf.getDbBTable();

                    if (tableA == null || tableA.trim().isEmpty() || tableB == null || tableB.trim().isEmpty()) {
                        writer.write("ERROR: compare.mode=TABLE but one or both table names are missing in configuration.\n");
                        return;
                    }

                    // Compare just the specified table pair
                    compareSpecificTable(metaA, metaB, connA, connB, writer, tableA.trim(), tableB.trim());

                } else {
                    // Full database comparison
                    compareTables(metaA, metaB, connA, connB, writer);
                }
            } catch (SQLException e) {
                writer.write("ERROR: Database connection error - " + e.getMessage() + "\n");
            }
        }
    }

    private void compareTables(DatabaseMetaData metaA, DatabaseMetaData metaB, Connection connA, Connection connB, BufferedWriter writer) throws SQLException, IOException {
        Set<String> tableSetA = getTables(metaA, dbConf.getDbAName());
        Set<String> tableSetB = getTables(metaB, dbConf.getDbBName());

        writer.write("===== TABLE COMPARISON =====\n");

        writer.write("Tables in Database A: " + tableSetA + "\n");
        writer.write("Tables in Database B: " + tableSetB + "\n\n");

        Set<String> missingInB = new HashSet<>(tableSetA);
        missingInB.removeAll(tableSetB);
        writer.write("Tables present in Database A but missing in Database B:\n" + formatSet(missingInB) + "\n");

        Set<String> missingInA = new HashSet<>(tableSetB);
        missingInA.removeAll(tableSetA);
        writer.write("Tables present in Database B but missing in Database A:\n" + formatSet(missingInA) + "\n");

        Set<String> commonTables = new HashSet<>(tableSetA);
        commonTables.retainAll(tableSetB);
        writer.write("\nCommon Tables: " + commonTables + "\n\n");

        compareColumns(metaA, metaB, commonTables, connA, connB, writer);
    }

    /**
     * Compare a single pair of tables (tableA in DB A vs tableB in DB B).
     * This supports cases where names are the same or different.
     */
    private void compareSpecificTable(DatabaseMetaData metaA, DatabaseMetaData metaB,
                                      Connection connA, Connection connB, BufferedWriter writer,
                                      String tableA, String tableB) throws SQLException, IOException {
        writer.write("===== SPECIFIC TABLE COMPARISON =====\n");
        writer.write("Comparing DB A table: " + tableA + "  <=>  DB B table: " + tableB + "\n\n");

        // Check existence
        Set<String> tablesA = getTables(metaA, dbConf.getDbAName());
        Set<String> tablesB = getTables(metaB, dbConf.getDbBName());

        boolean existsA = tablesA.contains(tableA);
        boolean existsB = tablesB.contains(tableB);

        if (!existsA) {
            writer.write("Table " + tableA + " does not exist in Database A.\n");
        }
        if (!existsB) {
            writer.write("Table " + tableB + " does not exist in Database B.\n");
        }
        if (!existsA || !existsB) {
            return;
        }

        // Column comparison between tableA and tableB
        Map<String, String> columnMapA = getColumnDetails(metaA, dbConf.getDbAName(), tableA);
        Map<String, String> columnMapB = getColumnDetails(metaB, dbConf.getDbBName(), tableB);

        writer.write("Columns in " + tableA + " (A): " + columnMapA.keySet() + "\n");
        writer.write("Columns in " + tableB + " (B): " + columnMapB.keySet() + "\n");

        Set<String> missingColumnsB = new HashSet<>(columnMapA.keySet());
        missingColumnsB.removeAll(columnMapB.keySet());
        writer.write("Columns in " + tableA + " (A) but missing in " + tableB + " (B):\n" + formatSet(missingColumnsB) + "\n");

        Set<String> missingColumnsA = new HashSet<>(columnMapB.keySet());
        missingColumnsA.removeAll(columnMapA.keySet());
        writer.write("Columns in " + tableB + " (B) but missing in " + tableA + " (A):\n" + formatSet(missingColumnsA) + "\n");

        Set<String> differentTypeColumns = new HashSet<>();
        for (String column : columnMapA.keySet()) {
            if (columnMapB.containsKey(column) && !columnMapA.get(column).equals(columnMapB.get(column))) {
                differentTypeColumns.add(column + " (A: " + columnMapA.get(column) + " | B: " + columnMapB.get(column) + ")");
            }
        }
        writer.write("Columns with Different Data Types:\n" + formatSet(differentTypeColumns) + "\n");

        // If no structural differences, perform data comparison
        if (missingColumnsA.isEmpty() && missingColumnsB.isEmpty() && differentTypeColumns.isEmpty()) {
            writer.write("-> Proceeding with data comparison for table pair: " + tableA + " <=> " + tableB + "\n");
            String comparisonResult = dataCompareService.compareTableData(connA, connB, tableA, tableB);
            writer.write(comparisonResult + "\n");
            writer.write("\n");
        }
        writer.write("---------------------------------------------------\n");
    }

    private void compareColumns(DatabaseMetaData metaA, DatabaseMetaData metaB, Set<String> commonTables, Connection connA, Connection connB, BufferedWriter writer) throws SQLException, IOException {
        writer.write("===== COLUMN COMPARISON =====\n");

        for (String table : commonTables) {
            writer.write("\nTable: " + table + "\n");

            Map<String, String> columnMapA = getColumnDetails(metaA, dbConf.getDbAName(), table);
            Map<String, String> columnMapB = getColumnDetails(metaB, dbConf.getDbBName(), table);

            Set<String> missingColumnsB = new HashSet<>(columnMapA.keySet());
            missingColumnsB.removeAll(columnMapB.keySet());
            writer.write("Columns in " + table + " (A) but missing in (B):\n" + formatSet(missingColumnsB) + "\n");

            Set<String> missingColumnsA = new HashSet<>(columnMapB.keySet());
            missingColumnsA.removeAll(columnMapA.keySet());
            writer.write("Columns in " + table + " (B) but missing in (A):\n" + formatSet(missingColumnsA) + "\n");

            Set<String> differentTypeColumns = new HashSet<>();
            for (String column : columnMapA.keySet()) {
                if (columnMapB.containsKey(column) && !columnMapA.get(column).equals(columnMapB.get(column))) {
                    differentTypeColumns.add(column + " (A: " + columnMapA.get(column) + " | B: " + columnMapB.get(column) + ")");
                }
            }
            writer.write("Columns with Different Data Types:\n" + formatSet(differentTypeColumns) + "\n");

            // If no structural differences, perform data comparison
            if (missingColumnsA.isEmpty() && missingColumnsB.isEmpty() && differentTypeColumns.isEmpty()) {
                writer.write("-> Proceeding with data comparison for table: " + table + "\n");
                String comparisonResult = dataCompareService.compareTableData(connA, connB, table, table);
                writer.write(comparisonResult + "\n");
                writer.write("\n");
            }
            writer.write("---------------------------------------------------\n");
        }
    }

    private Map<String, String> getColumnDetails(DatabaseMetaData metaData, String dbName, String table) throws SQLException {
        Map<String, String> columnMap = new HashMap<>();
        try (ResultSet columns = metaData.getColumns(dbName, null, table, "%")) {
            while (columns.next()) {
                columnMap.put(columns.getString("COLUMN_NAME"), columns.getString("TYPE_NAME"));
            }
        }
        return columnMap;
    }

    private Set<String> getTables(DatabaseMetaData metaData, String dbName) throws SQLException {
        Set<String> tableSet = new HashSet<>();
        try (ResultSet tables = metaData.getTables(dbName, null, "%", new String[]{"TABLE"})) {
            while (tables.next()) {
                tableSet.add(tables.getString("TABLE_NAME"));
            }
        }
        return tableSet;
    }

    private String formatSet(Set<String> set) {
        return set.isEmpty() ? "None\n" : String.join(", ", set) + "\n";
    }

}