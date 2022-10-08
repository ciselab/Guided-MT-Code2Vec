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

        // For processing it is important to always have the same format (e.g. 1.00 instead of 1,00)
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

        // Create an initial population
        try {
            FileWriter resultWriter = new FileWriter(logDir + "results.txt");
            MetamorphicPopulation myPop =
                    new MetamorphicPopulation(genotypeSupport, 0);
            for (int i = 0; i < config.genetic.getPopSize(); i++) {
                MetamorphicIndividual newIndiv = new MetamorphicIndividual(genotypeSupport, 0);
                newIndiv.populateIndividual(randomGenerator, 1);
                myPop.saveIndividual(newIndiv);
            }
            MetamorphicIndividual initial = new MetamorphicIndividual(genotypeSupport, -1);
            initial.setJavaPath(Path.of(config.program.getDataDirectoryPath().toString(), genotypeSupport.getInitialDataset()).toString());
            writeInitialPopulationResults(resultWriter, myPop, initial);

            ArrayList<Double> fitnesses = new ArrayList<>();

            int generationCount = 0;
            while (myPop.getAverageSize() <= config.genetic.getMaxGeneLength()) {
                ArrayList<Double> generationFitness = new ArrayList<>();
                for (int i = 0; i < config.genetic.getPopSize(); i++) {
                    generationFitness.add(myPop.getIndividual(i).get().getFitness());
                    fitnesses.add(myPop.getIndividual(i).get().getFitness());
                }

                generationCount++;
                logger.info("Generation " + generationCount);
                resultWriter.write("Generation " + generationCount + " has an average population size" +
                        " of " + myPop.getAverageSize() + "\n");

                algorithm.checkPareto(myPop);
                logger.debug("Current Pareto set = " + paretoFront.displayPareto());

                resultWriter.write("Generation: " + generationCount + ", best: " + getBestForLog(generationFitness) + ", worst: " + getWorstForLog(generationFitness) +
                        ", average: " + getAverageForLog(generationFitness) + ", median: " + getMedianForLog(generationFitness) + "\n");


                logger.info("Generation: " + generationCount + " Fittest: " + myPop.getFittest().get().getFitness() + " Gene:");
                logger.info(myPop.getFittest().toString());

                // Write all current individuals to their respective json files
                for (var individual : myPop.getIndividuals()) {
                    individual.writeIndividualJSON();
                }

                myPop = algorithm.nextGeneration(myPop);
                logger.debug("Population of generation " + generationCount + " = " + myPop);
                resultWriter.write("Population of generation " + generationCount + " = " + myPop +
                        "\n");
            }
            logger.info("Program finished");
            // Report best, worst, average median
            resultWriter.write("At the end of the algorithm the results are: , best: " + getBestForLog(fitnesses) + ", worst: " + getWorstForLog(fitnesses) + ", " +
                    "average: " + getAverageForLog(fitnesses) + ", " + "median: " + getMedianForLog(fitnesses) + "\n");


            algorithm.checkPareto(myPop);
            writeResultsAfterAlgorithm(resultWriter);
            resultWriter.close();
            /*
            logger.info("Clean up other files.");
            FileManagement.removeOtherDirs(FileManagement.dataDir);
            */
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
            FileWriter resultWriter = new FileWriter(logDir + "GA_results.txt");
            MetamorphicPopulation myPop =
                    new MetamorphicPopulation(genotypeSupport, 0);
            myPop.initialize(config.genetic.getPopSize(),(int) config.genetic.getGrowthFactor(),random);
            MetamorphicIndividual best = new MetamorphicIndividual(genotypeSupport, -1);
            best.setJavaPath(Path.of(config.program.getDataDirectoryPath().toString(), genotypeSupport.getInitialDataset()).toString());
            double bestFitness = writeInitialPopulationResults(resultWriter, myPop, best);
            //if(dataPointSpecific)
            //    writeDataSpecificResults(resultWriter, best);

            // Evolve our population until we reach an optimum solution
            int generationCount = 0;
            int steadyGens = 0;
            int averageSizeSum = 0;
            while (!converged && timeDiffSmaller(start)) {
                generationCount++;
                logger.info("Starting Generation " + generationCount);
                resultWriter.write("Generation " + generationCount + " has an average population size" +
                        " of " + myPop.getAverageSize() + "\n");
                averageSizeSum += myPop.getAverageSize();
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

                resultWriter.write("Generation: " + generationCount + ", result: " + myPop.getFittest().get().getFitness() + "\n");
                resultWriter.write("Gene: " + myPop.getFittest() + "\n");

                logger.info("Generation: " + generationCount + " Fittest: " + myPop.getFittest().get().getFitness());
                logger.debug("Fittest Gene: " + myPop.getFittest().toString());

                // Write all current individuals to their respective json files
                myPop.getIndividuals().stream().forEach(MetamorphicIndividual::writeIndividualJSON);

                myPop = geneticAlgorithm.evolvePopulation(myPop);
                logger.debug("Population of generation " + generationCount + " = " + myPop);
                resultWriter.write("Population of generation " + generationCount + " = " + myPop +
                        "\n");
            }
            logger.info("Program finished");
            if (converged)
                logger.info("Terminated because too many steady generations.");
            else
                logger.info("Terminated because total minutes increased max.");
            resultWriter.write("Generation used: " + generationCount + "\n");
            resultWriter.write("Max fitness: " + best.getFitness() + "\n");
            resultWriter.write("Best individual: " + "\n");
            resultWriter.write(best + "\n");

            geneticAlgorithm.checkPareto(myPop);
            writeResultsAfterAlgorithm(resultWriter);

            resultWriter.write("Average population size over entire run was " + averageSizeSum / generationCount);

            resultWriter.close();
            /*
            logger.info("Clean up other files.");
            FileManagement.removeOtherDirs(FileManagement.dataDir);
            */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Write everything we need to know about the initial population and the initial individual in it.
     *
     * @param resultWriter The file writer with which we can write the results.
     * @param myPop        the current metamorphic population.
     * @param best         the best individual of this population.
     * @return the fitness of the best individual.
     * @throws IOException exception when the program can't access the file.
     */
    private static double writeInitialPopulationResults(FileWriter resultWriter, MetamorphicPopulation myPop, MetamorphicIndividual best) throws IOException {
        double bestFitness = best.getFitness();
        logger.debug("Initial population: " + myPop);
        resultWriter.write("Initial population: " + myPop + "\n");
        logger.info("Initial fitness without transformations: " + bestFitness);
        resultWriter.write("Initial fitness without transformations: " + bestFitness + "\n");
        //logger.info("The metric results corresponding to the transformations are: " + Arrays.toString(best.getMetrics()));

        // check best against pareto
        paretoFront.addToParetoOptimum(best);
        return bestFitness;
    }

    /**
     * Write results after algorithm is finished to the correct file.
     *
     * @param resultWriter The file writer with which we can write the results.
     * @throws IOException exception when the program can't access the file.
     */
    private static void writeResultsAfterAlgorithm(FileWriter resultWriter) throws IOException {
        resultWriter.write("Metrics are: " + Arrays.toString(cache.getMetrics().toArray()) + "\n");
        resultWriter.write("Pareto set: " + paretoFront.displayPareto() + "\n");

        long code2vecTime = genotypeSupport.getTotalCode2vevTime();
        int code2vecSec = (int) (code2vecTime % 60);
        int code2vecMin = (int) ((code2vecTime / 60) % 60);
        resultWriter.write("Total time spent on Code2Vec inference was " + code2vecMin + " minutes and " + code2vecSec + " seconds." + "\n");

        long transitionTime = genotypeSupport.getTotalTransformationTime();
        int transitionSec = (int) (transitionTime % 60);
        int transitionMin = (int) ((transitionTime / 60) % 60);
        resultWriter.write("Total time spent on Transformation operations was " + transitionMin + " minutes and " + transitionSec + " seconds." + "\n");
    }

    /**
     * Write the data specific scores to the result file for each metric.
     * @param resultWriter the file writer with which we can write the results.
     * @param individual the individual for which we want to write the results.
     * @throws IOException exception when the program can't access the file.

    private static void writeDataSpecificResults(FileWriter resultWriter, MetamorphicIndividual individual) throws IOException {
    Map<String, List<Float>> scores = individual.getScoresList();
    for(String metric: scores.keySet()) {
    String results = "{" + metric + ": " + Arrays.toString(scores.get(metric).toArray()) + "}";
    resultWriter.write(results + '\n');
    }
    }
     */

    /**
     * Determine whether a population is fitter than the current best.
     *
     * @param pop  the population.
     * @param best the current best.
     * @return whether the population is fitter.
     */
    public static boolean isFitter(MetamorphicPopulation pop, double best) {
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
     * Get median of list of doubles.
     *
     * @param values the list of double values.
     * @return the median of the list.
     */
    public static double getMedianForLog(ArrayList<Double> values) {
        Collections.sort(values);
        if (values.size() % 2 == 1)
            return values.get((values.size() + 1) / 2 - 1);
        else {
            double lower = values.get(values.size() / 2 - 1);
            double upper = values.get(values.size() / 2);

            return (lower + upper) / 2.0;
        }
    }

    /**
     * Get average of list of doubles.
     *
     * @param values the list of double values.
     * @return the average of the list.
     */
    public static double getAverageForLog(ArrayList<Double> values) {
        double sum = 0;
        for (double i : values) {
            sum += i;
        }
        return sum / values.size();
    }

    /**
     * Get worst value of list.
     *
     * @param values the list.
     * @return the worst value.
     */
    public static double getWorstForLog(ArrayList<Double> values) {
        Collections.sort(values);
        if (cache.doMaximize())
            return values.get(0);
        else
            return values.get(values.size() - 1);
    }

    /**
     * Get best value of list.
     *
     * @param values the list.
     * @return the best value.
     */
    public static double getBestForLog(ArrayList<Double> values) {
        Collections.sort(values);
        if (cache.doMaximize())
            return values.get(values.size() - 1);
        else
            return values.get(0);
    }

    /**
     * This is meant for Testing Only!
     *
     * @param config
     */
    public static void setConfig(Configuration config) {
        Main.config = config;
    }

    /**
     * This is meant for Testing only!
     *
     * @param cache
     */
    public static void setCache(MetricCache cache) {
        Main.cache = cache;
    }
}
