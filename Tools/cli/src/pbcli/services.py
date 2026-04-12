from __future__ import annotations

import argparse
import shutil
import socket
import subprocess
import sys
from pathlib import Path

from pbcli.models import ProjectTarget
from pbcli.registry import flat_targets, load_targets
from pbcli.runtime import forget_process, load_runtime_state, remember_process


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
        if not _is_port_available(requested_port):
            raise SystemExit(f"Port {requested_port} is already in use.")
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
    record = forget_process(args.target)
    if record is None:
        raise SystemExit(f"Target not running in CLI state: {args.target}")
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


def _apply_postgresql_sql(domain: str) -> int:
    db_name = f"pb_{domain}"
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
            "project_bible",
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
            "project_bible",
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
                "project_bible",
                "-d",
                db_name,
            ],
            input=sql,
            text=True,
            check=True,
        )
    print(f"Reset PostgreSQL database: {db_name}")
    return 0


def _apply_mysql_sql(domain: str) -> int:
    db_name = f"pb_{domain}"
    init_dir = REPO_ROOT / "Database" / "mysql" / domain
    schema_file = init_dir / "init" / "01_schema.sql"
    seed_file = init_dir / "seeds" / "01_seed.sql"
    subprocess.run(
        [
            "docker",
            "exec",
            "-i",
            "projectbible-mysql",
            "mysql",
            "-uroot",
            "-pproject_bible",
            "-e",
            f"DROP DATABASE IF EXISTS {db_name}; CREATE DATABASE {db_name} CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;",
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
                "projectbible-mysql",
                "mysql",
                "-uroot",
                "-pproject_bible",
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
        return _apply_postgresql_sql(args.domain)
    if args.engine == "mysql":
        return _apply_mysql_sql(args.domain)
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
