# nanocat

Syntax-highlight files or piped `stdin` directly in your terminal using GNU nano's `.nanorc` syntax definitions—so your CLI output matches the exact colours you see inside `nano`.

Zero external dependencies (Python 3 standard library only).

## Features

- Evaluates `.nanorc` `color` and `icolor` rules (including multi-line `start=... end=...` blocks) in nano's exact top-to-bottom precedence order
- Reads `~/.nanorc` `include` directives and `numbercolor` / `titlecolor` theme settings (plus `--theme` overrides)
- Line numbers (`-n` / `--number`), Git diff gutter markers (`-g` / `--git`), and line-range slicing (`-r 10:40`)
- Soft-wrapping (`-w` / `--wrap`), multi-file headers, and auto-paging through `less -R` (`--paging=auto|always|never`)

## Installation

### Quick Install (One-Liner)

```bash
curl -fsSL https://githubusercontent.com | bash
```

### Via pipx or uv

```bash
pipx install nanocat-cli
```

### Manual Install

```bash
git clone https://github.com
mkdir -p ~/.local/bin
ln -sf "\$(pwd)/nanocat/nanocat" ~/.local/bin/nanocat
```

## Usage

```bash
# Highlight one or more files directly (auto-detects extension or shebang)
nanocat path/to/file.tsx
nanocat -n -g src/App.tsx src/index.tsx

# Show line numbers, git diff markers, and slice lines 10 to 40
nanocat -n -g -r 10:40 path/to/file.go

# Highlight piped stdin using a specific language
grep -n "func " main.go | nanocat -l go

# List all detected .nanorc syntax definitions
nanocat --list
```
