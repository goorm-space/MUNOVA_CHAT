#!/bin/bash

TOKEN_FILE="auth.json"
LIMIT=1

if [ ! -f "$TOKEN_FILE" ]; then
  echo "❌ auth.json 파일이 없습니다!"
  exit 1
fi

# 문자열 배열로 되어 있다면 jq .[]
TOKENS=($(jq -r '.[]' $TOKEN_FILE | head -n $LIMIT))
VUSERS=${#TOKENS[@]}

RSOCKET_URL="ws://localhost:7070/rs"

echo "🚀 RSocket CONNECT Load Test Start (VUsers=$VUSERS)"

for i in "${!TOKENS[@]}"; do
  TOKEN="${TOKENS[$i]}"

  echo "▶ VUser-$i CONNECT 시도..."

  rsocket-cli \
    --debug \
    --connect "$RSOCKET_URL" \
    --metadata "$TOKEN" \
    --metadataMimeType "message/x.rsocket.authentication.v0" \
    --request \
    --route "ping.test" \
    "TEST" &

  sleep 0.1
done

wait

echo "🔥 CONNECT TEST DONE"