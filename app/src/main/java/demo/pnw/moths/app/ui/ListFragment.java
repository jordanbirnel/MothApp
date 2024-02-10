package demo.pnw.moths.app.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import demo.pnw.moths.app.MainActivity;
import demo.pnw.moths.app.R;


public class ListFragment extends Fragment{

    private DatabaseManager dbManager;
    View root;

    /**
     * Initializes the list fragment's user interface.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Returns the View for the fragment's UI.
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        dbManager = new DatabaseManager( getContext() );
        root = inflater.inflate(R.layout.fragment_list, container, false);
        updateView( );
        MainActivity main = (MainActivity) getActivity();
        main.cameraImage1 = null;
        main.cameraImage2 = null;
        main.cameraImage3 = null;
        main.cameraImage4 = null;
        return root;
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /**
     * Dynamically builds a view with all moth observations retrieved from the database.
     */
    public void updateView( ) {
        ArrayList<MothObservation> moments = dbManager.selectAll( );

        if( moments.size() > 0 ) {
            // Create ScrollView and GridLayout
            ScrollView scrollView = root.findViewById(R.id.momentScroll);
            scrollView.removeAllViews();
            GridLayout grid = new GridLayout( getContext() );
            grid.setRowCount( moments.size( ) );
            grid.setColumnCount( 6 );

            // Create arrays of components
            TextView [] ids = new TextView[moments.size( )];
            TextView[][] namesAndLocs = new TextView[moments.size( )][3]; //2
            ImageView[][] camImages = new ImageView[moments.size( )][4];
            Button[][] buttons = new Button[moments.size( )][2];
            ButtonHandler bh = new ButtonHandler( );
            UploadHandler upload = new UploadHandler( );

            // Retrieve width of screen
            Point size = new Point( );
            getActivity().getWindowManager( ).getDefaultDisplay( ).getSize( size );
            int width = size.x;

            int i = 0;
            for ( MothObservation moment : moments ) {
                // Create the TextView for the moths's id
                ids[i] = new TextView( getContext() );
                ids[i].setGravity( Gravity.CENTER );
                ids[i].setText( "" + moment.getIndex() );

                // Create the two TextViews for the moths's values
                namesAndLocs[i][0] = new TextView( getContext() );
                namesAndLocs[i][1] = new TextView( getContext() );
                namesAndLocs[i][2] = new TextView( getContext() );

                // Put the images of the moth taken by user into the views for the moth observation
                // Load the first image into the view, the rest into the onClick which will display 0-4 images
                ArrayList<Uri> images=new ArrayList<Uri>();
                for(int j=0; j < 4; j++){
                    camImages[i][j] = new ImageView( getContext() );

                    // See if the image is null
                    String tempImage = moment.getCameraImageK(j);
                    if (tempImage != null && !tempImage.equals("null")) {
                        Uri camera1 = Uri.parse(tempImage);
                        // Store this uri into the onClick listener for the image
                        images.add(camera1);
                    }
                }

                if(images.size() > 0) {
                    // Display the first image
                    Glide.with(getContext()).load(images.get(0)).into(camImages[i][0]);
                    camImages[i][0].setOnClickListener(new mothImageClick(images,moment.getIndex()));
                }

                //Show the name of the moth
                namesAndLocs[i][0].setText( beautifyName(moment.getMothName( )) );
                namesAndLocs[i][0].setTypeface(Typeface.DEFAULT,Typeface.ITALIC);
                namesAndLocs[i][0].setPadding(15, 0, 0, 0);

                namesAndLocs[i][1].setText( "" + moment.getMothLocation( ) );
                namesAndLocs[i][1]
                        .setInputType( InputType.TYPE_CLASS_NUMBER );
                namesAndLocs[i][0].setId( 10 * moment.getIndex( ) );
                namesAndLocs[i][1].setId( 10 * moment.getIndex( ) + 1 );

                // Create the upload button
                buttons[i][0] = new Button( getContext() );
                buttons[i][0].setText( "UPLOAD" );
                buttons[i][0].setAutoSizeTextTypeUniformWithConfiguration(
                        1, 17, 1, TypedValue.COMPLEX_UNIT_PT);
                buttons[i][0].setMinWidth(buttons[i][0].getAutoSizeMinTextSize());
                buttons[i][0].setId( moment.getIndex( )*100);

                // Create the remove button
                buttons[i][1] = new Button( getContext() );
                buttons[i][1].setText( "DELETE" );
                buttons[i][1].setAutoSizeTextTypeUniformWithConfiguration(
                        1, 17, 1, TypedValue.COMPLEX_UNIT_PT);
                buttons[i][1].setMinWidth(buttons[i][1].getAutoSizeMinTextSize());
                buttons[i][1].setId( moment.getIndex( ));

                // Set up event handling for uploading and deleting
                buttons[i][0].setOnClickListener(upload);
                buttons[i][1].setOnClickListener( bh );

                // Add the elements to grid
                grid.addView( ids[i], width / 10,
                        ViewGroup.LayoutParams.WRAP_CONTENT );
                grid.addView(camImages[i][0], width / 10,
                        ViewGroup.LayoutParams.WRAP_CONTENT );
                grid.addView( namesAndLocs[i][0], ( int ) ( width * .28 ),
                        ViewGroup.LayoutParams.WRAP_CONTENT );
                grid.addView( namesAndLocs[i][1], ( int ) ( width * .1 ),
                        ViewGroup.LayoutParams.WRAP_CONTENT );
                grid.addView( buttons[i][0], ( int ) ( width * .22 ),
                        ViewGroup.LayoutParams.WRAP_CONTENT );
                if(moment.isUploaded()){
                    //Log.e("TH","THUS");
                    buttons[i][0].setVisibility(View.INVISIBLE);
                }
                grid.addView( buttons[i][1], ( int ) ( width * .2 ),
                        ViewGroup.LayoutParams.WRAP_CONTENT );
                i++;
            }
            scrollView.addView( grid );
        }
        else{
            // Remove all the views as no data in local storage
            ScrollView scrollView = root.findViewById(R.id.momentScroll);
            scrollView.removeAllViews();
        }
    }

