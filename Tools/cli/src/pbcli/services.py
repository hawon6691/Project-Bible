from __future__ import annotations

import argparse
import shutil
import socket
import subprocess
import sys
from pathlib import Path

from pbcli.models import ProjectTarget
from pbcli.registry import flat_targets, load_targets
from pbcli.runtime import forget_process, load_runtime_state, remember_process, save_runtime_state


REPO_ROOT = Path(__file__).resolve().parents[4]
BACKEND_BASE_PORT = 8000
FRONTEND_BASE_PORT = 3000


def _quote_for_powershell(value: str) -> str:
    return value.replace("'", "''")


def _pid_is_alive(pid: int) -> bool:
    result = subprocess.run(
        ["powershell", "-NoProfile", "-Command", f"Get-Process -Id {pid} -ErrorAction SilentlyContinue"],
        check=False,
        capture_output=True,
        text=True,
    )
    return result.returncode == 0 and bool(result.stdout.strip())


def _find_listening_pid(port: int) -> int | None:
    command = (
        "Get-NetTCPConnection "
        f"-LocalPort {port} "
        "-State Listen "
        "-ErrorAction SilentlyContinue "
        "| Select-Object -First 1 -ExpandProperty OwningProcess"
    )
    result = subprocess.run(
        ["powershell", "-NoProfile", "-Command", command],
        check=False,
        capture_output=True,
        text=True,
    )
    value = result.stdout.strip()
    return int(value) if value.isdigit() else None


def _active_runtime_records() -> dict[str, object]:
    active = {}
    changed = False
    state = load_runtime_state()
    for name, record in state.items():
        if _pid_is_alive(record.pid):
            active[name] = record
        else:
            changed = True
    if changed:
        from pbcli.runtime import save_runtime_state

        save_runtime_state(active)
    return active


def _is_port_available(port: int) -> bool:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        try:
            sock.bind(("127.0.0.1", port))
        except OSError:
            return False
    return True


def _base_port_for(target: ProjectTarget) -> int:
    if target.kind == "backend":
        return BACKEND_BASE_PORT
    if target.kind == "frontend":
        return FRONTEND_BASE_PORT
    return target.port


def _resolve_port(target: ProjectTarget, requested_port: int | None) -> int:
    if requested_port is not None:
        active = _active_runtime_records()
        for record in active.values():
            if record.assigned_port == requested_port:
                raise SystemExit(
                    f"Port {requested_port} is already used by CLI target '{record.name}' "
                    f"(PID {record.pid}). Stop it first with: pb down {record.name} "
                    f"or pb down --port {requested_port}"
                )
        if not _is_port_available(requested_port):
            pid = _find_listening_pid(requested_port)
            pid_hint = f" by PID {pid}" if pid else ""
            raise SystemExit(
                f"Port {requested_port} is already in use{pid_hint}. "
                f"Stop that process first with: pb down --port {requested_port} "
                "or choose another port with --port."
            )
        return requested_port

    active = _active_runtime_records().values()
    occupied_ports = {record.assigned_port for record in active if record.kind == target.kind}
    port = _base_port_for(target)
    while port in occupied_ports or not _is_port_available(port):
        port += 1
    return port


def _resolve_frontend_api_base_url(target: ProjectTarget) -> str:
    active = _active_runtime_records().values()
    domain_ports = sorted(
        record.assigned_port
        for record in active
        if record.kind == "backend" and flat_targets().get(record.name) and flat_targets()[record.name].domain == target.domain
    )
    port = domain_ports[0] if domain_ports else BACKEND_BASE_PORT
    return f"http://localhost:{port}"


def _compose_start_command(target: ProjectTarget, port: int) -> str:
    if target.kind == "backend" and target.runtime == "java":
        return f"$env:SERVER_PORT='{port}'; $env:APP_PORT='{port}'; {target.start_command}"
    if target.kind == "backend" and target.runtime == "node":
        return f"$env:PORT='{port}'; $env:APP_PORT='{port}'; {target.start_command}"
    if target.kind == "frontend":
        api_base_url = _resolve_frontend_api_base_url(target)
        return (
            f"$env:PORT='{port}'; "
            f"$env:VITE_APP_PORT='{port}'; "
            f"$env:VITE_API_BASE_URL='{api_base_url}'; "
            f"{target.start_command}"
        )
    return target.start_command


