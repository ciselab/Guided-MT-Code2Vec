package com.github.ciselab.lampion.guided.algorithms;

import com.github.ciselab.lampion.guided.support.GenotypeSupport;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

public class MetamorphicPopulation {

    List<MetamorphicIndividual> individuals;
    int populationSize;
    GenotypeSupport genotypeSupport;

    /**
     * Initialize Metamorphic population, the initial population will be half of length 1 and half of length 2.
     * After that the evolution begins.
     *
     * @param popSize         the population size.
     * @param randomGenerator the random generator. This is kept the same everywhere for testing purposes.
     * @param initialize      whether the population should be initialized or just created as an object.
     * @param generation      the generation of the current population.
     */
    public MetamorphicPopulation(int popSize, RandomGenerator randomGenerator, boolean initialize
            , GenotypeSupport gen, int generation) {
        genotypeSupport = gen;
        this.populationSize = popSize;
        individuals = new LinkedList<>();
        if (initialize) {
            int cutOff = popSize / 2;
            for (int i = 0; i < cutOff; i++) {
                MetamorphicIndividual individual = new MetamorphicIndividual(genotypeSupport, generation);
                individual.populateIndividual(randomGenerator, 1);
                saveIndividual(individual);
            }
            for (int j = cutOff; j < popSize; j++) {
                MetamorphicIndividual individual = new MetamorphicIndividual(genotypeSupport, generation);
                individual.populateIndividual(randomGenerator, 2);
                saveIndividual(individual);
            }
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
        return populationSize;
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
}
