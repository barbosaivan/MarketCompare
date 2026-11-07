---
name: marketcompare-release-gate
description: Verify whether a MarketCompare milestone meets its acceptance criteria and is ready for human approval.
---

# Release gate procedure

1. Identify the gate and list its exact acceptance criteria.
2. Inspect backlog states and confirm dependencies are DONE.
3. Run available build, unit tests, lint/static analysis and relevant integration tests.
4. Inspect diff and confirm no unrelated changes or secrets.
5. Confirm dashboard includes command output, known failures, blocked tests and evidence.
6. Require the independent senior audit for the gate.
7. Return one result:
   - `READY FOR HUMAN APPROVAL`
   - `BLOCKED`
   - `REJECTED`
8. Never publish, sign or deploy. Those actions require explicit user authorization.
