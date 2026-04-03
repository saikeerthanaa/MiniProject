# TradeJournal — Architecture & Integration Guide

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     WEB BROWSER (Client)                        │
│                  ◈ TradeJournal Dashboard ◈                     │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  • Sidebar Navigation (Dashboard, Trade, Analytics)     │   │
│  │  • KPI Cards (P&L, Win Rate, Trade Count)              │   │
│  │  • Trade Entry Form with Risk Preview                  │   │
│  │  • Strategy Performance Table                          │   │
│  │  • Trade History Log with Search/Filter                │   │
│  │  • Real-time Status Indicator                          │   │
│  └──────────────────────────────────────────────────────────┘   │
└────────────┬──────────────────────────────────────────────────┬─┘
             │ HTTP/JSON (REST API Calls)                      │
             │                                                   │
    ┌────────▼─────────────────────────────────────────────┬────▼──┐
    │          Spring Boot 2.7.14 Server (Tomcat)         │       │
    │               http://localhost:8080                  │       │
    │                                                      │       │
    │  ┌────────────────────────────────────────────────┐ │       │
    │  │  TradeController (@RestController)             │ │       │
    │  │  ├─ POST /api/trades (trade submission)        │ │       │
    │  │  ├─ GET /api/trades (trade history)            │ │       │
    │  │  ├─ GET /api/analytics (portfolio summary)     │ │       │
    │  │  ├─ POST /api/trades/validate (dry-run)        │ │       │
    │  │  └─ GET /api/health (status check)             │ │       │
    │  └────────────┬─────────────────────────────────────┘ │       │
    │               │                                        │       │
    │  ┌────────────▼─────────────────────────────────────┐ │       │
    │  │  AnalyticsWrapper (@Service)                    │ │       │
    │  │  ├─ Wraps PortfolioAnalyticsService             │ │       │
    │  │  ├─ Converts BigDecimal → Double                │ │       │
    │  │  └─ Builds AnalyticsResponse DTOs               │ │       │
    │  └────────────┬─────────────────────────────────────┘ │       │
    │               │                                        │       │
    │  ┌────────────▼─────────────────────────────────────┐ │       │
    │  │  DTOs (@JsonProperty, @JsonInclude)             │ │       │
    │  │  ├─ TradeRequest (validation @NotBlank)         │ │       │
    │  │  ├─ TradeResponse (JSON serialization)          │ │       │
    │  │  ├─ AnalyticsResponse (portfolio metrics)       │ │       │
    │  │  └─ ErrorResponse (violation details)           │ │       │
    │  └──────────────────────────────────────────────────┘ │       │
    │                                                      │       │
    └──────────────┬────────────────────────────────────────┴────┬──┘
                   │ (Delegate to Legacy Services)                │
      ┌────────────▼──────────────────────────────────────────┬──▼─┐
      │  com.trade.service (Existing Business Logic)          │    │
      │                                                       │    │
      │  ┌──────────────────────────────┐                    │    │
      │  │ RiskEvaluationService        │                    │    │
      │  │ @Service (Spring Managed)    │                    │    │
      │  │                              │                    │    │
      │  │ Evaluates trades against:    │                    │    │
      │  │ ├─ ConcentrationRule         │                    │    │
      │  │ │  (10% max position)        │                    │    │
      │  │ ├─ PositionSizeRule          │                    │    │
      │  │ │  (1-10,000 shares)         │                    │    │
      │  │ └─ Custom rules (pluggable)  │                    │    │
      │  │                              │                    │    │
      │  │ Returns: isApproved (bool)   │                    │    │
      │  │ Logs: RiskFlag violations    │                    │    │
      │  └────────┬─────────────────────┘                    │    │
      │           │                                          │    │
      │  ┌────────▼──────────────────────┐                   │    │
      │  │ PortfolioAnalyticsService     │                   │    │
      │  │ @Service (Spring Managed)     │                   │    │
      │  │                               │                   │    │
      │  │ Calculates metrics:           │                   │    │
      │  │ ├─ PnL = Σ(SELL) - Σ(BUY)   │                   │    │
      │  │ ├─ Win Rate (%)               │                   │    │
      │  │ ├─ Strategy Stats             │                   │    │
      │  │ └─ Performance Summary        │                   │    │
      │  │                               │                   │    │
      │  │ Returns: BigDecimal values    │                   │    │
      │  │ Uses: TradeRepository         │                   │    │
      │  └────────┬──────────────────────┘                   │    │
      │           │                                          │    │
      │  ┌────────▼──────────────────────┐                   │    │
      │  │ TradeRepository               │                   │    │
      │  │ @Repository (Spring Managed)  │                   │    │
      │  │                               │                   │    │
      │  │ JDBC Data Access:             │                   │    │
      │  │ ├─ saveTrade() → INSERT       │                   │    │
      │  │ ├─ getAllTrades() → SELECT    │                   │    │
      │  │ └─ getTradesByPortfolio()     │                   │    │
      │  │                               │                   │    │
      │  │ Connection Pool: HikariCP     │                   │    │
      │  └────────┬──────────────────────┘                   │    │
      └──────────┬────────────────────────────────────────────┴──┬─┘
                 │ (Direct JDBC)                                │
    ┌────────────▼────────────────────────────────────────────┬─▼─┐
    │       MySQL 8.0 Database (localhost:3306)               │   │
    │                  TradeJournal                            │   │
    │                                                         │   │
    │  Tables:                                               │   │
    │  ┌──────────────────────────────────────────────┐      │   │
    │  │ User (id, name)                              │      │   │
    │  │ Strategy (id, name, description)             │      │   │
    │  │ Portfolio (id, user_id, total_capital, ...)  │      │   │
    │  │ Trade (id, portfolio_id, symbol, type, qty)  │      │   │
    │  │ RiskFlag (rule_name, violation_details)      │      │   │
    │  │ PerformanceSummary (total_pnl, win_rate)     │      │   │
    │  └──────────────────────────────────────────────┘      │   │
    │                                                         │   │
    │  Indexes: Portfolio(user_id), Trade(portfolio_id)      │   │
    │  Storage: InnoDB, UTF8MB4 Character Set               │   │
    └─────────────────────────────────────────────────────────┴───┘
