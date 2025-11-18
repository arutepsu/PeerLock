<p align="center">
  <img src="..\main\peerlockShort.png"
       alt="PeerLock Logo"
       height="400" />
</p>


# 🧠 PeerLock Architecture

PeerLock uses a hybrid architecture:  
**centralized authentication and peer discovery**, combined with  
**decentralized peer-to-peer communication** for all messages.

This provides simplicity, privacy, and zero message storage on the server.

---

# 1️⃣ Components Overview

PeerLock consists of two major components:

## **1. Authentication + Peer Registry Server (Spring Boot)**

The server acts as the *control plane* of the system.  
It is responsible for:

### 🔐 Authentication
- Register users  
- Login users  
- Issue session tokens  
- Validate tokens on API requests  

### 📡 Peer Registry
Tracks all online clients:
- Username  
- IP address  
- Listening port  
- Last-seen timestamps  

### 📝 Important
➡️ **The server never handles or stores chat messages.**  
➡️ It only helps clients find each other.

---

## **2. JavaFX Client (Peer-to-Peer Node)**

Each client behaves as a small **P2P node**, consisting of:

### 🔁 TCP Socket Server
- Listens on a local port  
- Accepts incoming connections from peers  
- Performs identity + version handshake  
- Receives chat messages  

### 📤 TCP Socket Client
- Connects to another peer’s IP + port  
- Performs the same handshake  
- Sends messages directly  

### 🔌 Direct Communication
Once connected:

- Messages go **directly from client to client**
- No server routing
- No server storage
- Optionally encrypted payload

---

# 2️⃣ Internal Client Architecture

### ⚙️ EventBus
The client uses an internal EventBus to keep the app modular:
- Socket receivers trigger events  
- UI listens to these events  
- Local storage listens to events  
- Peer list updates trigger UI refresh  

This avoids blocking the JavaFX UI thread.

---

# 3️⃣ Local Message Storage

Each user stores their own message history locally:

