from __future__ import annotations

from pathlib import Path

import yaml

from pbcli.models import ProjectTarget


def config_path() -> Path:
    return Path(__file__).resolve().parent / "config" / "projects.yaml"


def load_targets() -> dict[str, list[ProjectTarget]]:
    with config_path().open("r", encoding="utf-8") as file:
        raw = yaml.safe_load(file) or {}

    grouped: dict[str, list[ProjectTarget]] = {}
    for group in ("backend", "frontend", "database"):
        grouped[group] = [ProjectTarget(**item) for item in raw.get(group, [])]
    return grouped


def flat_targets() -> dict[str, ProjectTarget]:
    targets: dict[str, ProjectTarget] = {}
    for items in load_targets().values():
        for item in items:
            targets[item.name] = item
    return targets
