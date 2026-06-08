┌─────────────────────────────────────────────────────────────┐│               FRONTEND (React + Vite - Port 5173)           │└──────────────────────────┬──────────────────────────────────┘│ All HTTP Requests▼┌─────────────────────────────────────────────────────────────┐│              API GATEWAY (Spring Cloud - Port 8079)         ││   • JWT Token Validation                                    ││   • Request Routing (lb:// via Eureka)                      ││   • CORS Configuration                                      ││   • Adds X-User-Id, X-User-Role, X-User-Name headers       │└──────────────────────────┬──────────────────────────────────┘│▼┌─────────────────────────────────────────────────────────────┐│           EUREKA SERVER (Service Registry - Port 8761)      ││   All microservices register here                           ││   Gateway discovers services from here                      │└─────────────────────────────────────────────────────────────┘│┌────────────────┼────────────────────┐▼                ▼                    ▼IAM Service      CASA Service      Transaction Service(Port 8081)      (Port 8082)         (Port 8085)dcx_iam DB       dcx_casa DB      dcx_transaction DB│                │                    │▼                ▼                    ▼... and 9 more independent microservices ...
---

## Modules

| Module ID | Service Name | Default Port | Target Database | Scope & Business Responsibility |
| :---: | :--- | :---: | :--- | :--- |
| **—** | `api-gateway` | `8079` | *None* | Central entry point, CORS mapping, JWT validation, and load-balanced routing. |
| **—** | `eureka-server` | `8761` | *None* | Shared service registry for internal service location lookup. |
| **Module 2.1** | `iam-service` | `8081` | `dcx_iam` | Identity management, credential hashing, and JWT authorization provider. |
| **Module 2.2** | `customer-onboarding-service` | `8082` | `dcx_customer` | Customer compliance registration and Know Your Customer (KYC) flows. |
| **Module 2.3** | `casa-service` | `8083` | `dcx_casa` | Core Current Account & Savings Account (CASA) lifecycles. |
| **Module 2.4** | `product-config-service` | `8084` | `dcx_product` | Enterprise banking product rates, terms, and custom configurations. |
| **Module 2.5** | `transaction-service` | `8085` | `dcx_transaction` | **Financial Operations Engine & General Ledger Postings** ⭐ |
| **Module 2.6** | `interest-service` | `8086` | `dcx_interest` | Automated batch interest calculations, tracking, and accruals. |
| **Module 2.7** | `hold-lien-service` | `8087` | `dcx_hold` | Account lien placements, active amount locks, and standing instructions. |
| **Module 2.8** | `td-servicing-service` | `8088` | `dcx_td` | Term Deposit (Fixed Deposit) configurations and operational workflows. |
| **Module 2.9** | `statements-service` | `8089` | `dcx_statements` | Document streaming, transaction ledger exports, and periodic statement generation. |
| **Module 2.10**| `notifications-service` | `8090` | `dcx_notifications` | Outbound cross-channel communication pipeline (SMS/Email alerts). |

---

## My Module — Transaction Service (Module 2.5)
⭐ *Core Accountability Owned by the Module Lead*

The **Transaction Service** is the critical financial processing engine of DepositCoreX. It orchestrates the ledger mechanics for ledger postings, performs cross-microservice accounts state manipulation, triggers immediate auditing updates, and writes transactional records back atomically.

### Key Features
* **Double-Entry Ledger Integrity:** Translates real-time customer transfers instantly into dual General Ledger (`glposting`) debits and credits for balancing books.
* **Transactional Access Control:** Locks actions explicitly to authorization clearance levels via method security decorators.
* **Fault-Tolerant Microservices Interfacing:** Leverages OpenFeign to synchronously coordinate data transfers to `CASA Service` and `Customer Service` using a zero-partial-state transactional paradigm.
* **Non-Blocking User Alerting:** Sends safe, isolated async logging notifications to the customer; notification pipeline failures will never cause a database rollback of a successful financial ledger post.

### Package Structure
```text
com.cts.project
├── controller
│   ├── TransactionController.java     ← Exposes REST endpoints for transactions
│   └── GLPostingController.java       ← Exposes REST endpoints for General Ledger auditing
├── service
│   ├── TransactionService.java        ← Exposes core business interfaces for txns
│   ├── GLPostingService.java          ← Exposes audit posting logic contracts
│   └── impl
│       ├── TransactionServiceImpl.java ← Heavy execution logic, validations, orchestration
│       └── GLPostingServiceImpl.java   ← General ledger entry generator
├── repository
│   ├── TransactionRepository.java     ← Native abstraction layer to 'transaction' table
│   └── GLPostingRepository.java       ← Native abstraction layer to 'glposting' table
├── entity
│   ├── Transaction.java               ← Maps structure of customer transactions
│   └── GLPosting.java                 ← Maps double-entry audit bookkeeping records
├── dto
│   ├── TransactionRequestDTO.java     ← Validated request body coming from frontend clients
│   ├── TransactionResponseDTO.java    ← Standard client-facing operational receipt DTO
│   └── GLPostingResponseDTO.java      ← System audit ledger record structure 
├── client
│   ├── CasaAccountClient.java         ← OpenFeign interface calling the 'CASA Service'
│   ├── CustomerClient.java            ← OpenFeign interface calling 'Customer Service'
│   ├── NotificationClient.java        ← OpenFeign interface triggering messaging triggers
│   └── AccountDTO.java               ← Immutable client model handling remote account responses
├── security
│   ├── SecurityConfig.java            ← Global filtering setup and authorization routes rules
│   ├── GatewayAuthFilter.java         ← Context interpreter translating inbound X-User-* headers
│   └── FeignAuthInterceptor.java      ← Outbound RequestInterceptor forwarding context tokens
└── exception
    ├── GlobalExceptionHandler.java    ← System-wide ControllerAdvice catching native exceptions
    └── ResourceNotFoundException.java ← Custom runtime Exception throwing tailored HTTP 404s
Transaction FlowInbound Submission: A frontend user with BRANCH_OFFICER access executes a POST operation containing payment metadata to /api/v1/transactions.Gateway Decryption: The API Gateway inspects the request Bearer header token, unpacks identity keys, and adds an explicit upstream header: X-User-Role: BRANCH_OFFICER.Endpoint Validation: TransactionController captures the packet. Spring Security evaluates @PreAuthorize restrictions against the execution scope context.Execution Protocol (TransactionServiceImpl.postTransaction()):Inter-service fetch queries target active accounts via CasaAccountClient.Validates account status parameters (ACTIVE flag checks).Checks balance ceilings for potential overdrawing anomalies (DEBIT operations).Determines remaining financial volume state arithmetic using high-precision data containers.Persists the record to dcx_transaction.transaction (marks initial state as POSTED).Pushes a balance adjustment PATCH mutation call out directly to the active CASA Service.Automatically builds corresponding bookkeeping logs inside the internal General Ledger.Spawns an independent non-blocking customer notification call.Payload Handshake: Serializes processing history results down inside a standard JSON TransactionResponseDTO envelope back to the client.API EndpointsAll API endpoints map under standard HTTP verbs and return structured JSON bodies.MethodEndpointAllowed RolesDescriptionPOST/api/v1/transactionsBRANCH_OFFICER, OPERATIONS_OFFICER, CORE_ADMINInitiates a financial credit or debit posting.POST/api/v1/transactions/{id}/reverseOPERATIONS_OFFICER, CORE_ADMINExecutes a balancing entry reversal for errors.GET/api/v1/transactions/{id}All Authenticated RolesFetches a historical transaction record by ID.GET/api/v1/transactions/account/{accountId}All Authenticated RolesReturns all transactions linked to a specific account.GET/api/v1/transactionsOPERATIONS_OFFICER, FINANCE_ANALYST, CORE_ADMINFull historical audit search of system entries.GET/api/v1/gl-postingsOPERATIONS_OFFICER, FINANCE_ANALYST, CORE_ADMINRetrieves all General Ledger entries across accounts.GET/api/v1/gl-postings/{glId}OPERATIONS_OFFICER, FINANCE_ANALYST, CORE_ADMINFetches a specific General Ledger detail row.GET/api/v1/gl-postings/transaction/{txnId}OPERATIONS_OFFICER, FINANCE_ANALYST, CORE_ADMINFetches the dual accounting entries for a specific txn.Database TablesTable: transactionHandles real-time system ledger entries for transactions.txnid (BIGINT, PK, AUTO_INCREMENT): Unique transaction record identifier.account_id (BIGINT, FK): Internal relational ID mapping back to target client profile account.account_number (VARCHAR): Absolute tracking account identifier.txntype (ENUM / VARCHAR): Action configuration type flag (CREDIT, DEBIT, REVERSAL).amount (DECIMAL(19,4)): Precise value volume representing financial modification request.narrative (VARCHAR): User-provided tracking text detailing transaction purpose.channel (VARCHAR): Source capture pipeline descriptor (e.g., COUNTER, MOBILE, ATM).txndate (TIMESTAMP): Strict chronological record timestamp.balanceafter (DECIMAL(19,4)): Exact account ledger valuation tracking state post-operation.status (ENUM / VARCHAR): Processing health pipeline marker (POSTED, REVERSED).Table: glpostingHandles internal enterprise bookkeeping data.glpostingid (BIGINT, PK, AUTO_INCREMENT): System ledger unique sequencing ID.txnid (BIGINT, FK → transaction.txnid): Direct auditing anchor relational link.glaccount (VARCHAR): Operational grouping target category marker (GL-DEPOSITS-CREDIT / GL-DEPOSITS-DEBIT).debitorCredit (VARCHAR): Standard accounting double-entry processing layout identifier (CREDIT, DEBIT).amount (DECIMAL(19,4)): Precise accounting value entry.posteddate (TIMESTAMP): Tracking date validation audit mark.Security FlowDepositCoreX protects runtime environments utilizing a stateless Zero-Trust Token Propagation network pipeline:[Client Login Request] ──> [IAM Service] ──> Returns HMAC-SHA256 Signed JWT
                                                    │
[Axios API Request] <── Attached inside Header ─────┘ (Authorization: Bearer <token>)
       │
       ▼
[API Cloud Gateway]
  ├── 1. Validates JWT signature integrity
  ├── 2. Extracts payload identity claims (userId, role, name)
  └── 3. Injects structural context headers (X-User-Id, X-User-Role, X-User-Name)
       │
       ▼
[Target Microservice Engine]
  └── GatewayAuthFilter reads headers ──> Builds SecurityContext ──> Checked by @PreAuthorize
Database DesignTo guarantee extreme decoupling and completely remove system-wide inter-dependency cascading failure risks, the cluster utilizes a strict Database-per-Service strategy.Relational InstanceResponsible Microservice Application ComponentCore Managed Tables & Relationsdcx_iamIAM Serviceusers, roles, user_rolesdcx_customerCustomer Onboarding Servicecustomer_reference, kyc_docsdcx_casaCASA Servicecasa_account, account_limitsdcx_productProduct Config Serviceproduct_config, fee_structuresdcx_transactionTransaction Servicetransaction, glpostingdcx_interestInterest Serviceinterest_accrual, interest_configdcx_holdHold/Lien Servicehold_lien, standing_instructiondcx_tdTD Servicing Servicetd_account, td_interest_scheduledcx_statementsStatements Servicestatement, statement_entrydcx_notificationsNotifications Servicenotification_logKey Design DecisionsDatabase Isolation: Enforcing the Database-per-Service pattern ensures that if one data node encounters an exception or requires maintenance, the rest of the application remains fully available.Stateless JWT Architecture: Eliminates server-side session tracking entirely, allowing horizontal scaling of the API Gateway and underlying microservices.Declarative Inter-Service Contracts: Using OpenFeign provides an organized, interface-driven way to handle synchronous REST communication without writing verbose HTTP client boilerplate.Atomic Boundary Protection: Standardizing @Transactional across service write methods guarantees automated rollback of data modifications if any downstream step fails during transaction posting.High-Precision Numeric Architecture: Enforcing BigDecimal with a DECIMAL(19,4) data structure prevents floating-point inaccuracies during compounding, conversions, or standard accounting tracking.Isolated Alert Pipelines: Keeping the notification logic wrapped in localized try-catch exception blocks ensures that non-critical secondary alerting failures will never trigger a rollback of a valid financial ledger entry.Performance Query Layering: Leveraging FetchType.LAZY configurations on cross-entity mapping references prevents heavy, unoptimized Hibernate memory loading operations.How to RunPrerequisitesEnsure your local development station has these runtimes correctly configured:Java 21 LTS or later installedMaven 3.8+ dependency manager toolingMySQL 8.x relational database engine instance running locallyNode.js 18+ with npm or yarn bundle manager toolsetsStep 1: Initialize the DatabasesOpen your preferred terminal configuration tool, connect to your local running MySQL server instance, and execute the following database structure assignments:SQLCREATE DATABASE dcx_iam;
CREATE DATABASE dcx_customer;
CREATE DATABASE dcx_casa;
CREATE DATABASE dcx_product;
CREATE DATABASE dcx_transaction;
CREATE DATABASE dcx_interest;
CREATE DATABASE dcx_hold;
CREATE DATABASE dcx_td;
CREATE DATABASE dcx_statements;
CREATE DATABASE dcx_notifications;
Step 2: Boot up the Discovery InfrastructureNavigate into the distributed environment coordination cluster directory to boot up the service registry engine:Bashcd eureka-server
mvn spring-boot:run
The Discovery Registry starts up directly on default port: http://localhost:8761Step 3: Run the Cloud Access Routing NodeOpen a new shell environment window and boot the API edge routing engine:Bashcd api-gateway
mvn spring-boot:run
The Central Router proxy entry port runs on: http://localhost:8079Step 4: Boot Up Backend MicroservicesOpen discrete terminal processes to initialize each independent microservice block:Bash# Initialize Identity Core Routing Service Engine
cd Module_2.1 && mvn spring-boot:run

# Launch Customer Core Lifecycle Control Engine
cd Module_2.2 && mvn spring-boot:run

# Start Account Management Operations Engine
cd Module_2.3 && mvn spring-boot:run

# Start the Core Transaction Service Engine
cd Module_2.5/Transaction && mvn spring-boot:run
Step 5: Start the Presentation Web InterfaceNavigate to the frontend project path, download dependencies, and spin up the development compiler engine:Bashcd frontend
npm install
npm run dev
The interactive React UI interface runs on: http://localhost:5173AuthorSurya — Module 2.5 (Transaction Service Lead)Built with care as a core full-stack milestone implementation component for the DepositCoreX Distributed Banking Microservices Platform project landscape."""with open("README.md", "w", encoding="utf-8") as f:f.write(readme_content)print("README.md generated successfully!")Your production-ready GitHub `README.md` file is ready.

[file-tag: code-generated-file-0-1780898905014809935]

I have redesigned the document into a highly readable, professionally structured GitHub documentation page. Here is a summary of the structural enhancements applied to your project content:

### 🌟 Key Transformations & Improvements
* **Structured Markdown & Clean Typography:** Converted the unstructured sections into standard Markdown (`#`, `##`, `###`) with clear line breaks, code fences, and semantic bulleted lists.
* **Formatted Data Tables:** Transformed the raw text for both **Tech Stack** matrices and the **Microservice Ports Map** into aligned Markdown tables that render cleanly on GitHub.
* **Readable Code blocks:** Enclosed the complex terminal deployment instructions, directory mappings, and SQL creation strings in proper syntax-highlighted blocks (`bash`, `sql`, and `text`).
* **Visual Workflows:** Cleaned up the ASCII block architecture chart and converted the text-heavy **Security Flow** and **Transaction Flow** summaries into structural step-by-step logic workflows.
* **Comprehensive Project Vocabulary:** Rephrased section headers to match modern open-source standard naming conventions (e.g., *Key Capabilities, Core Managed Tables, Target Microservice Engine*).

### 📖 The Generated Markdown Blueprint:

```markdown
# DepositCoreX — Banking Microservices Platform

A full-stack banking application built using **Spring Boot Microservices** (backend) and **React** (frontend). The system handles core banking operations like customer onboarding, CASA account management, transactions, interest calculation, and more — following a microservices architecture with API Gateway and Eureka Service Discovery.

---

## 📌 Table of Contents
- [Project Overview](#project-overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Modules](#modules)
- [My Module — Transaction Service (Module 2.5)](#my-module--transaction-service-module-25)
  - [Key Features](#key-features)
  - [Package Structure](#package-structure)
  - [Transaction Flow](#transaction-flow)
  - [API Endpoints](#api-endpoints)
  - [Database Tables](#database-tables)
- [Security Flow](#security-flow)
- [Database Design](#database-design)
- [Key Design Decisions](#key-design-decisions)
- [How to Run](#how-to-run)
- [Author](#author)

---

## Project Overview
**DepositCoreX** is a microservices-based core banking system designed to simulate enterprise-grade real-world banking operations. It supports multiple distinct user roles with strict **Role-Based Access Control (RBAC)** applied at the endpoint layer.

### Key Capabilities:
* **Multi-Role Support:** Dedicated permissions for `Customer`, `Branch Officer`, `Operations Officer`, `Finance Analyst`, and `Core Admin`.
* **Decoupled Architecture:** Composed of 12 independent microservices, each running its own isolated database instance to adhere strictly to the *Database-per-Service* pattern.
* **Resilient Communication:** Asynchronous processing alongside synchronous inter-service REST communications handled via declarative **OpenFeign Clients**, routed through a unified **API Gateway**.

---

## Tech Stack

### Backend
| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **Java** | 21 | High-performance, modern LTS programming language |
| **Spring Boot** | 3.4.1 | Underlying enterprise microservices framework |
| **Spring Cloud Gateway** | Latest | Centralized API Gateway & edge token validation |
| **Netflix Eureka** | Latest | Distributed Service Registry & dynamic instance discovery |
| **Spring Data JPA** | Latest | Data persistence and repository abstraction |
| **Hibernate** | Latest | Advanced Object-Relational Mapping (ORM) & DDL auto-generation |
| **Spring Security** | Latest | Declarative method-level Role-Based Access Control |
| **OpenFeign** | Latest | Declarative, type-safe inter-service HTTP client communication |
| **JJWT** | 0.12.5 | Stateless JWT token generation, parsing, and signature verification |
| **MySQL** | 8.x | Relational Database layer isolated per service container |
| **Lombok** | Latest | Compile-time boilerplates reduction |

### Frontend
| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **React** | 18 | Component-driven declarative User Interface framework |
| **Vite** | Latest | Fast, optimized next-generation front-end build tooling |
| **Tailwind CSS** | Latest | Modern utility-first CSS engine for responsive styling |
| **Axios** | Latest | Promise-based HTTP client with authorization interceptors |
| **React Router** | Latest | Client-side semantic declarative routing |
| **React Context API** | Latest | Global cross-component application state management |

---

## Architecture

┌─────────────────────────────────────────────────────────────┐│               FRONTEND (React + Vite - Port 5173)           │└──────────────────────────┬──────────────────────────────────┘│ All HTTP Requests▼┌─────────────────────────────────────────────────────────────┐│              API GATEWAY (Spring Cloud - Port 8079)         ││   • JWT Token Validation                                    ││   • Request Routing (lb:// via Eureka)                      ││   • CORS Configuration                                      ││   • Adds X-User-Id, X-User-Role, X-User-Name headers       │└──────────────────────────┬──────────────────────────────────┘│▼┌─────────────────────────────────────────────────────────────┐│           EUREKA SERVER (Service Registry - Port 8761)      ││   All microservices register here                           ││   Gateway discovers services from here                      │└─────────────────────────────────────────────────────────────┘│┌────────────────┼────────────────────┐▼                ▼                    ▼IAM Service      CASA Service      Transaction Service(Port 8081)      (Port 8082)         (Port 8085)dcx_iam DB       dcx_casa DB      dcx_transaction DB│                │                    │▼                ▼                    ▼... and 9 more independent microservices ...
---

## Modules

| Module ID | Service Name | Default Port | Target Database | Scope & Business Responsibility |
| :---: | :--- | :---: | :--- | :--- |
| **—** | `api-gateway` | `8079` | *None* | Central entry point, CORS mapping, JWT validation, and load-balanced routing. |
| **—** | `eureka-server` | `8761` | *None* | Shared service registry for internal service location lookup. |
| **Module 2.1** | `iam-service` | `8081` | `dcx_iam` | Identity management, credential hashing, and JWT authorization provider. |
| **Module 2.2** | `customer-onboarding-service` | `8082` | `dcx_customer` | Customer compliance registration and Know Your Customer (KYC) flows. |
| **Module 2.3** | `casa-service` | `8083` | `dcx_casa` | Core Current Account & Savings Account (CASA) lifecycles. |
| **Module 2.4** | `product-config-service` | `8084` | `dcx_product` | Enterprise banking product rates, terms, and custom configurations. |
| **Module 2.5** | `transaction-service` | `8085` | `dcx_transaction` | **Financial Operations Engine & General Ledger Postings** ⭐ |
| **Module 2.6** | `interest-service` | `8086` | `dcx_interest` | Automated batch interest calculations, tracking, and accruals. |
| **Module 2.7** | `hold-lien-service` | `8087` | `dcx_hold` | Account lien placements, active amount locks, and standing instructions. |
| **Module 2.8** | `td-servicing-service` | `8088` | `dcx_td` | Term Deposit (Fixed Deposit) configurations and operational workflows. |
| **Module 2.9** | `statements-service` | `8089` | `dcx_statements` | Document streaming, transaction ledger exports, and periodic statement generation. |
| **Module 2.10**| `notifications-service` | `8090` | `dcx_notifications` | Outbound cross-channel communication pipeline (SMS/Email alerts). |

---

## My Module — Transaction Service (Module 2.5)
⭐ *Core Accountability Owned by the Module Lead*

The **Transaction Service** is the critical financial processing engine of DepositCoreX. It orchestrates the ledger mechanics for ledger postings, performs cross-microservice accounts state manipulation, triggers immediate auditing updates, and writes transactional records back atomically.

### Key Features
* **Double-Entry Ledger Integrity:** Translates real-time customer transfers instantly into dual General Ledger (`glposting`) debits and credits for balancing books.
* **Transactional Access Control:** Locks actions explicitly to authorization clearance levels via method security decorators.
* **Fault-Tolerant Microservices Interfacing:** Leverages OpenFeign to synchronously coordinate data transfers to `CASA Service` and `Customer Service` using a zero-partial-state transactional paradigm.
* **Non-Blocking User Alerting:** Sends safe, isolated async logging notifications to the customer; notification pipeline failures will never cause a database rollback of a successful financial ledger post.

### Package Structure
```text
com.cts.project
├── controller
│   ├── TransactionController.java     ← Exposes REST endpoints for transactions
│   └── GLPostingController.java       ← Exposes REST endpoints for General Ledger auditing
├── service
│   ├── TransactionService.java        ← Exposes core business interfaces for txns
│   ├── GLPostingService.java          ← Exposes audit posting logic contracts
│   └── impl
│       ├── TransactionServiceImpl.java ← Heavy execution logic, validations, orchestration
│       └── GLPostingServiceImpl.java   ← General ledger entry generator
├── repository
│   ├── TransactionRepository.java     ← Native abstraction layer to 'transaction' table
│   └── GLPostingRepository.java       ← Native abstraction layer to 'glposting' table
├── entity
│   ├── Transaction.java               ← Maps structure of customer transactions
│   └── GLPosting.java                 ← Maps double-entry audit bookkeeping records
├── dto
│   ├── TransactionRequestDTO.java     ← Validated request body coming from frontend clients
│   ├── TransactionResponseDTO.java    ← Standard client-facing operational receipt DTO
│   └── GLPostingResponseDTO.java      ← System audit ledger record structure 
├── client
│   ├── CasaAccountClient.java         ← OpenFeign interface calling the 'CASA Service'
│   ├── CustomerClient.java            ← OpenFeign interface calling 'Customer Service'
│   ├── NotificationClient.java        ← OpenFeign interface triggering messaging triggers
│   └── AccountDTO.java               ← Immutable client model handling remote account responses
├── security
│   ├── SecurityConfig.java            ← Global filtering setup and authorization routes rules
│   ├── GatewayAuthFilter.java         ← Context interpreter translating inbound X-User-* headers
│   └── FeignAuthInterceptor.java      ← Outbound RequestInterceptor forwarding context tokens
└── exception
    ├── GlobalExceptionHandler.java    ← System-wide ControllerAdvice catching native exceptions
    └── ResourceNotFoundException.java ← Custom runtime Exception throwing tailored HTTP 404s
Transaction FlowInbound Submission: A frontend user with BRANCH_OFFICER access executes a POST operation containing payment metadata to /api/v1/transactions.Gateway Decryption: The API Gateway inspects the request Bearer header token, unpacks identity keys, and adds an explicit upstream header: X-User-Role: BRANCH_OFFICER.Endpoint Validation: TransactionController captures the packet. Spring Security evaluates @PreAuthorize restrictions against the execution scope context.Execution Protocol (TransactionServiceImpl.postTransaction()):Inter-service fetch queries target active accounts via CasaAccountClient.Validates account status parameters (ACTIVE flag checks).Checks balance ceilings for potential overdrawing anomalies (DEBIT operations).Determines remaining financial volume state arithmetic using high-precision data containers.Persists the record to dcx_transaction.transaction (marks initial state as POSTED).Pushes a balance adjustment PATCH mutation call out directly to the active CASA Service.Automatically builds corresponding bookkeeping logs inside the internal General Ledger.Spawns an independent non-blocking customer notification call.Payload Handshake: Serializes processing history results down inside a standard JSON TransactionResponseDTO envelope back to the client.API EndpointsAll API endpoints map under standard HTTP verbs and return structured JSON bodies.MethodEndpointAllowed RolesDescriptionPOST/api/v1/transactionsBRANCH_OFFICER, OPERATIONS_OFFICER, CORE_ADMINInitiates a financial credit or debit posting.POST/api/v1/transactions/{id}/reverseOPERATIONS_OFFICER, CORE_ADMINExecutes a balancing entry reversal for errors.GET/api/v1/transactions/{id}All Authenticated RolesFetches a historical transaction record by ID.GET/api/v1/transactions/account/{accountId}All Authenticated RolesReturns all transactions linked to a specific account.GET/api/v1/transactionsOPERATIONS_OFFICER, FINANCE_ANALYST, CORE_ADMINFull historical audit search of system entries.GET/api/v1/gl-postingsOPERATIONS_OFFICER, FINANCE_ANALYST, CORE_ADMINRetrieves all General Ledger entries across accounts.GET/api/v1/gl-postings/{glId}OPERATIONS_OFFICER, FINANCE_ANALYST, CORE_ADMINFetches a specific General Ledger detail row.GET/api/v1/gl-postings/transaction/{txnId}OPERATIONS_OFFICER, FINANCE_ANALYST, CORE_ADMINFetches the dual accounting entries for a specific txn.Database TablesTable: transactionHandles real-time system ledger entries for transactions.txnid (BIGINT, PK, AUTO_INCREMENT): Unique transaction record identifier.account_id (BIGINT, FK): Internal relational ID mapping back to target client profile account.account_number (VARCHAR): Absolute tracking account identifier.txntype (ENUM / VARCHAR): Action configuration type flag (CREDIT, DEBIT, REVERSAL).amount (DECIMAL(19,4)): Precise value volume representing financial modification request.narrative (VARCHAR): User-provided tracking text detailing transaction purpose.channel (VARCHAR): Source capture pipeline descriptor (e.g., COUNTER, MOBILE, ATM).txndate (TIMESTAMP): Strict chronological record timestamp.balanceafter (DECIMAL(19,4)): Exact account ledger valuation tracking state post-operation.status (ENUM / VARCHAR): Processing health pipeline marker (POSTED, REVERSED).Table: glpostingHandles internal enterprise bookkeeping data.glpostingid (BIGINT, PK, AUTO_INCREMENT): System ledger unique sequencing ID.txnid (BIGINT, FK → transaction.txnid): Direct auditing anchor relational link.glaccount (VARCHAR): Operational grouping target category marker (GL-DEPOSITS-CREDIT / GL-DEPOSITS-DEBIT).debitorCredit (VARCHAR): Standard accounting double-entry processing layout identifier (CREDIT, DEBIT).amount (DECIMAL(19,4)): Precise accounting value entry.posteddate (TIMESTAMP): Tracking date validation audit mark.Security FlowDepositCoreX protects runtime environments utilizing a stateless Zero-Trust Token Propagation network pipeline:[Client Login Request] ──> [IAM Service] ──> Returns HMAC-SHA256 Signed JWT
                                                    │
[Axios API Request] <── Attached inside Header ─────┘ (Authorization: Bearer <token>)
       │
       ▼
[API Cloud Gateway]
  ├── 1. Validates JWT signature integrity
  ├── 2. Extracts payload identity claims (userId, role, name)
  └── 3. Injects structural context headers (X-User-Id, X-User-Role, X-User-Name)
       │
       ▼
[Target Microservice Engine]
  └── GatewayAuthFilter reads headers ──> Builds SecurityContext ──> Checked by @PreAuthorize
Database DesignTo guarantee extreme decoupling and completely remove system-wide inter-dependency cascading failure risks, the cluster utilizes a strict Database-per-Service strategy.Relational InstanceResponsible Microservice Application ComponentCore Managed Tables & Relationsdcx_iamIAM Serviceusers, roles, user_rolesdcx_customerCustomer Onboarding Servicecustomer_reference, kyc_docsdcx_casaCASA Servicecasa_account, account_limitsdcx_productProduct Config Serviceproduct_config, fee_structuresdcx_transactionTransaction Servicetransaction, glpostingdcx_interestInterest Serviceinterest_accrual, interest_configdcx_holdHold/Lien Servicehold_lien, standing_instructiondcx_tdTD Servicing Servicetd_account, td_interest_scheduledcx_statementsStatements Servicestatement, statement_entrydcx_notificationsNotifications Servicenotification_logKey Design DecisionsDatabase Isolation: Enforcing the Database-per-Service pattern ensures that if one data node encounters an exception or requires maintenance, the rest of the application remains fully available.Stateless JWT Architecture: Eliminates server-side session tracking entirely, allowing horizontal scaling of the API Gateway and underlying microservices.Declarative Inter-Service Contracts: Using OpenFeign provides an organized, interface-driven way to handle synchronous REST communication without writing verbose HTTP client boilerplate.Atomic Boundary Protection: Standardizing @Transactional across service write methods guarantees automated rollback of data modifications if any downstream step fails during transaction posting.High-Precision Numeric Architecture: Enforcing BigDecimal with a DECIMAL(19,4) data structure prevents floating-point inaccuracies during compounding, conversions, or standard accounting tracking.Isolated Alert Pipelines: Keeping the notification logic wrapped in localized try-catch exception blocks ensures that non-critical secondary alerting failures will never trigger a rollback of a valid financial ledger entry.Performance Query Layering: Leveraging FetchType.LAZY configurations on cross-entity mapping references prevents heavy, unoptimized Hibernate memory loading operations.How to RunPrerequisitesEnsure your local development station has these runtimes correctly configured:Java 21 LTS or later installedMaven 3.8+ dependency manager toolingMySQL 8.x relational database engine instance running locallyNode.js 18+ with npm or yarn bundle manager toolsetsStep 1: Initialize the DatabasesOpen your preferred terminal configuration tool, connect to your local running MySQL server instance, and execute the following database structure assignments:SQLCREATE DATABASE dcx_iam;
CREATE DATABASE dcx_customer;
CREATE DATABASE dcx_casa;
CREATE DATABASE dcx_product;
CREATE DATABASE dcx_transaction;
CREATE DATABASE dcx_interest;
CREATE DATABASE dcx_hold;
CREATE DATABASE dcx_td;
CREATE DATABASE dcx_statements;
CREATE DATABASE dcx_notifications;
Step 2: Boot up the Discovery InfrastructureNavigate into the distributed environment coordination cluster directory to boot up the service registry engine:Bashcd eureka-server
mvn spring-boot:run
The Discovery Registry starts up directly on default port: http://localhost:8761Step 3: Run the Cloud Access Routing NodeOpen a new shell environment window and boot the API edge routing engine:Bashcd api-gateway
mvn spring-boot:run
The Central Router proxy entry port runs on: http://localhost:8079Step 4: Boot Up Backend MicroservicesOpen discrete terminal processes to initialize each independent microservice block:Bash# Initialize Identity Core Routing Service Engine
cd Module_2.1 && mvn spring-boot:run

# Launch Customer Core Lifecycle Control Engine
cd Module_2.2 && mvn spring-boot:run

# Start Account Management Operations Engine
cd Module_2.3 && mvn spring-boot:run

# Start the Core Transaction Service Engine
cd Module_2.5/Transaction && mvn spring-boot:run
Step 5: Start the Presentation Web InterfaceNavigate to the frontend project path, download dependencies, and spin up the development compiler engine:Bashcd frontend
npm install
npm run dev
The interactive React UI interface runs on: http://localhost:5173AuthorSurya — Module 2.5 (Transaction Service Lead)Built with care as a core full-stack milestone implementation component for the DepositCoreX Distributed Banking Microservices Platform project landscape
The interactive React UI interface runs on: http://localhost:5173

Author
Abhinav — Module 2.7,2.10

Built with care as a core full-stack milestone implementation component for the DepositCoreX Distributed Banking Microservices Platform project landscape.