```

---

## Data Flow: Trade Submission

```
1. USER SUBMITS TRADE FORM
   ↓
   {symbol: "AAPL", tradeType: "BUY", quantity: 100, price: 150.25, strategyId: "1"}
   ↓
2. FRONTEND VALIDATION (JavaScript/HTML5)
   ├─ Symbol: not empty ✓
   ├─ Trade Type: BUY|SELL ✓
   ├─ Quantity: positive number ✓
   ├─ Price: positive decimal ✓
   └─ Strategy ID: not empty ✓
   ↓ (if invalid, show error alert)
   ↓
3. API REQUEST: POST /api/trades
   ├─ Headers: Content-Type: application/json
   ├─ Body: JSON with all fields
   └─ CORS: * (allowed for dev)
   ↓
4. TradeController.submitTrade()
   ├─ @Valid validates @NotBlank, @Positive, @Pattern
   ├─ Creates Trade object
   └─ Calls riskService.evaluateTrade()
   ↓
5. RiskEvaluationService.evaluateTrade()
   ├─ Rule 1: ConcentrationRule
   │  ├─ Gets current portfolio balance from DB
   │  ├─ Checks: (qty × price) ≤ 10% of balance
   │  ├─ If FAIL → Log RiskFlag, return false
   │  └─ If PASS → continue
   │
   ├─ Rule 2: PositionSizeRule
   │  ├─ Checks: 1 ≤ qty ≤ 10,000
   │  ├─ If FAIL → Log RiskFlag, return false
   │  └─ If PASS → continue
   │
   └─ If all rules PASS → return true
   ↓
