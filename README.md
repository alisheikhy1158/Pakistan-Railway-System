# Pakistan-Railway-System
This was a semester project which I created with my group member at Comsats University Islamabad.
1. Abstract
The Pakistan Railway System is a desktop-based GUI application developed in Java using Object-Oriented Programming principles. It enables users to search trains, book tickets, view and cancel bookings, and navigate through various system options in an interactive graphical interface. The application simulates a real-world ticket reservation experience while storing booking information in local text files. Designed for academic purposes, it highlights the use of classes, file handling, encapsulation, and GUI building using Java Swing.

2. Introduction
Background
Railway systems around the world depend on digital management tools to streamline booking and ticketing operations. This project mimics a basic version of such a system with a strong focus on OOP concepts, enabling students to understand how programming logic maps to real-world applications.
Problem Statement
Console-based or manual ticketing systems are inefficient, prone to human error, and lack user-friendliness. This project addresses this by offering an intuitive, modular, GUI-driven ticket booking system using Java Swing.
Objectives
•	Develop a GUI-based train reservation system
•	Apply OOP principles: encapsulation, modularity, and event-driven programming
•	Enable users to search trains, select dates, book, and cancel tickets
•	Use file handling to save booking records persistently

3. System Overview
Main Features
•	Train search using source/destination dropdowns
•	Calendar for selecting travel dates
•	Ticket booking and data storage in text file
•	Ticket viewing and cancellation functionality
•	Navigation between screens with a consistent UI style

4. System Design and Architecture
Major classes and their purposes:
•	Train: Represents a train with details like ID, source, destination, and time.
•	Passenger: Contains details about the user booking the ticket.
•	Ticket: Encapsulates train and passenger details and travel date.
•	PakistanRailwaySystem: Main class that manages all GUI screens and events.
OOP Concepts Applied:
•	Encapsulation: All classes hide data and provide access via getters/setters.
•	Inheritance: GUI forms follow reusable structures.
•	Polymorphism: Action listeners handle events dynamically.
•	Modularity: GUI logic is separated into different panels and methods.

5. Technologies Used
•	Java (JDK 21) – Core logic and OOP design
•	Java Swing – GUI development
•	Text Files – For booking persistence
•	VS Code – Development environment

6. GUI Flow Overview
Although actual screenshots aren’t shown here, below is the logical GUI flow:
1.	Main Menu – Options: Book Ticket, View Bookings
2.	Train Search – Dropdowns to select source/destination
3.	Calendar View – Select travel date
4.	Passenger Info Entry – Input name, Gender
5.	Confirmation Page – Shows thank-you message
6.	Cancel Ticket – Remove existing bookings

7. Data Handling
Bookings are stored as comma-separated lines in a .txt file. This simulates a basic database while keeping the project file-based for simplicity.

8. Testing and Validation
The application was tested for:
•	Field validation (empty inputs)
•	Navigation between screens
•	Duplicate booking entries
•	Files append accuracy
•	Booking cancellation flow
Basic error handling and input checks are implemented.


