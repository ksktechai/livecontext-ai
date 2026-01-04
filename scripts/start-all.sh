#!/bin/bash

# LiveContext-AI Startup Script
# This script starts all services in the correct order

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  LiveContext-AI Startup Script${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Get the project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

# Step 1: Start PostgreSQL
echo -e "${YELLOW}[1/6] Starting PostgreSQL...${NC}"
docker-compose up -d postgres
echo -e "${GREEN}✓ PostgreSQL started on port 5432${NC}\n"

# Wait for PostgreSQL to be healthy
echo -e "${YELLOW}Waiting for PostgreSQL to be ready...${NC}"
sleep 5
echo -e "${GREEN}✓ PostgreSQL is ready${NC}\n"

# Step 2: Start Market MCP
echo -e "${YELLOW}[2/6] Starting Market MCP...${NC}"
cd "$PROJECT_ROOT/mcp/market-mcp"
npm install > /dev/null 2>&1
npm run build > /dev/null 2>&1
npm start > /tmp/market-mcp.log 2>&1 &
echo $! > /tmp/market-mcp.pid
sleep 2
echo -e "${GREEN}✓ Market MCP started on port 8091${NC}\n"

# Step 3: Start News MCP
echo -e "${YELLOW}[3/6] Starting News MCP...${NC}"
cd "$PROJECT_ROOT/mcp/news-mcp"
npm install > /dev/null 2>&1
npm run build > /dev/null 2>&1
npm start > /tmp/news-mcp.log 2>&1 &
echo $! > /tmp/news-mcp.pid
sleep 2
echo -e "${GREEN}✓ News MCP started on port 8092${NC}\n"

# Step 4: Start Weather MCP
echo -e "${YELLOW}[4/6] Starting Weather MCP...${NC}"
cd "$PROJECT_ROOT/mcp/weather-mcp"
npm install > /dev/null 2>&1
npm run build > /dev/null 2>&1
npm start > /tmp/weather-mcp.log 2>&1 &
echo $! > /tmp/weather-mcp.pid
sleep 2
echo -e "${GREEN}✓ Weather MCP started on port 8093${NC}\n"

# Step 5: Start System MCP
echo -e "${YELLOW}[5/6] Starting System MCP...${NC}"
cd "$PROJECT_ROOT/mcp/system-mcp"
npm install > /dev/null 2>&1
npm run build > /dev/null 2>&1
npm start > /tmp/system-mcp.log 2>&1 &
echo $! > /tmp/system-mcp.pid
sleep 2
echo -e "${GREEN}✓ System MCP started on port 8094${NC}\n"

# Step 6: Start Backend
echo -e "${YELLOW}[6/6] Starting Java Backend...${NC}"
cd "$PROJECT_ROOT/backend"
java -jar build/libs/livecontext-backend-1.0.0.jar > /tmp/backend.log 2>&1 &
echo $! > /tmp/backend.pid
sleep 5
echo -e "${GREEN}✓ Backend started on port 8080${NC}\n"

# Step 7: Start Frontend
echo -e "${YELLOW}[7/7] Starting Frontend...${NC}"
cd "$PROJECT_ROOT/frontend"
npm install > /dev/null 2>&1
npm run dev > /tmp/frontend.log 2>&1 &
echo $! > /tmp/frontend.pid
sleep 3
echo -e "${GREEN}✓ Frontend started on port 5173${NC}\n"

# Summary
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}✓ All services started successfully!${NC}"
echo -e "${BLUE}========================================${NC}\n"

echo "Service Status:"
echo "  • PostgreSQL:    http://localhost:5432 (docker)"
echo "  • Market MCP:    http://localhost:8091/health"
echo "  • News MCP:      http://localhost:8092/health"
echo "  • Weather MCP:   http://localhost:8093/health"
echo "  • System MCP:    http://localhost:8094/health"
echo "  • Backend API:   http://localhost:8080/actuator/health"
echo "  • Frontend UI:   http://localhost:5173"
echo ""
echo "View logs:"
echo "  tail -f /tmp/market-mcp.log"
echo "  tail -f /tmp/news-mcp.log"
echo "  tail -f /tmp/weather-mcp.log"
echo "  tail -f /tmp/system-mcp.log"
echo "  tail -f /tmp/backend.log"
echo "  tail -f /tmp/frontend.log"
echo ""
echo -e "${YELLOW}To stop all services, run: ./scripts/stop-all.sh${NC}"
