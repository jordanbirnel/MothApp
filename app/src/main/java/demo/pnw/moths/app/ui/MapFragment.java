// This has the java code to add all the moth location points of all moths in the database
// The moth database location entries are connected to a search bar in the fragment_map.xml file, and can be added/removed from the
// map based on the user's input in the search bar.

package demo.pnw.moths.app.ui;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;

import demo.pnw.moths.app.R;

public class MapFragment extends Fragment implements View.OnClickListener{
    private static GoogleMap myMap;

    private static ArrayList<String> mothNames;
    private static ArrayList<MappedMoth> mappedMoths;

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     * @param inflater LayoutInflater object that can be used to inflate views in the fragment.
     * @param container Parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, the fragment is being re-constructed from a previous saved state.
     * @return Returns the View for the fragment's UI.
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Initialize view
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize map frag
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);

        // Async map
        supportMapFragment.getMapAsync((new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                // Set the map to point at the pnw
                LatLng pos = new LatLng(47, -122);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 6.0f));

                myMap = googleMap;

                // Initialize the mothNames arraylist
                mothNames = new ArrayList<String>();
                mappedMoths = new ArrayList<MappedMoth>();
                try{
                    // Populate the moth array with Moth location and name data
                    DatabaseManager db = new DatabaseManager(getActivity());
                    ArrayList<MothObservation> allObservations = db.selectAll( );
                    if(allObservations.size() > 0){
                        for(MothObservation mothObservation: allObservations){
                            String name = mothObservation.getMothName();
                            double lat = mothObservation.getLat();
                            double lng = mothObservation.getLong();
                            MappedMoth mark = new MappedMoth(name, lat, lng);
                            mappedMoths.add(mark);
                            if(!mothNames.contains(name)){
                                mothNames.add(name);
                            }
                        }
                    }
                } catch(NullPointerException e){
                    System.out.println("Null Pointer exception occurred in map");
                }
                Button search = (Button) view.findViewById(R.id.searchButton);
                search.setOnClickListener(MapFragment.this);
                updateView(view);
            }
        }));
        return view;
    }

    /**
     * Handles click events on views.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(@NonNull View v) {
        try {
            // Clear the map to prevent old marks from showing
            myMap.clear();

            AutoCompleteTextView printedVal = null;
            printedVal = (AutoCompleteTextView) getActivity().findViewById(R.id.searchMothType);
            if(printedVal != null){

                for (int i = 0; i < mappedMoths.size(); i++) {
                    LatLng cords = new LatLng(mappedMoths.get(i).latitude, mappedMoths.get(i).longitude);
                    MarkerOptions markerOptions = new MarkerOptions();
                    // If mappedMoths contains the specified moth type name, add the coordinate
                    if (printedVal.getText().toString().equals(mappedMoths.get(i).name)) {
                        markerOptions.position(cords);
                        markerOptions.title(mappedMoths.get(i).name);
                        myMap.addMarker(markerOptions);
                    }
                }

            } else {
                Log.e("MAPs fragment","Unable to add markers");
            }
        } catch (NullPointerException e) {
            Log.e("MAPs fragment","Null Pointer exception occurred in map");
        }
        return;
    }
    /**
     * Updates the autocomplete text view with moth names for the search functionality.
     *
     * @param v The current view that contains the AutoCompleteTextView to be updated.
     */
    public void updateView(View v){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.select_dialog_item, mothNames);        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actv = (AutoCompleteTextView) v.findViewById(R.id.searchMothType);
        actv.setThreshold(1);
        // Will start working from first character
        actv.setAdapter(adapter);
        // Setting the adapter data into the AutoCompleteTextView
        actv.setTypeface(null, Typeface.BOLD_ITALIC);
        actv.setTextColor(Color.rgb(3,27,52));
    }
}