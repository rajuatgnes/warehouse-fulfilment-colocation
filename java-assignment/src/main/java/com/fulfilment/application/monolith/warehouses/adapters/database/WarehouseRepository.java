package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.list("archivedAt is null").stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    DbWarehouse db = new DbWarehouse();
    db.businessUnitCode = warehouse.businessUnitCode;
    db.location = warehouse.location;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;
    db.createdAt = warehouse.createdAt != null ? warehouse.createdAt : LocalDateTime.now();
    db.archivedAt = warehouse.archivedAt;
    this.persist(db);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse db = this.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
            .firstResult();
    if (db != null) {
      db.location = warehouse.location;
      db.capacity = warehouse.capacity;
      db.stock = warehouse.stock;
      db.archivedAt = warehouse.archivedAt;
      this.persist(db);
    }
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    DbWarehouse db = this.find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
            .firstResult();
    if (db != null) {
      db.archivedAt = LocalDateTime.now();
      this.persist(db);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse db = this.find("businessUnitCode = ?1 and archivedAt is null", buCode)
            .firstResult();
    return db != null ? db.toWarehouse() : null;
  }

  public long countActiveByLocation(String location) {
    return this.count("location = ?1 and archivedAt is null", location);
  }

  public Warehouse findByIdOrBuCode(String identifier) {
    try {
      Long id = Long.parseLong(identifier);
      DbWarehouse db = this.findById(id);
      if (db != null && db.archivedAt == null) {
        return db.toWarehouse();
      }
    } catch (NumberFormatException ignored) {
      // Not a numeric ID, fall back to businessUnitCode
    }
    return findByBusinessUnitCode(identifier);
  }

  @Transactional
  public void archiveById(String identifier) {
    DbWarehouse db = null;
    try {
      Long id = Long.parseLong(identifier);
      db = this.findById(id);
    } catch (NumberFormatException ignored) {
      db = this.find("businessUnitCode = ?1 and archivedAt is null", identifier).firstResult();
    }

    if (db != null && db.archivedAt == null) {
      db.archivedAt = LocalDateTime.now();
      this.persist(db);
    } else {
      throw new IllegalArgumentException("Warehouse not found for ID: " + identifier);
    }
  }
}