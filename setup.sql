/* Setup our database */
DROP DATABASE IF EXISTS tijn;
CREATE DATABASE tijn;

/* Setup our user */
GRANT ALL ON tijn.* to 'tijn'@'localhost' IDENTIFIED BY 'tijn';

/* Setup out tables */
USE tijn;
CREATE TABLE user_account (
    SSN int NOT NULL,
    Name varchar(255) NOT NULL,
    Balance decimal(36,2) NOT NULL, 
    BankID int NOT NULL,
    BANumber int NOT NULL,
    PBAVerified boolean NOT NULL,
    PRIMARY KEY (SSN)
);
INSERT INTO user_account VALUES
(000000001, 'Edgar Frank Codd', 100.00, 122105155, 1000000001, True),
(000000002, 'Christopher J Date', 200.00, 082000549, 1000000002, False),
(000000003, 'Donald Chamberlin', 300.00, 121122676, 1000000003, True),
(000000004, 'Raymond Boyce', 400.00, 122235821, 1000000004, True),
(000000005, 'Ralph Kimball', 500.00, 122105155, 1000000005, True),
(000000006, 'Michael Stonebraker', 600.00, 082000549, 1000000006, False);
