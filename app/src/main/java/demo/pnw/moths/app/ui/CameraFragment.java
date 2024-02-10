package demo.pnw.moths.app.ui;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.File;

import demo.pnw.moths.app.MainActivity;
import demo.pnw.moths.app.R;

import static android.app.Activity.RESULT_OK;

public class CameraFragment extends Fragment {

    // Constants for permission request codes

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;

    // Boolean flags to track which image is being captured
    public boolean image1 = false;
    public boolean image2 = false;
    public boolean image3 = false;
    public boolean image4 = false;

    // Tracks the index of the currently active image
    private int activeImageIndex = -1;

    // Location services variables
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    // Arrays to hold references to ImageView widgets and their IDs
    int[] takenImageIds = {R.id.TakenImage1, R.id.TakenImage2, R.id.TakenImage3, R.id.TakenImage4};
    ImageView[] takenImages = new ImageView[takenImageIds.length];

    Uri image_uri;
    /**
     * Creates and returns the view hierarchy associated with the fragment.
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);

        // Set up location access
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        createLocationRequest();
        buildLocationCallback();
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());



        // Initialize ImageView references in a loop
        for (int i = 0; i < takenImageIds.length; i++) {
            takenImages[i] = root.findViewById(takenImageIds[i]);
        }

        int[] iconButtonIds = {R.id.iconImage1, R.id.iconImage2, R.id.iconImage3, R.id.iconImage4};
        for (int i = 0; i < iconButtonIds.length; i++) {
            ImageView iconButton = root.findViewById(iconButtonIds[i]);
            final int index = i;
            iconButton.setOnClickListener(v -> handleCameraIconClick(index));
        }

        return root;
    }

    /**
     * Handles click events on camera icon buttons to open the camera.
     * @param index Index of the button clicked.
     */
    private void handleCameraIconClick(int index) {
        boolean[] imageFlags = {image1, image2, image3, image4}; // Assuming these are defined elsewhere
        activeImageIndex = index; // Update the active image index

        // Set the corresponding flag to true
        imageFlags[index] = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            // Check if we have camera and write permissions
            if (getContext().checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                    getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                // Request permissions
                requestPermissions(permissions, PERMISSION_CODE);
            } else {
                // Permissions granted, open camera
                openCamera();
            }
        } else {
            // Permission is automatically granted on devices below Marshmallow upon installation
            openCamera();
        }
    }

    /**
     * Opens the camera to capture an image.
     */
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        // Set the camera intent to the screen
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE);
    }

    /**
     * Handles the result of permission requests initiated by requestPermissions.
     *
     * @param requestCode  The request code passed in requestPermissions.
     * @param permissions  The requested permissions.
     * @param grantResults The grant results for the corresponding permissions which is either PERMISSION_GRANTED or PERMISSION_DENIED.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // This method is called when the user presses ALLOW or DENY in the request permissions popup
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    // Permission denied! No camera access
                    Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Handles the result from starting an activity with startActivityForResult,
     * which occurs from utilizing the camera app
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK) {
            if (activeImageIndex != -1 && activeImageIndex < takenImages.length) {
                // Update the ImageView corresponding to the active index
                ImageView activeImageView = takenImages[activeImageIndex];
                activeImageView.setImageURI(image_uri);

                // Update the MainActivity with the new image URI
                MainActivity main = (MainActivity) getActivity();
                if (main != null) {
                    Uri[] cameraImages = {main.cameraImage1, main.cameraImage2, main.cameraImage3, main.cameraImage4};
                    boolean[] picFlags = {main.pic1, main.pic2, main.pic3, main.pic4};

                    // Update the corresponding URI and flag in MainActivity
                    //cameraImages[activeImageIndex] = image_uri;
                    //picFlags[activeImageIndex] = true;
                    main.setPic(activeImageIndex,true);
                    main.setCameraImage(activeImageIndex, image_uri);
                    // Simplify by removing redundant flags and directly setting values in MainActivity if possible
                    getCurrentLocation();
                }

                // Reset the active image index
                activeImageIndex = -1;
            }
        }
    }


    /**
     * Initiates a request for the device's current location.
     * Ensures necessary permissions are granted before accessing location information.
     */
    public void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                //Get last known location
                if (location != null) {
                    currentLocation = location;
                    MainActivity main = (MainActivity) getActivity();
                    main.mothLocation = currentLocation;
                } else {
                    Toast.makeText(getContext(), "Cannot access location, please check your settings!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Configures the LocationRequest with specific criteria for location updates.
     * Defines the interval, accuracy, and fastest interval for location requests.
     */
    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(100000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Prepares a callback for handling location update results.
     * This callback is passed to requestLocationUpdates to receive location updates.
     */
    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    currentLocation = location;
                }
            }

            ;
        };
    }
}