package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;
  private final WarehouseRepository warehouseRepository;

  @Inject
  public CreateWarehouseUseCase(
          WarehouseStore warehouseStore,
          LocationResolver locationResolver,
          WarehouseRepository warehouseRepository) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
    this.warehouseRepository = warehouseRepository;
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse payload cannot be null");
    }
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw new IllegalArgumentException("Business unit code is required");
    }

    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new IllegalArgumentException("Warehouse already exists with code: " + warehouse.businessUnitCode);
    }

    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid location identifier: " + warehouse.location);
    }

    long currentWarehouses = warehouseRepository != null
            ? warehouseRepository.countActiveByLocation(warehouse.location)
            : 0;
    if (currentWarehouses >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException("Maximum number of warehouses reached for location: " + warehouse.location);
    }

    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw new IllegalArgumentException("Warehouse capacity must be greater than zero");
    }
    if (warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException("Warehouse capacity exceeds maximum allowed for location: " + location.maxCapacity);
    }
    if (warehouse.stock == null || warehouse.stock < 0) {
      throw new IllegalArgumentException("Stock cannot be negative");
    }
    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Stock exceeds warehouse capacity");
    }

    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;

    warehouseStore.create(warehouse);
  }
}