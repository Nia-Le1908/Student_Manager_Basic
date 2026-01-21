# 🎓 University Credit Management System

![Java](https://img.shields.io/badge/Language-Java_17+-orange?style=flat-square)
![GUI](https://img.shields.io/badge/GUI-JavaFX-blue?style=flat-square)
![Database](https://img.shields.io/badge/Database-MySQL-blue?style=flat-square)
![Pattern](https://img.shields.io/badge/Architecture-MVC-green?style=flat-square)

> **A desktop application for managing university credits, featuring an automated "Student Aspiration" system and strict Role-Based Access Control (RBAC).**

## 📖 Introduction

This project is not just a simple CRUD application. It simulates a real-world **University Academic Portal** where students can manage their grades and register for courses.

The core highlight of this system is the **"Dynamic Class Opening" (Nguyện vọng)** feature. It solves the problem of scheduling retake classes by allowing students to vote/request courses. The system automatically processes these requests based on specific academic rules.

## 🚀 Key Features & Business Logic

### 1. 📈 Automated Course Aspiration (Logic Highlight)
This is the most complex logic in the system, handling the workflow of opening new classes based on demand.
* **Eligibility Check:** Students can only request to open a class if:
    * Their previous grade is below **C (GPA < 2.0)** (Retake).
    * OR they have never taken the course before (New registration).
* **Automation:** The system monitors the `Aspiration_Queue`. When the number of registered students for a specific subject reaches **10**, the system automatically changes the class status to **"OPEN"** and notifies the Academic Affairs department.
* **Anti-Fraud:** Lecturers are strictly prohibited from creating or modifying aspiration requests to ensure fairness.

### 2. 🛡️ Role-Based Access Control (RBAC)
The system implements strict security layers via the MVC Controller:
* **Admin:** Full access to system configuration and master data.
* **Lecturer:** Can only view and grade students in *classes they are assigned to*. Cannot access the global student database.
* **Student:** Can only view their own transcripts and submit course requests.

### 3. 📊 Academic Performance Tracking
* Real-time GPA/CPA calculation.
* Visualizing student performance trends (optional).

## 🛠️ Tech Stack & Architecture

* **Pattern:** **MVC (Model-View-Controller)** - Separation of concerns for maintainability.
* **Language:** Java (JDK 17 or higher).
* **GUI:** JavaFX (FXML for UI design).
* **Database:** MySQL.
* **Database Connectivity:** JDBC / Hibernate (nếu bạn dùng Hibernate thì sửa lại, không thì để JDBC).

## 🗄️ Database Design

The database schema is designed to handle Many-to-Many relationships between Students and Courses.

*(Place your ER Diagram image here - e.g., erd_diagram.png)*

**Key Tables:**
* `Students`, `Lecturers`
* `Courses` (Subject metadata)
* `Open_Classes` (Actual classes being taught)
* `Aspirations` (Stores student requests - **Key logic table**)

## 📸 Screenshots

| Login Screen | Student Dashboard | Aspiration Form |
|:---:|:---:|:---:|
| <img width="740" height="370" alt="image" src="https://github.com/user-attachments/assets/28d99db3-d002-497d-93a1-7b9d8bd2b74f" />
 | <img width="1196" height="777" alt="image" src="https://github.com/user-attachments/assets/17faf307-9976-4f12-93c0-9fe58a28b1c0" />
 | <img width="902" height="755" alt="image" src="https://github.com/user-attachments/assets/f38ab6ec-5035-4f06-bd79-8b64e70133a3" />
 |

## ⚙️ Installation & Setup

1.  **Clone the repository**
    ```bash
    git clone [https://github.com/username/university-credit-system.git](https://github.com/username/university-credit-system.git)
    ```
2.  **Database Setup**
    * Create a MySQL database named `university_db`.
    * Import the `database_schema.sql` file provided in the `sql/` folder.
    * Update database credentials in `src/utils/DatabaseConnection.java`.
    * <img width="815" height="791" alt="image" src="https://github.com/user-attachments/assets/d8f22cf9-8195-4880-9ac2-e0ed77fb3dc3" />

3.  **Run the Application**
    * Open the project in IntelliJ IDEA or Eclipse.
    * Run `Laucher.java`.

## 🧠 What I Learned

* **Complex SQL Logic:** Handling triggers and stored procedures to count student requests efficiently.
* **JavaFX State Management:** Managing UI states based on user roles (hiding buttons for Students vs Lecturers).
* **OOP Principles:** Applying Encapsulation and Inheritance in User models.

## 🤝 Contact

* **Author:** Trung Nghia
* **Email:** nghialt123528@gmail.com
