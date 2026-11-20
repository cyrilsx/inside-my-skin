# Task backlog

One file per task, named `<TASK-ID>.md`. Declare the TASK-ID at the start of every agent run.

Each task file contains:

- Goal: two sentences restating what the task delivers.
- Acceptance criteria: checkable statements.
- Dependencies: TASK-IDs that must be done first (per the build plan dependency graph).
- Lane: backend, frontend, or android, with the verification commands that apply.

Commit format: `task(TASK-ID): summary`. One task per branch.
