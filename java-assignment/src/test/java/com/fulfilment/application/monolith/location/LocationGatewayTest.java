package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocationGatewayTest {

  private LocationGateway locationGateway;

  @BeforeEach
  void setUp() {
    locationGateway = new LocationGateway();
  }

  @Test
  void testResolveExistingLocation() {
    var loc = locationGateway.resolveByIdentifier("ZWOLLE-001");
    assertNotNull(loc);
    assertEquals("ZWOLLE-001", loc.identification);
    assertEquals(1, loc.maxNumberOfWarehouses);
    assertEquals(40, loc.maxCapacity);
  }

  @Test
  void testResolveCaseInsensitive() {
    var loc = locationGateway.resolveByIdentifier("amsterdam-001");
    assertNotNull(loc);
    assertEquals("AMSTERDAM-001", loc.identification);
  }

  @Test
  void testResolveUnknownOrNullReturnsNull() {
    assertNull(locationGateway.resolveByIdentifier("UNKNOWN-999"));
    assertNull(locationGateway.resolveByIdentifier(null));
    assertNull(locationGateway.resolveByIdentifier("  "));
  }
}