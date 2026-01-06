# LiveContext-AI — Implementation Reference (CLAUDE)

This document reflects the **actual implementation** of `livecontext-ai` as of January 2026.

---

## PROJECT OVERVIEW

LiveContext-AI is a real-time intelligence agent that correlates:
- **Market data** (stock quotes via Stooq)
- **News** (RSS ingestion from Reddit, WSJ)
- **Weather** (forecasts via Open-Meteo)

The AI uses **Ollama LLM with tool-calling** to fetch evidence from MCP servers before answering questions.

---

## TECHNOLOGY STACK (ACTUAL)

### Backend
| Component | Technology |
|-----------|------------|
| Language | Java 25 (via Gradle toolchain) |
| Framework | Spring Boot 4 + WebFlux |
| Database | PostgreSQL + R2DBC (reactive) |
| LLM | Ollama (qwen2.5:7b model) |
| Logging | SLF4J + Logback (class-specific loggers) |
| Build | Gradle with Spotless (Google Java Format) |

### Frontend
| Component | Technology |
|-----------|------------|
| Runtime | Node.js + TypeScript |
| Framework | Vite + React |
| Real-time | Server-Sent Events (SSE) |

### MCP Servers (Node.js/TypeScript)
| Server | Port | Tools |
|--------|------|-------|
| market-mcp | 8091 | `get_quote`, `compute_indicators`, `detect_anomaly` |
| news-mcp | 8092 | `ingest_rss`, `search`, `analyze_sentiment` |
| weather-mcp | 8093 | `get_forecast`, `get_alerts` |
| system-mcp | 8094 | `alerts_create`, `db_query_readonly` |

---

## KEY ARCHITECTURE DECISIONS

### 1. LLM Tool-Calling (Agentic Flow)

The `LlmService` implements an agentic loop:

```
User Question → /api/chat endpoint
       ↓
LlmService → Ollama /api/chat with tools
       ↓
If LLM returns tool_calls:
  → Execute via McpClientService
  → Feed results back as tool role messages
  → Continue loop (max 5 iterations)
       ↓
Return final answer + evidence array
```

**File:** `backend/src/main/java/com/ai/livecontext/service/LlmService.java`

**Tools defined:**
- `get_quote` - Stock market quotes via Market MCP
- `search_news` - News search via News MCP  
- `get_weather` - Weather forecast via Weather MCP

### 2. Database Schema

All JSON columns use `TEXT` type instead of `JSONB` for R2DBC compatibility:

```sql
CREATE TABLE timeline_events (
    id BIGSERIAL PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    payload TEXT NOT NULL,      -- JSON stored as string
    sources TEXT,               -- JSON stored as string
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**File:** `backend/src/main/resources/schema.sql`

### 3. Logging Architecture

Using **class-specific SLF4J loggers** (not centralized AppLogger):

```java
private static final Logger logger = LoggerFactory.getLogger(LlmService.class);

logger.info("[llm_request_start] Starting LLM chat | model={} question={}", model, question);
```

**Log pattern** (application.yml):
```
%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p %pid [%t] [%X{correlationId}] %-40.40logger : %m%n
```

Logs show class name: `c.a.l.service.LlmService : [llm_request_start]...`

### 4. Configuration Format

Lists use **comma-separated strings** (not YAML lists) for `@Value` compatibility:

```yaml
livecontext:
  ingestion:
    market:
      symbols: AAPL.US,TSLA.US,GOOGL.US  # Comma-separated
    news:
      feeds: https://reddit.com/r/artificial/.rss,https://feeds.a.dj.com/rss/RSSMarketsMain.xml
```

---

## API ENDPOINTS

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/stream` | SSE stream of timeline events |
| POST | `/api/chat` | Ask question → LLM + MCP tools → answer + evidence |
| POST | `/api/alerts` | Create alert rule |
| GET | `/api/alerts` | List alert rules |
| GET | `/api/test/market/{symbol}` | Test market MCP tool |
| GET | `/api/test/news?query=` | Test news MCP tool |
| GET | `/api/test/weather` | Test weather MCP tool |
| GET | `/actuator/health` | Health check |

---

## RUNNING THE APPLICATION

### Local Development

```bash
# 1. Start PostgreSQL
docker run -d --name livecontext-postgres \
  -e POSTGRES_DB=livecontext \
  -e POSTGRES_USER=livecontext \
  -e POSTGRES_PASSWORD=livecontext \
  -p 5432:5432 postgres:15

# 2. Start MCP servers
cd mcp/market-mcp && npm install && npm run dev &
cd mcp/news-mcp && npm install && npm run dev &
cd mcp/weather-mcp && npm install && npm run dev &

# 3. Start Ollama with model
ollama pull qwen2.5:7b
ollama serve

# 4. Start backend
cd backend && ./gradlew bootRun

# 5. Start frontend
cd frontend && npm install && npm run dev
```

### Docker Compose

```bash
docker-compose up -d

# Pull Ollama model (first time only)
docker exec -it livecontext-ollama ollama pull qwen2.5:7b
```

---

## KEY FILES

### Backend
| File | Purpose |
|------|---------|
| `LlmService.java` | Agentic LLM loop with tool-calling |
| `McpClientService.java` | HTTP client for MCP tool calls |
| `IngestionService.java` | Scheduled data ingestion jobs |
| `LoggingFilter.java` | HTTP request/response logging |
| `application.yml` | Configuration (ports, URLs, cron) |

### MCP Servers
| File | Purpose |
|------|---------|
| `market-mcp/src/tools/getQuote.ts` | Fetch quotes from Stooq |
| `news-mcp/src/tools/ingestRss.ts` | Parse RSS feeds |
| `weather-mcp/src/tools/getForecast.ts` | Fetch from Open-Meteo |

---

## DIFFERENCES FROM CLAUDE.md

| CLAUDE.md Requirement | Actual Implementation |
|-----------------------|----------------------|
| JSON-structured logging (AppLogger) | SLF4J class-specific loggers |
| JSONB database columns | TEXT columns (R2DBC compatibility) |
| YAML list configuration | Comma-separated strings |
| Java 25 | Java 25 (Gradle toolchain) |
| Logging via central AppLogger | Each class has own Logger |
| Tool-calling undefined | Implemented via Ollama /api/chat |

---

## SCHEDULED JOBS

| Job | Cron | Description |
|-----|------|-------------|
| Market Ingestion | Every 1 min | Fetch quotes for AAPL.US, TSLA.US, GOOGL.US |
| News Ingestion | Every 5 min | Ingest RSS feeds |
| Weather Ingestion | Every 15 min | Fetch weather for configured locations |

---

## TROUBLESHOOTING

### MCP 400 Bad Request
- Check `body=` in logs shows proper JSON with parameters
- Verify comma-separated config in application.yml

### LLM not calling tools
- Ensure model supports tool-calling (qwen2.5:7b, llama3.1)
- Check `/api/chat` endpoint is used (not `/api/generate`)

### Database JSONB errors
- Use TEXT columns, not JSONB
- Store JSON as `.toString()` strings

### Empty ingestion (symbolCount=0)
- Use comma-separated strings in YAML
- Not YAML list format with `-` prefix
