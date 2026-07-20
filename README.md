<!-- PROJECT HEADER -->

<br />
<div align="center">

<img src="src/main/resources/com/example/cabiso_capstone/CABANA.png" width="180"/>

# 🏠 Cabana Dormitory Management System

<p align="center">

A desktop-based Dormitory Management Information System developed using <strong>JavaFX</strong>, <strong>MySQL</strong>, <strong>JDBC</strong>, and <strong>Java Serialization</strong>. The application streamlines tenant administration, room allocation, payment management, and secure authentication through a centralized and user-friendly platform.

</p>

</div>

---

# 📚 Table of Contents

- [About the Project](#-about-the-project)
- [Key Features](#-key-features)
- [Technologies Used](#-technologies-used)
- [System Architecture](#-system-architecture)
- [Java Serialization](#-java-serialization)
- [SOLID Principles Applied](#-solid-principles-applied)
- [Database Design](#-database-design)
- [Project Structure](#-project-structure)
- [Installation](#-installation)
- [Future Improvements](#-future-improvements)
- [Author](#-author)

---

# 📌 About the Project

The **Cabana Dormitory Management System** is a desktop-based information management application designed to modernize the daily administrative operations of boarding houses and dormitories. Instead of relying on paper records or spreadsheets, the system centralizes tenant information, room assignments, payment transactions, and authentication into a single integrated platform.

Built with **JavaFX**, **MySQL**, and **JDBC**, the application follows the **Model–View–Controller (MVC)** architecture to ensure modularity, maintainability, and scalability. It also implements **Java Serialization** to provide persistent user sessions, allowing authenticated users to continue their previous session after restarting the application until they explicitly log out.

The system currently supports two user roles:

### 👨‍💼 Administrator

The administrator has full access to manage the dormitory operations, including:

- Tenant Management
- Room Management
- Payment Management
- Tenant Approval
- Room Assignment & Transfer
- Occupancy Monitoring
- Dashboard Overview

### 🧑 Tenant

Each tenant has access to a personalized dashboard where they can:

- Securely log in
- View assigned room information
- View outstanding balance
- View payment history
- Access personal account information

---

# 🚀 Key Features

## 🔐 Authentication

- Administrator & Tenant Login
- Tenant Registration
- Administrator Approval Workflow
- Role-Based Access Control
- Account Status Validation
- Persistent Login Sessions
- Secure Logout

---

## 👥 Tenant Management

- Register New Tenant
- Approve Tenant Accounts
- Update Tenant Information
- Assign & Transfer Rooms
- Search & Filter Tenants
- Deactivate Tenant Accounts
- Automatic Occupancy Synchronization

---

## 🏠 Room Management

- Add & Update Rooms
- Search & Filter Rooms
- Capacity Validation
- Occupancy Monitoring
- Available Slot Calculation
- Room Status Management

---

## 💳 Payment Management

- Record Payments
- Update Payment Information
- Void Payments *(Soft Delete)*
- Search & Filter Payments
- Automatic Balance Computation
- Duplicate Payment Prevention

---

# 💻 Technologies Used

| Category | Technology |
|------------|------------|
| Programming Language | Java |
| User Interface | JavaFX |
| UI Layout | FXML |
| Styling | CSS |
| Database | MySQL |
| Database Connectivity | JDBC |
| Build Tool | Maven |
| IDE | IntelliJ IDEA |
| Session Persistence | Java Serialization |
| Software Architecture | MVC |

---

# 🏛 System Architecture

The application follows the **Model–View–Controller (MVC)** architectural pattern to separate the presentation layer, business logic, and data access layer.

```text
                       USER
                         │
                         ▼
               JavaFX User Interface
              (FXML Views + CSS Files)
                         │
                         ▼
                  JavaFX Controllers
────────────────────────────────────────────
 LoginController
 RegisterController
 AdminDashboardController
 TenantDashboardController
 TenantController
 RoomController
 PaymentController
────────────────────────────────────────────
                         │
                         ▼
                Business Logic Layer
────────────────────────────────────────────
 Authentication
 Validation
 Room Assignment
 Payment Computation
 Session Management
 Business Rules
────────────────────────────────────────────
                         │
                         ▼
                Persistence Layer
────────────────────────────────────────────
 JDBC
 Java Serialization
 DatabaseConnection
────────────────────────────────────────────
                         │
                         ▼
                     MySQL Database
────────────────────────────────────────────
 users
 tenants
 rooms
 payments
────────────────────────────────────────────
```

### Advantages

- Separation of Concerns
- Easier Maintenance
- Better Scalability
- Improved Readability
- Reusable Components

---

# 💾 Java Serialization

## Overview

The application implements **Java Object Serialization** to preserve authenticated user sessions. Once a user successfully logs in, a serialized session object is stored locally, allowing the application to restore the previous session automatically when reopened.

---

## Classes Involved

| Class | Responsibility |
|---------|----------------|
| `UserSession` | Stores authenticated user information |
| `SessionStorage` | Defines session storage abstraction |
| `FileSessionStorage` | Reads and writes serialized session files |
| `SessionManager` | Manages session creation, validation, loading, and deletion |

---

## Serialization Workflow

```text
Successful Login
        │
        ▼
SessionManager.saveSession()
        │
        ▼
session.txt Created
        │
        ▼
Application Closed
        │
        ▼
Application Reopened
        │
        ▼
SessionManager.loadSession()
        │
        ▼
Valid Session?
    │           │
   Yes         No
    │           │
    ▼           ▼
Dashboard     Login Screen
    │
    ▼
Logout
    │
    ▼
SessionManager.deleteSession()
    │
    ▼
session.txt Deleted
```

### Serialized Information

The serialized session stores only the following information:

- User ID
- Tenant ID *(if applicable)*
- Username
- Full Name
- User Role
- Account Status

> **Passwords are never serialized or stored inside the session file.**

### Benefits

- Persistent user sessions
- Automatic session restoration
- Faster application startup
- Improved user convenience
- Secure session validation
- Simplified desktop authentication

---

# 🧩 SOLID Principles Applied

The project applies two SOLID principles to improve maintainability and software quality.

## 1. Single Responsibility Principle (SRP)

Each class has only **one responsibility** and one reason to change.

| Class | Responsibility |
|--------|----------------|
| `UserSession` | Stores session information |
| `SessionManager` | Controls session lifecycle |
| `FileSessionStorage` | Handles serialization only |
| `DatabaseConnection` | Establishes database connections |
| Controllers | Manage JavaFX user interactions |

**Benefits**

- Better maintainability
- Easier debugging
- Cleaner architecture
- Higher code readability

---

## 2. Dependency Inversion Principle (DIP)

Instead of depending directly on file serialization, `SessionManager` communicates through the `SessionStorage` abstraction.

```text
SessionManager
      │
      ▼
SessionStorage (Interface)
      │
      ▼
FileSessionStorage
```

**Benefits**

- Loose coupling
- Easier future extensions
- Greater flexibility
- Better scalability
- Supports alternative storage implementations
# 🗄 Database Design

The system uses a relational database in **MySQL** to manage authentication, tenant information, room allocation, and payment transactions.

## Database Tables

| Table | Description |
|--------|-------------|
| **users** | Stores login credentials and user roles. |
| **tenants** | Stores tenant profile information. |
| **rooms** | Stores room details, capacity, rates, and availability. |
| **payments** | Stores tenant payment transactions and payment history. |

---

## Entity Relationships

```text
                   users
                     │
                     │ 1 : 1
                     ▼
                  tenants
                     │
         ┌───────────┴───────────┐
         │                       │
         │ MANY                  │ MANY
         ▼                       ▼
      payments                rooms
```

### Relationship Summary

| Relationship | Description |
|--------------|-------------|
| Users → Tenants | One user account corresponds to one tenant profile. |
| Rooms → Tenants | One room can accommodate multiple tenants up to its capacity. |
| Tenants → Payments | One tenant may have multiple payment records. |

---

# 📂 Project Structure

The project follows a modular package organization to improve maintainability and readability.

```text
Cabiso_Capstone
│
├── README.md
├── pom.xml
│
└── src
    └── main
        ├── java
        │   └── com.example.cabiso_capstone
        │       │
        │       ├── controllers
        │       │   ├── LoginController.java
        │       │   ├── RegisterController.java
        │       │   ├── AdminDashboardController.java
        │       │   ├── TenantDashboardController.java
        │       │   ├── TenantController.java
        │       │   ├── RoomController.java
        │       │   └── PaymentController.java
        │       │
        │       ├── database
        │       │   └── DatabaseConnection.java
        │       │
        │       ├── model
        │       │   ├── User.java
        │       │   ├── Administrator.java
        │       │   ├── Tenant.java
        │       │   ├── Room.java
        │       │   └── Payment.java
        │       │
        │       ├── session
        │       │   ├── UserSession.java
        │       │   ├── SessionStorage.java
        │       │   ├── FileSessionStorage.java
        │       │   └── SessionManager.java
        │       │
        │       │
        │       ├── MainApplication.java
        │       └── module-info.java
        │
        └── resources
            ├── *.fxml
            ├── *.css
            └── assets
```

---

# ⚙ Installation

## Prerequisites

Before running the project, ensure the following software is installed:

- Java Development Kit (JDK)
- IntelliJ IDEA
- Apache Maven
- MySQL Server

---

## Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/Cabana-Dormitory-Management-System.git
```

---

### 2. Open the Project

Open the project using **IntelliJ IDEA**.

---

### 3. Configure the Database

Create a MySQL database and import the provided SQL script.

---

### 4. Update Database Credentials

Edit the database configuration inside:

```text
DatabaseConnection.java
```

Update the following values:

- Database URL
- Username
- Password

---

### 5. Reload Maven

Allow IntelliJ IDEA to download all required dependencies.

---

### 6. Run the Application

Run:

```text
MainApplication.java
```

The application will automatically launch.


---

# 🔮 Future Improvements

The current implementation serves as a strong foundation for future development. Planned enhancements include:

- Password hashing using BCrypt
- Email verification and notifications
- PDF receipt generation
- Dashboard analytics and reports
- Audit logs
- Backup and restore functionality
- QR Code-based tenant check-in
- Multi-administrator support
- Advanced role and permission management
- Cloud-based data synchronization

---

# 👨‍💻 Author

<div align="center">


## Chestine May Mari C. Cabiso

**Bachelor of Science in Information Technology**

**Cebu Institute of Technology – University**

*Cabana Dormitory Management System*

OOP2 Capstone Mini Project • 2026

</div>

---

# License
<div align="center">

This project was developed as an academic requirement for the **Object-Oriented Programming 2 (OOP2)** course at **Cebu Institute of Technology – University**.

The source code is intended for **educational and portfolio purposes only**. Commercial redistribution or unauthorized reproduction without proper attribution is discouraged.

</div>
