package pl.grouptracker;

public class NameAndLocation {

	private String name;
	private int longitude;
	private int latitude;
	
	public NameAndLocation(String _name, int _longitude, int _latitude) {
		name = _name;
		longitude = _longitude;
		latitude = _latitude; 
	}
	public String getName() {
		return name;
	}
	public int getLongitude() {
		return longitude;
	}
	public int getLatitude() {
		return latitude;
	}
}
