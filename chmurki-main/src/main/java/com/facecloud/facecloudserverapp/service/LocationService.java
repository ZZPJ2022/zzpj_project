package com.facecloud.facecloudserverapp.service;

import com.facecloud.facecloudserverapp.entity.Location;
import com.facecloud.facecloudserverapp.exception.DataValidationException;
import com.facecloud.facecloudserverapp.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.lucene.util.SloppyMath.haversinMeters;


@Service
public class LocationService {

    @Autowired
    LocationRepository locationRepository;

    public String getAllLocationsXmlForUser(String userName) throws ParserConfigurationException, TransformerException {
        List<Location> locationList = locationRepository.findAllByUserName(userName);
        return parseToXml(locationList, userName);
    }

    public void addNewLocation(String xmlData) throws DataValidationException {
        try {
            Document document = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xmlData)));

            String userName = document.getElementsByTagName("username").item(0).getTextContent();
            int realLocationsCount = 0;

            NodeList locationsList = document.getElementsByTagName("location");
            for(int i=0; i<locationsList.getLength(); i++) {
                Node node = locationsList.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE) {
                    String latitude = ((Element) node).getElementsByTagName("latitude").item(0).getTextContent();
                    String longitude = ((Element) node).getElementsByTagName("longitude").item(0).getTextContent();
                    String time = ((Element) node).getElementsByTagName("time").item(0).getTextContent();
                    Location location = createLocation(userName,latitude, longitude, time);
                    realLocationsCount++;
                    locationRepository.save(location);
                }
            }
            if(realLocationsCount == 0) {
                throw new DataValidationException("Dostalem pustego requesta (nie bylo w nim lokacji)");
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
    }

    private Location createLocation(String userName, String latitude, String longitude, String time) throws DataValidationException {
        Location location = new Location();
        try {
            location.setUserName(userName);
            location.setLatitude(Double.parseDouble(latitude));
            location.setLongitude(Double.parseDouble(longitude));
            location.setTime(LocalDateTime.of(
                    Integer.parseInt(time.split("-")[0]),
                    Integer.parseInt(time.split("-")[1]),
                    Integer.parseInt(time.split(" ")[0].split("-")[2]),
                    Integer.parseInt(time.split(" ")[1].split(":")[0]),
                    Integer.parseInt(time.split(" ")[1].split(":")[1]),
                    Integer.parseInt(time.split(" ")[1].split(":")[2])
            )); // 2022-01-06 13:32:28
        } catch(Throwable e) {
            throw new DataValidationException(e);
        }
        return location;
    }

    public String parseToXml(List<Location> locations, String userName) throws ParserConfigurationException, TransformerException {
        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder().newDocument();
        Element root = document.createElement("locations");
        document.appendChild(root);

        Element name = document.createElement("username");
        name.appendChild(document.createTextNode(userName));
        root.appendChild(name);

        locations.forEach(location -> {
            Element locationElement = document.createElement("location");
            root.appendChild(locationElement);

            Element latitude = document.createElement("latitude");
            latitude.appendChild(document.createTextNode(Double.toString(location.getLatitude())));
            locationElement.appendChild(latitude);

            Element longitude = document.createElement("longitude");
            longitude.appendChild(document.createTextNode(Double.toString(location.getLongitude())));
            locationElement.appendChild(longitude);

            Element time = document.createElement("time");
            time.appendChild(document.createTextNode(location.getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
            locationElement.appendChild(time);
        });
        return transformDocumentToString(document);
    }

    private String transformDocumentToString(Document document) throws TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer();
        StringWriter sw = new StringWriter();
        trans.transform(new DOMSource(document), new StreamResult(sw));
        return sw.toString();
    }

    public String getStatistics(String userName) throws TransformerException, ParserConfigurationException {
        List<Location> locationList = locationRepository.findAllByUserName(userName);

        if(locationList.size() == 0) {
            throw new TransformerException("dany uzytkownik nie ma historii w tabeli");
        }

        Location firstPoint = locationList.get(0);
        Location lastPoint = locationList.get(locationList.size() - 1);

        double allDistance = haversinMeters(firstPoint.getLatitude(), firstPoint.getLongitude(), lastPoint.getLatitude(), lastPoint.getLongitude());
        long allDaysUsingApp = ChronoUnit.DAYS.between(firstPoint.getTime(), lastPoint.getTime()) + 1;

        List<Location> locationListFromToday = locationList.stream().filter(location ->
                (location.getTime().getYear() == LocalDateTime.now().getYear() &&
                        location.getTime().getMonth().equals(LocalDateTime.now().getMonth()) &&
                        location.getTime().getDayOfMonth() == LocalDateTime.now().getDayOfMonth())
        ).collect(Collectors.toList());

        double todayDistance;
        LocalDateTime todayUsingApp;

        if(locationListFromToday.size() != 0) {
            Location firstPointToday = locationListFromToday.get(0);
            Location lastPointToday = locationListFromToday.get(locationListFromToday.size() - 1);

            todayDistance = haversinMeters(firstPointToday.getLatitude(), firstPointToday.getLongitude(),
                    lastPointToday.getLatitude(), lastPointToday.getLongitude());
            todayUsingApp = LocalDateTime.of(1,1,1,
                    new Date((int) ChronoUnit.SECONDS.between(firstPoint.getTime(), lastPoint.getTime()) * 1000).getHours(),
                    new Date((int) ChronoUnit.SECONDS.between(firstPoint.getTime(), lastPoint.getTime()) * 1000).getMinutes(),
                    new Date((int) ChronoUnit.SECONDS.between(firstPoint.getTime(), lastPoint.getTime()) * 1000).getSeconds());
        } else {
            todayDistance = 0;
            todayUsingApp = LocalDateTime.of(1,1,1,0,0,0);
        }

        return parseToXmlStatistics(allDistance, allDaysUsingApp, todayDistance, todayUsingApp);
    }

    public String parseToXmlStatistics(double allDistance, long allDaysUsingApp, double todayDistance, LocalDateTime todayUsingApp) throws ParserConfigurationException, TransformerException {
        Document document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder().newDocument();
        Element root = document.createElement("statistics");
        document.appendChild(root);

        Element today = document.createElement("today");
        root.appendChild(today);

        Element distance1 = document.createElement("distance");
        distance1.appendChild(document.createTextNode(Double.toString(todayDistance)));
        today.appendChild(distance1);

        Element time = document.createElement("time");
        time.appendChild(document.createTextNode(todayUsingApp.format(DateTimeFormatter.ofPattern("HH:mm:ss"))));
        today.appendChild(time);

        Element all = document.createElement("all");
        root.appendChild(all);

        Element distance2 = document.createElement("distance");
        distance2.appendChild(document.createTextNode(Double.toString(allDistance)));
        all.appendChild(distance2);

        Element days = document.createElement("days");
        days.appendChild(document.createTextNode(Long.toString(allDaysUsingApp)));
        all.appendChild(days);

        return transformDocumentToString(document);
    }
}
