package TSim;

public class Train extends Thread {
	private TSimInterface tsi;

	private int train_id;
	private int train_speed;
	private boolean down;
	
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
	        }
	        if(this.train_id==2) {
	        	// train 2 starts at the bottomStation
	        	this.down=false;
	        	// get semaphore for bottomStation
	        }
	        
	        // fetch all sensor events
	        while (!this.isInterrupted()) {
	            //SensorEvent event = tsi.getSensor(train_id);
	            //this.handleSensorEvent(event);
	          }
	      }
	      catch (CommandException e) {
	        e.printStackTrace();    // or only e.getMessage() for the error
	        System.exit(1);
	      } //catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
	}

	private void handleSensorEvent(SensorEvent event) {
		// TODO Auto-generated method stub
		System.out.println(event);
		
	}

}

