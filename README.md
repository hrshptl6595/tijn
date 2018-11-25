# TIJN Payment Network

* [Description](docs/description.pdf)
* [Relational Schema](docs/schema.pdf)

## Requirements

* [MySQL](https://mysql.com) must be installed and running on the default port
* A [Java Development Kit](https;//openjdk.java.net) must be installed to compile

## Setting up the DB

The SQL commands to setup our database, user, and tables can be found in
setup.sql. Run them via the mysql client to get things setup:

```
mysql --user=root < setup.sql
```

## Compiling

```
javac *.java
```

## Running

The *full path* to the
[MySQL JDBC driver](https://dev.mysql.com/downloads/connector/j/) (found in this
directory) must be in your classpath.
```
java -cp /home/ryan/cs631/tijn/mysql-connector-java-8.0.13.jar tijn
```

## Usage

This payment network interface presents a text, menu-driven interface. A "> "
prompt indicates that the program is awaiting your input. Most options are
a single character shown in brackets. Case does not matter and Ctrl-C can be
used to quit at any time.
