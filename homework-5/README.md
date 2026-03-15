# Homework 5: Configure MCP Servers

**Student**: Yevgen Polukov
**Course**: AI Coding Partner / MCP Configuration

---

## Overview

This homework demonstrates installation and configuration of MCP (Model Context Protocol) servers to connect AI assistants (Claude Code, Copilot) to external tools and data sources.

---

## Tasks Completed

- [x] Task 1: GitHub MCP
- [x] Task 2: Filesystem MCP
- [x] Task 3: Jira MCP
- [x] Task 4: Custom MCP Server (FastMCP)

---

## Task 1: GitHub MCP

The GitHub MCP server connects Claude to GitHub so it can list pull requests, summarize commits, read files from repositories, and more.

### Configuration (`.mcp.json`)

```json
"github": {
  "command": "npx",
  "args": ["-y", "@modelcontextprotocol/server-github"],
  "env": {
    "GITHUB_PERSONAL_ACCESS_TOKEN": "<YOUR_GITHUB_TOKEN>"
  }
}
```

### How It Works

- `@modelcontextprotocol/server-github` is the official GitHub MCP server.
- Authentication is handled via a Personal Access Token (PAT) passed as an environment variable — no hardcoded secrets.
- Claude can call tools like `list_pull_requests`, `get_file_contents`, `search_code` against your GitHub account.

### Interactions Performed

Prompt used: *"List my recent pull requests in AI-Coding-Partner-Homework"*

See `docs/screenshots/github-mcp-result.png` for the captured result.

---

## Task 2: Filesystem MCP

The Filesystem MCP server exposes a local directory to Claude so it can list files, read file contents, and summarize directory structure — without manual copy-paste.

### Configuration (`.mcp.json`)

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/Users/levik/Documents/spd/AI-Coding-Partner-Homework"
      ]
    }
  }
}
```

### How It Works

- `@modelcontextprotocol/server-filesystem` is the official MCP server for local filesystem access.
- The path argument restricts access to only that directory (sandbox).
- Claude can call tools like `list_directory`, `read_file`, `search_files` against it.

### Interactions Performed

See `docs/screenshots/` for captured results.

---

## Task 3: Jira MCP

The Jira MCP server connects Claude to Atlassian Jira so it can query issues, list tickets, and summarize project activity.

### Configuration (`.mcp.json`)

```json
"jira": {
  "command": "npx",
  "args": ["-y", "@sooperset/mcp-atlassian"],
  "env": {
    "JIRA_URL": "https://yourcompany.atlassian.net",
    "JIRA_USERNAME": "your-email@example.com",
    "JIRA_API_TOKEN": "your_api_token_here"
  }
}
```

### How It Works

- `@sooperset/mcp-atlassian` is a community MCP server that wraps the Jira REST API.
- Credentials are passed via environment variables — no hardcoded secrets.
- Claude calls tools like `search_issues` with JQL queries under the hood.

### Interactions Performed

Prompt used: *"Give me the Jira tickets of the last 5 bugs on project \<KEY\>"*

See `docs/screenshots/jira-mcp-result.png` for the captured result.

---

## Task 4: Custom MCP Server (FastMCP)

A custom MCP server built with [FastMCP](https://github.com/jlowin/fastmcp) that exposes `lorem-ipsum.md` as both a Resource and a Tool.

### Concepts

- **Resources** are URIs that Claude can read from passively (e.g. files, APIs). They are identified by a URI scheme and return data when accessed.
- **Tools** are actions Claude can actively call to perform operations (e.g. reading a file, running a command). Tools can accept parameters and return results.

### What the server exposes

| Type | Name | Description |
|------|------|-------------|
| Resource | `lorem://ipsum/{word_count}` | Returns the first N words from `lorem-ipsum.md` (default: 30) |
| Tool | `read` | Callable by Claude; accepts optional `word_count`, returns word-limited content |

### Configuration (`.mcp.json`)

```json
"lorem-ipsum": {
  "command": "python",
  "args": ["custom-mcp-server/server.py"]
}
```

### Interactions Performed

Prompt used: *"Use the read tool with word_count=20"*

See `docs/screenshots/custom-mcp-read-tool-result.png` for the captured result.

---

## Project Structure

```
homework-5/
├── README.md
├── HOWTORUN.md
├── TASKS.md
├── .mcp.json
├── custom-mcp-server/
│   ├── server.py          # FastMCP server (resource + read tool)
│   ├── lorem-ipsum.md     # Source text for the resource
│   └── requirements.txt   # fastmcp dependency
└── docs/
    └── screenshots/
        ├── github-mcp-result.png
        ├── filesystem-mcp-result.png
        ├── jira-mcp-result.png
        └── custom-mcp-read-tool-result.png
```
