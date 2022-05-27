package com.facecloud.facecloudserverapp.controller;

import com.facecloud.facecloudserverapp.exception.DataValidationException;
import com.facecloud.facecloudserverapp.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

@Controller()
public class LocationController {

    @Autowired
    LocationService locationService;

    @PostMapping
    public ResponseEntity addNewLocations(@RequestBody String xml) {
        try {
            locationService.addNewLocation(xml);
        } catch (DataValidationException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
        return ResponseEntity.ok(xml);
    }

    @GetMapping("/{username}")
    public ResponseEntity<String> getAll(@PathVariable String username) {
        String data = null;
        try {
            data = locationService.getAllLocationsXmlForUser(username);
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
        return ResponseEntity.ok(data);
    }

    @GetMapping("{username}/statistics")
    public ResponseEntity<String> getStatistics(@PathVariable String username) {
        String data = null;
        try {
            data = locationService.getStatistics(username);
        } catch (ParserConfigurationException | TransformerException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
        return ResponseEntity.ok(data);
    }
}
