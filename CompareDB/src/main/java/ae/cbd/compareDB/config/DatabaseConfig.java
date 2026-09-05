package ae.cbd.compareDB.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DatabaseConfig {

	private static final String BASE_PATH = System.getProperty("user.dir");
	private static final String configFilePath = BASE_PATH + "/dbInput.txt";
//    private final String configFilePath = "C:/Users/dastagir.mulani/Documents/Project Work/dbInput.txt";

    private String dbAUrl;
    private String dbAName;
    private String dbAUsername;
    private String dbAPassword;
    private String dbADriver;
    private String dbATable;

    private String dbBUrl;
    private String dbBName;
    private String dbBUsername;
    private String dbBPassword;
    private String dbBDriver;
    private String dbBTable;
    
    private String comparisonMode;

    public DatabaseConfig() {
        loadProperties();
    }

    private void loadProperties() {
        Map<String, String> properties = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(configFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("=")) {
                    String[] keyValue = line.split("=", 2);
                    properties.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
            System.out.println("Loaded latest properties from dbInput.txt file.");

            dbAUrl = properties.get("dbA.url");
            dbAName = properties.get("dbA.name");
            dbAUsername = properties.get("dbA.username");
            dbAPassword = properties.get("dbA.password");
            dbADriver = properties.get("dbA.driver");
            dbATable = properties.get("dbA.table");
            
            dbBUrl = properties.get("dbB.url");
            dbBName = properties.get("dbB.name");
            dbBUsername = properties.get("dbB.username");
            dbBPassword = properties.get("dbB.password");
            dbBDriver = properties.get("dbB.driver");
            dbBTable = properties.get("dbB.table");
            
            comparisonMode = properties.get("compare.mode");

        } catch (IOException e) {
            System.err.println("Error loading properties from dbInput.txt: " + e.getMessage());
        }
    }

    public void reloadProperties() {
        loadProperties();
    }

    public String getDbAUrl() { return dbAUrl; }
    public String getDbAName() { return dbAName; }
    public String getDbAUsername() { return dbAUsername; }
    public String getDbAPassword() { return dbAPassword; }
    public String getDbADriver() { return dbADriver; }
    public String getDbATable() { return dbATable; }

    public String getDbBUrl() { return dbBUrl; }
    public String getDbBName() { return dbBName; }
    public String getDbBUsername() { return dbBUsername; }
    public String getDbBPassword() { return dbBPassword; }
    public String getDbBDriver() { return dbBDriver; }
    public String getDbBTable() { return dbBTable; }
    
    public String getCompareMode() { return comparisonMode; }
    
    @Bean("dataSourceA")
    public DataSource dataSourceA() {
        DriverManagerDataSource dataSourceA = new DriverManagerDataSource();
        dataSourceA.setDriverClassName(dbADriver);
        dataSourceA.setUrl(dbAUrl);
        dataSourceA.setUsername(dbAUsername);
        dataSourceA.setPassword(dbAPassword);
        return dataSourceA;
    }

    @Bean("dataSourceB")
    public DataSource dataSourceB() {
        DriverManagerDataSource dataSourceB = new DriverManagerDataSource();
        dataSourceB.setDriverClassName(dbBDriver);
        dataSourceB.setUrl(dbBUrl);
        dataSourceB.setUsername(dbBUsername);
        dataSourceB.setPassword(dbBPassword);
        return dataSourceB;
    }
}
