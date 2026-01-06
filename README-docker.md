# Docker Usage Guide for LiveContext-AI

This guide provides all necessary commands to run the full LiveContext-AI stack using Docker Compose.

## Quick Start

Start all services (Backend, Frontend, 4 MCP Servers, Postgres, Ollama):

```bash
docker compose up --build -d
```

## One-Time Setup (First Run Only)

After starting the containers for the first time, you must download the AI model:

```bash
docker exec -it livecontext-ollama ollama pull qwen2.5:1.5b
```

## Common Commands

### View Logs

**Stream all logs:**
```bash
docker compose logs -f
```

**Stream specific service logs:**
```bash
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f market-mcp
```

### Check Status

See running containers and their health status:
```bash
docker compose ps
```

### Stop Services

**Stop and remove containers:**
```bash
docker compose down
```

**Stop, remove containers, AND wipe data volumes (Fresh Start):**
```bash
docker compose down -v
```

## Troubleshooting

1.  **"Ollama not ready" / LLM issues**:
    *   Ensure you ran the `ollama pull` command above.
    *   Check Ollama logs: `docker compose logs -f ollama`

2.  **Port conflicts**:
    *   Example: `Bind for 0.0.0.0:8080 failed: port is already allocated`
    *   Solution: Stop the other service using that port or edit `docker-compose.yml` to map to a different host port (e.g., `"8081:8080"`).

3.  **Database connection errors**:
    *   The backend waits for Postgres to be healthy, but if it times out, restart the backend:
    *   `docker compose restart backend`

## Service Ports

| Service | Port | URL |
|---------|------|-----|
| Frontend | 5173 | http://localhost:5173 |
| Backend API | 8080 | http://localhost:8080 |
| Postgres | 5432 | localhost:5432 |
| Ollama | 11434 | http://localhost:11434 |
| Market MCP | 8091 | http://localhost:8091 |
| News MCP | 8092 | http://localhost:8092 |
| Weather MCP | 8093 | http://localhost:8093 |
| System MCP | 8094 | http://localhost:8094 |
