# CompareDB

CompareDB is a Java desktop application for comparing the schema and data of two databases. It provides a JavaFX configuration window, supports comparing complete databases or selected table pairs, and writes the result to `dbOutput.txt`.

## Features

- Compare tables present in each database
- Compare column names and data types
- Compare row data when the table structures match
- Compare tables with different names using `compare.mode=TABLE`
- Configure two database connections through the JavaFX UI
- Run locally with Maven or as a packaged JAR
- Run in Docker with the output and configuration stored on the host

## Requirements

### Local execution

- Java 21 JDK
- Maven 3.9+ or the included Maven Wrapper
- Network access to both database servers
- Read access to the databases being compared
- JavaFX-compatible desktop environment

### Docker execution

- Docker Engine
- A graphical display available to the container when using the JavaFX UI
- Network access from the container to both database servers
- Java 21 is not required on the host because the image contains a Java runtime

## Supported database drivers

The current project includes drivers for:

- MySQL
- Microsoft SQL Server

The UI also contains an Oracle driver option, but an Oracle JDBC dependency must be added to `pom.xml` before Oracle connections can be used.

## Configuration

The application reads `dbInput.txt` from its current working directory. Begin with the tracked template:

```bash
cp dbInput.example.txt dbInput.txt
```

Edit `dbInput.txt` with the connection details for both databases. The file contains credentials and must remain local. It is ignored by Git.

### Configuration fields

| Property | Description |
| --- | --- |
| `dbA.url` | JDBC URL for database A |
| `dbA.username` | Database A username |
| `dbA.password` | Database A password |
| `dbA.name` | Database/schema name for metadata lookup |
| `dbA.type` | Database type shown in the UI |
| `dbA.driver` | JDBC driver class |
| `dbB.*` | Equivalent settings for database B |
| `compare.mode` | `ALL` for the full database or `TABLE` for one table pair |
| `dbA.table` | Table in database A when using `TABLE` mode |
| `dbB.table` | Table in database B when using `TABLE` mode |

For remote databases, use the database server hostname or IP address. For example:

```text
jdbc:mysql://db-server.example.com:3306/classicmodels
```

Do not use `localhost` unless the database is running in the same network namespace as the application.

## Database permissions

The application only needs read access for comparison. Do not use the database administrator account in production. Create or use a dedicated read-only account according to your database platform's security policy.

For MySQL, the account must be allowed from the host seen by MySQL:

- A local application may connect as `user@localhost`.
- A Docker application may connect from a Docker network address such as `172.17.0.%`.
- A VDI application may connect from the VDI address range.

Example MySQL setup, to be executed once by a database administrator:

```sql
CREATE USER IF NOT EXISTS 'comparedb'@'172.17.0.%'
IDENTIFIED BY 'replace-with-a-strong-password';

GRANT SELECT ON database_a.* TO 'comparedb'@'172.17.0.%';
GRANT SELECT ON database_b.* TO 'comparedb'@'172.17.0.%';

FLUSH PRIVILEGES;
```

Use the correct source host or network range for your environment. Do not grant remote access to `root` unless your organization's security policy explicitly requires it.

## Run locally

Make the Maven Wrapper executable on Linux:

```bash
chmod +x mvnw
```

Build and test:

```bash
./mvnw clean test
```

Start the application:

```bash
./mvnw spring-boot:run
```

Alternatively, build a runnable JAR:

```bash
./mvnw clean package
java -jar target/CompareDB-0.0.1-SNAPSHOT.jar
```

The JavaFX window opens for database configuration. After submitting the form, the application writes `dbOutput.txt` to the current working directory.

## Run with Docker

Build the image:

```bash
docker build -t comparedb:latest .
```

On Linux, allow the container to use the host display before starting the application:

```bash
xhost +local:docker
```

Run the container from the project directory:

```bash
docker run --rm -it \
  --name comparedb \
  -e DISPLAY="$DISPLAY" \
  -v /tmp/.X11-unix:/tmp/.X11-unix:rw \
  -v "$PWD:/app/data" \
  --add-host=host.docker.internal:host-gateway \
  comparedb:latest
```

