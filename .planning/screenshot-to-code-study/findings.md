# Findings

## Reference Project
- `D:\Pro\screenshot-to-code` uses a concentrated frontend/backend connection boundary:
  - `frontend/src/config.ts` and WebSocket generation code centralize backend URL usage.
  - `frontend/src/generateCode.ts` wraps WebSocket lifecycle, typed message events, cancellation, and errors.
  - `backend/routes/generate_code.py` uses a pipeline/communicator split so request validation, status broadcast, generation, and error propagation are explicit stages.
- Useful idea for this project: keep cross-client communication details in a small request boundary instead of spreading retry/error/trace behavior through pages.

## Current Project
- Current project architecture:
  - `frontend`: UniApp/Vue 3 mini program.
  - `admin-web`: Vue 3 + Element Plus management website.
  - `backend`: Spring Boot API shared by both clients.
- Management website already sends `X-Trace-Id` from `admin-web/src/api/http.ts`.
- Mini program request wrapper tracks backend `traceId` returned in `ApiResponse`, but does not send a client trace header.
- Backend has `TraceIdFilter`, `TraceIdUtil`, and `ApiResponse.traceId`, but `TraceIdFilter` currently generates a new traceId and ignores inbound `X-Trace-Id`.

## Decision
- Implement a narrow trace propagation improvement:
  - Mini program generates `mp-...` traceId per API request.
  - Backend accepts a sane inbound `X-Trace-Id` and echoes it through `ApiResponse.traceId`.
  - Invalid/missing client traceId still falls back to existing server-generated traceId.
