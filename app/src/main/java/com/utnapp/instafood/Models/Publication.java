package com.utnapp.instafood.Models;

import android.graphics.Bitmap;

import com.utnapp.instafood.CommonUtilities;
import com.utnapp.instafood.JsonModels.PublicationJson;

import java.util.ArrayList;

public class Publication {
    private String imageId;
    public Bitmap image;
    public String description;
    public String city;

    public static ArrayList<Publication> Map(PublicationJson[] publicationsJsons) {
        ArrayList<Publication> publications = new ArrayList<>();
        for(int i = 0; i < publicationsJsons.length; i++){
            Publication publication = new Publication();
            publication.imageId = publicationsJsons[i].imageId;
            publication.image = CommonUtilities.StringToBitMap(publicationsJsons[i].imageBase64);
            publication.city = "TESTING";
            publication.description = "TESTING";

            publications.add(publication);
        }

        return publications;
    }
}
