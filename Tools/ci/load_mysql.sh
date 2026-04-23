#!/usr/bin/env bash

set -euo pipefail

domain="${1:?domain is required}"
db_name="${2:?db_name is required}"

host="${MYSQL_HOST:-127.0.0.1}"
port="${MYSQL_PORT:-3306}"
user="${MYSQL_USER:-project_bible}"
password="${MYSQL_PASSWORD:-project_bible}"

schema_path="Database/mysql/${domain}/init/01_schema.sql"
seed_path="Database/mysql/${domain}/seeds/01_seed.sql"

if [[ ! -f "${schema_path}" ]]; then
  echo "Schema file not found: ${schema_path}" >&2
  exit 1
fi

if [[ ! -f "${seed_path}" ]]; then
  echo "Seed file not found: ${seed_path}" >&2
  exit 1
fi

mysql --protocol=TCP -h "${host}" -P "${port}" -u "${user}" "-p${password}" -e "DROP DATABASE IF EXISTS \`${db_name}\`; CREATE DATABASE \`${db_name}\`;"
mysql --protocol=TCP -h "${host}" -P "${port}" -u "${user}" "-p${password}" "${db_name}" < "${schema_path}"
mysql --protocol=TCP -h "${host}" -P "${port}" -u "${user}" "-p${password}" "${db_name}" < "${seed_path}"
