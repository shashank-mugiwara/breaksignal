# House rules (every shared agent carries these)

Source: `~/aishwarya-hub/notes/ops.md` ("Tool routing", card ap-4e2de; "Silence, waits, exact reminders,
evidence close", card ap-9fd31) and `~/aishwarya-hub/harness/lessons.md`. When they change there, change here
in the same step and add a changelog line (`docs/LEARNINGS_LOOP.md`).

## 1. Evidence lines

- A result is reported only with the check that produced it: `Checked: \`<exact command>\` printed "<output
  line>"`. No "looks right", no relayed claim without the line that proves it (lessons L1, L2, L17).
- Any time written anywhere comes from `date`, run in the same batch, never estimated.
- A stage is done when a check that can fail has passed. If a stage has no such check, say so and mark the
  output unverified.
- Before reporting a blocker, re-read the live state (files, beads, the hub's latest message).

## 2. The evidence close

- Work lives in Beads. Claim before working; comment at every hand-off.
- The last comment on a bead is exactly `DONE: <sha> <log or artifact path>` or `BLOCKED: <reason> <card id>`.
  Upper case; prose "Done:" is not an exit line.
- Close with `~/Scripts/bd-close <id> --reason '...'`. The reason holds an artifact path, the exact command in
  backticks, an output line (`Output: "21 tests OK"`, `exit 0`, `GATE: PASS`) and a commit sha or URL.
- CI counts on main after the merge, not on the branch tip (lesson 2026-10-23, AgentManage).
- Silence is not an end state: finished, blocked or stopped, tell the hub that started you.

## 3. No secrets to free models (the DATA RULE)

Nothing that leaves the machine to a free or Flash model (opencode `*-free`, DeepSeek Flash on OpenRouter,
Gemini, agy) may contain: secrets or keys, health data, user data (ids, emails, accounts), AtomicSync product
source, memory text, or Shashank-private material (`~/Documents/shashankj`). Brief the tool with paths it can
read on this machine instead of pasting content. One exception: BreakSignal website and AgentManage platform
source may go to the opencode free pair. Every free or Flash output gets a failable check before use (three
facts spot-checked against the source, or the repo's tests and build).

## 4. Recall before acting

`pmem search "<decision or symptom>" --project <slug> -n 8` before any non-trivial step; do not re-derive a
stored decision; surface a conflict instead of overriding it. Store each new decision as one fact.

## 5. Scope

AtomicSync product code, and anything touching secrets, health data, money or user data: Claude only, never a
free model, never agy as first reviewer. BreakSignal and AgentManage code: written by the opencode free pair or
Antigravity (agy) and reviewed by a different model family; at most 3 write-review rounds; tests decide; no deploy
or merge by a non-Claude model; Claude sessions dispatch, gate and merge (Shashank, 12 Nov 2026, card ap-90be0;
replaces the 4 Nov wording). Hooks, settings.json and system prompts
change only on a card from Shashank. Nothing is published outside private GitHub repos without a card.
