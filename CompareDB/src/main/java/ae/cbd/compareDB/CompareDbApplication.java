package ae.cbd.compareDB;

import ae.cbd.compareDB.config.DatabaseConfig;
import ae.cbd.compareDB.service.DbCompareService;
import ae.cbd.compareDB.ui.ApplicationUI;
import javafx.application.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CompareDbApplication implements CommandLineRunner {

    @Autowired
    private DbCompareService dbCompareService;
    
    @Autowired
    private DatabaseConfig databaseConfig;

    public static void main(String[] args) {
        // Launch JavaFX UI
        Application.launch(ApplicationUI.class, args);
    }

    @Override
    public void run(String... args) {
        // This will be invoked after UI input is processed.
    }

    public void startComparison() {
        try {
            System.out.println("Loading Database Configuration...");

            // Reload properties from dbInput.txt
            databaseConfig.reloadProperties();

            System.out.println("Database Configuration Loaded.");

            System.out.println("Starting Database Comparison...");

            // Define the output file path
            String BASE_PATH = System.getProperty("user.dir");
            String outputFilePath = BASE_PATH + "/dbOutput.txt";

            // Run the comparison and save results
            dbCompareService.compareSchemasAndWriteToFile(outputFilePath);

            System.out.println("Comparison completed. Output saved to: " + outputFilePath);
        } catch (Exception e) {
            System.err.println("Error during database comparison: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
