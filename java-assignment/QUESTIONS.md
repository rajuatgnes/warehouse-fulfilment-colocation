# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would refactor the data access layer to standardize on a clean Hexagonal Architecture / Repository Pattern across all domains.

Currently, the code base mixes two distinct persistence paradigms:
1. Active Record Pattern (PanacheEntity): Used in Product and Store, where domain entities inherit from PanacheEntity and perform persistence directly via static methods (e.g., Product.persist(), Product.listAll()). While convenient for rapid prototyping, this tightly couples domain models with Hibernate/Panache ORM, making pure unit testing difficult without starting an in-memory database or mocking static methods.
2. Repository / Ports & Adapters (Hexagonal Architecture): Used in the Warehouse module, separating pure domain models (domain.models.Warehouse) from database entities (DbWarehouse) and abstracting persistence behind an explicit port (WarehouseStore) implemented by WarehouseRepository (PanacheRepositoryBase).

Why Refactor:
- Architectural Consistency: Having two different patterns in the same monolith increases cognitive load for engineers and leads to inconsistent architectural standards.
- Domain Purity & Testability: Decoupling business logic from the ORM layer allows use cases to be tested using lightweight in-memory stubs/mocks without requiring Hibernate sessions.
- Encapsulation: The Repository pattern enforces DDD boundaries, preventing controller or gateway classes from executing unchecked queries directly against database entities.

Recommendation:
Migrate Product and Store to use explicit repository interfaces (e.g., ProductRepository implements PanacheRepository<Product>) and keep domain business logic cleanly isolated in dedicated use-case services.