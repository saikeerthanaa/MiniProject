# Trade Journal - Render.com Deployment Guide

## Step 1: Prepare Your GitHub Repository

If not already done:
```bash
git init
git add .
git commit -m "Initial commit - ready for deployment"
git remote add origin https://github.com/YOUR_USERNAME/trade-journal.git
git push -u origin main
```

## Step 2: Create Free Render Account
1. Go to https://render.com
2. Sign up with GitHub
3. Allow access to your GitHub account

## Step 3: Create PostgreSQL Database on Render
1. Click **New +** → **PostgreSQL**
2. Set name: `trade-journal-db`
3. Leave other settings default
4. Click **Create Database**
5. Wait 2-3 minutes for database to be ready
6. Copy the **External Database URL**
   - Look for button labeled "Copy" next to "External Database URL"
   - Format: `postgresql://user:password@host/database`

## Step 4: Convert Database URL for Java/MySQL Driver

Your Render PostgreSQL URL looks like: `postgresql://user:pass@host:5432/db`

Convert to JDBC format:
```
jdbc:postgresql://host:5432/db
```

**Example**:
- PostgreSQL URL: `postgresql://xxxx:yyyy@abc.com:5432/tradejournal`
- JDBC URL: `jdbc:postgresql://abc.com:5432/tradejournal`
- Username: `xxxx`
- Password: `yyyy`

## Step 5: Create Web Service on Render
1. Click **New +** → **Web Service**
2. Connect your GitHub repo `trade-journal`
3. Settings:
   - **Name**: `trade-journal`
   - **Runtime**: `Java`
   - **Build Command**: `mvn clean package -q`
   - **Start Command**: `java -jar target/trade-journal-1.0.0.jar`
   - **Instance Type**: `Free` (for testing)
4. Click **Create Web Service**

## Step 6: Set Environment Variables
1. Go to your web service settings
2. Click **Environment**
3. Add these variables:

```
DB_URL     = jdbc:postgresql://xxxx.onrender.com:5432/your_db
DB_USER    = postgres_user
DB_PASSWORD = postgres_password
PORT       = 10000
```

4. Save and redeploy

## Step 7: Run Database Migration Script
1. Connect to your PostgreSQL database using any client:
   - DBeaver
   - pgAdmin
   - psql CLI

2. Run the setup_tables.sql script

## Step 8: Test the Deployment
1. Your app will be at: `https://trade-journal.onrender.com`
2. Test endpoints:
   - Health: `https://trade-journal.onrender.com/api/health`
   - Dashboard: `https://trade-journal.onrender.com/index.html`

---

## Alternative: MySQL on Railway (Similar Process)

If you prefer MySQL instead of PostgreSQL:

1. Go to https://railway.app
2. Sign up with GitHub
3. Create new project → Add MySQL
4. Get connection string
5. Add Railway service and set env vars
6. Same JDBC connection format

---

## Troubleshooting Errors

### Error: "Cannot connect to database"
- Check **DB_URL** format is correct
- Verify credentials in PostgreSQL admin panel
- Make sure network is allowing external connections

### Error: "Tables do not exist"
- Run setup_tables.sql on the hosted database

### Error: "Port already in use"
- Render assigns PORT automatically - don't override it
- Use `${PORT:8080}` in application.properties

### Error: "Application crashed after startup"
- Check logs in Render dashboard
- Verify database connection is working
- Check table structure matches application expectations
