package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "fulfillment_assignment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"storeId", "productId", "warehouseBuCode"})
)
public class FulfillmentAssignment extends PanacheEntity {
    public Long storeId;
    public Long productId;
    public String warehouseBuCode;

    public FulfillmentAssignment() {}

    public FulfillmentAssignment(Long storeId, Long productId, String warehouseBuCode) {
        this.storeId = storeId;
        this.productId = productId;
        this.warehouseBuCode = warehouseBuCode;
    }
}