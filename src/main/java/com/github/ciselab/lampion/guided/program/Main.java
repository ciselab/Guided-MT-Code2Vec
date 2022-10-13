package com.github.ciselab.lampion.guided.program;

import com.github.ciselab.lampion.guided.algorithms.GeneticAlgorithm;
import com.github.ciselab.lampion.guided.algorithms.MetamorphicIndividual;
import com.github.ciselab.lampion.guided.algorithms.MetamorphicPopulation;
import com.github.ciselab.lampion.guided.algorithms.RandomAlgorithm;
import com.github.ciselab.lampion.guided.configuration.ConfigManagement;
import com.github.ciselab.lampion.guided.configuration.Configuration;
import com.github.ciselab.lampion.guided.support.*;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalTime;
import java.util.*;
import java.util.random.RandomGenerator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This is the main class of the project where the evolutionary algorithm engine is created,
 * together with the MetamorphicProblem.
 * <p>
 * Important:
 * Due to Code2Vec Logic, there is 1 running file for results.
 * This gets updated for each new individual.
 * Hence, we first have to predict an individual, read all metrics in,
 * store them in java somewhere and then go on.
 */
public class Main {

    // GA parameters
    private final static Logger logger = LogManager.getLogger(Main.class);

    private static MetricCache cache;
    private static ParetoFront paretoFront;
    private static Configuration config;
    private static GenotypeSupport genotypeSupport;

    private static String logDir = "";

