package com.github.ciselab.lampion.guided.algorithms;

import com.github.ciselab.lampion.guided.support.GenotypeSupport;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

public class MetamorphicPopulation {

    private List<MetamorphicIndividual> individuals;
    private final int generation;
    private GenotypeSupport genotypeSupport;

    public MetamorphicPopulation(GenotypeSupport gen, int generation) {
        genotypeSupport = gen;
        individuals = new LinkedList<>();
        this.generation = generation;
    }
    public MetamorphicPopulation(GenotypeSupport gen) {
        genotypeSupport = gen;
        individuals = new LinkedList<>();
        this.generation = 0;
    }

    /**
     * Initializes the Population by a given size.
     * This will create brand-new individuals that have between one and three transformations.
     * Number of Transformations is evenly distributed.
     * @param populationSize   How many new Individuals to put in the Population
     * @param randomGenerator A random number provider
     */
    public void initialize(int populationSize, RandomGenerator randomGenerator){
        final int DEFAULT_GROWTHRATE = 3;
        initialize(populationSize,DEFAULT_GROWTHRATE,randomGenerator);
    }

    /**
     * Initializes the Population by a given size.
     * This will create brand-new individuals that have between one and growthfactor transformations.
     * Number of Transformations is evenly distributed.
     * @param populationSize   How many new Individuals to put in the Population
     * @param growthFactor up to how many transformation every individual has
     * @param randomGenerator A random number provider
     */
    public void initialize(int populationSize, int growthFactor, RandomGenerator randomGenerator){
        if (populationSize < 1 || growthFactor < 1){
            throw new IllegalArgumentException("PopulationSize and GrowthFactor must be bigger than 0");
        }
        for (int i = 0; i < populationSize; i++) {
            MetamorphicIndividual individual = new MetamorphicIndividual(genotypeSupport, generation);
            int transformations = randomGenerator.nextInt(1,growthFactor);
            individual.populateIndividual(randomGenerator, transformations);
            saveIndividual(individual);
        }
    }

    /**
     * Get the average size of this population.
     *
     * @return the average size.
     */
    public int getAverageSize() {
        int sum = 0;
        for (MetamorphicIndividual i : individuals) {
            sum += i.getLength();
        }
        return sum / individuals.size();
    }

    /**
     * Get a metamorphic individual based on the index.
     *
     * @param index the population index.
     * @return the metamorphic individual.
     */
    public Optional<MetamorphicIndividual> getIndividual(int index) {
        if (individuals.size() > index) {
            return Optional.of(individuals.get(index));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Changes a metamorphic individual at a given index.
     *
     * @param individual the metamorphic individual.
     */
    public void saveIndividual(MetamorphicIndividual individual) {
        individuals.add(individual);
    }

    /**
     * Get the fittest metamorphic individual of the metamorphic population.
     * Important: Fitness 1 is considered the "best".
     *
     * @return the fittest metamorphic individual.
     */
    public Optional<MetamorphicIndividual> getFittest() {
        Optional<MetamorphicIndividual> fittest = individuals.stream().findFirst();
        // Exit Early in case of empty individuals
        if (fittest.isEmpty())
            return Optional.empty();

        for (MetamorphicIndividual individual : individuals) {
            // Case 1: The Individual is fitter - set it as new fittest
            fittest = individual.getFitness() > fittest.get().getFitness() ? Optional.of(individual) : fittest;
            // Case 2: They are same fit, but one is shorter - pick shorter
            if (fittest.get().getFitness() == individual.getFitness())
                fittest = individual.getLength() < fittest.get().getLength() ? Optional.of(individual) : fittest;
            // Case 3: Fittest is fittest - do nothing
        }
        return fittest;
    }

    /**
     * Get the size of the population.
     *
     * @return the size of the population.
     */
    public int size() {
        return individuals.size();
    }

    @Override
    public String toString() {
        String output = "MetamorphicPopulation{";
        for (MetamorphicIndividual indiv : individuals)
            output += indiv.toString() + ", ";
        if (!output.equals("MetamorphicPopulation{"))
            return output.substring(0, output.length() - 2) + "}";
        else
            return output;
    }

    public List<MetamorphicIndividual> getIndividuals() {
        return this.individuals;
    }

    public int getGeneration() {
        return this.generation;
    }
}
