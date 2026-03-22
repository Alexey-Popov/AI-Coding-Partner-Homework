# Homework 5: Configure MCP Servers

**Author:** Aleksandr Stadnyk

## Description

This homework demonstrates configuring and using four MCP (Model Context Protocol) servers with Claude Code:

1. **GitHub MCP** — Connects Claude to GitHub to list pull requests, summarize commits, and interact with repositories.
2. **Filesystem MCP** — Connects Claude to a local directory, enabling file listing, reading, and directory exploration.
3. **Jira MCP** — Connects Claude to a Jira project to query tickets (e.g. last 5 bug tickets).
4. **Custom MCP (FastMCP)** — A hand-built Python MCP server using the FastMCP library. Exposes a `lorem-ipsum://content/{word_count}` resource and a `read` tool that returns a configurable number of words from a lorem ipsum file.

## Structure

```
homework-5/
├── README.md                        — this file
├── HOWTORUN.md                      — setup and usage instructions for the custom MCP server
├── TASKS.md                         — assignment task descriptions
├── .mcp.json                        — MCP server configuration
├── custom-mcp-server/
│   ├── server.py                    — FastMCP server implementation
│   ├── lorem-ipsum.md               — source text for the resource
│   └── requirements.txt             — Python dependencies (fastmcp)
└── docs/
    └── screenshots/                 — screenshots of MCP call results
```
