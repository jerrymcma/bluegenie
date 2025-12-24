import json
import os
import urllib.error
import urllib.request
from pathlib import Path
from urllib.parse import urlencode

config_path = Path('sparkifire-web/.env')
key = os.environ.get('GEMINI_API_KEY') or os.environ.get('VITE_GEMINI_API_KEY')

if not key and config_path.exists():
    for line in config_path.read_text().splitlines():
        line = line.strip()
        if line.startswith('VITE_GEMINI_API_KEY') or line.startswith('GEMINI_API_KEY'):
            key = line.split('=', 1)[1].strip()
            break

if not key:
    raise SystemExit('No Gemini API key found in environment or sparkifire-web/.env')

payload = {
    "contents": [
        {
            "parts": [
                {"text": "Hello! You are now using Gemini 2.0 Flash. Please confirm which model version you are and say hi in one sentence."}
            ]
        }
    ]
}
url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?{urlencode({'key': key})}"
req = urllib.request.Request(url, data=json.dumps(payload).encode('utf-8'), headers={'Content-Type': 'application/json'})
try:
    with urllib.request.urlopen(req, timeout=20) as resp:
        print(resp.status)
        print(resp.read().decode())
except urllib.error.HTTPError as e:
    print('HTTP error:', e.code)
    print(e.read().decode())
except Exception as e:
    print('Error:', e)
