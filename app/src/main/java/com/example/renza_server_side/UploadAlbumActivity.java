package com.example.renza_server_side;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.renza_server_side.Model.Constants;
import com.example.renza_server_side.Model.Upload;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadAlbumActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonChoose;
    private Button buttonUpload;
    private EditText edittextName;
    private ImageView imageView;
    String songsCategory;
    private static final int PICk_IMAGE_REQUESt=234;
    private Uri filepath;
    StorageReference storageReference;
    DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_album);

        buttonChoose=findViewById(R.id.buttonChoose);
        buttonUpload=findViewById(R.id.buttonupload);
        edittextName=findViewById(R.id.edit_text);
        imageView=findViewById(R.id.imageview);
        storageReference= FirebaseStorage.getInstance().getReference();
        mDatabase= FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS);
        Spinner spinner=findViewById(R.id.spinner);

        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);

        List<String> categories=new ArrayList<>();

        categories.add("English Love Songs");
        categories.add("English Sad Songs");
        categories.add("English Party Songs");
        categories.add("English Birthday Songs");
        categories.add("Hindi Devotional Songs");
        categories.add("English Motivational Songs");
        categories.add("Lo fi Songs");
        categories.add("Hindi Love Songs");
        categories.add("Hindi Sad Songs");
        categories.add("Hindi Party Songs");
        categories.add("Hindi Birthday Songs");
        categories.add("Bengali Devotional Songs");
        categories.add("Hindi Motivational Songs");
        categories.add("Rap");
        categories.add("EDM");
        categories.add("chill beats");
        categories.add("Bengali Love Songs");
        categories.add("Bengali Sad Songs");
        categories.add("Punjabi Love Songs");
        categories.add("Punjabi Sad Songs");
        categories.add("Misc mix");

        ArrayAdapter<String> dataAdapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                songsCategory=adapterView.getItemAtPosition(i).toString();
                Toast.makeText(UploadAlbumActivity.this, "Selected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        if(view==buttonChoose){
            showFileChoose();
        }
        else if(view == buttonUpload){
            uploadFile();
        }
    }

    private void uploadFile() {

    if(filepath!=null){
        ProgressDialog progressDialog =new ProgressDialog(this);
        progressDialog.setTitle("uploading..");
        progressDialog.show();
        final StorageReference sRef =storageReference.child(Constants.STORAGE_PATH_UPLOADS
        + System.currentTimeMillis() + "." + getFileExtension(filepath));
        sRef.putFile(filepath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

            sRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    
                    String url=uri.toString();
                    Upload upload =new Upload(edittextName.getText().toString().trim(),url,songsCategory);
                    String uploadId=mDatabase.push().getKey();
                    mDatabase.child(uploadId).setValue(upload);
                    progressDialog.dismiss();
                    Toast.makeText(UploadAlbumActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
                    
                    
                }
            });
            
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                
                progressDialog.dismiss();

                Toast.makeText(UploadAlbumActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress =(100.0 * taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                progressDialog.setMessage("uploaded " +((int)progress)+ "%....");

            }
        });
    }
    }

    private void showFileChoose() {

        Intent intent =new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICk_IMAGE_REQUESt);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICk_IMAGE_REQUESt && resultCode == RESULT_OK && data != null && data.getData()!= null){
            filepath=data.getData();
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),filepath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public String getFileExtension(Uri uri){
        ContentResolver cr= getContentResolver();
        MimeTypeMap mime =MimeTypeMap.getSingleton();
        return mime.getMimeTypeFromExtension(cr.getType(uri));
    }
}