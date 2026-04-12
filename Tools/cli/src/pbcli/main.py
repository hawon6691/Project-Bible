from __future__ import annotations

import argparse
import sys

from pbcli.gui import run_gui
from pbcli.services import (
    cmd_db_down,
    cmd_db_reset,
    cmd_db_up,
    cmd_doctor,
    cmd_down,
    cmd_list,
    cmd_test,
    cmd_up,
)


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(prog="pb", description="Project-Bible shared CLI")
    subparsers = parser.add_subparsers(dest="command", required=True)

    list_parser = subparsers.add_parser("list", help="List registered targets")
    list_parser.set_defaults(func=cmd_list)

    up_parser = subparsers.add_parser("up", help="Start a target in a new PowerShell window")
    up_parser.add_argument("target")
    up_parser.add_argument("--port", type=int, help="Override the assigned port")
    up_parser.set_defaults(func=cmd_up)

    down_parser = subparsers.add_parser("down", help="Stop a tracked target")
    down_parser.add_argument("target")
    down_parser.set_defaults(func=cmd_down)

    test_parser = subparsers.add_parser("test", help="Run tests for a target")
    test_parser.add_argument("target")
    test_parser.add_argument("--port", type=int, help="Optional port override for local test flows")
    test_parser.set_defaults(func=cmd_test)

    db_parser = subparsers.add_parser("db", help="Database operations")
    db_subparsers = db_parser.add_subparsers(dest="db_command", required=True)

    db_up_parser = db_subparsers.add_parser("up", help="Start database services")
    db_up_parser.set_defaults(func=cmd_db_up)

    db_down_parser = db_subparsers.add_parser("down", help="Stop database services")
    db_down_parser.set_defaults(func=cmd_db_down)

    reset_parser = db_subparsers.add_parser("reset", help="Reset a database domain")
    reset_parser.add_argument("engine", choices=("postgresql", "mysql"))
    reset_parser.add_argument("domain", choices=("post", "shop"))
    reset_parser.set_defaults(func=cmd_db_reset)

    doctor_parser = subparsers.add_parser("doctor", help="Check local toolchain")
    doctor_parser.set_defaults(func=cmd_doctor)

    gui_parser = subparsers.add_parser("gui", help="Open the local desktop GUI")
    gui_parser.set_defaults(func=lambda _args: run_gui())

    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()
    return args.func(args)


if __name__ == "__main__":
    sys.exit(main())
