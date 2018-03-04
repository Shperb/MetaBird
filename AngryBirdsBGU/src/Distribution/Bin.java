package Distribution;

public class Bin {
	double start;
	double end;
	public double getStart() {
		return start;
	}
	public void setStart(double start) {
		this.start = start;
	}
	public double getEnd() {
		return end;
	}
	public void setEnd(double end) {
		this.end = end;
	}
	public Bin(double start, double end) {
		super();
		this.start = start;
		this.end = end;
	}
	
	public boolean isInBin(double val){
		return (val>=start && val<=end);
	}
	
	public double getMid(){
		return (start+end)/2;
	}
	

}
