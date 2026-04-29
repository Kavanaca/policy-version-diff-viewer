from services.groq_client import GroqClient

client = GroqClient()

def call_ai(prompt):
    result = client.complete(prompt)

    if result["is_fallback"] or result["parsed"] is None:
        return None

    return result["parsed"]