6a. IF REJECTED (false)
    ├─ HTTP 403 Forbidden
    ├─ Response: ErrorResponse(code, message)
    └─ Frontend: Show risk alert "Concentration limit exceeded"
   ↓
6b. IF APPROVED (true)
    ├─ tradeRepository.saveTrade()
    │  ├─ INSERT INTO Trade VALUES (...)
    │  ├─ Get generated ID from database
    │  └─ Return saved Trade object
    │
    ├─ analyticsService.buildAnalyticsResponse()
    │  ├─ calculatePnL(portfolioId) → BigDecimal
    │  ├─ calculateWinRate(...) → Double
    │  ├─ getStrategyStats() → List<StrategyStatsDto>
    │  └─ Build AnalyticsResponse DTO
    │
    ├─ HTTP 200 OK
    ├─ Response: SuccessResponse(message, trade, analytics)
    └─ Frontend: Show success alert + update dashboard KPIs
   ↓
7. FRONTEND UPDATES
   ├─ Close trade form
   ├─ Update P&L card color (green/red)
   ├─ Update Win Rate percentage
   ├─ Re-render strategy table
   └─ Auto-refresh trade history
```

---

## File Manifest

### Spring Boot Configuration
- `pom.xml` — Maven dependency management
  - spring-boot-starter-web (REST API)
  - spring-boot-starter-data-jpa (ORM compatibility)
  - spring-boot-starter-validation (JSR 380)
  - mysql-connector-java 8.0.33
  - lombok (optional)

- `src/main/resources/application.properties` — Server & database config
  - `server.port=8080`
  - `spring.datasource.url=jdbc:mysql://localhost:3306/TradeJournal`
  - `spring.datasource.username=root`
  - `spring.datasource.password=redBlue3011!`
  - `spring.jpa.hibernate.ddl-auto=validate`
  - Logging configuration

### Application Layer (com.tradejournal.*)
- `TradeJournalApplication.java` — @SpringBootApplication entry point
  - Component scanning: "com.tradejournal", "com.trade"
  - Startup banner with ASCII art

- `controller/TradeController.java` — REST API endpoints (250 lines)
  - POST /api/trades → trade submission + risk eval
  - GET /api/trades → retrieve trade history
  - GET /api/analytics → portfolio summary
  - POST /api/trades/validate → dry-run risk check
  - GET /api/health → status endpoint

- `service/AnalyticsWrapper.java` — Bridge to legacy services (100 lines)
  - Converts PortfolioAnalyticsService to Spring bean
  - Wraps BigDecimal → Double conversions
  - Builds response DTOs

- `dto/DTOs.java` — Request/Response classes (200 lines)
  - TradeRequest (with @Valid, @NotBlank, @Positive)
  - TradeResponse
  - AnalyticsResponse
  - StrategyStatsDto
  - ErrorResponse
  - SuccessResponse
  - HealthResponse

- `config/WebConfig.java` — Static resource serving
  - Maps "/" → classpath:/static/index.html
  - Configures Spring MVC resource handlers

### Domain Layer (com.trade.*)
- `model/Trade.java` — Trade entity
  - Fields: id, symbol, type, quantity, price, strategyId
  - Getters/Setters

- `model/RiskRule.java` — Interface for risk validation
  - Method: validate(Trade, currentCapital) → boolean

- `model/ConcentrationRule.java` — 10% position limit
  - Prevents portfolio concentration > 10%
  - Uses BigDecimal for precision

- `model/PositionSizeRule.java` — Share quantity limits
  - Min: 1 share, Max: 10,000 shares

- `repository/TradeRepository.java` — JDBC data access
  - saveTrade() → INSERT with ID generation
  - getAllTrades() → SELECT * with 100 row limit
  - getTradesByPortfolio() → SELECT with filter
  - Uses PreparedStatement to prevent SQL injection

