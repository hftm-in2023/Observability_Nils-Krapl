# Observability Nils Krapl

Kleines Beispielprojekt für **Event-driven Communication** mit **Quarkus**, **Kafka/Redpanda** und einem kleinen **Observability-Stack** mit **Prometheus** und **Grafana**.

---

## Architektur

- **blogBackend** (REST + MySQL)  
  erstellt Blogposts und publiziert nach dem Speichern eine Validierungsanfrage an Kafka  

- **text-validation-service**  
  konsumiert diese Anfrage, prüft den Text anhand einer Blocklist und publiziert das Ergebnis zurück  

- **blogBackend**  
  konsumiert die Antwort und setzt den Status des Blogposts entsprechend  

Zusätzlich:
- Metriken
- Health Checks
- Monitoring

---

## Kafka Topics

- `blog-validation-request` – Validierungsanfragen (JSON String)  
- `blog-validation-response` – Validierungsergebnisse (JSON String)  

---

## Services & Ports (docker-compose)

| Service | Zweck | Port (Host → Container) |
|--------|------|--------------------------|
| MySQL | Persistenz für blogBackend | `3306 → 3306` |
| Redpanda | Kafka-compatible Broker | `9092 → 9092` |
| Redpanda Console | Kafka UI | `8088 → 8080` |
| blogBackend | REST API | `8080 → 8080` |
| text-validation-service | Validator REST / Health / Metrics | `8081 → 8081` |
| Prometheus | Sammeln der Metrics | `9090 → 9090` |
| Grafana | Visualisierung der Metrics | `3000 → 3000` |
| Jaeger | Distributed Tracing (OpenTelemetry Collector + UI) | `16686, 4317, 4318` |

---

## Quickstart (alles via Docker)

```bash
docker compose up -d
```

Danach sind die Komponenten unter folgenden URLs erreichbar:

- REST API blogBackend: http://localhost:8080  
- text-validation-service: http://localhost:8081  
- Redpanda Console: http://localhost:8088  
- Prometheus: http://localhost:9090  
- Grafana: http://localhost:3000  
- Jaeger: http://localhost:16686

---

## Beispiel-Flow

### 1) Blogpost erstellen (Status: PENDING)

```bash
curl -s -X POST http://localhost:8080/blogs \
  -H 'Content-Type: application/json' \
  -d '{"title":"Hallo","content":"Das ist sauberer Content."}'
```

### 2) Blogpost abfragen

```bash
curl -s http://localhost:8080/blogs/1
```

### 3) Alle freigegebenen Blogs listen

```bash
curl -s http://localhost:8080/blogs
```

---

## Weitere nützliche Curl-Requests

### APPROVED

```bash
curl -s -X POST http://localhost:8080/blogs \
  -H 'Content-Type: application/json' \
  -d '{"title":"Freigegebener Post","content":"Dies ist ein unkritischer Text ohne verbotene Begriffe."}'
```

### REJECTED

```bash
curl -s -X POST http://localhost:8080/blogs \
  -H 'Content-Type: application/json' \
  -d '{"title":"Abgelehnter Post","content":"Dieser Inhalt enthält ein verbotenes Wort. (scam)"}'
```

---

## Health Checks

```bash
curl -s http://localhost:8080/q/health
curl -s http://localhost:8081/q/health
```

---

## Metrics

```bash
curl -s http://localhost:8080/q/metrics
curl -s http://localhost:8081/q/metrics
```

---

## Grafana

### Zugriff
http://localhost:3000  

### Standard Login
* Benutzer: admin
* Passwort: admin

### Typische Inhalte des Dashboards
* Request Count
* HTTP Request Duration
* Validation Requests
* Validation Results (APPROVED / REJECTED)
* Error Rate
* Service Health / Verfügbarkeit

Im Dashboard kannst du beobachten, wie sich Requests und Validierungen verändern, während du die Curl-Requests ausführst.

---

## Jaeger

### Zugriff
http://localhost:16686

### Was wird getraced?
* Beim Ausführen eines Requests (z. B. POST /blogs) entsteht ein Trace mit mehreren Spans:
* HTTP Request im blogbackend
* Kafka Publish (blog-validation-request)
* Verarbeitung im validator
* Kafka Response (blog-validation-response)
* Update des Blog-Status

---

## Beobachtung des Event-Flows

### Redpanda Console
http://localhost:8088  

### Dort kannst du live prüfen:
* ob Nachrichten im Topic blog-validation-request ankommen
* ob Antworten im Topic blog-validation-response veröffentlicht werden
* ob der Validator korrekt konsumiert und produziert

---

## Troubleshooting

### Keine Messages in Kafka
* Redpanda Console prüfen
* Existieren die Topics?

### Status bleibt PENDING
* Läuft der Validator?
* Ist die Kafka-Verbindung korrekt?

### Grafana zeigt keine Daten
* Läuft Prometheus?
* Ist Prometheus als Data Source konfiguriert?


### Logs prüfen
```bash
docker compose logs blogbackend
docker compose logs text-validation-service
docker compose logs prometheus
docker compose logs grafana
```
