package camelinaction;

public class TicData {
	private String company;
	private Strategy strategy;
	private double minBid;
	private double minAsk;
	private double maxBid;
	private double maxAsk;
	private int totalNumOfBid;
	private int totalNumofAsk;
	private double totalPriceOfBid;
	private double totalPriceOfAsk;
	
	public TicData(String company) {
		this.company = company;
		minBid = Double.MAX_VALUE;
		minAsk = Double.MAX_VALUE;
		maxBid = Double.MIN_VALUE;
		maxAsk = Double.MIN_VALUE;
	}
	
	   public void ticMsgProcess(String message) {
	        String[] parts = message.split("\t");
	        double bidPrice = Double.parseDouble(parts[1]);
	        int bidNum =  Integer.valueOf(parts[2].replaceAll("[^\\d.]", ""));
	        double askPrice = Double.parseDouble(parts[3]);
	        int askNum = Integer.valueOf(parts[4].replaceAll("[^\\d.]", ""));
	        

	        minBid = Math.min(minBid, bidPrice); 
	        minAsk = Math.min(minAsk, askPrice);
	        maxBid = Math.max(maxBid, bidPrice); 
	        maxAsk = Math.max(maxAsk, askPrice);
	        
	        this.totalNumOfBid += bidNum;
	        this.totalNumofAsk += askNum;
	        
	        totalPriceOfBid += bidPrice * bidNum;
	        totalPriceOfAsk += askPrice * askNum;
	        
	    }
	
	public String getName() {
		return company;
	}
	

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}
	
	public double getBidPrice() {
		return strategy.calculate(minBid, maxBid, totalNumOfBid, totalPriceOfBid);
	}
	
	public double getAskPrice() {
		return strategy.calculate(minAsk, maxAsk, totalNumofAsk, totalPriceOfAsk);
	}
	
}
