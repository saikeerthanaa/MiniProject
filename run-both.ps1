# ═══════════════════════════════════════════════════════════════════
# TradeJournal - Start Both Backend and JavaFX Desktop App
# ═══════════════════════════════════════════════════════════════════

$projectPath = "c:\Users\saik3\OneDrive\Pictures\Desktop\College\DBS MiniProject"
$jarFile = Join-Path $projectPath "target\trade-journal-1.0.0.jar"
$mavenPath = "C:\Program Files\apache-maven-3.9.14-bin\apache-maven-3.9.14\bin\mvn.cmd"

Write-Host ""
Write-Host "╔═══════════════════════════════════════════════════════════════╗"
Write-Host "║         TradeJournal - Backend + Desktop App Launcher        ║"
Write-Host "╚═══════════════════════════════════════════════════════════════╝"
Write-Host ""

# Check if JAR exists
if (-not (Test-Path $jarFile)) {
    Write-Host "ERROR: JAR file not found at $jarFile" -ForegroundColor Red
    Write-Host "Please run: mvn clean package -DskipTests" -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Starting processes..." -ForegroundColor Green
Write-Host ""

# Start Backend in new PowerShell window
Write-Host "[1/2] Starting Spring Boot Backend on port 8080..." -ForegroundColor Cyan
$backendScript = "cd '$projectPath'; java -jar '$jarFile'; Read-Host 'Press Enter to close'"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $backendScript -WindowStyle Normal

# Wait 5 seconds for backend to initialize
Write-Host "Waiting for backend to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# Start JavaFX in new PowerShell window
Write-Host "[2/2] Starting JavaFX Desktop App..." -ForegroundColor Cyan
$javafxScript = "cd '$projectPath'; & '$mavenPath' javafx:run; Read-Host 'Press Enter to close'"
Start-Process powershell -ArgumentList "-NoExit", "-Command", $javafxScript -WindowStyle Normal

Write-Host ""
Write-Host "✓ Both applications started!" -ForegroundColor Green
Write-Host ""
Write-Host "  Backend:  http://localhost:8080" -ForegroundColor Green
Write-Host "  Desktop:  JavaFX window (should open automatically)" -ForegroundColor Green
Write-Host ""
Write-Host "You can close this window - the apps will keep running in separate windows" -ForegroundColor Yellow
Write-Host ""
