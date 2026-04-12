# pbcli

Shared Python CLI for Project-Bible scaffolding, local orchestration, and checks.

## Commands

- `pb list`
- `pb up <target>`
- `pb up <target> --port <number>`
- `pb down <target>`
- `pb test <target>`
- `pb db up`
- `pb db down`
- `pb db reset <engine> <domain>`
- `pb doctor`
- `pb gui`

## Runtime

- Process state file: `Tools/cli/.runtime/state.json`
- Target registry: `Tools/cli/src/pbcli/config/projects.yaml`
- GUI: Tkinter desktop panel backed by the same CLI service layer
- Default port policy: backend starts at `8000`, frontend starts at `3000`, then auto-increments by active runtime state
