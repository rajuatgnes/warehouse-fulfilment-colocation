package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArchiveWarehouseUseCaseTest {

    private WarehouseStore warehouseStore;
    private ArchiveWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        warehouseStore = mock(WarehouseStore.class);
        useCase = new ArchiveWarehouseUseCase(warehouseStore);
    }

    @Test
    void testArchiveSuccess() {
        Warehouse w = new Warehouse();
        w.businessUnitCode = "MWH.001";

        assertDoesNotThrow(() -> useCase.archive(w));
        verify(warehouseStore).remove(w);
    }

    @Test
    void testArchiveNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> useCase.archive(null));
    }
}