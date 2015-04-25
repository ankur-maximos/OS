package maximos.os.prodcons;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Prod_Cons {

	static int buffer[];
	static int count = 0;
	static int input;
	static int bufferLength;

	public static synchronized void producer(int in) {
		while (count == bufferLength) {
			try {
				Prod_Cons.class.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		buffer[count] = in;
		count++;
		Prod_Cons.class.notifyAll();
	}

	public static synchronized void consumer() {
		System.out.print("count:" + count);
		while (count == 0) {
			try {
				Prod_Cons.class.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.print("\nElement consumed:" + buffer[count-1] + "\n");
		count--;
		Prod_Cons.class.notifyAll();
	}

	class RunnableLogic implements Runnable {
		public void run() {
			if (input == 0)
				consumer();
			else
				producer(input);
		}
	}

	public static void main(String args[]) {

		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		try {
			bufferLength = Integer.parseInt(reader.readLine());
			buffer = new int[bufferLength];
			Scanner in = new Scanner(System.in);
			Prod_Cons procons = new Prod_Cons();
			RunnableLogic rc = procons.new RunnableLogic();
			while (in.hasNextInt()) {
				input = in.nextInt();
				// Here I am assuming that the when the user enters value equal
				// to 0, he intends to consume the
				// the last value in the array.Also i am implementing the array
				// as an stack i.e, follows LIFO
				new Thread(rc).start();
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
