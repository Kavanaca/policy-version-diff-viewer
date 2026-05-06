import hashlib
import time

CACHE = {}
TTL = 900  # 15 minutes


def generate_key(text):
    return hashlib.sha256(text.encode()).hexdigest()


def get_cache(text):
    key = generate_key(text)

    if key in CACHE:
        data, timestamp = CACHE[key]

        if time.time() - timestamp < TTL:
            return data

        del CACHE[key]

    return None


def set_cache(text, value):
    key = generate_key(text)
    CACHE[key] = (value, time.time())