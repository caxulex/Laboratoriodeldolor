#!/usr/bin/env python3
"""
Conservative Kotlin unused-imports cleanup.
Rules:
 - Only edits .kt files under app/src and top-level src if present.
 - Only removes explicit imports (no wildcard imports ending with .*).
 - For alias imports ("as"), checks alias token usage; otherwise checks last identifier.
 - Backups each modified file as filename.bak before writing.
 - Prints a simple summary and unified-diff-like lines.

Run from repo root: python scripts/cleanup_unused_imports.py
"""
import os
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SEARCH_DIRS = [ROOT / 'app' / 'src', ROOT / 'src']

import_line_re = re.compile(r'^\s*import\s+([\w\.\*]+)(?:\s+as\s+(\w+))?\s*$', re.MULTILINE)
identifier_re = re.compile(r'\b([A-Za-z_]\w*)\b')

changed_files = []

for base in SEARCH_DIRS:
    if not base.exists():
        continue
    for p in base.rglob('*.kt'):
        try:
            text = p.read_text(encoding='utf-8')
        except Exception:
            continue
        imports = []
        for m in import_line_re.finditer(text):
            full = m.group(1)
            alias = m.group(2)
            if full.endswith('.*'):
                continue
            # token to check: alias if present else last segment after dot
            token = alias if alias else full.split('.')[-1]
            imports.append((m.group(0), token, m.start(), m.end()))

        if not imports:
            continue

        # Remove the import lines if token not present elsewhere in file (excluding the import itself)
        new_lines = text.splitlines()
        modified = False
        for imp_line, token, s, e in imports:
            # count occurrences excluding the import lines area
            # naive check: search token with word boundaries
            # build text without import line to avoid false positive
            text_excluding_imports = re.sub(re.escape(imp_line), '', text)
            if re.search(r'\b' + re.escape(token) + r'\b', text_excluding_imports):
                continue
            # remove all exact matching import lines (there may be duplicates)
            for i, ln in enumerate(new_lines):
                if ln is None:
                    continue
                if ln.strip() == imp_line.strip():
                    new_lines[i] = None
                    modified = True
        if modified:
            # create a unique backup path if needed (avoid FileExistsError)
            base_backup = p.with_suffix(p.suffix + '.bak')
            backup = base_backup
            idx = 1
            while backup.exists():
                backup = p.with_suffix(p.suffix + f'.bak{idx}')
                idx += 1
            try:
                p.rename(backup)
            except Exception:
                # fallback: copy contents to backup path and continue
                try:
                    backup.write_text(text, encoding='utf-8')
                except Exception:
                    # give up on this file
                    continue
            # write filtered lines back
            filtered = '\n'.join([ln for ln in new_lines if ln is not None]) + '\n'
            p.write_text(filtered, encoding='utf-8')
            changed_files.append((p, backup))

# Summary
if not changed_files:
    print('No changes made — no obvious unused explicit imports found.')
else:
    print('Modified files:')
    for f, b in changed_files:
        print(f"- {f} (backup: {b.name})")
    print('\nBackups have .bak suffix. Review changes and run Gradle build.')
