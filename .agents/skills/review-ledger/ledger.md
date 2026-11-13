# Review ledger: what reviewers caught, and the rule each finding becomes

Every finding a reviewer raises is logged here once, as a **rule the writer must meet before round 1**. Both hubs
append to this file (it is the same file on every host: the hosts link to the JStack checkout). Writers get
`writer-checklist.md`, generated from this file, in every code brief. Goal: round-1 reviews find no critical bugs
and no missing functionality. Repeats are counted; a rule caught twice moves to the top of the checklist and the
weekly fold (`docs/LEARNINGS_LOOP.md`) carries it into the skill or agent it belongs to.

Write rows with the tool, not by hand: `bin/jstack-ledger add ...` for a new rule, `bin/jstack-ledger tag R<n> --where
<bead>` when a finding matches an existing rule. Both regenerate `writer-checklist.md`. `bin/jstack-ledger check`
validates this file. Columns: `id | date | where | sev (critical/major/minor) | phase (before/write/review) | caught |
rule | seen`. A tag appends to `date` and `where` with `; `.

Seeded 2026-11-08 from Steve's `~/.openclaw/workspace/harness/review-ledger.md` (R1 to R14, sha256
939362bdd1d96c84c1ba7a76ee44d24c92c347909bf130d495824150dfd8bb29) and `writer-checklist.md` (sha256
ba793443297a41a5dbb5dc2b2d6352e0d2f7678173002e870cef78eb9927afff); the phase column and the standing items are
JStack's additions.

## Rules

