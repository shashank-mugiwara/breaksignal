---
name: review-ledger
description: "The shared review ledger: every finding a code reviewer raises becomes a numbered rule (R<n>) a writer must meet before round 1, the rules render into writer-checklist.md that goes into every builder-web and code brief, reviewers tag each finding with a rule id or add a new one, and the weekly learnings fold promotes repeats into the owning skill or agent. Use when writing a code brief for any product (paste or inject the checklist), when reviewing a diff (tag findings), when a reviewer finding or a lesson about code quality needs a home, and in the Sunday learnings step. Both hubs (aishwarya-hub and Steve) write the same ledger; it lives in JStack and the hosts link to it."
jstack:
  source: "in-house; seeded 2026-11-08 from Steve's ~/.openclaw/workspace/harness/review-ledger.md (sha256 939362bd...) and writer-checklist.md (sha256 ba793443...), bead ash-wnxz"
  status: live
  reviewed_by: agents-skill-manager
---

# review-ledger

Shashank (8 Nov 2026, relayed by Steve): reviewers found many issues; are we learning from them? The goal is that
first-round reviews find no critical bugs and no missing functionality, that we test the cases that matter and skip
corner cases nobody hits. This skill closes that loop with three files and one tool, all in JStack:

| File | What it is | Who writes it |
|---|---|---|
| `ledger.md` | the ledger: one row per rule, `R<n>`, with the finding that created it and how often it was seen | reviewers and hubs, through `bin/jstack-ledger` |
| `writer-checklist.md` | generated from the ledger; repeats first, then before / while / before-review | `bin/jstack-ledger` only (never by hand) |
| `examples/builder-web-brief.md` | a filled builder-web brief with the checklist injected, the shape every code brief takes | generated with `bin/jstack-ledger brief`, then filled |

`bin/jstack-ledger` lives in the JStack checkout (`~/Documents/GitHub/jstack/bin/jstack-ledger`). The hosts' skill
folders are links into that checkout, so an append from either hub lands in the one ledger; the JStack owner commits
it in the weekly fold (and sooner when asked).

## 1. Writing a code brief (both hubs, any product)

Every brief that asks a session or a model to write code carries the writer checklist. Two ways:

- From a JStack start brief: `bin/jstack-ledger brief agents/shared/builder-web/prompt.md` prints the template with the
  current checklist in place of the `<!-- jstack:writer-checklist -->` marker. Fill the angle brackets and send.
- For any other brief (AtomicSync lead, a one-off worker, a free model): paste `skills/review-ledger/writer-checklist.md`
  under a heading `## Writer checklist`, or write "Meet `~/.claude/skills/review-ledger/writer-checklist.md` before
  asking for review" when the worker can read this machine. A free model gets the pasted text, never a path it cannot
  read; the checklist holds no secrets, user data or product source, so the DATA RULE allows it.

The brief still carries the hub's five house parts and the pre-decision block (ops.md "Brief and card checklist");
the checklist is in addition, not instead.

## 2. Reviewing a diff (the reviewer agent, or any reviewer)

Every finding is tagged with the rule it breaks. The reviewer brief gets the block below in place of
`<!-- jstack:reviewer-ledger -->` (`bin/jstack-ledger brief agents/shared/reviewer/prompt.md`):

- a finding that matches an existing rule: `bin/jstack-ledger tag R<n> --where <bead>` (seen +1, where appended);
- a finding no rule covers: `bin/jstack-ledger add --where <bead> --sev <critical|major|minor> --phase
  <before|write|review> --caught "<what was caught>" --rule "<the rule a writer can meet before round 1>"`, which
  prints the new id;
- write the id next to the finding in the saved review (`R3: no timeout on the webhook call`).

A review with an untagged finding is not finished. Both commands regenerate `writer-checklist.md`; `bin/jstack-ledger
check` confirms the ledger is valid and the checklist current (it runs in JStack's tests and CI).

Writing a rule: it is a pattern the writer can check before asking for review, not a description of one bug. "Every
network call has an explicit timeout and a test of its value" is a rule; "the webhook call had no timeout" is the
finding, which goes in `caught`.

## 3. The weekly fold (the JStack owner, Sunday)

`bin/jstack-learnings` reads three sources: this ledger, the hub's `~/aishwarya-hub/harness/lessons.md` and Steve's
`~/.openclaw/workspace/memory/lessons.md` (plus pmem lessons). Its review-ledger section lists:

- **promote**: rules seen twice or more. Fold each into the skill or agent it belongs to (`docs/LEARNINGS_LOOP.md`
  step 3) and say so in that file's changelog; the rule stays in the ledger with its count.
- **add or tag**: lessons lines from the window that read like reviewer findings and carry no `R<n>` yet. Decide per
  line: `add` (new rule), `tag` (an existing rule, bump it) or leave it (not a code-review lesson).

`bin/jstack-ledger candidates --since <last Sunday>` prints the same two lists on its own. Then lint, tests, commit
with the bead id, push, CI green, and tell both hubs.

## 4. What stays out

- No hooks and no automatic injection: the hub pastes or runs `brief`. If Shashank wants the checklist injected by a
  hook, that is a card, not this skill.
- No legal, security-incident or user-facing text: the ledger is about code and process findings.
- The ledger names beads, files and rules, never secrets, health or user data; a finding about a secret says "the
  key" and where, not the value.

## On OpenClaw (Steve's host)

The skill folder is linked at `~/.openclaw/workspace/skills/review-ledger`; the tool is
`~/Documents/GitHub/jstack/bin/jstack-ledger`. Steve's seed files (`harness/review-ledger.md`, `harness/writer-checklist.md`)
stay as he left them; new findings go here, not there, so both hubs read one ledger.

### Changelog

- 2026-11-08: created (hx-q44h.8, hub bead ash-wnxz) from Steve's 14 findings R1 to R14 and his writer checklist; added
  the phase column, the standing items P1 to P4, `bin/jstack-ledger` (list, add, tag, checklist, check, brief,
  candidates), the reviewer block, and the three-source fold in `bin/jstack-learnings`.
