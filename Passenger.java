import java.util.Vector;
import java.util.concurrent.Semaphore;

//Class that simulates a passenger in the haunted house with a thread
public class Passenger extends Thread {
	
	static Vector<Car> carVector;
	static Semaphore mutex; //Mutual exclusion for getting in a car
	static Semaphore mutex2; //Mutual exclusion for calculation of finished riders
	static Semaphore leaveMutex = new Semaphore(1); //Binary sem. for leaving the haunted house
	static int passengersPerCar; //Number of passengers allowed in each car
	static int numberOfPassengers = 0; //Total number of passengers that have gone on ride (each unique ride counts)
	static int totalPassengers; //The total number of passengers in the car
	private int id; //Each passenger's ID
	private Car tempCar; 
	private int numOfRides = 0; //Number of rides each individual passenger has gone on
	static int finishedRiders = 0, ridersThatLeft = 0; //Counts of passengers who are finished riding, and those that have left the park
	static boolean goneOnLastRide = false; //Keeps track of whether the last ride has been performed

	
	
	
	//Allows us to use msg() method.
		public static long time = System.currentTimeMillis();

		public void msg(String m) {
			System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+":"+m);
		}

		//Constructor that sequentially names passengers
		public Passenger(int id, int maxPassengersAllowed, int totalNumPassengers) {
		
			setName("Passenger-" + id);
			
			//Initialize variables with logistics about ride
			passengersPerCar = maxPassengersAllowed;
			totalPassengers = totalNumPassengers;
			
			//Store the passengers id (used to exit order)
			this.id = id;
			
			//The last passenger should acquire the leaveMutex first, so that everyone else blocks at exit
			if(id == totalPassengers){
				
				try {
					//We give the last passenger the lock to the leaveMutex before the others
					leaveMutex.acquire();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			}
			
			//Semaphore that locks access to finishedRiders count
			mutex2 = new Semaphore(1);
			
		 
		}
		
		public void run(){
			//The passengers should continue getting on rides until they've ridden three times
			while(numOfRides < 3){
				
				getInCar();

			}
			
			try {
				
				//Ensures mutual exclusion for the calculation of finished riders
				mutex2.acquire();
				
				finishedRiders++; //Riders who have finished riding (Not necessarily left haunted house yet, though)
				
				msg("Finished riding, waiting to leave. There are "+finishedRiders+" passengers that are finished");
				
				
				//Takes care of the case where there aren't enough passengers left to fill up a car
				if(finishedRiders > totalPassengers-passengersPerCar && finishedRiders != totalPassengers && !goneOnLastRide){
					goneOnLastRide = true;
					carVector.firstElement().carLock.release(); //Opens the lock on the car containing the last riders
				}
					
				//Give up mutually exlusive access
				mutex2.release();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			//This code block ensures that passengers leave the haunted house in decreasing order.
			//Note, the passenger with highest ID (Who we gave leaveMutex access to in the beginning) won't enter this if block
			if(id!=totalPassengers){
				try {
					//Riders that got blocked on the leaveMutex continually check to see if it's their turn to leave
					while(true){
						
						leaveMutex.acquire(); //Gain access to the leaveMutex
						
						//If it's their turn to leave, they leave.
						if(id == totalPassengers-ridersThatLeft){
							ridersThatLeft++;
							
							msg("Leaving Park");
							
							//If it so happens that the passenger leaving is the last passenger in the house, they terminate all the
							//cars
							if(ridersThatLeft == totalPassengers)
								for(int i = 0; i<carVector.size();i++){
									msg("ENDING CAR: "+carVector.get(i).getName());
									carVector.get(i).setKeepLooping(false);
									carVector.get(i).carLock.release();
								}
							
							leaveMutex.release(); //Release access to the critical section for leaving the haunted house
							break; //Break out of the while true loop, allowing them to reach end of method and terminate
						}
						
						//If it wasn't their turn to leave, they release access to the critical section, and loop back around,
						//attempting to acquire the leaveMutex lock again
						else{
							leaveMutex.release();
						}
					}
					
				} catch (InterruptedException e) {
					
				}
			}
			
			//This else statement takes care of the passenger with the highest ID
			else{
				
				//Don't need to acquire leaveMutex, because it already has it (we gave passenger access to leaveMutex
				//upon creation, in the constructor)
				ridersThatLeft++;
				msg("Leaving Park");
				
				//Niche case where there is only one passenger in the amusement park
				//Terminates cars
				if(ridersThatLeft == totalPassengers)
					for(int i = 0; i<carVector.size();i++){
						msg("ENDING CARS");
						carVector.get(i).setKeepLooping(false);
						carVector.get(i).carLock.release();
					}
				
				//Give the other passengers a chance to leave
				leaveMutex.release();
			}
			
			
		}
		
		//Method that facilitates the process of passengers getting into cars
		public void getInCar(){
			
			try {
				
				//Gives a passenger exclusive access to the entering car process
				mutex.acquire();
				
				numberOfPassengers++;
				
				//If statement checks if there are the maximum number of passengers in the car, or if there aren't
				//any remaining passengers left to fill up anymore cars (in which case, it accepts fewer than max passengers)
				//(Essentially, checks if the current passenger is the last one to be entering the car)
				if(numberOfPassengers % passengersPerCar == 0 || finishedRiders>(totalPassengers-passengersPerCar)){ // numberOfPassengers > (7*3)
					
					//If there aren't any cars in the station at the moment, wait until one comes back
					while(carVector.size() == 0){
						
					}
					
					//Removes the car from the line of picking up passengers
					tempCar = carVector.firstElement();
					carVector.remove(0);
					
					msg("Getting in "+ tempCar.getName() +  " I'm the last passenger entering");
					
					//Give up exclusive access to the loading process
					mutex.release();
					
					int oldPriority = getPriority();
					tempCar.carLock.release(); //signal to the car's semaphore, to let it go on the tour
					
					
					tempCar.passInCar.acquire(); //Acquire a permit from the car's semaphore for passengers riding it
					setPriority(MAX_PRIORITY); //Give the passenger high priority, so it'll get off car quickly
					
					carVector.add(tempCar); //Add the car to the back of the station line, to wait to fill up with more passengers
					
					msg("Finished ride, getting off of "+ tempCar.getName());
					
					numOfRides++; //Increases the passenger's number of rides
					
					setPriority(oldPriority); //Give the passenger it's normal priority back
					
					sleep(100); //Wander after riding
					
					
				}
				
				//If this passenger isn't the last one to be entering the car
				else{
					
					//If there's no car in the station, wait for one to come back
					while(carVector.size() == 0){
						
					}
					
					//Specify which car the passenger is entering
					tempCar = carVector.firstElement();
					
					//Give up exclusive access to loading process
					mutex.release();
					
					int oldPriority = getPriority();
					msg("Getting in "+tempCar.getName());
					
					tempCar.passInCar.acquire(); //Acquire a permit from the car's semaphore for passengers riding it
					
					setPriority(MAX_PRIORITY); //Give the passenger high priority, so it'll get off car quickly
					
					msg("Finished ride, getting off of "+ tempCar.getName());
					
					numOfRides++; //Increases the passenger's number of rides
					
					setPriority(oldPriority); //Give the passenger it's normal priority back
					
					sleep(100); //Wander after riding
					
				}
				
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		}
		

		static void setCarVector(Vector<Car> tempCarVec){
			carVector = tempCarVec;
		}
		
		static void setMutex(Semaphore tempMutex){
			mutex = tempMutex;
		}
}
