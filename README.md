# nanocat

Syntax-highlight files or piped `stdin` directly in your terminal using GNU nano ós `.nanorc` syntax definitionsâ€”so your CLI output matches the exact colours you see inside `nano`.

Zero external dependencies (Python 3 standard library only).

## Installation

```bash
git clone https://github.com/Ali-Mazaheri/nanocat.git
mkdir -p ~/.local/bin
ln -sf "$(pwd)/nanocat/nanocat" ~/.local/bin/nanocat
```

## Usage

```bash
# Highlight a file directly (auto-detects extension or shebang)
nanocat path/to/file.tsx

# Highlight piped stdin using a specific language
grep -n "func " main.go | nanocat -l go

# List all detected .nanorc syntax definitions
nanocat --list
```
