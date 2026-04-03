# Install Maven if needed
$MavenHome = "$env:USERPROFILE\maven"
$MavenBin = "$MavenHome\apache-maven-3.8.4\bin"
$MavenZip = "$env:TEMP\maven384.zip"

Write-Host "Maven Installation Script" -ForegroundColor Cyan

# Check if Maven already installed
if (Test-Path "$MavenBin\mvn.cmd") {
    Write-Host "Maven already installed  at $MavenHome" -ForegroundColor Green
} else {
    Write-Host "Installing Maven 3.8.4..." -ForegroundColor Yellow
    
    # Create directory
    if (-not (Test-Path $MavenHome)) {
        New-Item -ItemType Directory -Path $MavenHome -Force | Out-Null
    }
    
    # Download
    Write-Host "Downloading from archive.apache.org..." -ForegroundColor Yellow
    [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
    $url = "https://archive.apache.org/dist/maven/maven-3/3.8.4/binaries/apache-maven-3.8.4-bin.zip"
    
    try {
        (New-Object System.Net.WebClient).DownloadFile($url, $MavenZip)
        Write-Host "Download complete!" -ForegroundColor Green
    } catch {
        Write-Host "Download failed: $_" -ForegroundColor Red
        exit 1
    }
    
    # Extract   
    Write-Host "Extracting..." -ForegroundColor Yellow
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::ExtractToDirectory($MavenZip, $MavenHome, $true)
    Remove-Item $MavenZip
    Write-Host "Installation complete!" -ForegroundColor Green
}

# Add to PATH
$env:PATH = "$MavenBin;$env:PATH"

# Test Maven
Write-Host "Testing Maven..." -ForegroundColor Cyan
& "$MavenBin\mvn.cmd" -v

# Build the project
Write-Host "`nBuilding project..." -ForegroundColor Cyan
cd "c:\Users\saik3\OneDrive\Pictures\Desktop\College\DBS MiniProject"
& "$MavenBin\mvn.cmd" clean spring-boot:run
