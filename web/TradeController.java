package com.tradejournal.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tradejournal.model.Trade;
import com.tradejournal.model.RiskViolation;
import com.tradejournal.repository.TradeRepository;
import com.tradejournal.service.PortfolioAnalyticsService;
import com.tradejournal.service.RiskEvaluationService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * TradeController — REST bridge between the frontend dashboard and existing Java services.
 *
 * Mapped under /api to match app.js fetch calls:
 *   POST /api/trades    → validate → save → return analytics
 *   GET  /api/trades    → return full trade history
 *   GET  /api/analytics → return portfolio summary + strategy stats
 *
 * Requires Spring Boot with spring-boot-starter-web.
 * Add to pom.xml if not present:
 *   <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-web</artifactId>
 *   </dependency>
 *   <dependency>
 *     <groupId>org.springframework.boot</groupId>
 *     <artifactId>spring-boot-starter-validation</artifactId>
 *   </dependency>
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")   // Restrict to your domain in production
public class TradeController {

    private final RiskEvaluationService    riskService;
    private final PortfolioAnalyticsService analyticsService;
    private final TradeRepository          tradeRepo;

    // ─── Constructor injection (preferred over @Autowired on fields) ───
    public TradeController(RiskEvaluationService riskService,
                           PortfolioAnalyticsService analyticsService,
                           TradeRepository tradeRepo) {
        this.riskService       = riskService;
        this.analyticsService  = analyticsService;
        this.tradeRepo         = tradeRepo;
    }

    /* ══════════════════════════════════════════════════════════════════
       POST /api/trades
       Body: { symbol, tradeType, quantity, price, strategyId }

       Flow:
         1. Validate incoming JSON (Bean Validation)
         2. Call riskService.evaluateTrade()
            → violation found  → 403 with rule details
            → no violation     → save + return success + fresh analytics
    ══════════════════════════════════════════════════════════════════ */
    @PostMapping("/trades")
    public ResponseEntity<?> submitTrade(@Valid @RequestBody TradeRequest request) {

        // Build domain object
        Trade trade = new Trade();
        trade.setSymbol(request.symbol.trim().toUpperCase());
        trade.setTradeType(request.tradeType.trim().toUpperCase());
        trade.setQuantity(request.quantity);
        trade.setPrice(request.price);
        trade.setStrategyId(request.strategyId.trim());
        trade.setTimestamp(LocalDateTime.now());

        // ── Step 1: Risk evaluation ──────────────────────────────────
        RiskViolation violation = riskService.evaluateTrade(trade);
        // NOTE: Adapt to your RiskEvaluationService signature.
        //
        // If your service returns boolean:
        //   boolean passed = riskService.evaluateTrade(trade);
        //   if (!passed) { return buildRiskRejection("Concentration Limit", "Exceeds 20% portfolio weight"); }
        //
        // If it throws a custom exception:
        //   try { riskService.evaluateTrade(trade); }
        //   catch (RiskViolationException ex) { return buildRiskRejection(ex.getRule(), ex.getDetail()); }

        if (violation != null) {
            return buildRiskRejection(violation.getRule(), violation.getDetail());
        }

        // ── Step 2: Persist trade ────────────────────────────────────
        Trade saved = tradeRepo.saveTrade(trade);

        // ── Step 3: Refresh analytics and return combined response ───
        AnalyticsResponse analytics = buildAnalyticsResponse();

        TradeSuccessResponse body = new TradeSuccessResponse();
        body.message   = "Trade for " + saved.getSymbol() + " logged successfully.";
        body.trade     = saved;
        body.analytics = analytics;

        return ResponseEntity.ok(body);
    }

    /* ══════════════════════════════════════════════════════════════════
       GET /api/trades — Return full trade history
    ══════════════════════════════════════════════════════════════════ */
    @GetMapping("/trades")
    public ResponseEntity<List<Trade>> getAllTrades() {
        List<Trade> trades = tradeRepo.getAllTrades();
        return ResponseEntity.ok(trades);
    }

