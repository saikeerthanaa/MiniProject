package com.tradejournal.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.*;

import com.trade.model.Trade;
import com.trade.repository.TradeRepository;
import com.trade.service.RiskEvaluationService;
import com.tradejournal.dto.*;
import com.tradejournal.service.AnalyticsWrapper;

/**
 * TradeController — REST API for the TradeJournal application
 *
 * Endpoints:
 *   POST   /api/trades              → Submit new trade (with risk evaluation)
 *   GET    /api/trades              → Get all trades
 *   GET    /api/analytics           → Get portfolio analytics
 *   GET    /api/health              → Health check
 *   POST   /api/trades/validate     → Dry-run risk check (no save)
 *
 * All responses include proper HTTP status codes and error handling.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TradeController {

    @Autowired
    private RiskEvaluationService riskService;

    @Autowired
    private AnalyticsWrapper analyticsService;

    @Autowired(required = false)
    private TradeRepository tradeRepository;

    private final int PORTFOLIO_ID = 1;

    /* ══════════════════════════════════════════════════════════════════
       POST /api/trades — Submit and validate a trade
    ══════════════════════════════════════════════════════════════════ */
    @PostMapping("/trades")
    public ResponseEntity<?> submitTrade(@Valid @RequestBody TradeRequest request) {
        try {
            // Build Trade object
            Trade trade = new Trade(
                request.symbol.trim().toUpperCase(),
                request.tradeType.trim().toUpperCase(),
                request.quantity.intValue(),
                request.price,
                Integer.parseInt(request.strategyId)
            );

            // Step 1: Risk Evaluation
            boolean isApproved = riskService.evaluateTrade(trade, PORTFOLIO_ID);

            if (!isApproved) {
                return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(
                        "Trade rejected due to risk rule violation",
                        "RISK_VIOLATION",
                        "See RiskFlag table for details",
                        "Review portfolio concentration and position size limits"
                    ));
            }

            // Step 2: Save Trade
            Trade saved = tradeRepository != null ? tradeRepository.saveTrade(trade, PORTFOLIO_ID) : trade;

            // Step 3: Build Response
            TradeResponse tradeResp = mapToTradeResponse(saved);
            AnalyticsResponse analyticsResp = analyticsService.buildAnalyticsResponse();

            SuccessResponse response = new SuccessResponse(
                "Trade for " + saved.getSymbol() + " approved and logged successfully!",
                tradeResp,
                analyticsResp
            );

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("Invalid strategy ID format", "INVALID_FORMAT"));
        } catch (Exception e) {
            System.err.println("[TradeController] Error submitting trade: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
        }
    }

    /* ══════════════════════════════════════════════════════════════════
       GET /api/trades — Get all trades
    ══════════════════════════════════════════════════════════════════ */
    @GetMapping("/trades")
    public ResponseEntity<?> getAllTrades() {
        try {
            if (tradeRepository == null) {
                return ResponseEntity.ok(List.of());
            }

            List<Trade> trades = tradeRepository.getAllTrades();
            List<TradeResponse> responses = trades.stream()
                .map(this::mapToTradeResponse)
                .toList();

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            System.err.println("[TradeController] Error fetching trades: " + e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error fetching trades", "FETCH_ERROR"));
        }
    }

    /* ══════════════════════════════════════════════════════════════════
       GET /api/analytics — Get portfolio analytics
    ══════════════════════════════════════════════════════════════════ */
    @GetMapping("/analytics")
    public ResponseEntity<?> getAnalytics() {
        try {
            AnalyticsResponse response = analyticsService.buildAnalyticsResponse();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("[TradeController] Error fetching analytics: " + e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error fetching analytics", "ANALYTICS_ERROR"));
        }
    }

    /* ══════════════════════════════════════════════════════════════════
       POST /api/trades/validate — Validate trade without saving
    ══════════════════════════════════════════════════════════════════ */
    @PostMapping("/trades/validate")
    public ResponseEntity<?> validateTrade(@Valid @RequestBody TradeRequest request) {
        try {
            Trade trade = new Trade(
                request.symbol.trim().toUpperCase(),
                request.tradeType.trim().toUpperCase(),
                request.quantity.intValue(),
                request.price,
                Integer.parseInt(request.strategyId)
            );

            boolean isApproved = riskService.evaluateTrade(trade, PORTFOLIO_ID);

            Map<String, Object> response = Map.of(
                "symbol", trade.getSymbol(),
                "approved", isApproved,
                "tradeValue", trade.getQuantity() * trade.getPrice(),
                "message", isApproved ? "Trade would be approved" : "Trade would be rejected"
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity
                .badRequest()
                .body(new ErrorResponse("Validation failed", "VALIDATION_ERROR"));
        }
    }

    /* ══════════════════════════════════════════════════════════════════
       GET /api/health — Health check
    ══════════════════════════════════════════════════════════════════ */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(new HealthResponse());
    }

    /* ══════════════════════════════════════════════════════════════════
       HELPER METHODS
    ══════════════════════════════════════════════════════════════════ */

    /**
     * Map Trade domain object to TradeResponse DTO
     */
    private TradeResponse mapToTradeResponse(Trade trade) {
        TradeResponse resp = new TradeResponse();
        resp.symbol = trade.getSymbol();
        resp.tradeType = trade.getType();
        resp.quantity = (double) trade.getQuantity();
        resp.price = trade.getPrice();
        resp.tradeValue = resp.quantity * resp.price;
        resp.strategyId = String.valueOf(trade.getStrategyId());
        resp.tradeDate = LocalDateTime.now();
        return resp;
    }

    /* ══════════════════════════════════════════════════════════════════
       GLOBAL EXCEPTION HANDLERS
    ══════════════════════════════════════════════════════════════════ */

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationException(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        String errorMsg = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .findFirst()
            .orElse("Validation failed");

        return ResponseEntity
            .badRequest()
            .body(new ErrorResponse(errorMsg, "VALIDATION_ERROR"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        System.err.println("[TradeController] Unexpected error: " + ex.getMessage());
        ex.printStackTrace();

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("Internal server error", "INTERNAL_ERROR"));
    }
}
