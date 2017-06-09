package camelinaction;

public class CalculateMean implements Strategy{
	public double calculate(double min,double max, int totalNum, double totalPrice){
		return totalPrice / totalNum;
	}
}
