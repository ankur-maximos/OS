package com.maximos.os;

import java.util.concurrent.atomic.AtomicBoolean;

public class Cook {

	private int cookId;
	private int startServeTime;
	private int burgerMachineTime;
	private int friesMachineTime;
	private int cokeTime;
	private int foodServeTime;
	protected AtomicBoolean busy = new AtomicBoolean(false);

	public Cook(int cookId) {
		this.cookId = cookId;
		busy.set(false);
		this.friesMachineTime = 0;
		this.cokeTime = 0;
		this.foodServeTime = 0;
		this.burgerMachineTime = 0;
	}

	public void initializeCookForOrder() {
		burgerMachineTime = 0;
		friesMachineTime = 0;
		cokeTime = 0;
	}

	public int getStartServeTime() {
		return startServeTime;
	}

	public void setStartServeTime(int startServeTime) {
		this.startServeTime = startServeTime;
	}

	public boolean isBusy() {
		return busy.get();
	}

	public void setBusy(boolean busy) {
		this.busy.getAndSet(busy);
	}

	public int getCookId() {
		return cookId;
	}

	public void setCookId(int cookId) {
		this.cookId = cookId;
	}

	public int getBurgerMachineTime() {
		return burgerMachineTime;
	}

	public void setBurgerMachineTime(int burgerMachineTime) {
		this.burgerMachineTime = burgerMachineTime;
	}

	public int getFriesMachineTime() {
		return friesMachineTime;
	}

	public void setFriesMachineTime(int friesMachineTime) {
		this.friesMachineTime = friesMachineTime;
	}

	public int getCokeTime() {
		return cokeTime;
	}

	public void setCokeTime(int cokeTime) {
		this.cokeTime = cokeTime;
	}

	public int getFoodServeTime() {
		return foodServeTime;
	}

	public void setFoodServeTime(int foodServeTime) {
		this.foodServeTime = foodServeTime;
	}

}
