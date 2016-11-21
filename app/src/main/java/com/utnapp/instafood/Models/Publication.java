package com.utnapp.instafood.Models;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.utnapp.instafood.CommonUtilities;
import com.utnapp.instafood.JsonModels.PublicationJson;
import com.utnapp.instafood.Managers.LikesManager;

import java.util.ArrayList;

public class Publication implements Parcelable {
    public String id;
    public Bitmap image;
    public String description;
    public String city;
    public boolean liked;

    public Publication() {
    }

    protected Publication(Parcel in) {
        id = in.readString();
        image = in.readParcelable(Bitmap.class.getClassLoader());
        description = in.readString();
        city = in.readString();
        liked = in.readByte() != 0;
    }

    public static final Creator<Publication> CREATOR = new Creator<Publication>() {
        @Override
        public Publication createFromParcel(Parcel in) {
            return new Publication(in);
        }

        @Override
        public Publication[] newArray(int size) {
            return new Publication[size];
        }
    };

    public static ArrayList<Publication> Map(PublicationJson[] publicationsJsons, Context context) {
        LikesManager likesManager = new LikesManager(context);
        ArrayList<Publication> publications = new ArrayList<>();
        for(int i = 0; i < publicationsJsons.length; i++){
            Publication publication = new Publication();
            publication.id = publicationsJsons[i]._id;
            publication.image = CommonUtilities.StringToBitMap(publicationsJsons[i].img_base64);
            publication.city = publicationsJsons[i].city;
            publication.description = publicationsJsons[i].description;
            publication.liked = likesManager.getLikes(publication.id) > 0;

            publications.add(publication);
        }

        return publications;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(description);
        dest.writeString(city);
        dest.writeInt(liked ? 1 : 0);
        Bundle b = new Bundle();
        b.putParcelable("image", image);
        dest.writeBundle(b);
    }
}
