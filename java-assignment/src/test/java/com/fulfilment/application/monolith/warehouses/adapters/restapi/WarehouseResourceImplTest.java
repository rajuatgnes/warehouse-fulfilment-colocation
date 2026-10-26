package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ArchiveWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.CreateWarehouseUseCase;
import com.fulfilment.application.monolith.warehouses.domain.usecases.ReplaceWarehouseUseCase;
import jakarta.ws.rs.WebApplicationException;
import java.lang.reflect.Field;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WarehouseResourceImplTest {

    private WarehouseResourceImpl resource;
    private WarehouseRepository repository;
    private CreateWarehouseUseCase createUseCase;
    private ReplaceWarehouseUseCase replaceUseCase;
    private ArchiveWarehouseUseCase archiveUseCase;

    @BeforeEach
    void setUp() {
        resource = new WarehouseResourceImpl();
        repository = mock(WarehouseRepository.class);
        createUseCase = mock(CreateWarehouseUseCase.class);
        replaceUseCase = mock(ReplaceWarehouseUseCase.class);
        archiveUseCase = mock(ArchiveWarehouseUseCase.class);

        setField(resource, "warehouseRepository", repository);
        setField(resource, "createWarehouseUseCase", createUseCase);
        setField(resource, "replaceWarehouseUseCase", replaceUseCase);
        setField(resource, "archiveWarehouseUseCase", archiveUseCase);
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception ignored) {
        }
    }

    private com.warehouse.api.beans.Warehouse createApiBean(
            String buCode, String location, Integer capacity, Integer stock) {
        com.warehouse.api.beans.Warehouse bean = new com.warehouse.api.beans.Warehouse();
        try {
            bean.getClass().getMethod("setBusinessUnitCode", String.class).invoke(bean, buCode);
            bean.getClass().getMethod("setLocation", String.class).invoke(bean, location);
            bean.getClass().getMethod("setCapacity", Integer.class).invoke(bean, capacity);
            bean.getClass().getMethod("setStock", Integer.class).invoke(bean, stock);
        } catch (Exception e1) {
            try {
                Field buField = bean.getClass().getDeclaredField("businessUnitCode");
                buField.setAccessible(true);
                buField.set(bean, buCode);

                Field locField = bean.getClass().getDeclaredField("location");
                locField.setAccessible(true);
                locField.set(bean, location);

                Field capField = bean.getClass().getDeclaredField("capacity");
                capField.setAccessible(true);
                capField.set(bean, capacity);

                Field stockField = bean.getClass().getDeclaredField("stock");
                stockField.setAccessible(true);
                stockField.set(bean, stock);
            } catch (Exception ignored) {
            }
        }
        return bean;
    }

    private String extractBuCode(com.warehouse.api.beans.Warehouse bean) {
        if (bean == null) return null;
        try {
            return (String) bean.getClass().getMethod("getBusinessUnitCode").invoke(bean);
        } catch (Exception e) {
            try {
                Field f = bean.getClass().getDeclaredField("businessUnitCode");
                f.setAccessible(true);
                return (String) f.get(bean);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    @Test
    void testListAllWarehousesUnits() {
        Warehouse domainWarehouse = new Warehouse();
        domainWarehouse.businessUnitCode = "MWH.001";
        domainWarehouse.location = "ZWOLLE-001";
        domainWarehouse.capacity = 40;
        domainWarehouse.stock = 10;

        when(repository.getAll()).thenReturn(List.of(domainWarehouse));

        List<com.warehouse.api.beans.Warehouse> result = resource.listAllWarehousesUnits();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("MWH.001", extractBuCode(result.get(0)));
    }

    @Test
    void testCreateSuccess() {
        com.warehouse.api.beans.Warehouse apiBean =
                createApiBean("MWH.099", "AMSTERDAM-001", 50, 10);
        doNothing().when(createUseCase).create(any());

        com.warehouse.api.beans.Warehouse result = resource.createANewWarehouseUnit(apiBean);
        assertNotNull(result);
        assertEquals("MWH.099", extractBuCode(result));
    }

    @Test
    void testCreateValidationThrows400() {
        com.warehouse.api.beans.Warehouse apiBean =
                createApiBean("MWH.001", "AMSTERDAM-001", 50, 10);
        doThrow(new IllegalArgumentException("Already exists")).when(createUseCase).create(any());

        WebApplicationException ex =
                assertThrows(
                        WebApplicationException.class, () -> resource.createANewWarehouseUnit(apiBean));
        assertEquals(400, ex.getResponse().getStatus());
    }

    @Test
    void testGetByIdFound() {
        Warehouse domainWarehouse = new Warehouse();
        domainWarehouse.businessUnitCode = "MWH.001";
        domainWarehouse.location = "ZWOLLE-001";
        domainWarehouse.capacity = 40;
        domainWarehouse.stock = 10;

        when(repository.findByIdOrBuCode("MWH.001")).thenReturn(domainWarehouse);

        com.warehouse.api.beans.Warehouse result = resource.getAWarehouseUnitByID("MWH.001");
        assertNotNull(result);
        assertEquals("MWH.001", extractBuCode(result));
    }

    @Test
    void testGetByIdNotFoundThrows404() {
        when(repository.findByIdOrBuCode("MWH.999")).thenReturn(null);

        WebApplicationException ex =
                assertThrows(
                        WebApplicationException.class, () -> resource.getAWarehouseUnitByID("MWH.999"));
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    void testArchiveSuccess() {
        doNothing().when(repository).archiveById("1");
        assertDoesNotThrow(() -> resource.archiveAWarehouseUnitByID("1"));
        verify(repository).archiveById("1");
    }

    @Test
    void testArchiveNotFoundThrows404() {
        doThrow(new IllegalArgumentException("Not found")).when(repository).archiveById("999");
        WebApplicationException ex =
                assertThrows(
                        WebApplicationException.class, () -> resource.archiveAWarehouseUnitByID("999"));
        assertEquals(404, ex.getResponse().getStatus());
    }

    @Test
    void testReplaceSuccess() {
        com.warehouse.api.beans.Warehouse apiBean =
                createApiBean(null, "AMSTERDAM-001", 50, 10);
        doNothing().when(replaceUseCase).replace(any());

        com.warehouse.api.beans.Warehouse result =
                resource.replaceTheCurrentActiveWarehouse("MWH.001", apiBean);
        assertNotNull(result);
        assertEquals("MWH.001", extractBuCode(result));
    }

    @Test
    void testReplaceNotFoundThrows404() {
        com.warehouse.api.beans.Warehouse apiBean =
                createApiBean(null, "AMSTERDAM-001", 50, 10);
        doThrow(new IllegalArgumentException("Active warehouse not found"))
                .when(replaceUseCase)
                .replace(any());

        WebApplicationException ex =
                assertThrows(
                        WebApplicationException.class,
                        () -> resource.replaceTheCurrentActiveWarehouse("MWH.999", apiBean));
        assertEquals(404, ex.getResponse().getStatus());
    }
}