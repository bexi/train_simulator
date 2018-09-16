package TSim;

public class Train extends Thread {
	private TSimInterface tsi;

	private int train_id;
	private int train_speed;
	
	public Train(int train_id, int train_speed) {
		tsi = TSimInterface.getInstance();
		this.train_id = train_id;
		this.train_speed = train_speed;
		
	}
	
	public void run() {
	    try {
	        tsi.setSpeed(train_id, train_speed);
	      }
	      catch (CommandException e) {
	        e.printStackTrace();    // or only e.getMessage() for the error
	        System.exit(1);
	      }
	}

}

