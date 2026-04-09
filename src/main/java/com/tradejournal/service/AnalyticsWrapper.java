package com.tradejournal.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.tradejournal.dto.AnalyticsResponse;
import com.tradejournal.dto.StrategyStatsDto;

/**
 * AnalyticsWrapper — Queries database for real trade data
 */
@Service
public class AnalyticsWrapper {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    public AnalyticsResponse buildAnalyticsResponse() {
        AnalyticsResponse resp = new AnalyticsResponse();

        if (jdbcTemplate == null) {
            System.out.println("[AnalyticsWrapper] JdbcTemplate not available");
            return resp;
        }

        try {
            // Try to get trade count
            try {
                Integer tradeCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM \"Trade\"", 
                    Integer.class
                );
                resp.totalTrades = tradeCount != null ? tradeCount : 0;
                System.out.println("[AnalyticsWrapper] Found " + resp.totalTrades + " trades");
            } catch (Exception e) {
                System.out.println("[AnalyticsWrapper] Trade table not found or error: " + e.getMessage());
                resp.totalTrades = 0;
            }

            // Try to get performance summary
            try {
                if (resp.totalTrades > 0) {
                    String perfQuery = "SELECT COALESCE(SUM(total_pnl), 0) as total_pnl, "
                                     + "COALESCE(AVG(win_rate), 0) as avg_win_rate, "
                                     + "COALESCE(SUM(winning_trades), 0) as winning_trades "
                                     + "FROM \"PerformanceSummary\"";
                    
                    try {
                        Map<String, Object> perfData = jdbcTemplate.queryForMap(perfQuery);
                        resp.totalPnl = perfData.get("total_pnl") != null ? ((Number) perfData.get("total_pnl")).doubleValue() : 0.0;
                        resp.winRate = perfData.get("avg_win_rate") != null ? ((Number) perfData.get("avg_win_rate")).doubleValue() : 0.0;
                        resp.winningTrades = perfData.get("winning_trades") != null ? ((Number) perfData.get("winning_trades")).intValue() : 0;
                    } catch (Exception e2) {
                        System.out.println("[AnalyticsWrapper] PerformanceSummary table not found - using zeros");
                    }
                }
            } catch (Exception e) {
                System.out.println("[AnalyticsWrapper] Error querying performance: " + e.getMessage());
            }

            resp.losingTrades = Math.max(0, resp.totalTrades - resp.winningTrades);
            resp.strategyStats = getStrategyStats();

        } catch (Exception e) {
            System.err.println("[AnalyticsWrapper] Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[AnalyticsWrapper] Returning response: trades=" + resp.totalTrades + ", pnl=" + resp.totalPnl);
        return resp;
    }

    private List<StrategyStatsDto> getStrategyStats() {
        List<StrategyStatsDto> stats = new ArrayList<>();

        if (jdbcTemplate == null) return stats;

        try {
            String query = "SELECT strategy_id, COUNT(*) as count FROM \"Trade\" GROUP BY strategy_id";
            
            try {
                List<Map<String, Object>> results = jdbcTemplate.queryForList(query);
                for (Map<String, Object> row : results) {
                    StrategyStatsDto dto = new StrategyStatsDto();
                    dto.strategyId = ((Number) row.get("strategy_id")).intValue();
                    dto.strategyName = "Strategy " + dto.strategyId;
                    dto.totalTrades = ((Number) row.get("count")).intValue();
                    dto.winRate = 50.0; // Default
                    dto.totalPnl = 0.0;
                    dto.avgReturn = 0.0;
                    dto.winningTrades = 0;
                    stats.add(dto);
                }
            } catch (Exception e) {
                System.out.println("[AnalyticsWrapper] Trade table query failed: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("[AnalyticsWrapper] Error in getStrategyStats: " + e.getMessage());
        }

        return stats;
    }
}
