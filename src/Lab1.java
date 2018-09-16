import TSim.*;

public class Lab1 {

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
	  Train train1 = new Train(1, speed1);
	  train1.run();
	  Train train2 = new Train(2, speed2);
	  train2.run();

  }
}
