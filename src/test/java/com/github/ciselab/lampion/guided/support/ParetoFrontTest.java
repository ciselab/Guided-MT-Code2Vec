package com.github.ciselab.lampion.guided.support;

import com.github.ciselab.lampion.guided.algorithms.GeneticAlgorithm;
import com.github.ciselab.lampion.guided.algorithms.MetamorphicIndividual;
import com.github.ciselab.lampion.guided.algorithms.MetamorphicPopulation;
import com.github.ciselab.lampion.guided.configuration.Configuration;
import com.github.ciselab.lampion.guided.helpers.StubMetric;
import com.github.ciselab.lampion.guided.metric.Metric;
import com.github.ciselab.lampion.guided.support.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static com.github.ciselab.lampion.guided.helpers.Utils.makeEmptyCache;
import static com.github.ciselab.lampion.guided.helpers.Utils.storeIndividualForCaching;
import static com.github.ciselab.lampion.guided.support.FileManagement.dataDir;
import static org.junit.jupiter.api.Assertions.*;

public class ParetoFrontTest {
     // TODO: Redo


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

        assertEquals(1,pareto.getFrontier().size());
        var metrics = pareto.getFrontier().stream().findFirst().get();

        assertEquals(1,metrics.size());
        assertEquals(fitness,metrics.get(0));
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

        assertEquals(1,pareto.getFrontier().size());
        var metrics = pareto.getFrontier().stream().findFirst().get();

        assertEquals(1,metrics.size());
        assertEquals(fitness,metrics.get(0));
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

        assertEquals(1,pareto.getFrontier().size());
        var metrics = pareto.getFrontier().stream().findFirst().get();

        // TODO Assess elements???
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

        //boolean dominant = ParetoFront.paretoDominant(a,b,metrics);

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


}
