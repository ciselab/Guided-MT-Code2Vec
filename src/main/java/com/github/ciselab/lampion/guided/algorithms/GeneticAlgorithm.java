package com.github.ciselab.lampion.guided.algorithms;

import com.github.ciselab.lampion.guided.configuration.GeneticConfiguration;
import com.github.ciselab.lampion.guided.support.GenotypeSupport;
import com.github.ciselab.lampion.guided.support.ParetoFront;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.random.RandomGenerator;

/**
 * The metamorphic algorithm performs the evolution of the metamorphic populations.
 */
public class GeneticAlgorithm {

    GeneticConfiguration config;

    private RandomGenerator randomGenerator;
    private final GenotypeSupport genotypeSupport;
    private final ParetoFront paretoFront;
    private final Logger logger = LogManager.getLogger(GeneticAlgorithm.class);
    private int currentGeneration;

    /**
     * Constructor for this class.
     *
     * @param genotypeSupport the genotypeSupport.
     * @param paretoFront     the pareto front.
     */
    public GeneticAlgorithm(GeneticConfiguration config, GenotypeSupport genotypeSupport, ParetoFront paretoFront, RandomGenerator generator) {
        this.genotypeSupport = genotypeSupport;
        this.randomGenerator = generator;
        this.config = config;
        this.paretoFront = paretoFront;
        currentGeneration = 0;
    }

    /**
     * This method creates the next population with crossover and mutation.
     *
     * @param pop the current population.
     * @return the new metamorphic population
     */
    public MetamorphicPopulation evolvePopulation(MetamorphicPopulation pop) {
        logger.debug("Getting next Generation from Genetic Algorithm");
        currentGeneration += 1;
        MetamorphicPopulation newPopulation =
                new MetamorphicPopulation(genotypeSupport, currentGeneration);

        // Loop over the population size and create new individuals with
        // crossover
        int index = 0;
        while (index < config.getPopSize()) {
            MetamorphicIndividual individual1 = tournamentSelection(pop, randomGenerator).get();
            MetamorphicIndividual individual2 = tournamentSelection(pop, randomGenerator).get();
            List<MetamorphicIndividual> newIndividuals = crossover(individual1, individual2, randomGenerator);
            // Set parents for new individuals
            for (MetamorphicIndividual individual : newIndividuals) {
                individual.setParents(individual1, individual2);
            }
            newPopulation.saveIndividual(newIndividuals.get(0));
            index++;
            if (index < newPopulation.size()) {
                newPopulation.saveIndividual(newIndividuals.get(1));
                index++;
            }
        }

        // Mutate population
        for (int i = 0; i < newPopulation.size(); i++) {
            if (Math.random() <= config.getMutationRate())
                mutate(newPopulation.getIndividual(i).get());
        }

        return newPopulation;
    }

    /**
     * Mutate the current individual
     *
     * @param individual The individual to increase or decrease the size of.
     */
    protected void mutate(MetamorphicIndividual individual) {
        if (individual.getLength() >= config.getMaxGeneLength() || Math.random() > config.getIncreaseSizeRate())
            individual.decrease(randomGenerator);
        else {
            int counter = 0;
            // The base for this computation is a bit math-y.
            // We are looking for the value y, for which
            // sum(y^x),x->infinity
            // equals our GrowthFactor.
            // This took 1 Professor, 1 PhD and a lot of Wolfram Alpha to derive the base below.
            // If your result vary: These things work on high numbers, so 10000 runs and upwards.
            double base = (config.getGrowthFactor() - 1) / (config.getGrowthFactor());
            while (randomGenerator.nextDouble() < Math.pow(base, counter)
                    && individual.getLength() < config.getMaxGeneLength()) {
                individual.increase(config.getMaxGeneLength(), randomGenerator);
                counter++;
            }
        }
    }

    /**
     * Crossover two metamorphic individuals.
     * Our crossover checks for geneA at any position if we want to pick the corresponding geneB if possible.
     * The same is done for GeneB.
     * The output genes will have the same length as the input genes.
     * Used configuration variables: "CrossoverRate"
     * Note: If Gene A is 10 long and gene B is 20, then crossover can only happen in the first 10 genes for B.
     *
     * @param individual1 the first metamorphic individual.
     * @param individual2 the second metamorphic individual.
     * @return the new metamorphic individual.
     */
    List<MetamorphicIndividual> crossover(MetamorphicIndividual individual1, MetamorphicIndividual individual2, RandomGenerator r) {
        logger.trace("Performing crossover between " + individual1.hexHash() + " and " + individual2.hexHash());
        MetamorphicIndividual firstChild = new MetamorphicIndividual(genotypeSupport, currentGeneration);
        MetamorphicIndividual secondChild = new MetamorphicIndividual(genotypeSupport, currentGeneration);
        List<MetamorphicIndividual> offsprings = new ArrayList<>();

        // Build First Child
        for (int i = 0; i < individual1.getLength(); i++) {
            // We pick a gene from first individual if
            // A) second Individual is too short
            // B) We don't want to cross-over here (based on chance)
            if (r.nextDouble() < config.getCrossoverRate()
                    && i < individual2.getLength()) {
                firstChild.addGene(individual2.getGene(i));
            } else {
                firstChild.addGene(individual1.getGene(i));
            }
        }

        // Build First Child
        for (int i = 0; i < individual2.getLength(); i++) {
            // See above, mirror behaviour
            if (r.nextDouble() < config.getCrossoverRate()
                    && i < individual1.getLength()) {
                secondChild.addGene(individual1.getGene(i));
            } else {
                secondChild.addGene(individual2.getGene(i));
            }
        }

        offsprings.add(firstChild);
        offsprings.add(secondChild);
        return offsprings;
    }

    /**
     * This method chooses a number of metamorphic individuals to perform tournament selection on.
     * From these metamorphic individuals it chooses the best metamorphic individual and returns that.
     * Needed config Variable: config.tournamentSize
     *
     * @param pop    the current population.
     * @param random the random generator used in this run.
     * @return the new metamorphic individual.
     */
    protected Optional<MetamorphicIndividual> tournamentSelection(MetamorphicPopulation pop, RandomGenerator random) {
        // Exit early on empty Pops
        if (pop.getIndividuals().isEmpty())
            return Optional.empty();

        // Create a tournament population
        MetamorphicPopulation tournament = new MetamorphicPopulation(genotypeSupport, currentGeneration);
        Collection<MetamorphicIndividual> pool = config.doTournamentPutBack() ? new ArrayList<>() : new HashSet<>();

        // ShortCut: We do not draw elements double, and we draw more than pop
        // Just return the fittest of elements
        if (!config.doTournamentPutBack() && config.getTournamentSize() >= pop.size()){
               pop.getIndividuals().stream().forEach(tournament::saveIndividual);
               return tournament.getFittest();
        }

        // For each place in the tournament get a random individual
        while (pool.size() < config.getTournamentSize()){
            var candidate = pop.getIndividual(random.nextInt(pop.size())).get();
            pool.add(candidate);
        }
        pool.forEach(tournament::saveIndividual);

        return tournament.getFittest();
    }


    /**
     * Check population against the current Pareto set.
     *
     * @param population the population
     */
    public void checkPareto(MetamorphicPopulation population) {
        // This has to be an iteration, as the Pareto Front is maybe altered in during the run.
        // Hence, it has to be done step by step otherwise you get a concurrentModificationException
        for (int i = 0; i < population.size(); i++) {
            paretoFront.addToParetoOptimum(population.getIndividual(i).get());
        }
    }

}
