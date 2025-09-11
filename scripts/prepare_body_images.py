#!/usr/bin/env python3
"""
Generate density-specific (mdpi–xxxhdpi) WebP assets for body silhouettes
from the existing nodpi PNG sources (boy_front/back, girl_front/back).

- Preserves sharpness: resizes from a high-res base (assumed xxxhdpi).
- Reduces size: outputs WebP (lossless) which typically compresses better than PNG.
- Output files:
  res/drawable-<density>/<name>.webp

Run:
  python scripts/prepare_body_images.py

Requirements:
  pip install pillow
"""
from __future__ import annotations
import os
import sys
from pathlib import Path

try:
    from PIL import Image
except Exception as e:
    print("[ERROR] Pillow is required. Install with: pip install pillow", file=sys.stderr)
    raise

ROOT = Path(__file__).resolve().parents[1]
RES_DIR = ROOT / 'app' / 'src' / 'main' / 'res'
SRC_DIR = RES_DIR / 'drawable-nodpi'
FILES = ['boy_front.png', 'boy_back.png', 'girl_front.png', 'girl_back.png']

# Assume current nodpi PNGs are xxxhdpi base (scale 4.0 vs mdpi)
BASE_SCALE = 4.0
DENSITIES = {
    'mdpi': 1.0 / BASE_SCALE,
    'hdpi': 1.5 / BASE_SCALE,
    'xhdpi': 2.0 / BASE_SCALE,
    'xxhdpi': 3.0 / BASE_SCALE,
    'xxxhdpi': 4.0 / BASE_SCALE,
}

def ensure_dir(p: Path):
    p.mkdir(parents=True, exist_ok=True)


def generate_variants(src_file: Path):
    name = src_file.stem  # e.g., boy_front
    with Image.open(src_file) as im:
        im = im.convert('RGBA')  # ensure alpha preserved
        w0, h0 = im.size
        # Using xxxhdpi as base; no upscaling beyond original
        for dens, scale in DENSITIES.items():
            target_w = int(round(w0 * scale))
            target_h = int(round(h0 * scale))
            if target_w <= 0 or target_h <= 0:
                print(f"[SKIP] {name} -> {dens}: invalid size {target_w}x{target_h}")
                continue
            # Avoid unnecessary upscaling (should not happen with BASE_SCALE=4.0)
            if target_w > w0 or target_h > h0:
                print(f"[SKIP] {name} -> {dens}: would upscale beyond source; skipping")
                continue
            out_dir = RES_DIR / f'drawable-{dens}'
            ensure_dir(out_dir)
            out_path = out_dir / f'{name}.webp'
            # Use high-quality downscale with LANCZOS
            resized = im.resize((target_w, target_h), Image.LANCZOS)
            # Save as lossless WebP to preserve line art crispness
            try:
                resized.save(out_path, format='WEBP', lossless=True, quality=100, method=6)
                print(f"[OK] {name} -> {dens}: {target_w}x{target_h} -> {out_path.relative_to(ROOT)}")
            except Exception as e:
                # Fallback to PNG if webp not supported in environment
                fallback = out_dir / f'{name}.png'
                resized.save(fallback, format='PNG', optimize=True)
                print(f"[FALLBACK PNG] {name} -> {dens}: {target_w}x{target_h} -> {fallback.relative_to(ROOT)} ({e})")


def main():
    if not SRC_DIR.exists():
        print(f"[ERROR] Source dir missing: {SRC_DIR}", file=sys.stderr)
        sys.exit(1)
    missing = [f for f in FILES if not (SRC_DIR / f).exists()]
    if missing:
        print(f"[ERROR] Missing source files: {missing}", file=sys.stderr)
        sys.exit(2)

    for fname in FILES:
        generate_variants(SRC_DIR / fname)

    print("\n[INFO] Done. You can remove nodpi PNGs to rely on density-specific variants.")


if __name__ == '__main__':
    main()
