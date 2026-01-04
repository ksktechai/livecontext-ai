# LiveContext-AI Quick Start Guide

## Prerequisites

- **Docker & Docker Compose** - For PostgreSQL
- **Java 25** - For the backend
- **Node.js LTS** (v20+) - For MCP servers and frontend
- **Gradle** - Already included via gradlew wrapper

## Quick Start (Automated)

### Start Everything

```bash
./scripts/start-all.sh
```

This will start all services in the correct order:
1. PostgreSQL (Docker)
2. Market MCP (port 8091)
3. News MCP (port 8092)
4. Weather MCP (port 8093)
5. System MCP (port 8094)
6. Backend API (port 8080)
7. Frontend UI (port 5173)

### Stop Everything

```bash
./scripts/stop-all.sh
```

## Manual Startup (Step by Step)

If you prefer to start services manually or want more control:

### 1. Start PostgreSQL

```bash
docker-compose up -d postgres
```

Wait for it to be healthy:
```bash
docker-compose ps postgres
```

### 2. Start MCP Servers

Open 4 separate terminals and run:

**Terminal 1 - Market MCP:**
```bash
cd mcp/market-mcp
npm install
npm run build
npm start
```

**Terminal 2 - News MCP:**
```bash
cd mcp/news-mcp
npm install
npm run build
npm start
```

**Terminal 3 - Weather MCP:**
```bash
cd mcp/weather-mcp
npm install
npm run build
npm start
```

**Terminal 4 - System MCP:**
```bash
cd mcp/system-mcp
npm install
npm run build
npm start
```

### 3. Start Backend

**Terminal 5 - Backend:**
```bash
cd backend
java -jar build/libs/livecontext-backend-1.0.0.jar
```

### 4. Start Frontend

**Terminal 6 - Frontend:**
```bash
cd frontend
npm install
npm run dev
```

## Access the Application

Once all services are running:

- **Frontend UI**: http://localhost:5173
- **Backend API**: http://localhost:8080
- **API Health**: http://localhost:8080/actuator/health

### MCP Server Health Checks

```bash
curl http://localhost:8091/health  # Market MCP
curl http://localhost:8092/health  # News MCP
curl http://localhost:8093/health  # Weather MCP
curl http://localhost:8094/health  # System MCP
```

## Service Ports Summary

| Service | Port | URL |
|---------|------|-----|
| PostgreSQL | 5432 | localhost:5432 |
| Market MCP | 8091 | http://localhost:8091 |
| News MCP | 8092 | http://localhost:8092 |
| Weather MCP | 8093 | http://localhost:8093 |
| System MCP | 8094 | http://localhost:8094 |
| Backend API | 8080 | http://localhost:8080 |
| Frontend | 5173 | http://localhost:5173 |

## Troubleshooting

### Backend won't start - Database error

Make sure PostgreSQL is running:
```bash
docker-compose ps postgres
```

### Port already in use

Check what's using the port:
```bash
lsof -i :8080  # Replace with the port number
```

Kill the process:
```bash
kill -9 <PID>
```

### MCP server not responding

Check the logs (when using automated script):
```bash
tail -f /tmp/market-mcp.log
tail -f /tmp/news-mcp.log
tail -f /tmp/weather-mcp.log
tail -f /tmp/system-mcp.log
```

### Frontend can't connect to backend

Make sure the backend is running on port 8080. The frontend is configured to proxy `/api` requests to `http://localhost:8080`.

## Development Tips

### Rebuild Backend

```bash
cd backend
./gradlew clean build
```

### Rebuild MCP Server

```bash
cd mcp/market-mcp  # or any other MCP server
npm run build
```

### Hot Reload Frontend

The frontend uses Vite's hot module replacement - just save your changes and they'll reflect immediately!

### View Logs

When using the automated startup script:
```bash
# Backend logs
tail -f /tmp/backend.log

# Frontend logs
tail -f /tmp/frontend.log

# MCP server logs
tail -f /tmp/market-mcp.log
tail -f /tmp/news-mcp.log
tail -f /tmp/weather-mcp.log
tail -f /tmp/system-mcp.log
```

## Database Management

### Initialize Database Schema

The schema is automatically initialized on first run if `spring.sql.init.mode=always` is set.

### Connect to PostgreSQL

```bash
docker exec -it livecontext-postgres psql -U livecontext -d livecontext
```

### Reset Database

```bash
docker-compose down postgres
docker volume rm livecontext-ai_postgres-data
docker-compose up -d postgres
```

## Next Steps

- Access the UI at http://localhost:5173
- Try the Chat page to ask questions
- View the Dashboard for live updates
- Create alert rules in the Alerts page

For more details, see the main [README.md](README.md).
