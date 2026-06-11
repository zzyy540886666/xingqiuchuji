# Requirements Audit 2026-06-10

## Goal
Read `docs/星球出机小程序需求文档 V1.docx`, compare required functionality with the current repository implementation, identify missing or incomplete features, then coordinate implementation-ready work through a parent/sub-agent split.

## Assumptions
- The docx file is the requirement source for this audit.
- "实现真实程度不完整" means code exists but appears mocked, stubbed, static-only, UI-only, not wired to backend, missing persistence, missing validation, or not aligned with documented behavior.
- Code implementation tasks can be distributed only after the audit has concrete, bounded items with disjoint write scopes.

## Phases
- [complete] Phase 1: Read project instructions, scripts, API docs, and requirement document.
- [complete] Phase 2: Map required features to frontend, backend, admin, API docs, database, and tests.
- [complete] Phase 3: Classify features as implemented, incomplete, missing, or blocked/high-risk.
- [complete] Phase 4: Create a parent coordination prompt and spawn sub-agents for implementable, disjoint tasks.
- [complete] Phase 5: Collect sub-agent status and report audit findings, distributed tasks, verification state, and risks.

## Affected Areas To Inspect
- `docs/`
- `frontend/`
- `backend/`
- `admin-web/`
- `tests/`
- root scripts and harness tooling

## Validation Plan
- Audit-only phase: verify findings by file/path references and direct code/doc inspection.
- If sub-agents implement code, require each worker to run the narrowest relevant checks and report commands/results.

## Errors Encountered
| Error | Attempt | Resolution |
| --- | --- | --- |
| Python `zipfile` failed to open the docx because the Chinese path was converted to question marks under the sandboxed shell. | Extract docx by absolute Unicode path with Python. | Switch to PowerShell/.NET zip APIs, which preserve the Unicode path in this environment. |
| `rg --files` failed with access denied in the sandbox. | List frontend/backend/admin source files with ripgrep. | Use PowerShell `Get-ChildItem` and `Select-String` as fallback. |
| Worker A failed with 429 Too Many Requests. | Spawned frontend order worker. | Marked as failed/no code output; task remains in backlog. |
| Worker C failed with 429 Too Many Requests. | Spawned analytics worker. | Marked as failed/no code output; task remains in backlog. |
| Narrow status/read check timed out when run in parallel. | `git status --short` plus reading generated docs. | Retry narrower commands separately with longer timeout. |
| `npm run type-check` failed because PowerShell blocks `npm.ps1` execution. | Run root frontend static check via npm. | Retry with `npm.cmd run type-check`. |
| Worker A reported 429 although workspace contained task changes. | Trust notification only. | Reviewed actual diff, fixed remaining `pay-result.vue` stale status locally, and verified with static check. |
