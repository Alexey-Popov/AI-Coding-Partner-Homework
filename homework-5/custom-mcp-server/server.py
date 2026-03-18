"""
Custom MCP Server — Homework 5, Task 4
Built with FastMCP.

Resources vs Tools:
  - Resources are URIs that Claude can read from (e.g. files, APIs).
    They are passive data sources identified by a URI.
  - Tools are actions Claude can call to perform operations
    (e.g. reading a file, running a command, querying an API).
"""

from pathlib import Path
from fastmcp import FastMCP

mcp = FastMCP("lorem-ipsum-server")

LOREM_FILE = Path(__file__).parent / "lorem-ipsum.md"


@mcp.resource("lorem://ipsum/{word_count}")
def lorem_resource(word_count: int = 30) -> str:
    """Resource URI that returns the first `word_count` words from lorem-ipsum.md."""
    text = LOREM_FILE.read_text()
    words = text.split()
    return " ".join(words[:word_count])


@mcp.tool()
def read(word_count: int = 30) -> str:
    """Return the first `word_count` words from lorem-ipsum.md."""
    text = LOREM_FILE.read_text()
    words = text.split()
    return " ".join(words[:word_count])


if __name__ == "__main__":
    mcp.run()
