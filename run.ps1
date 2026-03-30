# Script chay project Student Management System

Write-Host "=== Student Management System Runner ===" -ForegroundColor Cyan

# Kiem tra Java
Write-Host "Checking Java..." -ForegroundColor Yellow
java -version
if ($LASTEXITCODE -ne 0) {
    Write-Host "Java not found!" -ForegroundColor Red
    exit 1
}

# Kiem tra MySQL
Write-Host "Checking MySQL..." -ForegroundColor Yellow
$mysqlService = Get-Service -Name "MySQL*" -ErrorAction SilentlyContinue
if ($mysqlService -and $mysqlService.Status -eq "Running") {
    Write-Host "MySQL is running" -ForegroundColor Green
} else {
    Write-Host "MySQL is not running!" -ForegroundColor Red
    exit 1
}

# Tao thu muc output
$outputDir = "target/classes"
if (!(Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
}

# Copy resources
Write-Host "Copying resources..." -ForegroundColor Yellow
Copy-Item -Path "src/main/resources/*" -Destination $outputDir -Recurse -Force

# Tim JAR files
Write-Host "Finding dependencies..." -ForegroundColor Yellow
$jars = Get-ChildItem -Path ".m2/repository" -Filter "*.jar" -Recurse | Select-Object -ExpandProperty FullName
$classpath = ($jars -join ";") + ";$outputDir"
Write-Host "Found $($jars.Count) JAR files" -ForegroundColor Green

# Compile
Write-Host "Compiling Java files..." -ForegroundColor Yellow
$sourceFiles = Get-ChildItem -Path "src/main/java" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName

javac -encoding UTF-8 -d $outputDir -cp $classpath $sourceFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "Compilation successful" -ForegroundColor Green
} else {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}

# Run
Write-Host ""
Write-Host "Starting application..." -ForegroundColor Cyan
Write-Host ""

java -cp $classpath com.qlsv.Main
