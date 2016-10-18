package com.utnapp.instafood.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.utnapp.instafood.Models.Publication;
import com.utnapp.instafood.R;

import java.util.ArrayList;

public class ImageGridAdapter extends ArrayAdapter<Publication> {
    private Context context;
    private int layoutId;
    private ArrayList<Publication> content = new ArrayList<>();

    public ImageGridAdapter(Context context, int layoutId, ArrayList<Publication> content) {
        super(context, layoutId, content);

        this.layoutId = layoutId;
        this.context = context;
        this.content = content;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.image = (ImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        Publication item = content.get(position);

        holder.imageTitle.setText(item.title);
        holder.image.setImageBitmap(item.image);
        holder.image.setTag(item.title);

        return row;
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}
