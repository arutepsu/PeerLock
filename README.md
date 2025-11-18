<p align="center">
  <img src="peerlock-assets\main\peerlockShort.png"
       alt="PeerLock Logo"
       height="400" />
</p>

<p align="center">
  <strong>Secure Peer-to-Peer Messaging in Java</strong><br/>
  🔐 JavaFX Client • 🧩 Spring Boot Auth Server • 📡 P2P TCP Sockets
</p>

<p align="center">

  <!-- Java -->
  <img src="https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white" />

  <!-- Spring Boot -->
  <img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />

  <!-- JavaFX (no native logo, using OpenJDK) -->
  <img src="https://img.shields.io/badge/JavaFX-1F8ACB?style=for-the-badge&logo=openjdk&logoColor=white" />

  <!-- Maven -->
  <img src="https://img.shields.io/badge/Maven-Build_Tool-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" />

  <!-- TCP/P2P (no official logo → use generic) -->
  <img src="https://img.shields.io/badge/P2P_Networking-444?style=for-the-badge&logo=network&logoColor=white" />

  <!-- JSON (valid logo name: json) -->
  <img src="https://img.shields.io/badge/JSON-000?style=for-the-badge&logo=json&logoColor=white" />

  <!-- SQLite -->
  <img src="https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white" />

  <!-- BCrypt (no official logo → use lock) -->
  <img src="https://img.shields.io/badge/BCrypt-555?style=for-the-badge&logo=lock&logoColor=white" />

  <!-- REST API (no official logo → use swagger) -->
  <img src="https://img.shields.io/badge/REST_API-0052CC?style=for-the-badge&logo=swagger&logoColor=white" />

  <!-- Token Auth (use JSON Web Tokens) -->
  <img src="https://img.shields.io/badge/Token_Auth-JWT_Style-CC0000?style=for-the-badge&logo=jsonwebtokens&logoColor=white" />

</p>

---

# 🚀 Overview

PeerLock is a modern **peer-to-peer encrypted messaging app** written in Java.  
It combines a **Spring Boot authentication server** with a **JavaFX desktop client** that communicates directly over TCP sockets.

This project demonstrates:

- 🔒 Token-based authentication  
- 📡 Direct peer-to-peer TCP communication  
- 🖥️ JavaFX UI with dynamic updates  
- 💾 Local chat history  
- ⚙️ EventBus-driven architecture  

---

# 🌟 Features

### 🔐 Authentication Server (Spring Boot)
- User registration + login  
- Password hashing with BCrypt  
- Stateless session tokens  
- Token validation filter  
- Peer registry to track online users  

---

### 📡 Peer-to-Peer Client (Java)
- Direct TCP socket sessions  
- Secure handshake during connection  
- Heartbeat to maintain online presence  
- Automatic reconnection handling  
- Local offline chat history  

---

### 🖥️ JavaFX UI
- Login screen  
- Main chat screen  
- Peer list with avatars  
- Chat history view  
- Message input + send button  

---

# 🧩 Architecture
For a detailed explanation of how PeerLock works internally — including the server component, client P2P mechanism, message flow, and design see:

👉 **[architecture.md](peerlock-assets/docs/architecture.md)**  

# 🧠 How PeerLock Works (Simple Explanation)

PeerLock consists of two parts:

### **1️⃣ Authentication Server**
- Runs on your machine  
- Stores registered users  
- Issues session tokens  
- Tracks online users  
- Shares peer IP/port information  

### **2️⃣ Client Application**
- Logs in to the server  
- Registers itself as "online"  
- Retrieves list of other online peers  
- Connects directly to them using TCP sockets  
- Exchanges messages without sending them through the server  

The server is *only* used for authentication and peer lookup.  
All chat messages go **directly between clients**.

---

## 🛠️ Setup Guide
### 1️⃣ Clone the Repository
```bash
git clone https://github.com/arutepsu/peerlock.git
cd peerlock
```

### 2️⃣ Run the Authentication Server (Spring Boot)
#### Requirements: 
- Java 21+
- Maven 3+

#### Run the server
```bash
cd peerlock-server
mvn spring-boot:run
```

This will start the server at: http://localhost:8080

### 3️⃣ Run the JavaFX Client (P2P Chat Application)
#### Requirements: 
- Java 21+
- Maven 3+

#### Run the client
```bash
cd peerlock-client
mvn exec:java
```

The JavaFX window should now open, allowing you to:

- Log in or register
- See online peers
- Open direct P2P chat sessions

### 4️⃣ How It Works After Startup

- Start server: handles login & peer registry
- Start multiple clients (on same LAN)
- Each client:
- Logs in
- Registers its IP + port
- Fetches list of online peers
- When selecting a peer:
- A direct TCP connection is opened
- Secure handshake occurs
- Chat begins P2P

## 👨‍💻 Developed by Arutepsu
