package demo.pnw.moths.app;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView navView;
    // URIs for images captured by the camera.
    public Uri cameraImage1;
    public Uri cameraImage2;
    public Uri cameraImage3;
    public Uri cameraImage4;
    // Flags indicating whether each image is set.
    public boolean pic1;
    public boolean pic2;
    public boolean pic3;
    public boolean pic4;
    // Location of the moth observation.
    public Location mothLocation;
    // Local file reference to the CSV downloaded from Firebase.
    public File csvKey;

    /**
     * Initializes the main activity, setting up navigation and downloading the CSV file if necessary.
     * @param savedInstanceState State of the application saved in a Bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            getCSV();
        }catch (Exception e){
            e.printStackTrace();
        }
        // Make the bottom nav bar and set it to make a custom sound
        navView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_map, R.id.navigation_dashboard, R.id.navigation_notifications, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }
    /**
     * Sets the specified pic flag based on the index.
     * @param index Index of the picture (0-3).
     * @param pic Flag indicating whether the picture is set.
     */
    public void setPic(int index, boolean pic) {
        switch(index) {
            case 0: this.pic1 = pic; break;
            case 1: this.pic2 = pic; break;
            case 2: this.pic3 = pic; break;
            case 3: this.pic4 = pic; break;
            default: break; // Optionally handle invalid index.
        }
    }

    /**
     * Sets the URI of a camera image based on the given index.
     * @param index Index of the camera image to set (0-3).
     * @param imageUri URI of the image.
     */
    public void setCameraImage(int index, Uri imageUri) {
        switch(index) {
            case 0: this.cameraImage1 = imageUri; break;
            case 1: this.cameraImage2 = imageUri; break;
            case 2: this.cameraImage3 = imageUri; break;
            case 3: this.cameraImage4 = imageUri; break;
            default: break; // Optionally handle invalid index.
        }
    }

    // Getter methods for each pic flag.
    public boolean getPic1(){ return pic1; }
    public boolean getPic2(){ return pic2; }
    public boolean getPic3(){ return pic3; }
    public boolean getPic4(){ return pic4; }

    /**
     * Sets the CSV file reference for the ID key.
     * @param f File reference to the downloaded CSV.
     */
    public void setCsvKey(File f){ csvKey = f; }

    /**
     * Attempts to download the CSV file from Firebase if it hasn't been downloaded yet.
     * @throws IOException if there's an error during the download process.
     */
    public void getCSV() throws IOException {

        if(csvKey==null) {

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

            StorageReference mothRef = storageRef.child("keyUpdate/recentkeycsv.csv");
            File localFile = File.createTempFile("csvKey","csv");
            mothRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created for the id key
                    setCsvKey(localFile);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    Log.e("FILE","FILE download Error");
                    Toast.makeText(MainActivity.this, "Failed to download file. Please check your connection and try again.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

} 