def _compose_test_command(target: ProjectTarget, port: int | None) -> str:
    if port is None:
        return target.test_command
    if target.kind == "backend" and target.runtime == "java":
        return f"$env:SERVER_PORT='{port}'; $env:APP_PORT='{port}'; {target.test_command}"
    if target.kind in {"backend", "frontend"} and target.runtime == "node":
        return f"$env:PORT='{port}'; $env:APP_PORT='{port}'; {target.test_command}"
    return target.test_command


def _spawn_powershell_window(workdir: Path, command: str) -> int:
    workdir_str = _quote_for_powershell(str(workdir))
    command_str = _quote_for_powershell(command)
    launcher = (
        "Start-Process powershell "
        "-WorkingDirectory '{0}' "
        "-ArgumentList @('-NoExit','-Command','{1}') "
        "-PassThru | Select-Object -ExpandProperty Id"
    ).format(workdir_str, command_str)
    result = subprocess.run(
        ["powershell", "-NoProfile", "-Command", launcher],
        check=True,
        capture_output=True,
        text=True,
    )
    return int(result.stdout.strip())


def _run_local(command: str, workdir: Path) -> int:
    result = subprocess.run(
        ["powershell", "-NoProfile", "-Command", command],
        cwd=str(workdir),
        check=False,
    )
    return result.returncode


def _require_target(name: str) -> ProjectTarget:
    targets = flat_targets()
    if name not in targets:
        raise SystemExit(f"Unknown target: {name}")
    return targets[name]


def cmd_list(_args: argparse.Namespace) -> int:
    groups = load_targets()
    for group in ("backend", "frontend", "database"):
        print(f"[{group}]")
        for item in groups.get(group, []):
            base_port = _base_port_for(item) if item.kind in {"backend", "frontend"} else item.port
            print(f"- {item.name}: {item.path} (base-port={base_port})")
    return 0


def _print_runtime_table(rows: list[dict[str, str]]) -> None:
    columns = [
        ("name", "name"),
        ("kind", "kind"),
        ("pid", "pid"),
        ("port", "assigned_port"),
        ("started_at", "started_at"),
    ]
    widths = {
        label: max(len(label), *(len(row[key]) for row in rows))
        for label, key in columns
    }
    print("  ".join(label.ljust(widths[label]) for label, _key in columns))
    print("  ".join("-" * widths[label] for label, _key in columns))
    for row in rows:
        print("  ".join(row[key].ljust(widths[label]) for label, key in columns))


def cmd_status(_args: argparse.Namespace) -> int:
    rows = list_runtime_records()
    if not rows:
        print("No tracked processes.")
        return 0
    _print_runtime_table(rows)
    return 0


def cmd_search(args: argparse.Namespace) -> int:
    keyword = args.keyword.lower()
    commands = [
        ("list", "List registered targets", "pb list"),
        ("status", "List running CLI-tracked targets", "pb status"),
        ("up", "Start a target in a new PowerShell window", "pb up web-post --port 3000"),
        ("down", "Stop a tracked target or process by port", "pb down web-post / pb down --port 3000"),
        ("test", "Run tests for a target", "pb test web-shop"),
        ("db up", "Start database services", "pb db up"),
        ("db down", "Stop database services", "pb db down"),
        ("db reset", "Reset a database domain", "pb db reset postgresql post"),
        ("doctor", "Check local toolchain", "pb doctor"),
        ("gui", "Open the local desktop GUI", "pb gui"),
    ]
    targets = flat_targets()
    rows = [
        ("command", name, description, example)
        for name, description, example in commands
        if keyword in name.lower() or keyword in description.lower() or keyword in example.lower()
    ]
    rows.extend(
        ("target", target.name, f"{target.kind} / {target.domain} / {target.framework}", f"pb up {target.name}")
        for target in targets.values()
        if keyword in target.name.lower()
        or keyword in target.kind.lower()
        or keyword in target.domain.lower()
        or keyword in target.framework.lower()
    )
    if not rows:
        print(f"No commands or targets matched: {args.keyword}")
        return 1
    for row_type, name, description, example in rows:
        print(f"[{row_type}] {name}: {description}")
        print(f"  example: {example}")
    return 0


def cmd_up(args: argparse.Namespace) -> int:
    target = _require_target(args.target)
    if target.kind == "database":
        raise SystemExit("Use 'pb db up' for database targets.")
    active = _active_runtime_records()
    if target.name in active:
        raise SystemExit(f"Target already running in CLI state: {target.name}")
    assigned_port = _resolve_port(target, getattr(args, "port", None))
    command = _compose_start_command(target, assigned_port)
    pid = _spawn_powershell_window(target.abs_path(REPO_ROOT), command)
    remember_process(target.name, pid, target.kind, target.path, assigned_port)
    print(f"Started {target.name} in new PowerShell window (PID {pid}, port {assigned_port})")
    return 0


