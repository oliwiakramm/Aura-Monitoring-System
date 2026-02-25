# Aura Monitoring System

A distributed JVM monitoring system consisting of 3 independent components communicating via HTTP/JSON. The system collects CPU and RAM metrics from running Java processes, verifies the availability of specified URLs, and displays everything in real time on a web dashboard.

---

## Components

### Aura Hub
The central REST API server built with Spring Boot 3. It receives reports from agents, stores them in an H2 database, and calculates each agent's status (`HEALTHY`, `CRITICAL`, `OFFLINE`) based on defined thresholds. Exposes a `GET /api/status` endpoint with the current state of all agents.

**Technologies:** Java 17, Spring Boot 3, Spring Data JPA, H2, JUnit 5, Mockito

**Status logic:**
- `HEALTHY` — CPU below 85%, RAM below 512MB, URL responds correctly
- `CRITICAL` — CPU above 85%, RAM above 512MB, or URL does not respond
- `OFFLINE` — no report received for more than 30 seconds

### Aura Agent
An autonomous metrics collector that every 10 seconds gathers CPU and RAM usage from the JVM, pings a specified URL, and sends a report to the Hub. Multiple instances can run simultaneously — each with a different `agentId`.

**Technologies:** Java 17, ScheduledExecutorService, java.net.http.HttpClient, Jackson

### Aura Dashboard
A real-time user interface. Every 5 seconds it queries the Hub and updates the view without reloading the page. Agents with `CRITICAL` status are highlighted with a pulsing animation. On mobile devices the table switches to a card layout.

**Technologies:** HTML, CSS, Vanilla JavaScript, nginx (in Docker)

---

## Architecture

```
┌─────────────────┐        POST /api/metrics        ┌─────────────────┐
│                 │ ──────────────────────────────► │                 │
│   Aura Agent    │     every 10 seconds            │    Aura Hub     │
│  (Java 17)      │                                 │  (Spring Boot)  │
│                 │                                 │                 │
└─────────────────┘                                 └────────┬────────┘
                                                             │
                                                  GET /api/status
                                                   every 5 seconds
                                                             │
                                                    ┌────────▼────────┐
                                                    │                 │
                                                    │ Aura Dashboard  │
                                                    │ (HTML/CSS/JS)   │
                                                    │                 │
                                                    └─────────────────┘
```

---

## Running with Docker

The easiest option — requires only [Docker Desktop](https://www.docker.com/products/docker-desktop) installed.

**1. Clone the repository**
```bash
git clone https://github.com/oliwiakramm/aura.git
cd aura-monitoring-system
```

**2. Start the entire system with one command**
```bash
docker compose up --build
```

The first run takes 3-5 minutes (downloading images, building JARs). Subsequent starts are instant.

**3. Open in your browser**

| Address | Description |
|---------|-------------|
| http://localhost | Dashboard |
| http://localhost:8080/api/status | Hub API — raw JSON data |

**Stop the system**
```bash
docker compose down
```

---

## Running locally (without Docker)

Requirements: Java 17+, Maven 3.8+

### Step 1 — Start Aura Hub

```bash
cd AuraHub
mvn spring-boot:run
```

Verify it works:
```bash
curl http://localhost:8080/api/status
# Should return: []
```

### Step 2 — Start Aura Agent

```bash
cd AuraAgent

# Build the JAR (only once)
mvn package -DskipTests

# Run the agent
java -jar target/aura-agent-1.0.0.jar --agentId=agent-local
```

Want to run more agents? Open a new terminal window:
```bash
java -jar AuraAgent/target/aura-agent-1.0.0.jar --agentId=agent-second
```

### Step 3 — Open the Dashboard

1. Open the `AuraDashboard` folder in VS Code
2. Right-click on `index.html` → **Open with Live Server**
3. The Dashboard will open automatically at `http://localhost:5500`

> **Note:** The Dashboard cannot be opened directly via `file://` — it must be served by a local HTTP server (Live Server handles this automatically).

> **Important:** before starting the Agent, make sure `config.json` has `"hubUrl": "http://localhost:8080/api/metrics"`. In Docker this is set to `aura-hub:8080` — these two settings differ.

---

## Tests

```bash
# Unit tests for Hub 
cd AuraHub
mvn test

