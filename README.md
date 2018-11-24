# TIJN Payment Network

* [Description](docs/description.pdf)
* [Relational Schema](docs/schema.pdf)

## Requirements

* [MySQL](https://mysql.com) must be installed and running on the default port

## Setting up the DB

The SQL commands to setup our database, user, and tables can be found in
setup.sql. Run them via the mysql client to get things setup:

```
mysql --user=root < setup.sql
```

## Compiling

```
javac tijn.java
```

## Running

The *full path* to the
[MySQL JDBC driver](https://dev.mysql.com/downloads/connector/j/) (found in this
directory) must be in your classpath.
```
java -cp /home/ryan/cs631/tijn/mysql-connector-java-8.0.13.jar tijn
