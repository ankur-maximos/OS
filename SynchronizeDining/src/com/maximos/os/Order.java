package com.maximos.os;

/**
 * Bean class to store values for arrival customers
 * 
 * @author maximos
 *
 */
public class Order {

	/*
	 * Food order variables
	 */
	private int numBurgers;
	private int numFries;
	private boolean coke;
	private int orderid;

	/*
	 * time parameters for the order
	 */
	private long localClock;
	private int arrivalTime;
	private int seatedTime;
	private int cookServeTime;
	private int foodServeTime;
	private int exitTime;

	// table number
	private int seatedTable;

	// serving cook
	private Cook servingCook;

	public Order() {
		this.arrivalTime = 0;
		this.numBurgers = 0;
		this.numFries = 0;
		this.coke = false;
		this.seatedTime = 0;
		this.seatedTable = 0;
		Cook cook = new Cook(0);
		cook.setBurgerMachineTime(0);
		cook.setCokeTime(0);
		cook.setCookId(0);
		cook.setFoodServeTime(0);
		cook.setFriesMachineTime(0);
		this.servingCook = cook;
		this.exitTime = 0;
	}

	public Order(int arr, int burgers, int fries, boolean coke, int seatedTime,
			int seatedTable, Cook servingCook, int exitTime) {
		this.arrivalTime = arr;
		this.numBurgers = burgers;
		this.coke = coke;
		this.seatedTime = seatedTime;
		this.seatedTable = seatedTable;
		this.servingCook = servingCook;
		this.exitTime = exitTime;
	}

	public long getLocalClock() {
		return localClock;
	}

	public void setLocalClock(long localClock) {
		this.localClock = localClock;
	}

	public int getOrderid() {
		return orderid;
	}

	public void setOrderid(int orderid) {
		this.orderid = orderid;
	}

	public int getFoodServeTime() {
		return foodServeTime;
	}

	public void setFoodServeTime(int foodServeTime) {
		this.foodServeTime = foodServeTime;
	}

	public int getCookServeTime() {
		return cookServeTime;
	}

	public void setCookServeTime(int cookServeTime) {
		this.cookServeTime = cookServeTime;
	}

	public Cook getServingCook() {
		return servingCook;
	}

	public void setServingCook(Cook servingCook) {
		this.servingCook = servingCook;
	}

	public int getExitTime() {
		return exitTime;
	}

	public void setExitTime(int exitTime) {
		this.exitTime = exitTime;
	}

	public int getSeatedTime() {
		return seatedTime;
	}

	public void setSeatedTime(int seatedTime) {
		this.seatedTime = seatedTime;
	}

	public int getSeatedTable() {
		return seatedTable;
	}

	public void setSeatedTable(int seatedTable) {
		this.seatedTable = seatedTable;
	}

	public int getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(int arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public int getNumBurgers() {
		return numBurgers;
	}

	public void setNumBurgers(int numBurgers) {
		this.numBurgers = numBurgers;
	}

	public int getNumFries() {
		return numFries;
	}

	public void setNumFries(int numFries) {
		this.numFries = numFries;
	}

	public boolean isCoke() {
		return coke;
	}

	public void setCoke(boolean coke) {
		this.coke = coke;
	}
}