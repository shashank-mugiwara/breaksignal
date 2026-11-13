You are a senior reviewer. The attached file is a full code pack (directory tree + every file with
line numbers). Review it as if this ships to production tomorrow.

Report, most severe first, each as: SEVERITY (critical/major/minor) | file:line | what is wrong |
why it matters | the fix (code if short).

Check in this order:
1. Correctness bugs and unhandled edge cases (empty input, nulls, timeouts, retries, concurrency).
2. Security: injection, authz gaps, secrets in code, unsafe defaults, SSRF, path traversal.
3. Data loss or corruption paths; migrations; idempotency.
4. Tests: which behaviours have no test; list the 5 most valuable missing tests.
5. Design: dead code, duplication, names that lie, layering violations.
6. Frontend only: accessibility, performance (bundle, images, fonts), layout at 375/768/1280/1920px.

Rules: cite file:line for every finding; do not invent files; say "none found" for an empty
section; end with a one-line ship / don't-ship verdict.
