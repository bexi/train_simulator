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
	public int[] sensors;

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
	  
	  this.addSensors();
	  //System.out.println("length of semaphore map: " + semaphores.size()); --> 6 

	  Train train1 = new Train(1, speed1);
	  train1.start();
	  Train train2 = new Train(2, speed2);
	  train2.start();

  }
  
  private void addSensors() {
		// TODO Auto-generated method stub
	  
		
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
					// ignore events which is outgoing from sensors
					if(event.getStatus() == SensorEvent.ACTIVE) {
			            this.handleSensorEvent(event);
					}
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

		private void handleSensorEvent(SensorEvent event) throws CommandException, InterruptedException {
			// TODO Auto-generated method stub
			System.out.println(event);
			
			
			// cross section - north sensors (in and out of cross section)
			if(
					this.isEqualSensor(event, Sensor.NorthTop) || 
					this.isEqualSensor(event, Sensor.NorthBottom)) 
			{
				if(this.down) {
					// going into cross section
					this.tryAcquire(CriticalArea.Crossroad);
					semaphores.get(CriticalArea.TopStation).release();	
				}else {
					// has been in cross section -> release semaphor
					semaphores.get(CriticalArea.Crossroad).release();
					this.enterStation();
				}
			}
			
			
			// cross section - south sensors (in and out of cross section)
			if(
					this.isEqualSensor(event, Sensor.NorthCenterTop1) || 
					this.isEqualSensor(event, Sensor.NorthCenterBottom1)) 
			{
				if(this.down) {
					// has been in cross section --> release semaphor
					semaphores.get(CriticalArea.Crossroad).release();
					// do not let go of north top station semaphor yet 
				}else {
					// going into cross section
					this.tryAcquire(CriticalArea.Crossroad);
				}
			}
			
			// going into east section from the north
			if(this.isEqualSensor(event, Sensor.NorthCenterTop2) && this.down) {
				this.tryAcquire(CriticalArea.Right);
				semaphores.get(CriticalArea.TopStation).release();
				// set switch to UP
				tsi.setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);
			}
			if(this.isEqualSensor(event, Sensor.NorthCenterBottom2) && this.down) {
				this.tryAcquire(CriticalArea.Right);
				// release possible semaphor for the other north station? 
				tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);	
			}	
			
			// going into east section from the south
			if(this.isEqualSensor(event, Sensor.SouthCenterTop1) && !this.down) {
				this.tryAcquire(CriticalArea.Right);
				semaphores.get(CriticalArea.Center).release(); // TODO release when the train is in the EAST 
				tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
			}
			if(this.isEqualSensor(event, Sensor.SouthCenterBottom1) && !this.down) {
				this.tryAcquire(CriticalArea.Right);
				// release possible semaphor for the other north station? 
				tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);	
			}
			
			// going out of east section (either to the north or south)
			if(this.isEqualSensor(event, Sensor.East)) {
				// going into the Center section
				if(this.down) {
					// try acquire the Center sem
					if(semaphores.get(CriticalArea.Center).tryAcquire()) {
						tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
					}
					// otherwise take the other route 
					else {
						tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
					}
				}
				// going into the NorthCenter section
				else {
					// try acquire the TopStation sem
					if(semaphores.get(CriticalArea.TopStation).tryAcquire()) {
						tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
					}
					// otherwise take the other route 
					else {
						tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
					}
					
				}
				// release semaphore when leaving the area
				semaphores.get(CriticalArea.Right).release();
			}
			
			// going into center section from the north - done in previous scope 
			
			// going into the center from the south 			
			if(this.isEqualSensor(event, Sensor.West) && !this.down) {
				// try acquire the TopStation sem
				if(semaphores.get(CriticalArea.Center).tryAcquire()) {
					tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
				}
				// otherwise take the other route (longer)
				else {
					tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);
				}
				// release semaphor when the train has entered the center part 
			}
			
			// going into west section from north
			if(this.isEqualSensor(event, Sensor.SouthCenterTop1) && this.down) {
				System.out.println("sensor: southcentertop1: should stop");
				this.tryAcquire(CriticalArea.Left);
				semaphores.get(CriticalArea.Center).release();
				// set switch to UP
				tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
			}
			if(this.isEqualSensor(event, Sensor.SouthCenterBottom1) && this.down) {
				this.tryAcquire(CriticalArea.Left);
				// release possible semaphor for the other north station? 
				tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);	
			}	
			// going into west section from south
			if(this.isEqualSensor(event, Sensor.SouthTop) && !this.down) {
				this.tryAcquire(CriticalArea.Left);
				// release possible sem? 
				// set switch to UP
				tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
			}
			if(this.isEqualSensor(event, Sensor.SouthBottom) && !this.down) {
				this.tryAcquire(CriticalArea.Left);
				semaphores.get(CriticalArea.BottomStation).release();
				tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);	
			}	
			
			if((this.isEqualSensor(event, Sensor.SouthCenterTop1) || this.isEqualSensor(event, Sensor.SouthCenterBottom1)) && !this.down) {
				System.out.println("train has been in the south, train id: " + this.train_id);
				semaphores.get(CriticalArea.Left).release();
			}
			
		}

		private void enterStation() throws CommandException {
			//slow down
			tsi.setSpeed(train_id, 0);
			// pause at station
			
			// start in the other direction 			
		}

		private boolean isEqualSensor(SensorEvent event, Sensor sensor) {
			return event.getXpos() == sensor.x && event.getYpos() == sensor.y;
		}

		private void tryAcquire(CriticalArea ca) throws CommandException, InterruptedException {
			// set speed to 0 - backup if the semaphore blocks 
			tsi.setSpeed(train_id, train_slowdown);
			// try to acquire semaphore
			semaphores.get(ca).acquire();
			// set speed
			tsi.setSpeed(train_id, train_speed);			
		}

	}

}





// question: using streams within a thread ? 