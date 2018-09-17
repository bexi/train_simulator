
public enum CriticalArea {
	
	// id from top
	
    // Top Station (top railway) // bottom part?
    // Crossroad
    // Right part
    // Center part (top railway) // bottom part? 
    // Left part
    // Bottom Station (bottom railway) // top part? 
	
	TopStation(0),
	Crossroad(1),
	Right(2),
	Center(3),
	Left(4),
	BottomStation(5);
	
	public final int id;  
	
    CriticalArea(int id) {
        this.id = id;
    }

}
