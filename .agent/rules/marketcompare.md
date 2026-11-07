---
description: Persistent engineering rules for MarketCompare
alwaysApply: true
---

# MarketCompare rules

- Follow `PROJECT_RULES.md`, `ARCHITECTURE.md`, `QUALITY_GATES.md`, and `BACKLOG.csv`.
- Inspect before editing; preserve user changes and never perform destructive operations without approval.
- Keep purchase records and external API price quotes separate in types, repositories, UI and lifecycle.
- External API quotes must not be persisted in Room, DataStore, files, HTTP cache, logs, analytics or history.
- Do not invent API endpoints, fields, credentials, quotas or terms. Mark API tasks blocked until official documentation is available.
- Use Material 3 theme tokens; no hardcoded colors or user-visible strings in composables.
- Use localized string resources for all visible text.
- No deprecated APIs without a documented reason; no dead code, unused dependencies or avoidable warnings.
- Money must use exact integer/decimal representation, not Double.
- Receipt OCR output is a draft. Preserve raw descriptions and require review for ambiguous values.
- Do not infer discounts without evidence.
- A task is DONE only after acceptance criteria, executed tests and independent review.
- Update dashboard and backlog after every task.
- Ask for human approval at gates and before destructive, paid, credential, signing, publishing or deployment actions.
- Follow the repository's Git policy. Use a task branch, atomic commits, and review the diff before committing.
- Create/push branches and PRs only when tools and permissions actually exist; never claim success without confirmation.
- Never force-push, rewrite shared history, bypass hooks, delete remote branches, or merge protected PRs without explicit authorization.
- Do not commit secrets, personal data, actual receipts, build artifacts, or transient external API prices.
- If remote permissions are missing, record the blocker and provide exact manual steps; never ask for secrets to be pasted into chat.
- Track task state separately from branch, commit, push, PR, review and merge state.

- Treat `docs/` as living project documentation; read its index before relevant work.
- Update relevant docs in the same PR as code changes and keep root README current.
- Keep docs factual and aligned with source, tests, dependencies, schema and actual commands.
- Mark planned/partial/implemented/deprecated accurately.
- Record significant decisions as ADRs; stale or contradictory docs block task completion.
