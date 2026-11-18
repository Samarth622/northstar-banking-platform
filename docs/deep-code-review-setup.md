# Run a local code review

The repository includes a read-only [Deep Code Review Copilot agent](../.github/agents/deep-code-review.agent.md), its reusable [review skill](../.github/skills/deep-code-review/SKILL.md), and a PowerShell script that reviews the repository with Ollama on your machine.

There is no GitHub Actions workflow for this review. Pushes do not start an automatic check, and no GitHub self-hosted runner is required.

When a Maven project is present, the script also runs `mvnw.cmd -B verify` before the AI review. A failed Maven verification is reported in the review and causes the script to exit with an error after saving the report.

## Run from VS Code

1. Start Ollama on your computer and install a model if needed:

   ```powershell
   ollama list
   ollama pull fredrezones55/qwen3.5-opus:4b
   ```

2. Open the repository in VS Code and open a PowerShell integrated terminal at the repository root.
3. Run:

   ```powershell
   .\review.cmd
   ```

   This short command runs the review with `fredrezones55/qwen3.5-opus:4b`. The launcher uses a temporary execution-policy override for that PowerShell process only; it does not change your saved system policy. To choose another installed model, invoke the script directly:

   ```powershell
   powershell.exe -NoProfile -ExecutionPolicy Bypass -File ".\.github\scripts\review-codebase.ps1" -Model "your-installed-model"
   ```

   Optional: override the local Ollama API address with `-OllamaHost "http://localhost:11434"`.

4. Read the report saved under the repository root's `logs` folder. Its path is printed as soon as the review starts and updated after each batch, so a failed run still leaves a partial report with its status and error. Maven verification results, when run, are included. Git ignores `logs/`, so reports do not appear in Source Control and are not committed; open the folder in VS Code Explorer to view them.

The script sends reviewable source, configuration, and documentation files to the Ollama endpoint on your machine in bounded batches; it does not send code to a hosted AI service. It excludes Git-ignored files, generated/build directories, `.env*` files, and binary files; files larger than 250 KB or with an exceptionally long line are listed as not reviewed. Batch-by-batch review can miss defects that depend on relationships between files in separate batches. The report lists the number of files and batches so you can understand the review scope.

The review is advisory, not a guarantee that every defect will be found. Run the project's tests and review the findings before merging.
