#!/bin/bash

set -u

BASE_URL="${1:-http://localhost:8081/brumkul-ramazan}"
API="$BASE_URL/api/students"
OWNER_EMAIL="ramazan.brumkul@example.com"
TEMP_EMAIL="temp.brumkul.$(date +%s)@example.com"
MISSING_ID=999999
PASSED=0
FAILED=0
HTTP_CODE=""
BODY=""
NL=$'\n'

pretty() {
    if command -v python3 >/dev/null 2>&1; then
        printf '%s\n' "$1" | python3 -m json.tool 2>/dev/null || printf '%s\n' "$1"
    else
        printf '%s\n' "$1"
    fi
}

json_field() {
    printf '%s' "$1" | python3 -c "import json, sys; print(json.load(sys.stdin)['$2'])"
}

id_by_email() {
    printf '%s' "$1" | python3 -c "import json, sys; print(next(s['id'] for s in json.load(sys.stdin) if s['email'] == '$2'))"
}

call() {
    local title="$1" expected="$2" method="$3" url="$4" data="${5:-}" response
    echo
    echo "=== $title"
    echo "$method $url"
    if [ -n "$data" ]; then
        echo "Request body: $data"
        response="$(curl -sS -X "$method" -H 'Content-Type: application/json' -d "$data" -w $'\n%{http_code}' "$url")"
    else
        response="$(curl -sS -X "$method" -w $'\n%{http_code}' "$url")"
    fi
    HTTP_CODE="${response##*$NL}"
    BODY="${response%$NL*}"
    [ "$BODY" = "$response" ] && BODY=""
    if [ -n "$BODY" ]; then
        pretty "$BODY"
    fi
    case " $expected " in
        *" $HTTP_CODE "*) echo "HTTP $HTTP_CODE  PASS (expected $expected)"; PASSED=$((PASSED + 1)) ;;
        *)                echo "HTTP $HTTP_CODE  FAIL (expected $expected)"; FAILED=$((FAILED + 1)) ;;
    esac
}

echo "API test of Ramazan Brumkul: $BASE_URL"
if ! curl -s -o /dev/null "$BASE_URL/api/info"; then
    echo "The application is not reachable at $BASE_URL"
    echo "Start it with: docker compose up -d"
    exit 1
fi

call "Application info" "200" GET "$BASE_URL/api/info"

call "Create student Ramazan Brumkul" "201 409" POST "$API" \
    "{\"firstName\":\"Ramazan\",\"lastName\":\"Brumkul\",\"email\":\"$OWNER_EMAIL\",\"groupName\":\"IT2-2301\"}"
if [ "$HTTP_CODE" = "201" ]; then
    OWNER_ID="$(json_field "$BODY" id)"
else
    echo "Ramazan Brumkul already exists from a previous run, looking up the ID"
    OWNER_ID="$(id_by_email "$(curl -sS "$API")" "$OWNER_EMAIL")"
fi
echo "Ramazan Brumkul has ID $OWNER_ID"

call "Create temporary student for the delete test" "201" POST "$API" \
    "{\"firstName\":\"Temp\",\"lastName\":\"Brumkul-Test\",\"email\":\"$TEMP_EMAIL\",\"groupName\":\"IT2-2301\"}"
TEMP_ID="$(json_field "$BODY" id)"

call "Get all students" "200" GET "$API"

call "Get student by ID $OWNER_ID" "200" GET "$API/$OWNER_ID"

call "Update student $OWNER_ID" "200" PUT "$API/$OWNER_ID" \
    "{\"firstName\":\"Ramazan\",\"lastName\":\"Brumkul\",\"email\":\"$OWNER_EMAIL\",\"groupName\":\"IT2-2301-DevOps\"}"

call "Delete temporary student $TEMP_ID" "204" DELETE "$API/$TEMP_ID"

call "Get deleted student $TEMP_ID" "404" GET "$API/$TEMP_ID"

call "Get nonexistent student $MISSING_ID" "404" GET "$API/$MISSING_ID"

call "Update nonexistent student $MISSING_ID" "404" PUT "$API/$MISSING_ID" \
    "{\"firstName\":\"Ramazan\",\"lastName\":\"Brumkul\",\"email\":\"nobody.brumkul@example.com\",\"groupName\":\"IT2-2301\"}"

call "Create student with invalid data" "400" POST "$API" \
    "{\"firstName\":\"\",\"lastName\":\"Brumkul\",\"email\":\"not-an-email\",\"groupName\":\"IT2-2301\"}"

echo
echo "=================================================="
echo " Result for Ramazan Brumkul: $PASSED passed, $FAILED failed"
echo "=================================================="
[ "$FAILED" -eq 0 ]
