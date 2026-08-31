package tz.go.nactvet.ict_inventory_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tz.go.nactvet.ict_inventory_management.entity.Zone;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    List<Zone> findAllByOrderByNameAsc();

    Optional<Zone> findByName(String name);

    boolean existsByName(String name);

    boolean existsByCode(String code);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByCodeAndIdNot(String code, Long id);
}
