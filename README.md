KioskSystem
An interactive Java‑based kiosk application for user sign‑ups, reporting, analytics, and company configuration.
Built as a standalone desktop system with persistent storage, CSV export, and a clean modular architecture.

Overview
KioskSystem is a fully‑functional kiosk application designed to simulate a real‑world sign‑up and reporting workflow.
It handles:

User registration

Company configuration

Data persistence

Advertising lists

Pro membership tracking

CSV report generation

Historical snapshot storage

Console‑based analytics

The project demonstrates practical Java development skills, clean architecture, and real‑world problem solving.

Custom Library: jTidy
This project includes jTidy, a custom Java library I built myself.

I created jTidy to demonstrate my ability to:

Design and implement a reusable Java library

Package and distribute it as a standalone .jar

Integrate it into other projects

Manage dependencies and build artifacts

Work with modular Java codebases

jTidy is bundled inside the kiosk’s executable JAR, showing that I can build both applications and the libraries they depend on.

Features
User System
Guided sign‑up flow

Age, gender, membership, and contact capture

Pro membership tracking

Advertising list generation

Company Configuration
Timeout settings

Reset options

Persistent company info storage

Reporting & Analytics
Console‑based report output

CSV export for external analysis

Historical .datal snapshots

Demographic breakdowns

Usage statistics

Storage
.data files for user records

.datal files for historical snapshots

Automatic folder creation

Clean separation of concerns

Project Structure
Code
src/
└── com/Kiosk/
    ├── NewUserSignUp.java
    ├── CompanyInfo.java
    ├── User.java
    ├── UserStorage.java
    ├── AdvertiseTo.java
    ├── ReportUtility.java
    └── Print.java

lib/
└── jTidy.jar   (custom library I built)

out/
└── artifacts/
    └── Kiosk.jar   (runnable application)
How to Run
Requires Java 17+.

Download the release JAR or run locally:

Code
java -jar Kiosk.jar
The kiosk will launch immediately and begin the interactive sign‑up flow.

Why I Built This
This project was created to demonstrate:

Strong Java fundamentals

Ability to design complete systems

Real‑world data handling

Clean modular architecture

Practical problem‑solving

Ability to build both applications and libraries

Professional project packaging and GitHub presentation

It serves as a portfolio piece showing my capability as a Java developer ready for commercial work.

Future Improvements
GUI version (JavaFX or Swing)

Admin dashboard

Export to JSON

Cloud‑based storage option

Graphical reporting

Multi‑company support

License
Creative Commons Attribution–NonCommercial (CC BY‑NC)
