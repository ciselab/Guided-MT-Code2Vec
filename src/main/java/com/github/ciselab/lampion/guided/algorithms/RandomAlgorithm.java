package com.github.ciselab.lampion.guided.algorithms;

import com.github.ciselab.lampion.guided.support.GenotypeSupport;
import com.github.ciselab.lampion.guided.support.MetricCache;
import com.github.ciselab.lampion.guided.support.ParetoFront;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;
import java.util.random.RandomGenerator;

public class RandomAlgorithm {

    private int currentGeneration;
    private RandomGenerator randomGenerator;
    private final GenotypeSupport genotypeSupport;
    private final ParetoFront paretoFront;
    private final Logger logger = LogManager.getLogger(RandomAlgorithm.class);


    public void initializeParameters(RandomGenerator randomGenerator) {
        logger.debug("Initialize parameters for the random algorithm");
        this.randomGenerator = randomGenerator;
    }

    public RandomAlgorithm(GenotypeSupport gen, ParetoFront paretoFront) {
        genotypeSupport = gen;
        this.paretoFront = paretoFront;
        currentGeneration = 0;
    }

    public MetamorphicPopulation nextGeneration(MetamorphicPopulation pop) {
        currentGeneration += 1;
        logger.debug("Creating a new population of length " + currentGeneration + " through the random algorithm.");
        MetamorphicPopulation newPop =
                new MetamorphicPopulation(genotypeSupport, currentGeneration);

        newPop.initialize(pop.size(), currentGeneration,randomGenerator);
        return newPop;
    }

    /**
     * Check population with the current Pareto set.
     *
     * @param population the population
     */
    public void checkPareto(MetamorphicPopulation population) {
        // This has to be an iteration, as the Pareto Front is maybe altered in during the run.
        // Hence, it has to be done step by step otherwise you get a concurrentmodificationexception
        population.getIndividuals().forEach(x -> paretoFront.addToParetoOptimum(x));
    }
}
