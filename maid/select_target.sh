#!/bin/bash -e
if [ -z "$1" ];then
    echo 'No target specified!'
    exit 1
fi
curl --request POST 'http://127.0.0.1:8220/api/db/kv/set' \
--header 'content-type: application/json' \
--data-raw '{
  "taskId": "00000000-0000-0000-0000-000000000000",
  "forwardFrom": "twitkit-app",
  "timestamp": "'$(date +%FT%T.%3N%:z)'",
  "data": {
    "twid":"'$1'"
  }
}
'
