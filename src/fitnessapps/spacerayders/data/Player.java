package fitnessapps.spacerayders.data;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Player implements Comparable, Parcelable {

	private String name;
	private String address;
	private String time;
	private int code;

	public Player() {
		name = "";
		address = "";
		time = "";
	}

	public Player(Parcel src) {
		name = src.readString();
		address = src.readString();
		time = src.readString();
	}

	public Player(String newName, String newAddress) {
		name = newName;
		address = newAddress;
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String newAddress) {
		address = newAddress;
	}

	@Override
	public String toString() {
		return name + " " + address;
	}

	public int compareTo(Object arg0) {
		if (code < ((Player) arg0).getHash())
			return -1;
		else if (code == ((Player) arg0).getHash())
			return 0;
		else if (code > ((Player) arg0).getHash())
			return 1;
		else
			return -1;
		// return address.toString().compareTo(
		// ((Player) arg0).getAddress().toString());
	}

	public void setHash(Date timestamp) {
		setTime(timestamp.toGMTString());
		code = (address + getTime()).hashCode();
	}

	public int getHash() {
		return code;
	}

	public int describeContents() {
		return hashCode();
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(address);
		dest.writeString(getTime());
	}

	public boolean equals(Player obj) {
		return this.getAddress().equals(obj.getAddress());
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public Player createFromParcel(Parcel source) {
			return new Player(source);
		}

		public Player[] newArray(int size) {
			return new Player[size];
		}
	};
	
	public String getTime() {
		return time;
	}
	
	public void setTime(String newTime) {
		time = newTime;
	}

}
