package com.github.ciselab.lampion.guided.algorithms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.github.ciselab.lampion.guided.algorithms.RandomAlgorithm;
import com.github.ciselab.lampion.guided.configuration.Configuration;
import com.github.ciselab.lampion.guided.support.FileManagement;
import com.github.ciselab.lampion.guided.support.GenotypeSupport;
import com.github.ciselab.lampion.guided.support.MetricCache;
import com.github.ciselab.lampion.guided.support.ParetoFront;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;

public class RandomAlgorithmTest {


    @AfterEach
    public void after() {

        var config = new Configuration();
        FileManagement.removeOtherDirs(config.program.getDataDirectoryPath().toString());
    }

    @Test
    public void InitializeParametersTest() {
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        var randomAlgorithm = new RandomAlgorithm(support, new ParetoFront(cache));

        RandomGenerator randomGenerator = new SplittableRandom(101010);
        randomAlgorithm.initializeParameters(randomGenerator);
    }

    @Test
    public void testRandomAlgorithm_InputPopStaysUnchanged(){
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        Random r = new Random(5);
        var inputPop = new MetamorphicPopulation(support);
        inputPop.initialize(10,10,r);

        Random r2 = new Random(5);
        var inputComparison = new MetamorphicPopulation(support);
        inputComparison.initialize(10,10,r2);

        assertEquals(inputComparison.getIndividuals(),inputPop.getIndividuals());

        RandomGenerator random1 = new Random(100);
        var testObject = new RandomAlgorithm(support, new ParetoFront(cache));
        testObject.initializeParameters(random1);

        var outputPop = testObject.nextGeneration(inputPop);

        assertEquals(inputComparison.getIndividuals(),inputPop.getIndividuals());
    }


    @Test
    public void testRandomAlgorithm_GenerationIsIncreased(){
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        Random r = new Random(5);
        var inputPop = new MetamorphicPopulation(support);
        inputPop.initialize(10,10,r);

        RandomGenerator random1 = new Random(100);
        var testObject = new RandomAlgorithm(support, new ParetoFront(cache));
        testObject.initializeParameters(random1);

        var outputPop = testObject.nextGeneration(inputPop);

        assertEquals(1,outputPop.getGeneration());
    }

    @ParameterizedTest
    @ValueSource(ints = {5,10,1996})
    public void testRandomAlgorithm_isDeterministic(int seed){
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        Random r = new Random(5);
        var inputPop = new MetamorphicPopulation(support);
        inputPop.initialize(10,10,r);

        RandomGenerator random1 = new Random(seed);
        var testObject = new RandomAlgorithm(support, new ParetoFront(cache));
        testObject.initializeParameters(random1);

        RandomGenerator random2 = new Random(seed);
        var comparison = new RandomAlgorithm(support, new ParetoFront(cache));
        comparison.initializeParameters(random2);

        var outputPop1 = testObject.nextGeneration(inputPop);
        var outputPop2 = comparison.nextGeneration(inputPop);

        assertEquals(outputPop1.getIndividuals(),outputPop2.getIndividuals());
    }
}
