package com.facecloud.facecloudserverapp.repository;

import com.facecloud.facecloudserverapp.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    public List<Location> findAllByUserName(String userName);
}
