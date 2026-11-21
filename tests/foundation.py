"""Ensure the pinned upstream source stays byte-for-byte intact."""
import hashlib
import json
from pathlib import Path
root = Path(__file__).resolve().parents[1]
vendor = root / 'vendor/kmp-application-foundation'
lock = json.loads((vendor / 'source-lock.json').read_text())
expected = {entry['path'] for entry in lock['files']}
assert {str(p.relative_to(vendor)) for p in vendor.rglob('*.kt')} == expected
for entry in lock['files']:
    assert hashlib.sha256((vendor / entry['path']).read_bytes()).hexdigest() == entry['sha256'], entry['path']
print(f"Foundation {lock['commit']}: {len(expected)} upstream files verified")
