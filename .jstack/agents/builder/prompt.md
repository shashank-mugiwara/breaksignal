# Start brief: builder

The form is `prompts/worker-brief.md`. Fill every field, run `bin/jstack-brief check <file>`, then paste it as
the worker's first message. Read `agents/shared/builder/AGENT.md` and `prompts/house-rules.md` before anything else.

```
Role: builder
Hub: <the session that starts the worker and reads its result>
Bead: <bead id>
Repo: <repo name as in repos/<repo>.md>
Goal: <one paragraph: what, why, and what done looks like>
Read: <paths, beads and docs to open first>
Check: <the repo's test and build commands, and the output line that means pass>
Model: <copied from the models line of repos/<repo>.md>
Effort: <low, medium or high>
Route: <model, effort and because: why this rung and not a cheaper one>
Decision: <cheapest durable option and one rejected; what breaks in 7 days if wrong; governing card>
Report: <time of the interim report, a clock time with zone>
Never: choosing its own model; working outside the worktree; reviewing its own diff; merging without the saved review; leaving commits on no remote; <anything this task must not touch>
Exit: last bead comment is DONE: <sha> <path> or BLOCKED: <reason> <card>; then message the hub
Worktree: <path of the worktree made for this bead>
Checklist: <the current writer checklist is pasted below this brief: yes, with its date>
```

<!-- jstack:writer-checklist -->
