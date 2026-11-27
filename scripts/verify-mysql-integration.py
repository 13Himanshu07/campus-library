#!/usr/bin/env python3
"""Fail CI if Testcontainers did not execute the real MySQL integration suite."""

import sys
import xml.etree.ElementTree as ET
from pathlib import Path

report = Path(sys.argv[1])
if not report.is_file():
    raise SystemExit(f"Missing MySQL integration test report: {report}")

suite = ET.parse(report).getroot()
tests = int(suite.attrib.get("tests", "0"))
failures = int(suite.attrib.get("failures", "0"))
errors = int(suite.attrib.get("errors", "0"))
skipped = int(suite.attrib.get("skipped", "0"))
if tests < 14 or failures or errors or skipped:
    raise SystemExit(
        f"MySQL Testcontainers suite must execute >=14 tests with no skips; "
        f"tests={tests}, failures={failures}, errors={errors}, skipped={skipped}"
    )
print(f"MySQL integration verified: tests={tests}, failures=0, errors=0, skipped=0")
