# TradeJournal - Portfolio Analytics & Trade Management System

> A professional Java-based trading platform with web and desktop interfaces, MySQL database, and rule-based risk evaluation.

![Status](https://img.shields.io/badge/status-complete-green) ![Java](https://img.shields.io/badge/java-21-blue) ![Spring Boot](https://img.shields.io/badge/spring%20boot-2.7.14-brightgreen) ![JavaFX](https://img.shields.io/badge/javafx-21-purple) ![MySQL](https://img.shields.io/badge/mysql-8.0-blue)

---

## 📋 Table of Contents

- [Quick Start](#-quick-start)
- [Features](#-features)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Building](#-building)
- [Running](#-running)
- [Usage Guide](#-usage-guide)
- [Project Structure](#-project-structure)
- [API Endpoints](#-api-endpoints)
- [Troubleshooting](#-troubleshooting)

---

## 🚀 Quick Start (60 seconds)

### **The Easiest Way**

Just **double-click** this file:
```
run-both.bat
```

Done! You'll see:
1. ✅ Black console window (backend running)
2. ✅ JavaFX desktop app opens automatically
3. ✅ Web app available at `http://localhost:8080`

**That's it!** Both web and desktop apps are running.

---

## ✨ Features

### 🌐 Web Dashboard (`http://localhost:8080`)
- **Modern dark-themed UI** with cyan accents
- **Real-time KPI cards**: P&L, Win Rate, Total Trades
- **Trade entry form** with validation preview
- **Analytics dashboard** with strategy breakdowns
- **Trade history** with search and filtering
- **Risk rule enforcement** prevents bad trades
- **Fully responsive design**

### 💻 Desktop Application (JavaFX)
- **Native Windows desktop app**
- **Professional UI** matching web version
- **Real-time metrics** synced with backend
- **Trade submission form** with validation
- **Activity log** showing all operations
- **Fast, lightweight, no browser needed**
- **Connects to same backend API**

### 📊 Backend Services (Spring Boot)
- **REST API** on port 8080
- **MySQL database** for persistent storage
- **Rule-Based Risk Engine**:
  - Concentration Rule: Max 10% of capital per trade
  - Position Size Rule: 1-10,000 shares per trade
  - Fully extensible for custom rules
- **Portfolio Analytics**:
  - Profit & Loss calculations
  - Win rate tracking  
  - Strategy-specific performance
  - Risk violation audit logs
- **Automatic data persistence**

---

## 📦 Prerequisites

### Required Software
1. **Java 21** → [Download JDK 21](https://www.oracle.com/java/technologies/downloads/)
2. **Maven 3.6+** → [Download Maven](https://maven.apache.org/download.cgi)
3. **MySQL 8.0+** → [Download MySQL](https://dev.mysql.com/downloads/)

### Verify Installation
```powershell
java -version          # Should show Java 21+
mvn --version          # Should show Maven 3.6+
mysql --version        # Should show MySQL 8.0+
```

---

## 📥 Installation & Setup

### Step 1: Install Maven (If Not Already Done)

**Windows:**
1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract to: `C:\Program Files\apache-maven-3.9.14-bin\apache-maven-3.9.14`
3. Add to System PATH:

```powershell
[Environment]::SetEnvironmentVariable(
    "PATH",
    $env:PATH + ";C:\Program Files\apache-maven-3.9.14-bin\apache-maven-3.9.14\bin",
    [EnvironmentVariableTarget]::Machine
)
```

4. Verify: `mvn --version`

### Step 2: Prepare MySQL

```powershell
# Start MySQL service (or install if needed)
# Open Command Prompt and run:

mysql -u root -p"redBlue3011!" -e "CREATE DATABASE IF NOT EXISTS TradeJournal;"

# Verify:
mysql -u root -p"redBlue3011!" -e "SELECT 1;"
```

### Step 3: Load Database Schema

```powershell
cd "c:\Users\saik3\OneDrive\Pictures\Desktop\College\DBS MiniProject"

mysql -u root -p"redBlue3011!" TradeJournal < setup_tables.sql
```

---

## 🔨 Building the Project

### Build Everything (Compile + Package)

```powershell
cd "c:\Users\saik3\OneDrive\Pictures\Desktop\College\DBS MiniProject"

mvn clean package -DskipTests
```

This will:
- ✓ Download all dependencies
- ✓ Compile Java source code (Java 21)
- ✓ Create executable JAR: `target/trade-journal-1.0.0.jar`
- ✓ Package web assets (HTML, CSS, JavaScript)

Takes ~1-2 minutes on first build, ~20 seconds on subsequent builds.

---

## ▶️ Running the Application

### **Option 1: One-Click Start (RECOMMENDED)**

Simply **double-click**: `run-both.bat`

Or from PowerShell:
```powershell
cd "c:\Users\saik3\OneDrive\Pictures\Desktop\College\DBS MiniProject"
.\run-both.bat
```

**What happens:**
1. Black console opens → Backend starts on port 8080
2. Waits 5 seconds for backend to initialize
3. JavaFX desktop app window opens
4. Both apps run simultaneously

### **Option 2: Manual - Separate Terminals**

**Terminal 1 - Start Backend:**
```powershell
cd "c:\Users\saik3\OneDrive\Pictures\Desktop\College\DBS MiniProject"
java -jar target/trade-journal-1.0.0.jar
```
Wait for: `Tomcat started on port(s): 8080`

**Terminal 2 - Start Desktop App:**
```powershell
cd "c:\Users\saik3\OneDrive\Pictures\Desktop\College\DBS MiniProject"
& "C:\Program Files\apache-maven-3.9.14-bin\apache-maven-3.9.14\bin\mvn.cmd" javafx:run
```

**Terminal 3 - Access Web:**
```
Open browser: http://localhost:8080
```

### **Option 3: Web Application Only**

```powershell
java -jar target/trade-journal-1.0.0.jar
```

Then open: `http://localhost:8080`

### **Option 4: Desktop Application Only**

```powershell
mvn javafx:run
```

*(Requires backend running in another terminal)*

---

## 💡 Usage Guide

### 🌐 Web Dashboard

Access at: `http://localhost:8080`

#### Dashboard Tab
1. View real-time portfolio metrics
2. See P&L, Win Rate, Total Trades
3. Check strategy-wise performance

#### Trade Entry Tab
1. **Symbol**: Enter stock symbol (e.g., RELIANCE, TCS, INFY)
2. **Type**: Select BUY or SELL
3. **Quantity**: Enter 1-10,000 shares
4. **Price**: Enter price per share (₹)
5. **Strategy**: Select strategy ID (1, 2, 3, etc.)
6. Click **"Submit Trade"**

**System validates:**
- ✓ Trade doesn't exceed 10% of portfolio capital
- ✓ Quantity is between 1-10,000
- Shows error message if validation fails ❌
- Adds trade to portfolio if approved ✅

#### Analytics Tab
- View per-strategy P&L
- Compare strategy performance
- Track aggregate portfolio metrics
- Monitor P&L trends

#### History Tab
- Search trades by date/symbol/strategy
- View detailed trade information
- Check trade execution history

### 💻 JavaFX Desktop App

A separate window opens with:

#### Dashboard Tab
- **KPI Cards** at top showing:
  - Total P&L (cyan)
  - Win Rate (green)
  - Total Trades (orange)
- **Real-time updates** from backend
- **Professional dark theme**

#### Add Trade Tab
- Form to submit new trades
- Same validation as web version
- Instant feedback in log

#### Navigation
- **🔄 Refresh**: Update all metrics
- **📈 Trades**: Go to main dashboard
- **⚙ Settings**: API configuration

#### Activity Log (Bottom)
- Shows all API calls
- Connection status
- Trade submissions
- Error messages

---

## 📁 Project Structure

```
DBS MiniProject/
│
├── 📄 README.md                          ← You are here
├── 📄 BUILD_AND_RUN.md                   Detailed setup guide
├── 📄 ARCHITECTURE.md                    System design docs
├── 📄 pom.xml                            Maven configuration
│
├── 🚀 run-both.bat                       ONE-CLICK LAUNCHER ⭐
├── 🚀 run-both.ps1                       PowerShell version
│
├── 🗄️ setup_tables.sql                   Database schema
│
├── src/main/java/
│   ├── com/trade/                        Business logic layer
│   │   ├── model/
│   │   │   ├── Trade.java                Trade transactions
│   │   │   ├── User.java                 User accounts
│   │   │   ├── Strategy.java             Trading strategies
│   │   │   ├── RiskRule.java             Rule interface
│   │   │   ├── ConcentrationRule.java    10% concentration limit
│   │   │   └── PositionSizeRule.java     1-10k shares limit
│   │   ├── repository/
│   │   │   └── TradeRepository.java      Database access
│   │   └── service/
│   │       ├── RiskEvaluationService.java    Risk validation
│   │       └── PortfolioAnalyticsService.java Analytics engine
│   │
│   └── com/tradejournal/                 Spring Boot layer
│       ├── TradeJournalApplication.java  Main entry point
│       ├── javafx/
│       │   └── TradeJournalFX.java       Desktop app
│       ├── controller/
│       │   └── TradeController.java      REST API endpoints
│       ├── service/
│       │   └── AnalyticsWrapper.java     API bridge
│       ├── dto/                          Data transfer objects
│       │   └── *.java                    Request/Response models
│       └── config/
│           └── WebConfig.java            Spring configuration
│
│   └── resources/
│       ├── application.properties
│       └── static/
│           ├── index.html                Web dashboard
│           ├── app.js                    Frontend logic
│           └── style.css                 Styling
│
├── target/
│   ├── classes/                          Compiled code
│   └── trade-journal-1.0.0.jar           Executable JAR ⭐
│
└── web/                                  Static assets
    ├── index.html
    ├── app.js
    └── style.css
```

---

## 🔌 API Endpoints

All endpoints: `http://localhost:8080/api/`

### Health Check
```
GET /api/health
```
Returns: `{ "status": "OK", "database": "TradeJournal" }`

### Portfolio Analytics
```
GET /api/analytics
```
Returns: Portfolio P&L, win rate, strategy stats, total trades

### Trade Management
```
GET /api/trades                  List all trades
POST /api/trades                Submit new trade
GET /api/trades/{id}            Get specific trade
DELETE /api/trades/{id}         Delete trade
```

### Strategy Information
```
GET /api/strategies             List all strategies
```

---

## 🗄️ Database Schema

### Connection Details
```
Host:     localhost
Port:     3306
Database: TradeJournal
User:     root
Password: redBlue3011!
```

### Tables

**Trade** - Transaction log
```sql
id (PK) | portfolio_id (FK) | symbol | trade_type | quantity | 
price | trade_date | strategy_id (FK)
```

**Portfolio** - Capital tracking
```sql
id (PK) | user_id (FK) | total_capital
```

**Strategy** - Strategy definitions
```sql
id (PK) | name | description
```

**User** - User accounts
```sql
id (PK) | name | email
```

**RiskFlag** - Violation audit log
```sql
id (PK) | portfolio_id (FK) | rule_violated | message | flag_date
```

**PerformanceSummary** - Analytics results
```sql
portfolio_id (PK/FK) | total_pnl | win_rate | last_updated
```

---

## 🔧 Troubleshooting

### ❌ "mvn is not recognized"
**Solution:** Maven isn't in PATH
```powershell
# Add Maven to PATH:
[Environment]::SetEnvironmentVariable(
    "PATH",
    $env:PATH + ";C:\Program Files\apache-maven-3.9.14-bin\apache-maven-3.9.14\bin",
    [EnvironmentVariableTarget]::Machine
)
# Restart PowerShell and try again
```

### ❌ "Cannot connect to MySQL"
**Solution:** Check MySQL service
```powershell
# Test connection:
mysql -u root -p"redBlue3011!" -e "SELECT 1;"

# If fails, start MySQL:
# Windows Services → MySQL80 → Right-click → Start
```

### ❌ "Port 8080 already in use"
**Solution:** Kill existing process
```powershell
# Find what's using port 8080:
netstat -ano | findstr :8080

# Kill the process (replace PID):
taskkill /PID {PID} /F

# Then try again:
java -jar target/trade-journal-1.0.0.jar
```

### ❌ "JAR file not found"
**Solution:** Build the project
```powershell
mvn clean package -DskipTests
```
Wait for **BUILD SUCCESS** message.

### ❌ "Desktop app won't open / Can't load analytics"
**Solution:** Backend isn't running
1. Check the black console window is open
2. Look for: `Tomcat started on port(s): 8080`
3. If missing, backend crashed - check error messages
4. Run `run-both.bat` again

### ❌ "Application startup failed"
**Solution:** Check logs
1. Look at the black console window for error messages
2. Common issues:
   - MySQL not running
   - Database 'TradeJournal' doesn't exist
   - Wrong MySQL password
   - Port 8080 in use

---

## 🌍 Deployment

### Deploy to Production

#### **Option 1: Standalone Server (AWS/Azure/DigitalOcean)**
```powershell
# Build
mvn clean package -DskipTests

# Copy JAR and MySQL to server
# Run:
java -jar trade-journal-1.0.0.jar
```

#### **Option 2: Docker Container**
Create `Dockerfile`:
```dockerfile
FROM openjdk:21-slim
COPY target/trade-journal-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build & run:
```powershell
docker build -t tradejournal .
docker run -p 8080:8080 tradejournal
```

#### **Option 3: Heroku**
```powershell
heroku login
heroku create your-app-name
git push heroku main
```

#### **Option 4: AWS Elastic Beanstalk**
1. Package JAR
2. Upload to Elastic Beanstalk
3. Configure RDS MySQL
4. Deploy

---

## 📚 Tech Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Backend** | Spring Boot | 2.7.14 |
| **Language** | Java | 21 |
| **Database** | MySQL | 8.0+ |
| **Build** | Maven | 3.6+ |
| **Desktop** | JavaFX | 21.0.2 |
| **Frontend** | HTML/CSS/JS | ES6 |

---

## 📞 Support & Help

### Common Issues
1. Read [Troubleshooting](#-troubleshooting) section above
2. Check console/log output
3. Verify all prerequisites installed
4. Ensure MySQL is running

### Getting More Help
- Check `BUILD_AND_RUN.md` for step-by-step instructions
- Review `ARCHITECTURE.md` for system design
- Look at error messages in terminal

---

## ✅ What You Now Have

- ✅ **Web Application** - Modern dashboard UI
- ✅ **Desktop Application** - Native Windows app
- ✅ **REST API** - Programmatic access
- ✅ **Database** - Persistent data storage
- ✅ **Risk Engine** - Automated trade validation
- ✅ **Analytics** - Portfolio performance tracking
- ✅ **One-Click Launcher** - Easy to start

---

## 🎓 Project Info

- **Type**: College Mini Project (Database Systems)
- **Status**: ✅ Complete & Production-Ready
- **Last Updated**: March 2026

---

**Happy Trading! 📈** 🚀

For more details, see:
- `BUILD_AND_RUN.md` - Comprehensive setup guide
- `ARCHITECTURE.md` - System design & components
- `IMPLEMENTATION_GUIDE.md` - Deep dive into code
