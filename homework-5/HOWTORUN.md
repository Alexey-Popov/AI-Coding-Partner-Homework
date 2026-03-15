# How to Run — Homework 5 MCP Setup

## Prerequisites

- Node.js 18+ (for `npx`)
- Claude Code CLI installed
- Python 3.10+ (for Task 4 custom MCP server)

---

## Task 2: Filesystem MCP Setup

### Step 1 — No install needed

The server runs via `npx` — no global install required. `npx` downloads and runs `@modelcontextprotocol/server-filesystem` on demand.

To verify it works manually:

```bash
npx -y @modelcontextprotocol/server-filesystem /Users/levik/Documents/spd/AI-Coding-Partner-Homework
```

You should see the server start and listen on stdio.

### Step 2 — Register with Claude Code

The `.mcp.json` file in this directory is automatically picked up by Claude Code when you run it from this folder:

```bash
cd homework-5
claude
```

Claude Code reads `.mcp.json` and starts the configured MCP servers as subprocesses.

### Step 3 — Verify the server is running

Inside Claude Code, run:

```
/mcp
```

You should see `filesystem` listed as a connected server.

### Step 4 — Interact with the filesystem

Example prompts to test the Filesystem MCP:

```
List all files in the AI-Coding-Partner-Homework directory
```

```
Read the README.md at the root of AI-Coding-Partner-Homework
```

```
Summarize the directory structure of homework-4
```

### Step 5 — Capture screenshot

Take a screenshot of the MCP response and save it to:

```
docs/screenshots/filesystem-mcp-result.png
```

---

## Task 1: GitHub MCP Setup

### Step 1 — Create a GitHub Personal Access Token

1. Go to https://github.com/settings/tokens
2. Generate a **classic** token with scopes: `repo`, `read:org`, `read:user`
3. Copy the token

### Step 2 — Set environment variable

```bash
export GITHUB_PERSONAL_ACCESS_TOKEN=ghp_your_token_here
```

Or add it to your shell profile (`~/.zshrc`).

### Step 3 — Update `.mcp.json`

Replace `<YOUR_GITHUB_TOKEN>` in `.mcp.json` with your actual token, or rely on the environment variable (the server reads `GITHUB_PERSONAL_ACCESS_TOKEN` from the environment automatically).

### Step 4 — Run Claude Code

```bash
cd homework-5
claude
```

Test with:

```
List my recent pull requests in AI-Coding-Partner-Homework
```

---

## Task 3: Jira MCP Setup

### Step 1 — Generate a Jira API Token

1. Go to https://id.atlassian.com/manage-profile/security/api-tokens
2. Click **Create API token**
3. Give it a label (e.g. `claude-mcp`) and copy the token

### Step 2 — Add Jira MCP server to `.mcp.json`

Add the following entry under `mcpServers` in your `.mcp.json`:

```json
"jira": {
  "command": "npx",
  "args": [
    "-y",
    "@sooperset/mcp-atlassian"
  ],
  "env": {
    "JIRA_URL": "https://yourcompany.atlassian.net",
    "JIRA_USERNAME": "your-email@example.com",
    "JIRA_API_TOKEN": "your_api_token_here"
  }
}
```

Replace the values:
- `JIRA_URL` — your Atlassian instance URL
- `JIRA_USERNAME` — your Atlassian account email
- `JIRA_API_TOKEN` — the token generated in Step 1

### Step 3 — Run Claude Code

```bash
cd homework-5
claude
```

### Step 4 — Verify the server is connected

Inside Claude Code, run:

```
/mcp
```

You should see `jira` listed as a connected server alongside `filesystem` and `github`.

### Step 5 — Query the last 5 bug tickets

Use this prompt inside Claude Code:

```
Give me the Jira tickets of the last 5 bugs on project <YOUR_PROJECT_KEY>
```

Replace `<YOUR_PROJECT_KEY>` with your actual Jira project key (e.g. `PROJ`, `DEV`, `BACK`).

Claude will call the Jira MCP tool and return the ticket numbers and metadata.

### Step 6 — Capture screenshot

Take a screenshot of the MCP request and response and save it to:

```
docs/screenshots/jira-mcp-result.png
```

---

## Task 4: Custom MCP Server (FastMCP)

### Step 1 — Install Python dependencies

Create a virtual environment inside `custom-mcp-server/` and install dependencies:

```bash
cd custom-mcp-server
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
```

> **Note:** Do NOT use system `pip3 install` directly — macOS protects system Python (PEP 668). Always use a venv.

### Step 2 — Verify the server starts

From the `custom-mcp-server/` directory:

```bash
.venv/bin/python server.py
```

You should see FastMCP start without errors. Press `Ctrl+C` to stop — Claude Code will manage the process automatically.

### Step 3 — Connect via `.mcp.json`

The `.mcp.json` uses the venv Python directly (no PATH dependency):

```json
"lorem-ipsum": {
  "command": "custom-mcp-server/.venv/bin/python",
  "args": ["custom-mcp-server/server.py"]
}
```

Claude Code will automatically start this server when launched from the `homework-5/` directory.

### Step 4 — Run Claude Code

```bash
cd homework-5
claude
```

Verify the server is connected:

```
/mcp
```

You should see `lorem-ipsum` listed alongside the other servers.

### Step 5 — Test the `read` tool

Use this prompt inside Claude Code:

```
Use the read tool with word_count=20
```

Expected result: Claude returns exactly 20 words from `lorem-ipsum.md`.

You can also test the resource directly:

```
Read from lorem://ipsum/20
```

### Step 6 — Capture screenshot

Save the result to:

```
docs/screenshots/custom-mcp-read-tool-result.png
```
