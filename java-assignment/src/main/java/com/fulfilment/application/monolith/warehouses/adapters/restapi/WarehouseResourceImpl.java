package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject WarehouseRepository warehouseRepository;
  @Inject CreateWarehouseUseCase createWarehouseUseCase;
  @Inject ReplaceWarehouseUseCase replaceWarehouseUseCase;
  @Inject ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    try {
      var domainModel = toDomainModel(data);
      createWarehouseUseCase.create(domainModel);
      return toWarehouseResponse(domainModel);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    var domain = warehouseRepository.findByIdOrBuCode(id);
    if (domain == null) {
      throw new WebApplicationException("Warehouse not found for ID: " + id, 404);
    }
    return toWarehouseResponse(domain);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    try {
      warehouseRepository.archiveById(id);
    } catch (IllegalArgumentException e) {
      throw new WebApplicationException(e.getMessage(), 404);
    }
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, @NotNull Warehouse data) {
    try {
      var domainModel = toDomainModel(data);
      domainModel.businessUnitCode = businessUnitCode;
      replaceWarehouseUseCase.replace(domainModel);
      return toWarehouseResponse(domainModel);
    } catch (IllegalArgumentException e) {
      if (e.getMessage().toLowerCase().contains("not found")) {
        throw new WebApplicationException(e.getMessage(), 404);
      }
      throw new WebApplicationException(e.getMessage(), 400);
    }
  }

  private Warehouse toWarehouseResponse(
          com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);
    return response;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainModel(Warehouse bean) {
    var model = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    model.businessUnitCode = bean.getBusinessUnitCode();
    model.location = bean.getLocation();
    model.capacity = bean.getCapacity();
    model.stock = bean.getStock();
    return model;
  }
}