    /**
     * The main method for the Guided-MT-Code2Vec project.
     *
     * @param args system arguments.
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        logger.info("Guided-MT started");

        // For processing, it is important to always have the same format (e.g. 1.00 instead of 1,00)
        // While we specify them later extra, we set them here too as a safety net.
        Locale.setDefault(new Locale("UK"));

        if (args.length == 0) {
            logger.info("No arguments found - loading default values");
        } else if (args.length == 4) {
            logger.debug("Received four arguments "
                    + "\nConfig input: " + args[0]
                    + "\nmodel: " + args[1]
                    + "\ndata input: " + args[2]
                    + "\ndata output: " + args[3]);

            config = ConfigManagement.readConfig(args[0]);
            cache = ConfigManagement.initializeMetricCache(args[0]);
            paretoFront = new ParetoFront(cache);
            genotypeSupport = new GenotypeSupport(cache, config);

            config.program.setModelPath(args[1]);
            FileManagement.copyDirectory(args[2],
                    Path.of(config.program.getDataDirectoryPath().toAbsolutePath().toString(), genotypeSupport.getInitialDataset(), "test").toString());
            config.program.setCode2vecDirectory(
                    Path.of(config.program.getBasePath().toAbsolutePath().toString(), "/code2vec/").toString()
            );
            config.program.setDataDirectoryPath(
                    Path.of(config.program.getCode2vecDirectory().toString(), "data").toString()
            );

            logDir = args[3] + "/";
        } else {
            logger.error("Received an unsupported amount of arguments");
            return;
        }

        if (config.program.useGA())
            runSimpleGA();
        else
            runRandomAlgo();
    }


    public static void runRandomAlgo() {
        RandomAlgorithm algorithm = new RandomAlgorithm(genotypeSupport, paretoFront);
        RandomGenerator randomGenerator = new SplittableRandom(config.program.getSeed());
        algorithm.initializeParameters(randomGenerator);
        logger.info("Using Random-Search");
        LocalTime start = LocalTime.now();

        // Create an initial population
        try {
            MetamorphicPopulation myPop =
                    new MetamorphicPopulation(genotypeSupport, 0);
            for (int i = 0; i < config.genetic.getPopSize(); i++) {
                MetamorphicIndividual newIndiv = new MetamorphicIndividual(genotypeSupport, 0);
                newIndiv.populateIndividual(randomGenerator, 1);
                myPop.saveIndividual(newIndiv);
            }
            MetamorphicIndividual initial = new MetamorphicIndividual(genotypeSupport, -1);
            initial.setJavaPath(Path.of(config.program.getDataDirectoryPath().toString(), genotypeSupport.getInitialDataset()).toString());
            double initialFitness = initial.getFitness();

            ArrayList<Double> fitnesses = new ArrayList<>();

            int generationCount = 0;
            while (generationCount <= config.genetic.getMaxGeneLength() && timeDiffSmaller(start)) {
                LocalTime generationStart = LocalTime.now();
                ArrayList<Double> generationFitness = new ArrayList<>();
                for (int i = 0; i < config.genetic.getPopSize(); i++) {
                    generationFitness.add(myPop.getIndividual(i).get().getFitness());
                    fitnesses.add(myPop.getIndividual(i).get().getFitness());
                }

                generationCount++;
                logger.info("Starting Generation " + generationCount);

                algorithm.checkPareto(myPop);
                // Write all current individuals to their respective json files
                for (var individual : myPop.getIndividuals()) {
                    individual.writeIndividualJSON();
                }

                myPop = algorithm.nextGeneration(myPop);
                LocalTime generationFinished = LocalTime.now();

                logGenerationInfo(myPop,generationCount,initialFitness,generationStart,generationFinished);
            }
            if (myPop.getAverageSize() >= config.genetic.getMaxGeneLength())
                logger.info("Terminated because max gene size reached.");
            else
                logger.info("Terminated because total minutes reached max.");

            logger.info("Program finished");
            algorithm.checkPareto(myPop);
            logResultsAfterSearch();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the custom simple genetic algorithm created for variable length chromosomes.
     */
    public static void runSimpleGA() {
        RandomGenerator random = new SplittableRandom(config.program.getSeed());
        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(config.genetic, genotypeSupport, paretoFront, random);

        logger.info("Using Genetic Search");
        LocalTime start = LocalTime.now();
        boolean converged = false;

        // Create an initial population
        try {
            MetamorphicPopulation myPop =
                    new MetamorphicPopulation(genotypeSupport, 0);
            myPop.initialize(config.genetic.getPopSize(),(int) config.genetic.getGrowthFactor(),random);
            MetamorphicIndividual best = new MetamorphicIndividual(genotypeSupport, -1);
            best.setJavaPath(Path.of(config.program.getDataDirectoryPath().toString(), genotypeSupport.getInitialDataset()).toString());
            double bestFitness = best.getFitness();
            logger.info(String.format("Initial (best) fitness without transformations: %.4f",bestFitness));
            double initialFitness = bestFitness;

            // Evolve our population until we reach an optimum solution
            int generationCount = 0;
            int steadyGens = 0;
            while (!converged && timeDiffSmaller(start)) {
                LocalTime generationStart = LocalTime.now();
                generationCount++;
                logger.info("Starting Generation " + generationCount);
                if (isFitter(myPop, bestFitness)) {
                    bestFitness = myPop.getFittest().get().getFitness();
                    best = myPop.getFittest().get();
                    steadyGens = 0;
                } else
                    steadyGens++;
                if (steadyGens > config.genetic.getMaxSteadyGenerations())
                    converged = true;
                geneticAlgorithm.checkPareto(myPop);
                if (paretoFront.getFrontier().isEmpty()) {
                    logger.debug("Current Pareto set is empty");
                } else {
                    logger.debug("Current Pareto set = " + paretoFront.displayPareto());
                }
                // Write all current individuals to their respective json files
                myPop.getIndividuals().stream().forEach(MetamorphicIndividual::writeIndividualJSON);

                myPop = geneticAlgorithm.evolvePopulation(myPop);

                var generationFinished = LocalTime.now();
                logGenerationInfo(myPop,generationCount,initialFitness,generationStart,generationFinished);

            }
            logger.info("Program finished");
            if (converged)
                logger.info("Terminated because too many steady generations.");
            else
                logger.info("Terminated because total minutes surpassed max.");

            geneticAlgorithm.checkPareto(myPop);
            logResultsAfterSearch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static private void logGenerationInfo(MetamorphicPopulation pop, int generation, double initialFitness, LocalTime start, LocalTime end){
        logger.info("Generation " + generation + " finished after " + Duration.between(start, end).getSeconds() + "s");
        if (pop.getFittest().isPresent()){

            double gene_fitness = pop.getFittest().get().getFitness();
            logger.info(String.format("Fittest: %s with %.4f (+/- %.4f)",
                    pop.getFittest().get().hexHash(),gene_fitness,Math.abs(gene_fitness-initialFitness)) );

            logger.debug("Fittest Gene: " + pop.getFittest().toString());
        } else {
            logger.warn("Generation " + generation +
                    " had no fittest element, Population had " + pop.getIndividuals().size() +
                    " elements with " + pop.getAverageSize() + " average transformations");
        }

        logger.trace("Population of generation " + generation + " = " + pop.toString());
    }


    /**
     * Log some meta-results from times taken.
     *
     * @throws IOException exception when the program can't access the file.
     */
    private static void logResultsAfterSearch() throws IOException {
        long code2vecTime = genotypeSupport.getTotalCode2vevTime();
        int code2vecSec = (int) (code2vecTime % 60);
        int code2vecMin = (int) ((code2vecTime / 60) % 60);
        logger.info("Total time spent on Code2Vec was " + code2vecMin + " minutes and " + code2vecSec + " seconds." + "\n");

        long transitionTime = genotypeSupport.getTotalTransformationTime();
        int transitionSec = (int) (transitionTime % 60);
        int transitionMin = (int) ((transitionTime / 60) % 60);
        logger.info("Total time spent on Transformation was " + transitionMin + " minutes and " + transitionSec + " seconds." + "\n");
    }

    /**
     * Determine whether a population is fitter than the current best.
     *
     * @param pop  the population.
     * @param best the current best.
     * @return whether the population is fitter.
     */
    public static boolean isFitter(MetamorphicPopulation pop, double best) {
        // If the population is empty, it cannot be fitter.
        if (pop.getIndividuals().isEmpty())
                return false;

        if (cache.doMaximize()) {
            return pop.getFittest().get().getFitness() > best;
        } else {
            return pop.getFittest().get().getFitness() < best;
        }
    }

    /**
     * Calculate whether the amount of minutes between a start date and now is less than the threshold.
     *
     * @param start the start time.s
     * @return whether the difference is less than the threshold.
     */
    public static boolean timeDiffSmaller(LocalTime start) {
        long minutesBetween = Duration.between(start, LocalTime.now()).getSeconds() / 60;
        return minutesBetween < config.program.getMaxTimeInMin();
    }

    /**
     * This is meant for Testing Only!
     *
     * @param config
     */
    protected static void setConfig(Configuration config) {
        Main.config = config;
    }

    /**
     * This is meant for Testing only!
     *
     * @param cache
     */
    protected static void setCache(MetricCache cache) {
        Main.cache = cache;
    }
}
