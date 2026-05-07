---
name: codex-openai-docs
description: >-
  Answer or implement tasks involving OpenAI products, APIs, models, SDKs,
  Codex, ChatGPT, Responses API, Assistants, tools, image generation, or model
  upgrades using current official OpenAI documentation. Use when the user asks
  how to use OpenAI products, compare OpenAI capabilities, or update OpenAI API
  code.
disable-model-invocation: false
---

# Codex OpenAI Docs

## Source Policy

- Prefer official OpenAI documentation and official OpenAI repositories.
- For current model names, pricing, product behavior, API parameters, and SDK usage, verify against current official sources instead of relying on memory.
- If browsing is available, restrict fallback browsing to official OpenAI domains unless the user requests otherwise.

## Workflow

1. Identify the exact OpenAI product or API surface involved.
2. Check current official docs for the relevant endpoint, model, SDK, and tool behavior.
3. Implement or answer using the documented current behavior.
4. Cite official links in user-facing answers when giving guidance.
5. For code changes, prefer the project's existing SDK/version patterns unless they conflict with current docs.

## Implementation Notes

- Keep prompts and model selection explicit.
- Avoid deprecated endpoints or model names when a current replacement exists.
- For migrations, state behavior changes and update tests or examples that depend on old output shapes.
- For Codex CLI/Desktop usage, distinguish terminal workflows from desktop app workflows.
