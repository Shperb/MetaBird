package PlayingAgent;

public interface TimeDistribution {
	
    public double getProbabilityBelowValue(long timeLeft);

    public double getExpectationBelowValue(long remainingTime);

}
