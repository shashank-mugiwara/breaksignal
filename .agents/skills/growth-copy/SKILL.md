---
name: growth-copy
description: >-
  Draft marketing, search and sales copy that fits each product's tone and makes no claim it cannot source. Use
  for App Store or Play listing text, release notes, website copy, search titles and descriptions, and outreach
  messages for AtomicSync, BreakSignal or AgentManage. Covers the per-product tone rules, the claims table,
  search and sales basics, and the review path. Drafts only; nothing here publishes.
jstack:
  status: draft
  reviewed_by: not yet reviewed by Shashank or the hub
---

# growth-copy

## Before writing

1. Read the live surface (the current listing, page or last release note). Copy never contradicts what is live
   unless the change is the point.
2. List the claims you intend to make. For each: where it comes from (the code, a live test, the party's own
   page) and the date read. A claim with no source is cut.
3. Check the product's tone rules below.

## Tone rules

**AtomicSync** (a wellness app, not a medical one)

- The avatar is the user's mirror and companion. Write about what the user sees change, not about targets missed.
- No streak pressure, no shaming, no clinical language.
- No medical claim, no diagnosis, no promise of a health outcome.
- Do not name AI or agents on public pages.
- Health data: say what is read and written in the same words the app's permission screens use.

**BreakSignal**

- Public copy never names AI models, vendors or tools.
- Describe the outcome for the client and how it is checked, not the machinery.

**AgentManage**

- Say what the platform does for an app that already exists: deploys, watches, patches and changes it.
- Do not promise what is still a plan. A plan is written as "plans to".

Where a product's own repo holds stricter rules, those win; put them in this skill's custom folder for that repo.

## Claims that need the party's own words

Anything about what a store, a law, a regulator or a vendor requires, allows, forbids or charges needs the
exact sentence from that party's page, the URL and the date read. Without the quote, do not write the claim.
Store review rules: use `skills/app-store-review`. Privacy wording: `skills/legal`, `skills/healthkit-privacy`,
`skills/dpdp-privacy` (the last two are drafts for a lawyer's review).

## Search copy

- Write the title and description for the question a person types.
- One page, one subject. Use the product's own terms consistently.
- No keyword lists, no hidden text, no claims that the page does not support.

## Sales and outreach

- One reader, one problem, one next step.
- No urgency that is not real, no invented numbers, testimonials or customer names.
- Outreach is a draft for Shashank to send as himself. This skill never sends.

## After writing

1. Run the `humanizer` skill on the draft, then re-check the claims table: no claim may have changed.
2. Save the draft with its claims table (claim, source, date read).
3. AtomicSync drafts go to its gatekeeper before Shashank; other products go to Shashank through the hub.

## Harness2 parts

- `checks/`, `canaries/`, `library/`: none yet. A useful canary pair is one approved listing and one that was
  rejected or corrected, with the reason.

### Changelog

- 2026-11-12: created (draft). Tone rules are Shashank's stated product rules.
