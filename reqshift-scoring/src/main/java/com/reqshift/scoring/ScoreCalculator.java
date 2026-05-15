package com.reqshift.scoring;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import com.reqshift.core.model.Category;
import com.reqshift.core.model.RuleResult;
import com.reqshift.core.model.Score;

public final class ScoreCalculator {

    private static final Map<Category, Integer> WEIGHTS =
            Map.of(
                    Category.SECURITY, 30,
                    Category.DESIGN, 20,
                    Category.DOCUMENTATION, 15,
                    Category.SCHEMAS, 15,
                    Category.CONFORMANCE, 10,
                    Category.HTTP_CODES, 10);

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
        int overall = (int) Math.round(weightedSum / totalWeight);

        return new Score(grade(overall), Map.copyOf(scoresByCategory), overall);
    }

    private char grade(int overall) {
        if (overall >= 90) return 'A';
        if (overall >= 80) return 'B';
        if (overall >= 70) return 'C';
        if (overall >= 60) return 'D';
        return 'F';
    }
}
