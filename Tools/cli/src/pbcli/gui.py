from __future__ import annotations

import threading
import tkinter as tk
from tkinter import messagebox, ttk

from pbcli.registry import load_targets
from pbcli.services import (
    cmd_db_down,
    cmd_db_up,
    cmd_doctor,
    cmd_down,
    cmd_test,
    cmd_up,
    list_runtime_records,
)


class GuiApp:
    def __init__(self) -> None:
        self.root = tk.Tk()
        self.root.title("Project-Bible CLI GUI")
        self.root.geometry("980x640")
        self.selected_name = tk.StringVar()
        self.port_value = tk.StringVar()
        self._build()
        self._load_targets()
        self._refresh_runtime()

    def _build(self) -> None:
        main = ttk.Frame(self.root, padding=12)
        main.pack(fill=tk.BOTH, expand=True)

        left = ttk.Frame(main)
        left.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)

        ttk.Label(left, text="Targets").pack(anchor=tk.W)
        self.tree = ttk.Treeview(left, columns=("path",), show="tree headings", height=18)
        self.tree.heading("#0", text="Name")
        self.tree.heading("path", text="Path")
        self.tree.column("#0", width=320)
        self.tree.column("path", width=380)
        self.tree.pack(fill=tk.BOTH, expand=True)
        self.tree.bind("<<TreeviewSelect>>", self._on_select)

        right = ttk.Frame(main, padding=(12, 0, 0, 0))
        right.pack(side=tk.RIGHT, fill=tk.Y)

        ttk.Label(right, text="Selected").pack(anchor=tk.W)
        ttk.Entry(right, textvariable=self.selected_name, width=48).pack(fill=tk.X)
        ttk.Label(right, text="Port Override").pack(anchor=tk.W, pady=(8, 0))
        ttk.Entry(right, textvariable=self.port_value, width=48).pack(fill=tk.X)

        for text, command in (
            ("Start", self._start_selected),
            ("Stop", self._stop_selected),
            ("Test", self._test_selected),
            ("DB Up", lambda: self._run_async(lambda: cmd_db_up(_Dummy()))),
            ("DB Down", lambda: self._run_async(lambda: cmd_db_down(_Dummy()))),
            ("Doctor", lambda: self._run_async(lambda: cmd_doctor(_Dummy()))),
            ("Refresh Runtime", self._refresh_runtime),
        ):
            ttk.Button(right, text=text, command=command).pack(fill=tk.X, pady=4)

        ttk.Label(right, text="Runtime State").pack(anchor=tk.W, pady=(12, 0))
        self.runtime = tk.Text(right, width=44, height=20)
        self.runtime.pack(fill=tk.BOTH, expand=True)

    def _load_targets(self) -> None:
        groups = load_targets()
        for group, items in groups.items():
            parent = self.tree.insert("", tk.END, text=group, values=("",), open=True)
            for item in items:
                self.tree.insert(parent, tk.END, text=item.name, values=(item.path,))

    def _on_select(self, _event: object) -> None:
        selected = self.tree.selection()
        if not selected:
            return
        node = selected[0]
        parent = self.tree.parent(node)
        if not parent:
            return
        self.selected_name.set(self.tree.item(node, "text"))

    def _run_async(self, action) -> None:
        def runner() -> None:
            try:
                action()
            except Exception as exc:  # noqa: BLE001
                self.root.after(0, lambda: messagebox.showerror("CLI GUI", str(exc)))
            finally:
                self.root.after(0, self._refresh_runtime)

        threading.Thread(target=runner, daemon=True).start()

    def _start_selected(self) -> None:
        name = self.selected_name.get().strip()
        if not name:
            messagebox.showwarning("CLI GUI", "Select a target first.")
            return
        port = self.port_value.get().strip()
        self._run_async(lambda: cmd_up(_TargetArgs(name, port)))

    def _stop_selected(self) -> None:
        name = self.selected_name.get().strip()
        if not name:
            messagebox.showwarning("CLI GUI", "Select a target first.")
            return
        self._run_async(lambda: cmd_down(_TargetArgs(name)))

    def _test_selected(self) -> None:
        name = self.selected_name.get().strip()
        if not name:
            messagebox.showwarning("CLI GUI", "Select a target first.")
            return
        self._run_async(lambda: cmd_test(_TargetArgs(name)))

    def _refresh_runtime(self) -> None:
        self.runtime.delete("1.0", tk.END)
        rows = list_runtime_records()
        if not rows:
            self.runtime.insert(tk.END, "No tracked processes.\n")
            return
        for row in rows:
            self.runtime.insert(
                tk.END,
                f"{row['name']} | {row['kind']} | PID {row['pid']} | port {row['assigned_port']} | {row['started_at']}\n",
            )

    def run(self) -> None:
        self.root.mainloop()


class _Dummy:
    pass


class _TargetArgs:
    def __init__(self, target: str, port: str | None = None) -> None:
        self.target = target
        self.port = int(port) if port else None


def run_gui() -> int:
    app = GuiApp()
    app.run()
    return 0
