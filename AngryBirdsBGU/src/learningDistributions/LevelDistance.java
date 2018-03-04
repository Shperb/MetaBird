package learningDistributions;

public class LevelDistance implements Comparable<LevelDistance> {
	Double mDistance;
	String mlevel;
	public LevelDistance(Double mDistance, String mlevel) {
		super();
		this.mDistance = mDistance;
		this.mlevel = mlevel;
	}
	
	
	
    public Double getDistance() {
		return mDistance;
	}



	public void setDistance(Double mDistance) {
		this.mDistance = mDistance;
	}



	public String getLevel() {
		return mlevel;
	}



	public void setLevel(String mlevel) {
		this.mlevel = mlevel;
	}



	@Override
    public int compareTo(LevelDistance other) {
        return this.getDistance().compareTo(other.getDistance());
    }

}
