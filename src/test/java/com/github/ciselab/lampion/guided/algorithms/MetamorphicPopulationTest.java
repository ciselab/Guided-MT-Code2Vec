package com.github.ciselab.lampion.guided.algorithms;

import com.github.ciselab.lampion.guided.helpers.StubMetric;
import com.github.ciselab.lampion.guided.configuration.Configuration;
import com.github.ciselab.lampion.guided.metric.Metric;
import com.github.ciselab.lampion.guided.support.FileManagement;
import com.github.ciselab.lampion.guided.support.GenotypeSupport;
import com.github.ciselab.lampion.guided.support.MetricCache;

import java.util.HashMap;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static com.github.ciselab.lampion.guided.helpers.Utils.makeEmptyCache;
import static org.junit.jupiter.api.Assertions.*;

public class MetamorphicPopulationTest {

    @AfterEach
    public void after() {
        var config = new Configuration();
        FileManagement.removeOtherDirs(config.program.getDataDirectoryPath().toString());
    }

    @Test
    public void createPopulation_withInitializeTest() {
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicPopulation population = new MetamorphicPopulation(support, 0);
        population.initialize(3,r);
        assertEquals(population.size(), 3);
        for(int i = 0; i < population.size(); i++) {
            assertNotNull(population.getIndividual(i));
        }
    }

