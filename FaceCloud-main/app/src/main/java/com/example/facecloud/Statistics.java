package com.example.facecloud;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Statistics extends AppCompatActivity {
    Button backToMapsActivity;
    String username;
    String response = "";
    ArrayList<String> data = new ArrayList<String>();
    TextView distanceTodayValue;
    TextView timeTodayValue;
    TextView distanceAllValue;
    TextView daysAllValue;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        username = getIntent().getExtras().getString("username");
        setContentView(R.layout.statistics);
        backToMapsActivity = findViewById(R.id.backToMapsActivity);
        distanceTodayValue = findViewById(R.id.distanceTodayValue);
        timeTodayValue = findViewById(R.id.timeTodayValue);
        distanceAllValue = findViewById(R.id.distanceAllValue);
        daysAllValue = findViewById(R.id.daysAllValue);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        try {
            response = httpGet();
            data = retrieveXMLElements(response);
            // day distance
            String tmp = data.get(0);
            tmp = tmp.substring(0,tmp.indexOf(".")+4);
            distanceTodayValue.setText(tmp + " m");
            timeTodayValue.setText(data.get(1));
            // whole distance
            tmp = data.get(2);
            tmp = tmp.substring(0,tmp.indexOf(".")+4);
            distanceAllValue.setText(tmp + " m");
            daysAllValue.setText(data.get(3));
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }

        backToMapsActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendStuff = new Intent(Statistics.this, MapsActivity.class);
                sendStuff.putExtra("username", username);
                startActivity(sendStuff);
            }
        });


    }

    public static ArrayList<String> retrieveXMLElements(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        //Build Document
        Document document = builder.parse(new InputSource(new StringReader(xml)));

        //Normalize the XML Structure; It's just too important !!
        document.getDocumentElement().normalize();

        //Here comes the root node
        Element root = document.getDocumentElement();
        System.out.println(root.getNodeName());

        NodeList today = document.getElementsByTagName("today");
        System.out.println("============================");
        ArrayList<String> data = new ArrayList<String>();
        int temp = 0;
        Node node = today.item(temp);
        System.out.println("");    //Just a separator
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            //Print each detail
            Element eElement = (Element) node;
            String distanceToday = eElement.getElementsByTagName("distance").item(0).getTextContent();
            data.add(distanceToday);
            System.out.println("Distance : " + distanceToday);
            String timeToday = eElement.getElementsByTagName("time").item(0).getTextContent();
            data.add(timeToday);
            System.out.println("Time : " + timeToday);
        }

        NodeList all = document.getElementsByTagName("all");
        System.out.println("============================");

        temp = 0;
        node = all.item(temp);
        System.out.println("");    //Just a separator
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            //Print each detail
            Element eElement = (Element) node;
            String distanceAll = eElement.getElementsByTagName("distance").item(0).getTextContent();
            data.add(distanceAll);
            System.out.println("Distance : " + distanceAll);
            String daysAll = eElement.getElementsByTagName("days").item(0).getTextContent();
            data.add(daysAll);
            System.out.println("Days : " + daysAll);
        }
        return data;
    }


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

    public String httpGet() {
        String url = "https://facecloudserver.azurewebsites.net/" + username + "/statistics";
        URL request_url = null;
        try {
            request_url = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpURLConnection http_conn = null;
        try {
            http_conn = (HttpURLConnection) request_url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        http_conn.setConnectTimeout(100000);
        http_conn.setReadTimeout(100000);
        http_conn.setInstanceFollowRedirects(true);
        String response = null;
        try {
            System.out.println(String.valueOf(http_conn.getResponseCode()));
            System.out.println(String.valueOf(http_conn.getContent()));
            // w response jest odpowiedź z chmury
            response = readInputStreamAsString((InputStream) http_conn.getContent());
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

}
