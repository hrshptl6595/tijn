/*** Setup our database ***/
DROP DATABASE IF EXISTS tijn;
CREATE DATABASE tijn;
USE tijn;

/*** Setup our user (this requires mysql >= 5.7.6) */
CREATE USER IF NOT EXISTS 'tijn'@'localhost' IDENTIFIED BY 'tijn';
GRANT ALL ON tijn.* to 'tijn'@'localhost';

/*** Setup out tables ***/

/* bank_account */
CREATE TABLE bank_account (
    BankID int NOT NULL,
    BANumber int NOT NULL,
    PRIMARY KEY (BankID, BANumber)
);

/* user_account */
CREATE TABLE user_account (
    SSN int NOT NULL,
    Name varchar(255) NOT NULL,
    Balance decimal(36, 2) NOT NULL, 
    BankID int NOT NULL,
    BANumber int NOT NULL,
    PBAVerified boolean NOT NULL,
    PRIMARY KEY (SSN),
    FOREIGN KEY (BankID, BANumber) REFERENCES bank_account(BankID, BANumber)
);

/* has_additional */
CREATE TABLE has_additional (
    SSN int NOT NULL,
    BankID int NOT NULL,
    BANumber int NOT NULL,
    Verified boolean NOT NULL,
    PRIMARY KEY (SSN, BankID, BANumber),
    FOREIGN KEY (SSN) REFERENCES user_account(SSN),
    FOREIGN KEY (BankID, BANumber) REFERENCES bank_account(BankID, BANumber)
);

/* electronic_address */
CREATE TABLE electronic_address (
    Identifier varchar(255) NOT NULL,
    SSN int NOT NULL,
    Type varchar(255) NOT NULL,
    Verified boolean NOT NULL,
    PRIMARY KEY (Identifier),
    FOREIGN KEY (SSN) REFERENCES user_account(SSN)
);

/* send_transaction */
CREATE TABLE send_transaction (
    STId INT NOT NULL AUTO_INCREMENT,
    Amount DECIMAL(36, 2) NOT NULL,
    ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Memo VARCHAR(255),
    Cancelled BOOLEAN NOT NULL,
    Claimed TIMESTAMP,
    SSN int NOT NULL,
    Identifier varchar(255) NOT NULL,
    PRIMARY KEY (STId),
    FOREIGN KEY (SSN) REFERENCES user_account(SSN)
);

/* request_transaction */
CREATE TABLE request_transaction (
    RTid INT NOT NULL AUTO_INCREMENT,
    Amount INT NOT NULL,
    ts TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    Memo VARCHAR(255),
    SSN INT NOT NULL,
    PRIMARY KEY(RTid),
    FOREIGN KEY(SSN) REFERENCES user_account(SSN)
);

/* from */
CREATE TABLE from_rq (
    RTid INT NOT NULL,
    Identifier VARCHAR(255) NOT NULL,
    Percentage DECIMAL(36,2) NOT NULL,
    PRIMARY KEY (RTid, Identifier),
    FOREIGN KEY (RTid) REFERENCES request_transaction(RTid),
    FOREIGN KEY (Identifier) REFERENCES electronic_address(Identifier)
);
 
/*** Setup our test data ***/

/* bank_account */
INSERT INTO bank_account VALUES
(122105155, 1000000001),
(082000549, 1000000002),
(121122676, 1000000003),
(122235821, 1000000004),
(122105155, 1000000005),
(082000549, 1000000006),
(122105155, 2000000001),
(122235821, 1000000005),
(122105155, 2000000002);

/* user_account */
INSERT INTO user_account VALUES
(000000001, 'Edgar Frank Codd', 100.00, 122105155, 1000000001, True),
(000000002, 'Christopher J Date', 200.00, 082000549, 1000000002, False),
(000000003, 'Donald Chamberlin', 300.00, 121122676, 1000000003, True),
(000000004, 'Raymond Boyce', 400.00, 122235821, 1000000004, True),
(000000005, 'Ralph Kimball', 500.00, 122105155, 1000000005, True),
(000000006, 'Michael Stonebraker', 600.00, 082000549, 1000000006, False);

/* has_additional */
INSERT INTO has_additional VALUES
(000000001, 122105155, 2000000001, False),
(000000001, 122105155, 2000000002, True),
(000000005, 122235821, 1000000005, True);

/* electronic_address */
INSERT INTO electronic_address VALUES
("efc@ibm.com", 000000001, "email", True),
("teddy@gmail.com", 000000001, "email", False),
("1-877-426-6006", 000000001, "phone", True),
("1-609-588-0326", 000000001, "phone", False),
("cjd@ibm.com", 000000002, "email", True),
("sql4lyfe@aol.com", 000000002, "email", False),
("1-514-220-8686", 000000002, "phone", True),
("1-215-664-4040", 000000003, "phone", True);

/* send_transaction */
INSERT INTO send_transaction (Amount, ts, Memo, Cancelled, Claimed, SSN, Identifier) VALUES
(5.00,  '2018-11-29 00:00:00', 'For coffee', False, NULL, 000000001, 'cjd@ibm.com'),
(5.25,  '2018-11-23 00:00:00', 'Donuts', False, NULL, 000000001, '1-514-220-8686'),
(5.25,  '2018-10-04 00:00:00', 'Soda', False, NULL, 000000001, 'sql4lyfe@aol.com'),
(4.25,  '2018-10-02 00:00:00', 'Splitting check', False, NULL, 000000001, 'greg@commerze.com'),
(14.25, '2018-06-02 00:00:00', 'Office Gift', False, NULL, 000000001, 'greg@commerze.com'),
(12.95, '2018-09-16 00:00:00', 'New Socks', False, '2018-09-16 12:00:00', 000000002, 'efc@ibm.com'),
(6.05,  '2018-10-01 00:00:00', 'Pizza', False, '2018-10-01 12:00:00', 000000002, 'teddy@gmail.com');
