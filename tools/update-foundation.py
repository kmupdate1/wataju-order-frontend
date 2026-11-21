"""Snapshot reviewed core/localization/UI sources from an upstream git checkout.
Usage: python3 tools/update-foundation.py /path/to/foundation <reviewed-full-sha>
Build adapters stay outside vendor. Review excluded APIs before updating the lock.
"""
import hashlib
import json
from pathlib import Path
import shutil
import subprocess
import sys
source = Path(sys.argv[1]).resolve()
expected = sys.argv[2]
commit = subprocess.check_output(['git', '-C', str(source), 'rev-parse', 'HEAD'], text=True).strip()
assert len(expected) == 40 and commit == expected, 'Checkout must match reviewed full SHA'
assert not subprocess.check_output(['git', '-C', str(source), 'status', '--porcelain'], text=True).strip(), 'Use a clean checkout'
root = Path(__file__).resolve().parents[1]
vendor = root / 'vendor/kmp-application-foundation'
files = []
for module in ['core', 'feature/localization', 'ui']:
    subtree = vendor / module / 'src/commonMain/kotlin'
    if subtree.exists(): shutil.rmtree(subtree)
    for file in sorted((source / module / 'src/commonMain/kotlin').rglob('*.kt')):
        relative = file.relative_to(source)
        target = vendor / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(file, target)
        files.append(dict(path=str(relative), sha256=hashlib.sha256(file.read_bytes()).hexdigest()))
(vendor / 'source-lock.json').write_text(json.dumps(dict(
    repository='https://github.com/BlueBikeBase-Inc/kmp-application-foundation', commit=commit,
    files=files), indent=2) + '\n')
print(f'Updated {len(files)} source files. Review adapters and run all verification before committing.')