- `service/RiskEvaluationService.java` — Risk rule orchestration (140 lines)
  - @Service for Spring autowiring
  - evaluateTrade(Trade, portfolioId) → boolean
  - addRule(RiskRule) → pluggable architecture
  - Queries Portfolio.total_capital from DB
  - Logs violations to RiskFlag table

- `service/PortfolioAnalyticsService.java` — Performance calculations (180 lines)
  - @Service for Spring autowiring
  - calculatePnL(portfolioId) → BigDecimal
  - calculateWinRate(strategyId, portfolioId) → BigDecimal (0-100)
  - getStrategyStatistics(portfolioId) → Map<Integer, BigDecimal>
  - updatePerformanceSummary() → persists metrics
  - Uses try-with-resources for DB connection safety

### Frontend (src/main/resources/static/)
- `index.html` — Dashboard UI (296 lines)
  - Sidebar navigation (4 sections)
  - Header with refresh button
  - KPI cards (P&L, Win Rate, Trades, Strategies)
  - Trade entry form with previews
  - Strategy performance table
  - Analytics details section
  - Trade history with search

- `app.js` — Frontend application logic (394 lines)
  - Navigation & section switching
  - Real-time clock display
  - Trade value preview calculations
  - Form submission with validation
  - API integration (fetch)
  - Analytics rendering
  - Trade history filtering
  - Error/success alerts
  - Connection status indicator

