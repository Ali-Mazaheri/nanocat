# nanocat

Syntax-highlight files or piped `stdin` directly in your terminal using GNU nano's `.nanorc` syntax definitions—so your CLI output matches the exact colours you see inside `nano`.

Zero external dependencies (Python 3 standard library only).

## Features

- Evaluates `.nanorc` `color` and `icolor` rules (including multi-line `start=... end=...` blocks) in nano's exact top-to-bottom precedence order
- Reads `~/.nanorc` `include` directives and `numbercolor` / `titlecolor` theme settings (plus `--theme` overrides)
- Line numbers (`-n` / `--number`), Git diff gutter markers (`-g` / `--git`), and line-range slicing (`-r 10:40`)
- Soft-wrapping (`-w` / `--wrap`), multi-file headers, and auto-paging through `less -R` (`--paging=auto|always|never`)

## Installation

### Python (`pipx` / `pip`)

```bash
pipx install nanocat-cli
```

### Debian / Ubuntu (`.deb`)

Download `nanocat_0.4.4_all.deb` from the [Releases](https://github.comAli-Mazaheri/nanocat/releases) page and run:

```bash
sudo apt install ./nanocat_0.4.4_all.deb
```

### TypeScript / JavaScript (`npm`)

```bash
npm install @ali.mazaheri/nanocat
```

### Quick Install Script (Linux / macOS)

```bash
curl -fsSL https://githubusercontent.comAli-Mazaheri/nanocat/main/install.shAli-Mazaheri/nanocat/main/install.sh | bash
```

### Standalone (Copy Into Any Project or Container)

Because `nanocat` is a single file with zero external dependencies, you can copy it directly into your repository, `bin/` folder, or Docker image:

```bash
curl -fsSL https://githubusercontent.com{USER_REPO}/main/nanocat -o ./nanocat
chmod +x ./nanocat
./nanocat -n path/to/file.py
```

## CLI Usage

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

## TypeScript / JavaScript SDK Usage

```ts
import { parseNanorc, highlight } from "@ali.mazaheri/nanocat";

const syntaxes = parseNanorc(nanorcFileContent);
const ansiOutput = highlight(code, syntaxes[0], "ansi");
const htmlOutput = highlight(code, syntaxes[0], "html");
```
