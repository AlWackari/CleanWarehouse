# Contributing Guidelines

Thank you for your interest in contributing to this project! To keep the codebase maintainable, readable, and easy to test, please follow the guidelines below.

---

## Code Quality Guidelines

### 1. Method Size and Complexity
Every method should follow the Single Responsibility Principle and do only one thing well.

- **Instruction & Length Limit:** Keep methods compact, ideally under **10-15 lines of code** (or around **5–10 executable statements**).
- **Scoping** If two or more methods basically result in a similar output starting from different input that means those methods must be shipped with the overload technique.
- **Nesting** When overloads criteria are met, it's highly encouraged to create a private Least Common Multiple (LCM) method able to serve all the overloading method. 
- **Refactoring:** If methods handles multiple levels of abstraction, extract sub-routines into private helper methods. The same apply for all-around methods in higher levels of architecture (input): when they start to face uncontrolled grow in term of lines decompose the scope with specialized new supporting class/methods (nested class maybe).
- **Complexity:** Minimize nested conditionals (`if`, loops, `switch`). Prefer early returns or functional compositions to keep logic flat and readable.

### 2. Testing approach
- Every new Domain element must have its own collector of unit test case
- Unit test case must reproduce the failure and check for the application catching the failure
- Every new feature must be checked against an existing integration test scenario, they should already cover the architecture chain of events. If that's not the case it means you're extending the architecture, that's fine but please be complaint with port & adapters. 

### 3. Verification
Before submitting a Pull Request:
- Run local static analysis and formatting checks.
- Ensure all existing and new unit tests pass.

### 4. Pull Request Process
1. Create a focused branch (`feature/feature-name` or `fix/issue-name`).
2. Make atomic commits with descriptive messages.
3. Detail your changes and how to verify them in the PR description.