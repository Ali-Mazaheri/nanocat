#!/usr/bin/env bash
set -euo pipefail

VERSION="${1:-0.4.0}"
PKG_DIR="dist/nanocat_${VERSION}_all"

rm -rf "$PKG_DIR"
mkdir -p "$PKG_DIR/DEBIAN"
mkdir -p "$PKG_DIR/usr/bin"
mkdir -p "$PKG_DIR/usr/share/doc/nanocat"

cp nanocat "$PKG_DIR/usr/bin/nanocat"
chmod 755 "$PKG_DIR/usr/bin/nanocat"
cp README.md LICENSE "$PKG_DIR/usr/share/doc/nanocat/"

cat << CONTROL > "$PKG_DIR/DEBIAN/control"
Package: nanocat
Version: ${VERSION}
Section: utils
Priority: optional
Architecture: all
Depends: python3 (>= 3.10)
Maintainer: Ali Mazaheri <alimazaheri@yahoo.com>
Homepage: https://github.com
Description: Syntax-highlight files or stdin using GNU nano (.nanorc) rules
 Zero-dependency CLI tool and multi-language SDK that renders terminal
 output using GNU nano's .nanorc syntax definitions.
CONTROL

dpkg-deb --build "$PKG_DIR"
echo "Built dist/nanocat_${VERSION}_all.deb"
