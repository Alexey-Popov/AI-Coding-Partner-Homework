# How to Run the Custom MCP Server

## Prerequisites

- Python 3.10+
- `pip`

---

## 1. Install Dependencies

```bash
cd homework-5/custom-mcp-server
pip install -r requirements.txt
```

---

## 2. Run the Server (standalone test)

```bash
python server.py
```

The server starts and listens on stdio. You can also use the FastMCP CLI:

```bash
fastmcp run server.py
```

---

## 3. Connect via MCP Configuration

The `.mcp.json` file in `homework-5/` already includes the custom server entry:

```json
{
  "mcpServers": {
    "custom-mcp": {
      "command": "python",
      "args": [
        "/Users/o.stadnyk/homework/AI-Coding-Partner-Homework/homework-5/custom-mcp-server/server.py"
      ]
    }
  }
}
```

Claude Code picks this up automatically when you open the `homework-5/` folder.

---

## 4. Use and Test the `read` Tool

Once Claude Code has started the MCP server, prompt Claude with:

```
Use the read tool to get 50 words from the lorem ipsum resource.
```

Or with the default word count (30):

```
Call the read tool.
```

### Expected behaviour

- `read()` → returns the first **30** words from `lorem-ipsum.md`
- `read(word_count=50)` → returns the first **50** words
- `read(word_count=5)` → returns `"Lorem ipsum dolor sit amet"`

---

## Concepts

| Concept  | Description |
|----------|-------------|
| **Resource** | A URI that Claude can read from (e.g. a file, an API endpoint). Registered with `@mcp.resource(uri)`. This server exposes `lorem-ipsum://content`. |
| **Tool** | An action Claude can call to perform an operation (e.g. reading a file, running a command). Registered with `@mcp.tool()`. This server exposes the `read` tool which accepts an optional `word_count` parameter. |
