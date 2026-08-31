package tz.go.nactvet.ict_inventory_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tz.go.nactvet.ict_inventory_management.entity.Office;

@Repository
public interface OfficeRepository extends JpaRepository<Office, Long> {

    List<Office> findAllByOrderByZoneNameAscOfficeCodeAsc();

    List<Office> findByZoneIdOrderByOfficeCodeAsc(Long zoneId);

    boolean existsByZoneIdAndOfficeCode(Long zoneId, String officeCode);

    boolean existsByZoneIdAndOfficeCodeAndIdNot(Long zoneId, String officeCode, Long id);
}