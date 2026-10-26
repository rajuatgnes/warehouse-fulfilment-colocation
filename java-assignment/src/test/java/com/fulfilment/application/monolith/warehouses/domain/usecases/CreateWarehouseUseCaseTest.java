package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreateWarehouseUseCaseTest {

    private WarehouseStore warehouseStore;
    private LocationResolver locationResolver;
    private WarehouseRepository warehouseRepository;
    private CreateWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        warehouseStore = mock(WarehouseStore.class);
        locationResolver = mock(LocationResolver.class);
        warehouseRepository = mock(WarehouseRepository.class);
        useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver, warehouseRepository);
    }

    @Test
    void testCreateSuccess() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "MWH.099";
        w.location = "AMSTERDAM-001";
        w.capacity = 50;
        w.stock = 20;

        when(warehouseStore.findByBusinessUnitCode("MWH.099")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(new Location("AMSTERDAM-001", 5, 100));
        when(warehouseRepository.countActiveByLocation("AMSTERDAM-001")).thenReturn(2L);

        assertDoesNotThrow(() -> useCase.create(w));
        verify(warehouseStore).create(w);
    }

    @Test
    void testDuplicateBusinessUnitCodeThrows() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "MWH.001";
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(new Warehouse());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(w));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void testInvalidLocationThrows() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "MWH.999";
        w.location = "INVALID-LOC";
        when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("INVALID-LOC")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(w));
        assertTrue(ex.getMessage().contains("Invalid location identifier"));
    }

    @Test
    void testMaxWarehousesReachedThrows() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "MWH.999";
        w.location = "ZWOLLE-001";
        when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(new Location("ZWOLLE-001", 1, 40));
        when(warehouseRepository.countActiveByLocation("ZWOLLE-001")).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.create(w));
        assertTrue(ex.getMessage().contains("Maximum number of warehouses reached"));
    }

    @Test
    void testExceedsCapacityOrStockExceedsCapacityThrows() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "MWH.999";
        w.location = "AMSTERDAM-001";
        w.capacity = 150; // exceeds 100
        w.stock = 50;

        when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(new Location("AMSTERDAM-001", 5, 100));

        assertThrows(IllegalArgumentException.class, () -> useCase.create(w));

        w.capacity = 50;
        w.stock = 60; // exceeds capacity
        assertThrows(IllegalArgumentException.class, () -> useCase.create(w));
    }
}