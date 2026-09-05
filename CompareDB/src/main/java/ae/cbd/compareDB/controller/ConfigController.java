//package ae.cbd.compareDB.controller;
//
////import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import java.io.*;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
////@Controller
//public class ConfigController {
//
//    private static final String CONFIG_FILE_PATH = "src/main/resources/application.properties";
//
//    private static final Map<String, String> DRIVER_MAP = new LinkedHashMap<>();
//
//    static {
//        DRIVER_MAP.put("mysql", "com.mysql.cj.jdbc.Driver");
//        DRIVER_MAP.put("mssql", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
//        DRIVER_MAP.put("oracle", "oracle.jdbc.OracleDriver");
//    }
//
//    @PostMapping("/db-comparer/uploadConfig")
//    public String uploadConfigFromFile(@RequestParam("filePath") String filePath) {
//        File inputFile = new File(filePath);
//        if (!inputFile.exists()) {
//            System.out.println("Input file not found: " + filePath);
//            return "redirect:/error";
//        }
//
//        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
//            StringBuilder fileContent = new StringBuilder();
//            Map<String, String> dbProperties = new LinkedHashMap<>();
//            boolean dbSectionStarted = false;
//
//            String line;
//            while ((line = reader.readLine()) != null) {
//                if (line.startsWith("dbA.") || line.startsWith("dbB.")) {
//                    dbSectionStarted = true;
//                }
//
//                if (!dbSectionStarted) {
//                    fileContent.append(line).append("\n");
//                } else {
//                    String[] keyValue = line.split("=", 2);
//                    if (keyValue.length == 2) {
//                        String key = keyValue[0].trim();
//                        String value = keyValue[1].trim();
//
//                        if (key.equals("dbA.type") || key.equals("dbB.type")) {
//                            value = DRIVER_MAP.getOrDefault(value.toLowerCase(), "unknown.driver");
//                            key = key.replace("type", "driver");
//                        }
//
//                        dbProperties.put(key, value);
//                    }
//                }
//            }
//
//            FileWriter writer = new FileWriter(CONFIG_FILE_PATH);
//            writer.write(fileContent.toString());
//
//            for (Map.Entry<String, String> entry : dbProperties.entrySet()) {
//                writer.write(entry.getKey() + "=" + entry.getValue() + "\n");
//            }
//
//            writer.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return "redirect:/";
//    }
//}
//
//
//
////package ae.cbd.compareDB.controller;
////
////import org.springframework.stereotype.Controller;
////import org.springframework.web.bind.annotation.PostMapping;
////import org.springframework.web.bind.annotation.RequestParam;
////
////import java.io.*;
////import java.util.LinkedHashMap;
////import java.util.Map;
////
////@Controller
////public class ConfigController {
////
////    private static final String CONFIG_FILE_PATH = "src/main/resources/application.properties"; 
////
////    private static final Map<String, String> DRIVER_MAP = new LinkedHashMap<>();
////    
////    static {
////        DRIVER_MAP.put("mysql", "com.mysql.cj.jdbc.Driver");
////        DRIVER_MAP.put("mssql", "com.microsoft.sqlserver.jdbc.SQLServerDriver");
////        DRIVER_MAP.put("oracle", "oracle.jdbc.OracleDriver");
////    }
////
////    @PostMapping("/db-comparer/saveConfig")
////    public String saveDatabaseConfig(@RequestParam String dbAType, @RequestParam String dbAUrl, 
////                                     @RequestParam String dbAUser, @RequestParam String dbAPass, 
////                                     @RequestParam String dbAName, @RequestParam String dbBType, 
////                                     @RequestParam String dbBUrl, @RequestParam String dbBUser, 
////                                     @RequestParam String dbBPass, @RequestParam String dbBName) {
////        try {
////            File file = new File(CONFIG_FILE_PATH);
////            BufferedReader reader = new BufferedReader(new FileReader(file));
////            StringBuilder fileContent = new StringBuilder();
////            Map<String, String> dbProperties = new LinkedHashMap<>();
////            boolean dbSectionStarted = false;
////
////            String line;
////            while ((line = reader.readLine()) != null) {
////                if (line.startsWith("dbA.") || line.startsWith("dbB.")) {
////                    dbSectionStarted = true;
////                    continue; 
////                }
////                if (!dbSectionStarted) {
////                    fileContent.append(line).append("\n"); 
////                }
////            }
////            reader.close();
////
////            String dbADriver = DRIVER_MAP.get(dbAType);
////            String dbBDriver = DRIVER_MAP.get(dbBType);
////
////            dbProperties.put("dbA.url", dbAUrl);
////            dbProperties.put("dbA.username", dbAUser);
////            dbProperties.put("dbA.password", dbAPass);
////            dbProperties.put("dbA.name", dbAName);
////            dbProperties.put("dbA.driver", dbADriver);
////            dbProperties.put("dbB.url", dbBUrl);
////            dbProperties.put("dbB.username", dbBUser);
////            dbProperties.put("dbB.password", dbBPass);
////            dbProperties.put("dbB.name", dbBName);
////            dbProperties.put("dbB.driver", dbBDriver);
////
////            for (Map.Entry<String, String> entry : dbProperties.entrySet()) {
////                fileContent.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
////            }
////
////            // Write back to the file
////            FileWriter writer = new FileWriter(file);
////            writer.write(fileContent.toString());
////            writer.close();
////
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        return "redirect:/";
////    }
////}
//
//
////package ae.cbd.compareDB.controller;
////
////import org.springframework.stereotype.Controller;
////import org.springframework.web.bind.annotation.PostMapping;
////import org.springframework.web.bind.annotation.RequestParam;
////
////import java.io.*;
////import java.util.LinkedHashMap;
////import java.util.Map;
////import java.util.Properties;
////
////@Controller
////public class ConfigController {
////
////    private static final String CONFIG_FILE_PATH = "src/main/resources/application.properties"; // Update path if needed
////
////    @PostMapping("/db-comparer/saveConfig")
////    public String saveDatabaseConfig(@RequestParam String dbAUrl, @RequestParam String dbAUser,
////                                     @RequestParam String dbAPass, @RequestParam String dbAName,
////                                     @RequestParam String dbBUrl, @RequestParam String dbBUser,
////                                     @RequestParam String dbBPass, @RequestParam String dbBName) {
////        try {
////            // Read the existing properties file
////            File file = new File(CONFIG_FILE_PATH);
////            BufferedReader reader = new BufferedReader(new FileReader(file));
////            StringBuilder fileContent = new StringBuilder();
////            Properties props = new Properties();
////            Map<String, String> dbProperties = new LinkedHashMap<>();
////            boolean dbSectionStarted = false;
////
////            String line;
////            while ((line = reader.readLine()) != null) {
////                if (line.startsWith("dbA.") || line.startsWith("dbB.")) {
////                    dbSectionStarted = true;
////                    continue; // Skip existing database properties
////                }
////                if (!dbSectionStarted) {
////                    fileContent.append(line).append("\n"); // Preserve initial properties
////                }
////            }
////            reader.close();
////
////            // Set new database properties
////            dbProperties.put("dbA.url", dbAUrl);
////            dbProperties.put("dbA.username", dbAUser);
////            dbProperties.put("dbA.password", dbAPass);
////            dbProperties.put("dbA.name", dbAName);
////            dbProperties.put("dbB.url", dbBUrl);
////            dbProperties.put("dbB.username", dbBUser);
////            dbProperties.put("dbB.password", dbBPass);
////            dbProperties.put("dbB.name", dbBName);
////
////            // Append updated database properties to file content
////            for (Map.Entry<String, String> entry : dbProperties.entrySet()) {
////                fileContent.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
////            }
////
////            // Write back to the file
////            FileWriter writer = new FileWriter(file);
////            writer.write(fileContent.toString());
////            writer.close();
////
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
////        return "redirect:/";
////    }
////}
////
