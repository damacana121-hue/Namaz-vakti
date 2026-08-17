#!/usr/bin/env bash
set -euo pipefail
BASE_URL="${PRAYER_API_BASE_URL:-https://api.aladhan.com/v1}"
LAT="${PRAYER_TEST_LATITUDE:-41.0082}"
LON="${PRAYER_TEST_LONGITUDE:-28.9782}"
DATE="${PRAYER_TEST_DATE:-$(date -u +%d-%m-%Y)}"
URL="${BASE_URL}/timings/${DATE}?latitude=${LAT}&longitude=${LON}&method=13&school=1"
echo "Prayer API smoke test: ${URL}"
json="$(curl --fail --silent --show-error --retry 3 --connect-timeout 10 --max-time 30 "$URL")"
python3 - "$json" <<'PY'
import json, sys
p=json.loads(sys.argv[1])
assert p.get('code') == 200 and p.get('status') == 'OK', p
x=p.get('data',{}).get('timings',{})
required=['Fajr','Sunrise','Dhuhr','Asr','Maghrib','Isha']
missing=[k for k in required if not x.get(k)]
assert not missing, f'Missing timings: {missing}'
for k in required:
    v=x[k].split()[0]
    h,m=map(int,v.split(':')[:2])
    assert 0 <= h <= 23 and 0 <= m <= 59, (k,v)
print('API OK:', {k:x[k] for k in required})
PY
