#!/bin/bash

# LiveContext-AI Shutdown Script
# This script stops all running services

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  LiveContext-AI Shutdown Script${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Get the project root directory
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

# Stop Frontend
#if [ -f /tmp/frontend.pid ]; then
#    echo -e "${RED}Stopping Frontend...${NC}"
#    kill $(cat /tmp/frontend.pid) 2>/dev/null || true
#    rm /tmp/frontend.pid
#    echo -e "${GREEN}✓ Frontend stopped${NC}\n"
#fi

# Stop Backend
#if [ -f /tmp/backend.pid ]; then
#    echo -e "${RED}Stopping Backend...${NC}"
#    kill $(cat /tmp/backend.pid) 2>/dev/null || true
#    rm /tmp/backend.pid
#    echo -e "${GREEN}✓ Backend stopped${NC}\n"
#fi

# Stop System MCP
if [ -f /tmp/system-mcp.pid ]; then
    echo -e "${RED}Stopping System MCP...${NC}"
    kill $(cat /tmp/system-mcp.pid) 2>/dev/null || true
    rm /tmp/system-mcp.pid
    echo -e "${GREEN}✓ System MCP stopped${NC}\n"
fi

# Stop Weather MCP
if [ -f /tmp/weather-mcp.pid ]; then
    echo -e "${RED}Stopping Weather MCP...${NC}"
    kill $(cat /tmp/weather-mcp.pid) 2>/dev/null || true
    rm /tmp/weather-mcp.pid
    echo -e "${GREEN}✓ Weather MCP stopped${NC}\n"
fi

# Stop News MCP
if [ -f /tmp/news-mcp.pid ]; then
    echo -e "${RED}Stopping News MCP...${NC}"
    kill $(cat /tmp/news-mcp.pid) 2>/dev/null || true
    rm /tmp/news-mcp.pid
    echo -e "${GREEN}✓ News MCP stopped${NC}\n"
fi

# Stop Market MCP
if [ -f /tmp/market-mcp.pid ]; then
    echo -e "${RED}Stopping Market MCP...${NC}"
    kill $(cat /tmp/market-mcp.pid) 2>/dev/null || true
    rm /tmp/market-mcp.pid
    echo -e "${GREEN}✓ Market MCP stopped${NC}\n"
fi

# Stop PostgreSQL
echo -e "${RED}Stopping PostgreSQL...${NC}"
cd "$PROJECT_ROOT"
docker-compose stop postgres
echo -e "${GREEN}✓ PostgreSQL stopped${NC}\n"

echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}✓ All services stopped successfully!${NC}"
echo -e "${BLUE}========================================${NC}"
