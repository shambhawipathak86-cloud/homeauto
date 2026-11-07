# HomeAuto - Smart Home Control System

A college project developed for home automation control. This project provides a web dashboard that allows users to monitor and control household appliances like lighting and air conditioning using simple UI buttons or voice commands in English.

---

## What It Does

- Turn devices on and off (Lights, AC, switches) from a web browser.
- Voice control: Speak commands through the microphone to operate appliances.
- Clean dashboard interface built with standard web technologies.
- Java Spring Boot backend to handle requests and manage the application.

---

## Tech Stack

- **Backend:** Java 17+, Spring Boot
- **Frontend:** HTML, CSS, JavaScript (Web Speech API for voice input)
- **Build Tool:** Maven (Wrapper included)

---

## Project Structure

```text
homeauto/
├── src/
│   ├── main/
│   │   ├── java/com/example/homeauto/      # Java source code
│   │   └── resources/
│   │       ├── static/                    # HTML, CSS, and JS files
│   │       └── application.properties     # App configuration
│   └── test/                              # Tests
├── pom.xml                                # Maven dependencies
└── mvnw / mvnw.cmd                        # Maven wrapper scripts
