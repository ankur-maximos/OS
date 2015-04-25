package com.maximos.os;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Main class containing the threading logic
 * 
 * @author maximos
 *
 */
public class Dining {

	static int TIME_CONVERTER = 1000;
	static int numTables;
	static int numCooks;
	static String COOK_THREAD_GROUP = "cooks";

	static List<Cook> cookList = new ArrayList<Cook>();
	static Object cooks = new Object();
	static Object tables = new Object();
	static Object cookCounter = new Object();
	static Object waitForCook = new Object();
	static boolean restaurantClosed = false;

	// shared resources
	static AtomicInteger currentTables;
	static AtomicInteger currentCooks;
	static AtomicBoolean friesMachineBusy;
	static AtomicBoolean burgerMachineBusy;
	static AtomicBoolean cokeMachineBusy;
	static AtomicIntegerArray tablesInfo;
	static AtomicInteger customerServed;
	static CountDownLatch exitLatch;

	/**
	 * Method to print customers order summary
	 * 
	 * @param order
	 *            customer order
	 * 
	 */
	static public void printOrders(Order order) {
		System.out.println("Arrival time " + order.getArrivalTime());
		System.out.println("Seating time " + order.getSeatedTime());
		System.out.println("Cook Serve time " + order.getCookServeTime());
		System.out.println("Food Serve time " + order.getFoodServeTime());
		System.out.println("Exit time " + order.getExitTime());
		System.out
				.println("Cook id time " + order.getServingCook().getCookId());
		System.out.println("Table Number " + order.getSeatedTable());
	}

	/*
	 * This Class thread is for the order thread
	 * 
	 * So Here we basically keep track of each customer,Few basic functions
	 * which are performed here are, 1.Users enters the restaurant and look for
	 * table, 2.User then look for a cook, 3.After getting cook assigned, it
	 * waits for food, 4. After getting food, he take around 30 min to finish
	 * the food and vacate the table for a new Customer
	 */
	class OrderThread extends Thread {
		private Order order;
		private ThreadGroup tg;
		private CountDownLatch latch;
		private long localTime;
		private boolean gotSeat;
		private boolean gotCook;
		private boolean waitingForCook;
		private boolean waitingForSeat;

		public OrderThread(Order order, ThreadGroup tg, CountDownLatch latch,
				long localTime) {
			this.order = order;
			this.tg = tg;
			this.latch = latch;
			this.localTime = localTime;
			this.gotCook = false;
			this.gotSeat = false;
			waitingForCook = false;
			waitingForSeat = false;
		}

		public void updateLocalTime() {
			this.localTime = System.currentTimeMillis();
		}

		public long getLocaltime() {
			return localTime;
		}

