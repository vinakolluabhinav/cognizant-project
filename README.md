DepositCoreX — Banking Microservices Platform
A full-stack banking application built using Spring Boot Microservices (backend) and React (frontend). The system handles core banking operations like customer onboarding, CASA account management, transactions, interest calculation, and more — following a microservices architecture with API Gateway and Eureka Service Discovery.

📌 Table of Contents
Project Overview
Tech Stack
Architecture
Modules
My Module — Transaction Service
Database Design
How to Run
API Endpoints
Project Overview
DepositCoreX is a microservices-based core banking system that simulates real-world banking operations. It supports multiple user roles (Customer, Branch Officer, Operations Officer, Finance Analyst, Core Admin) with role-based access control on every API endpoint.

The system is divided into 12 independent microservices, each owning its own database, communicating via REST using Feign Clients, all routed through a central API Gateway.

Tech Stack
Backend
Technology	Version	Purpose
Java	21	Programming Language
Spring Boot	3.4.1	Microservices Framework
Spring Cloud Gateway	Latest	API Gateway & JWT Validation
Netflix Eureka	Latest	Service Discovery & Registry
Spring Data JPA	Latest	ORM / Database Layer
Hibernate	Latest	Auto DDL & SQL Generation
Spring Security	Latest	Role-Based Access Control
OpenFeign	Latest	Inter-Service HTTP Communication
JJWT	0.12.5	JWT Token Generation & Validation
MySQL	8.x	Relational Database (per service)
Lombok	Latest	Boilerplate Code Reduction
Frontend
Technology	Version	Purpose
React	18	UI Framework
Vite	Latest	Build Tool
Tailwind CSS	Latest	Styling
Axios	Latest	HTTP Client with Interceptors
React Router	Latest	Client-Side Routing
React Context API	Latest	Global State Management
Architecture
┌─────────────────────────────────────────────────────────────┐
│               FRONTEND (React + Vite - Port 5173)           │
└──────────────────────────┬──────────────────────────────────┘
                           │ All HTTP Requests
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              API GATEWAY (Spring Cloud - Port 8079)         │
│   • JWT Token Validation                                    │
│   • Request Routing (lb:// via Eureka)                      │
│   • CORS Configuration                                      │
│   • Adds X-User-Id, X-User-Role, X-User-Name headers       │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│           EUREKA SERVER (Service Registry - Port 8761)      │
│   All microservices register here                           │
│   Gateway discovers services from here                      │
└─────────────────────────────────────────────────────────────┘
                           │
          ┌────────────────┼────────────────────┐
          ▼                ▼                    ▼
    IAM Service      CASA Service      Transaction Service
    (Port 8081)      (Port 8082)         (Port 8085)
    dcx_iam DB       dcx_casa DB      dcx_transaction DB
          │                │                    │
          ▼                ▼                    ▼
   ... and 9 more independent microservices ...
Modules
Module	Service Name	Port	Database	Description
api-gateway	api-gateway	8079	—	JWT Validation, Routing, CORS
eureka-server	eureka-server	8761	—	Service Registry & Discovery
Module 2.1	iam-service	8081	dcx_iam	Identity & Access Management, JWT Auth
Module 2.2	customer-onboarding-service	8082	dcx_customer	Customer Registration & KYC
Module 2.3	casa-service	8083	dcx_casa	CASA Account Management
Module 2.4	product-config-service	8084	dcx_product	Banking Product Configuration
Module 2.5	transaction-service	8085	dcx_transaction	Transactions & GL Postings ⭐
Module 2.6	interest-service	8086	dcx_interest	Interest Calculation & Accrual
Module 2.7	hold-lien-service	8087	dcx_hold	Holds, Liens & Standing Instructions
Module 2.8	td-servicing-service	8088	dcx_td	Term Deposit Management
Module 2.9	statements-service	8089	dcx_statements	Account Statements & Reports
Module 2.10	notifications-service	8090	dcx_notifications	SMS/Email Notifications
My Module — Transaction Service (Module 2.5)
⭐ I am responsible for this module

What it does
The Transaction Service handles all financial transactions in the banking system. When a Branch Officer deposits money into a customer's account, this service processes it — validates the account, calculates the new balance, records the transaction, updates the CASA service balance, creates a General Ledger (GL) entry for accounting purposes, and sends a notification to the customer.

Key Features
Post CREDIT / DEBIT transactions on CASA accounts
Reverse posted transactions
Auto-create GL (General Ledger) postings for every transaction
Role-based access control on all endpoints
Inter-service communication with CASA, Customer, and Notification services via Feign Clients
Atomic operations using @Transactional — no partial saves
Tech Used
Spring Boot 3.4.1, Java 21
Spring Data JPA + Hibernate (auto DDL)
Spring Security + @PreAuthorize
OpenFeign for inter-service calls
MySQL (dcx_transaction database)
Lombok, BigDecimal for monetary precision
Package Structure
com.cts.project
├── controller
│   ├── TransactionController.java     ← REST endpoints for transactions
│   └── GLPostingController.java       ← REST endpoints for GL postings
├── service
│   ├── TransactionService.java        ← Interface
│   ├── GLPostingService.java          ← Interface
│   └── impl
│       ├── TransactionServiceImpl.java ← Business logic
│       └── GLPostingServiceImpl.java   ← Business logic
├── repository
│   ├── TransactionRepository.java     ← JPA repository for transactions
│   └── GLPostingRepository.java       ← JPA repository for GL postings
├── entity
│   ├── Transaction.java               ← DB entity → transaction table
│   └── GLPosting.java                 ← DB entity → glposting table
├── dto
│   ├── TransactionRequestDTO.java     ← Incoming request from frontend
│   ├── TransactionResponseDTO.java    ← Outgoing response to frontend
│   └── GLPostingResponseDTO.java      ← GL posting response
├── client
│   ├── CasaAccountClient.java         ← Feign: calls CASA service
│   ├── CustomerClient.java            ← Feign: calls Customer service
│   ├── NotificationClient.java        ← Feign: calls Notification service
│   └── AccountDTO.java               ← DTO for CASA account data
├── security
│   ├── SecurityConfig.java            ← Spring Security configuration
│   ├── GatewayAuthFilter.java         ← Reads X-User-* headers from Gateway
│   └── FeignAuthInterceptor.java      ← Propagates auth headers in Feign calls
└── exception
    ├── GlobalExceptionHandler.java    ← Centralized exception handling
    └── ResourceNotFoundException.java ← Custom 404 exception
Transaction Flow
Branch Officer submits transaction (POST /api/v1/transactions)
        ↓
API Gateway validates JWT → adds X-User-Role: BRANCH_OFFICER
        ↓
TransactionController receives request
@PreAuthorize checks role ✅
        ↓
TransactionServiceImpl.postTransaction()
  1. Call CASA service → get account details
  2. Validate: account is ACTIVE
  3. Validate: sufficient balance (for DEBIT)
  4. Calculate new balance
  5. Save transaction to DB (status: POSTED)
  6. Update balance in CASA service
  7. Create GL Posting entry
  8. Send notification to customer (async, never fails transaction)
        ↓
Return TransactionResponseDTO as JSON
API Endpoints
Method	Endpoint	Role	Description
POST	/api/v1/transactions	BRANCH_OFFICER, OPERATIONS_OFFICER, CORE_ADMIN	Create new transaction
POST	/api/v1/transactions/{id}/reverse	OPERATIONS_OFFICER, CORE_ADMIN	Reverse a transaction
GET	/api/v1/transactions/{id}	ALL roles	Get transaction by ID
GET	/api/v1/transactions/account/{accountId}	ALL roles	Get transactions by account
GET	/api/v1/transactions	OPERATIONS_OFFICER, FINANCE_ANALYST, CORE_ADMIN	Get all transactions
GET	/api/v1/gl-postings	OPERATIONS_OFFICER, FINANCE_ANALYST, CORE_ADMIN	Get all GL postings
GET	/api/v1/gl-postings/{glId}	OPERATIONS_OFFICER, FINANCE_ANALYST, CORE_ADMIN	Get GL posting by ID
GET	/api/v1/gl-postings/transaction/{txnId}	OPERATIONS_OFFICER, FINANCE_ANALYST, CORE_ADMIN	Get GL postings by transaction
Database Tables
Table: transaction

txnid (PK, AUTO_INCREMENT)
account_id (FK)
account_number
txntype         → CREDIT / DEBIT / REVERSAL
amount          → DECIMAL(19,4)
narrative
channel
txndate
balanceafter    → DECIMAL(19,4)
status          → POSTED / REVERSED
Table: glposting

glpostingid (PK, AUTO_INCREMENT)
txnid (FK → transaction.txnid)
glaccount       → GL-DEPOSITS-CREDIT / GL-DEPOSITS-DEBIT
debitorCredit   → CREDIT / DEBIT
amount          → DECIMAL(19,4)
posteddate
User Roles
Role	Access Level
CUSTOMER	View own transactions and account
BRANCH_OFFICER	Create transactions, view transactions
OPERATIONS_OFFICER	Create & reverse transactions, view all
FINANCE_ANALYST	View all transactions and GL postings
CORE_ADMIN	Full access to all endpoints
Security Flow
1. User logs in → IAM Service creates JWT token
   Token contains: { userId, role, name }

2. Frontend stores token in localStorage

3. Every API call → Axios sends:
   Authorization: Bearer <token>

4. API Gateway JwtAuthenticationFilter:
   - Validates JWT signature using HMAC-SHA256
   - Extracts userId, role, name from token claims
   - Adds headers: X-User-Id, X-User-Role, X-User-Name
   - Routes to correct microservice

5. Microservice GatewayAuthFilter:
   - Reads X-User-Role header
   - Creates Spring Security authentication
   - Sets in SecurityContextHolder

6. @PreAuthorize on each endpoint:
   - Checks if user's role is allowed
   - Throws AccessDeniedException if not → 403 Forbidden
Database Design
Each microservice has its own isolated database — following the Database-per-Service pattern:

Database	Microservice	Tables
dcx_iam	IAM Service	users, roles
dcx_customer	Customer Service	customer_reference
dcx_casa	CASA Service	casa_account
dcx_product	Product Config	product_config
dcx_transaction	Transaction Service	transaction, glposting
dcx_interest	Interest Service	interest_accrual, interest_config
dcx_hold	Hold/Lien Service	hold_lien, standing_instruction
dcx_td	TD Servicing	td_account, td_interest
dcx_statements	Statements	statement, statement_entry
dcx_notifications	Notifications	notification_log
How to Run
Prerequisites
Java 21
Maven 3.8+
MySQL 8.x
Node.js 18+
npm or yarn
Step 1: Start MySQL
Create all required databases:

CREATE DATABASE dcx_iam;
CREATE DATABASE dcx_customer;
CREATE DATABASE dcx_casa;
CREATE DATABASE dcx_product;
CREATE DATABASE dcx_transaction;
CREATE DATABASE dcx_interest;
CREATE DATABASE dcx_hold;
CREATE DATABASE dcx_td;
CREATE DATABASE dcx_statements;
CREATE DATABASE dcx_notifications;
Step 2: Start Eureka Server
cd eureka-server
mvn spring-boot:run
# Runs on http://localhost:8761
Step 3: Start API Gateway
cd api-gateway
mvn spring-boot:run
# Runs on http://localhost:8079
Step 4: Start Microservices
# Start each module
cd Module_2.1 && mvn spring-boot:run   # IAM Service
cd Module_2.2 && mvn spring-boot:run   # Customer Service
cd Module_2.3 && mvn spring-boot:run   # CASA Service
cd Module_2.5/Transaction && mvn spring-boot:run   # Transaction Service
# ... start other modules as needed
Step 5: Start Frontend
cd frontend
npm install
npm run dev
# Runs on http://localhost:5173
Key Design Decisions
Decision	Why
Separate database per service	Data isolation, independent deployments
JWT stateless auth	No session storage needed, scalable
Feign Clients for inter-service calls	Clean, declarative HTTP calls
@Transactional on write operations	Atomic DB operations, no partial saves
BigDecimal for monetary amounts	Exact decimal arithmetic, no floating-point errors
Notification in try-catch	Notification failure must never rollback a valid transaction
CORS only at Gateway	Single entry point, one CORS config for all
FetchType.LAZY on GL→Transaction	Avoid loading unnecessary data
Author
abhinav — Module 2.7,2.10

Built as part of the DepositCoreX banking project
