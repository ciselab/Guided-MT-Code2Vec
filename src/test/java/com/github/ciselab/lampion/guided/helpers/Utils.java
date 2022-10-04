package com.github.ciselab.lampion.guided.helpers;

import com.github.ciselab.lampion.guided.algorithms.MetamorphicIndividual;
import com.github.ciselab.lampion.guided.metric.Metric;
import com.github.ciselab.lampion.guided.support.MetricCache;

import java.util.HashMap;

public class Utils {

    public static void storeIndividualForCaching(MetamorphicIndividual ind, MetricCache cache, StubMetric m, Double value){
        m.valuesToReturn.put(ind,value);
        HashMap<Metric,Double> aMetrics = new HashMap<>();
        aMetrics.put(m,value);
        cache.putMetricResults(ind,aMetrics);
        cache.addMetric(m);
    }


    /**
     * @return A Cache without any active metrics, will not call file system for any evaluation
     */
    public static MetricCache makeEmptyCache(){
        MetricCache cache = new MetricCache();
        cache.getMetrics().removeIf(x -> true);
        cache.getActiveMetrics().removeIf(x -> true);
        return cache;
    }
}
