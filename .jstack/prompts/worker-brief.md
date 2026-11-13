# Worker brief (the form a hub fills to start any worker)

A hub starts a worker by filling this form and pasting it as the worker's first message. One line per field,
`Field: value`. No field is left blank and no angle bracket is left in place: `bin/jstack-brief check <file>`
refuses the brief otherwise. Each role adds its own fields (`brief_fields` in its `AGENT.md`); the role's
`prompt.md` is this form with those fields added.

Why a form: on 2026-11-12 the hub had run 74 workers under 11 role names and 9 of those names had no definition
file, so every brief was written fresh (Google Doc "Agents: who runs, when, and how they are orchestrated",
12 Nov 2026). A fixed form makes the brief checkable before a model is spent on it.

## The form

```
Role: <role folder name, for example builder; company/role when two companies share a name>
Hub: <the session that starts the worker and reads its result>
Bead: <bead id; the bead is the state, not this chat>
Repo: <repo name as in repos/<repo>.md, or none for work outside a repo>
Goal: <one paragraph: what, why, and what done looks like>
Read: <paths, beads and docs the worker opens first; paths, never pasted secrets>
Check: <the command that can fail, and the output line that means pass>
Model: <model, taken from the repo manifest's models line or the hub's routing ladder>
Effort: <low, medium or high>
Route: <model, effort and because: one line saying why this rung and not a cheaper one>
Decision: <cheapest durable option and one rejected; what breaks in 7 days if wrong; governing card>
Report: <time of the interim report, a clock time with zone>
Never: <the role's never line, plus anything this task must not touch>
Exit: last bead comment is DONE: <sha> <path> or BLOCKED: <reason> <card>; then message the hub
```

Roles that judge a tool, model, method or idea (`researcher`, `architect`) add one more field:

```
Trial: <"fair trial: build it to succeed first" and what is trialled, or "none">
```

## Rules for the hub

1. `Check` is a command, not a description. A brief whose done-state cannot fail is not ready to send.
2. `Model` comes from `repos/<repo>.md`. The worker never picks its own model. `Route` says why that rung.
   `Decision` is the pre-decision block: the cheapest durable option and one that was rejected, what breaks in
   7 days if the choice is wrong, and the card that governs it. `Report` is when the worker posts an interim
   comment on the bead, so a silent worker is noticed (a bead labelled `silent-after-45` is chased at 45 minutes).
   `Trial` is `fair trial: build it to succeed first` when the work trials a tool, model, method or idea
   (read the maker's guide, try at least 3 constructions, report a negative as "my construction X did not work"),
   else `none`.
3. `Read` names files the worker can open on its machine. Content that must not leave the machine is never
   pasted into a brief for a free or Flash model (`prompts/house-rules.md`, section 3).
4. One bead per brief. A second task is a second bead and a second worker.
5. Save the filled brief beside the bead's evidence before sending it, so a replacement worker can start from
   the same text.

## Rules for the worker

1. Read the role's `AGENT.md` and `prompts/house-rules.md` first, then recall (`pmem search`), then the `Read` list.
2. If a field is wrong or cannot be met, stop and write `BLOCKED: brief <field> <why>` on the bead. Do not guess.
3. The last bead comment is exactly the `Exit` line's form. Silence is not an end state.
4. A lesson worth keeping goes in the repo's `LEARNINGS.md` in the format in `docs/SCOPE.md`, with its evidence.
