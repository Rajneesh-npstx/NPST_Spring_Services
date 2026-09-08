# Bharat Bank Omnichannel Platform
## Spring Boot Microservices Functional & Architecture Specification

**Document Version:** 1.0  
**Target Platform:** Bharat Bank Omnichannel Platform (Retail & Corporate)  
**Reference PRD:** Bharat Bank Omnichannel Platform — PRD v1.0  
**Ownership Domain:** Pod D — Core Banking Domains (Spring Boot Microservices)

---

## 1. Executive Overview & Microservice Boundaries

As defined in the Bharat Bank Omnichannel Platform PRD, the backend follows a decoupled microservices architecture. While authentication, bill payments, and admin portal capabilities are maintained in dedicated services (NestJS), all core transactional and financial banking domains are implemented as standalone, horizontally scalable **Spring Boot microservices**.

```
                           +------------------------+
                           |   API Gateway (Kong)   |
                           +-----------+------------+
                                       |
    +-------------------+--------------+---------------+-------------------+
    |                   |                              |                   |
+---v-------------+ +---v------------------------+ +---v-------------+ +---v-------------+
| Account Service | | Funds Transfer Service     | | Term Deposit Svc| |  Loan Service   |
+-----------------+ +----------------------------+ +-----------------+ +-----------------+
    |                   |                              |                   |
    +-------------------+--------------+---------------+-------------------+
                                       |
                        +--------------v---------------+
                        |  Other Banking Services      |
                        |  (Cards & Cheque Operations) |
                        +--------------+---------------+
                                       |
                   +-------------------v-------------------+
                   | Core Banking System (CBS) & Switches  |
                   +---------------------------------------+
```

### Core Spring Boot Services (Pod D Scope)
1. **Account Service (`account-service`):** Master view of customer accounts, balance inquiries, mini/detailed statements, transaction limits, and clearing funds.
2. **Funds Transfer Service (`funds-transfer-service`):** Beneficiary lifecycle, intra-bank transfers, domestic payment rails (IMPS, NEFT, RTGS), and scheduled transfers.
3. **Term Deposit Service (`term-deposit-service`):** Lifecycle management for Fixed Deposits (FD) and Recurring Deposits (RD), advice generation, and tax/TDS document servicing.
4. **Loan Service (`loan-service`):** Loan tracking, repayment schedules, automated standing instructions (Auto-Pay), disbursement schedules, and tax interest certificates.
5. **Other Banking Services (`other-services`):** Debit card security controls (lock/unlock/block) and paper clearing instruments (cheque books, stop payment, status inquiries, demand drafts).

---

## 2. Comprehensive Service Function Breakdown (PRD Mapping)

### 2.1 Account Service
Responsible for read-heavy and configuration-related customer account operations across Savings, Current, Deposit, and Loan categories.

* **Consolidated Account Relationship Summary:** Aggregates real-time balances and status across all account types associated with the authenticated customer's Customer Information File (CIF).
* **Account Information & Profile:** Retrieves branch IFSC, MICR, account status (Active, Dormant, Inactive), nominee status, and operating instructions.
* **Mini Statement:** Fetches the last 10 transactions with transaction type, reference number, and running balance.
* **Detailed Statement:** Supports paginated transaction history with filtering by date range, transaction type (debit/credit), and amount ranges.
* **Periodical Statement Export:** Generates digitally signed statement files in PDF and Excel formats for user-selected periods (enforcing a maximum date window).
* **Transaction Limit Configuration:** Allows customers to set daily/per-transaction limits for domestic transfers, ATM withdrawals, and POS/e-Commerce channels.
* **Inward Clearing Funds Visibility:** Surfaces funds under cheque clearing with expected clearance timelines.

---

### 2.2 Funds Transfer Service
Handles financial routing, beneficiary rules, and transaction dispatch across inter-bank and intra-bank payment rails.

* **Beneficiary Management:**
  * Addition of intra-bank and inter-bank beneficiaries with mandatory OTP verification.
  * Integration with IFSC lookup directory to auto-populate bank and branch details.
  * Enforcement of configurable cooling-off windows (e.g., maximum ₹50,000 within the first 24 hours of addition) to mitigate fraud risk.
  * Beneficiary listing, modification of nicknames, and deletion.
* **Intra-Bank Transfer:** Real-time ledger movement between Bharat Bank accounts.
* **Immediate Payment Service (IMPS):** Instant 24/7 transfers supporting Person-to-Account (P2A) using Account Number and IFSC, and Person-to-Person (P2P) using MMID and mobile number.
* **National Electronic Funds Transfer (NEFT) & Real Time Gross Settlement (RTGS):** Scheduled clearing-batch and high-value gross settlement transfers routed through NPST's payment switch.
* **Scheduled & Recurring Fund Transfers:** One-time future-dated transfers and recurring frequency mandates (weekly, monthly) with edit/cancel capabilities prior to execution.

---

### 2.3 Other Banking Services (Cards & Cheque Operations)
Provides self-service operational workflows for instruments and physical credentials.

