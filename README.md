# TIJN Payment Network

* [Description](docs/description.pdf)
* [Relational Schema](docs/schema.pdf)

## Requirements

* [MySQL >= 5.7.6](https://mysql.com) or [MariaDB](https://go.mariadb.com) must be installed and running on the default port
* A [Java Development Kit](https;//openjdk.java.net) must be installed to compile

## Getting the source

```bash
$ git clone https://github.com/rxt1077/tijn.git
$ cd tijn
```

## Setting up the DB

The SQL commands to setup our database, user, and tables can be found in
setup.sql. Run them via the mysql client to get things setup:

```bash
$ mysql --user=root < setup.sql
```

## Compiling

```bash
$ javac *.java
```

## Running

The *full path* to the
[MySQL](https://dev.mysql.com/downloads/connector/j/) or
[MariaDB](https://downloads.mariadb.com/#connectors) JDBC Driver (both found in
this directory) must be in your classpath. Substitute *your* path in the
following commands.

```bash
Linux:

$ java -cp .:/home/ryan/cs631/tijn/mysql-connector-java-8.0.13.jar tijn

Windows:

$ java -cp '.;C:\Users\rtolboom\tijn\mysql-connector-java-8.0.13.jar' tijn
```

## Usage

This payment network interface presents a text, menu-driven interface. A "> "
prompt indicates that the program is awaiting your input. Most options are
a single character shown in brackets. Case does not matter and Ctrl-C can be
used to quit at any time.

## Design Considerations

* When a user changes their primary account information, if the information
is already in an additional account for the user the account's verification
status will be preserved and the duplicate account will be removed from
additional accounts.
* A *Claimed* timestamp was added to *send_transaction* to keep track of which
payments have been processed.
* As soon as a user sends money it is removed from their balance, even if it
has not been claimed yet. This is to prevent users from ending up in the red.
* Users are warned when sending money to an unknown identifier.
* The percentage column is used to indicate what percentage of a total amount
the payment is sending.
* Realistically all report information can be derived from send_transaction.
It tells you what people have send and received. request_transaction is just
that, a request. It's a notification that someone would like money from you.
* An account search only lets you search for the Name of someone's account by
one of their electronic_address identifiers. No wildcards are supported and no
information other than the name is given. This is for privacy reasons.
* A transaction search shows all transactions you sent or transactions you
received based on a fuzzy match of either an Identifier or a Memo. SSNs that
are not your own will be censored.

