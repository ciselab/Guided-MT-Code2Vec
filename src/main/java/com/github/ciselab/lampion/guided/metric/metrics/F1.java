package com.github.ciselab.lampion.guided.metric.metrics;

import com.github.ciselab.lampion.guided.algorithms.MetamorphicIndividual;
import com.github.ciselab.lampion.guided.metric.Metric;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;
import java.util.List;

/**
 * The F1_Score metric.
 * This metric is already calculated by the code2vec project and gotten from a file.
 */
public class F1 extends Metric {
    private static final String EXPECTEDFILE = "F1_score_log.txt";

    public F1() {
        this.name = Name.F1;
    }

    private double calculateScore(String path) {
        if (!path.contains("results"))
            path = path + File.separator + "results";
        List<String> lines = readPredictions(path + File.separator + EXPECTEDFILE);
        Double score = Double.NaN;
        for (String i : lines) {
            if (i.contains("F1")) {
                score = Double.parseDouble(i.split("F1: ")[1]);
            }
        }
        return score;
    }

    @Override
    public boolean isSecondary() {
        return false;
    }

    @Override
    public Double apply(MetamorphicIndividual individual) {
        return individual.getResultPath()
                .map(i -> calculateScore(i))
                .orElse(0.0);
    }

    @Override
    public boolean canBeBiggerThanOne() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof F1 ed) {
            return ed.getWeight() == this.getWeight();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(weight).hashCode();
    }
}