* **Card Security Operations:**
  * Real-time soft lock and unlock for debit cards without requiring card re-issuance.
  * Irreversible hard-blocking of compromised/lost debit cards.
  * Setting channel-level card limits (ATM, POS, Contactless/NFC, International).
* **Cheque Servicing:**
  * Cheque book issuance requests with address verification and dispatch tracking.
  * Stop payment instructions on specific cheque numbers or cheque ranges with validation against presentation status.
  * Real-time cheque status inquiry (Presented, Cleared, Returned, Stopped).
* **Demand Draft (DD) Issuance:** Requisition of physical demand drafts with debit to customer account and branch collection/dispatch options.
* **Electronic Statement Registration:** Opt-in/opt-out for monthly automated e-statements via registered email.

---

## 3. Deep-Dive: Term Deposit Service

### 3.1 PRD Functional Requirements
* **Online Booking (FD / RD):** Self-service creation of cumulative/reinvestment Fixed Deposits, non-cumulative deposits (monthly/quarterly interest payout), and Recurring Deposits directly debited from a linked savings or current account.
* **Deposit Advice Generation:** Instant generation and download of digital Fixed Deposit receipts containing deposit principal, tenure, applicable rate, maturity value, and nominee details.
* **Tax Document Servicing:**
  * Tax Deducted at Source (TDS) enquiry and summary.
  * Generation and download of Form 16A (quarterly TDS certificate).
  * Integration and viewing of Tax Credit Statement (Form 26AS).
  * Submission and status verification of Form 15G / Form 15H for exemption of tax deduction.
  * Generation of annual Deposit Interest Certificates for filing income tax returns.

---

### 3.2 Definitely Required Underlying Production Services

To support the customer-facing capabilities reliably, the Term Deposit Service must incorporate the following dedicated backend engines:

#### 1. Compounding & Interest Calculation Engine
* **Cumulative Deposit (Reinvestment Plan):** Implements quarterly compounding following the standard Indian banking formula:
  $$\text{Maturity Amount} = P \times \left(1 + \frac{r}{4}\right)^{4 \times t}$$
  where $P$ is principal, $r$ is annual rate, and $t$ is tenure in years.
* **Non-Cumulative Payout Engine:**
  * Monthly payout: Calculates discounted present value of interest so the effective yield matches the annual rate.
  * Quarterly payout: Simple quarterly payout without compounding.
* **Recurring Deposit (RD) Calculation:** Implements compound interest on monthly tranches using RBI standard uniform quarterly compounding tables.
* **Day-Count Convention:** Standardized calculation on `Actual/365` day count.

#### 2. Premature Liquidation & Penalty Recalculation Engine
* **Tenure-Slab Rate Lookup:** Determines the interest rate applicable for the period the deposit was actually held (not the original contracted tenure).
* **Penalty Application:** Deducts the bank's premature penalty (typically 0.50% to 1.00%) from the applicable slab rate.
* **Recovery of Excess Payouts:** For non-cumulative deposits where monthly or quarterly interest was already paid out, calculates net principal payout by adjusting excess interest credited against the principal balance.

#### 3. Tax Compliance & TDS Deductor Engine
* **Section 194A Income Tax Rule Enforcement:**
  * Tracks cumulative interest earned across all term deposits under a single PAN/CIF in a financial year.
  * Enforces the statutory TDS threshold (₹40,000 for regular individuals, ₹50,000 for senior citizens).
  * Applies 10% TDS for accounts with valid PAN, and 20% for accounts lacking PAN or with inoperative PANs.
* **Form 15G / 15H Validation Service:**
  * Validates age criteria (Form 15H for senior citizens $\ge 60$ years; Form 15G for individuals $< 60$ years).
  * Checks that estimated total income does not exceed the basic tax exemption limit.
  * Automatically pauses future TDS deductions upon successful verification and updates CBS records.

#### 4. Deposit Auto-Renewal & Settlement Engine
* **Maturity Rule Processor:** Executes automated instructions configured at booking:
  * Auto-renew Principal + Interest (cumulative roll-over).
  * Auto-renew Principal only and credit interest to CASA.
  * Close and credit total proceeds to the customer's linked CASA account.
* **Unclaimed Deposit Handling:** Moves matured, non-renewed deposits to overdue deposit accounts earning savings interest rates.

#### 5. Lien Marking & Overdraft Facility Service
* **Lien Registry:** Marks encumbrances/liens on deposits pledged as collateral for Overdraft against FD or third-party facilities.
* **Restricted Liquidation:** Blocks premature closure, auto-renewal changes, or withdrawals on any deposit bearing an active lien.

---

## 4. Deep-Dive: Loan Service

