# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**
### Challenges:
1. Shared & Indirect Cost Attribution: Overhead expenses (e.g., facility leases, utilities, IT infrastructure, cross-dock transportation) cannot be mapped 1:1 to a single store or stock-keeping unit.
2. Dynamic Variable Costs: Labor costs fluctuate across peak/off-peak shifts, seasonal demand surges, and handling complexity (e.g., bulk pallet vs. each-level picking). Transportation costs change dynamically with carrier surcharges, fuel indexing, and multi-drop routing.
3. Inventory Depreciation & Shrinkage: Allocating losses from damaged stock, obsolescence, and holding carrying costs across fulfillment centers and receiving retail stores over time.

### Key Architectural & Domain Considerations:
- Activity-Based Costing (ABC): Allocate indirect costs based on actual consumption of resources (e.g., per pick, pack, cubic-meter-day of storage, and delivery trip) rather than flat split percentages.
- Granular Event-Driven Cost Ingestion: Capture cost events at the transaction level (receiving, picking, dispatching, transit leg completion) using distributed messaging (Kafka/event logs) to maintain an audit trail.

### Clarifying Questions for Business & Stakeholders:
- What is the required allocation granularity (unit level, product category, order level, or aggregate store level)?
- How should multi-destination consolidation runs be split among participating stores?
- What cadence is required for cost reconciliation (real-time stream vs. end-of-day batch ledger updates)?

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**
### Potential Strategies & Expected Outcomes:
1. Dynamic Multi-Node Fulfillment Routing: Route store replenishment and customer orders to the geographically nearest warehouse with sufficient stock to minimize transit legs, carrier fees, and last-mile transit times.
2. Smart Stock Balancing & Slotting: Place high-velocity (fast-moving) items in forward fulfillment locations and lower-level shelves to reduce labor pick times.
3. Consolidated Batched Dispatch: Batch store deliveries into optimal shipping windows to maximize vehicle volume utilization (cube-out before weigh-out).

### Identification, Prioritization & Implementation Framework:
- Identification: Aggregate fulfillment operational data (cost-per-pick, route efficiency, storage utilization vs. idle capacity) to locate cost bottlenecks.
- Prioritization (Impact vs. Effort Matrix): Prioritize "High Impact, Low/Medium Effort" initiatives—such as automated nearest-warehouse routing algorithms—before high-capex warehouse automation or conveyor overhauls.
- Implementation: Implement changes using A/B testing or regional pilot rollouts. Track KPIs such as Cost Per Unit (CPU), On-Time-In-Full (OTIF) delivery rate, and fulfillment cycle duration to ensure service quality is maintained.

### Clarifying Questions for Business & Stakeholders:
- What are the non-negotiable SLAs (e.g., maximum store replenishment lead time)?
- Are warehouse contracts fixed-lease or third-party logistics (3PL) on-demand variable rate models?

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
### Importance & Business Benefits:
1. Real-Time Financial Visibility: Eliminates month-end reporting surprises by giving finance teams immediate visibility into operational expenses, margins, and cost overruns.
2. Accelerated Book Closing: Automated ledger synchronization eliminates tedious manual data exports and spreadsheet reconciliations.
3. Unified Audit Trail: Establishes a verifiable chain of custody linking physical warehouse movements to general ledger entries.

### Ensuring Seamless Integration & Data Synchronization:
- Asynchronous Event-Driven Messaging: Decouple fulfillment services from financial ERPs (e.g., SAP, NetSuite) using Kafka/message brokers to ensure high availability and prevent ERP downtime from impacting warehouse operations.
- Transactional Outbox Pattern: Ensure that internal operational database updates and outgoing financial events are published atomically, preventing data discrepancies.
- Idempotency & Deduplication: Assign unique transaction identifiers (idempotency keys) to every cost entry so retries do not create duplicate balance adjustments in the ERP.
- Two-Way Reconciliation Jobs: Run automated nightly reconciliation jobs to flag and alert on any discrepancies between operational cost stores and financial general ledgers.

### Clarifying Questions for Business & Stakeholders:
- What integration interfaces does the financial system expose (REST APIs, GraphQL, Kafka, or SFTP/batch files)?
- What retry policies, dead-letter queue (DLQ) procedures, and manual reconciliation workflows are required by compliance and audit teams?

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
### Importance:
Predictive budgeting enables supply chain leaders to anticipate seasonal peaks (e.g., holiday seasons, flash sales), procure temporary storage and labor at favorable pre-negotiated rates, and avoid costly emergency logistics or stockouts.

### System Design Considerations:
1. Multi-Variable Driver Modeling: Factor in historical cost trends, seasonal velocity, planned promotional calendars, inflation, and carrier contract escalations.
2. Dynamic Scenario Simulation ("What-If" Analysis): Allow operations managers to model hypothetical disruptions (e.g., "What if regional fuel surcharges rise 12%?" or "What if Store X doubles throughput?").
3. Rolling Forecasts vs. Static Annual Budgets: Continually refine 30/60/90-day projections based on real-time consumption data rather than relying solely on fixed annual plans.
4. Granular Dimensionality: Design the persistence layer to aggregate and slice data across multiple dimensions (by Geography, Business Unit Code, Product Class, and Vendor).

### Clarifying Questions for Business & Stakeholders:
- What variance thresholds should trigger automated budget overrun alerts to regional directors?
- How frequently will forecasting algorithms be recalibrated against actuals?

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
### Why Preserving Cost History is Critical:
1. Baseline Benchmarking: Historical operational data (cost-per-unit, average utility overhead, maintenance expenditures) serves as the baseline for evaluating whether the replacement facility actually delivers projected ROI and operational efficiencies.
2. Compliance & Long-Term Auditing: Tax, lease, and financial regulations require complete historical records of physical assets, depreciation schedules, and historical operational expenditures.
3. Transition Cost Isolation: Migration costs (decommissioning the old facility, physical stock transfer, setup of new infrastructure) must be segregated from steady-state operational expenses to avoid distorting baseline budgets.

### Keeping the New Warehouse Operation Within Budget:
- Entity Versioning / Temporal Modeling: Even though the Business Unit Code (BUC) is reused, the database must maintain distinct physical warehouse records with lifecycle timestamps (`createdAt`, `archivedAt`). Cost records must link to both the immutable record ID and the persistent BUC.
- Step-Cost Tracking: Track dual-running costs during the transition phase when both facilities may temporarily overlap to prevent budget overruns.
- Variance Alerting: Compare post-launch operational costs against historical BUC trends; identify any immediate post-replacement spikes (e.g., higher cooling costs or onboarding-related pick inefficiencies).

### Clarifying Questions for Business & Stakeholders:
- How should transitional moving costs be amortized (capitalized or expensed directly to the existing BUC)?
- Does the finance team require combined historical BUC reports, or do they need distinct separated views for pre-replacement and post-replacement entities?

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.