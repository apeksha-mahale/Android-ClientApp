package com.example.clientapp;

import android.Manifest;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    String value = null;
    //String closest=null;
    GPSTracker gps;
    final String[] ambValue = {null};
    int av=0;

    private static final int REQUEST_CODE_PERMISSION = 2;
    String mPermission = Manifest.permission.ACCESS_FINE_LOCATION;
    TextView minAmbText;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        minAmbText = (TextView) findViewById(R.id.minAmb);

        Button clickBut = (Button) findViewById(R.id.clickBut);


        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("AmbulanceList");
        DatabaseReference zero = myRef.child("0");
        DatabaseReference zero1 = zero.child("Location");



        zero1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                value=dataSnapshot.getValue(String.class);
                Log.i("VaLue", value);
                String [] separated = value.split(",");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });
    }
    public void buttonClicked(View view){
        /*Intent intent=new Intent(MainActivity.this,MapsActivity.class);
        intent.putExtra("LOCVAL",value);
        startActivity(intent);*/
        findClosestLocation();
    }


    public void findClosestLocation() {
        final int[] closestAmb = {0};
        final int flag=0;
        final String[] locArray = new String[3];
        for(int x=0;x<=2;x++) {
            final int m = x;
            DatabaseReference myR = FirebaseDatabase.getInstance().getReference("AmbulanceList");
            final DatabaseReference DLoc5 = myR.child(x + "");
            DatabaseReference DDStatus = DLoc5.child("State");
            DDStatus.addValueEventListener(new ValueEventListener() {
                @Override

                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i("State Value ",dataSnapshot.getValue(String.class) );
                    final String statusVal = dataSnapshot.getValue(String.class);
                    if (statusVal.equals("0")||m==2) {
                        DatabaseReference DDLoc = DLoc5.child("Location");
                        Log.i("State Value inside",dataSnapshot.getValue(String.class) );
                        DDLoc.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (statusVal.equals("0")) {
                                    ambValue[0] = dataSnapshot.getValue(String.class);
                                    Log.i("Coordinates", ambValue[0]);
                                    locArray[m] = ambValue[0];
                                    Log.i("Coordinates of m", Integer.toString(m) + " : " + ambValue[0] + " , " + locArray[m]);
                                }
                                    Double[] dis = new Double[3];
                                    int j = -1;
                                    Double minDist = Double.MAX_VALUE;

                                if (m == 2) {

                                    for (int y = 0; y <= 2; y++)
                                        Log.i("Coordinates of y", Integer.toString(y) + " , " + locArray[y]);
                                    for(int u=0;u<=2;u++)dis[u]= Double.MAX_VALUE;
                                    int minAmb = -1;
                                    int c=0;
                                    for (String eachLoc : locArray) {
                                        String[] latlong = new String[1];
                                        j++;
                                        if (eachLoc != null) {
                                            latlong = eachLoc.split(",");
                                            double latitude = Double.parseDouble(latlong[0]);
                                            double longitude = Double.parseDouble(latlong[1]);
                                            LatLng latLangLocation = new LatLng(latitude, longitude);
                                            Double distance = CalculateDistance(latLangLocation);
                                            Log.i("Distance" + Integer.toString(j) + " : ", Double.toString(distance));
                                            dis[j] = distance;
                                            Log.i("Coordinates of distance" + Integer.toString(j) + " : ", "Distance is " + Double.toString(dis[j]));
                                            c++;
                                        }

                                    }
                                    if(c==0){
                                        Toast.makeText(getApplicationContext(), "Ambulance unavailable",
                                                Toast.LENGTH_SHORT).show();

                                    }
                                    for(int u=0;u<=2;u++)Log.i("Coordinates of distance outside" + Integer.toString(j) + " : ", "Distance is " + Double.toString(dis[j]));
                                    double minDistance = Collections.min(Arrays.asList(dis));
                                    if (minDistance == dis[0]) minAmb = 0;
                                    else if (minDistance == dis[1]) minAmb = 1;
                                    else minAmb=2;

                                    Toast.makeText(getApplicationContext(), Integer.toString(minAmb),
                                            Toast.LENGTH_SHORT).show();
                                    DatabaseReference myRef1 = FirebaseDatabase.getInstance().getReference("AmbulanceList");
                                    DatabaseReference zero = myRef1.child("PickupLoc");
                                    zero.setValue(locArray[minAmb]);
                                    DatabaseReference myR2 = FirebaseDatabase.getInstance().getReference("AmbulanceList");
                                    DatabaseReference DLoc6 = myR2.child(minAmb + "");
                                    DatabaseReference DDMinAmbStatus = DLoc6.child("State");
                                    DDMinAmbStatus.setValue("1");
                                    av = minAmb;
                                    Log.i("Min Ambulance", Integer.toString(minAmb));
                                    TextView textView = (TextView) findViewById(R.id.LocText);
                                /*textView.setText("Distances are : "+ Double.toString(dis[0])+ " "+ Double.toString(dis[1])+ " "+
                                        Double.toString(dis[2])+ " "+ "Minimum Ambulance is " + Integer.toString(minAmb));*/
                                    minAmbText.setText("Closest Ambulance is " + minAmb + "");
                                    Log.i("Log 1", "Before db");
                                    DatabaseReference myR = FirebaseDatabase.getInstance().getReference("AmbulanceList");
                                    DatabaseReference DLoc = myR.child(minAmb + "");
                                    DatabaseReference DDLoc = DLoc.child("PhoneNum");
                                    Log.i("Log 2", "After db " + minAmb + "");
                                    DDLoc.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            final String phoneNum = dataSnapshot.getValue(String.class);

                                            try {

                                                Log.i("Log 3", "Before Toast");
                                                DatabaseReference myR = FirebaseDatabase.getInstance().getReference("AmbulanceList");
                                                DatabaseReference zero = myR.child("PickupLoc");

                                                zero.addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        String value = null;
                                                        value = dataSnapshot.getValue(String.class);
                                                        SmsManager smsManager = SmsManager.getDefault();
                                                        if(value==null){
                                                            Toast.makeText(getApplicationContext(), "No ambulance currently available",
                                                                    Toast.LENGTH_LONG).show();
                                                            System.exit(0);
                                                        }
                                                        String[] ltlng = value.split(",");
                                                        Uri builder = Uri.parse("http://maps.google.com/maps?daddr=" + ltlng[0] + "," + ltlng[1]);
                                                        String myUrl = builder.toString();
                                                        smsManager.sendTextMessage(phoneNum, null, "Go To Location " + myUrl, null, null);
                                                        Log.i("Inside loop: ", value);
                                                        Toast.makeText(getApplicationContext(), "SMS Sent!",
                                                                Toast.LENGTH_SHORT).show();
                                                        Log.i("Log 4", "After toast");
                                                        System.exit(0);
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                            } catch (Exception e) {
                                                Toast.makeText(getApplicationContext(),
                                                        "SMS faild, please try again later!",
                                                        Toast.LENGTH_SHORT).show();
                                                Log.i("Log 5", "Error");
                                                e.printStackTrace();
                                            }

                                        }

                                        public void onCancelled(DatabaseError databaseError) {


                                        }
                                    });

                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {


                            }
                        });
                    }
                }
                    public void onCancelled(DatabaseError databaseError) {


                    }
                });


        }



    }
    private double CalculateDistance(LatLng endPoint)
    {

        //Convert LatLng to Location
        gps=new GPSTracker(MainActivity.this);
        double latitude=0,longitude=0;
        //location=(TextView) findViewById(R.id.locationText);
        if(gps.canGetLocation()){
            latitude=gps.getLatitude();
            longitude=gps.getLongitude();

        }
        else{
            gps.showSettingsAlert();
        }
        Location currentLocation = new Location("tmploc1");
        currentLocation.setLatitude(latitude);
        currentLocation.setLongitude(longitude);
        Location location = new Location("tmploc2");
        location.setLatitude(endPoint.latitude);
        location.setLongitude(endPoint.longitude);

        location.setTime(currentLocation.getTime());

        //Set time as current position's time. (could also be datetime.now)
        //Log.i("Distance in Loop", Double.toString())
        return currentLocation.distanceTo(location);
    }


}


