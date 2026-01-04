#!/bin/bash
set -e

echo "🚀 Starting LiveContext-AI Development Environment..."

# Check Docker is running
if ! docker info > /dev/null 2>&1; then
  echo "❌ Docker is not running. Please start Docker and try again."
  exit 1
fi

# Check if Ollama is running (optional)
if ! curl -s http://localhost:11434/api/tags > /dev/null 2>&1; then
  echo "⚠️  Ollama is not running. Backend will run in mock mode."
  echo "   To install Ollama: https://ollama.ai"
  echo ""
fi

# Start services
echo "📦 Starting Docker Compose services..."
docker compose up -d

echo ""
echo "⏳ Waiting for services to be healthy..."
sleep 5

# Wait for backend
echo "🔍 Checking backend health..."
for i in {1..30}; do
  if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "✅ Backend is ready!"
    break
  fi
  echo "   Waiting for backend... ($i/30)"
  sleep 2
done

echo ""
echo "✨ LiveContext-AI is running!"
echo ""
echo "🌐 Frontend:  http://localhost:5173"
echo "🔧 Backend:   http://localhost:8080"
echo "📊 Health:    http://localhost:8080/actuator/health"
echo ""
echo "📋 View logs:"
echo "   docker compose logs -f backend"
echo "   docker compose logs -f market-mcp"
echo ""
echo "🛑 To stop:"
echo "   docker compose down"
echo ""
