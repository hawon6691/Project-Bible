#!/usr/bin/env bash

set -euo pipefail

domain="${1:?domain is required}"
db_name="${2:?db_name is required}"

host="${MYSQL_HOST:-127.0.0.1}"
port="${MYSQL_PORT:-3306}"
user="${MYSQL_USER:-admin}"
password="${MYSQL_PASSWORD:-1234}"
bootstrap_user="${MYSQL_BOOTSTRAP_USER:-root}"
bootstrap_password="${MYSQL_ROOT_PASSWORD:-${password}}"

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

mysql --protocol=TCP -h "${host}" -P "${port}" -u "${bootstrap_user}" "-p${bootstrap_password}" -e "DROP DATABASE IF EXISTS \`${db_name}\`; CREATE DATABASE \`${db_name}\`; CREATE USER IF NOT EXISTS '${user}'@'%' IDENTIFIED BY '${password}'; GRANT ALL PRIVILEGES ON \`${db_name}\`.* TO '${user}'@'%'; FLUSH PRIVILEGES;"
mysql --protocol=TCP -h "${host}" -P "${port}" -u "${bootstrap_user}" "-p${bootstrap_password}" "${db_name}" < "${schema_path}"
mysql --protocol=TCP -h "${host}" -P "${port}" -u "${bootstrap_user}" "-p${bootstrap_password}" "${db_name}" < "${seed_path}"
