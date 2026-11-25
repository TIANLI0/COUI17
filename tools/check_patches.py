#!/usr/bin/env python3
"""Check that the upstream-owned sources are exactly upstream at UPSTREAM_SHA plus patches/*.patch.

The daily sync (tools/sync_upstream.py) replaces ``<module>/src/{commonMain,androidMain}`` of the
upstream modules with upstream's and re-applies ``patches/*.patch``. A change made to those paths
directly, without a patch, is therefore silently reverted by the next sync. This script rebuilds
that tree in a scratch directory and compares it with the working tree; any difference is a change
that still needs a patch (or a patch that no longer matches the sources).

Usage (from anywhere):

    python -I tools/check_patches.py --repo <path-to-Costben/coui17-clone>

Exit status 0 when the trees match, 1 when they differ.
"""

from __future__ import annotations

import argparse
import filecmp
import shutil
import sys
import tempfile
from pathlib import Path

HERE = Path(__file__).resolve().parent.parent
sys.dont_write_bytecode = True
sys.path.insert(0, str(HERE / "tools"))

import sync_upstream  # noqa: E402  (tools/ is not a package)


def read_normalized(path: Path) -> bytes:
    return path.read_bytes().replace(b"\r\n", b"\n")


def tree_files(root: Path) -> set[str]:
    if not root.exists():
        return set()
    return {p.relative_to(root).as_posix() for p in root.rglob("*") if p.is_file()}


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--repo", required=True, type=Path, help="local clone of Costben/coui17")
    args = parser.parse_args()

    sha = (HERE / "UPSTREAM_SHA").read_text(encoding="utf-8").strip()
    scratch = Path(tempfile.mkdtemp(prefix="coui17-patch-check-"))
    try:
        shutil.copytree(HERE / "patches", scratch / "patches")
        sys.argv = ["sync_upstream.py", "--repo", str(args.repo.resolve()), "--sha", sha, "--dest", str(scratch)]
        sync_upstream.main()

        differences: list[str] = []
        for module in sync_upstream.MODULES:
            for source_set in sync_upstream.SOURCE_SETS:
                rel = f"{module}/src/{source_set}"
                expected_root = scratch / rel
                actual_root = HERE / rel
                expected = tree_files(expected_root)
                actual = tree_files(actual_root)
                for f in sorted(actual - expected):
                    differences.append(f"only here (needs a patch): {rel}/{f}")
                for f in sorted(expected - actual):
                    differences.append(f"missing here (deleted without a patch): {rel}/{f}")
                for f in sorted(expected & actual):
                    a, b = expected_root / f, actual_root / f
                    if not filecmp.cmp(a, b, shallow=False) and read_normalized(a) != read_normalized(b):
                        differences.append(f"differs from upstream + patches: {rel}/{f}")
    finally:
        shutil.rmtree(scratch, ignore_errors=True)

    if differences:
        print("Upstream-owned sources do not match upstream @ %s + patches/*.patch:" % sha)
        for line in differences:
            print("  " + line)
        print("Record the change as a patch (see UPSTREAM.md) or the next upstream sync reverts it.")
        return 1
    print("upstream-owned sources == upstream @ %s + patches/*.patch" % sha)
    return 0


if __name__ == "__main__":
    sys.exit(main())
