import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import TSim.*;

public class Lab1 {
	
	// Semaphores for the critical areas, e.i. areas where there can only be one train at a time 
	
	public Map<CriticalArea, Semaphore> semaphores = new EnumMap<CriticalArea, Semaphore>(CriticalArea.class);
	//enumMap.put(Color.RED, "red");
	//String value = enumMap.get(Color.RED);
	
	
	// Sensors  

  public Lab1(int speed1, int speed2) {
//    TSimInterface tsi = TSimInterface.getInstance();
//
//    try {
//      tsi.setSpeed(1,speed1);
//    }
//    catch (CommandException e) {
//      e.printStackTrace();    // or only e.getMessage() for the error
//      System.exit(1);
//    }
	  System.out.println(CriticalArea.TopStation.id);
	  
	  // Create semaphore for all critical areas
	  // Semaphore semaphore = new Semaphore(1);
	  
	  
	  for( CriticalArea area : CriticalArea.values()) {
		  System.out.println(area);
		  semaphores.put(area, new Semaphore(1));
	  }
	  //System.out.println("length of semaphore map: " + semaphores.size()); --> 6 

	  Train train1 = new Train(1, speed1);
	  train1.start();
	  Train train2 = new Train(2, speed2);
	  train2.start();

  }
  
  // Internal class for a train
  public class Train extends Thread {
		private TSimInterface tsi;

		private int train_id;
		private int train_speed;
		private boolean down;
		private int train_slowdown = 0;
		
		
		public Train(int train_id, int train_speed) {
			tsi = TSimInterface.getInstance();
			this.train_id = train_id;
			this.train_speed = train_speed;
			
		}
		
		public void run() {
		    try {
		        tsi.setSpeed(train_id, train_speed);
		        
		        // set up for the two trains 
		        if(this.train_id==1) {
		        	// train 1 starts at the topStation
		        	this.down=true;
		        	// get semaphore for topStation
		        	semaphores.get(CriticalArea.TopStation).acquire();
		        	
		        	
		        }
		        if(this.train_id==2) {
		        	// train 2 starts at the bottomStation
		        	this.down=false;
		        	// get semaphore for bottomStation
		        	semaphores.get(CriticalArea.BottomStation).acquire();

		        }
		        
		        // handle all sensor events
		        while (!this.isInterrupted()) {
		            SensorEvent event = tsi.getSensor(train_id);
		            this.handleSensorEvent(event);
		          }
		      }
		      catch (CommandException e) {
		        e.printStackTrace();    // or only e.getMessage() for the error
		        System.exit(1);
		      } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}

		private void handleSensorEvent(SensorEvent event) throws CommandException {
			// TODO Auto-generated method stub
			System.out.println(event);
			
			// Sensor X (after crossroad top)
			if(event.getXpos()== 14 & event.getYpos()==7) {
				System.out.println("in sensor pos! ");
				
				// set speed to 0 - backup if the semaphore blocks 
				tsi.setSpeed(train_id, train_slowdown);
				// try to acquire semaphore
				semaphores.get(CriticalArea.Right);
				semaphores.get(CriticalArea.TopStation).release();
				// set speed
				tsi.setSpeed(train_id, train_speed);
				// set switch to UP
				tsi.setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);
			}
			
			
		}

	}

}





// question: using streams within a thread ? 