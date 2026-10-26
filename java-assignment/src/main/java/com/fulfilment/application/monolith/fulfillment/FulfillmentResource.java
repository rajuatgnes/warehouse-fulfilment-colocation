package com.fulfilment.application.monolith.fulfillment;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("fulfillment")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FulfillmentResource {

    @Inject FulfillmentService fulfillmentService;

    public static class AssignRequest {
        public Long storeId;
        public Long productId;
        public String warehouseBuCode;
    }

    @POST
    @Path("assign")
    public Response assign(AssignRequest request) {
        if (request == null || request.storeId == null || request.productId == null || request.warehouseBuCode == null) {
            return Response.status(400).entity("storeId, productId, and warehouseBuCode are required").build();
        }
        var assignment = fulfillmentService.assign(request.storeId, request.productId, request.warehouseBuCode);
        return Response.status(201).entity(assignment).build();
    }
}