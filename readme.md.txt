

# 📊 CompareDB - Database Comparer Utility

**CompareDB** is a lightweight **standalone desktop application** that allows users to **compare the schema and data** of two different databases easily and also specifically any two tables of databases also 
The application is packaged as a **runnable JAR** file and comes with a **simple UI** !

---

## 🚀 How to Use

1. **Run the Application:**
   - Double-click the `CompareDB.jar` file  
   *(or run it using the command `java -jar CompareDB.jar` if needed)*.

2. **Fill in the Database Details:**
   - The **UI will pop up** asking for:
     - Database A Connection Details (URL, Username, Password, Type)
     - Database B Connection Details (URL, Username, Password, Type)
     - If want to have comparison between any two Specific tables, then click on Toggle button with title "Compare Mode". Fields for inserting table names for both the databases will appear, put in your table name 

3. **Submit:**
   - Click the **"Submit"** button.
   - The application will save your input internally and begin the **comparison process**.

4. **Completion:**
   - Once the comparison is done, you will see a **Success Alert** saying **"Database Comparison Completed"**.

5. **Output:**
   - A file named **`db_output.txt`** will be generated **at the same location** where the `CompareDB.jar` exists.
   - This file contains the **comparison results** (schema and data differences, if any).

---

## 🛠 Features

- **Simple and clean UI** — no command-line hassle
- **Supports multiple database types** (MySQL, SQL Server, Oracle)
- **Connects to local and remote databases** dynamically
- **Automatic file generation**:
  - `dbInput.txt` — stores your provided database details
  - `db_output.txt` — stores the comparison results
- **Cross-platform**: Works anywhere Java is installed
- **Fully self-contained**: No additional setup required after download
- **Specific Table Comparison Functionality**: If we want we can compare two specific tables from databases also

---

## 📋 Requirements

- **Java 11** or above installed on your system
- **Valid database credentials** and accessible database instances

---

## 📦 Folder Structure After Running

```
/CompareDB/
├── CompareDB.jar
├── dbInput.txt      (generated automatically after filling UI)
├── db_output.txt    (generated after comparison)
```

---

## ❓ Troubleshooting

- Make sure your database server allows connections from your IP address.
- Ensure database ports (like **3306** for MySQL or **1433** for MSSQL) are open.
- If using cloud databases, whitelist your IP and use the full JDBC URL provided by your cloud provider.

---

## 📞 Support

If you encounter any issues or have feature suggestions, feel free to reach out me at dastagir.mulani@accenture.com

---