
public enum CriticalArea {
	
	NorthStation(0),
	Crossroad(1),
	East(2),
	Center(3),
	West(4),
	SouthStation(5);
	
	public final int id;  
	
    CriticalArea(int id) {
        this.id = id;
    }

}
