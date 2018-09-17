
public enum Sensor {
	
	NorthTop(6,6),
	NorthBottom(9,5),
	NorthCenterTop1(10,7),
	NorthCenterTop2(14,7),
	NorthCenterBottom1(10,8),
	NorthCenterBottom2(16,8),
	East(19,8),
	SouthCenterTop(10,9),
	SouthCenterBottom(10,10),
	West(1,10),
	SouthTop(5,11),
	SouthBottom(5,13);
	
	public final int x;
	public final int y;

    Sensor(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
