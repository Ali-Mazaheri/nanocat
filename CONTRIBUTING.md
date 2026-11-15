# Contributing to nanocat

Contributions, bug reports, and feature requests are welcome!

## Guidelines
- Keep `nanocat` dependency-free (Python 3 standard library only).
- Test your changes before opening a Pull Request:
  ```bash
  python3 -m py_compile nanocat
  ./nanocat --list
  ./nanocat nanocat
  ```
- Open a Pull Request against `main` with a clear summary of what your change fixes or adds.
