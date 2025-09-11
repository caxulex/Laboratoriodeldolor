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
import argparse
from pathlib import Path
from PIL import Image

DENSITY_SCALE = {
    "mdpi": 1.0,
    "hdpi": 1.5,
    "xhdpi": 2.0,
    "xxhdpi": 3.0,
    "xxxhdpi": 4.0,
}

NAMES = ["boy_front", "boy_back", "girl_front", "girl_back"]

def ensure_dir(p: Path):
    p.mkdir(parents=True, exist_ok=True)

def ensure_res_dir(root: Path, bucket: str) -> Path:
    p = root / f"drawable-{bucket}"
    p.mkdir(parents=True, exist_ok=True)
    return p

def resize_to_density(img: Image.Image, base_scale: float, target_scale: float) -> Image.Image:
    ratio = target_scale / base_scale
    w = max(1, int(round(img.width * ratio)))
    h = max(1, int(round(img.height * ratio)))
    return img.resize((w, h), Image.Resampling.LANCZOS)

def main():
    ap = argparse.ArgumentParser(description="Generate density-specific WebP variants for body images.")
    ap.add_argument("--module", default="app", help="Module dir (default: app)")
    ap.add_argument("--source-dir", default="app/src/main/res/drawable-nodpi", help="Source images directory")
    ap.add_argument("--base-density", default="xxhdpi", choices=list(DENSITY_SCALE.keys()), help="Assume source PNGs are this density")
    ap.add_argument("--lossless", action="store_true", help="Save WebP lossless")
    ap.add_argument("--delete-source-png", action="store_true", help="Delete original PNGs after conversion")
    args = ap.parse_args()

    project = Path(".").resolve()
    res_root = project / args.module / "src" / "main" / "res"
    src_dir = project / args.source_dir

    if not src_dir.exists():
        print(f"[ERR] Source dir not found: {src_dir}", file=sys.stderr)
        sys.exit(1)

    base_scale = DENSITY_SCALE[args.base_density]
    total = 0

    for name in NAMES:
        # find PNG or WebP source by name in src_dir
        src = None
        for ext in (".png", ".PNG", ".webp", ".WEBP"):
            cand = src_dir / f"{name}{ext}"
            if cand.exists():
                src = cand
                break
        if not src:
            print(f"[WARN] Source not found for {name} in {src_dir}, skipping.")
            continue

        with Image.open(src) as im:
            im = im.convert("RGBA")
            for bucket, scale in DENSITY_SCALE.items():
                out_dir = ensure_res_dir(res_root, bucket)
                out_path = out_dir / f"{name}.webp"
                out_img = resize_to_density(im, base_scale, scale)
                out_img.save(out_path, format="WEBP", lossless=args.lossless, method=6, quality=95)
                print(f"[OK] Wrote {out_path} ({out_img.width}x{out_img.height})")
                total += 1

        if args.delete_source_png and src.suffix.lower() == ".png":
            try:
                src.unlink()
                print(f"[OK] Deleted source {src}")
            except Exception as e:
                print(f"[WARN] Could not delete {src}: {e}")

    print(f"[DONE] Wrote {total} WebP assets.")

if __name__ == "__main__":
    main()
