package demo.pnw.moths.app.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import demo.pnw.moths.app.MainActivity;
import demo.pnw.moths.app.R;

/**
 * Fragment for displaying and handling the ID key functionality for moth identification.
 * This fragment manages the UI for selecting moth attributes and displaying possible moth matches.
 */
public class IDkeyFragment<idCount> extends Fragment {

    // make a db to store the observations and an arraylist to keep track of them
    private DatabaseManager dbManager;
    public ArrayList<String> mothResults = new ArrayList<String>();
    //public static String tempName = "";
    //private IDkeyViewModel dashboardViewModel;
    int num_top_buttons = 0;
    public String mothInsertName = "";
    private boolean fileLoaded = false;
    private File localFile;

    /**
     * Loads state abbreviations from resources to map full state names to their abbreviations.
     *
     * @return A map of state full names to their abbreviations.
     */
    private Map<String, String> loadStateAbbreviationsMap() {
        Map<String, String> stateAbbreviationsMap = new HashMap<>();

        String[] stateNames = getResources().getStringArray(R.array.location_names);
        String[] stateAbbreviations = getResources().getStringArray(R.array.location_abbreviations);

        for (int i = 0; i < stateNames.length; i++) {
            stateAbbreviationsMap.put(stateNames[i], stateAbbreviations[i]);
        }

        return stateAbbreviationsMap;
    }

