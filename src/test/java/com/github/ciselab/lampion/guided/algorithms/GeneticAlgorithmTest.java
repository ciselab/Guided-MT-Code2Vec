package com.github.ciselab.lampion.guided.algorithms;

import com.github.ciselab.lampion.guided.algorithms.GeneticAlgorithm;
import com.github.ciselab.lampion.guided.configuration.Configuration;
import com.github.ciselab.lampion.guided.helpers.StubMetric;
import com.github.ciselab.lampion.guided.metric.Metric;
import com.github.ciselab.lampion.guided.support.GenotypeSupport;
import com.github.ciselab.lampion.guided.support.MetricCache;
import com.github.ciselab.lampion.guided.support.ParetoFront;
import org.junit.Ignore;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Random;
import java.util.SplittableRandom;
import java.util.stream.IntStream;

import static com.github.ciselab.lampion.guided.helpers.Utils.makeEmptyCache;
import static com.github.ciselab.lampion.guided.helpers.Utils.storeIndividualForCaching;
import static org.junit.jupiter.api.Assertions.*;


public class GeneticAlgorithmTest {

    @Test
    public void initializeParametersTest() {
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);
        GeneticAlgorithm geneticAlgorithm = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),new Random(1));    }


    @Tag("Probabilistic")
    @Tag("Seeded")
    @ParameterizedTest
    @ValueSource(ints = {3,5,10})
    public  void testMutate_withHighGrowthRate_ShouldHaveOnAverageHighElements(int growthFactor){
        var r = new SplittableRandom(10);
        var config = new Configuration();
        config.genetic.setGrowthFactor(growthFactor);
        config.genetic.setIncreaseSizeRate(1);
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        GeneticAlgorithm ga = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),r);

        var averageIncrease = IntStream.range(0,100)
                .map(t -> {
                    var mi = new MetamorphicIndividual(support,0);
                    ga.mutate(mi);
                    return mi.getLength();
                }).average();

        assertTrue(averageIncrease.isPresent());
        assertTrue(averageIncrease.getAsDouble()>1.5);

        assertEquals(growthFactor,averageIncrease.getAsDouble(),growthFactor);
    }

    @Tag("Probabilistic")
    @Tag("Seeded")
    @ParameterizedTest
    @ValueSource(ints = {20,40,60,100})
    public  void testMutate_withVeryHighGrowthRate_ShouldHaveOnAverageHighElements(int growthFactor){
        var r = new SplittableRandom(10);
        var config = new Configuration();
        config.genetic.setGrowthFactor(growthFactor);
        config.genetic.setIncreaseSizeRate(1);
        config.genetic.setMaxGeneLength(200);
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        GeneticAlgorithm ga = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),r);

        var averageIncrease = IntStream.range(0,200)
                .map(t -> {
                    var mi = new MetamorphicIndividual(support,0);
                    ga.mutate(mi);
                    return mi.getLength();
                }).average();

        assertTrue(averageIncrease.isPresent());
        assertTrue(averageIncrease.getAsDouble()>1.5);

        assertEquals(growthFactor,averageIncrease.getAsDouble(),growthFactor);
    }


    @Tag("Probabilistic")
    @Tag("Seeded")
    @ParameterizedTest
    @ValueSource(ints = {20,40,60,100})
    public  void testMutate_withVeryHighGrowthRate_butMaxLength10_ShouldHaveOnAverage10Elements(int growthFactor){
        var r = new SplittableRandom(10);
        var config = new Configuration();
        config.genetic.setGrowthFactor(growthFactor);
        config.genetic.setIncreaseSizeRate(1);
        config.genetic.setMaxGeneLength(10);
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        GeneticAlgorithm ga = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),r);

        var anyAboveMaxSize = IntStream.range(0,250)
                .map(t -> {
                    var mi = new MetamorphicIndividual(support,0);
                    ga.mutate(mi);
                    return mi.getLength();
                }).anyMatch(x -> x > 10);

        assertFalse(anyAboveMaxSize);
    }


    @Tag("Probabilistic")
    @Tag("Seeded")
    @RepeatedTest(3)
    public  void testMutate_withHighGrowthRate_ShouldBeHigherThanOneMutation(){
        var r = new SplittableRandom(10);
        var config = new Configuration();
        config.genetic.setGrowthFactor(3);
        config.genetic.setIncreaseSizeRate(1);
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        GeneticAlgorithm ga = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),r);

        var averageIncrease = IntStream.range(0,250)
                .map(t -> {
                    var mi = new MetamorphicIndividual(support,0);
                    ga.mutate(mi);
                    return mi.getLength();
                }).average();

        assertTrue(averageIncrease.isPresent());
        assertTrue(averageIncrease.getAsDouble()>1.5);
    }


    @Tag("Probabilistic")
    @Tag("Seeded")
    @Tag("Regression")
    @ParameterizedTest
    @ValueSource(ints = {3,5,10})
    public  void testMutate_HasElementsSmallerThanGrowthRate(int growthFactor){
        var r = new SplittableRandom(10);
        var config = new Configuration();
        config.genetic.setGrowthFactor(growthFactor);
        config.genetic.setIncreaseSizeRate(1);
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        GeneticAlgorithm ga = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),r);

        var anyAboveGrowthRate = IntStream.range(0,250)
                .map(t -> {
                    var mi = new MetamorphicIndividual(support,0);
                    ga.mutate(mi);
                    return mi.getLength();
                }).anyMatch(x -> x < growthFactor);

        assertTrue(anyAboveGrowthRate);
    }
    @Tag("Probabilistic")
    @Tag("Seeded")
    @Tag("Regression")
    @ParameterizedTest
    @ValueSource(ints = {3,5,7,9,10,12})
    public  void testMutate_HasElementsBiggerThanGrowthRate(int growthFactor){
        var r = new SplittableRandom(10);
        var config = new Configuration();
        config.genetic.setGrowthFactor(growthFactor);
        config.genetic.setIncreaseSizeRate(1);
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        GeneticAlgorithm ga = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),r);

        var anyAboveGrowthRate = IntStream.range(0,2000)
                .map(t -> {
                    var mi = new MetamorphicIndividual(support,0);
                    ga.mutate(mi);
                    return mi.getLength();
                }).anyMatch(x -> x > growthFactor);

        assertTrue(anyAboveGrowthRate);
    }

    @Test
    public void testCrossover_twoEmptyGenes_returnsTwoEmptyGenes(){
        // This Test mostly checks if there is an exception thrown
        // Which it should not!
        var r = new SplittableRandom(10);
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);

        GeneticAlgorithm ga = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),r);

        var results = ga.crossover(a,b,r);

        assertEquals(2,results.size());
        results.forEach(i -> assertEquals(a,i));
        results.forEach(i -> assertEquals(b,i));
    }


    @Tag("Probabilistic")
    @Tag("Seeded")
    @Test
    public void testCrossover_twoFilledGenes_returnsTwoUnseenGenes(){
        // This Test mostly checks if there is an exception thrown
        // Which it should not!
        var r = new SplittableRandom(10);
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,10);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,10);

        GeneticAlgorithm ga = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),r);

        var results = ga.crossover(a,b,r);

        assertEquals(2,results.size());

        var c = results.get(0);
        var d = results.get(1);

        // C different:
        assertFalse(c.equals(a) || c.equals(b));
        // D different:
        assertFalse(d.equals(a) || d.equals(b));
    }

    @Test
    public void testCrossover_oneGeneEmpty_returnsIdenticalGeneAndOneEmpty(){
        // There is no crossover possible here, gene stays the same
        var r = new SplittableRandom(10);
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,10);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);

        GeneticAlgorithm ga = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),r);

        var results = ga.crossover(a,b,r);

        assertEquals(2,results.size());

        var c = results.get(0);
        var d = results.get(1);

        assertEquals(a,c);
        assertEquals(b,d);
    }

    @Test
    public void testCrossover_twoFilledGenes_returnsGenesAreNotEmpty(){
        // This Test mostly checks if there is an exception thrown
        // Which it should not!
        var r = new SplittableRandom(10);
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,10);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,10);

        GeneticAlgorithm ga = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),r);

        var results = ga.crossover(a,b,r);

        assertEquals(2,results.size());

        var c = results.get(0);
        var d = results.get(1);

        assertNotEquals(0,c.getLength());
        assertNotEquals(0,d.getLength());
    }


    @Test
    public void testCrossover_twoFilledGenes_withDifferentSize_sizeOfChildrenSame(){
        var r = new SplittableRandom(10);
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        int aSize = 10;
        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,aSize);
        int bSize = 20;
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,bSize);

        GeneticAlgorithm ga = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),r);

        var results = ga.crossover(a,b,r);

        assertEquals(2,results.size());

        var c = results.get(0);
        var d = results.get(1);

        assertEquals(aSize,c.getLength());
        assertEquals(bSize,d.getLength());
    }

    @Tag("Regression")
    @RepeatedTest(5)
    public void testCrossover_twoFilledGenes_withDifferentSize_sizeOfChildrenSame_VariantB(){
        // Variant A has A>B
        // Variant B has B>A
        var r = new SplittableRandom();
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        int aSize = 20;
        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,aSize);
        int bSize = 5;
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,bSize);

        GeneticAlgorithm ga = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),r);

        var results = ga.crossover(a,b,r);

        assertEquals(2,results.size());

        var c = results.get(0);
        var d = results.get(1);

        assertEquals(aSize,c.getLength());
        assertEquals(bSize,d.getLength());
    }


    @Test
    public void testCrossover_whenSeeded_differentSeeds_isDeterministic(){
        var r = new SplittableRandom(10);
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        int aSize = 20;
        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,aSize);
        int bSize = 5;
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,bSize);

        GeneticAlgorithm ga = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),r);

        var gen1 = new SplittableRandom(55);
        var gen2 = new SplittableRandom(100);
        var results1 = ga.crossover(a,b,gen1);
        var results2 = ga.crossover(a,b,gen2);

        var c1 = results1.get(0);
        var d1 = results1.get(1);
        var c2 = results2.get(0);
        var d2 = results2.get(1);

        assertNotEquals(c1,c2);
        assertNotEquals(d1,d2);
    }

    @Test
    public void testCrossover_whenSeeded_isDeterministic(){
        var r = new SplittableRandom(10);
        var config = new Configuration();
        MetricCache cache = new MetricCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        int aSize = 20;
        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,aSize);
        int bSize = 5;
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,bSize);

        GeneticAlgorithm ga = new GeneticAlgorithm(config.genetic,support,new ParetoFront(cache),r);

        var gen1 = new SplittableRandom(55);
        var gen2 = new SplittableRandom(55);
        var results1 = ga.crossover(a,b,gen1);
        var results2 = ga.crossover(a,b,gen2);

        var c1 = results1.get(0);
        var d1 = results1.get(1);
        var c2 = results2.get(0);
        var d2 = results2.get(1);

        assertEquals(c1,c2);
        assertEquals(d1,d2);
    }

    @Tag("Integration")
    @Test
    public void testTournamentSelection_OneElement_OneTournament_shouldReturnElement(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);
        ParetoFront pareto = new ParetoFront(cache);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,3);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,0.9);

        MetamorphicPopulation testPopulation = new MetamorphicPopulation(support);
        testPopulation.saveIndividual(a);

        config.genetic.setTournamentSize(1);
        GeneticAlgorithm ga= new GeneticAlgorithm(config.genetic,support,pareto,r);

        var result = ga.tournamentSelection(testPopulation,r);

        assertTrue(result.isPresent());
        assertEquals(a,result.get());
    }

    @Tag("Integration")
    @Test
    public void testTournamentSelection_TwoElement_OneTournament_shouldReturnFittest(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);
        ParetoFront pareto = new ParetoFront(cache);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,3);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,4);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,0.9);
        storeIndividualForCaching(b,cache,stub,0.5);

        MetamorphicPopulation testPopulation = new MetamorphicPopulation(support);
        testPopulation.saveIndividual(a);
        testPopulation.saveIndividual(b);

        config.genetic.setTournamentSize(1);
        GeneticAlgorithm ga= new GeneticAlgorithm(config.genetic,support,pareto,r);

        var result = ga.tournamentSelection(testPopulation,r);

        assertTrue(result.isPresent());
        assertEquals(a,result.get());
    }


    @Tag("Integration")
    @Test
    public void testEvolvePop_shouldHaveHigherGeneration(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);
        ParetoFront pareto = new ParetoFront(cache);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,3);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,4);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,0.9);
        storeIndividualForCaching(b,cache,stub,0.5);

        MetamorphicPopulation testPopulation = new MetamorphicPopulation(support);
        testPopulation.saveIndividual(a);
        testPopulation.saveIndividual(b);

        config.genetic.setTournamentSize(1);
        GeneticAlgorithm ga= new GeneticAlgorithm(config.genetic,support,pareto,r);

        var result = ga.evolvePopulation(testPopulation);

        assertEquals(1,result.getGeneration());
    }


    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testTournamentSelection_ThreeElements_ThreeTournamentSize_shouldReturnFittest(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);
        ParetoFront pareto = new ParetoFront(cache);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,3);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,4);
        MetamorphicIndividual c = new MetamorphicIndividual(support,0);
        c.populateIndividual(r,4);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,0.9);
        storeIndividualForCaching(b,cache,stub,0.5);
        storeIndividualForCaching(c,cache,stub,0.4);

        MetamorphicPopulation testPopulation = new MetamorphicPopulation(support);
        testPopulation.saveIndividual(a);
        testPopulation.saveIndividual(b);
        testPopulation.saveIndividual(c);

        config.genetic.setTournamentPutBack(false);
        config.genetic.setTournamentSize(3);
        GeneticAlgorithm ga= new GeneticAlgorithm(config.genetic,support,pareto,r);

        var result = ga.tournamentSelection(testPopulation,r);

        assertTrue(result.isPresent());
        assertEquals(a,result.get());
    }

    @Tag("Probabilistic")
    @Tag("Seeded")
    @Tag("Regression")
    @Tag("Integration")
    @Test
    public void testTournamentSelection_ThreeElements_ThreeTournamentSizeWithPutBack_MightNotSeeAllElements(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);
        ParetoFront pareto = new ParetoFront(cache);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,3);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,4);
        MetamorphicIndividual c = new MetamorphicIndividual(support,0);
        c.populateIndividual(r,4);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,0.9);
        storeIndividualForCaching(b,cache,stub,0.5);
        storeIndividualForCaching(c,cache,stub,0.4);

        MetamorphicPopulation testPopulation = new MetamorphicPopulation(support);
        testPopulation.saveIndividual(a);
        testPopulation.saveIndividual(b);
        testPopulation.saveIndividual(c);

        config.genetic.setTournamentPutBack(true);
        config.genetic.setTournamentSize(3);
        GeneticAlgorithm ga= new GeneticAlgorithm(config.genetic,support,pareto,r);

        var result = ga.tournamentSelection(testPopulation,r);

        assertTrue(result.isPresent());
        assertEquals(b,result.get());
    }

    @Tag("Integration")
    @Test
    public void testTournamentSelection_ThreeElements_OneElementIsShorterAtSameFitness_shouldReturnShorter(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);
        ParetoFront pareto = new ParetoFront(cache);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,8);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,3);
        MetamorphicIndividual c = new MetamorphicIndividual(support,0);
        c.populateIndividual(r,5);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,0.8);
        storeIndividualForCaching(b,cache,stub,0.9);
        storeIndividualForCaching(c,cache,stub,0.4);

        MetamorphicPopulation testPopulation = new MetamorphicPopulation(support);
        testPopulation.saveIndividual(a);
        testPopulation.saveIndividual(b);
        testPopulation.saveIndividual(c);

        config.genetic.setTournamentSize(10);
        GeneticAlgorithm ga= new GeneticAlgorithm(config.genetic,support,pareto,r);

        var result = ga.tournamentSelection(testPopulation,r);

        assertTrue(result.isPresent());
        assertEquals(b,result.get());
    }

    @Tag("Integration")
    @Test
    public void testTournamentSelection_TwoElements_OneElementIsShorterAtSameFitness_shouldReturnShorter(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);
        ParetoFront pareto = new ParetoFront(cache);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,8);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,3);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,0.9);
        storeIndividualForCaching(b,cache,stub,0.9);

        MetamorphicPopulation testPopulation = new MetamorphicPopulation(support);
        testPopulation.saveIndividual(a);
        testPopulation.saveIndividual(b);

        config.genetic.setTournamentSize(5);
        config.genetic.setTournamentPutBack(false);
        GeneticAlgorithm ga= new GeneticAlgorithm(config.genetic,support,pareto,r);

        var result = ga.tournamentSelection(testPopulation,r);

        assertTrue(result.isPresent());
        assertEquals(b,result.get());
    }


    @Tag("Integration")
    @Test
    public void testTournamentSelection_MoreTournamentSizeThanElements_ShouldBeOk(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);
        ParetoFront pareto = new ParetoFront(cache);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,3);
        MetamorphicIndividual c = new MetamorphicIndividual(support,0);
        c.populateIndividual(r,4);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,0.9);
        storeIndividualForCaching(b,cache,stub,0.6);
        storeIndividualForCaching(c,cache,stub,0.4);

        MetamorphicPopulation testPopulation = new MetamorphicPopulation(support);
        testPopulation.saveIndividual(a);
        testPopulation.saveIndividual(b);
        testPopulation.saveIndividual(c);

        config.genetic.setTournamentSize(20);
        GeneticAlgorithm ga= new GeneticAlgorithm(config.genetic,support,pareto,r);

        var result = ga.tournamentSelection(testPopulation,r);

        assertTrue(result.isPresent());
        assertEquals(a,result.get());
    }

    @Tag("Integration")
    @Test
    public void testTournamentSelection_shouldBeDeterministic(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);
        ParetoFront pareto = new ParetoFront(cache);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,3);
        MetamorphicIndividual c = new MetamorphicIndividual(support,0);
        c.populateIndividual(r,4);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,0.9);
        storeIndividualForCaching(b,cache,stub,0.6);
        storeIndividualForCaching(c,cache,stub,0.4);

        MetamorphicPopulation testPopulation = new MetamorphicPopulation(support,0);
        testPopulation.saveIndividual(a);
        testPopulation.saveIndividual(b);
        testPopulation.saveIndividual(c);

        config.genetic.setTournamentSize(3);
        config.genetic.setTournamentPutBack(false);
        GeneticAlgorithm ga= new GeneticAlgorithm(config.genetic,support,pareto,r);

        var result = ga.tournamentSelection(testPopulation,r);

        assertTrue(result.isPresent());
        assertEquals(a,result.get());
    }

    @Tag("Integration")
    @Test
    public void testTournamentSelection_MoreTournamentSizeThanElements_ShouldBeOk_withNoPutback(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);
        ParetoFront pareto = new ParetoFront(cache);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,3);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,0.9);
        storeIndividualForCaching(b,cache,stub,0.6);

        MetamorphicPopulation testPopulation = new MetamorphicPopulation(support,0);
        testPopulation.saveIndividual(a);
        testPopulation.saveIndividual(b);

        config.genetic.setTournamentSize(1);
        config.genetic.setTournamentPutBack(false);
        GeneticAlgorithm ga= new GeneticAlgorithm(config.genetic,support,pareto,r);

        Random random1 = new Random(10);
        Random random2 = new Random(10);

        var result1 = ga.tournamentSelection(testPopulation,random1);
        var result2 = ga.tournamentSelection(testPopulation,random2);

        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(result1.get(),result2.get());
    }

    @Test
    public void testTournamentSelection_EmptyPopulation_ShouldBeEmpty(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);
        ParetoFront pareto = new ParetoFront(cache);

        MetamorphicPopulation testPopulation = new MetamorphicPopulation(support,0);

        config.genetic.setTournamentSize(10);
        GeneticAlgorithm ga= new GeneticAlgorithm(config.genetic,support,pareto,r);

        var result = ga.tournamentSelection(testPopulation,r);

        assertTrue(result.isEmpty());
    }
}
