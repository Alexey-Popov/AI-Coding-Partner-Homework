# Claude Code: Skills & Sub-Agent Runs

This guide explains how to enable and configure **Skills** (plugins) and **Sub-Agent runs** for Claude Code in this GitHub repository.

---

## Table of Contents

- [Overview](#overview)
- [Skills (Plugins)](#skills-plugins)
  - [What Are Skills?](#what-are-skills)
  - [How to Enable Skills](#how-to-enable-skills)
  - [Example: Code Review Skill](#example-code-review-skill)
  - [Adding Custom Skills](#adding-custom-skills)
- [Sub-Agent Runs](#sub-agent-runs)
  - [What Are Sub-Agents?](#what-are-sub-agents)
  - [How Sub-Agents Are Triggered](#how-sub-agents-are-triggered)
  - [Enabling Sub-Agent Tools](#enabling-sub-agent-tools)
  - [Sub-Agent Permissions](#sub-agent-permissions)
- [Workflow Configuration Reference](#workflow-configuration-reference)
- [Existing Workflows in This Repo](#existing-workflows-in-this-repo)
- [Troubleshooting](#troubleshooting)

---

## Overview

This repository uses [Claude Code Action](https://github.com/anthropics/claude-code-action) — an official Anthropic GitHub Action that brings Claude AI into your GitHub workflow. It supports two advanced features:

| Feature | Description |
|---------|-------------|
| **Skills** | Reusable slash-command plugins that give Claude specialized capabilities (e.g., `/code-review`, `/security-audit`) |
| **Sub-Agents** | Claude spawns specialized sub-processes to handle complex, multi-step tasks autonomously (e.g., Bash agents, Explore agents, Plan agents) |

---

## Skills (Plugins)

### What Are Skills?

Skills are pre-built, reusable command modules that extend what Claude can do inside a workflow. They are loaded via **plugin marketplaces** — Git repositories that host skill definitions. A skill is invoked using a slash command like `/skill-name:action`.

This repo already uses the built-in `code-review` skill in `.github/workflows/claude-code-review.yml`.

### How to Enable Skills

To enable skills, add two parameters to the `anthropics/claude-code-action@v1` step in your workflow:

```yaml
- name: Run Claude Code
  uses: anthropics/claude-code-action@v1
  with:
    claude_code_oauth_token: ${{ secrets.CLAUDE_CODE_OAUTH_TOKEN }}

    # 1. Specify the marketplace(s) where skill packages are hosted
    plugin_marketplaces: 'https://github.com/anthropics/claude-code.git'

    # 2. Specify which skill(s) to load: '<skill-name>@<branch-or-tag>'
    plugins: 'code-review@claude-code-plugins'

    # 3. Invoke the skill via prompt
    prompt: '/code-review:code-review ${{ github.repository }}/pull/${{ github.event.pull_request.number }}'
```

**Parameter breakdown:**

| Parameter | Purpose | Example |
|-----------|---------|---------|
| `plugin_marketplaces` | Git URL of the repository hosting skill definitions | `https://github.com/anthropics/claude-code.git` |
| `plugins` | `<skill-name>@<ref>` — name and branch/tag of the skill | `code-review@claude-code-plugins` |
| `prompt` | The slash command to invoke the skill, with any arguments | `/code-review:code-review owner/repo/pull/42` |

> **Note:** Multiple marketplaces and plugins can be specified as comma-separated or newline-separated values.

### Example: Code Review Skill

The `code-review` skill (already active in this repo) automatically reviews every PR. Here is the full workflow:

```yaml
# .github/workflows/claude-code-review.yml
name: Claude Code Review

on:
  pull_request:
    types: [opened, synchronize, ready_for_review, reopened]

jobs:
  claude-review:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      pull-requests: read
      issues: read
      id-token: write

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4
        with:
          fetch-depth: 1

      - name: Run Claude Code Review
        uses: anthropics/claude-code-action@v1
        with:
          claude_code_oauth_token: ${{ secrets.CLAUDE_CODE_OAUTH_TOKEN }}
          plugin_marketplaces: 'https://github.com/anthropics/claude-code.git'
          plugins: 'code-review@claude-code-plugins'
          prompt: '/code-review:code-review ${{ github.repository }}/pull/${{ github.event.pull_request.number }}'
```

### Adding Custom Skills

To add another skill (e.g., a security audit skill):

1. Find or create a skill repository that defines the skill using the Claude Code plugin format.
2. Add it to your workflow:

```yaml
plugin_marketplaces: |
  https://github.com/anthropics/claude-code.git
  https://github.com/your-org/your-custom-skills.git

plugins: |
  code-review@claude-code-plugins
  security-audit@main
```

3. Invoke it via prompt:

```yaml
prompt: '/security-audit:run ${{ github.repository }}'
```

---

## Sub-Agent Runs

### What Are Sub-Agents?

When Claude handles a complex task, it can **spawn sub-agents** — specialized processes that work autonomously on a specific sub-task and report results back. This is the "multi-agent" capability of Claude Code.

Available built-in sub-agent types:

| Agent Type | Use Case |
|------------|---------|
| `Bash` | Run shell commands, git operations, terminal tasks |
| `general-purpose` | Research, multi-step tasks, codebase searches |
| `Explore` | Fast codebase exploration, file pattern matching, keyword search |
| `Plan` | Architectural planning, implementation strategy design |
| `claude-code-guide` | Questions about Claude Code itself |

Sub-agents are launched automatically by Claude when it determines that the task benefits from delegation. You do not invoke sub-agents directly — Claude decides when to use them.

### How Sub-Agents Are Triggered

Sub-agents are triggered when Claude encounters complex tasks, such as:

- Searching a large codebase for a pattern across many files
- Running tests and fixing failures iteratively
- Planning a multi-file refactor before implementing it

Example comment that would trigger sub-agent usage:

```
@claude Search the entire codebase for all usages of the deprecated `getUserById` function
and replace them with the new `findUserById` function, then run the tests.
```

Claude will internally spawn Explore agents to search, then Bash agents to make edits and run tests.

### Enabling Sub-Agent Tools

Sub-agent capabilities depend on the **tools** Claude is allowed to use. Tools are controlled by the `claude_args` parameter:

```yaml
- name: Run Claude Code
  uses: anthropics/claude-code-action@v1
  with:
    claude_code_oauth_token: ${{ secrets.CLAUDE_CODE_OAUTH_TOKEN }}

    # Grant Claude (and its sub-agents) access to specific tools
    claude_args: '--allowedTools Bash,Read,Write,Edit,Glob,Grep,Task'
```

**Common tool allowlists:**

| Scenario | Recommended `--allowedTools` |
|----------|------------------------------|
| Read-only code review | `Read,Glob,Grep` |
| Full code changes | `Bash,Read,Write,Edit,Glob,Grep,Task` |
| Git operations only | `Bash(git *)` |
| PR operations | `Bash(gh pr:*)` |

> **Security tip:** Use the narrowest allowlist that still lets Claude complete the task.

### Sub-Agent Permissions

Sub-agents inherit the GitHub Actions permissions of the parent job. For sub-agents to perform write operations (push code, create files), the workflow job needs:

```yaml
jobs:
  claude:
    runs-on: ubuntu-latest
    permissions:
      contents: write       # Required to push commits
      pull-requests: write  # Required to comment on PRs
      issues: write         # Required to comment on issues
      id-token: write       # Required for OIDC authentication
      actions: read         # Required to read CI results
```

The minimal read-only configuration:

```yaml
permissions:
  contents: read
  pull-requests: read
  issues: read
  id-token: write
```

---

## Workflow Configuration Reference

Full set of available parameters for `anthropics/claude-code-action@v1`:

```yaml
- name: Run Claude Code
  uses: anthropics/claude-code-action@v1
  with:
    # Required: OAuth token for Claude (stored as a GitHub secret)
    claude_code_oauth_token: ${{ secrets.CLAUDE_CODE_OAUTH_TOKEN }}

    # Optional: Custom system prompt for Claude's behavior in this workflow
    prompt: 'Review the PR and suggest improvements.'

    # Optional: CLI arguments passed to Claude Code
    # See https://code.claude.com/docs/en/cli-reference
    claude_args: '--allowedTools Bash,Read,Write,Edit,Glob,Grep,Task'

    # Optional: Additional GitHub permissions
    additional_permissions: |
      actions: read

    # Optional: Skill marketplace(s)
    plugin_marketplaces: 'https://github.com/anthropics/claude-code.git'

    # Optional: Skills to load (name@branch)
    plugins: 'code-review@claude-code-plugins'

    # Optional: Timeout in minutes (default: 30)
    timeout_minutes: '60'
```

---

## Existing Workflows in This Repo

This repo has two Claude Code workflows configured in `.github/workflows/`:

### 1. `claude.yml` — Interactive Assistant

**Trigger:** Any comment or issue mentioning `@claude`

**Capabilities:** Answers questions, reviews code on demand, implements changes when asked, runs as a full agent with sub-agent support.

**How to use:**
- Open an issue and mention `@claude` in the body, or
- Comment `@claude` followed by your request on any PR or issue

**Example:**
```
@claude Review this PR and check for any security issues.
@claude Fix the failing tests in homework-4/
@claude Explain how the user authentication works in this codebase.
```

### 2. `claude-code-review.yml` — Automatic PR Reviewer

**Trigger:** Every PR that is opened, updated, or marked ready for review.

**Capabilities:** Automated code review using the `code-review` skill. Posts review feedback as a comment on the PR.

**How it works:** No manual trigger needed. Claude automatically reviews every PR using the `code-review@claude-code-plugins` skill from the Anthropic marketplace.

---

## Prerequisites

Before skills and sub-agents work, ensure the following is set up:

### 1. GitHub Secret

The `CLAUDE_CODE_OAUTH_TOKEN` secret must be set in your repository:

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Click **New repository secret**
3. Name: `CLAUDE_CODE_OAUTH_TOKEN`
4. Value: Your Claude Code OAuth token from [claude.ai/settings](https://claude.ai/settings)

### 2. Workflow Permissions

Ensure workflows have the right permissions. Go to **Settings** → **Actions** → **General** → **Workflow permissions** and select:

- "Read and write permissions" (if Claude needs to push code)
- Check "Allow GitHub Actions to create and approve pull requests"

---

## Troubleshooting

| Problem | Cause | Fix |
|---------|-------|-----|
| Claude doesn't respond to `@claude` | Workflow not triggered or secret missing | Check Actions tab for run logs; verify `CLAUDE_CODE_OAUTH_TOKEN` secret |
| `plugin_marketplaces` fails | Network access restricted or wrong URL | Verify the marketplace URL is a public Git repo |
| Sub-agent can't write files | Insufficient permissions | Add `contents: write` to job permissions |
| Claude can't run bash commands | Tool not in allowlist | Add `Bash` to `--allowedTools` in `claude_args` |
| Workflow not running on PRs | Wrong trigger configuration | Check `on:` section of the workflow file |
| `CLAUDE_CODE_OAUTH_TOKEN` error | Secret not set or expired | Regenerate token at claude.ai and update the secret |

---

## Further Reading

- [Claude Code Action Documentation](https://github.com/anthropics/claude-code-action/blob/main/docs/usage.md)
- [Claude Code CLI Reference](https://code.claude.com/docs/en/cli-reference)
- [Claude Code Action FAQ](https://github.com/anthropics/claude-code-action/blob/main/docs/faq.md)
- [Anthropics Claude Code Plugins](https://github.com/anthropics/claude-code/tree/claude-code-plugins)
