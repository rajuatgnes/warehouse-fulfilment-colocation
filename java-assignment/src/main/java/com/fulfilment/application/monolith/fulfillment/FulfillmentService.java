package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class FulfillmentService {

    @Inject WarehouseRepository warehouseRepository;
    @Inject ProductRepository productRepository;

    @Transactional
    public FulfillmentAssignment assign(Long storeId, Long productId, String warehouseBuCode) {
        if (Store.findById(storeId) == null) {
            throw new WebApplicationException("Store does not exist: " + storeId, 404);
        }
        if (productRepository.findById(productId) == null) {
            throw new WebApplicationException("Product does not exist: " + productId, 404);
        }
        if (warehouseRepository.findByBusinessUnitCode(warehouseBuCode) == null) {
            throw new WebApplicationException("Warehouse does not exist: " + warehouseBuCode, 404);
        }

        // Check if already assigned
        FulfillmentAssignment existing = FulfillmentAssignment.find(
                "storeId = ?1 and productId = ?2 and warehouseBuCode = ?3",
                storeId, productId, warehouseBuCode).firstResult();
        if (existing != null) {
            return existing;
        }

        // Constraint 1: Max 2 Warehouses per Product per Store
        long warehousesForProductInStore = FulfillmentAssignment.find(
                        "storeId = ?1 and productId = ?2", storeId, productId)
                .stream().map(a -> ((FulfillmentAssignment) a).warehouseBuCode).distinct().count();
        if (warehousesForProductInStore >= 2) {
            throw new WebApplicationException("Each product can be fulfilled by a maximum of 2 warehouses per store", 400);
        }

        // Constraint 2: Max 3 Warehouses per Store
        long currentWarehousesInStore = FulfillmentAssignment.find("storeId = ?1", storeId)
                .stream().map(a -> ((FulfillmentAssignment) a).warehouseBuCode).distinct().count();
        boolean isNewWarehouseForStore = FulfillmentAssignment.count(
                "storeId = ?1 and warehouseBuCode = ?2", storeId, warehouseBuCode) == 0;
        if (isNewWarehouseForStore && currentWarehousesInStore >= 3) {
            throw new WebApplicationException("Each store can be fulfilled by a maximum of 3 warehouses", 400);
        }

        // Constraint 3: Max 5 types of Products per Warehouse
        long currentProductsInWarehouse = FulfillmentAssignment.find("warehouseBuCode = ?1", warehouseBuCode)
                .stream().map(a -> ((FulfillmentAssignment) a).productId).distinct().count();
        boolean isNewProductForWarehouse = FulfillmentAssignment.count(
                "warehouseBuCode = ?1 and productId = ?2", warehouseBuCode, productId) == 0;
        if (isNewProductForWarehouse && currentProductsInWarehouse >= 5) {
            throw new WebApplicationException("Each warehouse can store maximally 5 types of products", 400);
        }

        FulfillmentAssignment assignment = new FulfillmentAssignment(storeId, productId, warehouseBuCode);
        assignment.persist();
        return assignment;
    }
}