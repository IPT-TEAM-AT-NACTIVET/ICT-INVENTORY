package tz.go.nactvet.ict_inventory_management.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import tz.go.nactvet.ict_inventory_management.entity.Asset;
import tz.go.nactvet.ict_inventory_management.enums.DeviceStatus;
import tz.go.nactvet.ict_inventory_management.enums.OwnershipType;
import tz.go.nactvet.ict_inventory_management.enums.VerificationStatus;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    @EntityGraph(attributePaths = {"user", "user.directorate", "user.section", "user.unit", "zone", "office", "deviceType"})
    List<Asset> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"user", "user.directorate", "user.section", "user.unit", "zone", "office", "deviceType"})
    List<Asset> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"user", "user.directorate", "user.section", "user.unit", "zone", "office", "deviceType"})
    List<Asset> findByDeviceTypeId(Long deviceTypeId);

    @EntityGraph(attributePaths = {"user", "user.directorate", "user.section", "user.unit", "zone", "office", "deviceType"})
    List<Asset> findByDeviceStatus(DeviceStatus status);

    @EntityGraph(attributePaths = {"user", "user.directorate", "user.section", "user.unit", "zone", "office", "deviceType"})
    List<Asset> findByVerificationStatus(VerificationStatus status);

    @EntityGraph(attributePaths = {"user", "user.directorate", "user.section", "user.unit", "zone", "office", "deviceType"})
    List<Asset> findByUserIdAndVerificationStatus(Long userId, VerificationStatus status);

    @EntityGraph(attributePaths = {"user", "user.directorate", "user.section", "user.unit", "zone", "office", "deviceType"})
    Optional<Asset> findWithDetailsById(Long id);

    Optional<Asset> findByAssetNumber(String assetNumber);

    Optional<Asset> findBySerialNumber(String serialNumber);

    boolean existsByAssetNumber(String assetNumber);

    boolean existsBySerialNumber(String serialNumber);

    boolean existsByAssetNumberAndIdNot(String assetNumber, Long id);

    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    long countByVerificationStatus(VerificationStatus status);

    long countByDeviceStatus(DeviceStatus status);

    long countByDeviceTypeId(Long deviceTypeId);

    long countByUserId(Long userId);

    long countByUserIdAndVerificationStatus(Long userId, VerificationStatus status);

    long countByUserIdAndDeviceStatus(Long userId, DeviceStatus status);

    @Query("SELECT a.deviceType.id, a.deviceType.name, COUNT(a) FROM Asset a GROUP BY a.deviceType.id, a.deviceType.name")
    List<Object[]> countByDeviceTypeGrouped();

    @Query("SELECT a.user.directorate.id, a.user.directorate.name, COUNT(a) FROM Asset a WHERE a.user.directorate IS NOT NULL GROUP BY a.user.directorate.id, a.user.directorate.name")
    List<Object[]> countByDirectorateGrouped();

    @Query("SELECT a.user.section.id, a.user.section.name, COUNT(a) FROM Asset a WHERE a.user.section IS NOT NULL GROUP BY a.user.section.id, a.user.section.name")
    List<Object[]> countBySectionGrouped();

    @Query("SELECT a.zone.id, a.zone.name, COUNT(a) FROM Asset a WHERE a.zone IS NOT NULL GROUP BY a.zone.id, a.zone.name")
    List<Object[]> countByZoneGrouped();

    @Query("SELECT a.verificationStatus, COUNT(a) FROM Asset a GROUP BY a.verificationStatus")
    List<Object[]> countByVerificationStatusGrouped();

    @Query("SELECT a.deviceStatus, COUNT(a) FROM Asset a GROUP BY a.deviceStatus")
    List<Object[]> countByDeviceStatusGrouped();

    @Query("SELECT a.office.id, a.office.officeCode, COUNT(a) FROM Asset a WHERE a.office IS NOT NULL GROUP BY a.office.id, a.office.officeCode")
    List<Object[]> countByOfficeGrouped();

    @Query("SELECT a.user.unit.id, a.user.unit.name, COUNT(a) FROM Asset a WHERE a.user.unit IS NOT NULL GROUP BY a.user.unit.id, a.user.unit.name")
    List<Object[]> countByUnitGrouped();

    @EntityGraph(attributePaths = {"user", "user.directorate", "user.section", "user.unit", "zone", "office", "deviceType"})
    Page<Asset> findByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT a FROM Asset a " +
           "LEFT JOIN FETCH a.user u " +
           "LEFT JOIN FETCH u.directorate " +
           "LEFT JOIN FETCH u.section " +
           "LEFT JOIN FETCH u.unit " +
           "LEFT JOIN FETCH a.zone " +
           "LEFT JOIN FETCH a.office " +
           "LEFT JOIN FETCH a.deviceType WHERE " +
           "(:assetNumber IS NULL OR a.assetNumber LIKE %:assetNumber%) AND " +
           "(:serialNumber IS NULL OR a.serialNumber LIKE %:serialNumber%) AND " +
           "(:deviceName IS NULL OR a.deviceName LIKE %:deviceName%) AND " +
           "(:deviceTypeId IS NULL OR a.deviceType.id = :deviceTypeId) AND " +
           "(:employeeId IS NULL OR a.user.employeeId LIKE %:employeeId%) AND " +
           "(:userName IS NULL OR a.user.fullName LIKE %:userName% OR a.user.username LIKE %:userName%) AND " +
           "(:userId IS NULL OR a.user.id = :userId) AND " +
           "(:directorateId IS NULL OR a.user.directorate.id = :directorateId) AND " +
           "(:sectionId IS NULL OR a.user.section.id = :sectionId) AND " +
           "(:unitId IS NULL OR a.user.unit.id = :unitId) AND " +
           "(:zoneId IS NULL OR a.zone.id = :zoneId) AND " +
           "(:officeId IS NULL OR a.office.id = :officeId) AND " +
           "(:ownershipType IS NULL OR a.ownershipType = :ownershipType) AND " +
           "(:deviceStatus IS NULL OR a.deviceStatus = :deviceStatus) AND " +
           "(:verificationStatus IS NULL OR a.verificationStatus = :verificationStatus) " +
           "ORDER BY a.createdAt DESC")
    Page<Asset> findByFilters(
           @Param("assetNumber") String assetNumber,
           @Param("serialNumber") String serialNumber,
           @Param("deviceName") String deviceName,
           @Param("deviceTypeId") Long deviceTypeId,
           @Param("employeeId") String employeeId,
           @Param("userName") String userName,
           @Param("userId") Long userId,
           @Param("directorateId") Long directorateId,
           @Param("sectionId") Long sectionId,
           @Param("unitId") Long unitId,
           @Param("zoneId") Long zoneId,
           @Param("officeId") Long officeId,
           @Param("ownershipType") OwnershipType ownershipType,
           @Param("deviceStatus") DeviceStatus deviceStatus,
           @Param("verificationStatus") VerificationStatus verificationStatus,
           Pageable pageable);
}