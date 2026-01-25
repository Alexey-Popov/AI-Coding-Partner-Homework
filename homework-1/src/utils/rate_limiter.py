"""Rate limiting configuration for the Banking API"""
from slowapi import Limiter
from slowapi.util import get_remote_address

# Create limiter instance using client IP as the key
# Default limit: 100 requests per minute per IP
limiter = Limiter(key_func=get_remote_address)

# Rate limit string for 100 requests per minute
RATE_LIMIT = "100/minute"
