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

class ReplaceWarehouseUseCaseTest {

    private WarehouseStore warehouseStore;
    private LocationResolver locationResolver;
    private WarehouseRepository warehouseRepository;
    private ReplaceWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        warehouseStore = mock(WarehouseStore.class);
        locationResolver = mock(LocationResolver.class);
        warehouseRepository = mock(WarehouseRepository.class);
        useCase = new ReplaceWarehouseUseCase(warehouseStore, locationResolver, warehouseRepository);
    }

    @Test
    void testReplaceSuccess() {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "MWH.001";
        existing.location = "ZWOLLE-001";
        existing.capacity = 40;
        existing.stock = 10;

        Warehouse replacement = new Warehouse();
        replacement.businessUnitCode = "MWH.001";
        replacement.location = "ZWOLLE-001";
        replacement.capacity = 40;
        replacement.stock = 10;

        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(new Location("ZWOLLE-001", 1, 40));

        assertDoesNotThrow(() -> useCase.replace(replacement));
        verify(warehouseStore).remove(existing);
        verify(warehouseStore).create(replacement);
    }

    @Test
    void testReplaceNonExistentWarehouseThrows() {
        Warehouse replacement = new Warehouse();
        replacement.businessUnitCode = "MWH.404";
        when(warehouseStore.findByBusinessUnitCode("MWH.404")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
        assertTrue(ex.getMessage().contains("Active warehouse not found"));
    }

    @Test
    void testCapacityCannotAccommodatePreviousStockThrows() {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "MWH.001";
        existing.location = "ZWOLLE-001";
        existing.stock = 25;

        Warehouse replacement = new Warehouse();
        replacement.businessUnitCode = "MWH.001";
        replacement.location = "ZWOLLE-001";
        replacement.capacity = 20; // less than stock of 25
        replacement.stock = 25;

        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(new Location("ZWOLLE-001", 1, 40));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
        assertTrue(ex.getMessage().contains("cannot accommodate current stock"));
    }

    @Test
    void testStockMismatchThrows() {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "MWH.001";
        existing.location = "ZWOLLE-001";
        existing.stock = 10;

        Warehouse replacement = new Warehouse();
        replacement.businessUnitCode = "MWH.001";
        replacement.location = "ZWOLLE-001";
        replacement.capacity = 40;
        replacement.stock = 15; // Does not match existing 10

        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
        when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
                .thenReturn(new Location("ZWOLLE-001", 1, 40));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> useCase.replace(replacement));
        assertTrue(ex.getMessage().contains("must match previous warehouse stock"));
    }
}