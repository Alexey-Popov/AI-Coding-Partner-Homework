from pathlib import Path
from fastmcp import FastMCP

mcp = FastMCP("lorem-ipsum-server")

LOREM_IPSUM_PATH = Path(__file__).parent / "lorem-ipsum.md"
DEFAULT_WORD_COUNT = 30


@mcp.resource("lorem-ipsum://content/{word_count}")
def lorem_ipsum_resource(word_count: int = DEFAULT_WORD_COUNT) -> str:
    """
    Resource URI: lorem-ipsum://content/{word_count}

    Resources are URIs that Claude can read from (e.g., files, APIs).
    This resource reads from lorem-ipsum.md and returns exactly `word_count`
    words (default: 30). Example: lorem-ipsum://content/50
    """
    text = LOREM_IPSUM_PATH.read_text(encoding="utf-8")
    words = text.split()
    return " ".join(words[:word_count])


@mcp.tool()
def read(word_count: int = DEFAULT_WORD_COUNT) -> str:
    """
    Tool: read

    Tools are actions Claude can call to perform operations (e.g., reading a
    file, running a command). This tool returns content from the
    lorem-ipsum://content/{word_count} resource, limited to `word_count` words
    (default: 30).

    Args:
        word_count: Number of words to return (default 30).
    """
    return lorem_ipsum_resource(word_count)


if __name__ == "__main__":
    mcp.run()
