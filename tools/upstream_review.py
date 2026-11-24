#!/usr/bin/env python3
"""Decide whether an upstream sync may be merged without a person looking at it.

Reads the staged diff of the repository (``git diff --cached``) and, optionally, the
diff of upstream's build files between the old and the new pinned commit, and prints
one line per reason a human should review the change. Exit code 0 = nothing found,
3 = review needed.

A sync is held for review when the incoming code

* reaches into non-SDK or vendor-private APIs (hidden-API bypass, reflection,
  ``com.oplus`` / ``com.color`` framework classes),
* starts doing network or file I/O, loads native code or runs processes,
* declares a broadcast receiver, or
* comes with a change to upstream's dependency catalog or module build files (this
  repository keeps its own build files, so new dependencies must be added by hand).

Only ADDED lines are inspected: code that was already here has been reviewed.

Usage:
    python -I tools/upstream_review.py [--upstream <clone> --old <sha> --new <sha>]
"""

from __future__ import annotations

import argparse
import re
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

RULES: list[tuple[str, re.Pattern[str]]] = [
    ("hidden-API bypass", re.compile(r"hiddenapibypass|HiddenApiBypass|setHiddenApiExemptions", re.I)),
    ("reflection", re.compile(r"Class\.forName|getDeclaredMethod|getDeclaredField|\.isAccessible\s*=|setAccessible\(|java\.lang\.reflect")),
    ("vendor-private framework class", re.compile(r"""["'](com\.oplus|com\.color|com\.coloros|android\.os\.SystemProperties)""")),
    ("network access", re.compile(r"java\.net\.|HttpURLConnection|okhttp3|io\.ktor|\bSocket\(|android\.permission\.INTERNET")),
    ("file or process access", re.compile(r"java\.io\.File\b|FileOutputStream|FileInputStream|Runtime\.getRuntime|ProcessBuilder|System\.loadLibrary|DexClassLoader")),
    ("broadcast receiver", re.compile(r"registerReceiver\(|BroadcastReceiver\b")),
]

BUILD_FILES = ["gradle/libs.versions.toml", "*/build.gradle.kts", "build.gradle.kts", "settings.gradle.kts"]


def git(args: list[str], cwd: Path) -> str:
    result = subprocess.run(["git", "-c", "core.quotepath=off", *args], cwd=cwd, capture_output=True, check=False)
    if result.returncode != 0:
        raise SystemExit(f"git {' '.join(args)} failed: {result.stderr.decode(errors='replace')}")
    return result.stdout.decode("utf-8", errors="replace")


def added_lines(diff: str):
    """Yield (path, line) for every added line of a unified diff."""
    path = ""
    for line in diff.splitlines():
        if line.startswith("+++ "):
            path = line[6:] if line.startswith("+++ b/") else line[4:]
        elif line.startswith("+") and not line.startswith("+++"):
            yield path, line[1:]


def review_sources(diff: str) -> list[str]:
    findings: list[str] = []
    for path, line in added_lines(diff):
        if not path.endswith((".kt", ".kts", ".xml", ".java")):
            continue
        stripped = line.strip()
        if stripped.startswith(("//", "*", "/*")):
            continue
        for label, pattern in RULES:
            if pattern.search(line):
                findings.append(f"{label}: {path}: {stripped[:160]}")
    return findings


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--upstream", type=Path, help="local clone of the upstream repository")
    parser.add_argument("--old", help="previously pinned upstream SHA")
    parser.add_argument("--new", help="upstream SHA being synced to")
    args = parser.parse_args()

    findings = review_sources(git(["diff", "--cached", "--unified=0", "--", "coui-*"], ROOT))

    if args.upstream and args.old and args.new:
        changed = git(["diff", "--name-only", args.old, args.new, "--", *BUILD_FILES], args.upstream.resolve()).split()
        for name in changed:
            findings.append(f"upstream build file changed (check dependencies by hand): {name}")

    for finding in findings:
        print(finding)
    return 3 if findings else 0


if __name__ == "__main__":
    sys.exit(main())
