package com.github.ciselab.lampion.guided.support;

import com.github.ciselab.lampion.guided.algorithms.MetamorphicIndividual;
import com.github.ciselab.lampion.guided.metric.Metric;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class implements the maintenance for the Pareto front.
 * It checks whether a new solution is Pareto dominant over the current solutions and is thus
 * able to add it to the Pareto front.
 *
 * Note: We opted for a Pareto Front that can hold multiple elements if they have identical metrics.
 * That is, we consider dominance as "not being worse than anything else in the frontier".
 *
 * For more explanation on the Pareto front you can take a look at
 * https://en.wikipedia.org/wiki/Pareto_front
 */
public class ParetoFront {

    private Set<MetamorphicIndividual> frontier = new HashSet<>();
    private List<Metric> metrics;

    public ParetoFront(MetricCache cache) {
        metrics = cache.getMetrics();
    }

    public Set<MetamorphicIndividual> getFrontier(){
        return this.frontier;
    }

    /**
     * Add to the Pareto set if no solution dominates the current solution.
     * Elements that are dominated by the new solution are removed.
     * @param solution the current solution.
     */
    public void addToParetoOptimum(MetamorphicIndividual solution){
        // Exit early if Element is already saved
        if(this.frontier.contains(solution))
            return;
        // Exit early if Element is not Pareto Dominant to anything
        for(var individual: frontier) {
            boolean isDominated = paretoDominant(individual, solution,metrics);
            if (isDominated) {
                return;
            }
        }

        List<MetamorphicIndividual> toRemove = new ArrayList<>();
        for(MetamorphicIndividual i: frontier) {
            // if solution is dominant over i then delete i
            if (paretoDominant(solution, i, metrics)) {
                toRemove.add(i);
            }
        }
        toRemove.forEach(frontier::remove);
        frontier.add(solution);
    }

    /**
     * Check if a is Pareto dominant over b.
     * Pareto Dominance is the case if for any metris A is better than B,
     * if A is not worse in any other metrics.
     * @param a an individual.
     * @param b an individual.
     * @return whether a is pareto dominant over b.
     */
    public static boolean paretoDominant(
            MetamorphicIndividual a, MetamorphicIndividual b,
            List<Metric> metrics){
        // Error Cases: Exit early with no-dominance.
        if (a == null || b == null || metrics == null || metrics.isEmpty() || a.equals(b)){
            return false;
        }

        boolean allEqual = true;
        for(Metric m:metrics){
            // Case 1: Is worse in any?
            var aVal = m.apply(a);
            var bVal =  m.apply(b);
            if (aVal < bVal)
                return false;
            // Double Equality is difficult, so we check for the difference to be super duper small
            allEqual = allEqual && Math.abs(aVal - bVal) < 0.00001;
        }
        // Case 2: All "as good"?
        if(allEqual)
            return false;
        // Case 3: Default Return True
        return true;
    }

    public String displayPareto() {
        Set<MetamorphicIndividual> paretoIndivs = this.getFrontier();
        String out = "{";
        for(var v: paretoIndivs)
            out += v.toString() + ", ";
        return out.substring(0, out.length()-2) + "}";
    }

}
