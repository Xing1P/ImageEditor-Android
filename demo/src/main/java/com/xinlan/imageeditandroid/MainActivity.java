package com.xinlan.imageeditandroid;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.xinlan.imageeditlibrary.editimage.EditImageActivity;
import com.xinlan.imageeditlibrary.editimage.utils.BitmapUtils;
import com.xinlan.imageeditlibrary.picchooser.SelectPictureActivity;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_PERMISSON_SORAGE = 1;

    public static final int SELECT_GALLERY_IMAGE_CODE = 7;
    public static final int TAKE_PHOTO_CODE = 8;
    public static final int ACTION_REQUEST_EDITIMAGE = 9;
    public static final int ACTION_STICKERS_IMAGE = 10;
    private MainActivity context;
    private ImageView imgView;
    private View openAblum;
    private View editImage;//
    private Bitmap mainBitmap;
    private int imageWidth, imageHeight;//
    private String path;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        context = this;
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        imageWidth = metrics.widthPixels;
        imageHeight = metrics.heightPixels;

        imgView = (ImageView) findViewById(R.id.img);
        openAblum = findViewById(R.id.select_ablum);
        editImage = findViewById(R.id.edit_image);

        openAblum.setOnClickListener(new SelectClick());
        editImage.setOnClickListener(new EditImageClick());
    }

    /**
     * 编辑选择的图片
     *
     * @author panyi
     */
    private final class EditImageClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (TextUtils.isEmpty(path)) {
                Toast.makeText(MainActivity.this, R.string.no_choose, Toast.LENGTH_SHORT).show();
                return;
            }

            Intent it = new Intent(MainActivity.this, EditImageActivity.class);
            it.putExtra(EditImageActivity.FILE_PATH, path);
            File outputFile = FileUtils.getEmptyFile("tietu"
                    + System.currentTimeMillis() + ".jpg");
            it.putExtra(EditImageActivity.EXTRA_OUTPUT,
                    outputFile.getAbsolutePath());
            MainActivity.this.startActivityForResult(it,
                    ACTION_REQUEST_EDITIMAGE);
        }
    }// end inner class

    private final class SelectClick implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                openAblumWithPermissionsCheck();
            } else {
                openAblum();
            }//end if
        }
    }// end inner class

    private void openAblum() {
        MainActivity.this.startActivityForResult(new Intent(
                        MainActivity.this, SelectPictureActivity.class),
                SELECT_GALLERY_IMAGE_CODE);
    }

    private void openAblumWithPermissionsCheck() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_PERMISSON_SORAGE);
            return;
        }
        openAblum();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_PERMISSON_SORAGE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openAblum();
        }//end if
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            // System.out.println("RESULT_OK");
            switch (requestCode) {
                case SELECT_GALLERY_IMAGE_CODE://
                    handleSelectFromAblum(data);
                    break;
                case ACTION_REQUEST_EDITIMAGE://
                    handleEditorImage(data);
                    break;
            }// end switch
        }
    }

    private void handleEditorImage(Intent data) {
        String newFilePath = data.getStringExtra("save_file_path");
        Toast.makeText(this, getString(R.string.save_path, newFilePath), Toast.LENGTH_LONG).show();
        //System.out.println("newFilePath---->" + newFilePath);
        LoadImageTask loadTask = new LoadImageTask();
        loadTask.execute(newFilePath);
    }

    private void handleSelectFromAblum(Intent data) {
        String filepath = data.getStringExtra("imgPath");
        path = filepath;
        // System.out.println("path---->"+path);
        LoadImageTask task = new LoadImageTask();
        task.execute(path);
    }


    private final class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... params) {
            return BitmapUtils.getSampledBitmap(params[0], imageWidth / 4, imageHeight / 4);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onCancelled(Bitmap result) {
            super.onCancelled(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            if (mainBitmap != null) {
                mainBitmap.recycle();
                mainBitmap = null;
                System.gc();
            }
            mainBitmap = result;
            imgView.setImageBitmap(mainBitmap);
        }
    }// end inner class

}//end class