    /* ══════════════════════════════════════════════════════════════════
       GET /api/analytics — Portfolio summary + strategy breakdown
    ══════════════════════════════════════════════════════════════════ */
    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsResponse> getAnalytics() {
        return ResponseEntity.ok(buildAnalyticsResponse());
    }

    /* ══════════════════════════════════════════════════════════════════
       PRIVATE HELPERS
    ══════════════════════════════════════════════════════════════════ */

    /** Builds the risk-rejection 403 response body. */
    private ResponseEntity<RiskRejectionResponse> buildRiskRejection(String rule, String detail) {
        RiskRejectionResponse body = new RiskRejectionResponse();
        body.message = "Trade rejected: risk rule violation.";
        body.rule    = rule;
        body.detail  = detail;
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    /**
     * Calls PortfolioAnalyticsService and maps results to the AnalyticsResponse DTO.
     * Adapt the method names below to match your actual service API.
     */
    private AnalyticsResponse buildAnalyticsResponse() {
        AnalyticsResponse resp = new AnalyticsResponse();

        // ── Adapt these calls to your PortfolioAnalyticsService methods ──
        resp.totalPnl       = analyticsService.getTotalPnl();
        resp.winRate        = analyticsService.getWinRate();        // Expected: 0–100 (percent)
        resp.totalTrades    = analyticsService.getTotalTradeCount();
        resp.winningTrades  = analyticsService.getWinningTradeCount();
        resp.strategyStats  = analyticsService.getStrategyStats();  // List<StrategyStats>
        // ─────────────────────────────────────────────────────────────────

        return resp;
    }

    /* ══════════════════════════════════════════════════════════════════
       REQUEST / RESPONSE DTOs
       Keep these as inner static classes or move to separate files.
    ══════════════════════════════════════════════════════════════════ */

    /** Incoming trade payload from the frontend. */
    public static class TradeRequest {

        @NotBlank(message = "Symbol is required")
        @Size(max = 10, message = "Symbol too long")
        public String symbol;

        @NotBlank(message = "Trade type is required")
        @Pattern(regexp = "BUY|SELL", message = "Trade type must be BUY or SELL")
        public String tradeType;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be positive")
        public Double quantity;

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        public Double price;

        @NotBlank(message = "Strategy ID is required")
        @Size(max = 50, message = "Strategy ID too long")
        public String strategyId;
    }

    /** Returned on successful trade submission. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TradeSuccessResponse {
        public String            message;
        public Trade             trade;
        public AnalyticsResponse analytics;
    }

    /** Returned when a risk rule rejects the trade (HTTP 403). */
    public static class RiskRejectionResponse {
        public String message;
        public String rule;       // e.g. "CONCENTRATION_LIMIT"
        public String detail;     // human-readable explanation
    }

    /**
     * Portfolio analytics snapshot — mirrors what the frontend expects:
     * { totalPnl, winRate, totalTrades, winningTrades, strategyStats[] }
     *
     * StrategyStats should have: { strategyId, totalTrades, winRate, totalPnl }
     * Adapt the List type to match your PortfolioAnalyticsService return type.
     */
    public static class AnalyticsResponse {
        public double     totalPnl;
        public double     winRate;        // 0–100
        public int        totalTrades;
        public int        winningTrades;
        public List<?>    strategyStats;  // Replace <?> with your StrategyStats type
    }

    /* ══════════════════════════════════════════════════════════════════
       GLOBAL EXCEPTION HANDLER (add to a @ControllerAdvice class or keep inline)

       Catches Bean Validation errors and returns structured JSON
       so the frontend can display them consistently.
    ══════════════════════════════════════════════════════════════════ */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        String errorMsg = ex.getBindingResult()
                           .getFieldErrors()
                           .stream()
                           .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                           .findFirst()
                           .orElse("Validation failed");

        return ResponseEntity
            .badRequest()
            .body(Map.of("message", errorMsg));
    }

    /** Catch-all for unexpected errors — never leak stack traces to the client. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericError(Exception ex) {
        // Log the real error server-side:
        System.err.println("[TradeController] Unexpected error: " + ex.getMessage());
        ex.printStackTrace();

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("message", "An internal server error occurred. Please try again."));
    }
}