    @Test
    public void populationStringTest() {
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicPopulation population = new MetamorphicPopulation(support, 0);
        population.initialize(3,r);

        assertTrue(population.toString().contains("MetamorphicPopulation{"));
    }

    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testMetamorphicPopulation_withMetricsCached_getFittest_ShouldReturnHighestFitness_WhenMinimizing(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,3);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,3);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);
        stub.valuesToReturn.put(a,0.75);
        stub.valuesToReturn.put(b,0.50);
        cache.addMetric(stub);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub,0.75);
        cache.putMetricResults(a,aMetrics);

        HashMap<Metric,Double> bMetrics = new HashMap<>();
        bMetrics.put(stub,0.5);
        cache.putMetricResults(b,bMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support,0);


        testObject.saveIndividual(a);
        testObject.saveIndividual(b);

        var result = testObject.getFittest();
        assertTrue(result.isPresent());
        assertEquals(a,result.get());
        assertEquals(0.75,result.get().getFitness(),0.001);
    }


    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testMetamorphicPopulation_withMetricsCached_getFittest_ShouldReturnHighestFitness_WhenMinimizing_variantC(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,4);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,2);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);
        stub.valuesToReturn.put(a,0.75);
        stub.valuesToReturn.put(b,0.0);
        cache.addMetric(stub);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub,0.75);
        cache.putMetricResults(a,aMetrics);

        HashMap<Metric,Double> bMetrics = new HashMap<>();
        bMetrics.put(stub,0.5);
        cache.putMetricResults(b,bMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support,0);


        testObject.saveIndividual(a);
        testObject.saveIndividual(b);

        var result = testObject.getFittest();
        assertTrue(result.isPresent());
        assertEquals(a,result.get());
        assertEquals(0.75,result.get().getFitness(),0.001);
    }


    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testMetamorphicPopulation_withMetricsCached_getFittest_ShouldReturnHighestFitness_WhenMinimizing_VariantD(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,4);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,10);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);
        stub.valuesToReturn.put(a,0.75);
        stub.valuesToReturn.put(b,0.0);
        cache.addMetric(stub);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub,0.75);
        cache.putMetricResults(a,aMetrics);

        HashMap<Metric,Double> bMetrics = new HashMap<>();
        bMetrics.put(stub,0.5);
        cache.putMetricResults(b,bMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support,0);


        testObject.saveIndividual(a);
        testObject.saveIndividual(b);

        var result = testObject.getFittest();
        assertTrue(result.isPresent());
        assertEquals(a,result.get());
        assertEquals(0.75,result.get().getFitness(),0.001);
    }

    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testMetamorphicPopulation_withMetricsCached_getFittest_ShouldReturnHighestFitness_WhenMinimizing_VariantB(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,3);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,3);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);
        stub.valuesToReturn.put(a,0.9);
        stub.valuesToReturn.put(b,0.2);
        cache.addMetric(stub);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub,0.9);
        cache.putMetricResults(a,aMetrics);

        HashMap<Metric,Double> bMetrics = new HashMap<>();
        bMetrics.put(stub,0.2);
        cache.putMetricResults(b,bMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support,0);


        testObject.saveIndividual(a);
        testObject.saveIndividual(b);

        var result = testObject.getFittest();
        assertTrue(result.isPresent());
        assertEquals(a,result.get());
        assertEquals(0.9,result.get().getFitness(),0.001);
    }

    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testMetamorphicPopulation_withNegativeMetricsCached_getFittest_ShouldReturnHighestFitness_WhenMaximizing(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,3);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,3);

        StubMetric stub = new StubMetric();
        stub.setWeight(-1);
        stub.valuesToReturn.put(a,0.75);
        stub.valuesToReturn.put(b,0.50);
        cache.addMetric(stub);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub,0.75);
        cache.putMetricResults(a,aMetrics);
        HashMap<Metric,Double> bMetrics = new HashMap<>();
        bMetrics.put(stub,0.5);
        cache.putMetricResults(b,bMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);


        testObject.saveIndividual(a);
        testObject.saveIndividual(b);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(b,result.get());
        assertEquals(0.5,result.get().getFitness(),0.001);
    }

    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testMetamorphicPopulation_withNegativeMetricsCached_getFittest_ShouldReturnHighestFitness_WhenMaximizing_VariantB(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,3);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,3);

        StubMetric stub = new StubMetric();
        stub.setWeight(-1);
        stub.valuesToReturn.put(a,0.7);
        stub.valuesToReturn.put(b,0.2);
        cache.addMetric(stub);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub,0.7);
        cache.putMetricResults(a,aMetrics);
        HashMap<Metric,Double> bMetrics = new HashMap<>();
        bMetrics.put(stub,0.2);
        cache.putMetricResults(b,bMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);


        testObject.saveIndividual(a);
        testObject.saveIndividual(b);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(b,result.get());
        assertEquals(1-0.2,result.get().getFitness(),0.001);
    }

    @Tag("Regression")
    @Tag("Integration")
    @ParameterizedTest
    @ValueSource(doubles = {0.0,0.1, 0.25, 0.5, 0.9, 1.0})
    public void testGetFittest_OneElement_ShouldReturnSpecifiedFitness_WhenMinimizing(double fitness){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);
        stub.valuesToReturn.put(a,fitness);
        cache.addMetric(stub);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub,fitness);
        cache.putMetricResults(a,aMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);


        testObject.saveIndividual(a);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(fitness,result.get().getFitness(),0.001);
    }

    @Tag("Regression")
    @Tag("Integration")
    @ParameterizedTest
    @ValueSource(doubles = {0.0,0.1, 0.25, 0.5,0.9,1.0})
    public void testGetFittest_OneElement_ShouldReturnSpecifiedFitness_WhenMaximizing(double fitness){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        StubMetric stub = new StubMetric();
        stub.setWeight(-1);
        stub.valuesToReturn.put(a,fitness);
        cache.addMetric(stub);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub,fitness);
        cache.putMetricResults(a,aMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);


        testObject.saveIndividual(a);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(1-fitness,result.get().getFitness(),0.001);
    }

    @Tag("Regression")
    @Tag("Integration")
    @ParameterizedTest
    @ValueSource(doubles = {0.0,0.1, 0.25, 0.5,0.9,1.0})
    public void testGetFittest_OneElement_WeightIsPoint5_ShouldReturnHalfOfSpecifiedFitness_WhenMaximizing(double fitness){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        StubMetric stub = new StubMetric();
        stub.setWeight(-0.5);
        stub.valuesToReturn.put(a,fitness);
        cache.addMetric(stub);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub,fitness);
        cache.putMetricResults(a,aMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);


        testObject.saveIndividual(a);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        Double expectedFitness = (1.0/2.0) - (fitness/2);

        assertEquals(expectedFitness,result.get().getFitness(),0.001);
    }

    @Tag("Regression")
    @Tag("Integration")
    @ParameterizedTest
    @ValueSource(doubles = {0.0,0.1, 0.25, 0.5,0.9,1.0})
    public void testGetFittest_OneElement_WeightIsPoint5_ShouldReturnHalfOfSpecifiedFitness_WhenMinimizing(double fitness){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        StubMetric stub = new StubMetric();
        stub.setWeight(0.5);
        stub.valuesToReturn.put(a,fitness);
        cache.addMetric(stub);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub,fitness);
        cache.putMetricResults(a,aMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);


        testObject.saveIndividual(a);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals((fitness)/2,result.get().getFitness(),0.001);
    }

    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testGetFittest_WithMixedMetrics_FitnessIsEvenedOut(){
        /**
         * If we have Metric A with weight 0.5 and Value 1
         * and Metric B with weight -0.5 and 1
         * We expect an output of 0.5 (That is, it did max in one and least in other)
         */
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        StubMetric stub1 = new StubMetric();
        stub1.setWeight(0.5);
        stub1.valuesToReturn.put(a,1.0);
        cache.addMetric(stub1);

        StubMetric stub2 = new StubMetric();
        stub2.setWeight(-0.5);
        stub2.valuesToReturn.put(a,1.0);
        cache.addMetric(stub2);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub1,1.0);
        aMetrics.put(stub2,1.0);
        cache.putMetricResults(a,aMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);
        testObject.saveIndividual(a);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(0.5,result.get().getFitness(),0.001);
    }

    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testGetFittest_WithMixedMetrics_FitnessIsMaxed(){
        /**
         * If we have Metric A with weight 0.5 and Value 1
         * and Metric B with weight -0.5 and 0
         * We expect an output of 1.0
         * (Both Metrics are in their co-respective best value)
         */
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        StubMetric stub1 = new StubMetric();
        stub1.setWeight(0.5);
        stub1.valuesToReturn.put(a,1.0);
        cache.addMetric(stub1);

        StubMetric stub2 = new StubMetric();
        stub2.setWeight(-0.5);
        stub2.valuesToReturn.put(a,0.0);
        cache.addMetric(stub2);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub1,1.0);
        aMetrics.put(stub2,0.0);
        cache.putMetricResults(a,aMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);
        testObject.saveIndividual(a);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(1.0,result.get().getFitness(),0.001);
    }

    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testGetFittest_WithMixedMetrics_FitnessIsMaxed_VariantB(){
        /**
         * If we have Metric A with weight 0.5 and Value 1
         * and Metric B with weight -0.5 and 0
         * We expect an output of 1.0
         * (Both Metrics are in their co-respective best value)
         * Variant B: Metrics flipped, otherwise same values
         */
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        StubMetric stub1 = new StubMetric();
        stub1.setWeight(-0.5);
        stub1.valuesToReturn.put(a,0.0);
        cache.addMetric(stub1);

        StubMetric stub2 = new StubMetric();
        stub2.setWeight(0.5);
        stub2.valuesToReturn.put(a,1.0);
        cache.addMetric(stub2);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub1,0.0);
        aMetrics.put(stub2,1.0);
        cache.putMetricResults(a,aMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);
        testObject.saveIndividual(a);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(1.0,result.get().getFitness(),0.001);
    }


    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testGetFittest_PositiveAndNegativeMetricResults_AreEvenedOut(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        StubMetric stub1 = new StubMetric();
        stub1.setWeight(0.5);
        stub1.valuesToReturn.put(a,0.0);
        cache.addMetric(stub1);

        StubMetric stub2 = new StubMetric();
        stub2.setWeight(0.5);
        stub2.valuesToReturn.put(a,1.0);
        cache.addMetric(stub2);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub1,0.0);
        aMetrics.put(stub2,1.0);
        cache.putMetricResults(a,aMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);
        testObject.saveIndividual(a);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(0.5,result.get().getFitness(),0.001);
    }

    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testGetFittest_PositiveAndNegativeMetricResults_AreEvenedOut_VariantB(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        StubMetric stub1 = new StubMetric();
        stub1.setWeight(-0.5);
        stub1.valuesToReturn.put(a,0.0);
        cache.addMetric(stub1);

        StubMetric stub2 = new StubMetric();
        stub2.setWeight(-0.5);
        stub2.valuesToReturn.put(a,1.0);
        cache.addMetric(stub2);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub1,0.0);
        aMetrics.put(stub2,1.0);
        cache.putMetricResults(a,aMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);
        testObject.saveIndividual(a);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(0.5,result.get().getFitness(),0.001);
    }

    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testGetFittest_With3MixedMetrics_FitnessIsMaxed(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        StubMetric stub1 = new StubMetric();
        stub1.setWeight(-0.333);
        stub1.valuesToReturn.put(a,0.0);
        cache.addMetric(stub1);

        StubMetric stub2 = new StubMetric();
        stub2.setWeight(0.333);
        stub2.valuesToReturn.put(a,1.0);
        cache.addMetric(stub2);

        StubMetric stub3 = new StubMetric();
        stub3.setWeight(0.333);
        stub3.valuesToReturn.put(a,1.0);
        cache.addMetric(stub3);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub1,0.0);
        aMetrics.put(stub2,1.0);
        aMetrics.put(stub3,1.0);
        cache.putMetricResults(a,aMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);
        testObject.saveIndividual(a);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(1.0,result.get().getFitness(),0.01);
    }

    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testGetFittest_With3MixedMetrics_FitnessIsMaxed_VariantB(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        StubMetric stub1 = new StubMetric();
        stub1.setWeight(0.333);
        stub1.valuesToReturn.put(a,1.0);
        cache.addMetric(stub1);

        StubMetric stub2 = new StubMetric();
        stub2.setWeight(-0.333);
        stub2.valuesToReturn.put(a,0.0);
        cache.addMetric(stub2);

        StubMetric stub3 = new StubMetric();
        stub3.setWeight(-0.333);
        stub3.valuesToReturn.put(a,0.0);
        cache.addMetric(stub3);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub1,1.0);
        aMetrics.put(stub2,0.0);
        aMetrics.put(stub3,0.0);
        cache.putMetricResults(a,aMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);
        testObject.saveIndividual(a);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(1.0,result.get().getFitness(),0.01);
    }

    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testGetFittest_With3MixedMetrics_OneIsPoor_FitnessEvened(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        StubMetric stub1 = new StubMetric();
        stub1.setWeight(0.333);
        stub1.valuesToReturn.put(a,0.0);
        cache.addMetric(stub1);

        StubMetric stub2 = new StubMetric();
        stub2.setWeight(-0.333);
        stub2.valuesToReturn.put(a,0.0);
        cache.addMetric(stub2);

        StubMetric stub3 = new StubMetric();
        stub3.setWeight(-0.333);
        stub3.valuesToReturn.put(a,0.0);
        cache.addMetric(stub3);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub1,0.0);
        aMetrics.put(stub2,0.0);
        aMetrics.put(stub3,0.0);
        cache.putMetricResults(a,aMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);
        testObject.saveIndividual(a);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(0.6666,result.get().getFitness(),0.01);
    }


    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testGetFittest_WithMixedMetrics_WorstValues_FitnessIsMin(){
        /**
         * If we have Metric A with weight 0.5 and Value 1
         * and Metric B with weight -0.5 and 0
         * We expect an output of 1.0
         * (Both Metrics are in their co-respective best value)
         */
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        StubMetric stub1 = new StubMetric();
        stub1.setWeight(-0.5);
        stub1.valuesToReturn.put(a,1.0);
        cache.addMetric(stub1);

        StubMetric stub2 = new StubMetric();
        stub2.setWeight(0.5);
        stub2.valuesToReturn.put(a,0.0);
        cache.addMetric(stub2);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub1,1.0);
        aMetrics.put(stub2,0.0);
        cache.putMetricResults(a,aMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);
        testObject.saveIndividual(a);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(0.0,result.get().getFitness(),0.001);
    }



    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testGetFittest_TwoElementsWithSameFitness_ReturnShorter(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,10);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);
        stub.valuesToReturn.put(a,0.5);
        stub.valuesToReturn.put(b,0.5);
        cache.addMetric(stub);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub,0.5);
        cache.putMetricResults(a,aMetrics);
        HashMap<Metric,Double> bMetrics = new HashMap<>();
        bMetrics.put(stub,0.5);
        cache.putMetricResults(b,bMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);


        testObject.saveIndividual(a);
        testObject.saveIndividual(b);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(a,result.get());
    }

    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testGetFittest_TwoElementsWithSameFitness_ReturnShorter_VariantB(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,10);

        StubMetric stub = new StubMetric();
        stub.setWeight(-1);
        stub.valuesToReturn.put(a,0.5);
        stub.valuesToReturn.put(b,0.5);
        cache.addMetric(stub);

        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(stub,0.5);
        cache.putMetricResults(a,aMetrics);
        HashMap<Metric,Double> bMetrics = new HashMap<>();
        bMetrics.put(stub,0.5);
        cache.putMetricResults(b,bMetrics);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);


        testObject.saveIndividual(a);
        testObject.saveIndividual(b);

        var result = testObject.getFittest();

        assertTrue(result.isPresent());
        assertEquals(a,result.get());
    }

    @Test
    public void testMetamorphicPopulation_savingIndividual_shouldHaveIndividual(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);

        testObject.saveIndividual(a);

        assertEquals(1,testObject.getIndividuals().size());
    }

    @Test
    public void testSize_savingIndividual_sizeIsIncreased(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);

        testObject.saveIndividual(a);

        assertEquals(1,testObject.size());
    }

    @Test
    public void testInitialize_isDeterministic(){
        Random r1 = new Random(5);
        Random r2 = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);
        testObject.initialize(10,r1);

        MetamorphicPopulation comparison = new MetamorphicPopulation(support);
        comparison.initialize(10,r2);

        assertEquals(testObject.size(),comparison.size());

        for(int i = 0; i< testObject.size();i++){
            assertEquals(testObject.getIndividual(i),comparison.getIndividual(i));
        }
    }

    @ParameterizedTest
    @ValueSource(doubles = {5.0,10.0,95.0,200.0})
    public void testInitialize_withGrowthFactor_shouldHaveBiggerElements(double growthrate){
        Random random = new Random(100);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);
        testObject.initialize(100,(int) growthrate,random);

        MetamorphicPopulation comparison = new MetamorphicPopulation(support);
        comparison.initialize(100,((int) growthrate/2),random);

        assertTrue(comparison.getAverageSize() < testObject.getAverageSize());
    }

    @ParameterizedTest
    @ValueSource(ints = {1,3,10,20,50,100,250})
    public void testInitialize_shouldHaveInitializationSize(int size){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);

        testObject.initialize(size,r);

        assertEquals(size,testObject.size());
    }

    @Test
    public  void testInitialize_zeroValue_throwsError(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);

        assertThrows(IllegalArgumentException.class, () -> testObject.initialize(0,r));
    }
    @Test
    public  void testInitialize_negativeValue_throwsError(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);

        assertThrows(IllegalArgumentException.class, () -> testObject.initialize(-1,r));
    }

    @Tag("Regression")
    @Test
    public void testGetAverageSize_emptyPopulation_shouldGive0(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);

        assertEquals(0.0,testObject.getAverageSize(),0.0001);
    }

    @Tag("Regression")
    @RepeatedTest(3)
    public void testInitialize_withGrowthFactor1_shouldNotThrowError(){
        // There was an issue that we cannot use GrowthFactor 1, as that would mess with math.nextint(1,1)
        // So we just treat it as 1 in that case
        Random random = new Random();
        assertThrows(Exception.class, () -> random.nextInt(1,1));

        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);
        testObject.initialize(10,1,random);

        assertEquals(1.0, testObject.getAverageSize(),0.001);
    }

    @Tag("Probabilistic")
    @Tag("Regression")
    @ParameterizedTest
    @ValueSource(doubles = {3.0,5.0,7.0})
    public void testGetAverageSize_initializedPopulation_shouldHaveAverageSize(double growth){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicPopulation testObject = new MetamorphicPopulation(support);
        testObject.initialize(100, (int) growth,r);

        assertTrue(testObject.getAverageSize()>1);
    }
}
