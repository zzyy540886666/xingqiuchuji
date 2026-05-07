---
name: codex-content-artifacts
description: >-
  Create, edit, analyze, or verify content artifacts with Codex-style workflows:
  documents, spreadsheets, presentations, PDFs, images, and visual assets. Use
  when working with .docx, .xlsx, .csv, .pptx, PDFs, generated images, edited
  images, charts, tables, or rendered deliverables.
disable-model-invocation: false
---

# Codex Content Artifacts

## General Workflow

1. Identify the artifact type and the expected final deliverable.
2. Use a structured library or dedicated tool for the file format when available.
3. Preserve existing formatting and content unless the user asks to rewrite it.
4. Render or otherwise inspect generated artifacts before delivery.
5. Iterate until layout, data, and output are correct.

## Documents

- For `.docx`, preserve styles, numbering, comments, and tracked changes when relevant.
- Render pages for visual QA when layout matters.
- Avoid manual XML edits unless the format-specific tooling cannot perform the task safely.

## Spreadsheets

- For `.xlsx`, `.xls`, `.csv`, and `.tsv`, use structured parsers.
- Preserve formulas, number formats, tables, charts, filters, and sheet names where relevant.
- Recalculate or verify formulas when the workflow supports it.

## Presentations

- For `.pptx`, keep slide layout, theme, and visual hierarchy consistent.
- Render slides for visual QA before delivery.
- Check for clipped text, broken images, and inconsistent alignment.

## Images

- Use bitmap image generation or editing when the user asks for a new raster image, photo-like visual, illustration, texture, sprite, mockup, or image transformation.
- Prefer code-native SVG/CSS/canvas when the asset belongs to an existing vector/icon system.
- Show or inspect generated visual output when practical.
