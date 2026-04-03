package com.tradejournal.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * AnalyticsResponse — Portfolio summary sent to frontend
 * Expected by Dashboard and Analytics pages
 *
 * JSON structure:
 * {
 *   "totalPnl": -25000.00,
 *   "winRate": 45.5,
 *   "totalTrades": 20,
 *   "winningTrades": 9,
 *   "losingTrades": 11,
 *   "strategyStats": [
 *     {
 *       "strategyId": "1",
 *       "strategyName": "Momentum",
 *       "totalTrades": 10,
 *       "winningTrades": 6,
 *       "winRate": 60.0,
 *       "totalPnl": 15000.00,
 *       "avgReturn": 1500.00
 *     }
 *   ]
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyticsResponse {
    public Double totalPnl;
    public Double winRate;
    public Integer totalTrades;
    public Integer winningTrades;
    public Integer losingTrades;
    public List<StrategyStatsDto> strategyStats;
}
