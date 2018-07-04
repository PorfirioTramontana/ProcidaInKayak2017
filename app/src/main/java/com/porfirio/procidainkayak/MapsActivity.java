package com.porfirio.procidainkayak;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import Modules.DirectionFinder;
import Modules.DirectionFinderListener;
import Modules.Route;

import static android.R.attr.name;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
//import static com.google.android.gms.fitness.FitnessActivities.WALKING;


public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        DirectionFinderListener, LocationListener {
    private GoogleMap mMap;
    private ImageButton btnFindPath;
    //private ImageButton btnSetting;
    private ImageButton btnOfflineInfo;
    private ImageButton btnCalendar;
    //private EditText etOrigin;
    //private EditText etDestination;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private double latitude = 0.0, longitude = 0.0;
    private String mode, avoid;
    //private com.porfirio.procidainkayak.GPSTracker gps;
    private LocationManager locationManager;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    private boolean canGetLocation;
    // la minima distanza di aggiornamenti in metri
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // tempo minimo tra aggiornamenti in millisecondi
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1 * 1; // 1 second
    private ImageButton btnDirections;
    private String origin;
    private String destination = "Via Marina Chiaiolella, 33, 80079 Procida";
    private View btnWebPage;
    private LatLng position = null;

    public Tracker mTracker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.enableAdvertisingIdCollection(true);


        setContentView(com.porfirio.procidainkayak.R.layout.activity_maps);
        // Ottiene SupportMapFragment e notifica quando la mappa è pronta per essere usata.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(com.porfirio.procidainkayak.R.id.map);
        mapFragment.getMapAsync(this);
        checkPerm();

        //btnSetting = (ImageButton) findViewById(com.porfirio.procidainkayak.R.id.btnSettings);
        btnFindPath = (ImageButton) findViewById(com.porfirio.procidainkayak.R.id.btnFindPath);
        btnDirections = (ImageButton) findViewById(com.porfirio.procidainkayak.R.id.btnDirections);
        btnWebPage = (ImageButton) findViewById(R.id.btnWebPage);
        btnOfflineInfo = (ImageButton) findViewById(R.id.btnOfflineInfo);
        btnCalendar = (ImageButton) findViewById(R.id.btnCalendar);
        //etOrigin = (EditText) findViewById(com.porfirio.procidainkayak.R.id.etOrigin);
        //etDestination = (EditText) findViewById(com.porfirio.procidainkayak.R.id.etDestination);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //Alla pressione del bottone "Cerca" viene chiamata la funzione SendRequest()
        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Calcola le coordinate attuali
                position = getLocation();
                origin = new String(String.format("%s, %s", position.latitude, position.longitude));
                //mMap.moveCamera(CameraUpdateFactory.zoomTo((float) 2.0));
                //Nascondo la testiera di input
//                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//                imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0);
                sendRequest();

            }
        });

        btnDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //chiama StreetMap impostando la destinazione costante
                Uri gmmIntentUri = Uri.parse("google.navigation:q=Via+Marina+Chiaiolella,+33,+80079+Procida&mode=w");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });


        btnOfflineInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent;
                intent = new Intent(MapsActivity.this, OfflineInfoActivity.class);
                startActivity(intent);
            }
        });

        btnWebPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //apre il sito web
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("http://www.procidainkayak.it/kayak/"));
                startActivity(i);
            }
        });

        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(MapsActivity.this, CalendarActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTracker.setScreenName("Image~" + name);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private boolean checkPerm(){
        //se non sono stati inseriti ne indirizzo partezza ne indirizzo destinazione viene dato un messaggio di Alert
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            //&& ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.INTERNET}, 1
            ); //Manifest.permission.ACCESS_COARSE_LOCATION,
        }
            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                System.exit(0);
            }
            return false;
    };

    //Funzione che controlla se sono stati inseriti gli indirizzi di partenza e destinazione
    private boolean sendRequest() {
        try {
            if (origin!=null)
                //if (origin.isEmpty())
                new DirectionFinder(this, origin, destination, "walking", avoid).execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


        return true;
    }


    public LatLng getLocation() {
        Location location = null;
        LatLng l = null;
        try {

            isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                // lo stato provider della rete non è abilitata
                //Toast.makeText(this, "Inserisci l'indirizzo di partenza o l'indirizzo di destinazione...", Toast.LENGTH_SHORT).show();
                //Porto di Procida
                return l = new LatLng(14.026555, 40.765711);
            } else {
                canGetLocation = true;
                // se il GPS è abilitato prendo latitudine e longitudine attraverso il sensore GPS
                if (isGPSEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return l = new LatLng(14.026555, 40.765711);
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, (android.location.LocationListener) this);
//                        Log.d("GPS Enabled", "GPS Enabled");
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location == null) {
                            l = new LatLng(14.026555, 40.765711);
                            return l;
                        } else if (location.getLatitude()==0.0 || location.getLongitude()==0.0)
                            return new LatLng(14.026555, 40.765711);
                        else
                            return new LatLng(location.getLatitude(), location.getLongitude());
                    }
                } else if (isNetworkEnabled) {
//                    locationManager.requestLocationUpdates(
//                            LocationManager.NETWORK_PROVIDER,
//                            MIN_TIME_BW_UPDATES,
//                            MIN_DISTANCE_CHANGE_FOR_UPDATES, (android.location.LocationListener) this);
//                    Log.d("Network", "Network");
//                    if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location != null) {
                        l = new LatLng(location.getLatitude(), location.getLongitude());
                        return l;
                    } else {
                        return l = new LatLng(14.026555, 40.765711);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return l = new LatLng(14.026555, 40.765711);
    }

    @Override
    //Quando ricevo la mappa controllo lo stato della rete e del gps e localizzo la mia posizione
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1
            ); //,             Manifest.permission.ACCESS_COARSE_LOCATION
        }
        {
            position = getLocation();
            //LatLng loc = new LatLng(position.latitude, position.longitude);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, (float) 13.3));
            //   mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            originMarkers.add(mMap.addMarker(new MarkerOptions().title(getString(R.string.miaPosizione)).position(position)));
            mMap.setMyLocationEnabled(true);

            //etOrigin.setText(String.format("%s, %s", latitude, longitude));
            origin = new String(String.format("%s, %s", position.latitude, position.longitude));
            sendRequest();

        }
    }


    @Override
    //All'avvio della ricerca del percorso rimuovo i markers e le linee dei percorsi
    public void onDirectionFinderStart() {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.attendi));
        progressDialog.setCancelable(true);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.annulla), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        progressDialog.show();

        //progressDialog = ProgressDialog.show(this, getString(R.string.attendi),
        //       getString(R.string.percorso), true);
        //mMap.clear();


        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
        mMap.clear();
    }

    @Override
    //In caso ri ricerca andata a buon fine viene tracciato il percorso
    public void onDirectionFinderSuccess(List<Route> routes) {
        progressDialog.dismiss();
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, (float) 13.3));
            ((TextView) findViewById(com.porfirio.procidainkayak.R.id.tvDuration)).setText(route.duration.text);
            ((TextView) findViewById(com.porfirio.procidainkayak.R.id.tvDistance)).setText(route.distance.text);

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(com.porfirio.procidainkayak.R.drawable.start_blue))
                    .title(route.startAddress)
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(com.porfirio.procidainkayak.R.drawable.end_green))
                    .title(route.endAddress)
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.BLUE).
                    width(10);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));
        }
    }

    @Override
    public void onDirectionFinderError() {
        progressDialog.dismiss();

        Toast.makeText(this, getString(R.string.noConnection), Toast.LENGTH_SHORT).show();
        //originMarkers = new ArrayList<>();
        //destinationMarkers = new ArrayList<>();

        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
        mMap.clear();

        ((TextView) findViewById(com.porfirio.procidainkayak.R.id.tvDuration)).setText("3.1 km");
        ((TextView) findViewById(com.porfirio.procidainkayak.R.id.tvDistance)).setText("39 min");

        PolylineOptions polylineOptions = new PolylineOptions().
                geodesic(true).
                color(Color.BLUE).
                width(10);

        if (isGPSEnabled) {


            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (l != null) {
                    position = new LatLng(l.getLatitude(), l.getLongitude());
                    polylineOptions.add(position);
                } else {
                    position = new LatLng(14.026555, 40.765711);
                    polylineOptions.add(position);
                }

                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(com.porfirio.procidainkayak.R.drawable.start_blue))
                        .title(getString(R.string.miaPosizione))
                        .position(position)));

            }
            else
                originMarkers.add(mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(com.porfirio.procidainkayak.R.drawable.start_blue))
                        .title(getString(R.string.miaPosizione))
                        .position(new LatLng(40.765711,14.026555))));

            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromResource(com.porfirio.procidainkayak.R.drawable.end_green))
                    .title(getString(R.string.app_name))
                    .position(new LatLng(40.749403, 14.005773))));




        polylineOptions.add(new LatLng(40.749403, 14.005773));

        polylinePaths.add(mMap.addPolyline(polylineOptions));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(40.765711,14.026555), (float) 13.3));

    }


    @Override
    public void onLocationChanged(Location location) {
        if(location!=null) {
            position=new LatLng(location.getLatitude(),location.getLongitude());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


//    @Override
//    public void onLocationChanged(Location location) {
//        move(location);
//    }
//
//
//
//    public void move(Location location){
//        latitude=location.getLatitude();
//        longitude=location.getLongitude();
//        LatLng loc = new LatLng(latitude, longitude);
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 10));
//        //   mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//        //etOrigin.setText(String.format("%s, %s", latitude, longitude));
//        origin=new String(String.format("%s, %s", location.getLatitude(), location.getLongitude()));
//        Toast.makeText(this, getString(R.string.LocationChanged), Toast.LENGTH_LONG).show();
//        //sendRequest();
//    }
}
