import importlib.util
from pathlib import Path
import tempfile
import unittest

CHECKER = Path(__file__).resolve().parents[1] / "check-ux-module-boundaries.py"
spec = importlib.util.spec_from_file_location("module_boundaries", CHECKER)
boundaries = importlib.util.module_from_spec(spec)
spec.loader.exec_module(boundaries)


class ModuleBoundaryTests(unittest.TestCase):
    def test_product_and_test_dependencies_have_different_rules(self):
        errors = boundaries.dependency_errors([
            (":feature:messages", "implementation", ":core:designsystem"),
            (":feature:messages", "testImplementation", ":core:data"),
            (":feature:messages", "debugUnitTestCompileClasspath", ":feature:messages"),
            (":feature:messages", "debugImplementation", ":feature:live"),
            (":feature:messages", "testImplementation", ":app"),
            (":core:data", "implementation", ":feature:discover"),
            (":feature:media", "releaseApi", ":core:model"),
            (":feature:media", "implementation", ":core:data"),
        ])
        self.assertEqual(errors, [
            ":feature:messages debugImplementation: forbidden project dependency :feature:live",
            ":feature:messages testImplementation: forbidden project dependency :app",
            ":core:data implementation: forbidden project dependency :feature:discover",
            ":feature:media releaseApi: forbidden project dependency :core:model",
            ":feature:media implementation: forbidden project dependency :core:data",
        ])

    def test_app_allows_navigation_but_rejects_nested_feature_implementation(self):
        with tempfile.TemporaryDirectory() as directory:
            android = Path(directory)
            source = android / "app/src/main/kotlin/dev/fanstage/android/navigation"
            source.mkdir(parents=True)
            (source / "FanStageNavHost.kt").write_text(
                "package dev.fanstage.android.navigation\nfun FanStageNavHost() {}", encoding="utf-8",
            )
            self.assertEqual(boundaries.source_errors(android), [])
            feature = source / "misplaced" / "DetailsViewModel.kt"
            feature.parent.mkdir()
            feature.write_text(
                "package dev.fanstage.android.navigation\nclass DetailsViewModel : ViewModel()",
                encoding="utf-8",
            )
            self.assertEqual(boundaries.source_errors(android), [
                f"feature implementation in app: {feature.relative_to(android)}",
            ])


if __name__ == "__main__":
    unittest.main()
