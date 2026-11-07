---
name: marketcompare-orchestrator
description: Plan and coordinate implementation of MarketCompare through small validated tasks, maintaining backlog and dashboard.
---

# MarketCompare Orchestrator

1. Read `MASTER_PROMPT.md`, `PROJECT_RULES.md`, `ARCHITECTURE.md`, `QUALITY_GATES.md`, `AGENT_TEAM.md`, `BACKLOG.csv`, and `DASHBOARD.md`.
2. Inspect repository status and toolchain. Do not overwrite existing work.
3. Select the next unblocked task whose dependencies are DONE.
4. Assign one owner agent and a narrow file scope. Avoid parallel edits to shared files.
5. Require a plan and acceptance criteria before implementation.
6. Require test output and a concise diff summary after implementation.
7. Invoke the independent audit skill for P0/P1 tasks and milestone gates.
8. Update backlog/dashboard with evidence, not assumptions.
9. Stop and request approval at the human gates listed in `QUALITY_GATES.md`.
10. Do not claim completion while required tests have not run.
