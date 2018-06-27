package Distribution;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Created by bla on 6/27/2018.
 */
public class NamedDistribution extends Distribution {
    String levelName;
    Distribution distribution;

    public NamedDistribution(String levelName, Distribution distribution) {
        this.levelName = levelName;
        this.distribution = distribution;
    }

    public String getLevelName(){
        return levelName;
    }

    @Override
    public double getLikelihood(Integer pVal) {
        return distribution.getLikelihood(pVal);
    }

    @Override
    public double[] getExpectationAndProbabilityBelowValue(long value) {
        return distribution.getExpectationAndProbabilityBelowValue(value);
    }

    @Override
    public double getExpectation(long pSubstract) {
        return distribution.getExpectation(pSubstract);
    }

    @Override
    public int drawValue() throws Exception {
        return distribution.drawValue();
    }

    @Override
    public Set<Integer> getSupport() {
        return distribution.getSupport();
    }

    @Override
    public void round(int pFactor) {
        distribution.round(pFactor);
    }

    @Override
    public String distributionType() {
        return distribution.distributionType();
    }

    @Override
    public double getMaxValue() {
        return distribution.getMaxValue();
    }

    @Override
    public SortedMap<Integer, Double> getCDF() {
        return distribution.getCDF();
    }

    @Override
    public Map<String, Double> updateProbablity(int value) {
        return distribution.updateProbablity(value);
    }

}
