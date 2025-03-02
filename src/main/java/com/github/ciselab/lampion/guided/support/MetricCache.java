package com.github.ciselab.lampion.guided.support;

import com.github.ciselab.lampion.core.transformations.Transformer;
import com.github.ciselab.lampion.guided.algorithms.MetamorphicIndividual;
import com.github.ciselab.lampion.core.transformations.transformers.BaseTransformer;
import com.github.ciselab.lampion.guided.metric.Metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.lang.Math.abs;

public class MetricCache {

    List<Metric> metricList = new ArrayList<>(); // Any at all, including secondary
    List<Metric> activeMetrics = new ArrayList<>(); // Metrics that Guide Fitness

    private final Map<MetamorphicIndividual, String> fileLookup = new HashMap<>();
    private final Map<MetamorphicIndividual, Map<Metric, Double>> lookup = new HashMap<>();

    private final Logger logger = LogManager.getLogger(MetricCache.class);

    public List<Metric> getMetrics() {
        return metricList;
    }

    public List<Metric> getActiveMetrics() {
        return activeMetrics;
    }

    public void addMetric(Metric metric) {
        metricList.add(metric);
        if (metric.getWeight() != 0) {
            activeMetrics.add(metric);
        }
    }

    /**
     * Put a new transformer list and file combination into the fileLookup map.
     *
     * @param transformers the list of transformers.
     * @param file         the file name.
     */
    public void putFileCombination(MetamorphicIndividual transformers, String file) {
        fileLookup.put(transformers, file);
    }

    /**
     * Get the directory corresponding to the given genotype.
     *
     * @param genotype the list of transformers.
     * @return the directory string if it exists, null otherwise.
     */
    public Optional<String> getDir(List<Transformer> genotype) {
        String file = fileLookup.get(genotype);
        return Optional.ofNullable(file);
    }

    /**
     * Create a key value pair of an individual and the corresponding fitness.
     *
     * @param individual    the individual.
     * @param metricResults the fitness score.
     */
    public void storeMetricResults(MetamorphicIndividual individual, Map<Metric, Double> metricResults) {
        lookup.put(individual, metricResults);
    }

    public Optional<Map<Metric, Double>> getMetricResults(MetamorphicIndividual individual) {
        return Optional.ofNullable(lookup.get(individual));
    }


    /**
     * Store the current genotype together with the fitness and filename in the map for later reference.
     *
     * @param genotype the list of transformers.
     * @param fileName the file name.
     * @param scores   the fitness scores.
     */
    public void storeFiles(MetamorphicIndividual genotype, String fileName, Map<Metric, Double> scores) {
        fileLookup.put(genotype, fileName);
        lookup.put(genotype, scores);
    }

    /**
     * Initialize all weight properties and objectives.
     */
    public void initWeights() {
        removeZeroWeights();
        normalizeWeights();

        metricList.forEach(m -> m.setObjective(m.getWeight() > 0));
    }

    /**
     * Remove all metrics that have a weight of zero from activeMetrics.
     * The metrics will be kept in the normal metrics, but are not used for fitness calculation.
     */
    private void removeZeroWeights() {
        var toRemove = activeMetrics.stream().filter(m -> m.getWeight() == 0).toList();
        activeMetrics.removeAll(toRemove);
    }

    /**
     * Normalizes the weights and ensures that there is at least one metric enabled.
     * The resulting weights will be summed up 1, with the old proportions kept.
     */
    private void normalizeWeights() {
        final double sum = activeMetrics.stream().mapToDouble(x -> abs(x.getWeight())).sum();
        if (sum <= 0) {
            logger.error("Combined (absolute) weight is smaller or equal zero. There should be at least one metric enabled.");
            throw new IllegalArgumentException("There should be at least one metric enabled.");
        } else {
            activeMetrics.stream().forEach(
                    m -> m.setWeight(m.getWeight() / sum)
            );
        }
    }

    public void putMetricResults(MetamorphicIndividual i, Map<Metric, Double> inferMetrics) {
        this.lookup.put(i, inferMetrics);
    }

    /*
    Returns true if AT LEAST one metric has negative weight.
    In case of multiple positive weights and one negative, the result will be true.
     */
    public boolean doMaximize() {
        return activeMetrics.stream().anyMatch(m -> m.getWeight() < 0);
    }
}
