@echo off
REM ═══════════════════════════════════════════════════
REM TradeJournal — Build & Run Script (Windows)
REM ═══════════════════════════════════════════════════

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERROR: Maven is not installed or not in PATH!
    echo.
    echo Please install Apache Maven:
    echo   1. Download from: https://maven.apache.org/download.cgi
    echo   2. Extract to: C:\Program Files\Apache\maven
    echo   3. Add C:\Program Files\Apache\maven\bin to System PATH
    echo   4. Restart Command Prompt and try again
    echo.
    pause
    exit /b 1
)

REM Check if MySQL is running
echo Checking MySQL connection...
mysql -u root -p"redBlue3011!" -e "SELECT 1" >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo WARNING: Cannot connect to MySQL!
    echo.
    echo Please start MySQL and verify:
    echo   - MySQL is running
    echo   - Database 'TradeJournal' exists
    echo   - User 'root' with password 'redBlue3011!' is configured
    echo.
    echo After starting MySQL, restart this script.
    pause
    exit /b 1
)

echo.
echo ✓ Prerequisites verified
echo ✓ Maven found
echo ✓ MySQL connection OK
echo.

REM Get command from argument
if "%1%"=="" (
    echo Usage:
    echo   run-build.bat build     - Compile the project
    echo   run-build.bat start     - Run the application
    echo   run-build.bat clean     - Clean build artifacts
    echo   run-build.bat rebuild   - Clean build and compile
    pause
    exit /b 1
)

REM Execute requested command
if "%1%"=="build" (
    echo Running: mvn clean compile
    call mvn clean compile
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo ✓ Build successful!
        echo.
        echo Next step: run-build.bat start
    ) else (
        echo.
        echo ✗ Build failed!
        pause
    )
)

if "%1%"=="start" (
    echo.
    echo Starting TradeJournal application...
    echo Dashboard will be available at: http://localhost:8080/
    echo.
    call mvn spring-boot:run
)

if "%1%"=="clean" (
    echo Running: mvn clean
    call mvn clean
)

if "%1%"=="rebuild" (
    echo Running: mvn clean install
    call mvn clean install
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo ✓ Rebuild successful!
        echo.
        echo Next step: run-build.bat start
    ) else (
        echo.
        echo ✗ Rebuild failed!
        pause
    )
)

pause