    /**
     * Lifecycle method called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return Return the View for the fragment's UI, or null.
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //dashboardViewModel = new ViewModelProvider(this).get(IDkeyViewModel.class);
        View root = inflater.inflate(R.layout.fragment_idkey, container, false);

        // camera code for retrieving images from the camera fragment
        MainActivity main = (MainActivity) getActivity();


        //Access the db
        dbManager = new DatabaseManager(getContext());

        // Assuming MainActivity has been refactored to use arrays for camera images and pic flags
        //MainActivity main = (MainActivity) getActivity();

// Initialize arrays for ImageView and Uri objects
        ImageView[] mothViews = {
                root.findViewById(R.id.imageMothButton0),
                root.findViewById(R.id.imageMothButton1),
                root.findViewById(R.id.imageMothButton2),
                root.findViewById(R.id.imageMothButton3)
        };

        Uri[] imageUris = {
                main.cameraImage1,
                main.cameraImage2,
                main.cameraImage3,
                main.cameraImage4
        };

        boolean[] picFlags = {
                main.getPic1(),
                main.getPic2(),
                main.getPic3(),
                main.getPic4()
        };

        // Loop through and set images based on active flags
        for (int i = 0; i < picFlags.length; i++) {
            if (picFlags[i]) { // If the pic flag for this index is true
                mothViews[i].setImageURI(imageUris[i]); // Set the image to the corresponding ImageView
                main.setPic(i, false); // Reset the flag
            }
        }

        //set the csvFile to be from the main activity so it is downloaded once from Firebase
        localFile = main.csvKey;


        return root;
    }

    /**
     * Called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned, but before any saved state has been restored in to the view.
     *
     * @param view               The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        MainActivity main = (MainActivity) getActivity();

        //download the csvKey data and create the buttons
        LinearLayout myLayout = (LinearLayout) getView().findViewById(R.id.main);
        TextView myView = new TextView(getContext());
        myView.setText("Loading Id Key Buttons...");
        myLayout.addView(myView);
        try {
            createButtons(myView);
        } catch (IOException e) {
            showToast("Error Loading Buttons");
            //Log the error for debugging purposes
            Log.e("IDKeyFragment", "Error loading buttons", e);
        }


    }

    /**
     * Displays a Toast message safely from any thread.
     *
     * @param message The message to be displayed in the Toast.
     */
    public void showToast(final String message) {
        if (getActivity() == null) {
            Log.e("IDKeyFragment", "Error displaying toast");
            return; // Return if getActivity() is null to avoid NullPointerException
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Creates buttons based on the ID data loaded from a CSV file.
     *
     * @param myView The view where buttons will be displayed.
     */
    public void createButtons(View myView) throws IOException {

        //if we have already downloaded the csvFile to the temp file on the app
        if (fileLoaded) {
            IdData data = new IdData(localFile);
            //try to display buttons, catch if user switches screens resulting in null or void exceptions
            try {
                displayButtons(data, myView);
            } catch (Exception e) {
                showToast("Error Displaying Buttons");
                Log.e("IDKeyFragment", "Error displaying buttons", e);

            }
        }

        //if we need to download the csvkey file from firebase
        else {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
            StorageReference mothRef = storageRef.child("keyUpdate/recentkeycsv.csv");
            localFile = File.createTempFile("csvKey", "csv");
            mothRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    // create the id key buttons here
                    IdData data = new IdData( localFile);
                    //try to display buttons, catch if user switches screens resulting in null or void exceptions
                    try {
                        displayButtons(data, myView);
                    } catch (Exception e) {

                        showToast("Error Displaying Buttons");
                        Log.e("IDKeyFragment", "Error displaying buttons", e);

                    }
                    fileLoaded = true;


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                    showToast("Network error, please check your connection");
                }
            });

        }
    }

    /**
     * Dynamically creates and displays buttons based on the attributes of the ID data tree.
     * Each button represents a top-level attribute, and clicking a button will trigger further actions defined in the parentButtonClick listener.
     *
     * @param data The ID data object containing the tree of attributes to display.
     * @param myView A view that may be removed before adding buttons to the layout. Typically, a placeholder view indicating loading or initialization state.
     */
    public void displayButtons(IdData data, View myView) {

        // Attempt to set up the attribute tree from the ID data.
        // If unsuccessful, display an error message via toast.
        try {
            data.setUpTree();
        } catch (Exception e) {
            showToast("Error setting up ID Key, contact support to ensure the key is current");
        }

        // Retrieve top-level buttons attributes from ID data.
        ArrayList<Attribute> buttonAttrs = data.getButtons(1);

        // Early exit if the fragment's view is no longer available.
        if (getView() == null) {
            return;
        }

        // Get a reference to the layout where buttons will be added.
        LinearLayout myLayout = (LinearLayout) getView().findViewById(R.id.main);
        myLayout.removeView(myView);

        // Iterate through all top-level attributes to create buttons.
        for (int i = 0; i < buttonAttrs.size(); i++) {

            // create the button view
            Button myButton = new Button(getContext());
            ViewGroup.LayoutParams p = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1.0f);

            // set the margins
            myButton.setLayoutParams(p);
            myButton.setPadding(20, 20, 20, 20);


            // Configure button appearance and text.
            myButton.setText(buttonAttrs.get(i).name);
            myButton.setAutoSizeTextTypeUniformWithConfiguration(
                    1, 12, 1, TypedValue.COMPLEX_UNIT_PT);
            myButton.setWidth(460);
            myButton.setHeight(200);
            myButton.setGravity(Gravity.CENTER_HORIZONTAL);
            // Set a unique ID for each button, based on its position in the list.
            myButton.setId(1000 * (i + 1));

            // increment and set listener so id key is changed onClick
            num_top_buttons++;
            myButton.setOnClickListener(new parentButtonClick(buttonAttrs.get(i), data));
            myButton.setBackgroundColor(getResources().getColor(R.color.pnwYellow));
            myLayout.addView(myButton);
        }


    }

    /**
     * Retrieves the state abbreviation based on the full state name.
     *
     * @param stateName The full name of the state.
     * @return The abbreviation of the state if found, or null.
     */
    public String getStateAbbreviation(String stateName) {
        Map<String, String> stateAbbreviationsMap = loadStateAbbreviationsMap();
        // Return the abbreviation, or null if not found
        return stateAbbreviationsMap.get(stateName);
    }

    /**
     * Listener for dialog actions. Handles both positive and negative button clicks.
     * Adds moth information to db on positive click
     */
    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            MainActivity main = (MainActivity) getActivity();
            Uri image1 = main.cameraImage1;
            Uri image2 = main.cameraImage2;
            Uri image3 = main.cameraImage3;
            Uri image4 = main.cameraImage4;
            String camImage1 = null;
            String camImage2 = null;
            String camImage3 = null;
            String camImage4 = null;
            double latitude = 49;
            double longitude = 122;
            if (image1 != null) {
                camImage1 = image1.toString();
            }
            if (image2 != null) {
                camImage2 = image2.toString();
            }
            if (image3 != null) {
                camImage3 = image3.toString();
            }
            if (image4 != null) {
                camImage4 = image4.toString();
            }

            if (main.mothLocation != null) {
                latitude = main.mothLocation.getLatitude();
                longitude = main.mothLocation.getLongitude();
            }
            //Get abbreviation of state name
            String state = getState(latitude, longitude);

            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    try {
                        MothObservation tempMoth = new MothObservation(0, mothInsertName, state, camImage1, camImage2, camImage3, camImage4, latitude, longitude, false);
                        dbManager.insert(tempMoth);
                        showToast("Observation Added!");
                    } catch (NumberFormatException nfe) {
                        showToast("Error saving, please try again");
                    }
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    };
    /**
     * Attempts to determine the state based on latitude and longitude using Geocoder.
     * @param latitude The latitude of the location.
     * @param longitude The longitude of the location.
     * @return The state abbreviation if found, an empty string otherwise.
     */
    public String getState(double latitude, double longitude) {
        String state = "";
        try {
            // Looping once
            Geocoder geocoder;
            List<Address> addresses;
            geocoder = new Geocoder(getContext(), Locale.getDefault());
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            state = addresses.get(0).getAdminArea();
            state = getStateAbbreviation(state);
            return state;
        } catch (IOException e) {
            showToast("Error retrieving location");
            Log.e("IDKeyFragment", "Error retrieving location", e);
            return "";
        }
    }
    /**
     * OnClickListener for ID button clicks. Initiates the process to add a moth observation.
     */
    public class idButtonClick implements View.OnClickListener {
        int spawnId;

        public idButtonClick(int id) {
            spawnId = id;
        }

        @Override
        public void onClick(View v) {
            mothInsertName = mothResults.get(spawnId);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Add Moth Observation?").setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();
        }
    }
    /**
     * OnClickListener for parent button clicks in the identification key.
     * Handles showing and hiding of child buttons based on the current selection.
     */
    public class parentButtonClick implements View.OnClickListener {
        Attribute attr;
        IdData data;
        int spawnId;
        boolean clicked = false;

        public parentButtonClick(Attribute attr, IdData data) {
            this.attr = attr;
            this.data = data;
            spawnId = (attr.layer * 100);
        }

        @Override
        public void onClick(View v) {
            // reveal all of the child buttons
            if (!clicked) {

                int size = attr.children.size();
                LinearLayout myLayout = getActivity().findViewById(R.id.secondLayer);

                // if we jump to a different parent button from a lower button
                if (attr.layer == 1) {

                    // turn off all other parent buttons' lower views
                    ((ViewGroup) getActivity().findViewById(R.id.secondLayer)).removeAllViews();
                    Button tempButton;
                } else {
                    //if we are in a lower level we need to remove views from each parent view in the list
                    ViewGroup secondLayer = ((ViewGroup) getActivity().findViewById(R.id.secondLayer));
                    int numChildren = secondLayer.getChildCount();
                    for (int i = attr.layer - 1; i < numChildren; i++) {
                        secondLayer.removeViewAt(attr.layer - 1);
                    }

                }
                ViewGroup parentGroup = (ViewGroup) v.getParent();
                int numChildren = parentGroup.getChildCount();
                for (int i = 0; i < numChildren; i++) {
                    Button b = (Button) parentGroup.getChildAt(i);
                    ColorDrawable bg = (ColorDrawable) b.getBackground();
                    if (bg.getColor() == getResources().getColor(R.color.pnwGreen)) {
                        b.performClick();
                    }
                }


                // set the parent button color
                v.setBackgroundColor(getResources().getColor(R.color.pnwGreen));

                //Create new Scroll View and Linear Layout within to display buttons down the page
                ScrollView myScroll = new ScrollView(getActivity().getApplicationContext());

                // set params for the scroll view
                myScroll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1.0f));
                myLayout.addView(myScroll);
                spawnId += 1;
                myScroll.setId(spawnId);

                // create the linear layout, and set the params for the scrollview inside of it
                LinearLayout newLayout = new LinearLayout(getActivity().getApplicationContext());
                newLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                newLayout.setOrientation(LinearLayout.VERTICAL);
                myScroll.addView(newLayout);

                // create child buttons for that scrollview
                for (int i = 0; i < attr.children.size(); i++) {

                    // add the button with the correct idkey data
                    Button myButton = new Button(getActivity().getApplicationContext());
                    myButton.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            1.0f));
                    myButton.setPadding(20, 100, 20, 20);
                    myButton.setText(attr.children.get(i).name);
                    myButton.setAutoSizeTextTypeUniformWithConfiguration(
                            1, 17, 1, TypedValue.COMPLEX_UNIT_PT);
                    myButton.setWidth(460);

                    //add OnclickListener if parent button just add reveal listener, else add select attribute listener
                    if (attr.children.get(i).parent) {
                        myButton.setOnClickListener(new parentButtonClick(attr.children.get(i), data));
                    } else {
                        myButton.setOnClickListener(new childButtonClick(attr.children.get(i), data));
                        myButton.setOnLongClickListener(new childButtonClickLong(attr.children.get(i)));
                    }
                    if (attr.children.get(i).selected) {
                        myButton.setBackgroundColor(getResources().getColor(R.color.pnwGreen));
                    } else {
                        myButton.setBackgroundColor(getResources().getColor(R.color.pnwYellow));
                    }
                    newLayout.addView(myButton);
                }
                clicked = true;
                return;
            } else {

                // if the the views are active and unclicked then remove their children
                if (getActivity().findViewById(spawnId) != null) {
                    ((ViewGroup) getActivity().findViewById(spawnId)).removeAllViews();
                    ((ViewGroup) getActivity().findViewById(spawnId).getParent()).removeView(getActivity().findViewById(spawnId));
                }

                // if we are the top layer and unclick, remove all lower views
                if (attr.layer == 1) {
                    if (getActivity().findViewById(R.id.secondLayer) != null) {
                        ((ViewGroup) getActivity().findViewById(R.id.secondLayer)).removeAllViews();
                    }
                }
                clicked = false;
                v.setBackgroundColor(getResources().getColor(R.color.pnwYellow));
                return;
            }
        }
    }
    /**
     * OnClickListener for child button  clicks in the identification key.
     * Narrows down results for moth identification
     */
    public class childButtonClick implements View.OnClickListener {
        Attribute attr;
        IdData data;
        boolean clicked = false;

        public childButtonClick(Attribute attr, IdData data) {
            this.attr = attr;
            this.data = data;
        }

        @Override
        public void onClick(View v) {
            //this method will take the number of the attr, search with all the other attr's selected, and display moth results
            TextView resultView = getView().findViewById(R.id.results);
            HashSet<Integer> results = new HashSet<Integer>();
            if (this.attr.selected) {
                results = data.removeResults(attr);
                v.setBackgroundColor(getResources().getColor(R.color.pnwYellow));
            } else {
                results = data.searchResults(attr);
                v.setBackgroundColor(getResources().getColor(R.color.pnwGreen));
            }
            this.attr.selected = !this.attr.selected;
            resultView.setText("Moth Results: " + Integer.toString(results.size()) + " matches (will show at most 20) ");

            // if the amount of results is 20 or less, imageidkey0 to 19 inclusive
            int resSize = results.size();
            int idCount = 0;
            if (resSize <= 20) {
                int counter = 0;
                // go through each result and obtain an image from the database
                mothResults.clear();
                for (Integer currInt : results) {

                    // Remove capitals and spaces
                    String tempName = data.getName(currInt);
                    tempName = tempName.toLowerCase();
                    tempName = tempName.replace(" ", "_");
                    mothResults.add(tempName);

                    // Get the asset
                    // String imgPath = "thumbs/" + tempName + "_a_d_tn.jpg";
                    String imgPath = "hdMoths/" + tempName + "_a_d.jpg";

                    // With the image, set the imageidkey# to it, also set the textidkey#
                    String imageIDVar = "imageidkey" + Integer.toString(idCount);
                    String textIDVar = "textidkey" + Integer.toString(idCount);
                    idCount++;
                    int resID = getActivity().getResources().getIdentifier(imageIDVar, "id", getActivity().getPackageName());
                    int resIDText = getActivity().getResources().getIdentifier(textIDVar, "id", getActivity().getPackageName());

                    StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                    StorageReference mothRef = storageRef.child(imgPath);


                    // set the resIDText to the moth species
                    TextView mothTextView = (TextView) getActivity().findViewById(resIDText);
                    // process tempName to be presentable
                    mothTextView.setText(beautifyName(tempName));

                    mothRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            // set the image URI from the remote database
                            String imageURL = uri.toString();
                            ImageView mothView = (ImageView) getActivity().findViewById(resID);
                            Glide.with(getActivity()).load(imageURL).into(mothView);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // set the image URI from the default image
                            ImageView mothView = (ImageView) getActivity().findViewById(resID);
                            mothView.setImageResource(R.drawable.id_moth);

                            // set the resIDText to the moth species but leave an error note
                            TextView mothTextView = (TextView) getActivity().findViewById(resIDText);
                            mothTextView.setText(mothTextView.getText() + "\nNo image found.");
                        }
                    });

                    // do not set more than 19 images
                    if (counter > 19) {
                        break;
                    }
                }

                //Clear old results
                for (int i = resSize; i < 20; i++) {
                    // With the image, set the imageidkey# to it, also set the textidkey#
                    String imageIDVar = "imageidkey" + Integer.toString(idCount);
                    String textIDVar = "textidkey" + Integer.toString(idCount);
                    idCount++;
                    int resID = getActivity().getResources().getIdentifier(imageIDVar, "id", getActivity().getPackageName());
                    int resIDText = getActivity().getResources().getIdentifier(textIDVar, "id", getActivity().getPackageName());
                    // set the resIDText to the moth species
                    TextView mothTextView = (TextView) getActivity().findViewById(resIDText);
                    // process tempName to be presentable
                    mothTextView.setText("");
                    ImageView mothView = (ImageView) getActivity().findViewById(resID);
                    mothView.setImageResource(R.drawable.id_moth);
                    mothView.setOnClickListener(null);
                }
                // onClick for each button
                // line up to the mothref name so we can send it to a local persistent storage
                for (int i = 0; i < resSize; i++) {
                    String imageIDVar = "imageidkey" + Integer.toString(i);
                    int resID = getActivity().getResources().getIdentifier(imageIDVar, "id", getActivity().getPackageName());
                    ImageView mothView = (ImageView) getActivity().findViewById(resID);
                    mothView.setOnClickListener(new idButtonClick(i));
                }
            }
        }
    }

    /**
     * OnClickListener for child button long clicks in the identification key.
     * Displays images related to the selected attribute for better identification.
     */
    public class childButtonClickLong implements View.OnLongClickListener {
        Attribute attr;

        public childButtonClickLong(Attribute attr) {
            this.attr = attr;
        }

        @Override
        public boolean onLongClick(View v) {

            //set up inner view to display the four images
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View toDisplay = inflater.inflate(R.layout.attribute_image_display, null);

            //get the text of this child button. This will be used
            //to get the corresponding image to display
            Button b = (Button) v;
            String message = b.getText().toString();
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(toDisplay).setMessage(message).show();

            StorageReference storageRef = FirebaseStorage.getInstance().getReference();

            //ADD 2 to align with db indexing
            int num = Integer.parseInt(attr.number) + 2;

            String imageUrl = "keyUpdate/keyImages/" + Integer.toString(num) + ".jpg";
            StorageReference mothRef = storageRef.child(imageUrl);

            mothRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    // set the image URI from the remote database
                    String imageURL = uri.toString();
                    ImageView mothView = (ImageView) toDisplay.findViewById(R.id.attribute);
                    Glide.with(getActivity())
                            .load(imageURL)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.id_moth)
                                    .error(R.drawable.id_moth))
                            .into(mothView);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // No image at this location, instead show default not found message and image
                    ImageView mothView = (ImageView) toDisplay.findViewById(R.id.attribute);
                    Glide.with(getActivity())
                            .load(R.drawable.id_moth)
                            .into(mothView);
                }
            });


            return true;
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mothResults.clear();
    }

    /**
     * @param givenString Capitalizes first char, removes underscore for a space
     *                    sets textView to italic
     *                    returns a beautiful version of the moth's name
     */
    public static String beautifyName(String givenString) {
        String retString = "";
        String capital = givenString.substring(0, 1).toUpperCase();
        givenString = capital + givenString.substring(1);
        retString = givenString.replaceAll("_", " ");
        return retString;
    }

}