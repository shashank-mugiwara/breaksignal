---
name: code-build
description: >-
  The procedure for one code change from brief to pushed, reviewed commit. Use when a builder starts a bead that
  changes code in any repo, when a change is ready for review, or when closing a code bead. Covers recall, the
  three-line plan, the writer checklist, the repo's own failable check, review by a different session, pushing
  before every hand-off, the evidence close and the LEARNINGS.md line. Not for design direction, release gates or
  machine changes.
jstack:
  status: draft
  reviewed_by: not yet reviewed by Shashank or the hub
---

# code-build

One bead, one worktree, one change. The steps are in order; do not skip to writing.

## Before writing

1. **Recall.** `pmem search "<the change or the symptom>" --project <slug> -n 8`. A stored decision is followed
   or its conflict is raised on the bead. It is not re-derived.
2. **Read.** The bead, the files it names, the tests that cover them, and the repo's `AGENTS.md`. Where the repo
   is indexed, find callers through the code graph (`skills/code-context`) instead of guessing with grep.
3. **Plan in three lines** as a bead comment: what changes, what proves it, what is out of scope.
4. **Writer checklist.** Open the checklist the brief carries (`skills/review-ledger/writer-checklist.md`). Each
   rule is a finding a reviewer already raised once. Meet each before round 1.

## Writing

5. Work only in the brief's worktree. Smallest change that meets the goal; no drive-by refactor.
6. A new behaviour gets a test that fails without the change. Run it once without the change to see it fail.
7. Run the brief's `Check`. Quote the command and its output line on the bead (`Checked: ...`).

## Review

8. The reviewer is never the writer's session. Claude-only repos use the `reviewer` role; the free-pair repos
   use the pair's second model. Save each review to the path the brief names and post it on the pull request.
9. Three rounds at most. Findings still open are `BLOCKED: review findings open <card>`.

## Hand-off and close

10. **Push before every hand-off and before the exit line.** Commits on no remote are invisible to every other
    session and machine (hub failure F1, 10-11 Nov 2026: 33 commits on no remote).
11. CI counts on main after the merge, not on the branch tip.
12. Last bead comment: `DONE: <pushed sha> <log or artifact path>` or `BLOCKED: <reason> <card>`. Then tell the hub.
13. If something here cost you a round, add one line to the repo's `LEARNINGS.md` (format in `docs/SCOPE.md`,
    section 6) with the evidence.

## Harness2 parts

- `checks/`: none yet. The repo's own tests are the check for the change; a check for this procedure would
  replay a real bead and compare two runs, which has not been built.
- `canaries/`: none yet. Add a real bead that went wrong (for example, one closed with unpushed commits) as a
  known-bad case when the replay exists.
- `library/`: none yet. Entries come only from a measured change.

### Changelog

- 2026-11-12: created (draft) from `agents/shared/builder-web`, `prompts/house-rules.md` and the harness target
  architecture of 11 Nov 2026 (the planned code-build package).
