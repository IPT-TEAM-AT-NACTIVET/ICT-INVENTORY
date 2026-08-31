package tz.go.nactvet.ict_inventory_management.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tz.go.nactvet.ict_inventory_management.entity.Unit;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {

    List<Unit> findAllByOrderByNameAsc();

    boolean existsByName(String name);

    boolean existsByCode(String code);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByCodeAndIdNot(String code, Long id);
}