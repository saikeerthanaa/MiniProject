package com.tradejournal.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.tradejournal.dto.AnalyticsResponse;
import com.tradejournal.dto.StrategyStatsDto;

/**
 * AnalyticsWrapper — Simple analytics service that queries the database directly
 * for actual portfolio performance metrics.
 */
@Service
public class AnalyticsWrapper {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    private final int PORTFOLIO_ID = 1;  // Default portfolio

    /**
     * Build complete analytics response for frontend by querying database
     */
    public AnalyticsResponse buildAnalyticsResponse() {
        AnalyticsResponse resp = new AnalyticsResponse();

        if (jdbcTemplate == null) {
            return resp;
        }

        try {
            // Query PerformanceSummary table for aggregated metrics
            String perfQuery = "SELECT SUM(total_pnl) as total_pnl, AVG(win_rate) as avg_win_rate, "
                             + "SUM(total_trades) as total_trades, SUM(winning_trades) as winning_trades "
                             + "FROM \"PerformanceSummary\"";
            
            jdbcTemplate.queryForMap(perfQuery).forEach((key, value) -> {
                if ("total_pnl".equals(key)) {
                    resp.totalPnl = value != null ? ((Number) value).doubleValue() : 0.0;
                } else if ("avg_win_rate".equals(key)) {
                    resp.winRate = value != null ? ((Number) value).doubleValue() : 0.0;
                } else if ("total_trades".equals(key)) {
                    resp.totalTrades = value != null ? ((Number) value).intValue() : 0;
                } else if ("winning_trades".equals(key)) {
                    resp.winningTrades = value != null ? ((Number) value).intValue() : 0;
                }
            });

            resp.losingTrades = Math.max(0, resp.totalTrades - resp.winningTrades);

            // Get strategy-wise stats
            resp.strategyStats = getStrategyStats();

        } catch (Exception e) {
            System.err.println("[AnalyticsWrapper] Error building analytics: " + e.getMessage());
        }

        return resp;
    }

    /**
     * Get strategy-wise performance statistics
     */
    private List<StrategyStatsDto> getStrategyStats() {
        List<StrategyStatsDto> stats = new ArrayList<>();

        if (jdbcTemplate == null) return stats;

        try {
            // Query trades grouped by strategy
            String query = "SELECT s.id, s.name, "
                         + "COUNT(CASE WHEN t.trade_type = 'SELL' THEN 1 END) as total_trades, "
                         + "COUNT(CASE WHEN t.price > (SELECT AVG(price) FROM \"Trade\" t2 WHERE t2.strategy_id = s.id AND t2.trade_type = 'BUY') THEN 1 END) as winning_trades "
                         + "FROM \"Strategy\" s "
                         + "LEFT JOIN \"Trade\" t ON s.id = t.strategy_id "
                         + "GROUP BY s.id, s.name "
                         + "HAVING COUNT(t.id) > 0";

            jdbcTemplate.queryForList(query).forEach(row -> {
                StrategyStatsDto dto = new StrategyStatsDto();
                dto.strategyId = (Integer) row.get("id");
                dto.strategyName = (String) row.get("name");
                dto.totalTrades = (Integer) row.get("total_trades");
                dto.winningTrades = (Integer) row.get("winning_trades");
                dto.winRate = dto.totalTrades > 0 ? (dto.winningTrades * 100.0 / dto.totalTrades) : 0.0;
                dto.totalPnl = 0.0;  // Would need to calculate from individual trades
                dto.avgReturn = 0.0;
                
                stats.add(dto);
            });

        } catch (Exception e) {
            System.err.println("[AnalyticsWrapper] Error getting strategy stats: " + e.getMessage());
        }

        // If no strategies found from trades, return empty list
        return stats;
    }
}
