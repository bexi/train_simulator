import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import TSim.*;

public class Lab1 {
	
	// Semaphores for the critical areas, e.i. areas where there can only be one train at a time 
	private Map<CriticalArea, Semaphore> semaphores = new EnumMap<CriticalArea, Semaphore>(CriticalArea.class);
	private int maxSpeed = 15;
	
	public Lab1(int speed1, int speed2) {
		// Create the actual semaphores for the CriticalArea enums 
		for( CriticalArea area : CriticalArea.values()) {
			semaphores.put(area, new Semaphore(1));
		}
		// max speed is 15
		if(speed1>this.maxSpeed) {
			speed1 = this.maxSpeed;
			System.out.println("Wanted speed is over the maxSpeed. Speed is set to: " + this.maxSpeed);
		}
		if(speed2>this.maxSpeed) {
			speed2 = this.maxSpeed;
			System.out.println("wanted speed is over the maxSpeed. Speed is set to: " + this.maxSpeed);
		}
		
		// Create the two trains for the simulation
		Train train1 = new Train(1, speed1);
		train1.start();
		Train train2 = new Train(2, speed2);
		train2.start();
	}

	// Internal class for a train
	public class Train extends Thread {
		// interface for communication with the tsim
		private TSimInterface tsi;

		private int train_id;
		private int train_speed;
		private boolean down;
		private int train_slowdown = 0;
		
		// states for who got the semaphore where there is two possible paths
		private boolean gotNorth;
		private boolean gotSouth;
		private boolean gotCenter;
		
		public Train(int train_id, int train_speed) {
			tsi = TSimInterface.getInstance();
			this.train_id = train_id;
			this.train_speed = train_speed;	
			this.gotCenter = false;
		}
		
		public void run() {
		    try {
		        tsi.setSpeed(train_id, train_speed);
		        
		        // setup for the two trains 
		        if(this.train_id==1) {
		        	// train 1 starts at the topStation
		        	this.down=true;
		        	// get semaphore for topStation
		        	semaphores.get(CriticalArea.NorthStation).acquire();
		        	this.gotNorth = true;
		        }        
		        if(this.train_id==2) {
		        	// train 2 starts at the bottomStation
		        	this.down=false;
		        	// get semaphore for bottomStation
		        	semaphores.get(CriticalArea.SouthStation).acquire();
		        	this.gotSouth = true;
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
		        e.printStackTrace();    
		        System.exit(1);
		      } catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}

		private void handleSensorEvent(SensorEvent event) throws CommandException, InterruptedException {
			
			// --------------- NORTH SECTION (from north)
			// north station
			if((this.isEqualSensor(event, Sensor.NorthTopStation) || this.isEqualSensor(event, Sensor.NorthBottomStation) ) && !this.down) {
				this.enterStation();
			}				
			// North -> Crossroad
			if((this.isEqualSensor(event, Sensor.NorthTop) || this.isEqualSensor(event, Sensor.NorthBottom) ) && this.down) {
					this.acquire(CriticalArea.Crossroad);
			}
			// (Crossroad) -> North
			if((this.isEqualSensor(event, Sensor.NorthTop) || this.isEqualSensor(event, Sensor.NorthBottom) ) && !this.down) {
				this.release(CriticalArea.Crossroad);
			}
			// (Crossroad) -> NorthCenter
			if((this.isEqualSensor(event, Sensor.NorthCenterTop1) || this.isEqualSensor(event, Sensor.NorthCenterBottom1) ) && this.down) {
				this.release(CriticalArea.Crossroad);	
			}
			// NorthCenter -> Crossroad
			if((this.isEqualSensor(event, Sensor.NorthCenterTop1) || this.isEqualSensor(event, Sensor.NorthCenterBottom1) ) && !this.down) {
				this.acquire(CriticalArea.Crossroad);
			}		
			// NorthCenterTop -> East 
			if(this.isEqualSensor(event, Sensor.NorthCenterTop2)  && this.down) {
				this.acquire(CriticalArea.East);	
				tsi.setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);	
			}
			// NorthCenterBottom -> East 
			if(this.isEqualSensor(event, Sensor.NorthCenterBottom2) && this.down) {
				this.acquire(CriticalArea.East);	
				tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);	
			}	
			// (NorthStation / NorthCenterTop) -> East   
			if(this.isEqualSensor(event, Sensor.East) && this.down) {
				if(this.gotNorth) {
					this.release(CriticalArea.NorthStation);
					this.gotNorth = false;
				}
			}	
			
			// --------------- EAST SECTION (from east)
			// (East) -> NorthCenter
			if((this.isEqualSensor(event, Sensor.NorthCenterTop2) || this.isEqualSensor(event, Sensor.NorthCenterBottom2)) && !this.down) {
				this.release(CriticalArea.East);
			}
			// East -> NorthCenter
			if(this.isEqualSensor(event, Sensor.East) && !this.down) {
				// two possible roads
				if(semaphores.get(CriticalArea.NorthStation).tryAcquire()) {
					this.gotNorth = true;
					tsi.setSwitch(17, 7, TSimInterface.SWITCH_RIGHT);
				}
				// otherwise take the other route 
				else {
					tsi.setSwitch(17, 7, TSimInterface.SWITCH_LEFT);
				}				
			}	
			// East -> SouthCenter
			if(this.isEqualSensor(event, Sensor.East) && this.down) {
				// two possible roads
				if(semaphores.get(CriticalArea.Center).tryAcquire()) {
					this.gotCenter = true;
					tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
				}
				// otherwise take the other route 
				else {
					tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);	
				}
			}
			// (East) -> SouthCenter
			if((this.isEqualSensor(event, Sensor.SouthCenterTop2) || this.isEqualSensor(event, Sensor.SouthCenterBottom2)) && this.down) {
				this.release(CriticalArea.East);
			}
			
