package hd.ocr;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {


    ImageView ivThumbnailPhoto;
    Button btnSelectPhoto;
    Bitmap saveImagedata;
    Button Binary, thining;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSelectPhoto = (Button) findViewById(R.id.btnSelectPhoto);
        ivThumbnailPhoto = (ImageView) findViewById(R.id.viewImage);
        btnSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

      //convert imageview to bitmap
        ivThumbnailPhoto = (ImageView) findViewById(R.id.viewImage);
        BitmapDrawable drawable = (BitmapDrawable) ivThumbnailPhoto.getDrawable();
        final Bitmap imgbitmap = drawable.getBitmap();
        Binary = (Button) findViewById(R.id.btnbinary);
        Binary.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //convert bitmap to grayscaleToBinary

                Bitmap bitMapBinaryImage = GrayscaleToBin(saveImagedata);
                ivThumbnailPhoto.setImageBitmap(bitMapBinaryImage);

            }
        });

        thining = (Button) findViewById(R.id.btnthin);
        thining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bitmap bitMapthiningimage = prosesThin(saveImagedata);
                ivThumbnailPhoto.setImageBitmap(bitMapthiningimage);
                boolean success = false;
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/Thinned_images");
                myDir.mkdirs();
                Random generator = new Random();
                int n = 10000;
                n = generator.nextInt(n);
                String fname = "Image-"+ n +".jpg";
                File file = new File (myDir, fname);
                if (file.exists ()) file.delete ();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    Bitmap resized = Bitmap.createScaledBitmap(bitMapthiningimage, 160, 160, true);
                    resized.compress(Bitmap.CompressFormat.JPEG, 90, out);

                    out.flush();
                    out.close();
                    success = true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (success) {
                    Toast.makeText(getApplicationContext(), "Image saved with success",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(),
                        "Error during image saving", Toast.LENGTH_LONG).show();
               }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds options to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void selectImage() {

        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (options[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File f = new File(android.os.Environment.getExternalStorageDirectory(), "temp.jpg");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                    startActivityForResult(intent, 1);
                } else if (options[item].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 2);

                } else if (options[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                File f = new File(Environment.getExternalStorageDirectory().toString());
                for (File temp : f.listFiles()) {
                    if (temp.getName().equals("temp.jpg")) {
                        f = temp;
                        break;
                    }
                }
                try {
                    Bitmap bitmap;
                    BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                    bitmapOptions.inSampleSize = 4;
                    bitmap = BitmapFactory.decodeFile(f.getAbsolutePath(),
                            bitmapOptions);
                    ivThumbnailPhoto.setImageBitmap(bitmap);
                    saveImagedata = bitmap;
                    String path = android.os.Environment
                            .getExternalStorageDirectory()
                            + File.separator
                            + "Phoenix" + File.separator + "default";
                    f.delete();
                    OutputStream outFile = null;
                    File file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg");
                    try {
                        outFile = new FileOutputStream(file);

                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outFile);
                        outFile.flush();
                        outFile.close();
                        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 500, 500, true);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 2) {

                Uri selectedImage = data.getData();
                String[] filePath = {MediaStore.Images.Media.DATA};
                Cursor c = getContentResolver().query(selectedImage, filePath, null, null, null);
                c.moveToFirst();
                int columnIndex = c.getColumnIndex(filePath[0]);
                String picturePath = c.getString(columnIndex);
                c.close();
                Bitmap thumbnail = (BitmapFactory.decodeFile(picturePath));
                Log.w("path of image from gallery......******************.........", picturePath + "");
                ivThumbnailPhoto.setImageBitmap(thumbnail);
                Bitmap resized = Bitmap.createScaledBitmap(thumbnail, 500, 500, true);
                saveImagedata = resized;
            }
        }
    }

    private Bitmap prosesThin(Bitmap image) {
        int[][] imageData = new int[image.getHeight()][image.getWidth()];
        Color c;
        for (int y = 0; y < imageData.length; y++) {
            for (int x = 0; x < imageData[y].length; x++) {

                if (image.getPixel(x, y) == Color.BLACK) {
                    imageData[y][x] = 1;
                } else {
                    imageData[y][x] = 0;

                }
            }
        }

        ThinningService thinningService = new ThinningService();

        thinningService.Thining(imageData);

        for (int y = 0; y < imageData.length; y++) {

            for (int x = 0; x < imageData[y].length; x++) {

                if (imageData[y][x] == 1) {
                    image.setPixel(x, y, Color.BLACK);

                } else {
                    image.setPixel(x, y, Color.WHITE);
                }
            }
        }
          return image;
    }

    public static Bitmap GrayscaleToBin(Bitmap bm2)
    {
        Bitmap bm;
        bm=bm2.copy(Bitmap.Config.RGB_565, true);
        final   int width = bm.getWidth();
        final   int height = bm.getHeight();

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
public class ThinningService {

public int[][] Thining(int[][] binaryImage) {
    int a, b;
    List pointsToChange = new LinkedList();
    boolean hasChange;
    do {
        hasChange = false;
        for (int y = 1; y + 1 < binaryImage.length; y++) {
            for (int x = 1; x + 1 < binaryImage[y].length; x++) {
                a = getA(binaryImage, y, x);
                b = getB(binaryImage, y, x);
                if ( binaryImage[y][x]==1 && 2 <= b && b <= 6 && a == 1
                        && (binaryImage[y - 1][x] * binaryImage[y][x + 1] * binaryImage[y + 1][x] == 0)
                        && (binaryImage[y][x + 1] * binaryImage[y + 1][x] * binaryImage[y][x - 1] == 0)) {
                    pointsToChange.add(new Point(x, y));
                    //binaryImage[y][x] = 0;
                    hasChange = true;
                }
            }
        }

        for ( int i = 0; i < pointsToChange.size(); i++) {
            Point point = (Point)pointsToChange.get(i);
            binaryImage[point.getY()][point.getX()] = 0;
        }

        pointsToChange.clear();

        for (int y = 1; y + 1 < binaryImage.length; y++) {
            for (int x = 1; x + 1 < binaryImage[y].length; x++) {
                a = getA(binaryImage, y, x);
                b = getB(binaryImage, y, x);
                if ( binaryImage[y][x]==1 && 2 <= b && b <= 6 && a == 1
                        && (binaryImage[y - 1][x] * binaryImage[y][x + 1] * binaryImage[y][x - 1] == 0)
                        && (binaryImage[y - 1][x] * binaryImage[y + 1][x] * binaryImage[y][x - 1] == 0)) {
                    pointsToChange.add(new Point(x, y));

                    hasChange = true;
                }
            }
        }

        for ( int i = 0; i < pointsToChange.size(); i++) {
            Point point = (Point)pointsToChange.get(i);
            binaryImage[point.getY()][point.getX()] = 0;
        }

        pointsToChange.clear();

    } while (hasChange);

    return binaryImage;
}

        private class Point {

            private int x, y;

            public Point(int x, int y) {
                this.x = x;
                this.y = y;
            }

            public int getX() {
                return x;
            }

            public void setX(int x) {
                this.x = x;
            }

            public int getY() {
                return y;
            }

            public void setY(int y) {
                this.y = y;
            }
        };

        private int getA(int[][] binaryImage, int y, int x) {

            int count = 0;
            //p2 p3
            if (binaryImage[y - 1][x] == 0 && binaryImage[y - 1][x + 1] == 1) {
                count++;
            }
            //p3 p4
            if (binaryImage[y - 1][x + 1] == 0 && binaryImage[y][x + 1] == 1) {
                count++;
            }
            //p4 p5
            if (binaryImage[y][x + 1] == 0 && binaryImage[y + 1][x + 1] == 1) {
                count++;
            }
            //p5 p6
            if (binaryImage[y + 1][x + 1] == 0 && binaryImage[y + 1][x] == 1) {
                count++;
            }
            //p6 p7
            if (binaryImage[y + 1][x] == 0 && binaryImage[y + 1][x - 1] == 1) {
                count++;
            }
            //p7 p8
            if (binaryImage[y + 1][x - 1] == 0 && binaryImage[y][x - 1] == 1) {
                count++;
            }
            //p8 p9
            if (binaryImage[y][x - 1] == 0 && binaryImage[y - 1][x - 1] == 1) {
                count++;
            }
            //p9 p2
            if (binaryImage[y - 1][x - 1] == 0 && binaryImage[y - 1][x] == 1) {
                count++;
            }

            return count;
        }

        private int getB(int[][] binaryImage, int y, int x) {

            return binaryImage[y - 1][x] + binaryImage[y - 1][x + 1] + binaryImage[y][x + 1]
                    + binaryImage[y + 1][x + 1] + binaryImage[y + 1][x] + binaryImage[y + 1][x - 1]
                    + binaryImage[y][x - 1] + binaryImage[y - 1][x - 1];
        }
    }
}