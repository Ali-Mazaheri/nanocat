# nanocat

Syntax-highlight files or piped `stdin` directly in your terminal using GNU nano's `.nanorc` syntax definitions—so your CLI output matches the exact colours you see inside `nano`.

Zero external dependencies (Python 3 standard library only).

## Features

- Evaluates `.nanorc` `color` and `icolor` rules (including multi-line `start=... end=...` blocks) in nano's exact top-to-bottom precedence order
- Reads `~/.nanorc` `include` directives and `set numbercolor` theme settings automatically
- Line numbers (`-n` / `--number`) and line-range slicing (`-r 10:40`)
- Auto-paging through `less -R` for long files (`--paging=auto|always|never`) and smart TTY colour detection (`--color=auto|always|never`)

## Installation

### Quick Install (One-Liner)

```bash
curl -fsSL https://githubusercontent.com | bash
```

### Via pipx or uv

```bash
pipx install git+https://github.com
```

### Manual Install

```bash
git clone https://github.com
mkdir -p ~/.local/bin
ln -sf "\$(pwd)/nanocat/nanocat" ~/.local/bin/nanocat
```

## Usage

```bash
# Highlight a file directly (auto-detects extension or shebang)
nanocat path/to/file.tsx

# Show line numbers and slice a specific line range (e.g., lines 10 to 40)
nanocat -n -r 10:40 path/to/file.go

# Highlight piped stdin using a specific language
grep -n "func " main.go | nanocat -l go

# List all detected .nanorc syntax definitions
nanocat --list
```
