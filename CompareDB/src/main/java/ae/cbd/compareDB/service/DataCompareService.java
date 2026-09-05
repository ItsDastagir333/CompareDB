package ae.cbd.compareDB.service;

import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
public class DataCompareService {

    private static final int BATCH_SIZE = 1000;

    public String compareTableData(Connection connA, Connection connB, String tableAName, String tableBName) throws SQLException {
        StringBuilder output = new StringBuilder();
        output.append("\n===== DATA COMPARISON FOR TABLE: ").append(tableAName).append(" VS ").append(tableAName).append(" =====\n");

        String queryA = "SELECT * FROM " + tableAName + " LIMIT ? OFFSET ?";
        String queryB = "SELECT * FROM " + tableBName + " LIMIT ? OFFSET ?";

        int offset = 0;
        boolean hasMoreData = true;
        List<String> mismatches = new ArrayList<>();

        while (hasMoreData) {
            try (PreparedStatement stmtA = connA.prepareStatement(queryA);
                 PreparedStatement stmtB = connB.prepareStatement(queryB)) {

                stmtA.setInt(1, BATCH_SIZE);
                stmtA.setInt(2, offset);
                stmtB.setInt(1, BATCH_SIZE);
                stmtB.setInt(2, offset);

                try (ResultSet rsA = stmtA.executeQuery();
                     ResultSet rsB = stmtB.executeQuery()) {

                    hasMoreData = compareResultSets(rsA, rsB, mismatches);
                }
            }
            offset += BATCH_SIZE;
        }

        if (mismatches.isEmpty()) {
            output.append("No differences found in table ").append(tableAName).append(" & ").append(tableAName).append("\n");
        } else {
            output.append("Mismatches found:\n");
            for (String mismatch : mismatches) {
                output.append(mismatch).append("\n");
            }
        }

        return output.toString();
    }


    private boolean compareResultSets(ResultSet rsA, ResultSet rsB, List<String> mismatches) throws SQLException {
        boolean hasMoreData = false;
        int rowNumber = 1;

        while (rsA.next() && rsB.next()) {
            int columnCount = rsA.getMetaData().getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                String columnName = rsA.getMetaData().getColumnName(i);
                Object valueA = rsA.getObject(i);
                Object valueB = rsB.getObject(i);

                if (!Objects.equals(valueA, valueB)) {
                    String mismatchInfo = "Row " + rowNumber + " | Column: " + columnName + " | (DB A: " + valueA + " | DB B: " + valueB + ")";
                    mismatches.add(mismatchInfo);
                }
            }
            rowNumber++;
            hasMoreData = true;
        }

        // Handle missing rows in A or B
        if (rsA.next()) {
            mismatches.add("Extra rows found in Database A starting from row " + rowNumber);
        } else if (rsB.next()) {
            mismatches.add("Extra rows found in Database B starting from row " + rowNumber);
        }

        return hasMoreData;
    }

}
