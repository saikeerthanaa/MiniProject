package com.tradejournal.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trade.service.PortfolioAnalyticsService;
import com.tradejournal.dto.AnalyticsResponse;
import com.tradejournal.dto.StrategyStatsDto;

/**
 * AnalyticsWrapper — Adapts the existing PortfolioAnalyticsService
 * to provide Spring-friendly methods that the TradeController expects.
 *
 * This service bridges the gap between:
 * - Existing backend: PortfolioAnalyticsService (calculates metrics)
 * - Spring Controller: Needs specific getter methods
 * - Frontend: Expects specific JSON structure
 */
@Service
public class AnalyticsWrapper {

    @Autowired(required = false)
    private PortfolioAnalyticsService analyticsService;

    private final int PORTFOLIO_ID = 1;  // Default portfolio (can be made configurable)

    /**
     * Get total P&L for the portfolio
     * @return Total PnL as double (negative = loss, positive = gain)
     */
    public Double getTotalPnl() {
        if (analyticsService == null) return 0.0;
        try {
            BigDecimal pnl = analyticsService.calculatePnL(PORTFOLIO_ID);
            return pnl != null ? pnl.doubleValue() : 0.0;
        } catch (Exception e) {
            System.err.println("Error calculating PnL: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Get win rate as a percentage (0-100)
     * @return Win rate percentage (e.g., 66.67 for 66.67%)
     */
    public Double getWinRate() {
        if (analyticsService == null) return 0.0;
        try {
            // Calculate win rate for default strategy (ID = 1)
            BigDecimal winRate = analyticsService.calculateWinRate(1, PORTFOLIO_ID);
            return winRate != null ? winRate.doubleValue() : 0.0;
        } catch (Exception e) {
            System.err.println("Error calculating win rate: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Get total number of trades
     * @return Count of all trades
     */
    public Integer getTotalTradeCount() {
        if (analyticsService == null) return 0;
        // For now, return a placeholder - integrate with your Trade repository
        // to get actual count from database
        return 0;  // TODO: Inject TradeRepository and call getAllTrades().size()
    }

    /**
     * Get number of winning trades
     * @return Count of profitable trades
     */
    public Integer getWinningTradeCount() {
        if (analyticsService == null) return 0;
        try {
            Double winRate = getWinRate();
            Integer totalTrades = getTotalTradeCount();
            if (totalTrades == null || totalTrades == 0) return 0;
            return (int) (totalTrades * winRate / 100.0);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get strategy-wise performance statistics
     * @return List of StrategyStatsDto with metrics per strategy
     */
    public List<StrategyStatsDto> getStrategyStats() {
        List<StrategyStatsDto> stats = new ArrayList<>();

        if (analyticsService == null) return stats;

        try {
            Map<Integer, BigDecimal> strategyPnL = analyticsService.getStrategyStatistics(PORTFOLIO_ID);

            if (strategyPnL == null || strategyPnL.isEmpty()) {
                return stats;
            }

            for (Map.Entry<Integer, BigDecimal> entry : strategyPnL.entrySet()) {
                Integer strategyId = entry.getKey();
                BigDecimal pnl = entry.getValue();

                StrategyStatsDto dto = new StrategyStatsDto();
                dto.strategyId = strategyId;
                dto.strategyName = getStrategyName(strategyId);  // Map ID to name
                dto.totalPnl = pnl.doubleValue();
                dto.totalTrades = 0;  // TODO: Calculate from database
                dto.winningTrades = 0;
                dto.winRate = 0.0;
                dto.avgReturn = dto.totalTrades > 0 ? dto.totalPnl / dto.totalTrades : 0.0;

                stats.add(dto);
            }
        } catch (Exception e) {
            System.err.println("Error retrieving strategy stats: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Build complete analytics response for frontend
     * @return AnalyticsResponse with all metrics
     */
    public AnalyticsResponse buildAnalyticsResponse() {
        AnalyticsResponse resp = new AnalyticsResponse();
        resp.totalPnl = getTotalPnl();
        resp.winRate = getWinRate();
        resp.totalTrades = getTotalTradeCount();
        resp.winningTrades = getWinningTradeCount();
        resp.losingTrades = Math.max(0, resp.totalTrades - resp.winningTrades);
        resp.strategyStats = getStrategyStats();
        return resp;
    }

    /**
     * Helper: Map strategy ID to strategy name
     * This should query the Strategy table or use a cache
     */
    private String getStrategyName(Integer strategyId) {
        if (strategyId == null) return "Unknown";
        
        // TODO: Query Strategy table for name
        // For now, return a placeholder
        return switch (strategyId) {
            case 1 -> "Trend Following";
            case 2 -> "Mean Reversion";
            case 3 -> "Momentum";
            case 4 -> "Value";
            default -> "Strategy " + strategyId;
        };
    }
}
