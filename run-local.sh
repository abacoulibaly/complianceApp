#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT"

ENV_FILE="$ROOT/.env"
JAR="$ROOT/target/compliance-automation-1.0.0-SNAPSHOT.jar"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Error: .env file not found. Copy .env.example to .env and set OPENAI_API_KEY." >&2
  exit 1
fi

# Load environment variables from .env (handles Windows CRLF line endings)
set -a
while IFS= read -r line || [[ -n "$line" ]]; do
  line="${line%$'\r'}"
  line="${line#"${line%%[![:space:]]*}"}"
  line="${line%"${line##*[![:space:]]}"}"
  [[ -z "$line" || "$line" =~ ^# ]] && continue
  if [[ "$line" =~ ^([A-Za-z_][A-Za-z0-9_]*)=(.*)$ ]]; then
    key="${BASH_REMATCH[1]}"
    value="${BASH_REMATCH[2]}"
    value="${value%\"}"; value="${value#\"}"
    value="${value%\'}"; value="${value#\'}"
    export "$key=$value"
  fi
done < "$ENV_FILE"
set +a

if [[ ! -f "$JAR" ]]; then
  echo "Building application..."
  mvn -q -DskipTests package
fi

echo "Starting compliance-automation on http://localhost:8080"
if [[ -z "${OPENAI_API_KEY:-}" ]]; then
  echo "Warning: OPENAI_API_KEY is not set. Vision extraction will be disabled." >&2
else
  echo "OpenAI API key loaded from .env"
fi

exec java -jar "$JAR"