- `style.css` — Responsive dark theme (772 lines)
  - Palette: Dark base (#0a0b0f) with gold accent (#e8c44a)
  - Fonts: Syne (display) + JetBrains Mono (data)
  - Animations: pulse-icon, blink, slide-down, spin
  - Responsive breakpoints: 1100px, 768px
  - CSS Grid for layout
  - Dark mode color transitions

### Documentation
- `BUILD_AND_RUN.md` — Comprehensive setup guide
  - Prerequisites installation
  - Project structure explanation
  - Build commands
  - Database setup SQL
  - API endpoint reference
  - Testing procedures
  - Troubleshooting guide

- `run-build.bat` — Quick build script (Windows)
  - Prerequisite checking
  - Maven build/run automation

- `README.md` — Original comprehensive documentation
  - Architecture diagrams
  - Component descriptions
  - Risk rules explanation

---

## Technology Stack

| Layer | Technology | Version | Purpose |
|-------|-----------|---------|---------|
| **Runtime** | Java | 11+ | JVM execution |
| **Framework** | Spring Boot | 2.7.14 | REST API, DI, auto-config |
| **Web** | Spring Web | 2.7.14 | @RestController, @RequestMapping |
| **Data** | Spring Data JPA | 2.7.14 | ORM compatibility |
| **Validation** | JSR 380 | Latest | @Valid, @NotBlank, @Positive |
| **Database** | MySQL | 8.0 | Persistent data storage |
| **JDBC Driver** | MySQL Connector | 8.0.33 | Database connectivity |
| **Build** | Maven | 3.6+ | Dependency & project management |
| **Frontend** | HTML5 | Latest | Semantic markup |
| **Frontend** | ES6 JavaScript | Latest | Form handling, API calls |
| **Frontend** | CSS3 | Latest | Dark theme, responsive design |
| **Fonts** | Google Fonts | Latest | Syne, JetBrains Mono |

---

## Key Design Decisions

### 1. **Pluggable Risk Rules**
- RiskRule interface allows adding new rules without modifying RiskEvaluationService
- Currently: ConcentrationRule + PositionSizeRule
- Easy to extend: Create class implementing RiskRule, register with addRule()

### 2. **BigDecimal for Finance**
- Prevents floating-point precision errors
- Essential for accurate PnL calculations
- Conversions to Double only at DTO serialization layer

### 3. **Dual Service Design**
- Legacy backend (com.trade.*) keeps existing logic untouched
- Spring layer (com.tradejournal.*) wraps with REST API
- Minor annotations (@Service, @Repository) enable Spring management
- Preserves monolithic JDBC while supporting future JPA migration

### 4. **DTO Pattern for API**
- Decouples REST API contract from domain models
- Provides validation (@Valid) at entry point
- Handles JSON serialization with @JsonProperty
- Excludes null fields with @JsonInclude(NON_NULL)

### 5. **WebConfig for Static Resources**
- Serves frontend files from classpath:/static/
- Maps root "/" to index.html
- Enables single-server deployment (no separate frontend server needed)

### 6. **CORS Enabled for Development**
- @CrossOrigin(origins = "*") allows frontend development on different ports
- Can be restricted in production to specific domain

---

## Security Considerations (Current State)

⚠️ **Current Implementation is NOT production-ready:**

### Existing Vulnerabilities:
1. **No Authentication** — Anyone can call /api/trades
2. **No Authorization** — No user isolation, portfolio hardcoded to ID=1
3. **Hardcoded Credentials** — DB password in plain text in properties file
4. **CORS Open** — Accepts requests from any origin
5. **No HTTPS** — Data sent unencrypted

### Recommended Improvements:
1. Add Spring Security with JWT tokens
2. Store DB password in environment variables or vault
3. Restrict CORS to specific origins
4. Implement user-based portfolio isolation
5. Enable HTTPS (use self-signed certs for dev)
6. Add rate limiting on API endpoints
7. Validate & sanitize all inputs
8. Add audit logging for trades
9. Implement soft-deletes (never actually delete trades)
10. Add transaction support (TX rollback on failures)

---

## Performance Considerations

### Current Optimizations:
- PreparedStatement prevents SQL injection
- Connection pooling via HikariCP (Spring default)
- Try-with-resources ensures connection cleanup
- Limited query results (100 trades max in getAllTrades)
- Indexes on foreign keys (implicit in MySQL)

### Further Optimizations:
- Add caching layer (Redis) for analytics (recalculated on every request)
- Denormalize PerformanceSummary table (pre-computed metrics)
- Add database indexes on frequently filtered columns (stock_symbol, strategy_id)
- Implement pagination for trade history (currently returns all)
- Use Spring Data JPA query methods instead of manual JDBC
- Add database connection monitoring

---

## Deployment Guide (Future)

### Option 1: Docker Container
```dockerfile
FROM maven:3.8-openjdk-11
COPY . /app
WORKDIR /app
RUN mvn clean package -DskipTests
EXPOSE 8080
CMD ["java", "-jar", "target/tradejournal.jar"]
```

### Option 2: Direct JVM
```bash
mvn clean package -DskipTests
java -jar target/tradejournal-1.0.0.jar
```

### Option 3: Application Server (Tomcat)
```bash
mvn clean package -DskipTests -Dpackaging=war
```

---

## Testing Checklist

- [ ] Maven builds successfully
- [ ] Spring Boot starts on http://localhost:8080
- [ ] Dashboard loads (index.html served)
- [ ] Form validation works (empty field shows error)
- [ ] Trade submission rejected for >10% concentration
- [ ] Trade submission accepted for valid amounts
- [ ] Success alert shows after approved trade
- [ ] Analytics update after trade
- [ ] Trade history logs new trades
- [ ] P&L displays correctly (green for profit, red for loss)
- [ ] Win rate calculation works
- [ ] Strategy stats breakdown is accurate
- [ ] Refresh button updates data
- [ ] Navigation between sections works
- [ ] API endpoints accessible via curl

---

## Conclusion

The TradeJournal system successfully integrates:
- **Clean Architecture**: Separated concerns (models, services, repositories)
- **Spring Boot Integration**: REST API with proper annotations
- **Business Logic**: Risk evaluation + portfolio analytics
- **Modern Frontend**: Responsive dashboard with real-time updates
- **Database Persistence**: MySQL with proper schema
- **Extensible Design**: Pluggable risk rules

The system is **ready for development and testing** with clear paths for production hardening.
