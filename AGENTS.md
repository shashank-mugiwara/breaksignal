# AGENTS.md

<!-- jstack:begin -->
## From JStack (managed block: edit in JStack, not here)

- This repo's skills, agents and MCP servers are copies from JStack, listed in `.jstack/manifest.json`.
  They are read-only here. A repo-specific variant goes in JStack under that skill's or agent's
  `custom/breaksignal/` folder; `bin/jstack-repo apply breaksignal <path>` brings it back.
- Models allowed in this repo: Code is written by the opencode free pair (opencode/deepseek-v4.1-flash-free, opencode/glm-5.3-flash-free) or by Antigravity (agy) and reviewed by a different model family; at most 3 write-review rounds; tests decide; no deploy or merge by a non-Claude model; Claude sessions dispatch, gate and merge (Shashank, 12 Nov 2026, card ap-90be0 correction 1).
- Skills: `code-build`, `review-ledger`, `code-context`, `growth-copy`.
- Roles a hub may start here (definitions in `.jstack/agents/`; house rules and the brief form in `.jstack/prompts/`): `builder`, `reviewer`.
- MCP servers: `codebase-memory-mcp`, `context7`.
- A lesson from work here goes in `LEARNINGS.md`, one dated line with its evidence.
<!-- jstack:end -->
