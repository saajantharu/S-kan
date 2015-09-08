package com.saajan.sample;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnClickListener {


    ImageView ivThumbnailPhoto;
    private static int RESULT_LOAD_IMG = 1;
   String imgDecodableString;

    Button binary, thin, btnTakePic;

    int[] pix, linh;

    int width, height, count;

   private static int TAKE_PICTURE = 1;

    Bitmap saveImagedata, bm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnTakePic = (Button) findViewById(R.id.btnTakePic);

        thin = (Button) findViewById(R.id.btnthn);

        binary = (Button) findViewById(R.id.binary);


        ivThumbnailPhoto = (ImageView) findViewById(R.id.imageView);
        //convert imageview to bitmap


        // add onclick listener to the button
        btnTakePic.setOnClickListener(this);


        ivThumbnailPhoto = (ImageView) findViewById(R.id.imageView);
        BitmapDrawable drawable = (BitmapDrawable) ivThumbnailPhoto.getDrawable();
        final Bitmap imgbitmap = drawable.getBitmap();

        //convert bitmap to grayscale
        binary.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


                Bitmap bitMapBinaryImage = GrayscaleToBin(saveImagedata);
                ivThumbnailPhoto.setImageBitmap(bitMapBinaryImage);

            }
        });

    }

    // Image capture button is clicked
    @Override
    public void onClick(View view) {

        // create intent with ACTION_IMAGE_CAPTURE action
        Intent intentImageCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Intent intentImageCapture = new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA );

        // start camera activity
        startActivityForResult(intentImageCapture, TAKE_PICTURE);

    }
    public void loadImagefromGallery(View view) {
        // Create intent to Open Image applications like Gallery, Google Photos
           Intent galleryIntent = new Intent(Intent.ACTION_PICK,
               android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    // The Android Camera application encodes the photo in the return Intent
    // delivered to onActivityResult()
    // as a small Bitmap in the extras, under the key "data"
    @Override

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
               if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                // Move to first row
               cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                ImageView imgView = (ImageView) findViewById(R.id.imageView);
                // Set the Image in ImageView after decoding the String
                imgView.setImageBitmap(BitmapFactory
                        .decodeFile(imgDecodableString));
                Bitmap bm;
                bm = BitmapFactory.decodeFile(imgDecodableString);
                saveImagedata=bm;

            }
           else if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK
                    && data!= null  ) {
                // get bundle
                Bundle extras = data.getExtras();

                // extracting the data out of extras via intent
                Bitmap bitMapImage = (Bitmap) extras.get("data");
                ivThumbnailPhoto.setImageBitmap(bitMapImage);
                saveImagedata = bitMapImage;

            }
            else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }



    }

    public static Bitmap GrayscaleToBin(Bitmap bm2)
    {
        Bitmap bm;
        bm=bm2.copy(Bitmap.Config.RGB_565, true);
        final   int width = bm.getWidth();
        final  int height = bm.getHeight();

        int pixel1,pixel2,pixel3,pixel4,A,R;
        int[]  pixels;
        pixels = new int[width*height];
        bm.getPixels(pixels,0,width,0,0,width,height);
        int size=width*height;
        int s=width/8;
        int s2=s>>1;
        double t=0.15;
        double it=1.0-t;
        int []integral= new int[size];
        int []threshold=new int[size];
        int i,j,diff,x1,y1,x2,y2,ind1,ind2,ind3;
        int sum=0;
        int ind=0;
        while(ind<size)
        {
            sum+=pixels[ind] & 0xFF;
            integral[ind]=sum;
            ind+=width;
        }
        x1=0;
        for(i=1;i<width;++i)
        {
            sum=0;
            ind=i;
            ind3=ind-s2;
            if(i>s)
            {
                x1=i-s;
            }
            diff=i-x1;
            for(j=0;j<height;++j)
            {
                sum+=pixels[ind] & 0xFF;
                integral[ind]=integral[(int)(ind-1)]+sum;
                ind+=width;
                if(i<s2)continue;
                if(j<s2)continue;
                y1=(j<s ? 0 : j-s);
                ind1=y1*width;
                ind2=j*width;

                if (((pixels[ind3]&0xFF)*(diff * (j - y1))) < ((integral[(int)(ind2 + i)] - integral[(int)(ind1 + i)] - integral[(int)(ind2 + x1)] + integral[(int)(ind1 + x1)])*it)) {
                    threshold[ind3] = 0x00;
                } else {
                    threshold[ind3] = 0xFFFFFF;
                }
                ind3 += width;
            }
        }

        y1 = 0;
        for( j = 0; j < height; ++j )
        {
            i = 0;
            y2 =height- 1;
            if( j <height- s2 )
            {
                i = width - s2;
                y2 = j + s2;
            }

            ind = j * width + i;
            if( j > s2 ) y1 = j - s2;
            ind1 = y1 * width;
            ind2 = y2 * width;
            diff = y2 - y1;
            for( ; i < width; ++i, ++ind )
            {

                x1 = ( i < s2 ? 0 : i - s2);
                x2 = i + s2;

                // check the border
                if (x2 >= width) x2 = width - 1;

                if (((pixels[ind]&0xFF)*((x2 - x1) * diff)) < ((integral[(int)(ind2 + x2)] - integral[(int)(ind1 + x2)] - integral[(int)(ind2 + x1)] + integral[(int)(ind1 + x1)])*it)) {
                    threshold[ind] = 0x00;
                } else {
                    threshold[ind] = 0xFFFFFF;
                }
            }
        }
   /*-------------------------------
    * --------------------------------------------*/
        bm.setPixels(threshold,0,width,0,0,width,height);

        return bm;
    }

}

