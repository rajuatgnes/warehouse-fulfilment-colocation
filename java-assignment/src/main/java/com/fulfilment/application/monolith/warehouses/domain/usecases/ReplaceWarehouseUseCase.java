package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;
  private final WarehouseRepository warehouseRepository;

  @Inject
  public ReplaceWarehouseUseCase(
          WarehouseStore warehouseStore,
          LocationResolver locationResolver,
          WarehouseRepository warehouseRepository) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
    this.warehouseRepository = warehouseRepository;
  }

  @Override
  @Transactional
  public void replace(Warehouse newWarehouse) {
    if (newWarehouse == null || newWarehouse.businessUnitCode == null) {
      throw new IllegalArgumentException("Warehouse and business unit code are required");
    }

    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      throw new IllegalArgumentException("Active warehouse not found for code: " + newWarehouse.businessUnitCode);
    }

    Location targetLocation = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (targetLocation == null) {
      throw new IllegalArgumentException("Invalid location identifier: " + newWarehouse.location);
    }

    if (newWarehouse.capacity == null || newWarehouse.capacity > targetLocation.maxCapacity) {
      throw new IllegalArgumentException("Capacity exceeds target location maximum capacity: " + targetLocation.maxCapacity);
    }
    if (newWarehouse.capacity < existing.stock) {
      throw new IllegalArgumentException("New warehouse capacity cannot accommodate current stock: " + existing.stock);
    }

    if (newWarehouse.stock == null || !newWarehouse.stock.equals(existing.stock)) {
      throw new IllegalArgumentException("New warehouse stock (" + newWarehouse.stock
              + ") must match previous warehouse stock (" + existing.stock + ")");
    }

    if (!existing.location.equalsIgnoreCase(newWarehouse.location) && warehouseRepository != null) {
      long currentWarehouses = warehouseRepository.countActiveByLocation(newWarehouse.location);
      if (currentWarehouses >= targetLocation.maxNumberOfWarehouses) {
        throw new IllegalArgumentException("Target location has reached maximum warehouse capacity");
      }
    }

    warehouseStore.remove(existing);

    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}