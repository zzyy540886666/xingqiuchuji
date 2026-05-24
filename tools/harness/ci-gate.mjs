import { existsSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import path from "node:path";
import { spawnSync } from "node:child_process";
import process from "node:process";

const root = process.cwd();
const outputDir = path.join(root, ".harness", "ci");
const latestPath = path.join(outputDir, "latest.json");
const testJsonPath = path.join(outputDir, "frontend-test-results.json");

mkdirSync(outputDir, { recursive: true });

const useShell = process.platform === "win32";
const npmCommand = process.platform === "win32" ? "npm.cmd" : "npm";

function run(command, args) {
  const startedAt = new Date().toISOString();
  const result = spawnSync(command, args, {
    cwd: root,
    encoding: "utf8",
    shell: useShell,
  });

  return {
    command: [command, ...args].join(" "),
    startedAt,
    finishedAt: new Date().toISOString(),
    exitCode: typeof result.status === "number" ? result.status : 1,
    stdout: result.stdout || "",
    stderr: result.stderr || result.error?.message || "",
  };
}

function parseTestSummary() {
  if (!existsSync(testJsonPath)) {
    return {
      total: 0,
      passed: 0,
      failed: 0,
      tests: 0,
      success: false,
      parseError: `Missing test report: ${testJsonPath}`,
    };
  }

  try {
    const report = JSON.parse(readFileSync(testJsonPath, "utf8"));
    const total = Number(report.total ?? 0);
    const passed = Number(report.passed ?? 0);
    const failed = Number(report.failed ?? Math.max(total - passed, 0));
    return {
      total,
      passed,
      failed,
      tests: total,
      success: total > 0 && passed === total && failed === 0,
    };
  } catch (error) {
    return {
      total: 0,
      passed: 0,
      failed: 0,
      tests: 0,
      success: false,
      parseError: error instanceof Error ? error.message : String(error),
    };
  }
}

const typeCheck = run(npmCommand, ["run", "type-check"]);
const unitTests = run(npmCommand, ["run", "test:unit"]);
const testSummary = parseTestSummary();

const status = typeCheck.exitCode === 0
  && unitTests.exitCode === 0
  && testSummary.success
  ? "SUCCESS"
  : "FAILURE";

const summary = {
  status,
  tests: testSummary.tests,
  passed: testSummary.passed,
  total: testSummary.total,
  failed: testSummary.failed,
  generatedAt: new Date().toISOString(),
  gates: {
    typeCheck: typeCheck.exitCode === 0 ? "SUCCESS" : "FAILURE",
    unitTests: unitTests.exitCode === 0 && testSummary.success ? "SUCCESS" : "FAILURE",
    nonZeroTests: testSummary.tests > 0 ? "SUCCESS" : "FAILURE",
    allPassed: testSummary.passed === testSummary.total && testSummary.total > 0 ? "SUCCESS" : "FAILURE",
  },
  commands: {
    typeCheck,
    unitTests,
  },
  artifacts: {
    latest: latestPath,
    tests: testJsonPath,
  },
  parseError: testSummary.parseError,
};

writeFileSync(latestPath, `${JSON.stringify(summary, null, 2)}\n`, "utf8");

console.log(JSON.stringify({
  status: summary.status,
  tests: summary.tests,
  passed: summary.passed,
  total: summary.total,
  failed: summary.failed,
  artifact: latestPath,
}, null, 2));

if (summary.status !== "SUCCESS") {
  process.exitCode = 1;
}
