package com.example.fireappimage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button chooseButton,saveButton,displayButton;
    private EditText imageNameEditText;
    private ImageView imageView;
    private ProgressBar progressBar;
    private Uri imageUri;
    StorageTask uploadTask;

    DatabaseReference databaseReference;
    StorageReference storageReference;

    private static final int IMAGE_REQUEST=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference= FirebaseDatabase.getInstance().getReference("Upload");
        storageReference= FirebaseStorage.getInstance().getReference("Upload");

        chooseButton=findViewById(R.id.chooseImagebtn);
        saveButton=findViewById(R.id.saveImagebtn);
        displayButton=findViewById(R.id.displayImagebtn);
        progressBar=findViewById(R.id.progressBar);

        imageNameEditText=findViewById(R.id.imageNameEt);
        imageView=findViewById(R.id.imageView);

        saveButton.setOnClickListener(this);
        displayButton.setOnClickListener(this);
        chooseButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){

            case R.id.chooseImagebtn:
                openFileChooser();
                break;

            case R.id.saveImagebtn:
                if (uploadTask!=null && uploadTask.isInProgress())
                {
                    Toast.makeText(getApplicationContext(),"Upload In Progress.....",Toast.LENGTH_LONG).show();

                }
                else {
                    saveData();
                }
                break;

            case R.id.displayImagebtn:
                Intent intent=new Intent(MainActivity.this,ImageActivity.class);
                startActivity(intent);
                break;
        }

    }

    public String getFileExtension(Uri imageUri)
    {
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return  mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    private void saveData() {
    final String imageName=imageNameEditText.getText().toString().trim();
    if (imageName.isEmpty())
    {
        imageNameEditText.setError("Enter the image name");
        imageNameEditText.requestFocus();
        return;
    }
    StorageReference ref=storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));

        ref.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        Toast.makeText(getApplicationContext(),"Image stored Succesful",Toast.LENGTH_LONG).show();
                        Log.d("SAVE", "onSuccess:........................................ ");

                        Task<Uri> urlTask=taskSnapshot.getStorage().getDownloadUrl();

                        while (!urlTask.isSuccessful());

                        Uri downloadUrl=urlTask.getResult();

                        Upload upload=new Upload(imageName,downloadUrl.toString());

                        String uploadId=databaseReference.push().getKey();
                        databaseReference.child(uploadId).setValue(upload);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Toast.makeText(getApplicationContext(),"Not Succesful",Toast.LENGTH_LONG).show();

                        // Handle unsuccessful uploads
                        // ...
                    }
                });
    }

    private void openFileChooser() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            imageUri=data.getData();
            Picasso.get().load(imageUri).into(imageView);
        }
    }
}
