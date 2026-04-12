from __future__ import annotations

from dataclasses import dataclass
from pathlib import Path


@dataclass
class ProjectTarget:
    name: str
    path: str
    kind: str
    domain: str
    runtime: str
    framework: str
    build: str
    db: str
    port: int
    start_command: str
    test_command: str
    env_file: str

    def abs_path(self, repo_root: Path) -> Path:
        return repo_root / self.path


@dataclass
class RuntimeRecord:
    name: str
    pid: int
    kind: str
    path: str
    assigned_port: int
    started_at: str