def cmd_down(args: argparse.Namespace) -> int:
    target = getattr(args, "target", None)
    port = getattr(args, "port", None)
    if not target and port is None:
        raise SystemExit("Provide a target name or --port. Example: pb down web-post or pb down --port 3000")
    if target and port is not None:
        raise SystemExit("Use either target name or --port, not both.")

    if port is not None:
        state = load_runtime_state()
        for name, record in list(state.items()):
            if record.assigned_port == port:
                state.pop(name)
                save_runtime_state(state)
                subprocess.run(
                    ["taskkill", "/PID", str(record.pid), "/T", "/F"],
                    check=False,
                    capture_output=True,
                    text=True,
                )
                print(f"Stopped {record.name} (PID {record.pid}, port {record.assigned_port})")
                return 0

        pid = _find_listening_pid(port)
        if pid is None:
            raise SystemExit(f"No CLI target or listening process found on port {port}.")
        subprocess.run(
            ["taskkill", "/PID", str(pid), "/T", "/F"],
            check=False,
            capture_output=True,
            text=True,
        )
        print(f"Stopped untracked process using port {port} (PID {pid})")
        return 0

    record = forget_process(target)
    if record is None:
        raise SystemExit(f"Target not running in CLI state: {target}")
    subprocess.run(
        ["taskkill", "/PID", str(record.pid), "/T", "/F"],
        check=False,
        capture_output=True,
        text=True,
    )
    print(f"Stopped {record.name} (PID {record.pid}, port {record.assigned_port})")
    return 0


def cmd_test(args: argparse.Namespace) -> int:
    target = _require_target(args.target)
    return _run_local(_compose_test_command(target, getattr(args, "port", None)), target.abs_path(REPO_ROOT))


def _docker_compose_path() -> Path:
    return REPO_ROOT / "Database" / "docker" / "docker-compose.yml"


def _docker_compose(command: str) -> int:
    compose = _docker_compose_path()
    return _run_local(f"docker compose -f '{compose}' {command}", REPO_ROOT)


def _database_env() -> dict[str, str]:
    env_path = REPO_ROOT / "Database" / "docker" / ".env"
    fallback_path = REPO_ROOT / "Database" / "docker" / ".env.example"
    source = env_path if env_path.exists() else fallback_path
    values: dict[str, str] = {}
    if not source.exists():
        return values
    for line in source.read_text(encoding="utf-8").splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("#") or "=" not in stripped:
            continue
        key, value = stripped.split("=", 1)
        values[key.strip()] = value.strip().strip('"').strip("'")
    return values


def _confirm_database_overwrite(engine: str, domain: str, db_name: str, assume_yes: bool) -> None:
    if assume_yes:
        return
    prompt = (
        f"This will drop and recreate {engine} database '{db_name}' for domain '{domain}'. "
        "Continue? [Y/N]: "
    )
    answer = input(prompt).strip().lower()
    if answer not in {"y", "yes"}:
        raise SystemExit("Database reset cancelled.")


def _mysql_identifier(value: str) -> str:
    return f"`{value.replace('`', '``')}`"


def _mysql_literal(value: str) -> str:
    escaped = value.replace("\\", "\\\\").replace("'", "''")
    return f"'{escaped}'"


def _apply_postgresql_sql(domain: str, assume_yes: bool = False) -> int:
    db_name = f"pb_{domain}"
    _confirm_database_overwrite("postgresql", domain, db_name, assume_yes)
    env = _database_env()
    user = env.get("POSTGRES_USER", "admin")
    init_dir = REPO_ROOT / "Database" / "postgresql" / domain
    schema_file = init_dir / "init" / "01_schema.sql"
    seed_file = init_dir / "seeds" / "01_seed.sql"
    subprocess.run(
        [
            "docker",
            "exec",
            "-i",
            "projectbible-postgres",
            "psql",
            "-U",
            user,
            "-d",
            "postgres",
            "-c",
            (
                "SELECT pg_terminate_backend(pid) "
                "FROM pg_stat_activity "
                f"WHERE datname = '{db_name}' AND pid <> pg_backend_pid();"
            ),
        ],
        check=True,
    )
    subprocess.run(
        [
            "docker",
            "exec",
            "-i",
            "projectbible-postgres",
            "psql",
            "-U",
            user,
            "-d",
            "postgres",
            "-c",
            f"DROP DATABASE IF EXISTS {db_name};",
        ],
        check=True,
    )
    subprocess.run(
        [
            "docker",
            "exec",
            "-i",
            "projectbible-postgres",
            "psql",
            "-U",
            user,
            "-d",
            "postgres",
            "-c",
            f"CREATE DATABASE {db_name};",
        ],
        check=True,
    )
    for file_path in (schema_file, seed_file):
        sql = file_path.read_text(encoding="utf-8")
        subprocess.run(
            [
                "docker",
                "exec",
                "-i",
                "projectbible-postgres",
                "psql",
                "-U",
                user,
                "-d",
                db_name,
                "-v",
                "ON_ERROR_STOP=1",
            ],
            input=sql,
            text=True,
            check=True,
        )
    print(f"Reset PostgreSQL database: {db_name}")
    return 0


