#!/usr/bin/env python3
"""Fail CI if Testcontainers did not execute the real MySQL integration suite."""

import sys
import xml.etree.ElementTree as ET
from pathlib import Path

EXPECTED_CLASS = "com.library.MySqlCirculationIntegrationTest"
MIN_EXECUTED_TESTS = 14

if len(sys.argv) != 2:
    raise SystemExit("Usage: verify-mysql-integration.py <surefire-report.xml>")

report = Path(sys.argv[1])
if not report.is_file():
    raise SystemExit(f"Missing MySQL integration test report: {report}")

root = ET.parse(report).getroot()
suites = [node for node in root.iter("testsuite") if node.attrib.get("name") == EXPECTED_CLASS]
if root.tag == "testsuite" and root.attrib.get("name") == EXPECTED_CLASS:
    suites = [root]
if not suites:
    raise SystemExit(f"Surefire report contains no {EXPECTED_CLASS} suite")

cases = [case for suite in suites for case in suite.findall("testcase")]
tests = len(cases)
failures = sum(case.find("failure") is not None for case in cases)
errors = sum(case.find("error") is not None for case in cases)
skipped = sum(case.find("skipped") is not None for case in cases)
executed = tests - skipped
if executed < MIN_EXECUTED_TESTS or failures or errors or skipped:
    raise SystemExit(
        f"{EXPECTED_CLASS} must execute >={MIN_EXECUTED_TESTS} tests with no skips; "
        f"tests={tests}, executed={executed}, failures={failures}, errors={errors}, skipped={skipped}"
    )
print(f"MySQL integration verified: class={EXPECTED_CLASS}, executed={executed}, failures=0, errors=0, skipped=0")
