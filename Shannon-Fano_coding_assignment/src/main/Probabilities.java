package main;

public class Probabilities implements Comparable<Probabilities> {
	private double value;
	private double xStar;
	private String codeword;
	
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	public double getxStar() {
		return xStar;
	}
	public void setxStar(double xStar) {
		this.xStar = xStar;
	}
	public String getCodeword() {
		if(codeword == null) return "";
		return codeword;
	}
	public void setCodeword(String codeword) {
		this.codeword = codeword;
	}
	
	@Override
	public int compareTo(Probabilities probability) {
		Double thisDouble = new Double(this.getValue());
		Double paramDouble = new Double(probability.getValue());
		
		return thisDouble.compareTo(paramDouble);
	}
}
