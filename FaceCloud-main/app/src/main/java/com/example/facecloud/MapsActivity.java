package com.example.facecloud;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.facecloud.databinding.ActivityMapsBinding;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.Source;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private double distance = 0;
    private String username = "";

    private LocationListener locationListener;
    private LocationManager locationManager;

    private final long MIN_TIME = 1000; // 1 second
    private final long MIN_DIST = 5; // 5 Meters

    private StringBuffer[] coordinates;
    boolean flag = false;
    boolean isStarted = false;
    private LatLng latLng;
    ArrayList<Double> latitudes = new ArrayList<Double>();
    ArrayList<Double> longtitudes = new ArrayList<Double>();
    ArrayList<String> times = new ArrayList<String>();

    int iterator = 0;
    int iterator2 = 0;

    Button start;
    Button ping;
    Button stats;
    TextView username1;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    public static String readInputStreamAsString(InputStream in)
            throws IOException {

        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString();
    }

    public void httpPost(String text) {
        System.out.println("Ping przycisk");
        System.out.println(text);
        String url = "https://facecloudserver.azurewebsites.net";
        URL request_url = null;
        try {
            request_url = new URL(url);
        } catch (MalformedURLException e) {
        }
        HttpURLConnection http_conn = null;
        try {
            http_conn = (HttpURLConnection) request_url.openConnection();
        } catch (IOException e) {
        }
        System.out.println(http_conn);
        http_conn.setConnectTimeout(100000);
        http_conn.setReadTimeout(100000);
        http_conn.setInstanceFollowRedirects(true);
        http_conn.setDoOutput(true);
        http_conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
        PrintWriter out = null;
        try {
            out = new PrintWriter(http_conn.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        out.println(text);
        out.close();
        out = null;
        try {
            System.out.println(String.valueOf(http_conn.getResponseCode()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        super.onCreate(savedInstanceState);
        username = getIntent().getExtras().getString("username");
        System.out.println("otrzymana wiadomosc " + username);
        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        username1 = findViewById(R.id.textView3);
        username1.setText(username);

        // buttons
        start = findViewById(R.id.start1);
        setColor(start, true);
        ping = findViewById(R.id.ping);
        setColor(ping, true);
        stats = findViewById(R.id.stats);
        setColor(stats, true);


        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO obsługa startu
                if (isStarted) {
                    isStarted = false;
                    start.setText("Start");
                    setColor(start, true);
                    ArrayList<Double> tempLatitudes = new ArrayList<Double>(latitudes.subList(iterator2, latitudes.size()));
                    ArrayList<Double> tempLongtitudes = new ArrayList<Double>(longtitudes.subList(iterator2, longtitudes.size()));
                    ArrayList<String> tempTimes = new ArrayList<String>(times.subList(iterator2, times.size()));
                    String xml = createXmlFile(username, tempLatitudes, tempLongtitudes, tempTimes);
                    httpPost(xml);
                } else {
                    isStarted = true;
                    start.setText("Stop");
                    setColor(start, false);
                }
                iterator = 0;
            }
        });

        ping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO obsługa pingu
                flag = true;
                if (latitudes.size() != 0) {
                    ArrayList<Double> oneLatitude = new ArrayList<Double>();
                    ArrayList<Double> oneLongtitude = new ArrayList<Double>();
                    ArrayList<String> oneTime = new ArrayList<String>();
                    oneLatitude.add(latitudes.get(latitudes.size() - 1));
                    oneLongtitude.add(longtitudes.get(longtitudes.size() - 1));
                    oneTime.add(times.get(times.size() - 1));
                    // stworzenie XMLa
                    String xml = createXmlFile(username, oneLatitude, oneLongtitude, oneTime);
                    httpPost(xml);
                    flag = false;
                }
            }
        });

        stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO obsługa statystyk
                Intent sendStuff = new Intent(MapsActivity.this, Statistics.class);
                sendStuff.putExtra("username", username);
                startActivity(sendStuff);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ActivityCompat.requestPermissions(this, new String[]

                {
                        Manifest.permission.ACCESS_FINE_LOCATION
                }, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]

                {
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("SdCardPath")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                try {
                    System.out.println("wejscie");
                    if (isStarted && !flag) {
                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        System.out.println(location.getLatitude() + " " + location.getLongitude());
                        // tablice do przechowywania wspolrzednych
                        longtitudes.add(location.getLongitude());
                        latitudes.add(location.getLatitude());
                        iterator++;
                        mMap.addMarker(new MarkerOptions().position(latLng).title("My position"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        String currentDateandTime = sdf.format(new Date());
                        times.add(currentDateandTime);
                        System.out.println(times);
                        if (iterator % 5 == 0) {
                            //TODO wysłanie lokalizacji
                            ArrayList<Double> tempLatitudes = new ArrayList<Double>(latitudes.subList(iterator2, iterator));
                            ArrayList<Double> tempLongtitudes = new ArrayList<Double>(longtitudes.subList(iterator2, iterator));
                            ArrayList<String> tempTimes = new ArrayList<String>(times.subList(iterator2, iterator));
                            String xml = createXmlFile(username, tempLatitudes, tempLongtitudes, tempTimes);
                            System.out.println(xml);
                            httpPost(xml);
                            iterator2 += 5;
                        }
                    } else if (flag && !isStarted) {
                        latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        System.out.println(location.getLatitude() + " " + location.getLongitude());
                        // tablice do przechowywania wspolrzednych
                        longtitudes.add(location.getLongitude());
                        latitudes.add(location.getLatitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title("My position"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        String currentDateandTime = sdf.format(new Date());
                        times.add(currentDateandTime);
                    }
                } catch (SecurityException e) {
                    System.out.print("Security exception caught");
                }
            }

            @Override
            public void onFlushComplete(int requestCode) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }
        };
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
        } catch (SecurityException e) {
            System.out.print("Security exception caught");
        }
    }

    public String createXmlFile(String username, ArrayList<Double> latitude, ArrayList<Double> longitude, ArrayList<String> time) {
        String dataWrite = "";
        try {
            XmlSerializer xmlSerializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();
            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.startTag(null, "locations");
            xmlSerializer.startTag(null, "username");
            xmlSerializer.text(username);
            xmlSerializer.endTag(null, "username");
            for (int i = 0; i < latitude.size(); i++) {
                xmlSerializer.startTag(null, "location");
                xmlSerializer.startTag(null, "latitude");
                xmlSerializer.text(String.valueOf(latitude.get(i)));
                xmlSerializer.endTag(null, "latitude");
                xmlSerializer.startTag(null, "longitude");
                xmlSerializer.text(String.valueOf(longitude.get(i)));
                xmlSerializer.endTag(null, "longitude");
                xmlSerializer.startTag(null, "time");
                xmlSerializer.text(String.valueOf(time.get(i)));
                xmlSerializer.endTag(null, "time");
                xmlSerializer.endTag(null, "location");
            }
            xmlSerializer.endTag(null, "locations");
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            dataWrite = writer.toString();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dataWrite;
    }

    public static void setColor(Button button, boolean isDefault) {
        if (isDefault) {
            button.setBackgroundColor(android.graphics.Color.parseColor("#3700B3"));
            button.setTextColor(Color.parseColor("#FFFFFF"));
        } else {
            button.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
            button.setTextColor(Color.parseColor("#3700B3"));
        }
    }

}