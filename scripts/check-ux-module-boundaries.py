#!/usr/bin/env python3
"""Verify evaluated Gradle dependencies and Android source ownership."""
from pathlib import Path
import json
import re
import subprocess
import sys

ANDROID = Path(__file__).resolve().parents[1] / "android"
FEATURE_UI = {":core:model", ":core:artwork", ":core:designsystem"}
PRODUCT_DEPENDENCIES = {
    ":core:model": set(),
    ":core:data": {":core:model"},
    ":core:artwork": {":core:model"},
    ":core:designsystem": set(),
    ":feature:live": {":core:designsystem"},
    ":feature:discover": FEATURE_UI,
    ":feature:community": FEATURE_UI,
    ":feature:media": FEATURE_UI,
    ":feature:messages": FEATURE_UI,
    ":app": {
        ":feature:live", ":feature:discover", ":feature:community", ":feature:media",
        ":feature:messages", ":core:model", ":core:data", ":core:designsystem",
    },
    ":macrobenchmark": {":app"},
}
# Tests may use Fake implementations without making them a product dependency.
TEST_ONLY_DEPENDENCIES = {
    module: {":core:data"}
    for module in PRODUCT_DEPENDENCIES
    if module.startswith(":feature:")
}
REPORT_PREFIX = "FANSTAGE_MODULE_DEPENDENCIES="
APP_PACKAGE = "dev.fanstage.android"

ANDROID_MODEL_REFERENCE = re.compile(r"(?<![\w.])(?:android|androidx)\.|\bR\.drawable\b|\bContext\b")
FEATURE_TYPE = re.compile(
    r"\b(?:class|interface|object|typealias)\s+\w*(?:ViewModel|Repository|Transport)\b"
    r"|:\s*(?:[\w.]+\.)?(?:AndroidViewModel|ViewModel|ChatTransport)\b"
)
TEST_CONFIGURATION = re.compile(r"^(?:test|androidTest|hiltAndroidTest)|(?:AndroidTest|UnitTest|TestFixtures)")
PACKAGE = re.compile(r"^\s*package\s+([\w.]+)", re.M)


def read_gradle_dependencies():
    result = subprocess.run(
        ["./gradlew", "--quiet", "--no-configuration-cache",
         "-I", "gradle/module-boundaries.init.gradle", "fanStageModuleDependencies"],
        cwd=ANDROID, text=True, stdout=subprocess.PIPE, check=True,
    )
    reports = [line.removeprefix(REPORT_PREFIX) for line in result.stdout.splitlines()
               if line.startswith(REPORT_PREFIX)]
    if len(reports) != 1:
        raise ValueError("Expected one dependency report from Gradle")
    return json.loads(reports[0])


def dependency_errors(dependencies):
    errors = []
    for module, configuration, target in dependencies:
        test = TEST_CONFIGURATION.search(configuration) and module != ":macrobenchmark"
        # AGP adds the tested project itself to its test classpaths.
        if test and target == module and configuration.endswith("Classpath"):
            continue
        allowed = PRODUCT_DEPENDENCIES.get(module, set())
        if test:
            allowed = allowed | TEST_ONLY_DEPENDENCIES.get(module, set())
        if target not in allowed or (not test and configuration.lower().endswith(("api", "apielements"))):
            errors.append(f"{module} {configuration}: forbidden project dependency {target}")
    return errors


def sources(root):
    return (path for path in root.rglob("*") if path.suffix in {".kt", ".java"} and path.is_file())


def source_errors(android):
    errors = []
    for path in sources(android / "core/model/src/main"):
        if ANDROID_MODEL_REFERENCE.search(path.read_text(encoding="utf-8")):
            errors.append(f"Android UI reference in pure model: {path.relative_to(android)}")
    for source_set in (android / "app/src").iterdir():
        if "test" in source_set.name.lower():
            continue
        for path in sources(source_set):
            source = path.read_text(encoding="utf-8")
            package = PACKAGE.search(source)
            allowed_package = package and package[1] in {
                APP_PACKAGE, f"{APP_PACKAGE}.navigation", f"{APP_PACKAGE}.di",
            }
            if not allowed_package:
                errors.append(f"unexpected app source package: {path.relative_to(android)}")
            # The benchmark APK has its own synthetic ChatTransport.
            benchmark_fixture = source_set.name == "benchmark" and path.name == "BenchmarkRoomActivity.kt"
            if not benchmark_fixture and FEATURE_TYPE.search(source):
                errors.append(f"feature implementation in app: {path.relative_to(android)}")
    return errors


def main():
    try:
        errors = dependency_errors(read_gradle_dependencies()) + source_errors(ANDROID)
    except (OSError, ValueError, subprocess.CalledProcessError) as error:
        print(f"FAIL: could not verify module boundaries: {error}", file=sys.stderr)
        return 1
    if errors:
        print("FAIL\n" + "\n".join(errors), file=sys.stderr)
        return 1
    print("PASS: Gradle project dependencies and API visibility, pure models, and app composition root")
    return 0


if __name__ == "__main__":
    sys.exit(main())
