package com.reqshift.scoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.Rule;
import com.reqshift.core.model.RuleResult;
import com.reqshift.core.model.Score;
import com.reqshift.core.model.Severity;
import com.reqshift.core.model.Violation;

import io.swagger.v3.oas.models.OpenAPI;

class ScoreCalculatorTest {

    private final ScoreCalculator calculator = new ScoreCalculator();

    @Test
    void noViolationsGivesPerfectAGrade() {
        Score score = calculator.compute(List.of());

        assertThat(score.grade()).isEqualTo('A');
        assertThat(score.overall()).isEqualTo(100);
        assertThat(score.byCategory()).allSatisfy((c, s) -> assertThat(s).isEqualTo(100));
    }

    @Test
    void criticalSecurityViolationDropsCategoryScore() {
        Rule securityRule = fakeRule("SEC_FAKE", Severity.CRITICAL, Category.SECURITY);
        Violation violation = new Violation("SEC_FAKE", Severity.CRITICAL, "boom", "#/x", "fix");
        RuleResult result = new RuleResult(securityRule, List.of(violation, violation));

        Score score = calculator.compute(List.of(result));

        assertThat(score.byCategory().get(Category.SECURITY)).isEqualTo(70);
        assertThat(score.byCategory().get(Category.DESIGN)).isEqualTo(100);
    }

    @Test
    void everyCategoryHeavilyPenalisedYieldsFGrade() {
        // 10 CRITICAL violations per category => penalty 150 each => every category scores 0
        List<RuleResult> allCategoriesBroken =
                java.util.Arrays.stream(Category.values())
                        .map(
                                cat -> {
                                    Rule rule = fakeRule("R_" + cat, Severity.CRITICAL, cat);
                                    Violation v =
                                            new Violation(
                                                    "R_" + cat, Severity.CRITICAL, "m", "l", "s");
                                    return new RuleResult(
                                            rule, java.util.Collections.nCopies(10, v));
                                })
                        .toList();

        Score score = calculator.compute(allCategoriesBroken);

        assertThat(score.grade()).isEqualTo('F');
        assertThat(score.overall()).isEqualTo(0);
    }

    @Test
    void moderatePenaltyAcrossCategoriesYieldsBGrade() {
        // 1 WARNING (3 pts) in each weighted category => each category scores 97
        List<RuleResult> mildlyBroken =
                WEIGHTED_CATEGORIES.stream()
                        .map(
                                cat -> {
                                    Rule rule = fakeRule("R_" + cat, Severity.WARNING, cat);
                                    Violation v =
                                            new Violation(
                                                    "R_" + cat, Severity.WARNING, "m", "l", "s");
                                    return new RuleResult(rule, List.of(v));
                                })
                        .toList();

        Score score = calculator.compute(mildlyBroken);

        assertThat(score.overall()).isEqualTo(97);
        assertThat(score.grade()).isEqualTo('A');
    }

    private static final List<Category> WEIGHTED_CATEGORIES =
            List.of(
                    Category.SECURITY,
                    Category.DESIGN,
                    Category.DOCUMENTATION,
                    Category.SCHEMAS,
                    Category.CONFORMANCE,
                    Category.HTTP_CODES);

    private static Rule fakeRule(String id, Severity severity, Category category) {
        return new Rule() {
            @Override
            public String id() {
                return id;
            }

            @Override
            public Severity severity() {
                return severity;
            }

            @Override
            public Category category() {
                return category;
            }

            @Override
            public List<Violation> check(OpenAPI openApi) {
                return List.of();
            }
        };
    }
}
