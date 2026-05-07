---
name: codex-skill-authoring
description: >-
  Create or update Cursor/Codex-style skills and rule files: concise frontmatter,
  clear trigger descriptions, scoped procedural instructions, optional resources,
  and validation. Use when the user asks to add skills, update skills, port rules,
  author agent instructions, or organize reusable AI workflows.
disable-model-invocation: false
---

# Codex Skill Authoring

## Skill Design

- Keep skills concise. Include only context an agent needs to perform the task better.
- Put trigger conditions in the `description` frontmatter because that is what determines when the skill is selected.
- Use imperative procedural instructions in the body.
- Prefer one focused skill over a large mixed-purpose skill.
- Do not add auxiliary files such as README, changelog, or install guides unless they are directly used by the skill.

## Cursor Skill Format

Use a folder with `SKILL.md`:

```yaml
---
name: skill-name
description: >-
  What the skill does and when to use it.
disable-model-invocation: false
---
```

The folder name and `name` should use lowercase hyphen-case.

## Cursor Rule Format

Use `.cursor/rules/*.mdc`:

```yaml
---
description: Short rule description.
alwaysApply: true
---
```

Set `alwaysApply: true` only for broad project rules. Use `false` for domain-specific rules.

## Update Process

1. Inspect existing rules and skills to avoid duplication.
2. Decide whether the behavior belongs in a global rule or a triggered skill.
3. Add the smallest useful instruction set.
4. Preserve existing project-specific constraints.
5. Validate YAML frontmatter and file paths.
