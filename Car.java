import java.util.Random;
import java.util.concurrent.Semaphore;

//Class that simulates a car giving tours with a thread
public class Car extends Thread {
	
	public Semaphore passInCar; //Semaphore for passengers currently in the car
	public Semaphore carLock; //Semaphore that locks car's access to the track
	public int numOfPassengers; //Passengers per car
	public boolean keepLooping = true; //Allows the car to keep going on rides
	private Random rand = new Random(); //Dictates the length of the tour
	
	//Allows us to use msg() method
		public static long time = System.currentTimeMillis();

		public void msg(String m) {
			System.out.println("["+(System.currentTimeMillis()-time)+"] "+getName()+":"+m);
		}
	
	//Car constructor
		public Car(int id, Semaphore sem, int numOfPassengers) {
			setName("Car-" + id); //Sequentially names car threads
			
			passInCar = sem; //Counting semaphore to keep track of passengers in the car
			
			carLock = new Semaphore(0); //A binary semaphore that locks/gives access to the track
			
			this.numOfPassengers = numOfPassengers;
		}
		
		public void run(){
			
			//Car keeps trying to fill up with passengers, until keepLooping is set to false (when the last passenger finishes)
			while(keepLooping){
				fillUp();
			}
			
		}
		
		//Method allows cars to fill up with passengers
		public void fillUp(){
				
			try {
				
				//Attempts to gain access to the track
				carLock.acquire();
				
				//If the car should keep trying to fill up with passengers
				if(keepLooping){
				
					msg("Departing from station");
					sleep(rand.nextInt(4001)); //The ride lasts for somewhere between 0 and 4 seconds
					
					msg("Pulling into station");

					//Once ride is finished, passInCar semaphore is signaled a number of times equal to the number
					//of passengers in the car
					for(int i = 0; i<numOfPassengers;i++)
						passInCar.release();
				
				}
								
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		public void setKeepLooping(boolean tempKeepLooping){
			keepLooping = tempKeepLooping;
		}
}