### 4.1 PRD Functional Requirements
* **Loan Relationship Overview:** Consolidated summary of all loan accounts (Home, Personal, Auto, Education, Commercial) under the customer's CIF.
* **Outstanding Balance & Repayment Tracking:** Displays sanctioned loan limit, current principal balance, accrued interest, upcoming EMI amount, and overdue arrears (if any).
* **Next Payment Due Date:** Highlights installment due dates, grace periods, and late-fee alerts.
* **Loan Account Statements:** Paginated ledger statement of past debits, EMI credits, and charges with options to view, print, or email as a password-protected PDF.
* **Auto-Pay / Standing Instruction (SI):** Direct linkage of the customer's operative account to the loan account for automated monthly debit, validated via OTP.
* **Disbursement Schedule Details:** Comprehensive view of total sanctioned amount, already disbursed tranches, pending disbursements, and stage-wise completion requirements.
* **Tax Interest Certificate for Loans:** Provision of provisional and final interest certificates split into:
  * Principal repayment (eligible for deduction under Section 80C).
  * Interest component (eligible for deduction under Section 24b for home loans).

---

### 4.2 Definitely Required Underlying Production Services

The Loan Service requires specialized core banking automation engines to maintain ledger accuracy and regulatory compliance:

#### 1. Reducing-Balance Amortization Engine
* **Equated Monthly Installment (EMI) Computation:**
  Calculates monthly installments using the reducing-balance method:
  $$\text{EMI} = P \times r \times \frac{(1 + r)^n}{(1 + r)^n - 1}$$
  where $P$ is principal, $r$ is monthly interest rate ($\text{Annual Rate} / 1200$), and $n$ is tenure in months.
* **Schedule Generator:** Dynamically projects month-by-month principal amortisation, interest payments, and declining balance for the remaining loan lifecycle.
* **Rate Revision Engine:** Re-amortizes the schedule when floating interest rates change (e.g., RBI repo-rate changes), giving customers the option to extend tenure or increase EMI.

#### 2. Auto-Debit & Standing Instruction Execution Engine
* **Pre-Debit Reminder Service:** Dispatches automated SMS/Email/Push notifications to the customer $T-2$ days prior to the EMI due date, alerting them to maintain sufficient balance.
* **Daily Due-Date Batch Processor:** High-throughput scheduler running on loan installment due dates to trigger debit calls to the customer's CASA account.
* **Retry and Arrears Recovery Logic:**
  * Handles failed debits due to insufficient funds without dropping instructions.
  * Re-attempts debit on subsequent days (e.g., $T+1, T+3$).
  * Calculates penal interest/late payment charges as mandated by credit policy.
* **e-NACH / National Automated Clearing House Integration:** Manages automated collection mandates for external bank accounts when the customer holds their primary salary/operative account outside Bharat Bank.

#### 3. Prepayment, Part-Payment & Foreclosure Service
* **Foreclosure Statement Generator:** Computes exact settlement figures as-of a specific date, including:
  * Outstanding principal balance.
  * Pro-rata interest accrued from the last billing date to the settlement date.
  * Applicable foreclosure/prepayment charges (enforcing regulatory waivers, such as RBI rules prohibiting foreclosure charges on floating-rate individual home loans).
* **Part-Prepayment Allocator:** Re-amortizes the remaining loan upon receipt of ad-hoc lump-sum payments, offering the customer a choice between reduced tenure or lower monthly EMI.
* **NOC & Closure Processing:** Automatically triggers lien release on pledged assets and generates No Objection Certificates (NOC) upon complete loan clearance.

#### 4. Multi-Tranche Disbursement Tracker
* **Tranche Management:** Records partial disbursements against sanctioned limits (common in home construction and project financing).
* **Pre-EMI Interest Computation:** Calculates simple interest solely on the disbursed amount until the final tranche is released and full EMI amortisation commences.

---

## 5. Architectural & Implementation Standards

To ensure stability across all Spring Boot microservices, the following architectural guidelines must be adhered to:

### 5.1 Financial Precision
* All monetary amounts must be handled using `BigDecimal` with explicit scale and `RoundingMode.HALF_UP`. Floating-point types (`float`, `double`) are strictly prohibited in financial calculations.
* Day counts, interest factors, and intermediate calculations must use standardized `MathContext` (minimum 10 digits of precision).

### 5.2 Idempotency & Concurrency
* All financial operations (FD creation, premature closure, transfer initiation, standing instruction registration) must require an `Idempotency-Key` header.
* Distributed locking (via Redis or database row-level locking) must guard account balance modifications to eliminate race conditions.

### 5.3 Audit & Compliance Logging
* Every service must generate structured audit logs capturing:
  * Customer CIF and Authenticated User ID.
  * Originating IP, device fingerprint, and channel (Mobile/Admin).
  * Pre-operation state and post-operation state for configuration updates.
  * CBS transaction reference and response codes.
* No sensitive customer data (passwords, debit card PINs, OTPs, CVVs) may appear in system logs or telemetry.

### 5.4 CBS Adapter & Circuit Breaking
* Microservices must never be tightly coupled to proprietary CBS protocols (e.g., ISO 8583, legacy XML RPC). 
* Each service must interact with Core Banking through an Anti-Corruption Layer (CBS Adapter) equipped with circuit breakers (e.g., Resilience4j) and fallback mechanisms to ensure graceful degradation during CBS maintenance windows.
