package tz.go.nactvet.ict_inventory_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tz.go.nactvet.ict_inventory_management.entity.Section;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    Optional<Section> findByName(String name);

    List<Section> findByDirectorateIdOrderByDirectorateIdAscNameAsc(Long directorateId);

    List<Section> findAllByOrderByIdAsc();

    boolean existsByName(String name);

    boolean existsByCode(String code);

    boolean existsByNameAndIdNot(String name, Long id);

    boolean existsByCodeAndIdNot(String code, Long id);
}