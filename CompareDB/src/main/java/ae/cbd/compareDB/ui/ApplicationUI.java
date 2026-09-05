package ae.cbd.compareDB.ui;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;

import ae.cbd.compareDB.CompareDbApplication;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ApplicationUI extends Application {
	
	private static final String BASE_PATH = System.getProperty("user.dir");
	private static final String CONFIG_FILE_PATH = BASE_PATH + "/dbInput.txt";

//    private static final String CONFIG_FILE_PATH = "C:/Users/dastagir.mulani/Documents/Project Work/dbInput.txt";
    private static final Map<String, String> DRIVER_MAP = new LinkedHashMap<>();
    
    static {
    	DRIVER_MAP.put("SQL Server", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
        DRIVER_MAP.put("MySQL", "com.mysql.cj.jdbc.Driver");
        DRIVER_MAP.put("Oracle", "oracle.jdbc.OracleDriver");
    }
    
    private static final String[] PERSISTENT_CONFIG = {
        "spring.application.name=CompareDB",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
        "spring.data.jpa.repositories.enabled=false",
        "server.port=8081"
    };
    
    private ApplicationContext context;
    private CompareDbApplication compareDbApplication;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Database Configuration");

        TextField dbAUrl = new TextField();
        TextField dbAUsername = new TextField();
        PasswordField dbAPassword = new PasswordField();
        TextField dbAName = new TextField();
        ComboBox<String> dbAType = new ComboBox<>();
        dbAType.getItems().addAll(DRIVER_MAP.keySet());

        TextField dbBUrl = new TextField();
        TextField dbBUsername = new TextField();
        PasswordField dbBPassword = new PasswordField();
        TextField dbBName = new TextField();
        ComboBox<String> dbBType = new ComboBox<>();
        dbBType.getItems().addAll(DRIVER_MAP.keySet());

        Button submitButton = new Button("Submit");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("DB A URL:"), 0, 0);
        grid.add(dbAUrl, 1, 0);
        grid.add(new Label("DB A Username:"), 0, 1);
        grid.add(dbAUsername, 1, 1);
        grid.add(new Label("DB A Password:"), 0, 2);
        grid.add(dbAPassword, 1, 2);
        grid.add(new Label("DB A Name:"), 0, 3);
        grid.add(dbAName, 1, 3);
        grid.add(new Label("DB A Type:"), 0, 4);
        grid.add(dbAType, 1, 4);

        grid.add(new Label("DB B URL:"), 0, 5);
        grid.add(dbBUrl, 1, 5);
        grid.add(new Label("DB B Username:"), 0, 6);
        grid.add(dbBUsername, 1, 6);
        grid.add(new Label("DB B Password:"), 0, 7);
        grid.add(dbBPassword, 1, 7);
        grid.add(new Label("DB B Name:"), 0, 8);
        grid.add(dbBName, 1, 8);
        grid.add(new Label("DB B Type:"), 0, 9);
        grid.add(dbBType, 1, 9);

        // Compare mode toggle (off = Whole DB, on = Specific Table)
        grid.add(new Label("Compare Mode:"), 0, 10);
        ToggleButton tbSpecificTable = new ToggleButton("Specific Table");
        grid.add(tbSpecificTable, 1, 10);

        // Table name fields for DB A and DB B (hidden unless Specific Table selected)
        Label tableALabel = new Label("Table Name (DB A):");
        TextField tableAField = new TextField();
        Label tableBLabel = new Label("Table Name (DB B):");
        TextField tableBField = new TextField();

        tableALabel.setVisible(false);
        tableALabel.setManaged(false);
        tableAField.setVisible(false);
        tableAField.setManaged(false);
        tableBLabel.setVisible(false);
        tableBLabel.setManaged(false);
        tableBField.setVisible(false);
        tableBField.setManaged(false);

        grid.add(tableALabel, 0, 11);
        grid.add(tableAField, 1, 11);
        grid.add(tableBLabel, 0, 12);
        grid.add(tableBField, 1, 12);

        // Move submit button down
        grid.add(submitButton, 1, 13);

        // Show/hide table name fields when toggle changes
        tbSpecificTable.selectedProperty().addListener((obs, oldV, newV) -> {
            boolean isTable = Boolean.TRUE.equals(newV);
            tableALabel.setVisible(isTable);
            tableALabel.setManaged(isTable);
            tableAField.setVisible(isTable);
            tableAField.setManaged(isTable);
            tableBLabel.setVisible(isTable);
            tableBLabel.setManaged(isTable);
            tableBField.setVisible(isTable);
            tableBField.setManaged(isTable);
        });

        submitButton.setOnAction(e -> {
            // If Specific Table is selected, validate both table names
            if (tbSpecificTable.isSelected()) {
                if (tableAField.getText() == null || tableAField.getText().trim().isEmpty()
                        || tableBField.getText() == null || tableBField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Please provide table names for both DB A and DB B.", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
            }

            String compareMode = tbSpecificTable.isSelected() ? "TABLE" : "ALL";

            saveProperties(
                dbAUrl.getText(), dbAUsername.getText(), dbAPassword.getText(), dbAName.getText(), dbAType.getValue(),
                dbBUrl.getText(), dbBUsername.getText(), dbBPassword.getText(), dbBName.getText(), dbBType.getValue(),
                compareMode, tableAField.getText(), tableBField.getText()
            );
            // Start Spring Boot AFTER saving properties
            startSpringBoot();
        });

        Scene scene = new Scene(grid, 500, 520);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void saveProperties(String dbAUrl, String dbAUsername, String dbAPassword, String dbAName, String dbAType,
                                String dbBUrl, String dbBUsername, String dbBPassword, String dbBName, String dbBType,
                                String compareMode, String tableAName, String tableBName) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE_PATH))) {
            // Writing database A properties
            writer.write("dbA.url=" + dbAUrl);
            writer.newLine();
            writer.write("dbA.username=" + dbAUsername);
            writer.newLine();
            writer.write("dbA.password=" + dbAPassword);
            writer.newLine();
            writer.write("dbA.name=" + dbAName);
            writer.newLine();
            writer.write("dbA.type=" + dbAType);
            writer.newLine();
            writer.write("dbA.driver=" + DRIVER_MAP.getOrDefault(dbAType, ""));
            writer.newLine();

            // Writing database B properties
            writer.write("dbB.url=" + dbBUrl);
            writer.newLine();
            writer.write("dbB.username=" + dbBUsername);
            writer.newLine();
            writer.write("dbB.password=" + dbBPassword);
            writer.newLine();
            writer.write("dbB.name=" + dbBName);
            writer.newLine();
            writer.write("dbB.type=" + dbBType);
            writer.newLine();
            writer.write("dbB.driver=" + DRIVER_MAP.getOrDefault(dbBType, ""));
            writer.newLine();

            // Write compare mode and optional table names
            writer.write("compare.mode=" + compareMode);
            writer.newLine();
            if ("TABLE".equalsIgnoreCase(compareMode)) {
                if (tableAName != null && !tableAName.trim().isEmpty()) {
                    writer.write("dbA.table=" + tableAName.trim());
                    writer.newLine();
                }
                if (tableBName != null && !tableBName.trim().isEmpty()) {
                    writer.write("dbB.table=" + tableBName.trim());
                    writer.newLine();
                }
            }

            // Writing persistent configurations
            for (String config : PERSISTENT_CONFIG) {
                writer.write(config);
                writer.newLine();
            }

            // Confirm save
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Configuration saved successfully!", ButtonType.OK);
            alert.show();

        } catch (IOException ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error saving configuration!", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void startSpringBoot() {
        new Thread(() -> {
            context = new SpringApplicationBuilder(CompareDbApplication.class)
                    .run(); 
            compareDbApplication = context.getBean(CompareDbApplication.class);
            compareDbApplication.startComparison(); 
            
            javafx.application.Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Database Comparison Completed");
                alert.setHeaderText(null);
                alert.setContentText("Database comparison has been completed successfully.\nPlease check db_output.txt file.");
                alert.showAndWait();
            });
            
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
