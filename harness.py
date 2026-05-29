#!/usr/bin/env python3

import os
import re
import subprocess
from datetime import datetime, timezone
from pathlib import Path

CLJ_CMD = os.environ.get("IJFY_CLJ_CMD", "clj")
BB_CMD = os.environ.get("IJFY_BB_CMD", "bb")
JANK_CMD = os.environ.get("IJFY_JANK_CMD", "jank")

DRYRUN = os.environ.get("IJFY_DRYRUN", "False").lower() == "true"
OUTPUT_DIR = os.environ["IJFY_OUTPUT_DIR"]


def parse_time_macro(s: str):
    m = re.search(r"Elapsed time: (\d+\.\d+)", s)
    if not m:
        raise RuntimeError("invalid time macro format")
    return float(m.group(1))


class Runner:
    def name(self) -> str: ...

    def version(self) -> str: ...

    def run(self, file: Path) -> float: ...


class ClojureRunner(Runner):
    def _run_clojure(args: list[str]):
        cmd = f"{CLJ_CMD} {' '.join(args)}"
        return subprocess.run(
            cmd,
            shell=True,
            capture_output=True,
            text=True,
        )

    def name(self) -> str:
        return "clojure"

    def version(self) -> str:
        result = ClojureRunner._run_clojure(["--version"])
        match = re.search(r"version (.+)", result.stdout)
        return match.group(1)

    def run(self, file: Path) -> float:
        result = ClojureRunner._run_clojure(
            [
                "-Sdeps",
                '\'{:paths ["lib", "vendor/ruuter/src"] :deps {criterium/criterium {:mvn/version "0.4.6"}}}\'',
                "-M",
                str(file),
            ]
        )
        return parse_time_macro(result.stdout)


class BabashkaRunner(Runner):
    def _run_bb(args: list[str]):
        cmd = f"{BB_CMD} {' '.join(args)}"
        return subprocess.run(
            cmd,
            shell=True,
            capture_output=True,
            text=True,
        )

    def name(self) -> str:
        return "babashka"

    def version(self) -> str:
        result = BabashkaRunner._run_bb(["--version"])
        match = re.search(r"babashka v(.+)", result.stdout)
        return match.group(1)

    def run(self, file: Path) -> float:
        result = BabashkaRunner._run_bb(["-cp", "lib:vendor/ruuter/src", str(file)])
        return parse_time_macro(result.stdout)


class JankRunner(Runner):
    def _run_jank(args: list[str]):
        cmd = f"{JANK_CMD} {' '.join(args)}"
        return subprocess.run(
            cmd,
            shell=True,
            capture_output=True,
            text=True,
        )

    def name(self) -> str:
        return "jank"

    def version(self) -> str:
        result = JankRunner._run_jank([])
        match = re.search(r"jank compiler jank-(.+)", result.stdout)
        return match.group(1)

    def run(self, file: Path) -> float:
        result = JankRunner._run_jank(
            [
                "--module-path",
                "lib:vendor/ruuter/src",
                "-O3",
                "--eagerness",
                "eager",
                "run",
                str(file),
            ]
        )
        return parse_time_macro(result.stdout)


if __name__ == "__main__":
    start_time = datetime.now(timezone.utc)
    print(start_time)

    runners = [ClojureRunner(), BabashkaRunner(), JankRunner()]
    benches = list(Path("benches").glob("*.cljc"))

    for bench in benches:
        print()
        print(bench)
        print(20 * "-")

        data_dir = Path(OUTPUT_DIR) / "data"
        output_path = (data_dir / bench.name).with_suffix(".csv")

        with open(output_path, "a") as output_file:
            for runner in runners:
                try:
                    duration = runner.run(bench)
                    duration_ms = 1e3 * duration

                    print(
                        f"{runner.name():<20}{runner.version():<20}\t{duration_ms:.3f}ms"
                    )

                    if not DRYRUN:
                        output_file.write(
                            ",".join(
                                [
                                    str(start_time),
                                    runner.name(),
                                    runner.version(),
                                    str(duration_ms),
                                ]
                            )
                            + "\n"
                        )
                except RuntimeError as e:
                    print("Failed to run benchmark, ignoring:", e)