		@Override
		public void run() {
			System.out.println("Current tables " + currentTables.get());
			while (!gotSeat) {
				if (currentTables.get() > 0
						&& currentTables.getAndDecrement() > 0) {
					gotSeat = true;
					long seatedTime = System.currentTimeMillis() - localTime;
					updateLocalTime();
					order.setSeatedTable(getEmptyTable());
					order.setSeatedTime(order.getArrivalTime()
							+ (int) Math.round((double) seatedTime
									/ TIME_CONVERTER));
					System.out.println("Customer order " + order.getOrderid()
							+ " seated on table " + order.getSeatedTable()
							+ " at time " + order.getSeatedTime() + " min");

					/*
					 * After getting a seat, user will look for a cook
					 * 
					 * If cook is available then well and good else the user
					 * will have to wait for the cook to be available.
					 */
					while (!gotCook) {
						if (currentCooks.get() > 0
								&& currentCooks.getAndDecrement() > 0) {
							gotCook = true;
							long cookTime = System.currentTimeMillis()
									- localTime;
							order.setCookServeTime((int) Math
									.round((double) cookTime / TIME_CONVERTER)
									+ order.getSeatedTime());
							updateLocalTime();
							order.setLocalClock(localTime);
							Cook cook = assignCook(tg, order, latch);
							if (cook != null) {
								order.setServingCook(cook);
							} else {
								try {
									cooks.wait();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
							try {
								Thread.sleep(30000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							long exitTime = System.currentTimeMillis()
									- localTime;
							updateLocalTime();
							order.setExitTime(order.getFoodServeTime()
									+ (int) Math.round((double) exitTime
											/ TIME_CONVERTER));
							System.out.println("Customer order "
									+ order.getOrderid() + " exit time is: "
									+ order.getExitTime() + " min");
							setEmptyTable(order.getSeatedTable());
							currentTables.incrementAndGet();
							customerServed.incrementAndGet();
							exitLatch.countDown();
							synchronized (tables) {
								tables.notifyAll();
							}
						} else {
							if (!waitingForCook) {
								waitForCook = true;
								updateLocalTime();
							}
							if (currentCooks.get() < 0) {
								currentCooks.getAndSet(0);
							}
							synchronized (cooks) {
								try {
									cooks.wait();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					}
				} else {
					if (!waitingForSeat) {
						waitingForSeat = true;
						updateLocalTime();
					}
					if (currentTables.get() < 0) {
						currentTables.getAndSet(0);
					}
					synchronized (tables) {
						try {
							tables.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	class CookThread extends Thread {

		private Order order;
		private Cook cook;
		private CountDownLatch latch;

		// constructor
		public CookThread(ThreadGroup threadGroup, int cookId) {
			super(threadGroup, "cook" + cookId);
			cook = new Cook(cookId);
			this.cook.setCookId(cookId);
			this.cook.setBusy(false);
		}

		public Order getOrder() {
			return order;
		}

		public void setOrder(Order order) {
			this.order = order;
		}

		public Cook getCook() {
			return cook;
		}

		public void setCook(Cook cook) {
			this.cook = cook;
		}

		public void setLatch(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void run() {
			System.out.println(Thread.currentThread().getName()
					+ " is ready to take orders");
			while (restaurantClosed != true) {
				while (this.cook.busy.get() == false)
					;
				/*
				 * Cook got his order which he will start to process
				 * 
				 * -- Work: Basically responsible for computation of the
				 * Customer Timing, here the cook just tries to look for empty
				 * machines if he finds any empty machine, for the requested
				 * order
				 */
				System.out.println(this.getName()
						+ " started processing order " + order.getOrderid()
						+ " at" + order.getCookServeTime() + " min");
				long startTime = System.currentTimeMillis();
				while (true) {
					if (order.getNumFries() > 0
							&& friesMachineBusy.compareAndSet(false, true)) {
						long friesTime = System.currentTimeMillis()
								- order.getLocalClock();
						System.out.println(this.getName()
								+ " using fries Machine for customer order "
								+ order.getOrderid()
								+ " at "
								+ ((int) Math.round((double) friesTime
										/ TIME_CONVERTER) + order
											.getCookServeTime()) + " min");
						while (order.getNumFries() > 0) {
							try {
								Thread.sleep(3 * TIME_CONVERTER);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							order.setNumFries(order.getNumFries() - 1);
						}
						friesMachineBusy.set(false);
						synchronized (waitForCook) {
							waitForCook.notifyAll();
						}
					} else if (order.getNumBurgers() > 0
							&& burgerMachineBusy.compareAndSet(false, true)) {
						long burgerTime = System.currentTimeMillis()
								- order.getLocalClock();
						System.out.println(this.getName()
								+ " using burger Machine for customer order "
								+ order.getOrderid()
								+ " at "
								+ ((int) Math.round((double) burgerTime
										/ TIME_CONVERTER) + order
											.getCookServeTime()) + " min");
						while (order.getNumBurgers() > 0) {
							try {
								Thread.sleep(5 * TIME_CONVERTER);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							order.setNumBurgers(order.getNumBurgers() - 1);
						}
						burgerMachineBusy.set(false);
						synchronized (waitForCook) {
							waitForCook.notifyAll();
						}
					} else if (order.isCoke()
							&& cokeMachineBusy.compareAndSet(false, true)) {
						long cokeTime = System.currentTimeMillis()
								- order.getLocalClock();
						System.out.println(this.getName()
								+ " using coke Machine for customer order "
								+ order.getOrderid()
								+ " at "
								+ ((int) Math.round((double) cokeTime
										/ TIME_CONVERTER) + order
											.getCookServeTime()) + " min");
						try {
							Thread.sleep(1 * TIME_CONVERTER);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						order.setCoke(false);
						cokeMachineBusy.set(false);
						synchronized (waitForCook) {
							waitForCook.notifyAll();
						}
					} else if (!order.isCoke() && order.getNumBurgers() == 0
							&& order.getNumFries() == 0) {
						long elaspedTime = System.currentTimeMillis()
								- startTime;
						System.out.println("total time for the order "
								+ order.getOrderid() + "is" + elaspedTime
								/ TIME_CONVERTER + "min");
						latch.countDown();
						cook.busy.compareAndSet(true, false);
						currentCooks.incrementAndGet();
						synchronized (cooks) {
							cooks.notifyAll();
						}
						break;
					}
				}
			}
		}
	}

	/**
	 * this method finds all the threads currently active under a thread group
	 * 
	 * @param group
	 *            group name
	 * @return all the threads under the thread group name
	 */
	public static Thread[] getGroupThreads(final ThreadGroup group) {
		if (group == null)
			throw new NullPointerException("Null group");
		int nAlloc = group.activeCount();
		int n = 0;
		Thread[] threads = null;
		do {
			nAlloc *= 2;
			threads = new Thread[nAlloc];
			n = group.enumerate(threads, false);
		} while (n == nAlloc);
		return java.util.Arrays.copyOf(threads, n);
	}

	/**
	 * This method gets cook for the user
	 * 
	 * It performs below operations
	 * 
	 * a.fetch the idle cook b.assigns the cook to the order c.updates the cook
	 * status d.updates the global cook count e.updates the cook serve time for
	 * the order f.initialize the cook for the new order
	 * 
	 * @param tg
	 *            thread group to find the active threads under Cook thread
	 *            group
	 * @param order
	 *            customer order
	 * @return the Cook who processed the order
	 */
	public static Cook assignCook(ThreadGroup tg, Order order,
			CountDownLatch latch) {
		Thread cooks[] = getGroupThreads(tg);
		for (int ii = 0; ii < cooks.length; ii++) {
			CookThread cook = (CookThread) cooks[ii];
			if (cook.getCook().busy.get() == false) {
				cook.setOrder(order);
				cook.setLatch(latch);
			}
			if (cook.getCook().busy.compareAndSet(false, true)) {
				cook.setOrder(order);
				cook.setLatch(latch);
				try {
					latch.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				long foodServeTime = System.currentTimeMillis()
						- ((OrderThread) Thread.currentThread()).getLocaltime();
				((OrderThread) Thread.currentThread()).updateLocalTime();
				order.setFoodServeTime((int) Math.round((double) foodServeTime
						/ TIME_CONVERTER)
						+ order.getCookServeTime());
				System.out
						.println("Food served for customer "
								+ order.getOrderid() + " at"
								+ order.getFoodServeTime());
				return cook.getCook();
			}
		}
		return null;
	}

	/**
	 * This method returns the empty table, if there is no empty table it
	 * returns -1
	 * 
	 * @return table number
	 */
	public static int getEmptyTable() {
		int num = -1;
		for (int ii = 0; ii < tablesInfo.length(); ii++) {
			if (tablesInfo.get(ii) == 0 && tablesInfo.getAndIncrement(ii) == 0) {
				return ii;
			}
		}
		return num;
	}

	/**
	 * This method sets the occupied table as empty by assigning it value equal
	 * to 0
	 * 
	 * @param table
	 *            Table id
	 * 
	 */
	public static void setEmptyTable(int table) {
		tablesInfo.getAndDecrement(table);
	}

	public static void main(String args[]) {

		int numDiners;
		Scanner sc = new Scanner(System.in);

		/*
		 * Initializing the restaurant
		 * 
		 * TablesCount, Machines availability, making Cooks ready for servicing
		 */

		// initializing diners
		numDiners = Integer.parseInt(sc.nextLine().trim());

		// initializing tables and cooks
		numTables = Integer.parseInt(sc.nextLine().trim());
		currentTables = new AtomicInteger(numTables);
		numCooks = Integer.parseInt(sc.nextLine().trim());
		currentCooks = new AtomicInteger(numCooks);

		// initializing tables Info and customer served
		tablesInfo = new AtomicIntegerArray(numTables);
		customerServed = new AtomicInteger();

		// initializing machines status
		friesMachineBusy = new AtomicBoolean(false);
		burgerMachineBusy = new AtomicBoolean(false);
		cokeMachineBusy = new AtomicBoolean(false);

		// starting main class
		Dining dine = new Dining();

		// initializing thread group for the cooks
		ThreadGroup tg = new ThreadGroup(COOK_THREAD_GROUP);

		// initializing the latch
		exitLatch = new CountDownLatch(numDiners);

		// Starting Cook threads
		System.out.println("Intializing Cooks..");
		for (int ii = 0; ii < numCooks; ii++) {
			CookThread cook = dine.new CookThread(tg, (ii + 1));
			cook.start();
		}

		/*
		 * Now processing each order and creating threads for it
		 * 
		 * if tables are available then well and good, else order will have to
		 * wait
		 */
		Order order;
		int sleepingTime = 0;
		long startRestaurant = System.currentTimeMillis();
		int latestCustomerTime = 0;
		int ii = 0;
		while (sc.hasNextLine()) {
			String str[] = sc.nextLine().split(" +");
			order = new Order();
			order.setOrderid((ii + 1));
			order.setArrivalTime(Integer.parseInt(str[0].trim()));
			order.setNumBurgers(Integer.parseInt(str[1].trim()));
			order.setNumFries(Integer.parseInt(str[2].trim()));
			if (Integer.parseInt(str[3]) == 0) {
				order.setCoke(false);
			} else {
				order.setCoke(true);
			}
			sleepingTime = order.getArrivalTime() - latestCustomerTime;
			latestCustomerTime = order.getArrivalTime();
			System.out.println("Customer " + order.getOrderid()
					+ " enters after " + sleepingTime + " min");
			try {
				Thread.sleep(sleepingTime * TIME_CONVERTER);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			OrderThread customer = dine.new OrderThread(order, tg,
					new CountDownLatch(1), System.currentTimeMillis());
			customer.start();
			if (++ii >= numDiners)
				break;
		}

		try {
			exitLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		long closeRestaurant = System.currentTimeMillis();
		System.out.println("Restaurant Total Running time :"
				+ (int) Math.round((double) (closeRestaurant - startRestaurant)
						/ TIME_CONVERTER) + " min");
		sc.close();
	}
}