| id | date | where | sev | phase | caught | rule | seen |
|---|---|---|---|---|---|---|---|
| R1 | 2026-11-07 | AgentManage am-z1g | critical | write | Runtime data files read via a source-tree path, missing from the installed wheel/image | Any data file read at runtime ships inside the package (package-data / force-include) and is loaded via importlib.resources; test it from a built artifact, not the source tree | 1 |
| R2 | 2026-11-07 | AgentManage am-z1g (CI) | critical | write | Build stage did not copy a file the package now needs (Docker COPY + .dockerignore) | When packaging changes, update every build manifest (Dockerfile COPY, .dockerignore, CI) in the same change; run the image build locally or in CI before review | 1 |
| R3 | 2026-11-08 | AgentManage am-0ox | major | write | No timeout on outbound HTTP | Every network call has an explicit timeout and a test that the timeout is the intended value | 1 |
| R4 | 2026-11-08 | AgentManage am-0ox | major | write | Scheduled job could overlap itself and create duplicates | Every scheduled job is non-overlapping (singleton key or in-flight guard), and writes are idempotent (unique key or upsert) | 1 |
| R5 | 2026-11-08 | AgentManage am-0ox | major | write | Trusted the remote server's filter (role=ops) instead of checking | Never trust a remote filter for correctness: re-check the condition on the parsed data | 1 |
| R6 | 2026-11-08 | AgentManage am-0ox | major | write | New label value not allowed by the schema enum | When code writes a new enum/label value, update the schema and add a test that the exact value validates | 1 |
| R7 | 2026-11-08 | AgentManage am-0ox | major | write | Mirror matched any row with an id field, could overwrite manual data | Ownership of rows is scoped by two markers (id + source label), never one guessable field | 1 |
| R8 | 2026-11-08 | AgentManage am-0ox | major | write | Unknown enum from a remote system would fail every pass | Normalise external enums to a safe default; one bad record must not fail the whole batch | 1 |
| R9 | 2026-11-08 | AgentManage am-0ox | minor | write | Opaque parse error, no context | Errors name the operation, path and status; never the secret | 1 |
| R10 | 2026-11-08 | AgentManage am-0ox | minor | write | Reopen left stale close_reason | State transitions reset every field the previous state set | 1 |
| R11 | 2026-11-08 | AgentManage am-0ox | minor | write | Unbounded external strings | Cap or truncate external text before storing | 1 |
| R12 | 2026-11-08 | Process (am-0ox merge) | critical | review | Review not saved, reviewer never saw the final diff, reviewer sent a partial diff (missed tests) | Reviewer gets the full final diff INCLUDING tests; review saved to a file and posted on the PR; re-review after the last change | 1 |
| R13 | 2026-11-08 | BreakSignal site r1 (product) | critical | review | Generic, text-only design; Shashank: "not professional" | Public pages use the taste skill run (gate + copy-lint), show the real product, one motion moment; screenshot at 1440 and 390 before asking anyone | 1 |
| R14 | 2026-11-08 | Card ap-a2780 | major | review | Card named an access path the merged code didn't use | Cards and plans are written from the merged code: quote the exact call/path the code uses | 1 |
| R15 | 2026-11-08; 2026-11-08 | hub 2026-11-08, 2 sessions (haiku-high 12:2x, atomicsync-opus-max 13:3x); second session the same day (hub count 2) | major | write | Claude Code permission prompt (rm on possibly-empty variable path); unattended sessions stall on it | Never rm a path built from variables in agent shell commands; use "${VAR:?}"/name, a literal path, or a mktemp dir removed by trap; no bash -c wrapper around rm | 2 |
| R16 | 2026-11-10 | hx-tubl | major | before | Template tests failed 81/81 on Node 26 (built-in localStorage hides jsdom's); the repo pins Node 22 but nothing enforced it | Run tests only on the pinned runtime from .nvmrc and fail loudly with a clear message on any other version | 1 |
| R17 | 2026-11-10 | hx-tubl | major | before | Auth tests failed because a dependency in the lockfile (jose) was not installed locally | A done-check starts with a clean install (npm ci or npm install) before running tests, so a stale install cannot show up as red tests | 1 |
| R18 | 2026-11-10 | hx-tubl | minor | review | README test counts (53) and status were typed once and went stale; real counts 60/81/83/157 | Numbers in docs come from a command's output, quoted with the date, never from memory | 1 |
| R19 | 2026-11-10 | hx-tubl | critical | before | Clinic contract has no edit or delete for slots, no remove for receptionists, no sign out, settings or light/dark; only free text says so and no check noticed | Every table declares create, view, edit and delete (or a written reason for each omission) and every app declares sign in, sign out, settings and appearance, each backed by a generated test that passes | 1 |
| R20 | 2026-11-10 | hx-aiqn | major | before | Free writer pair ran out of quota 30 minutes into a multi-route run, leaving half-finished branches | Count model calls before a multi-job run, stagger them, and make every run script stop on a quota error and write a resume marker; keep a second writer model as fallback that never reviews its own code | 1 |
| R21 | 2026-11-10 | hx-aiqn | minor | write | Standalone page could not use the page shell or data grid without a hard-coded Data heading, forced pager and broken caption | Every component renders alone in a test and exposes options for title, pager and caption; no placeholder text ships | 1 |
| R22 | 2026-11-10 | hx-aiqn | minor | before | A colour pack had to edit shared tokens and base CSS to change the heading font | Everything a look can change is a design token; a pack overrides tokens only and never edits shared CSS | 1 |
| R23 | 2026-11-10 | hx-aiqn | major | before | Kits (Tailwind, react-admin) pull non-MIT/Apache transitive packages that a direct-dependency licence rule misses | Run the licence check on the whole installed tree and list every package with its licence before a dependency is accepted | 1 |
| R24 | 2026-11-10 | hx-bs6b | critical | before | Runtime gave owner role to emails starting with owner@ and accepted tokens with no exp or iss; DO created before auth | Roles come only from stored rows, never from email shape or client input; verify tokens with algorithm, issuer, audience and required claims pinned and fail closed on missing config; reject before creating any per-tenant object | 1 |
| R25 | 2026-11-10 | hx-bs6b | major | before | Customers could read every user and booking in a tenant; no per-column read rule | Every table and column declares who may read and write it; default for end users is own rows only | 1 |
| R26 | 2026-11-10 | hx-bs6b | critical | write | Customer could create a booking for a nonexistent slot with status arrived and another patient's id; plan names pasted into SQL | Validate every write against enum, required, foreign key and per-role writable fields in the same transaction; validate plans strictly (name pattern, known operator types, references exist) | 1 |
| R27 | 2026-11-10 | hx-bs6b | major | write | Migration 'if not exists' left a changed plan unapplied while the version advanced | A migration diffs old against new plan, applies additive changes in one transaction, rejects the rest, and refuses the same version with different content; test a real version bump | 1 |
| R28 | 2026-11-10 | hx-bs6b | major | review | Eleven green tests hid auth, validation and migration holes; strictness test used word boundaries and scanned one folder | Write hostile-input and changed-plan tests first for every route and rule; a test that cannot fail is a finding; string scans use substrings over all source files | 1 |
| R29 | 2026-11-10 | hx-bs6b | critical | write | Writer put test accounts (staff@<owner domain>, staff@<tenant>.test) into production code to keep old tests green | When a test and correct behaviour disagree, stop and report a test dispute; fix the test set-up; never add test accounts, special emails or tenant patterns to production code; scan source for email literals | 1 |
| R30 | 2026-11-10 | hx-bs6b | critical | write | Staff could change the owner's email and role; guard listed one forbidden value instead of who may change identity fields | Authorization states who may change identity fields (email, role) and owner rows, as an allow-list; never a block-list of values | 1 |
| R31 | 2026-11-10 | hx-bs6b | critical | write | Runtime kept 'legacy' open read rules for plan_version 1 and 2 only so three older tests stay green (third time a writer bent production code for old tests) | A version, mode, flag or environment branch that exists to keep an old test passing is forbidden; report a test dispute instead; reviewers remove each such branch and see which tests fail | 1 |
| R32 | 2026-11-10 | hx-bs6b | major | before | Sign-in service ticket (aud=hostname, single use) and runtime bearer (aud=tenant, no jti) do not meet; unverified emails accepted; no end-to-end login test | When two services depend on each other, write the contract between them first and test the real path across both before building on top | 1 |
| R33 | 2026-11-10 | hx-aiqn | minor | review | Vendor-name test scanned class names and attributes in the DOM, so a writer dropped the icon package and hand-drew 369 lines of icons to pass it (test dispute not reported) | Public-copy tests check what a customer can read (visible text, titles, alt text, accessible names, meta), never class names or attributes; a test that forces hand-written replacements for ready-made parts is a test dispute to report | 1 |
| R34 | 2026-11-11 | breaksignal.ai AtomicSync phone, hx-5mly, 2026-11-11 | major | write | phone picture sat 32px off centre in its frame on phones; shipped because checks measured height only and eyes on screenshots missed it twice | Any centred picture, card, mock-up or panel must be verified by a measured browser check, never by eye: left gap equals right gap within 2px, no horizontal overflow, no cropped picture, at 360, 390, 768 and wide widths; when overriding a kit or library layout rule reset every offset property it sets (left, right, top, bottom, transform, margin), not only the one you see; run npm run verify (site) or scripts/layout-gate.mjs (platform) before review and quote its PASS line | 1 |

## Standing items (process, not findings; they frame the checklist)

| id | phase | slot | item |
|---|---|---|---|
| P1 | before | first | Restate the feature in 3 lines: user, outcome, what "done" looks like. List what is out of scope |
| P2 | before | first | List the tests first: the main path, the 3-5 edge cases a real user or a real failure will hit (timeouts, empty, duplicate, bad input from outside, restart mid-run). Skip two-failures-at-once cases and paths no user reaches; note the skips |
| P3 | review | first | Run the full test suite, typecheck, lint, and the build (image or site). Quote the last lines |
| P4 | review | last | In the PR: what was tested, what was consciously skipped and why, and the rule ids you checked against |
| P5 | before | last | AtomicSync UI or feature work: the brief carries the filled Apple rules checklist (skills/apple-design-rules) and `bin/jstack-apple-check <brief>` printed PASS; a T* trigger names its card |

## What NOT to spend time on

Skip edge cases that need two unlikely failures at once, features no user flow reaches, and micro-optimisation;
write one line in the PR saying what was consciously skipped and why.
