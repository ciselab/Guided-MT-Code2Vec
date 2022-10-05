package com.github.ciselab.lampion.guided.support;

import com.github.ciselab.lampion.guided.algorithms.MetamorphicIndividual;
import com.github.ciselab.lampion.guided.configuration.Configuration;
import com.github.ciselab.lampion.guided.helpers.StubMetric;
import com.github.ciselab.lampion.guided.metric.Metric;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static com.github.ciselab.lampion.guided.helpers.Utils.makeEmptyCache;
import static com.github.ciselab.lampion.guided.helpers.Utils.storeIndividualForCaching;
import static org.junit.jupiter.api.Assertions.*;

public class ParetoFrontTest {

    @Test
    public void testInitialize_shouldHaveEmptyFrontier(){
        var cache = new MetricCache();
        var pp = new ParetoFront(cache);
        assertTrue(pp.getFrontier().isEmpty());
    }

    @Tag("Integration")
    @ParameterizedTest
    @ValueSource(doubles = {0.0,0.5,0.9,1.0})
    public void testAddOneElement_toEmptyPareto_ShouldBeInTheFront(double fitness){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,fitness);

        ParetoFront pareto = new ParetoFront(cache);
        pareto.addToParetoOptimum(a);

        var front = pareto.getFrontier();
        assertEquals(1,front.size());
        assertEquals(a,front.stream().findFirst().get());
    }

    @Tag("Integration")
    @ParameterizedTest
    @ValueSource(doubles = {0.0,0.5,0.9,1.0})
    public void testAddOneElement_toEmptyPareto_addElementTwice_ShouldBeInTheFront(double fitness){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,fitness);

        ParetoFront pareto = new ParetoFront(cache);
        pareto.addToParetoOptimum(a);
        pareto.addToParetoOptimum(a);

        var front = pareto.getFrontier();
        assertEquals(1,front.size());
        assertEquals(a,front.stream().findFirst().get());
    }


    @Tag("Integration")
    @ParameterizedTest
    @ValueSource(doubles = {0.0,0.5,0.9})
    public void testAddToPareto_TwoElements_FitterIsKept_OneElement(double fitness){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,3);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,fitness);
        storeIndividualForCaching(b,cache,stub,fitness+0.1);


        ParetoFront pareto = new ParetoFront(cache);
        pareto.addToParetoOptimum(a);
        pareto.addToParetoOptimum(b);

        var front = pareto.getFrontier();
        assertEquals(1,front.size());
        assertEquals(b,front.stream().findFirst().get());
    }

    @Tag("Integration")
    @ParameterizedTest
    @ValueSource(doubles = {0.1,0.5,0.9,1.0})
    public void testAddToPareto_TwoElements_FitterIsKept_OneElement_VariantB(double fitness){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,3);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,fitness);
        storeIndividualForCaching(b,cache,stub,fitness-0.1);


        ParetoFront pareto = new ParetoFront(cache);
        pareto.addToParetoOptimum(a);
        pareto.addToParetoOptimum(b);

        var front = pareto.getFrontier();
        assertEquals(1,front.size());
        assertEquals(a,front.stream().findFirst().get());
    }


    @Tag("Integration")
    @ParameterizedTest
    @ValueSource(doubles = {0.1,0.5,0.9,1.0})
    public void testAddToPareto_TwoElements_equalFitness_BothAreKept(double fitness){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,3);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        storeIndividualForCaching(a,cache,stub,fitness);
        storeIndividualForCaching(b,cache,stub,fitness);


        ParetoFront pareto = new ParetoFront(cache);
        pareto.addToParetoOptimum(a);
        pareto.addToParetoOptimum(b);

        var front = pareto.getFrontier();
        assertEquals(2,front.size());
        assertTrue(front.contains(a));
        assertTrue(front.contains(b));
    }


    @Tag("Integration")
    @ParameterizedTest
    @ValueSource(doubles = {0.1,0.9,1.0})
    public void testAddToPareto_AddManyElementsWithSameFitness_ShouldALlBeKept(double fitness){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);


        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        ParetoFront pareto = new ParetoFront(cache);
        final int ELEMENTSTOADD = 10;

        for(int i = 0;i<ELEMENTSTOADD;i++) {
            MetamorphicIndividual a = new MetamorphicIndividual(support, 0);
            a.populateIndividual(r, 5);
            storeIndividualForCaching(a, cache, stub, fitness);
            pareto.addToParetoOptimum(a);
        }

        var front = pareto.getFrontier();
        assertEquals(ELEMENTSTOADD,front.size());
    }

    @Test
    public void testParetoDominance_EmptyMetrics_defaultsToFalse(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);

        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,10);

        boolean dominant = ParetoFront.paretoDominant(a,b,new ArrayList<>());

        assertFalse(dominant);
    }


    @Test
    public void testParetoDominance_NullElements_defaultsToFalse(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);
        var metrics = new ArrayList<Metric>();
        metrics.add(stub);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);

        assertFalse(ParetoFront.paretoDominant(a,null,metrics));
        assertFalse(ParetoFront.paretoDominant(null,a,metrics));
    }


    @Test
    public void testParetoDominance_OneElementIsDominantToOther_ShouldReturnFitter(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);
        var metrics = new ArrayList<Metric>();
        metrics.add(stub);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,9);

        stub.valuesToReturn.put(a,0.5);
        stub.valuesToReturn.put(b,0.6);

        assertFalse(ParetoFront.paretoDominant(a,b,metrics));
        assertTrue(ParetoFront.paretoDominant(b,a,metrics));
    }

    @Test
    public void testParetoDominance_OneElementIsDominantToOther_ShouldReturnFitter_variantB(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);
        var metrics = new ArrayList<Metric>();
        metrics.add(stub);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,9);

        stub.valuesToReturn.put(a,0.8);
        stub.valuesToReturn.put(b,0.4);

        assertTrue(ParetoFront.paretoDominant(a,b,metrics));
        assertFalse(ParetoFront.paretoDominant(b,a,metrics));
    }


    @Test
    public void testParetoDominance_TwoMetrics_OneElementIsDominantToOtherInBoth_ShouldDominate(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        var metrics = new ArrayList<Metric>();
        StubMetric stub1 = new StubMetric();
        metrics.add(stub1);
        StubMetric stub2 = new StubMetric();
        metrics.add(stub2);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,9);

        stub1.valuesToReturn.put(a,0.8);
        stub1.valuesToReturn.put(b,0.4);
        stub2.valuesToReturn.put(a,0.8);
        stub2.valuesToReturn.put(b,0.4);

        assertTrue(ParetoFront.paretoDominant(a,b,metrics));
        assertFalse(ParetoFront.paretoDominant(b,a,metrics));
    }

    @Test
    public void testParetoDominance_TwoMetrics_EachElementIsBestInOne_ShouldNotDominate(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        var metrics = new ArrayList<Metric>();
        StubMetric stub1 = new StubMetric();
        metrics.add(stub1);
        StubMetric stub2 = new StubMetric();
        metrics.add(stub2);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,9);

        stub1.valuesToReturn.put(a,0.8);
        stub1.valuesToReturn.put(b,0.0);
        stub2.valuesToReturn.put(a,0.0);
        stub2.valuesToReturn.put(b,0.8);

        assertFalse(ParetoFront.paretoDominant(a,b,metrics));
        assertFalse(ParetoFront.paretoDominant(b,a,metrics));
    }


    @Test
    public void testParetoDominance_TwoMetrics_HaveOneTheSame_ShouldDominateOneWay(){
        // A(0.8,0.8) and B(0.0,0.8)
        // Then A Dominates B and B does not Dominate A
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        var metrics = new ArrayList<Metric>();
        StubMetric stub1 = new StubMetric();
        metrics.add(stub1);
        StubMetric stub2 = new StubMetric();
        metrics.add(stub2);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,9);

        stub1.valuesToReturn.put(a,0.8);
        stub2.valuesToReturn.put(a,0.8);

        stub1.valuesToReturn.put(b,0.0);
        stub2.valuesToReturn.put(b,0.8);

        assertTrue(ParetoFront.paretoDominant(a,b,metrics));
        assertFalse(ParetoFront.paretoDominant(b,a,metrics));
    }

    @Test
    public void testParetoDominance_TwoMetrics_HaveOneTheSame_ShouldDominateOneWay_variantB(){
        // A(0.8,0.8) and B(0.8,0.0)
        // Then A Dominates B and B does not Dominate A
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        var metrics = new ArrayList<Metric>();
        StubMetric stub1 = new StubMetric();
        metrics.add(stub1);
        StubMetric stub2 = new StubMetric();
        metrics.add(stub2);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,9);

        stub1.valuesToReturn.put(a,0.8);
        stub2.valuesToReturn.put(a,0.8);

        stub1.valuesToReturn.put(b,0.8);
        stub2.valuesToReturn.put(b,0.0);

        assertTrue(ParetoFront.paretoDominant(a,b,metrics));
        assertFalse(ParetoFront.paretoDominant(b,a,metrics));
    }

    @Test
    public void testParetoDominance_ItemToItself_DefaultsFalse(){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        StubMetric stub = new StubMetric();
        stub.setWeight(1);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);

        storeIndividualForCaching(a,cache,stub,0.5);

        boolean dominant = ParetoFront.paretoDominant(a,a,new ArrayList<>());

        assertFalse(dominant);
    }
    @ParameterizedTest
    @ValueSource(doubles = {0.0,0.5,1.0})
    public void testParetoDominance_SameFitness_ShouldBeNonDominant(double fitness){
        Random r = new Random(5);
        var config = new Configuration();
        MetricCache cache = makeEmptyCache();
        GenotypeSupport support = new GenotypeSupport(cache,config);

        MetamorphicIndividual a = new MetamorphicIndividual(support,0);
        a.populateIndividual(r,5);
        MetamorphicIndividual b = new MetamorphicIndividual(support,0);
        b.populateIndividual(r,3);

        StubMetric stub = new StubMetric();
        List<Metric> metrics = new ArrayList<>();
        metrics.add(stub);

        stub.valuesToReturn.put(a,fitness);
        stub.valuesToReturn.put(b,fitness);

        assertFalse(ParetoFront.paretoDominant(a,b,metrics));
        assertFalse(ParetoFront.paretoDominant(b,a,metrics));
    }

}
