# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**
Fulfillment cost is rarely incurred “at the place that looks like the cost centre.” Labour sits in a warehouse, inventory carrying cost sits on SKUs, transport sits on lanes between warehouse and store, and overhead is shared. If we allocate poorly, stores look unprofitable, warehouses look efficient, and we make the wrong network decisions.

Challenges I would expect:
- Shared resources: one warehouse fulfils several stores; one truck drop may serve multiple stores; corporate IT and leases are not naturally “per warehouse.”
- Time: stock moves; a warehouse can be replaced and archived while its cost history must remain comparable.
- Granularity vs effort: activity-based costing is more accurate but expensive to operate; a simple % of revenue allocation is cheap but hides loss-making nodes.
- Data quality: labour clocked to the wrong business unit, missing trip-level freight, inventory valued at different standards.

Considerations / what I have seen work:
- Separate *capture* (what was spent, when, against which source system) from *allocation* (rules that assign that spend to warehouse, store, product, or channel). Rules will change; raw actuals should not.
- Use a small set of allocation keys that operations already trust: shipped units, cubic volume, pick lines, kilometres, occupied pallet positions—not a single key for every cost type.
- Align dimensions with this domain model: Location, Warehouse (business unit code + time range, because replace archives a row), Store, Product. Cost posted to a warehouse should survive archive.
- Treat inventory cost and fulfilment cost separately. Mixing COGS with pick/pack/ship makes optimisation discussions muddy.

Questions I would ask before scoping:
- Which decisions will this allocation actually change (network design, store ranging, 3PL vs in-house, pricing)?
- What is already in finance (GL, cost centres, labour, freight invoices) vs what operations systems can provide (picks, kilometres, cube)?
- Do we need product-level cost or is warehouse/store enough for year one?
- Who owns the allocation rules when ops and finance disagree?
- How do we handle a warehouse replacement: same business unit code, two physical sites, overlapping labour during ramp-down/ramp-up?

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**
I would not start with “cut 10%.” I would start from cost-to-serve and service promises (lead time, fill rate, split-shipment rate), then look for waste.

Candidate strategies (typical fulfilment levers):
- Network: fewer warehouses where location caps and demand allow; colocation vs extra sites; avoid filling a location to max warehouses if utilisation is poor.
- Inventory: stock the right SKUs in the warehouses that already fulfil a store (this is why the 2-warehouses-per-product-per-store and 3-warehouses-per-store rules matter—they bound complexity).
- Transport: full truckload vs many small drops; store density; delivery windows.
- Labour: batch picking, slotting, reducing exception handling (missing stock vs warehouse capacity mismatches).
- Make vs buy: 3PL for overflow instead of opening a new warehouse at a location that is already at max capacity.
- Process: reduce replacements/emergency transfers caused by poorly sized warehouses.

How I would identify and prioritise:
1. Baseline cost-to-serve by store and by warehouse (from Scenario 1).
2. Segment: high-cost lanes, low-utilisation warehouses, stores with many fulfilling warehouses (complexity tax).
3. Score initiatives on impact, confidence, reversibility, and service risk. A warehouse close is high impact and high risk; slotting is lower impact and safer.
4. Run time-boxed pilots with explicit service SLOs (on-time, fill rate) so “savings” are not just deferred stockouts.
5. Implement in waves: data/visibility first, then operating changes, then network changes.

Expected outcomes if done well: lower cost per unit shipped, fewer split shipments, higher warehouse utilisation without breaching location max capacity, and a clearer view of which stores are expensive to serve. If done badly: service collapse that costs more in lost sales than warehouse savings.

Questions:
- What service metrics are non-negotiable (next-day %, complete orders)?
- Where is the current bottleneck: space, labour, transport, or inventory accuracy?
- Are we over-constrained by the colocation rules (max warehouses per location) vs actual demand?
- What is the cost of a failed delivery vs an extra warehouse?

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**
Without finance integration, the tool becomes a second set of books. Ops will not trust it for decisions, and finance will not use it for close. The value of integration is one version of actuals, faster close, and the ability to explain warehouse/store P&L with operational drivers.

Benefits:
- Actuals vs budget in the same dimensions we use operationally (warehouse business unit, store, period).
- Less manual Excel allocation at month-end.
- Audit trail: who changed an allocation rule, which GL accounts map to labour vs freight.
- Near-real-time *operational* indicators (units, picks) feeding *periodic* financial actuals—not necessarily posting every pick to the GL in real time.

