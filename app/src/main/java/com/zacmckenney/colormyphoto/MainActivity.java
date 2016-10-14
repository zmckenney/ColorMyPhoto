package com.zacmckenney.colormyphoto;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.algorithmia.APIException;
import com.algorithmia.AlgorithmException;
import com.algorithmia.Algorithmia;
import com.algorithmia.algo.AlgoFailure;
import com.algorithmia.algo.AlgoResponse;
import com.algorithmia.data.DataDirectory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private boolean mCameraSupported = false;
    private ImageView mThumbView;
    private Bitmap mThumbnail;
    private Bitmap bm;

    InputStream output;
    Bitmap bitmapFromStream;
    private Bitmap mThumbnailRotated;
    byte[] thumbByte;
    ContentValues values;
    Uri imageUri;

    private static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10;
    private String imageUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                Toast.makeText(this, "Needs Permission To Run Camera", Toast.LENGTH_LONG);

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }

        mCameraSupported = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mThumbView = (ImageView) findViewById(R.id.thumbnailImage);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void onTakePhotoClick(View view) {
        dispatchTakePictureIntent();
    }

    /**
     * Dispatches an {@link android.content.Intent} to take a photo. Result will be returned back
     * in onActivityResult().
     */
    private void dispatchTakePictureIntent() {
        values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Test Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Color My Photo");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);


        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            try {
                mThumbnail = MediaStore.Images.Media.getBitmap(
                        getContentResolver(), imageUri);
                imageUrl = getRealPathFromURI(imageUri);
                Log.v("@@@@@@@ ImageUri", "ImageURI for this image is : " + imageUri);
                Log.v("@@@@ imageURL", "THE IMAGE URL IS:  " + imageUrl);


                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                mThumbnailRotated = Bitmap.createBitmap(mThumbnail, 0, 0, mThumbnail.getWidth(), mThumbnail.getHeight(), matrix, true);

                bm = Bitmap.createBitmap(mThumbnailRotated.getWidth(), mThumbnailRotated.getHeight(),
                        Bitmap.Config.RGB_565);
                Canvas c = new Canvas(bm);
                Paint paint = new Paint();
                ColorMatrix cm = new ColorMatrix();
                cm.setSaturation(0);
                ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
                paint.setColorFilter(f);
                c.drawBitmap(mThumbnailRotated, 0, 0, paint);
                mThumbnailRotated.recycle();

                mThumbView.setImageBitmap(bm);


                ByteArrayOutputStream out = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 40, out);
                thumbByte = out.toByteArray();
//                shrunkThumbnail = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));



                new AsyncTask<Void,Void,AlgoResponse>() {

                    @Override
                    protected AlgoResponse doInBackground(Void... params) {

                        try {
                            return Algorithmia.client(getString(R.string.algorithmia_api_key)).algo("algo://deeplearning/ColorfulImageColorization/1.0.0").pipe(thumbByte);
                        } catch (APIException e) {
                            Log.v("@@@@ FAILED ALGO", "Failed to return an Algorithmia.client call");
                            return new AlgoFailure(new AlgorithmException(e));
                        }
                    }
                    @Override
                    protected void onPostExecute(final AlgoResponse response) {
                        if(response == null) {
                            Log.v("@@@@@@@@@@@ FAILED", " CONNECTION FAILED");
                        } else if(response.isSuccess()) {
                            Log.v("@@@@@@@@@@ SUCCESS", "SUCCESS ON CALL  ");


                            //If our response is a success then we will pull the data from the temp folder of our Algorithmia directory and set it as the image on screen
                            new AsyncTask<Void, Void, Bitmap>() {
                                @Override
                                protected Bitmap doInBackground(Void... voids) {

                                    Log.v("@@@@ Ran Network Async", "Ran the network Asynctask to get the image");
                                    DataDirectory temp = Algorithmia.client(getString(R.string.algorithmia_api_key)).dir("data://.algo/deeplearning/ColorfulImageColorization/temp");

                                    // Download file and get the file handle
                                    try {
                                        output = temp.file("output.png").getInputStream();
                                        bitmapFromStream = BitmapFactory.decodeStream(output);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return bitmapFromStream;
                                }

                                @Override
                                protected void onPostExecute(Bitmap bitmap) {

                                    if (bitmap == null){
                                        Log.v("@@@@NULL BITMAP", " Our bitmmap is null which is no good");
                                    }
                                    else {
                                        mThumbView.setImageBitmap(bitmap);
                                        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "test" + System.currentTimeMillis(), "TestingImage");
                                    }
                                }
                            }.execute();

//

                        } else {
                            AlgoFailure failure = (AlgoFailure) response;
                            Log.v("@@@@@@@@@@ FAIL", " ALGO FAIL - ERROR CODE " + failure.error );
                        }
                    }
                }.execute();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

}
