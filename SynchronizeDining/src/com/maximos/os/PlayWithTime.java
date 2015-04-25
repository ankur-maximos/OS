package com.maximos.os;

public class PlayWithTime {

	public static void allAboutMainThread() {
		System.out.println("Active threads" + Thread.activeCount());
		System.out.println("thread name: " + Thread.currentThread().getName());

	}

	public static void allAboutThreadGroup() {

	}

	public static void main(String args[]) {
		// System.out.println("Thread :" + Thread.);
		allAboutMainThread();
		long t = 5099;
		long startTime = System.currentTimeMillis();
		try {
			Thread.sleep(t);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		long elaspedTime = System.currentTimeMillis() - startTime;
		System.out.println("Elasped Time :" + Math.round((double)elaspedTime/1000));
	}
}
