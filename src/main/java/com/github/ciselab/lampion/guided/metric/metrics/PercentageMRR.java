package com.github.ciselab.lampion.guided.metric.metrics;

import com.github.ciselab.lampion.guided.algorithms.MetamorphicIndividual;
import com.github.ciselab.lampion.guided.metric.Metric;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The percentage Mean Reciprocal Rank (Percentage_MRR) custom metric.
 */
public class PercentageMRR extends Metric {

    private static final String EXPECTEDFILE =  "results.txt";
    public PercentageMRR() {
        this.name = Name.PMRR;
    }

    private double calculateScore(String path) {
        if(!path.contains("results"))
            path = path + File.separator + "results";
        List<String> predictions = readPredictions(path  + File.separator + EXPECTEDFILE);
        var  scores = new ArrayList<>();
        double score = 0;
        int size = 0;
        for(int i = 0; i < predictions.size(); i++) {
            String current = predictions.get(i);
            if(current.contains("No results for predicting:")) {
                score += 0;
                scores.add(0f);
                size++;
            } else if(current.contains("score: ")) {
                double rank = Double.parseDouble(current.split("score: ")[1]);
                score += (rank/100);
                scores.add((float) (rank/100));
                size++;
            }
        }
        return score/size;
    }


    @Override
    public boolean isSecondary() {
        return false;
    }

    @Override
    public Double apply(MetamorphicIndividual individual) {
        double score =  individual.getResultPath()
                .map(i -> calculateScore(i))
                .orElse(0.0);
        if(!objective)
            return 1-score;
        else
            return score;
    }

    @Override
    public boolean canBeBiggerThanOne() {
        return false;
    }


    @Override
    public boolean equals(Object o){
        if (o == this) {
            return true;
        }
        if (o instanceof PercentageMRR ed) {
            return ed.getWeight() == this.getWeight();
        }
        return false;
    }

    @Override
    public int hashCode(){
        return new HashCodeBuilder().append(name).append(weight).hashCode();
    }
}
