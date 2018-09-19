
public enum Sensor {
	NorthTopStation(14,3),
	NorthTop(6,6),
	NorthBottomStation(14,5),
	NorthBottom(9,5),
	NorthCenterTop1(10,7),
	NorthCenterTop2(14,7),
	NorthCenterBottom1(10,8),
	NorthCenterBottom2(16,8),
	East(19,8),
	SouthCenterTop2(12,9),
	SouthCenterTop1(7,9),
	SouthCenterBottom2(12,10),
	SouthCenterBottom1(7,10),
	West(1,10),
	SouthTop(5,11),
	SouthTopStation(14,11),
	SouthBottom(5,13),
	SouthBottomStation(14,13);
	
	public final int x;
	public final int y;

    Sensor(int x, int y) {
        this.x = x;
        this.y = y;
    }
}