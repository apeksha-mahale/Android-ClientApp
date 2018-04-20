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
                /*textView.setText(value+"Hi");*/
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
        final String[] locArray = new String[3];
        for(int x=0;x<=2;x++){
            DatabaseReference myR = FirebaseDatabase.getInstance().getReference("AmbulanceList");
            DatabaseReference DLoc = myR.child(x+"");
            DatabaseReference DDLoc= DLoc.child("Location");

            final int m=x;
            DDLoc.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ambValue[0] = dataSnapshot.getValue(String.class);
                    Log.i("Coordinates", ambValue[0]);
                    locArray[m]=ambValue[0];
                    Log.i("Coordinates of m", Integer.toString(m)+" : " +ambValue[0]+" , " +locArray[m]);
                    Double[] dis = new Double[3];
                    int j = 0;
                    Double minDist=Double.MAX_VALUE;
                    if(m==2) {

                        for(int y=0;y<=2;y++)
                            Log.i("Coordinates of y", Integer.toString(y) + " , " + locArray[y]);
                        int minAmb = -1;
                        for (String eachLoc : locArray) {
                            String[] latlong = new String[1];
                            if (eachLoc != null) {
                                latlong = eachLoc.split(",");
                                double latitude = Double.parseDouble(latlong[0]);
                                double longitude = Double.parseDouble(latlong[1]);
                                LatLng latLangLocation = new LatLng(latitude, longitude);
                                Double distance = CalculateDistance(latLangLocation);
                                Log.i("Distance"+Integer.toString(j)+" : ", Double.toString(distance));
                                dis[j] = distance;
                                Log.i("Coordinates of distance"+Integer.toString(j)+" : ", "Distance is " + Double.toString(dis[j]));
                                if (dis[j] < minDist) {
                                    dis[j] = minDist;
                                    minAmb = j;
                                }
                                j++;
                            }
                            DatabaseReference myRef1 = FirebaseDatabase.getInstance().getReference("AmbulanceList");
                            DatabaseReference zero = myRef1.child("PickupLoc");
                            zero.setValue(locArray[minAmb]);

                        }
                        av = minAmb;
                        Log.i("Min Ambulance", Integer.toString(minAmb));
                        TextView textView=(TextView) findViewById(R.id.LocText);
                        /*textView.setText("Distances are : "+ Double.toString(dis[0])+ " "+ Double.toString(dis[1])+ " "+
                                Double.toString(dis[2])+ " "+ "Minimum Ambulance is " + Integer.toString(minAmb));*/
                        minAmbText.setText("Closest Ambulance is " + minAmb+"");
                        Log.i("Log 1", "Before db");
                        DatabaseReference myR = FirebaseDatabase.getInstance().getReference("AmbulanceList");
                        DatabaseReference DLoc = myR.child(minAmb+"");
                        DatabaseReference DDLoc= DLoc.child("PhoneNum");
                        Log.i("Log 2", "After db "+minAmb+"");
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
                                            String[] ltlng = value.split(",");
                                           /* Uri.Builder builder = new Uri.Builder();
                                            builder.scheme("https")
                                                    .authority("www.google.com")
                                                    .appendPath("maps")
                                                    .appendPath("search")
                                                    .appendQueryParameter("api", "1")
                                                    .appendQueryParameter("map_action", "map")
                                                    .appendQueryParameter("center", "1.732"+","+"7.732");*/
                                            Uri builder = Uri.parse("http://maps.google.com/maps?daddr=" + ltlng[0] + "," + ltlng[1]);
                                            String myUrl = builder.toString();
                                            smsManager.sendTextMessage(phoneNum, null, "Go To Location "+ myUrl , null, null);
                                            Log.i("Inside loop: ", value);
                                            Toast.makeText(getApplicationContext(), "SMS Sent!",
                                                    Toast.LENGTH_LONG).show();
                                            Log.i("Log 4", "After toast");
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                } catch (Exception e) {
                                    Toast.makeText(getApplicationContext(),
                                            "SMS faild, please try again later!",
                                            Toast.LENGTH_LONG).show();
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


