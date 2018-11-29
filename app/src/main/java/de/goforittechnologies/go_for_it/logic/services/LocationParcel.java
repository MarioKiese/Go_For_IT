package de.goforittechnologies.go_for_it.logic.services;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class LocationParcel implements Parcelable {

    private Location location;

    public LocationParcel(Location location){

        this.location = location;

    }

    protected LocationParcel(Parcel in) {

        location = Location.CREATOR.createFromParcel(in);

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LocationParcel> CREATOR = new Creator<LocationParcel>() {
        @Override
        public LocationParcel createFromParcel(Parcel in) {
            return new LocationParcel(in);
        }

        @Override
        public LocationParcel[] newArray(int size) {
            return new LocationParcel[size];
        }
    };

    public Location getLocation() {
        return location;
    }

}
