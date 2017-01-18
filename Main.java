import java.util.Vector;
import java.util.concurrent.Semaphore;

//This project simulates a haunted house, in which a given number of passengers will take tours
//on a given number of cars. Each passenger and car is represented with a thread, and these threads
//are synchronized using semaphores.
//
//Note: Cars and Passengers are numbered starting from 1 (So, there won't be a Passenger-0 or Car-0)
//Note: Car tours last a random time from 0 to 4 seconds (0 to 4000 milliseconds).
//
//Project By: Steven Wojsnis
//CS340 Project 2

public class Main {

	static int numOfPassengers = 11;
	static int numOfCars = 3;
	static int passengersPerCar = 4;
	static int processCounterPass = 0; //Used to create desired number of passengers
	static int processCounterCar = 0; // Used to create desired number of cars
	
	public static void main(String[] args){
		
		//Gives the user the option to manually set specifications through command line parameters
		if(args.length == 3){
			numOfPassengers = Integer.parseInt(args[0]);
			numOfCars = Integer.parseInt(args[1]);
			passengersPerCar = Integer.parseInt(args[2]);
		}
		else if(args.length!=0 && args.length != 3){
			System.out.println("If not using default values, please specify values for each parameter. Running with default values.");
		}
		
		//A vector to hold the cars
		Vector<Car> vecCar = new Vector<Car>();
		
		//The car threads are created, each with it's own semaphore for passengers
		for(int i = 1;i<=numOfCars;i++){
			Semaphore sem = new Semaphore(0);
			Car car = new Car(i, sem, passengersPerCar);
			vecCar.add(car); //The car is added to the car vector
			car.start(); //Car thread is started
			
		}
		
		//Each passenger is given a copy of the car vector, so they can signal semaphores in the car thread
		Passenger.setCarVector(vecCar);
		Passenger.setMutex(new Semaphore(1));
		
		//Each passenger thread is created
		for(int i = 1; i<=numOfPassengers; i++){
			Passenger passenger = new Passenger(i, passengersPerCar, numOfPassengers);
			passenger.start();
		}
		
		
	}
}
