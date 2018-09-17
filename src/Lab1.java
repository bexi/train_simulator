import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import TSim.*;

public class Lab1 {
	
	// Semaphores for the critical areas, e.i. areas where there can only be one train at a time 
	
	Map<CriticalArea, Semaphore> semaphores = new EnumMap<CriticalArea, Semaphore>(CriticalArea.class);
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
	  train1.run();
	  Train train2 = new Train(2, speed2);
	  train2.run();

  }
}


// question: using streams within a thread ? 