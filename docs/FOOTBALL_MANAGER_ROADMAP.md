# Football Manager Simulation — Production Roadmap

This roadmap turns the current prototype into a coherent, data-driven football management game. A feature is not complete until its data, simulation, AI, UI, persistence and tests work together.

## Product pillars

1. A persistent football world that advances every day.
2. Credible squad building and club economics.
3. Tactical decisions with visible match consequences.
4. Clear, adaptive interfaces on phones, tablets and landscape screens.
5. Deterministic saves: every fixture, event, transaction and result is persisted.

## Phase 1 — World and season foundation

- Validate imported nations, leagues, clubs, players, contracts and competition membership.
- Define competition rules for league, domestic cup, continental cup and friendly matches.
- Generate an entire season calendar before day one, including match, training, recovery, scouting, board and transfer-window events.
- Prevent fixture collisions and enforce recovery gaps.
- Simulate every AI club on the same world clock as the user.
- Acceptance: calendar shows the whole season; standings and brackets update from persisted results; save/reload produces the same world state.

## Phase 2 — Squad lifecycle

- Separate First Team, Reserve/U23 and Academy registrations.
- Add promotion/demotion, squad roles, contracts, morale, fatigue, fitness, sharpness, suspension and injury availability.
- Training plans operate by unit and individual focus; medical workload influences injury risk and return dates.
- Acceptance: unavailable players cannot be selected; team movement persists; training changes attributes/fitness over time.

## Phase 3 — Tactics and selection

- Complete formation library plus custom formation slots.
- Drag players and positions on the pitch with bounds, collision and positional-suitability feedback.
- Team instructions: mentality, width, tempo, pressing, defensive line, passing, transition and time wasting.
- Player instructions: role, duty, pressing, marking, movement and risk.
- Acceptance: exactly eleven valid starters, bench rules enforced, tactical choices feed measurable modifiers into simulation.

## Phase 4 — Match engine

- Timeline states: pre-match, first half, half-time pause, second half, stoppage time and full-time.
- Generate possession chains, chances, shots, saves, goals, cards, injuries, substitutions and VAR/set-piece events.
- Contextual localized commentary for every important event.
- User can change tactics and make substitutions at half-time and during stoppages; AI reacts to score, fatigue and cards.
- Acceptance: event totals reconcile with match statistics and final score; player minutes and ratings are exact.

## Phase 5 — Recruitment and finance

- Scouting assignments by region, competition, position, age and role; knowledge controls attribute uncertainty.
- Transfer AI evaluates need, budget, value, contract length, potential and squad status.
- Negotiation includes fee structure, clauses, wages, bonuses, agent fee and work/registration constraints.
- Finance ledger covers transfers, wages, prize money, gate receipts, sponsorship and monthly operating costs.
- Acceptance: no club can spend unavailable funds; AI completes transfers; every balance change has a ledger entry.

## Phase 6 — UI, accessibility and localization

- Adaptive navigation and content breakpoints for compact, medium and expanded Android windows.
- Calendar uses agenda detail on phones and split view on tablets/landscape.
- Settings has separate Game, Display, Audio and Language sections.
- Consistent scoreboards, home/away alignment, touch targets, contrast, scrolling and empty/error states.
- Externalize all user-visible strings and ship English and Bahasa Indonesia first.
- Acceptance: screenshot tests at phone portrait, phone landscape, 7-inch tablet and large tablet; no clipping or unreachable controls.

## Release gates

- Unit tests for scheduling, standings, selection, transfers, finance, injuries and match-event reconciliation.
- Room migration tests and deterministic simulation tests with seeded randomness.
- Compose UI tests for calendar, tactics, half-time, substitution, settings and result screens.
- Pull requests must pass assembleDebug, unit tests and lint; release builds are signed only from the protected main branch.
