package PlayingAgent;

import Utils.TruncatedNormal;
import org.apache.commons.math3.distribution.NormalDistribution;

public class LearnedTimeDistribution implements TimeDistribution {
    private final NormalDistribution normalDistribution;
    private final double sd;
    private final double mu;

    public LearnedTimeDistribution(double mu, double sd) {
        this.mu = mu;
        this.sd = sd;
        this.normalDistribution = new NormalDistribution(mu, sd);
    }

    public double getProbabilityBelowValue(long timeLeft){
        return this.normalDistribution.cumulativeProbability(timeLeft);
    }

    public double getExpectationBelowValue(long remainingTime) {
        return new TruncatedNormal(this.mu, this.sd, 0, remainingTime).getNumericalMean();
    }
}
