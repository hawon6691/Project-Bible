# pbcli

Shared Python CLI for Project-Bible scaffolding, local orchestration, and checks.

## Commands

- `pb list`
- `pb up <target>`
- `pb up <target> --port <number>`
- `pb down <target>`
- `pb down --port <number>`
- `pb test <target>`
- `pb db up`
- `pb db down`
- `pb db reset <engine> <domain>`
- `pb db reset <engine> <domain> --yes`
- `pb doctor`
- `pb gui`
- `pb search <keyword>`
- `pb --help`, `pb <command> --help`, `pb /?`, `pb <command> /?`

From the repository root, `pb.cmd` runs the same CLI without installing the package first:

```powershell
.\pb.cmd list
.\pb.cmd db reset mysql shop
```

## Runtime

- Process state file: `Tools/cli/.runtime/state.json`
- Target registry: `Tools/cli/src/pbcli/config/projects.yaml`
- GUI: Tkinter desktop panel backed by the same CLI service layer
- Default port policy: backend starts at `8000`, frontend starts at `3000`, then auto-increments by active runtime state
- If a requested port is already open, the CLI prints the occupying target or PID and asks you to stop it first with `pb down <target>` or `pb down --port <number>`.
- Command help supports both Unix-style `--help` and Windows-style `/?`.
- `pb search <keyword>` searches command names, descriptions, examples, and registered target names.
- `pb db reset` drops and recreates the selected database, then applies `init/01_schema.sql` and `seeds/01_seed.sql`. It asks for `Y/N` confirmation before overwriting; use `--yes` only for scripted runs.
