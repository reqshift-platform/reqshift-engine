package com.reqshift.scoring;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.RuleResult;
import com.reqshift.core.model.Score;
import com.reqshift.core.model.Severity;

public final class ScoreCalculator {

    private static final Map<Category, Integer> WEIGHTS =
            Map.of(
                    Category.SECURITY, 30,
                    Category.DESIGN, 20,
                    Category.DOCUMENTATION, 15,
                    Category.SCHEMAS, 15,
                    Category.CONFORMANCE, 10,
                    Category.HTTP_CODES, 10);

    private static final int CAP_CRITICAL = 79;
    private static final int CAP_ERROR = 89;

    public Score compute(List<RuleResult> results) {
        Map<Category, Integer> penalties = new EnumMap<>(Category.class);
        for (Category c : Category.values()) {
            penalties.put(c, 0);
        }
        for (RuleResult result : results) {
            int total =
                    result.violations().stream().mapToInt(v -> v.severity().penaltyPoints()).sum();
            penalties.merge(result.rule().category(), total, Integer::sum);
        }

        Map<Category, Integer> scoresByCategory = new EnumMap<>(Category.class);
        for (Map.Entry<Category, Integer> e : penalties.entrySet()) {
            scoresByCategory.put(e.getKey(), Math.max(0, 100 - e.getValue()));
        }

        double weightedSum = 0;
        int totalWeight = 0;
        for (Map.Entry<Category, Integer> w : WEIGHTS.entrySet()) {
            weightedSum += scoresByCategory.get(w.getKey()) * w.getValue();
            totalWeight += w.getValue();
        }
        int rawOverall = (int) Math.round(weightedSum / totalWeight);

        Severity highest = highestSeverity(results);
        int capped = rawOverall;
        Severity cappedBy = null;
        if (highest == Severity.CRITICAL && rawOverall > CAP_CRITICAL) {
            capped = CAP_CRITICAL;
            cappedBy = Severity.CRITICAL;
        } else if (highest == Severity.ERROR && rawOverall > CAP_ERROR) {
            capped = CAP_ERROR;
            cappedBy = Severity.ERROR;
        }

        return new Score(grade(capped), Map.copyOf(scoresByCategory), capped, rawOverall, cappedBy);
    }

    private Severity highestSeverity(List<RuleResult> results) {
        Severity max = null;
        for (RuleResult r : results) {
            for (var v : r.violations()) {
                if (max == null || v.severity().ordinal() > max.ordinal()) {
                    max = v.severity();
                }
            }
        }
        return max;
    }

    private char grade(int overall) {
        if (overall >= 90) return 'A';
        if (overall >= 80) return 'B';
        if (overall >= 70) return 'C';
        if (overall >= 60) return 'D';
        return 'F';
    }
}
