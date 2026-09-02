package tz.go.nactvet.ict_inventory_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tz.go.nactvet.ict_inventory_management.entity.User;
import tz.go.nactvet.ict_inventory_management.enums.Role;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByRole(Role role);

    boolean existsByEmployeeId(String employeeId);

    boolean existsByEmail(String email);

    boolean existsByEmployeeIdAndIdNot(String employeeId, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    long countByRole(Role role);

    long countByRoleAndEnabled(Role role, boolean enabled);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.directorate.id = :directorateId")
    long countByRoleAndDirectorateId(Role role, Long directorateId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.section.id = :sectionId")
    long countByRoleAndSectionId(Role role, Long sectionId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.unit.id = :unitId")
    long countByRoleAndUnitId(Role role, Long unitId);

    @Query("SELECT u FROM User u WHERE u.role = :role AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.employeeId) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<User> searchStaff(@Param("role") Role role, @Param("search") String search);
}
