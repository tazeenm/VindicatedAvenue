package com.wordpress.keerthanasriranga.locations;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.R.attr.data;


public class MainActivity extends AppCompatActivity {

    private TextView get_place;
    int PLACE_PICKER_REQUEST=1;
    Button rateButton;
    float rating;
    RatingBar ratingBar;
    HashMap<LatLng,ArrayList<Float>> RateMap;
    String queriedLocation;
    String queriedname;
    Button searchButton;
    FirebaseDatabase fb;
    DatabaseReference dref;
    LatLng loclatlng;
    ArrayList<String> rateList = new ArrayList<String>();
    int flag =0;
    HashMap hm;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        get_place=(TextView)findViewById(R.id.textview);
        rateButton = (Button)findViewById(R.id.rateButton);
        ratingBar = (RatingBar)findViewById(R.id.ratingBar);


        searchButton=(Button)findViewById(R.id.search_button);
        final MainActivity myActivity = this;

        fb = FirebaseDatabase.getInstance();

        get_place.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                Intent intent;
                try {
                    intent = builder.build(myActivity);
                    startActivityForResult(intent, PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && flag == 0) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                String address = String.format("Place: %s", place.getAddress());
                queriedLocation = place.getId();
                queriedname = place.getName().toString();
                loclatlng = place.getLatLng();
                Log.v("PlaceId is", "" + queriedLocation);
                get_place.setText(address);
            }
        }
            if (requestCode == PLACE_PICKER_REQUEST && flag == 1) {
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(data, this);
                    String address = String.format("Place: %s", place.getAddress());
                    queriedLocation = place.getId();
                    loclatlng = place.getLatLng();
                    Log.v("Place Id is", "" + queriedLocation);
                    fetchrate();
                    flag=0;
                }

        }
    }

    public void fetchrate(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child(queriedLocation);
        ref.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Get map of
                        // in datasnapshot
                        Double sum = 0.0;
                        Double avg=0.0;

                        //collectPhoneNumbers((Map<String,Object>) dataSnapshot.getValue());
                        for (DataSnapshot item : dataSnapshot.getChildren()) {
                            Log.d("Snapshot:", item.getValue().toString());
                            rateList.add(item.getValue().toString());


                        }
                        for(String r : rateList){

                            Pattern p = Pattern.compile("[+-]?(([1-9][0-9]*)|(0))([.,][0-9]+)?");
                            Matcher m = p.matcher(r);
                            if(m.find())System.out.println(m.group(0));
                            sum+=Double.parseDouble(m.group(0));
                        }
                        avg = sum/rateList.size();
                        Log.i("Ratings are : ", rateList.toString());
                        Log.i("Sum is : ",sum.toString() );
                        Log.i("Average is : ",avg.toString() );
                        if(avg==0.0)
                            Toast.makeText(getApplicationContext(), "Not yet rated",
                                    Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getApplicationContext(), "Safety rating : " + avg.toString(),
                                    Toast.LENGTH_LONG).show();
                        avg = 0.0;
                        sum =0.0;
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //handle databaseError
                    }
                });
    }






   public void doneRating(View view){
       rating=ratingBar.getRating();
       Log.i("Longitude is", "" + queriedLocation);
       DatabaseReference myref = fb.getReference(queriedLocation);
       myref = myref.push();
       myref.child("Rating").setValue(rating);
       Toast.makeText(this, "Thanks for Rating "+rating, Toast.LENGTH_LONG).show();
       ArrayList<Float> rateList=new ArrayList<>();


   }

   public void getRating(View view){
       PlacePicker.IntentBuilder buildit = new PlacePicker.IntentBuilder();
       Intent intent;
       flag = 1;
       final MainActivity myActivity = this;
       try {
           intent = buildit.build(myActivity);
           startActivityForResult(intent, PLACE_PICKER_REQUEST);
       } catch (GooglePlayServicesRepairableException e) {
           e.printStackTrace();
       } catch (GooglePlayServicesNotAvailableException e) {
           e.printStackTrace();
       }
   }
   public void navigate(View view){
       Double lat = loclatlng.latitude;
       Double lang = loclatlng.longitude;
       Double lat1 = lat+1;
       Double lang1 = lang+1;
       Double lat2 = lat+2;
       Double lang2 = lang+2;
       Uri gmmIntentUri = Uri.parse("http://maps.google.com/maps?daddr=" + lat + "," + lang + "+to:" +lat1 + "," + lang1+ "+to:" +lat2 + "," + lang2);
       Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
       mapIntent.setPackage("com.google.android.apps.maps");
       startActivity(mapIntent);


   }
   public void getStreetView(View view){
       Double lat = loclatlng.latitude;
       Double lang = loclatlng.longitude;
       Uri gmmIntentUr = Uri.parse("google.streetview:cbll="+lat.toString()+","+lang.toString());
       Intent mapInten = new Intent(Intent.ACTION_VIEW, gmmIntentUr);
       mapInten.setPackage("com.google.android.apps.maps");
       startActivity(mapInten);

   }

}
