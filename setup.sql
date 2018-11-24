/*** Setup our database ***/
DROP DATABASE IF EXISTS tijn;
CREATE DATABASE tijn;
USE tijn;

/*** Setup our user ***/
GRANT ALL ON tijn.* to 'tijn'@'localhost' IDENTIFIED BY 'tijn';

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
    Balance decimal(36,2) NOT NULL, 
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
(122235821, 1000000005);

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
(000000005, 122235821, 1000000005, True);
