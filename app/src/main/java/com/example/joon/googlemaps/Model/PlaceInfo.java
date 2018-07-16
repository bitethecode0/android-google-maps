package com.example.joon.googlemaps.Model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class PlaceInfo implements Parcelable {
    private String name;
    private String address;
    private String phone_number;
    private String id;
    private Uri websiteUri;
    private LatLng latlng;
    private float rating;
    private String attribution;

    public PlaceInfo() {
    }

    public PlaceInfo(String name, String address, String phone_number, String id,
                     Uri websiteUri, LatLng latlng, float rating, String attribution) {
        this.name = name;
        this.address = address;
        this.phone_number = phone_number;
        this.id = id;
        this.websiteUri = websiteUri;
        this.latlng = latlng;
        this.rating = rating;
        this.attribution = attribution;
    }

    protected PlaceInfo(Parcel in) {
        name = in.readString();
        address = in.readString();
        phone_number = in.readString();
        id = in.readString();
        websiteUri = in.readParcelable(Uri.class.getClassLoader());
        latlng = in.readParcelable(LatLng.class.getClassLoader());
        rating = in.readFloat();
        attribution = in.readString();
    }

    public static final Creator<PlaceInfo> CREATOR = new Creator<PlaceInfo>() {
        @Override
        public PlaceInfo createFromParcel(Parcel in) {
            return new PlaceInfo(in);
        }

        @Override
        public PlaceInfo[] newArray(int size) {
            return new PlaceInfo[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Uri getWebsiteUri() {
        return websiteUri;
    }

    public void setWebsiteUri(Uri websiteUri) {
        this.websiteUri = websiteUri;
    }

    public LatLng getLatlng() {
        return latlng;
    }

    public void setLatlng(LatLng latlng) {
        this.latlng = latlng;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * make this model parceable to pass to the other fragments
     * @return
     */
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(phone_number);
        dest.writeString(id);
        dest.writeParcelable(websiteUri, flags);
        dest.writeParcelable(latlng, flags);
        dest.writeFloat(rating);
        dest.writeString(attribution);
    }
}
