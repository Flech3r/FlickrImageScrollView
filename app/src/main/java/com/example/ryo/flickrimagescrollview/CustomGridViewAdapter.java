package com.example.ryo.flickrimagescrollview;


import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Ryo on 2017-05-03.
 */
public class CustomGridViewAdapter  extends BaseAdapter{ // this adapter was created in order to place images and texts to gridView.

    ArrayList<String> photoPaths, photoNames;
    Context context;
    private static LayoutInflater inflater=null;
    public CustomGridViewAdapter(MainActivity mainActivity, ArrayList<String> photoPaths, ArrayList<String> photoNames) {
        this.photoPaths = photoPaths;
        this.photoNames = photoNames;
        context=mainActivity;
        inflater = ( LayoutInflater )context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return photoPaths.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView text;
        ImageView img;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;

        rowView = inflater.inflate(R.layout.image, null);
        holder.text =(TextView) rowView.findViewById(R.id.textView);
        holder.img =(ImageView) rowView.findViewById(R.id.imageView);

        holder.text.setText(photoNames.get(position));

        holder.img.setImageBitmap(BitmapFactory.decodeFile(photoPaths.get(position)));

        rowView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, PhotoView.class);
                intent.putExtra("name", photoNames.get(position));
                intent.putExtra("photo", photoPaths.get(position));
                context.startActivity(intent);
                //Toast.makeText(context, "You Clicked "+ photoNames.get(position), Toast.LENGTH_SHORT).show();
            }
        });

        return rowView;
    }


}