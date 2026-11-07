---
name: marketcompare-audit
description: Independently audit a MarketCompare change for architecture, correctness, UI quality, privacy and maintainability.
---

# Senior audit checklist

Inspect the actual diff and relevant files, not just the implementer's summary.

## Correctness
- Is business logic in domain/use cases rather than composables?
- Are money calculations exact?
- Are discounts evidenced and correctly represented?
- Are product variants distinguished by brand, type, size, unit and presentation?
- Are OCR ambiguities surfaced for review?

## Privacy and source provenance
- Can any external API price reach Room, DataStore, files, HTTP cache, logs, analytics or purchase history?
- Are external quote objects temporary and cleared on leaving the screen?
- Are purchase prices and reference quotes clearly distinguished?
- Are credentials protected from source control and logs?

## UI
- Are strings localized through resources?
- Are colors and typography derived from MaterialTheme?
- Are loading, empty, error, cancellation and offline states handled?
- Is accessibility and text scaling considered?

## Maintainability
- Any deprecated API, dead code, unused imports/dependencies, duplicated logic, god class, unsafe global mutable state, unnecessary abstraction, or avoidable warnings?
- Are errors handled explicitly?
- Are tests meaningful and executed?

## Output
Return:
- `PASS`, `PASS WITH EXCEPTIONS`, or `FAIL`.
- Findings with severity P0–P3, file/line, consequence and smallest recommended fix.
- Commands executed and exact outcomes.
- Missing evidence and tests not run.
Do not mark PASS if any P0/P1 issue remains.