			// --------------- CENTER SECTION (from center)
			// SouthCenterTop -> East
			if(this.isEqualSensor(event, Sensor.SouthCenterTop2) && !this.down) {
				this.acquire(CriticalArea.East);
				tsi.setSwitch(15, 9, TSimInterface.SWITCH_RIGHT);
			}
			// SouthCenterBottom -> East
			if(this.isEqualSensor(event, Sensor.SouthCenterBottom2) && !this.down) {
				this.acquire(CriticalArea.East);
				tsi.setSwitch(15, 9, TSimInterface.SWITCH_LEFT);
			}
			// SouthCenterTop -> West
			if(this.isEqualSensor(event, Sensor.SouthCenterTop1) && this.down) {
				this.acquire(CriticalArea.West);
				tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
			}
			// SouthCenterBottom -> West
			if(this.isEqualSensor(event, Sensor.SouthCenterBottom1) && this.down) {
				this.acquire(CriticalArea.West);
				tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);
			}			
			// (Center) -> West
			if(this.isEqualSensor(event, Sensor.West) && this.down) {
				if(this.gotCenter) {
					this.release(CriticalArea.Center);
					this.gotCenter = false;
				}
			}
			// (Center) -> East
			if(this.isEqualSensor(event, Sensor.East) && !this.down) {
				if(this.gotCenter) {
					this.release(CriticalArea.Center);
					this.gotCenter = false;

				}			
			}
			
			// --------------- WEST SECTION (from west)
			// (West) -> SouthCenter
			if((this.isEqualSensor(event, Sensor.SouthCenterTop1) || this.isEqualSensor(event, Sensor.SouthCenterBottom1))&& !this.down) {
				this.release(CriticalArea.West);
			}
			// (West) -> SouthStation
			if((this.isEqualSensor(event, Sensor.SouthTop) || this.isEqualSensor(event, Sensor.SouthBottom))&& this.down) {
				this.release(CriticalArea.West);
			}	
			// West -> SouthCenter
			if(this.isEqualSensor(event, Sensor.West) && !this.down) {	
	            if (semaphores.get(CriticalArea.Center).tryAcquire()) {
	            	this.gotCenter = true;
	            	tsi.setSwitch(4, 9, TSimInterface.SWITCH_LEFT);
	            } else {
	              tsi.setSwitch(4, 9, TSimInterface.SWITCH_RIGHT);
	            }
			}
			// West -> SouthStation
			if(this.isEqualSensor(event, Sensor.West) && this.down) {
				// two possible roads
				if(semaphores.get(CriticalArea.SouthStation).tryAcquire()) {
					this.gotSouth = true;
					tsi.setSwitch(3, 11, TSimInterface.SWITCH_LEFT);
				}
				// otherwise take the other route 
				else {
					tsi.setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
				}		
			}
			
			// --------------- SOUTH SECTION (from SouthStations)
			// SouthStationTop -> West
			if(this.isEqualSensor(event, Sensor.SouthTop) && !this.down) {
				this.acquire(CriticalArea.West);
				tsi.setSwitch(3, 11, TSimInterface.SWITCH_LEFT);
			}
			// SouthStationBottom -> West
			if(this.isEqualSensor(event, Sensor.SouthBottom) && !this.down) {
				this.acquire(CriticalArea.West);
				tsi.setSwitch(3, 11, TSimInterface.SWITCH_RIGHT);
			}
			// (SouthStation Top) -> West
			if(this.isEqualSensor(event, Sensor.West) && !this.down) {
				if(this.gotSouth) {
					this.release(CriticalArea.SouthStation);
					this.gotSouth = false;
				}
			}
			// SouthStation
			if((this.isEqualSensor(event, Sensor.SouthTopStation) || this.isEqualSensor(event, Sensor.SouthBottomStation) ) && this.down) {
				this.enterStation();
			}
		}

		private void release(CriticalArea ca) {
			semaphores.get(ca).release();			
		}

		private void enterStation() throws CommandException, InterruptedException {
			// slow down
			tsi.setSpeed(train_id, 0);
			// pause at station
			Thread.sleep(this.calculatePauseAtStation(this.train_speed));
			// change direction 	
		    this.train_speed = -this.train_speed;
		    this.down = !this.down;
		    // accelerate train again
			tsi.setSpeed(train_id, train_speed);
		}

		private boolean isEqualSensor(SensorEvent event, Sensor sensor) {
			return event.getXpos() == sensor.x && event.getYpos() == sensor.y;
		}

		private void acquire(CriticalArea ca) throws CommandException, InterruptedException {
			// set speed to 0 - backup if the semaphore blocks 
			tsi.setSpeed(train_id, train_slowdown);
			// try to acquire semaphore
			semaphores.get(ca).acquire();
			// set speed
			tsi.setSpeed(train_id, train_speed);						
		}
		
		private long calculatePauseAtStation(int train_speed) {
			return 1000 + (20 * Math.abs(train_speed));
		}
	}
}