    /**
     * Saves a note associated with a specific moth observation.
     *
     * @param note The note to be saved.
     * @param spawnId The identifier for the moth observation the note is associated with.
     */
    public void saveNote(String note,int spawnId) {
        // Get the Shared Preferences editor
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Put the string into the editor under the key "note"
        editor.putString("note"+String.valueOf(spawnId), note);

        // Commit the changes
        editor.apply();
    }
    /**
     * Retrieves a saved note associated with a specific moth observation.
     *
     * @param spawnId The identifier for the moth observation the note is associated with.
     * @return The retrieved note or null if no note is associated.
     */
    public String getNote(int spawnId) {
        // Get the Shared Preferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);

        // Retrieve the value for the key "note"; if not found, return null
        return sharedPreferences.getString("note"+String.valueOf(spawnId), null);
    }
    /**
     * Handles click events on moth images in the list, showing more images as well as
     * a section to take notes on the current moth being observed
     */
    public class mothImageClick implements View.OnClickListener {
        // The four images of the moth
        int spawnId;
        ArrayList<Uri> images;

        public mothImageClick(ArrayList<Uri> images,int spawnId) {
            this.images = images;
            this.spawnId=spawnId;
        }
        @Override
        public void onClick(View v) {

            // onClick show the 4 images of the moth in the AlertDialog, an input field for notes, and a "Save" button
            LayoutInflater inflater = requireActivity().getLayoutInflater();
            View toDisplay = inflater.inflate(R.layout.moth_image_display, null);

            for(int i = 0; i < images.size(); i++) {
                // Set up inner view to display the images
                String mothIDVar = "moth" + Integer.toString(i);
                int resID = getActivity().getResources().getIdentifier(mothIDVar, "id", getActivity().getPackageName());
                ImageView moth0 = toDisplay.findViewById(resID);
                Glide.with(toDisplay).load(images.get(i)).into(moth0);
            }

            // Find the EditText for notes
            EditText notesEditText = toDisplay.findViewById(R.id.notesEditText);
            // Load and display the existing note
            String existingNote = getNote(spawnId); // Use the method defined above
            if (existingNote != null) {
                notesEditText.setText(existingNote);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setView(toDisplay)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Here, you can handle the saving of the notes
                            String notes = notesEditText.getText().toString();
                            saveNote(notes,spawnId);
                            // Save or use the notes string as needed
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
    }

    /**
     * Handles click events for delete buttons associated with moth observations.
     */
    private class ButtonHandler implements View.OnClickListener {
        public void onClick( View v ) {
            // Retrieve name and price of the candy
            int mothId = v.getId( );

            // Remove moth in database
            try {
                dbManager.deleteById( mothId);
                Toast.makeText( getContext(), "Observation Removed", Toast.LENGTH_SHORT ).show( );
                // Update screen
                updateView( );

            } catch( NumberFormatException nfe ) {
                Toast.makeText( getContext(), "Server error", Toast.LENGTH_LONG ).show( );
            }
        }
    }

    /**
     * Handles click events for upload buttons associated with moth observations, facilitating upload to Firebase.
     * In the future, a web app for vetting the uploaded moth images and locations
     * will be connected to the firebase storage
     */
    private class UploadHandler implements View.OnClickListener {
        public void onClick( View v ) {
            int mothId = (v.getId( ))/100;
            MothObservation moth = dbManager.selectById(mothId);
            MappedMoth upload = new MappedMoth(moth.getMothName(), moth.getLat(), moth.getLong());

            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("moths");
            myRef.child(upload.name).push().setValue(upload);
            v.setVisibility(View.INVISIBLE);
            dbManager.setUploaded(mothId);
        }
    }

    /**
     * @param givenString
     * Capitalizes first char, removes underscore for a space
     * sets textView to italic
     */
    public String beautifyName (String givenString)
    {
        String retString = "";
        String capital = givenString.substring(0,1).toUpperCase();
        givenString = capital + givenString.substring(1);
        retString = givenString.replaceAll("_"," ");
        return retString;
    }
}



