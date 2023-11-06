package com.example.assignment_04;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class Location extends AppCompatActivity {

    private static final int GALLERY_REQUEST_CODE = 1;
    private ImageView userProfilePicture;
    private EditText id;
    private EditText userName;
    private EditText userEmail;
    private Button saveButton;
    private Button editButton;
    private Button updateButton;
    private Button deleteButton;
    private Uri selectedImageUri;

    private ProgressBar progressBar;
    private TextView uploadTextView;

    private DatabaseReference databaseReference;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        userProfilePicture = findViewById(R.id.userProfilePicture);
        id = findViewById(R.id.id);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        saveButton = findViewById(R.id.saveButton);
        editButton = findViewById(R.id.edit);
        updateButton = findViewById(R.id.update);
        deleteButton = findViewById(R.id.del);
        progressBar = findViewById(R.id.progressBar);
        uploadTextView = findViewById(R.id.txt);

        userProfilePicture.setOnClickListener(v -> openImageSelector());
        uploadTextView.setOnClickListener(v -> openImageSelector());

        saveButton.setOnClickListener(v -> saveUserData());
        editButton.setOnClickListener(v -> editUserData());
        updateButton.setOnClickListener(v -> updateUserData());
        deleteButton.setOnClickListener(v -> deleteUserData());

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
    }

    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            userProfilePicture.setImageURI(selectedImageUri);
        }
    }

    private void saveUserData() {
        String userId = id.getText().toString().trim();
        String name = userName.getText().toString().trim();
        String description = userEmail.getText().toString().trim();

        if (userId.isEmpty() || name.isEmpty() || description.isEmpty() || selectedImageUri == null) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(Location.this, "ID already exists. Please update the record instead.", Toast.LENGTH_SHORT).show();
                } else {
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                            .child("location_pictures/" + userId + ".jpg");
                    storageReference.putFile(selectedImageUri).addOnSuccessListener(taskSnapshot -> {
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String imageUrl = uri.toString();
                            User user = new User(name, description, imageUrl, Integer.parseInt(userId));
                            databaseReference.child(userId).setValue(user);

                            progressBar.setVisibility(View.GONE);
                            id.setText("");
                            userName.setText("");
                            userEmail.setText("");
                            userProfilePicture.setImageResource(0);

                            Toast.makeText(Location.this, "Record added successfully", Toast.LENGTH_SHORT).show();
                        });
                    }).addOnFailureListener(exception -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(Location.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Location.this, "Error checking user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editUserData() {
        String userId = id.getText().toString().trim();

        if (userId.isEmpty()) {
            Toast.makeText(this, "Please enter an ID to edit", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUser = dataSnapshot.getValue(User.class);

                    userName.setText(currentUser.getName());
                    userEmail.setText(currentUser.getDescription());

                    // Load and display the user's image
                    if (currentUser.getProfileImageUrl() != null && !currentUser.getProfileImageUrl().isEmpty()) {
                        // Use an image loading library like Picasso or Glide to load and display the image
                        // Here, I'm using Picasso as an example (make sure to add the Picasso library to your dependencies)
                        Picasso.get().load(currentUser.getProfileImageUrl()).into(userProfilePicture);
                    }

                    Toast.makeText(Location.this, "Editing user data", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Location.this, "ID does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(Location.this, "Error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateUserData() {
        if (currentUser != null) {
            String name = userName.getText().toString().trim();
            String description = userEmail.getText().toString().trim();

            if (!name.isEmpty() && !description.isEmpty()) {
                currentUser.setName(name);
                currentUser.setDescription(description);

                databaseReference.child(String.valueOf(currentUser.getId())).setValue(currentUser);

                Toast.makeText(this, "User data updated", Toast.LENGTH_SHORT).show();

                userName.setText("");
                userEmail.setText("");
                currentUser = null;
            } else {
                Toast.makeText(this, "Name and description are required", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Please edit a user before updating", Toast.LENGTH_SHORT).show();
        }
    }

        private void deleteUserData() {
            if (currentUser != null) {
                databaseReference.child(String.valueOf(currentUser.getId())).removeValue();
                Toast.makeText(this, "User data deleted", Toast.LENGTH_SHORT).show();

                userName.setText("");
                userEmail.setText("");
                currentUser = null;
            } else {
                Toast.makeText(this, "Please edit a user before deleting", Toast.LENGTH_SHORT).show();
            }
        }
}
