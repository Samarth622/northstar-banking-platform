# Automated code review

The repository includes a read-only [Deep Code Review Copilot agent](../.github/agents/deep-code-review.agent.md), its reusable [review skill](../.github/skills/deep-code-review/SKILL.md), and a GitHub Actions workflow that runs on every branch push.

## What runs

- On a Windows x64 self-hosted GitHub Actions runner, the workflow runs `mvnw.cmd -B verify` when `pom.xml` exists. Until the Maven project and wrapper are added, this check is reported as skipped.
- The workflow sends the pushed diff to the Ollama service on the runner machine. It publishes the review in the Actions run summary and updates a single comment if the pushed commit belongs to an open pull request.
- The final check fails if Maven verification or the Ollama review fails. A truncated diff is clearly identified in the review report.

The review is advisory, not a guarantee that every defect will be found. Review the findings and run the project's checks before merging.

## Configure a self-hosted runner

1. In GitHub, open the repository's **Settings → Actions → Runners → New self-hosted runner** and follow GitHub's Windows instructions. Register it for this repository and keep the default `self-hosted`, `windows`, and `x64` labels.
2. Keep the runner machine online while pushes are expected. Install the JDK version required by the project's `pom.xml` when it is added. Do not expose the runner to untrusted contributors: Maven verification executes build plugins and tests from the checked-out repository.
3. Install Ollama on the runner machine, start its local service, and pull the model you want to use. Confirm `http://localhost:11434/api/tags` is reachable from the runner's Windows account.
4. In **Settings → Secrets and variables → Actions → Variables**, add:
   - `OLLAMA_MODEL`: the exact installed model name returned by `/api/tags` (for example, `qwen2.5-coder:14b` only if you have installed it).
   - `OLLAMA_HOST` (optional): the local Ollama API URL; defaults to `http://localhost:11434`.
5. Ensure Actions' repository token can read contents and write issue and pull-request comments. The workflow declares these minimum permissions; organization policy may restrict them.
6. Push a commit and inspect **Actions → Push review**. Check both the Actions summary and, if applicable, the pull-request comment.

The review diff is sent only to the configured Ollama endpoint on the self-hosted runner; it is not sent by this workflow to a hosted AI API. The runner still downloads the referenced GitHub Actions and must communicate with GitHub. Protect the runner and repository, and do not use this workflow for pushes from untrusted contributors or public-fork code.
