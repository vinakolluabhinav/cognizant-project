-- ============================================================
-- Module 2.5 - Align DB tables to match Spring Boot Entities
-- Run this in MySQL on database: depositcorex
-- ============================================================

USE depositcorex;

-- -------------------------------------------------------
-- Step 1: Drop existing tables (in order due to FK)
-- -------------------------------------------------------
DROP TABLE IF EXISTS gl_posting;
DROP TABLE IF EXISTS transaction;
DROP TABLE IF EXISTS deposit_account;

-- -------------------------------------------------------
-- Step 2: Recreate tables matching Entity column names
-- -------------------------------------------------------

-- Matches DepositAccount entity
-- Note: CustomerID and ProductID are FK references to other modules
-- We keep them as plain BIGINT since other module tables may not exist yet
CREATE TABLE DepositAccount (
  AccountID     BIGINT AUTO_INCREMENT PRIMARY KEY,
  CustomerID    BIGINT,
  ProductID     BIGINT,
  AccountNumber VARCHAR(40) NOT NULL UNIQUE,
  Category      VARCHAR(40),
  Currency      VARCHAR(10),
  OpenDate      DATE,
  Status        VARCHAR(30) NOT NULL DEFAULT 'ACTIVE'
);

-- Matches Transaction entity
CREATE TABLE Transaction (
  TxnID         BIGINT AUTO_INCREMENT PRIMARY KEY,
  AccountID     BIGINT NOT NULL,
  TxnType       VARCHAR(40),
  Amount        DECIMAL(19,4),
  Narrative     VARCHAR(200),
  Channel       VARCHAR(40),
  TxnDate       DATETIME,
  BalanceAfter  DECIMAL(19,4),
  Status        VARCHAR(30) NOT NULL DEFAULT 'POSTED',
  CONSTRAINT fk_txn_account FOREIGN KEY (AccountID)
    REFERENCES DepositAccount(AccountID)
    ON DELETE RESTRICT ON UPDATE CASCADE
);

-- Matches GLPosting entity
CREATE TABLE GLPosting (
  GLPostingID   BIGINT AUTO_INCREMENT PRIMARY KEY,
  TxnID         BIGINT NOT NULL,
  GLAccount     VARCHAR(60),
  DebitOrCredit VARCHAR(20),
  Amount        DECIMAL(19,4),
  PostedDate    DATETIME,
  CONSTRAINT fk_gl_txn FOREIGN KEY (TxnID)
    REFERENCES Transaction(TxnID)
    ON DELETE CASCADE ON UPDATE CASCADE
);

-- -------------------------------------------------------
-- Step 3: Insert test data for Module 2.5 testing
-- -------------------------------------------------------

-- Test Account 1 - ACTIVE SAVINGS
INSERT INTO DepositAccount (CustomerID, ProductID, AccountNumber, Category, Currency, OpenDate, Status)
VALUES (1, 1, 'ACC0001234567', 'SAVINGS', 'INR', '2025-01-01', 'ACTIVE');

-- Test Account 2 - ACTIVE CURRENT
INSERT INTO DepositAccount (CustomerID, ProductID, AccountNumber, Category, Currency, OpenDate, Status)
VALUES (2, 2, 'ACC0009876543', 'CURRENT', 'INR', '2025-01-15', 'ACTIVE');

-- Test Account 3 - DORMANT (to test rejection)
INSERT INTO DepositAccount (CustomerID, ProductID, AccountNumber, Category, Currency, OpenDate, Status)
VALUES (3, 1, 'ACC0005555555', 'SAVINGS', 'INR', '2024-06-01', 'DORMANT');

-- -------------------------------------------------------
-- Step 4: Verify
-- -------------------------------------------------------
SELECT * FROM DepositAccount;
