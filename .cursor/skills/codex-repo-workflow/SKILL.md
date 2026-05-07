---
name: codex-repo-workflow
description: >-
  Apply a Codex-style repository workflow for code changes: inspect before editing,
  preserve user work, make scoped patches, follow existing patterns, run focused
  verification, and report concrete outcomes. Use when implementing features,
  fixing bugs, refactoring, updating tests, or modifying project files.
disable-model-invocation: false
---

# Codex Repository Workflow

## Procedure

1. Inspect the relevant files, docs, tests, and scripts before editing.
2. Identify the smallest change that satisfies the request.
3. Match local conventions for naming, module boundaries, error handling, imports, formatting, and tests.
4. Preserve unrelated user changes. Do not revert or reformat files outside the task.
5. Edit only the files needed for the behavior change.
6. Run the narrowest meaningful verification first; expand only when the touched surface justifies it.
7. Report changed behavior, verification, and remaining risk.

## Discovery

- Use `rg` / `rg --files` for searches.
- Read package/build/test scripts before inventing commands.
- For API work, check `docs/api/openapi/openapi.yaml`, `docs/api/00-接口通则.md`, and matching frontend/backend callers.
- For project-domain work, prefer existing `.cursor/skills/xingqiu-*` skills over general assumptions.

## Editing Constraints

- Keep diffs surgical.
- Avoid speculative configuration, unused abstraction, and broad cleanup.
- Remove only dead code introduced by the current change unless the user asks for cleanup.
- Add comments sparingly and only for non-obvious logic.

## Verification

Choose verification based on the change:

- Unit or integration tests for logic, services, API behavior, and bug fixes.
- Type check or build for frontend/backend compile safety.
- Lint/format only when the project already uses it or the touched files require it.
- Manual smoke checks for UI or flows that cannot be covered quickly by tests.

If verification cannot be completed, state the exact reason and what is unverified.
