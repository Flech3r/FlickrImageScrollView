package com.example.ryo.flickrimagescrollview;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Ryo on 2017-05-04.
 */
public class PhotoView extends Activity { // this class was created in order to see only selected photo.
    ImageView img;
    TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_layout);

        String name = getIntent().getStringExtra("name");
        String photo = getIntent().getStringExtra("photo");

        img= (ImageView) findViewById(R.id.imageView2);
        text= (TextView) findViewById(R.id.textView2);
        img.setImageBitmap(BitmapFactory.decodeFile(photo));
        text.setText(name);
    }
}
