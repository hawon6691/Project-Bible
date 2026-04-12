from __future__ import annotations

import json
from dataclasses import asdict
from datetime import datetime, timezone
from pathlib import Path

from pbcli.models import RuntimeRecord


def runtime_dir() -> Path:
    directory = Path(__file__).resolve().parents[2] / ".runtime"
    directory.mkdir(parents=True, exist_ok=True)
    return directory


def runtime_state_path() -> Path:
    return runtime_dir() / "state.json"


def load_runtime_state() -> dict[str, RuntimeRecord]:
    path = runtime_state_path()
    if not path.exists():
        return {}
    with path.open("r", encoding="utf-8") as file:
        raw = json.load(file)
    return {name: RuntimeRecord(**value) for name, value in raw.items()}


def save_runtime_state(state: dict[str, RuntimeRecord]) -> None:
    path = runtime_state_path()
    serializable = {name: asdict(record) for name, record in state.items()}
    with path.open("w", encoding="utf-8") as file:
        json.dump(serializable, file, ensure_ascii=True, indent=2)


def remember_process(name: str, pid: int, kind: str, path: str, assigned_port: int) -> RuntimeRecord:
    state = load_runtime_state()
    record = RuntimeRecord(
        name=name,
        pid=pid,
        kind=kind,
        path=path,
        assigned_port=assigned_port,
        started_at=datetime.now(timezone.utc).isoformat(),
    )
    state[name] = record
    save_runtime_state(state)
    return record


def forget_process(name: str) -> RuntimeRecord | None:
    state = load_runtime_state()
    record = state.pop(name, None)
    save_runtime_state(state)
    return record
