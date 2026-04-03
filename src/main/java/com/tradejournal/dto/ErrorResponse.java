package com.tradejournal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * ErrorResponse — Risk violation or validation error
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    public String message;
    public String code;
    public String rule;       // For risk violations: the rule that failed
    public String detail;     // Human-readable explanation
    public Long timestamp = System.currentTimeMillis();
    
    public ErrorResponse(String message, String code) {
        this.message = message;
        this.code = code;
    }
    
    public ErrorResponse(String message, String code, String rule, String detail) {
        this.message = message;
        this.code = code;
        this.rule = rule;
        this.detail = detail;
    }
}
