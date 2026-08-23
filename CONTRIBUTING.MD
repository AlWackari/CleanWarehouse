# Contributing Guidelines

Thank you for your interest in contributing to this project! To keep the codebase maintainable, readable, and easy to test, please follow the guidelines below.

---

## Code Quality Guidelines

### 1. Method Size and Complexity
Every method should follow the Single Responsibility Principle and do only one thing well.

- **Instruction & Length Limit:** Keep methods compact, ideally under **15–20 lines of code** (or around **10–15 executable statements**).
- **Refactoring:** If a method exceeds this threshold or handles multiple levels of abstraction, extract sub-routines into private helper methods.
- **Complexity:** Minimize nested conditionals (`if`, loops, `switch`). Prefer early returns or functional compositions to keep logic flat and readable.

### 2. Verification
Before submitting a Pull Request:
- Run local static analysis and formatting checks.
- Ensure all existing and new unit tests pass.

### 3. Pull Request Process
1. Create a focused branch (`feature/feature-name` or `fix/issue-name`).
2. Make atomic commits with descriptive messages.
3. Detail your changes and how to verify them in the PR description.