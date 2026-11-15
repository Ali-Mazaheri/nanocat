#!/usr/bin/env bash
set -euo pipefail

INSTALL_DIR="${HOME}/.local/bin"
RAW_URL="https://raw.githubusercontent.com/Ali-Mazaheri/nanocat/main/nanocat"

mkdir -p "$INSTALL_DIR"
curl -fsSL "$RAW_URL" -o "$INSTALL_DIR/nanocat"
chmod +x "$INSTALL_DIR/nanocat"

echo "Installed nanocat to $INSTALL_DIR/nanocat"
if ! command -v nanocat >/dev/null 2>&1; then
    echo "Note: Add $INSTALL_DIR to your PATH (e.g., export PATH=\"\$HOME/.local/bin:\$PATH\")"
fi
