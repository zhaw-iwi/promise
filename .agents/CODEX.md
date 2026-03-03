# CODEX.md

Project execution rules for Codex sessions

## 1. Role of this file

This file defines how Codex should work in this repository.

Priority order when information conflicts:

1. Running code and tests
2. Schema and configuration in the repository
3. README.md
4. PROJECT.md
5. Other documentation

If documentation disagrees with code, treat the code as correct and update the documentation within the same milestone.

This file governs execution discipline, not product architecture.

---

## 2. Response Style Guidelines

When responding in chat:

- Use short paragraphs with clear section headings.
- Use bullet lists sparingly and only for compact sets.
- Use numbered lists only for strictly ordered procedures.
- Avoid nested numbering.
- Prefer at most one list per response unless explicitly asked for a checklist.
- Prefer clarity and structure over verbosity.

---

## 3. Engineering Principles

### 3.1 Design Constraints

1. Modularity  
   Keep components small and focused. Prefer clear boundaries over clever abstractions.

2. Separation of concerns  
   UI, API, domain logic, persistence, and infrastructure must remain separated. Controllers validate and delegate; they do not contain core logic.

3. Encapsulation  
   Hide internal implementation details. Expose the smallest stable surface necessary.

4. Orthogonality  
   Features should compose without hidden coupling. New functionality should require minimal refactoring.

5. Determinism and reproducibility  
   Any randomness must be controlled by explicit seeds or policies. Generated artifacts must be reproducible or clearly marked as non-deterministic.

6. Observability  
   Provide structured logs, actionable error messages, and enough tracing to diagnose failures quickly.

---

### 3.2 Correctness and Safety

1. Validate inputs at boundaries  
   All public entry points must validate inputs and fail with actionable errors.

2. Fail loudly and clearly  
   Avoid silent fallbacks unless explicitly designed. Use consistent error structures.

3. Security basics by default  
   No secrets in git. No arbitrary code execution. No path traversal. Apply least privilege principles.

---

## 4. Prototype Bias and Clean Slate Rule

This repository is considered prototype-oriented unless explicitly stated otherwise.

The default rule is:

Prefer clarity and minimalism over backward compatibility.

Therefore:

- Remove obsolete code instead of deprecating it.
- Refactor cleanly rather than layering compatibility adapters.
- Delete unused endpoints, classes, and helpers.
- Do not preserve legacy behavior unless explicitly required.
- Do not accumulate alternative implementations of the same concept.
- Prefer schema reset over complex migrations during early-stage development unless persistence stability is explicitly required.

If a change invalidates an old design and there is no explicit instruction to maintain compatibility, remove the old design entirely.

Dead code must not remain in the repository.

---

## 5. Repository Documentation Contract

### 5.1 PROJECT.md is mandatory

Maintain a `PROJECT.md` at repository root as the engineering audit trail.

Required contents:

1. Short project summary
2. Milestones checklist
3. For each milestone:
   - Date
   - Goal
   - What changed
   - How to run
   - How to test
   - Known issues and decisions
   - Next steps

Update `PROJECT.md` at the end of every milestone before requesting a commit.

---

### 5.2 README.md is the entry point

Update `README.md` whenever a milestone changes:

1. How to run
2. How to test
3. Configuration or environment variables
4. Public API, CLI, or user-facing behavior
5. Project structure or key concepts

README must remain concise and practical.

---

### 5.3 Keep Documentation in Sync

Behavior changes and documentation updates must occur in the same milestone.

Documentation drift is not allowed.

---

## 6. Milestone-Based Workflow

All work must be executed as explicit milestones.

For each milestone:

1. Define milestone scope  
   Clearly state the goal and concrete deliverables.

2. Implement the change  
   Keep changes minimal, coherent, and aligned with engineering principles.

3. Add or update tests  
   Provide a minimal, high-value set of tests that cover the intended behavior.

4. Run tests  
   Run the full suite or the affected subset. Fix failures before proceeding.

5. Update documentation  
   Update README.md and other relevant documentation. Update PROJECT.md with a milestone entry.

6. Stop and request commit  
   Only after steps 1 to 5 are complete.

---

## 7. Testing Rules

### 7.1 Minimal High-Value Test Sets

When creating tests:

Create a minimal, sound set of high-value tests covering:

- The primary success path.
- One representative failure path.
- Any non-trivial branching logic.

Do not generate exhaustive or combinatorial test matrices unless explicitly requested.

Avoid redundant tests that restate the same logic in multiple forms.

Prefer meaningful coverage over volume.

---

### 7.2 Deterministic Tests

- Tests must be reproducible.
- Avoid network access unless explicitly allowed.
- Use mocks or fixtures where appropriate.
- Control randomness explicitly.

---

### 7.3 Isolation

- Tests must not write outside temporary directories.
- Use temporary databases or fixtures for state.
- Do not rely on global environment side effects.

---

### 7.4 Test Scope Discipline

Prefer:

- Unit tests for pure logic.
- Integration tests for boundary flows.
- End-to-end tests only for critical smoke coverage.

Do not escalate to higher-level tests unless necessary.

---

## 8. Change Management Rules

### 8.1 Backward Compatibility

Backward compatibility is not required by default.

If compatibility must be preserved, it must be explicitly stated in the milestone scope.

Otherwise:

- Refactor cleanly.
- Remove outdated APIs.
- Update documentation accordingly.

---

### 8.2 Versioning and Migrations

If persisted state exists:

1. Introduce a schema version when stabilization begins.
2. During early prototype stages, prefer clear reset instructions over complex migration layers.
3. Add tests for version handling when stabilization is required.

---

### 8.3 Error Handling

Use consistent error shapes:

- Stable `error_code`
- Human-readable `message`
- Optional `details` field

Avoid ad-hoc error formats.

---

## 9. Code Quality Rules

1. Prefer clarity over cleverness.
2. Keep functions short and named by intent.
3. Remove duplication by extracting helpers.
4. Use type hints where supported.
5. Keep dependencies minimal and justified.
6. Centralize configuration.
7. Remove unused imports, classes, and functions immediately.
8. Do not leave commented-out code blocks.

---

## 10. Deliverables Checklist per Milestone

A milestone is complete when:

1. Functionality works as specified.
2. A minimal high-value test set covers the behavior.
3. Tests pass.
4. README.md updated if required.
5. PROJECT.md updated with the milestone entry.
6. Dead or obsolete code has been removed.

---

## 11. Default File Conventions

Expected files at repository root:

1. README.md
2. CODEX.md
3. PROJECT.md
4. LICENSE optional
5. .gitignore

If the project grows, maintain clear structure:

1. `src` or `app` for code
2. `tests` for tests
3. `docs` for additional documentation
4. `scripts` for tooling
5. `examples` for runnable examples

---

## 12. Session Startup Routine

At the start of a session:

1. Read CODEX.md and PROJECT.md.
2. Identify the next milestone or propose one.
3. Inspect existing code and tests to confirm the current state.
4. Execute work strictly using the milestone workflow.
5. Apply the clean slate rule unless explicitly instructed otherwise.
