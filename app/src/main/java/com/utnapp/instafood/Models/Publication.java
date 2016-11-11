package com.utnapp.instafood.Models;

import android.content.Context;
import android.graphics.Bitmap;

import com.utnapp.instafood.CommonUtilities;
import com.utnapp.instafood.JsonModels.PublicationJson;
import com.utnapp.instafood.Managers.LikesManager;

import java.util.ArrayList;

public class Publication {
    public String id;
    public Bitmap image;
    public String description;
    public String city;
    public boolean liked;

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
}
