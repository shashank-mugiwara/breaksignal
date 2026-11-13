# Writer checklist (meet it BEFORE asking for review)

Generated from `skills/review-ledger/ledger.md` by `bin/jstack-ledger checklist --write`; edit the ledger, not this file.
A round-1 review should find nothing critical and nothing missing. Every item names the rule id the reviewer
tags, so a repeat shows up as a count, not a surprise.

## Caught more than once: meet these first

- R15 (major, seen 2 times): Never rm a path built from variables in agent shell commands; use "${VAR:?}"/name, a literal path, or a mktemp dir removed by trap; no bash -c wrapper around rm

## Before you write

1. Restate the feature in 3 lines: user, outcome, what "done" looks like. List what is out of scope (P1)
2. List the tests first: the main path, the 3-5 edge cases a real user or a real failure will hit (timeouts, empty, duplicate, bad input from outside, restart mid-run). Skip two-failures-at-once cases and paths no user reaches; note the skips (P2)
3. Every table declares create, view, edit and delete (or a written reason for each omission) and every app declares sign in, sign out, settings and appearance, each backed by a generated test that passes (R19, critical)
4. Roles come only from stored rows, never from email shape or client input; verify tokens with algorithm, issuer, audience and required claims pinned and fail closed on missing config; reject before creating any per-tenant object (R24, critical)
5. Run tests only on the pinned runtime from .nvmrc and fail loudly with a clear message on any other version (R16, major)
6. A done-check starts with a clean install (npm ci or npm install) before running tests, so a stale install cannot show up as red tests (R17, major)
7. Count model calls before a multi-job run, stagger them, and make every run script stop on a quota error and write a resume marker; keep a second writer model as fallback that never reviews its own code (R20, major)
8. Run the licence check on the whole installed tree and list every package with its licence before a dependency is accepted (R23, major)
9. Every table and column declares who may read and write it; default for end users is own rows only (R25, major)
10. When two services depend on each other, write the contract between them first and test the real path across both before building on top (R32, major)
11. Everything a look can change is a design token; a pack overrides tokens only and never edits shared CSS (R22, minor)
12. AtomicSync UI or feature work: the brief carries the filled Apple rules checklist (skills/apple-design-rules) and `bin/jstack-apple-check <brief>` printed PASS; a T* trigger names its card (P5)

## While you write

13. Never rm a path built from variables in agent shell commands; use "${VAR:?}"/name, a literal path, or a mktemp dir removed by trap; no bash -c wrapper around rm (R15, major) **(repeat x2)**
14. Any data file read at runtime ships inside the package (package-data / force-include) and is loaded via importlib.resources; test it from a built artifact, not the source tree (R1, critical)
15. When packaging changes, update every build manifest (Dockerfile COPY, .dockerignore, CI) in the same change; run the image build locally or in CI before review (R2, critical)
16. Validate every write against enum, required, foreign key and per-role writable fields in the same transaction; validate plans strictly (name pattern, known operator types, references exist) (R26, critical)
17. When a test and correct behaviour disagree, stop and report a test dispute; fix the test set-up; never add test accounts, special emails or tenant patterns to production code; scan source for email literals (R29, critical)
18. Authorization states who may change identity fields (email, role) and owner rows, as an allow-list; never a block-list of values (R30, critical)
19. A version, mode, flag or environment branch that exists to keep an old test passing is forbidden; report a test dispute instead; reviewers remove each such branch and see which tests fail (R31, critical)
20. Every network call has an explicit timeout and a test that the timeout is the intended value (R3, major)
21. Every scheduled job is non-overlapping (singleton key or in-flight guard), and writes are idempotent (unique key or upsert) (R4, major)
22. Never trust a remote filter for correctness: re-check the condition on the parsed data (R5, major)
23. When code writes a new enum/label value, update the schema and add a test that the exact value validates (R6, major)
24. Ownership of rows is scoped by two markers (id + source label), never one guessable field (R7, major)
25. Normalise external enums to a safe default; one bad record must not fail the whole batch (R8, major)
26. A migration diffs old against new plan, applies additive changes in one transaction, rejects the rest, and refuses the same version with different content; test a real version bump (R27, major)
27. Any centred picture, card, mock-up or panel must be verified by a measured browser check, never by eye: left gap equals right gap within 2px, no horizontal overflow, no cropped picture, at 360, 390, 768 and wide widths; when overriding a kit or library layout rule reset every offset property it sets (left, right, top, bottom, transform, margin), not only the one you see; run npm run verify (site) or scripts/layout-gate.mjs (platform) before review and quote its PASS line (R34, major)
28. Errors name the operation, path and status; never the secret (R9, minor)
29. State transitions reset every field the previous state set (R10, minor)
30. Cap or truncate external text before storing (R11, minor)
31. Every component renders alone in a test and exposes options for title, pager and caption; no placeholder text ships (R21, minor)

## Before review

32. Run the full test suite, typecheck, lint, and the build (image or site). Quote the last lines (P3)
33. Reviewer gets the full final diff INCLUDING tests; review saved to a file and posted on the PR; re-review after the last change (R12, critical)
34. Public pages use the taste skill run (gate + copy-lint), show the real product, one motion moment; screenshot at 1440 and 390 before asking anyone (R13, critical)
35. Cards and plans are written from the merged code: quote the exact call/path the code uses (R14, major)
36. Write hostile-input and changed-plan tests first for every route and rule; a test that cannot fail is a finding; string scans use substrings over all source files (R28, major)
37. Numbers in docs come from a command's output, quoted with the date, never from memory (R18, minor)
38. Public-copy tests check what a customer can read (visible text, titles, alt text, accessible names, meta), never class names or attributes; a test that forces hand-written replacements for ready-made parts is a test dispute to report (R33, minor)
39. In the PR: what was tested, what was consciously skipped and why, and the rule ids you checked against (P4)

## What NOT to spend time on

Edge cases that need two unlikely failures at once, features no user flow reaches, micro-optimisation.
Write one line in the PR saying what was consciously skipped and why.
