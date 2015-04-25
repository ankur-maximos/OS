package maximos.os.allocateprinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class AllocatePrinter {
	//Considering array as data structure for processes and each number entry as its priority
	//we can choose methods also for determining priority but that is not our purpose instead
	//allocating resources to printer is out motive
	
	static int num_processes;
	static List<Integer> processes = new ArrayList<Integer>();
	static Object printer1 = new Object();
	static Object printer2 = new Object();
	static Object printer3 = new Object();
	static Object lock = new Object();
	static Object lock1 = new Object();
	
	static int current_printing = 0;
	static int waiting_list = 0;
	static int tot_printed = 0;
	static boolean wait = true;
	
	static void print_arraylist() {
		System.out.println("Array list");
		for(int i: processes) {
			System.out.print(i + " ");
		}
	}
	
	class RunnablePrinter implements Runnable{
		Map<Integer,Boolean> printer_busy = new HashMap<Integer, Boolean>();
		
		public RunnablePrinter() {
			printer_busy.put(1, false);
			printer_busy.put(2, false);
			printer_busy.put(3, false);
		}
		
		@Override
		public void run() {
			if(!printer_busy.get(1)) {
				synchronized (printer1) {
					printer_busy.put(1, true);
					System.out.println("Job getting Printed at 1:" + Thread.currentThread().getName());
					try {
						Thread.currentThread().sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					synchronized (lock1) {
						current_printing--;
						tot_printed++;
						lock1.notifyAll();
					}
					synchronized (lock) {
						lock.notifyAll();
					}
					printer_busy.put(1, false);
				}
			} else if(!printer_busy.get(2)) {
				synchronized (printer2) {
					printer_busy.put(2, true);
					System.out.println("Job getting Printed at 2:" + Thread.currentThread().getName());
					try {
						Thread.currentThread().sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					synchronized (lock1) {
						current_printing--;
						tot_printed++;
						lock1.notifyAll();
					}
					synchronized (lock) {
						lock.notifyAll();
					}
					printer_busy.put(2, false);
				}
			} else {
				synchronized (printer3) {
					printer_busy.put(3, true);
					System.out.println("Job getting Printed at 3:" + Thread.currentThread().getName());
					try {
						Thread.currentThread().sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					synchronized (lock1) {
						current_printing--;
						tot_printed++;
						lock1.notifyAll();
					}
					synchronized (lock) {
						lock.notifyAll();
					}
					printer_busy.put(3, false);
				}
			}
		}
	}
		
	
	public static void main(String args[]) {
		int temp = 0;
		Scanner sc = new Scanner(System.in);
		num_processes = sc.nextInt();
		temp = num_processes;
		AllocatePrinter allocatePrinter = new AllocatePrinter();
		RunnablePrinter rc = allocatePrinter.new RunnablePrinter();
		while(temp!=0) {
			 temp--;
			 processes.add(sc.nextInt());
		}
		Collections.sort(processes);
		print_arraylist();
		for(int i=0;i<processes.size();i++){
			synchronized (lock) {
				if(current_printing<3) {
					current_printing++;
					 Thread th = new Thread(rc);
					 th.setName("thread_" + processes.get(i));
					 th.start();
					} 
				else {
					i--;
					try {
						lock.wait();
						continue;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
		while(tot_printed != num_processes){
			try {
				synchronized (lock) {
					lock.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("All Jobs Printed!!");
		sc.close();
	}
}
