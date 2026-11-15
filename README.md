# nanocat

Syntax-highlight files or piped standard input directly in your terminal using GNU nano's `.nanorc` syntax definitions, so your CLI output matches the colours you see inside `nano`.

Zero external dependencies. Uses only the Python 3 standard library.

## Features

- Evaluates `.nanorc` `color` and `icolor` rules, including multi-line `start=... end=...` blocks, following nano's top-to-bottom rule precedence.
- Reads `~/.nanorc` `include` directives and `numbercolor` / `titlecolor` theme settings, with support for `--theme` overrides.
- Line numbers (`-n` / `--number`), Git diff gutter markers (`-g` / `--git`), and line-range slicing (`-r 10:40`).
- Soft wrapping (`-w` / `--wrap`), multi-file headers, and automatic paging through `less -R` (`--paging=auto|always|never`).

## Installation

### Python (`pipx` / `pip`)

```bash
pipx install nanocat-cli
```

### Debian / Ubuntu (`.deb`)

Download `nanocat_0.4.4_all.deb` from the [Releases](https://github.com/Ali-Mazaheri/nanocat/releases) page and install it:

```bash
sudo apt install ./nanocat_0.4.4_all.deb
```

### TypeScript / JavaScript (`npm`)

```bash
npm install @ali.mazaheri/nanocat
```

### Quick install script (Linux / macOS)

```bash
curl -fsSL https://raw.githubusercontent.com/Ali-Mazaheri/nanocat/main/install.sh | bash
```

### Standalone (copy into any project or container)

Because `nanocat` is a single file with zero external dependencies, you can copy it directly into your repository, `bin/` folder, or Docker image:

```bash
curl -fsSL https://raw.githubusercontent.com/Ali-Mazaheri/nanocat/main/nanocat -o ./nanocat
chmod +x ./nanocat
./nanocat -n path/to/file.py
```

## CLI usage

```bash
# Highlight one or more files directly (auto-detects extension or shebang)
nanocat path/to/file.tsx
nanocat -n -g src/App.tsx src/index.tsx

# Show line numbers, Git diff markers, and slice lines 10 to 40
nanocat -n -g -r 10:40 path/to/file.go

# Highlight piped standard input using a specific language
grep -n "func " main.go | nanocat -l go

# List all detected .nanorc syntax definitions
nanocat --list
```

## TypeScript / JavaScript SDK usage

```ts
import { parseNanorc, highlight } from "@ali.mazaheri/nanocat";

const syntaxes = parseNanorc(nanorcFileContent);
const ansiOutput = highlight(code, syntaxes[0], "ansi");
const htmlOutput = highlight(code, syntaxes[0], "html");
```1
