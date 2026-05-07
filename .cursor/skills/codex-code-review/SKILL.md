---
name: codex-code-review
description: >-
  Perform a Codex-style code review focused on bugs, regressions, security,
  contract mismatches, data consistency, missing tests, and production risk.
  Use when the user asks to review code, inspect a diff, check a PR, audit a
  change, or find issues before delivery.
disable-model-invocation: false
---

# Codex Code Review

## Review Stance

Prioritize findings. Do not lead with a broad summary unless there are no issues.

## Process

1. Inspect the diff and the surrounding code needed to understand behavior.
2. Check project contracts, docs, tests, migrations, and callers touched by the change.
3. Look for concrete failure modes, not style preferences.
4. Order findings by severity.
5. Keep each finding actionable and tied to a specific file and line.

## Findings To Prefer

- Runtime bugs, broken flows, or incorrect state transitions.
- Security issues: auth bypass, missing authorization, injection, XSS, secret exposure, unsafe file handling.
- API contract mismatches: paths, fields, enums, error shapes, pagination, or status semantics.
- Money/payment/order issues: integer cents, idempotency, audit trail, duplicate processing.
- Cache and data consistency bugs.
- Missing tests where the changed behavior is risky or likely to regress.

## Output Shape

- Findings first, highest severity first.
- Then open questions or assumptions.
- Then a brief summary only if useful.
- If no findings, say that clearly and mention any checks not run or residual risk.
