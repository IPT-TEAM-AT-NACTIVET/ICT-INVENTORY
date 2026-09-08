package tz.go.nactvet.ict_inventory_management.repository;

import java.util.Collection;
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

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    @EntityGraph(attributePaths = {"zone", "deviceType", "directorate"})
    List<Asset> findAllByOrderByCreatedAtDesc();

    @EntityGraph(attributePaths = {"zone", "deviceType", "directorate"})
    List<Asset> findByDeviceTypeId(Long deviceTypeId);

    @EntityGraph(attributePaths = {"zone", "deviceType", "directorate"})
    List<Asset> findByDeviceStatus(DeviceStatus status);

    @EntityGraph(attributePaths = {"zone", "deviceType", "directorate"})
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

    @Query("SELECT a.assetNumber, a.deviceModel, a.deviceType.name, a.userOfAsset, " +
           "a.zone.name, a.office, cb.fullName, a.createdAt " +
           "FROM Asset a " +
           "LEFT JOIN a.createdBy cb " +
           "ORDER BY a.createdAt DESC")
    List<Object[]> findRecentRegistrations(Pageable pageable);

    @Query("SELECT a.zone.id, a.zone.name, COUNT(a) FROM Asset a " +
           "WHERE a.zone IS NOT NULL AND (" +
           "  (:s IS NULL) OR " +
           "  LOWER(COALESCE(a.assetNumber,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.serialNumber,'')) LIKE :s OR " +
           "  LOWER(a.deviceModel) LIKE :s OR " +
           "  LOWER(COALESCE(a.userOfAsset,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.office,'')) LIKE :s OR " +
           "  LOWER(a.zone.name) LIKE :s OR " +
           "  LOWER(COALESCE(a.directorate.name,'')) LIKE :s OR " +
           "  LOWER(a.deviceType.name) LIKE :s OR " +
           "  LOWER(CAST(a.deviceStatus AS string)) LIKE :s " +
           ") GROUP BY a.zone.id, a.zone.name")
    List<Object[]> countByZoneGroupedWithSearch(@Param("s") String search);

    @Query("SELECT a.deviceType.id, a.deviceType.name, COUNT(a) FROM Asset a " +
           "WHERE a.deviceType IS NOT NULL AND (" +
           "  (:s IS NULL) OR " +
           "  LOWER(COALESCE(a.assetNumber,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.serialNumber,'')) LIKE :s OR " +
           "  LOWER(a.deviceModel) LIKE :s OR " +
           "  LOWER(COALESCE(a.userOfAsset,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.office,'')) LIKE :s OR " +
           "  LOWER(a.zone.name) LIKE :s OR " +
           "  LOWER(COALESCE(a.directorate.name,'')) LIKE :s OR " +
           "  LOWER(a.deviceType.name) LIKE :s OR " +
           "  LOWER(CAST(a.deviceStatus AS string)) LIKE :s " +
           ") GROUP BY a.deviceType.id, a.deviceType.name")
    List<Object[]> countByDeviceTypeGroupedWithSearch(@Param("s") String search);

    @Query("SELECT a.deviceStatus, COUNT(a) FROM Asset a " +
           "WHERE (" +
           "  (:s IS NULL) OR " +
           "  LOWER(COALESCE(a.assetNumber,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.serialNumber,'')) LIKE :s OR " +
           "  LOWER(a.deviceModel) LIKE :s OR " +
           "  LOWER(COALESCE(a.userOfAsset,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.office,'')) LIKE :s OR " +
           "  LOWER(a.zone.name) LIKE :s OR " +
           "  LOWER(COALESCE(a.directorate.name,'')) LIKE :s OR " +
           "  LOWER(a.deviceType.name) LIKE :s OR " +
           "  LOWER(CAST(a.deviceStatus AS string)) LIKE :s " +
           ") GROUP BY a.deviceStatus")
    List<Object[]> countByDeviceStatusGroupedWithSearch(@Param("s") String search);

    @Query("SELECT COUNT(a) FROM Asset a WHERE (" +
           "  (:s IS NULL) OR " +
           "  LOWER(COALESCE(a.assetNumber,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.serialNumber,'')) LIKE :s OR " +
           "  LOWER(a.deviceModel) LIKE :s OR " +
           "  LOWER(COALESCE(a.userOfAsset,'')) LIKE :s OR " +
           "  LOWER(COALESCE(a.office,'')) LIKE :s OR " +
           "  LOWER(a.zone.name) LIKE :s OR " +
           "  LOWER(COALESCE(a.directorate.name,'')) LIKE :s OR " +
           "  LOWER(a.deviceType.name) LIKE :s OR " +
           "  LOWER(CAST(a.deviceStatus AS string)) LIKE :s " +
           ")")
    long countWithSearch(@Param("s") String search);

    @EntityGraph(attributePaths = {"zone", "deviceType", "directorate"})
    Page<Asset> findByOrderByCreatedAtDesc(Pageable pageable);

