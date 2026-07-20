
# 🏠 Cabana Dormitory Management System

> A desktop-based dormitory management application built with JavaFX, MySQL, and Java Serialization to simplify tenant administration, room allocation, payment tracking, and secure user authentication.

---

# Overview

The **Cabana Dormitory Management System** is a desktop information system developed to digitize the daily operations of a boarding house or dormitory. The application replaces traditional paper-based and spreadsheet-based record keeping with an integrated platform that centralizes tenant management, room administration, payment processing, and user authentication.

The system provides two distinct user roles:

- **Administrator** – responsible for managing tenants, rooms, payments, and overall system operations.
- **Tenant** – allowed to securely access personal account information, assigned room details, outstanding balances, and payment history.

To improve usability, the application implements **persistent login sessions through Java Serialization**, allowing authenticated users to continue their previous session until they explicitly log out.

---

# Objectives

The project aims to:

- Digitize dormitory administrative processes.
- Improve the accuracy of tenant and room records.
- Reduce manual errors during payment tracking.
- Provide role-based access control.
- Demonstrate object-oriented software engineering principles using Java.

---

# System Modules

## Authentication Module

Responsible for user identity verification and authorization.

### Features

- Administrator Login
- Tenant Login
- Tenant Registration
- Account Approval Workflow
- Session Serialization
- Automatic Session Restoration
- Secure Logout
- Role-Based Access Control

---

## Tenant Management Module

Handles the complete lifecycle of tenant accounts.

### Capabilities

- Register new tenants
- Approve pending registrations
- Update tenant information
- Deactivate tenant accounts
- Assign rooms
- Transfer rooms
- Search tenants
- Filter tenants by status
- Automatic room synchronization

Business Rules

- Pending tenants cannot log in.
- Inactive tenants cannot log in.
- A tenant cannot occupy multiple rooms simultaneously.
- Room assignment updates occupancy automatically.

---

## Room Management Module

Manages physical dormitory rooms.

### Capabilities

- Add rooms
- Update room information
- Deactivate rooms
- Search rooms
- Filter room status
- Occupancy calculation
- Available-slot computation

Business Rules

- Capacity cannot be exceeded.
- Full rooms reject new assignments.
- Maintenance rooms cannot receive tenants.
- Inactive rooms cannot receive tenants.
- Occupancy updates automatically after every assignment.

---

## Payment Management Module

Maintains all tenant financial transactions.

### Capabilities

- Record payments
- Update payments
- Void payments (Soft Delete)
- Search payment records
- Filter payment status
- Automatic balance computation
- Duplicate payment prevention

Business Rules

- Only one PAID payment is allowed per tenant for the same billing month.
- Voided payments remain stored for auditing.
- Tenant balance updates automatically after every payment modification.

---

# Session Management

The application uses **Java Object Serialization** to preserve authenticated sessions.

Workflow:

```text
Successful Login
        │
        ▼
session.dat created
        │
        ▼
Application Closed
        │
        ▼
Session Restored Automatically
        │
        ▼
Correct Dashboard Opens
        │
        ▼
Logout
        │
        ▼
session.dat deleted
````

Serialized Data

* User ID
* Tenant ID
* Username
* Full Name
* Role
* Account Status

Passwords are **never serialized**.

---

# Software Architecture

The project follows a layered architecture:

```text
Presentation Layer
(JavaFX Views + Controllers)

        │

Business Logic Layer
(Session Manager
Validation
Business Rules)

        │

Persistence Layer
(JDBC
MySQL
Serialization)

        │

Database
(users
tenants
rooms
payments)
```

---

# SOLID Principles Applied

## Single Responsibility Principle (SRP)

Each class has one well-defined responsibility.

Examples:

* UserSession
* SessionManager
* DatabaseConnection
* TenantController
* RoomController
* PaymentController

---

## Dependency Inversion Principle (DIP)

Session management depends on the **SessionStorage abstraction** instead of a concrete serialization implementation.

```text
SessionManager
      │
      ▼
SessionStorage (Interface)
      │
      ▼
FileSessionStorage
```

---

# Technologies

| Category        | Technology       |
| --------------- | ---------------- |
| Language        | Java             |
| GUI             | JavaFX           |
| Database        | MySQL            |
| Connectivity    | JDBC             |
| Build Tool      | Maven            |
| IDE             | IntelliJ IDEA    |
| Session Storage | Java Serialization |
| Architecture    | MVC              |

---

# Database Design

Main Tables

* users
* tenants
* rooms
* payments

Relationships

```text
users
   │
   │ 1:1
   ▼
tenants
   │
   │ 1:M
   ▼
payments

rooms
   │
   │ 1:M
   ▼
tenants
```

---

# Project Structure

```text
src
└──java
     ├── controllers
     ├── database
     ├── model
     ├── session
 └── MainApplication.java
 └── module-info.java
 └──resources
    └── FXML files
```

---

# Installation

1. Clone the repository.
2. Configure the MySQL database.
3. Import the provided SQL schema.
4. Update `DatabaseConnection.java`.
5. Reload Maven dependencies.
6. Run `MainApplication.java`.

---

# Future Enhancements

* Email notifications
* Monthly reports
* Receipt generation
* Data backup and recovery
* Password hashing
* Role permission management
* Dashboard analytics
* Audit logs

---

# Author

**Chestine May Mari C. Cabiso**

Bachelor of Science in Information Technology

Cebu Institute of Technology – University

Capstone Mini Project

