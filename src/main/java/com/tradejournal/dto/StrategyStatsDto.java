package com.tradejournal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * StrategyStatsDto — Per-strategy performance metrics
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StrategyStatsDto {
    public Integer strategyId;
    public String strategyName;
    public Integer totalTrades;
    public Integer winningTrades;
    public Double winRate;
    public Double totalPnl;
    public Double avgReturn;
}
