package com.threasury.compliance.dto;

public record RuleCheckResult(
        String ruleName,
        String ruleType,
        boolean passed,
        String expected,
        String actual,
        String message
) {
    public static RuleCheckResult passed(String ruleName, String ruleType, String expected, String actual) {
        return new RuleCheckResult(ruleName, ruleType, true, expected, actual, "Rule satisfied.");
    }

    public static RuleCheckResult failed(String ruleName, String ruleType, String expected, String actual, String message) {
        return new RuleCheckResult(ruleName, ruleType, false, expected, actual, message);
    }
}