I would challenge “real-time synchronization” as a blanket requirement. GL actuals are batched; freight invoices lag; labour is often weekly. Forcing everything real-time increases cost and reconciliation noise. A better design:
- Real-time or near-real-time for operational events (warehouse created/archived/replaced, fulfilment associations, stock levels).
- Scheduled, reconcilable feeds for financial actuals (daily or on close calendar).
- Explicit reconciliation: sum of allocated cost = source GL accounts within a tolerance.

How I would integrate:
- Master data: warehouse business unit code as the durable key (survives replace); store and product ids mapped to finance cost centres / items.
- Events out of this monolith (store committed, warehouse archived) so finance and the cost tool see confirmed data only after our DB commit—the same lesson as the legacy store gateway.
- Idempotent interfaces; dead-letter and replay; never dual-write without an outbox.
- Reporting from a warehouse of aligned facts (ops events + finance actuals), not by querying the monolith OLTP schema.

Questions:
- Which system is system of record for each cost type?
- Close calendar and materiality—what latency is acceptable for dashboards vs statutory reporting?
- Chart of accounts mapping and who maintains it?
- Do we need event-level freight or monthly accruals?

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**
Fulfillment is operationally leveraged: a small demand miss or a warehouse replacement can swing labour and transport more than store rent. Budgeting is how we reserve capacity (people, space, 3PL) before the peak; forecasting is how we notice we are off-plan early.

Why it matters:
- Hiring and 3PL contracts have lead time.
- Location max warehouses / max capacity are physical constraints—forecasts that ignore them are fiction.
- Warehouse replace creates a dual-run period; budget must include ramp, not just steady state of the new site.

Design considerations:
- Drivers, not just last year + %. Forecast units, lines, cube, and kilometres; convert with rates (labour minutes per line, cost per km). Recalibrate rates from actuals (Scenario 1).
- Scenarios: base / peak / warehouse-offline. Replacement of a warehouse should be a first-class scenario.
- Granularity: warehouse and store monthly is enough to start; weekly for peak. Product-level only where a few SKUs dominate cube or handling.
- Rolling forecast plus a locked annual budget. Variance comments tied to drivers (volume vs rate vs mix).
- Feed from this application: planned warehouses (including replacements), fulfilment topology (which warehouse serves which store/product)—that topology changes cost-to-serve even if volume is flat.

Questions:
- What planning calendar and owner (ops vs finance FP&A)?
- Peak season profile and service constraints?
- Are rates contractual (3PL) or internal?
- How do we budget a warehouse that will be archived mid-year but keep its cost history?

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**
Replacement is not a rename. It is a controlled cutover: same commercial identity (business unit code) for reporting continuity, different physical asset with its own cost curve (fit-out, dual running, transport pattern change, labour learning curve).

Why preserve history:
- Without it, the new site inherits the old site’s run-rate and looks “over budget” on day one, or worse, we lose the baseline and cannot tell if the business case was real.
- Statutory and internal audit need costs attached to the period and facility that incurred them, not only to the live row.
- Trend analysis (cost per unit) must be spliceable: old warehouse until archive date, new warehouse after create date, same BU for management reporting if desired.

How this should work in the model we have:
- Archive sets archivedAt; a new row is created with the same business unit code. Cost facts should key off warehouse *row identity* (id + validity interval), not only BU code.
- Management reports can still roll up by BU code across generations.
- Dual-running costs (two buildings, extra transport, project labour) should be tagged as transition, not loaded into the new site’s run-rate budget.

Keeping the new operation in budget:
- Budget the new warehouse with its own rates and a transition envelope; do not copy last year’s actuals blindly.
- Transfer stock matching (same stock, capacity must fit) is an operational constraint that also has a cost: overtime, extra freight, write-offs if capacity is wrongly sized.
- Freeze a baseline from the archived warehouse’s last N months (per unit and per m²) as the benchmark the new site must beat after hypercare.
- Review fulfilment associations: stores still pointing at an archived warehouse must be re-pointed, or cost-to-serve and service will silently degrade.

Questions:
- Overlap period length and who pays dual rent/labour?
- Capex vs opex treatment of the new site?
- Do finance reports stay on BU code only, or do they need facility-level IDs?
- What is the success metric 90 days after go-live (cost/unit, service, inventory accuracy)?

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.