@Query("SELECT a FROM Asset a " +
           "LEFT JOIN FETCH a.zone " +
           "LEFT JOIN FETCH a.deviceType " +
           "LEFT JOIN FETCH a.directorate " +
           "LEFT JOIN FETCH a.createdBy " +
           "WHERE LOWER(COALESCE(a.assetNumber, '')) LIKE :search " +
           "OR LOWER(COALESCE(a.serialNumber, '')) LIKE :search " +
           "OR LOWER(a.deviceModel) LIKE :search " +
           "OR LOWER(a.userOfAsset) LIKE :search " +
           "OR LOWER(COALESCE(a.office, '')) LIKE :search " +
           "OR LOWER(COALESCE(a.directorate.name, '')) LIKE :search " +
           "OR LOWER(CAST(a.deviceStatus AS string)) LIKE :search " +
           "OR LOWER(a.zone.name) LIKE :search " +
           "OR LOWER(a.deviceType.name) LIKE :search " +
           "ORDER BY a.createdAt DESC")
    Page<Asset> findBySearch(@Param("search") String search, Pageable pageable);

    @Query("SELECT a FROM Asset a " +
           "LEFT JOIN FETCH a.zone " +
           "LEFT JOIN FETCH a.deviceType " +
           "LEFT JOIN FETCH a.directorate " +
           "LEFT JOIN FETCH a.createdBy " +
           "WHERE (:statusOn = true AND a.deviceStatus IN :statuses) " +
           "OR (:statusOn = false AND (:term IS NULL " +
           "OR LOWER(COALESCE(a.assetNumber, '')) LIKE :term " +
           "OR LOWER(COALESCE(a.serialNumber, '')) LIKE :term " +
           "OR LOWER(a.deviceModel) LIKE :term " +
           "OR LOWER(COALESCE(a.userOfAsset, '')) LIKE :term " +
           "OR LOWER(COALESCE(a.office, '')) LIKE :term " +
           "OR LOWER(COALESCE(a.createdBy.fullName, '')) LIKE :term " +
           "OR LOWER(a.zone.name) LIKE :term " +
           "OR LOWER(COALESCE(a.directorate.name, '')) LIKE :term " +
           "OR LOWER(a.deviceType.name) LIKE :term)) " +
           "ORDER BY a.createdAt DESC")
    List<Asset> searchAll(@Param("term") String term,
                          @Param("statusOn") boolean statusOn,
                          @Param("statuses") Collection<DeviceStatus> statuses);

    @Query("SELECT a FROM Asset a " +
           "LEFT JOIN FETCH a.zone " +
           "LEFT JOIN FETCH a.deviceType " +
           "LEFT JOIN FETCH a.directorate WHERE " +
           "(:assetNumber IS NULL OR a.assetNumber LIKE %:assetNumber%) AND " +
           "(:serialNumber IS NULL OR a.serialNumber LIKE %:serialNumber%) AND " +
           "(:deviceModel IS NULL OR a.deviceModel LIKE %:deviceModel%) AND " +
           "(:deviceTypeId IS NULL OR a.deviceType.id = :deviceTypeId) AND " +
           "(:userOfAsset IS NULL OR a.userOfAsset LIKE %:userOfAsset%) AND " +
           "(:zoneId IS NULL OR a.zone.id = :zoneId) AND " +
           "(:office IS NULL OR a.office LIKE %:office%) AND " +
           "(:deviceStatus IS NULL OR a.deviceStatus = :deviceStatus) " +
           "ORDER BY a.createdAt DESC")
    Page<Asset> findByFilters(
           @Param("assetNumber") String assetNumber,
           @Param("serialNumber") String serialNumber,
           @Param("deviceModel") String deviceModel,
           @Param("deviceTypeId") Long deviceTypeId,
           @Param("userOfAsset") String userOfAsset,
           @Param("zoneId") Long zoneId,
           @Param("office") String office,
           @Param("deviceStatus") DeviceStatus deviceStatus,
           Pageable pageable);

    @Query("SELECT a FROM Asset a " +
           "LEFT JOIN FETCH a.zone " +
           "LEFT JOIN FETCH a.deviceType " +
           "LEFT JOIN FETCH a.directorate " +
           "LEFT JOIN FETCH a.createdBy " +
           "WHERE (" +
           "  (:statusOn = true AND a.deviceStatus IN :statuses) " +
           "  OR " +
           "  (:statusOn = false AND (" +
           "    LOWER(COALESCE(a.assetNumber,'')) LIKE :term OR " +
           "    LOWER(COALESCE(a.serialNumber,'')) LIKE :term OR " +
           "    LOWER(a.deviceModel) LIKE :term OR " +
           "    LOWER(COALESCE(a.userOfAsset,'')) LIKE :term OR " +
           "    LOWER(COALESCE(a.office,'')) LIKE :term OR " +
           "    LOWER(a.zone.name) LIKE :term OR " +
           "    LOWER(COALESCE(a.directorate.name,'')) LIKE :term OR " +
           "    LOWER(a.deviceType.name) LIKE :term OR " +
           "    LOWER(CAST(a.deviceStatus AS string)) LIKE :term OR " +
           "    LOWER(COALESCE(a.createdBy.fullName,'')) LIKE :term" +
           "  ))" +
           ") " +
           "AND (:deviceTypeId IS NULL OR a.deviceType.id = :deviceTypeId) " +
           "AND (:explicitStatus IS NULL OR a.deviceStatus = :explicitStatus) " +
           "AND (:zoneId IS NULL OR a.zone.id = :zoneId) " +
           "AND (:office = '' OR LOWER(COALESCE(a.office,'')) LIKE :office) " +
           "AND (:userOfAsset = '' OR LOWER(COALESCE(a.userOfAsset,'')) LIKE :userOfAsset) " +
           "AND (:registeredBy = '' OR LOWER(COALESCE(a.createdBy.fullName,'')) LIKE :registeredBy) " +
           "AND a.createdAt >= COALESCE(:fromDt, a.createdAt) " +
           "AND a.createdAt <= COALESCE(:toDt, a.createdAt) " +
           "ORDER BY a.createdAt DESC")
    List<Asset> findForReport(
           @Param("term") String term,
           @Param("statusOn") boolean statusOn,
           @Param("statuses") Collection<DeviceStatus> statuses,
           @Param("deviceTypeId") Long deviceTypeId,
           @Param("explicitStatus") DeviceStatus explicitStatus,
           @Param("zoneId") Long zoneId,
           @Param("office") String office,
           @Param("userOfAsset") String userOfAsset,
           @Param("registeredBy") String registeredBy,
           @Param("fromDt") java.time.LocalDateTime fromDt,
           @Param("toDt") java.time.LocalDateTime toDt);

    @Query("SELECT DISTINCT a.office FROM Asset a " +
           "WHERE a.office IS NOT NULL AND a.office <> '' ORDER BY a.office")
    List<String> findDistinctOffices();

    @Query("SELECT DISTINCT a.userOfAsset FROM Asset a " +
           "WHERE a.userOfAsset IS NOT NULL AND a.userOfAsset <> '' ORDER BY a.userOfAsset")
    List<String> findDistinctUsersOfAsset();

    @Query("SELECT a.createdBy.id, a.createdBy.fullName FROM Asset a " +
           "WHERE a.createdBy IS NOT NULL " +
           "GROUP BY a.createdBy.id, a.createdBy.fullName " +
           "ORDER BY a.createdBy.fullName")
    List<Object[]> findDistinctRegistrars();
}