def _apply_mysql_sql(domain: str, assume_yes: bool = False) -> int:
    db_name = f"pb_{domain}"
    _confirm_database_overwrite("mysql", domain, db_name, assume_yes)
    env = _database_env()
    root_password = env.get("MYSQL_ROOT_PASSWORD", "1234")
    app_user = env.get("MYSQL_USER", "admin")
    app_password = env.get("MYSQL_PASSWORD", "1234")
    init_dir = REPO_ROOT / "Database" / "mysql" / domain
    schema_file = init_dir / "init" / "01_schema.sql"
    seed_file = init_dir / "seeds" / "01_seed.sql"
    db_identifier = _mysql_identifier(db_name)
    app_user_literal = _mysql_literal(app_user)
    app_password_literal = _mysql_literal(app_password)
    subprocess.run(
        [
            "docker",
            "exec",
            "-i",
            "-e",
            f"MYSQL_PWD={root_password}",
            "projectbible-mysql",
            "mysql",
            "-uroot",
            "-e",
            (
                f"DROP DATABASE IF EXISTS {db_identifier}; "
                f"CREATE DATABASE {db_identifier} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; "
                f"CREATE USER IF NOT EXISTS {app_user_literal}@'%' IDENTIFIED BY {app_password_literal}; "
                f"ALTER USER {app_user_literal}@'%' IDENTIFIED BY {app_password_literal}; "
                f"GRANT ALL PRIVILEGES ON {db_identifier}.* TO {app_user_literal}@'%'; "
                "FLUSH PRIVILEGES;"
            ),
        ],
        check=True,
    )
    for file_path in (schema_file, seed_file):
        sql = file_path.read_text(encoding="utf-8")
        subprocess.run(
            [
                "docker",
                "exec",
                "-i",
                "-e",
                f"MYSQL_PWD={root_password}",
                "projectbible-mysql",
                "mysql",
                "-uroot",
                "--default-character-set=utf8mb4",
                db_name,
            ],
            input=sql,
            text=True,
            check=True,
        )
    print(f"Reset MySQL database: {db_name}")
    return 0


def cmd_db_up(_args: argparse.Namespace) -> int:
    return _docker_compose("up -d")


def cmd_db_down(_args: argparse.Namespace) -> int:
    return _docker_compose("down")


def cmd_db_reset(args: argparse.Namespace) -> int:
    if args.engine == "postgresql":
        return _apply_postgresql_sql(args.domain, getattr(args, "yes", False))
    if args.engine == "mysql":
        return _apply_mysql_sql(args.domain, getattr(args, "yes", False))
    raise SystemExit(f"Unsupported engine: {args.engine}")


def cmd_doctor(_args: argparse.Namespace) -> int:
    checks = [
        ("python", sys.executable),
        ("node", shutil.which("node")),
        ("npm", shutil.which("npm")),
        ("java", shutil.which("java")),
        ("docker", shutil.which("docker")),
    ]
    for name, value in checks:
        status = value if value else "missing"
        print(f"{name}: {status}")
    print(f"runtime-state: {(REPO_ROOT / 'Tools' / 'cli' / '.runtime' / 'state.json')}")
    return 0


def list_runtime_records() -> list[dict[str, str]]:
    rows: list[dict[str, str]] = []
    for record in _active_runtime_records().values():
        rows.append(
            {
                "name": record.name,
                "kind": record.kind,
                "pid": str(record.pid),
                "assigned_port": str(record.assigned_port),
                "started_at": record.started_at,
            }
        )
    return rows
