package com.threasury.compliance.service;

import com.threasury.compliance.dto.ExpectedFormData;
import com.threasury.compliance.dto.ExtractedLabelData;
import com.threasury.compliance.dto.RuleCheckResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComplianceRuleEngineTest {

    private ComplianceRuleEngine engine;

    @BeforeEach
    void setUp() {
        engine = new ComplianceRuleEngine();
    }

    @Test
    void fuzzyBrandName_matchesRegardlessOfCaseAndWhitespace() {
        assertTrue(engine.fuzzyMatch("Stone's Throw", "STONE'S THROW"));
        assertTrue(engine.fuzzyMatch("Stone's Throw", "stone s throw"));
    }

    @Test
    void perfectHealthWarning_requiresExactPhrase() {
        var expected = new ExpectedFormData("Brand", "12.5", "Wine");
        var compliant = new ExtractedLabelData(
                "BRAND",
                "12.5%",
                "Wine",
                "Some text GOVERNMENT WARNING: (1) According to the Surgeon General..."
        );
        var nonCompliantLower = new ExtractedLabelData(
                "BRAND",
                "12.5%",
                "Wine",
                "government warning: missing exact casing"
        );
        var missing = new ExtractedLabelData("BRAND", "12.5%", "Wine", "No warning here");

        assertTrue(warningPassed(engine.evaluate(expected, compliant)));
        assertFalse(warningPassed(engine.evaluate(expected, nonCompliantLower)));
        assertFalse(warningPassed(engine.evaluate(expected, missing)));
    }

    private boolean warningPassed(java.util.List<RuleCheckResult> results) {
        return results.stream()
                .filter(r -> "Government Health Warning".equals(r.ruleName()))
                .findFirst()
                .map(RuleCheckResult::passed)
                .orElse(false);
    }
}
