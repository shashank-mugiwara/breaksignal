---
name: code-context
description: "Explore a codebase cheaply with the codebase-memory-mcp graph (search_graph, search_code, get_architecture, trace_path) before reading files, or pack a non-AtomicSync repo into one file for a Gemini web review with code2prompt through the gemini-pack wrapper, which enforces the data rule (allow-list, no AtomicSync or CentralProject, no secrets, packs only in a private folder). Use before reading many files in any repo, and when an outside second review of BreakSignal, AgentManage or another non-AtomicSync repo is wanted."
jstack:
  source: "in-house (Steve, branch steve/code-context-skill at 6968692, bead hx-3t1z); reviewed and hardened by agents-skill-manager 2026-11-08"
  status: live
  reviewed_by: agents-skill-manager
  pinned_tools: "code2prompt 4.3.0 (brew; the wrapper refuses any other version), codebase-memory-mcp 0.8.1 (~/.local/bin, local, no network)"
---

# code-context

Two tools, two jobs. Graph first for exploring; a pack only for an outside reviewer, and only for repos the data
rule allows.

## 1. Explore a repo with codebase-memory-mcp (saves tokens)

Installed: `~/.local/bin/codebase-memory-mcp` 0.8.1 (local, no network; indexes in `~/.cache/codebase-memory-mcp`).
Claude Code sessions already have it as an MCP server (`~/.claude.json`, auto_index on); the same tools exist as a CLI
for other agents: `codebase-memory-mcp cli <tool> '<json>'`.

1. Check the repo is indexed: `codebase-memory-mcp cli list_projects` (project name = path with `/` -> `-`).
   Not there, or stale after big changes: `codebase-memory-mcp cli index_repository '{"repo_path":"/abs/path"}'`.
   Index the repo root, never `~/Documents` or `~/Desktop` (huge, useless indexes).
2. Ask the graph before opening files, because one query replaces a dozen greps and reads:
   - shape: `get_architecture '{"project":P,"aspects":["languages","entry_points"]}'`
   - find symbols: `search_graph '{"project":P,"name_pattern":"(?i).*reader.*","label":"Function"}'`
   - text: `search_code '{"project":P,"pattern":"pg-boss","limit":5}'`
   - who calls what: `trace_path '{"project":P,"function_name":"...","mode":"calls"}'`; exact source:
     `get_code_snippet`; what a diff touches: `detect_changes`.
3. Open only the files the graph points to.

Check it worked: the query returns results with `file` and `start_line`; an empty result says so with a hint.

## 2. Pack a repo for Gemini web review (code2prompt)

Installed: `code2prompt` 4.3.0 (brew). Wrapper: `scripts/gemini-pack` in this skill (run it from the host link, for
example `~/.claude/skills/code-context/scripts/gemini-pack`; do not keep copies elsewhere, they drift).

1. Full pack: `gemini-pack ~/Documents/GitHub/brace`.
   PR pack: `gemini-pack REPO --diff main feature-branch`.
   Too big (it prints the token count): add `--exclude 'public/assets/**,docs/**'` or `--include 'src/**'`.
   Keep packs under about 200K tokens so Gemini reads all of it.
2. In Gemini web (Pro), attach the pack and paste `references/gemini-review.md` as the instruction.
3. Treat Gemini's findings as claims: check each one in the code before acting, then log real ones with
   `bin/jstack-ledger add` or `tag` (review-ledger skill) so the writer checklist learns.
4. Delete the pack after the upload: `gemini-pack --clean`. Packs are not kept.

### The data rule, as the wrapper enforces it (ops.md Tool routing, card ap-4e2de)

In this order, each a refusal before anything is written:

1. The repo path is resolved through symlinks and `..` (`pwd -P`) and must be under `~/Documents/GitHub` or
   `~/worktrees`. A symlink in an allowed folder that points elsewhere is refused.
2. A resolved path containing `atomicsync` or `centralproject` (any case) is refused: AtomicSync code is reviewed by
   Claude sessions only.
3. A symlink inside the repo that resolves outside the repo is refused and named (code2prompt never follows symlinks
   here, the wrapper does not pass `-L`; this is the second lock).
4. The pack is written only to `~/.cache/gemini-pack/` (mode 700) or a `--out` outside every repo, `~/Documents`,
   `~/Desktop`, `~/Downloads`, `~/worktrees`, iCloud, `~/Library/CloudStorage`, `/Volumes` and `/tmp`.
5. `code2prompt --version` must equal the pinned 4.3.0; a new release is reviewed, then the pin moves (here and in the
   script).
6. Excluded by name before packing: `.env*`, keys and certificates (`.pem .key .p12 .p8 .pfx .jks .keystore
   .mobileprovision`), `id_rsa*`, `id_ed25519*`, terraform state, `.netrc .npmrc .pypirc`, `credentials*`, `*secret*`,
   service-account and Google config files, lockfiles, binaries and media. `.gitignore` is honoured.
7. After packing, any credential-looking string (AWS, Anthropic, OpenAI, GitHub, Slack, Google keys, private keys,
   JWTs, `KEY/SECRET/PASSWORD/TOKEN = "..."` assignments) deletes the pack; exit 4 says which to exclude.
8. Any failure removes a partial pack (trap).

Tests: `tests/test_code_context.py` (red-first, 2026-11-08): outside path, AtomicSync names under the allow-list,
symlinked repo folder, `../` escape, AtomicSync worktree, symlink inside the repo pointing outside, `--out` in a
shared folder, secret file names, credential in source deletes the pack, `.env` never in a pack, version pin,
`--clean`. The refusal tests run without code2prompt (CI); the packing tests run where it is installed.

## What this skill does not do

No hooks, no automatic indexing beyond what `~/.claude.json` already does, no network. Gemini sees only what the
wrapper lets through; memory text, user data and secrets never go in a brief or a pack (DATA RULE in
`prompts/house-rules.md`). Deploys, releases and merges still follow the beads and review rules.

## On OpenClaw (Steve's host)

The skill is linked at `~/.openclaw/workspace/skills/code-context`; call the wrapper from there. Steve's earlier copies
(`~/.openclaw/workspace/bin/gemini-pack`, `~/.openclaw/workspace/prompts/gemini-review.md`) should become links to the
skill files, or be removed, so one reviewed version runs.

### Changelog

- 2026-11-08: Steve's skill (6968692) reviewed and merged (hx-3t1z). Wrapper hardened: HOME resolved before the
  allow-list check (a temp or symlinked home broke it), symlink-escape scan inside the repo, pack folder restricted to
  `~/.cache/gemini-pack` (700) with shared and synced folders refused, code2prompt version pinned to 4.3.0, wider
  secret-file exclusions and credential patterns (Anthropic keys, JWTs, assigned secrets), partial packs removed on
  failure, `--clean`. SKILL.md: `trace_call_path` corrected to `trace_path`, instruction path points at this skill's
  `references/gemini-review.md`, versions pinned in frontmatter. Tests added (15).
