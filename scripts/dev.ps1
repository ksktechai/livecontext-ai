Write-Host "🚀 Starting LiveContext-AI Development Environment..." -ForegroundColor Cyan

# Check Docker is running
try {
    docker info | Out-Null
} catch {
    Write-Host "❌ Docker is not running. Please start Docker and try again." -ForegroundColor Red
    exit 1
}

# Check if Ollama is running (optional)
try {
    Invoke-WebRequest -Uri "http://localhost:11434/api/tags" -UseBasicParsing -TimeoutSec 2 | Out-Null
} catch {
    Write-Host "⚠️  Ollama is not running. Backend will run in mock mode." -ForegroundColor Yellow
    Write-Host "   To install Ollama: https://ollama.ai" -ForegroundColor Yellow
    Write-Host ""
}

# Start services
Write-Host "📦 Starting Docker Compose services..." -ForegroundColor Cyan
docker compose up -d

Write-Host ""
Write-Host "⏳ Waiting for services to be healthy..." -ForegroundColor Cyan
Start-Sleep -Seconds 5

# Wait for backend
Write-Host "🔍 Checking backend health..." -ForegroundColor Cyan
for ($i = 1; $i -le 30; $i++) {
    try {
        Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing -TimeoutSec 2 | Out-Null
        Write-Host "✅ Backend is ready!" -ForegroundColor Green
        break
    } catch {
        Write-Host "   Waiting for backend... ($i/30)" -ForegroundColor Gray
        Start-Sleep -Seconds 2
    }
}

Write-Host ""
Write-Host "✨ LiveContext-AI is running!" -ForegroundColor Green
Write-Host ""
Write-Host "🌐 Frontend:  http://localhost:5173" -ForegroundColor White
Write-Host "🔧 Backend:   http://localhost:8080" -ForegroundColor White
Write-Host "📊 Health:    http://localhost:8080/actuator/health" -ForegroundColor White
Write-Host ""
Write-Host "📋 View logs:" -ForegroundColor Cyan
Write-Host "   docker compose logs -f backend" -ForegroundColor Gray
Write-Host "   docker compose logs -f market-mcp" -ForegroundColor Gray
Write-Host ""
Write-Host "🛑 To stop:" -ForegroundColor Cyan
Write-Host "   docker compose down" -ForegroundColor Gray
Write-Host ""