The volume maps the project directory to `/app/data`, so the following files are available on the host:

- Configuration: `dbInput.txt`
- Report: `dbOutput.txt`

Use `host.docker.internal` in a JDBC URL only when the database is running on the VDI host. For a database on another server, use that server's hostname or IP address instead.

When the database server already allows connections from the VDI but not the Docker subnet, either add a database grant for the Docker source range or run the container with a suitable network configuration. Test the organization's firewall and database security policy before changing network settings.

After use, restore the X11 access restriction if appropriate:

```bash
xhost -local:docker
```

## Reading the report

The report is written to `dbOutput.txt`.

```bash
cat dbOutput.txt
less dbOutput.txt
```

When Docker is used with the volume command above, the report is written directly to the project directory on the host.

## Comparison modes

### Complete database comparison

Set:

```text
compare.mode=ALL
```

CompareDB lists tables in both databases, identifies missing tables, compares common table columns and data types, and compares data where the structures match.

### Specific table comparison

Set:

```text
compare.mode=TABLE
dbA.table=customers
dbB.table=employees
```

This mode compares the selected table pair even when their names differ. The UI also exposes these fields when `Specific Table` is selected.

## Troubleshooting

### `Host 'x.x.x.x' is not allowed to connect`

MySQL received the connection but has no account grant matching the client's source host. Check the `user` and `host` values in MySQL, then create or update a least-privilege account for the actual application source address.

### Connection timeout or connection refused

Check the JDBC hostname, database port, server bind address, firewall rules, VPN/VDI routing, and whether the database server is running.

### The application reads the wrong configuration

`dbInput.txt` is resolved from `System.getProperty("user.dir")`. Start the application from the directory containing the intended configuration file, or mount that directory as `/app/data` when using Docker.

### The report contains an error but the terminal says comparison completed

The service writes SQL connection errors into `dbOutput.txt` and then returns to the UI flow. Always inspect the report after a run; the terminal message confirms file creation, not that the comparison succeeded.

### Docker cannot open the JavaFX window

Check `DISPLAY`, the `/tmp/.X11-unix` mount, X11 permissions, and whether the VDI provides an X server. A headless execution mode would require a separate application change because the current application starts JavaFX.

## Security notes

- Never commit `dbInput.txt`, passwords, API keys, or generated reports.
- Rotate any password that has been shared in source files, screenshots, chat, or Git history.
- Use dedicated read-only database accounts.
- Prefer private network connectivity, VPN, firewall allowlists, and TLS-enabled JDBC connections for remote databases.
- If credentials were committed previously, removing the file in a new commit is not enough; rotate the credentials and consider removing the secret from Git history.

## Project layout

```text
src/main/java/ae/cbd/compareDB/
├── CompareDbApplication.java
├── config/DatabaseConfig.java
├── service/DataCompareService.java
├── service/DbCompareService.java
└── ui/ApplicationUI.java

src/main/resources/application.properties
Dockerfile
pom.xml
dbInput.example.txt
```

## Publishing to GitHub

Review the files before publishing and make sure no real credentials are staged. If `dbInput.txt` was already tracked, Git will continue tracking it even after it is added to `.gitignore`; remove it from the Git index without deleting your local file:

```bash
git rm --cached dbInput.txt
```

Then rotate any credentials that were previously committed.

Create an empty repository on GitHub without adding another README, license, or `.gitignore`. From this project directory, run:

```bash
git init
git add .
git status
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/<YOUR-USERNAME>/<YOUR-REPOSITORY>.git
git push -u origin main
```

For SSH instead of HTTPS:

```bash
git remote add origin git@github.com:<YOUR-USERNAME>/<YOUR-REPOSITORY>.git
git push -u origin main
```

Before `git push`, inspect `git status` and confirm that `dbInput.txt`, generated reports, JAR files, and `target/` are not included.

## License

Add the project's intended license before publishing. If no license is selected, the repository remains fully copyrighted by its author and others may not have permission to reuse the code.
