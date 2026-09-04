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

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    @EntityGraph(attributePaths = {"zone", "deviceType"})
    List<Asset> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"zone", "deviceType"})
    List<Asset> findByDeviceTypeId(Long deviceTypeId);

    @EntityGraph(attributePaths = {"zone", "deviceType"})
    List<Asset> findByDeviceStatus(DeviceStatus status);

    @EntityGraph(attributePaths = {"zone", "deviceType"})
    Optional<Asset> findWithDetailsById(Long id);

    Optional<Asset> findByAssetNumber(String assetNumber);

    Optional<Asset> findBySerialNumber(String serialNumber);

    boolean existsByAssetNumber(String assetNumber);

    boolean existsBySerialNumber(String serialNumber);

    boolean existsByAssetNumberAndIdNot(String assetNumber, Long id);

    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);

    long countByDeviceStatus(DeviceStatus status);

    long countByDeviceTypeId(Long deviceTypeId);

    long countByCreatedById(Long userId);

    long countByZoneId(Long zoneId);

    @Query("SELECT a.deviceType.id, a.deviceType.name, COUNT(a) FROM Asset a GROUP BY a.deviceType.id, a.deviceType.name")
    List<Object[]> countByDeviceTypeGrouped();

    @Query("SELECT a.userOfAsset, COUNT(a) FROM Asset a WHERE a.userOfAsset IS NOT NULL GROUP BY a.userOfAsset")
    List<Object[]> countByUserOfAssetGrouped();

    @Query("SELECT a.zone.id, a.zone.name, COUNT(a) FROM Asset a WHERE a.zone IS NOT NULL GROUP BY a.zone.id, a.zone.name")
    List<Object[]> countByZoneGrouped();

    @Query("SELECT a.deviceStatus, COUNT(a) FROM Asset a GROUP BY a.deviceStatus")
    List<Object[]> countByDeviceStatusGrouped();

    @Query("SELECT a.ownershipType, COUNT(a) FROM Asset a GROUP BY a.ownershipType")
    List<Object[]> countByOwnershipGrouped();

    @Query("SELECT a.assetNumber, a.deviceName, a.deviceType.name, a.userOfAsset, " +
           "a.zone.name, a.office, cb.fullName, a.createdAt " +
           "FROM Asset a " +
           "LEFT JOIN a.createdBy cb " +
           "ORDER BY a.createdAt DESC")
    List<Object[]> findRecentRegistrations(Pageable pageable);

    @Query("SELECT a.office, COUNT(a) FROM Asset a WHERE a.office IS NOT NULL GROUP BY a.office")
    List<Object[]> countByOfficeGrouped();

    @Query("SELECT a.office, COUNT(a) FROM Asset a " +
           "WHERE a.office IS NOT NULL AND (" +
           "  (:s IS NULL) OR " +
           "  LOWER(COALESCE(a.assetNumber,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.serialNumber,'')) LIKE :s OR " +
           "  LOWER(a.deviceName) LIKE :s OR " +
           "  LOWER(COALESCE(a.userOfAsset,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.office,'')) LIKE :s OR " +
           "  LOWER(a.zone.name) LIKE :s OR " +
           "  LOWER(a.deviceType.name) LIKE :s OR " +
           "  LOWER(CAST(a.ownershipType AS string)) LIKE :s OR " +
           "  LOWER(CAST(a.deviceStatus AS string)) LIKE :s " +
           ") GROUP BY a.office")
    List<Object[]> countByOfficeGroupedWithSearch(@Param("s") String search);

    @Query("SELECT a.zone.id, a.zone.name, COUNT(a) FROM Asset a " +
           "WHERE a.zone IS NOT NULL AND (" +
           "  (:s IS NULL) OR " +
           "  LOWER(COALESCE(a.assetNumber,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.serialNumber,'')) LIKE :s OR " +
           "  LOWER(a.deviceName) LIKE :s OR " +
           "  LOWER(COALESCE(a.userOfAsset,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.office,'')) LIKE :s OR " +
           "  LOWER(a.zone.name) LIKE :s OR " +
           "  LOWER(a.deviceType.name) LIKE :s OR " +
           "  LOWER(CAST(a.ownershipType AS string)) LIKE :s OR " +
           "  LOWER(CAST(a.deviceStatus AS string)) LIKE :s " +
           ") GROUP BY a.zone.id, a.zone.name")
    List<Object[]> countByZoneGroupedWithSearch(@Param("s") String search);

    @Query("SELECT a.deviceType.id, a.deviceType.name, COUNT(a) FROM Asset a " +
           "WHERE a.deviceType IS NOT NULL AND (" +
           "  (:s IS NULL) OR " +
           "  LOWER(COALESCE(a.assetNumber,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.serialNumber,'')) LIKE :s OR " +
           "  LOWER(a.deviceName) LIKE :s OR " +
           "  LOWER(COALESCE(a.userOfAsset,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.office,'')) LIKE :s OR " +
           "  LOWER(a.zone.name) LIKE :s OR " +
           "  LOWER(a.deviceType.name) LIKE :s OR " +
           "  LOWER(CAST(a.ownershipType AS string)) LIKE :s OR " +
           "  LOWER(CAST(a.deviceStatus AS string)) LIKE :s " +
           ") GROUP BY a.deviceType.id, a.deviceType.name")
    List<Object[]> countByDeviceTypeGroupedWithSearch(@Param("s") String search);

    @Query("SELECT a.deviceStatus, COUNT(a) FROM Asset a " +
           "WHERE (" +
           "  (:s IS NULL) OR " +
           "  LOWER(COALESCE(a.assetNumber,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.serialNumber,'')) LIKE :s OR " +
           "  LOWER(a.deviceName) LIKE :s OR " +
           "  LOWER(COALESCE(a.userOfAsset,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.office,'')) LIKE :s OR " +
           "  LOWER(a.zone.name) LIKE :s OR " +
           "  LOWER(a.deviceType.name) LIKE :s OR " +
           "  LOWER(CAST(a.ownershipType AS string)) LIKE :s OR " +
           "  LOWER(CAST(a.deviceStatus AS string)) LIKE :s " +
           ") GROUP BY a.deviceStatus")
    List<Object[]> countByDeviceStatusGroupedWithSearch(@Param("s") String search);

    @Query("SELECT a.ownershipType, COUNT(a) FROM Asset a " +
           "WHERE (" +
           "  (:s IS NULL) OR " +
           "  LOWER(COALESCE(a.assetNumber,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.serialNumber,'')) LIKE :s OR " +
           "  LOWER(a.deviceName) LIKE :s OR " +
           "  LOWER(COALESCE(a.userOfAsset,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.office,'')) LIKE :s OR " +
           "  LOWER(a.zone.name) LIKE :s OR " +
           "  LOWER(a.deviceType.name) LIKE :s OR " +
           "  LOWER(CAST(a.ownershipType AS string)) LIKE :s OR " +
           "  LOWER(CAST(a.deviceStatus AS string)) LIKE :s " +
           ") GROUP BY a.ownershipType")
    List<Object[]> countByOwnershipGroupedWithSearch(@Param("s") String search);

    @Query("SELECT COUNT(a) FROM Asset a WHERE (" +
           "  (:s IS NULL) OR " +
           "  LOWER(COALESCE(a.assetNumber,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.serialNumber,'')) LIKE :s OR " +
           "  LOWER(a.deviceName) LIKE :s OR " +
           "  LOWER(COALESCE(a.userOfAsset,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.office,'')) LIKE :s OR " +
           "  LOWER(a.zone.name) LIKE :s OR " +
           "  LOWER(a.deviceType.name) LIKE :s OR " +
           "  LOWER(CAST(a.ownershipType AS string)) LIKE :s OR " +
           "  LOWER(CAST(a.deviceStatus AS string)) LIKE :s " +
           ")")
    long countWithSearch(@Param("s") String search);

    @EntityGraph(attributePaths = {"zone", "deviceType"})
    Page<Asset> findByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT a FROM Asset a " +
           "LEFT JOIN FETCH a.zone " +
           "LEFT JOIN FETCH a.deviceType " +
           "WHERE LOWER(COALESCE(a.assetNumber, '')) LIKE :search " +
           "OR LOWER(COALESCE(a.serialNumber, '')) LIKE :search " +
           "OR LOWER(a.deviceName) LIKE :search " +
           "OR LOWER(a.userOfAsset) LIKE :search " +
           "OR LOWER(COALESCE(a.office, '')) LIKE :search " +
           "OR LOWER(CAST(a.ownershipType AS string)) LIKE :search " +
           "OR LOWER(CAST(a.deviceStatus AS string)) LIKE :search " +
           "OR LOWER(a.zone.name) LIKE :search " +
           "OR LOWER(a.deviceType.name) LIKE :search " +
           "ORDER BY a.createdAt DESC")
    Page<Asset> findBySearch(@Param("search") String search, Pageable pageable);

    @Query("SELECT a FROM Asset a " +
           "LEFT JOIN FETCH a.zone " +
           "LEFT JOIN FETCH a.deviceType WHERE " +
           "(:assetNumber IS NULL OR a.assetNumber LIKE %:assetNumber%) AND " +
           "(:serialNumber IS NULL OR a.serialNumber LIKE %:serialNumber%) AND " +
           "(:deviceName IS NULL OR a.deviceName LIKE %:deviceName%) AND " +
           "(:deviceTypeId IS NULL OR a.deviceType.id = :deviceTypeId) AND " +
           "(:userOfAsset IS NULL OR a.userOfAsset LIKE %:userOfAsset%) AND " +
           "(:zoneId IS NULL OR a.zone.id = :zoneId) AND " +
           "(:office IS NULL OR a.office LIKE %:office%) AND " +
           "(:ownershipType IS NULL OR a.ownershipType = :ownershipType) AND " +
           "(:deviceStatus IS NULL OR a.deviceStatus = :deviceStatus) " +
           "ORDER BY a.createdAt DESC")
    Page<Asset> findByFilters(
           @Param("assetNumber") String assetNumber,
           @Param("serialNumber") String serialNumber,
           @Param("deviceName") String deviceName,
           @Param("deviceTypeId") Long deviceTypeId,
           @Param("userOfAsset") String userOfAsset,
           @Param("zoneId") Long zoneId,
           @Param("office") String office,
           @Param("ownershipType") OwnershipType ownershipType,
           @Param("deviceStatus") DeviceStatus deviceStatus,
           Pageable pageable);
}
