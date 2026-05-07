---
name: codex-frontend-implementation
description: >-
  Build or modify frontend and miniprogram UI with Codex-style product-quality
  standards: usable first screen, responsive layout, stable controls, accessible
  icon usage, visual verification, and consistency with existing design patterns.
  Use when editing pages, components, styles, dashboards, tools, games, or visual
  UI behavior.
disable-model-invocation: false
---

# Codex Frontend Implementation

## Workflow

1. Inspect existing UI patterns, components, assets, style tokens, and routing.
2. Build the actual requested experience, not a placeholder or marketing wrapper.
3. Use controls users expect for the task: icons for tools, toggles for binary settings, sliders/inputs for numbers, menus for option sets, tabs for views.
4. Keep layouts responsive with stable dimensions and constraints.
5. Verify desktop and mobile widths for overflow, overlap, clipping, blank states, and broken assets.

## Design Rules

- Match the app's domain and existing visual language.
- Avoid cards inside cards and decorative page-section cards.
- Avoid emoji as functional icons. Use SVG or the project's icon approach.
- Avoid one-note color palettes and decorative gradient blobs/orbs.
- Keep text legible and inside its container. Do not use viewport-width font scaling.
- Add visible states for loading, empty, error, disabled, selected, and active interactions when relevant.

## Miniprogram Notes

- Keep network calls centralized in `services/` or the local project equivalent.
- Keep pages focused on presentation and user events.
- Ensure new pages are registered in the relevant app or package config.
- Align field names, enum values, and errors with backend contracts.

## Verification

- Run the project build/check when available.
- For visual changes, inspect or screenshot the affected screens across realistic viewport sizes.
- Confirm primary interactions work and no important control is hidden or clipped.
