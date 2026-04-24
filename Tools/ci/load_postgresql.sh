#!/usr/bin/env bash

set -euo pipefail

domain="${1:?domain is required}"
db_name="${2:?db_name is required}"

export PGPASSWORD="${POSTGRES_PASSWORD:-1234}"

host="${POSTGRES_HOST:-localhost}"
port="${POSTGRES_PORT:-5432}"
user="${POSTGRES_USER:-admin}"

schema_path="Database/postgresql/${domain}/init/01_schema.sql"
seed_path="Database/postgresql/${domain}/seeds/01_seed.sql"

if [[ ! -f "${schema_path}" ]]; then
  echo "Schema file not found: ${schema_path}" >&2
  exit 1
fi

if [[ ! -f "${seed_path}" ]]; then
  echo "Seed file not found: ${seed_path}" >&2
  exit 1
fi

psql -h "${host}" -p "${port}" -U "${user}" -d postgres -v ON_ERROR_STOP=1 -c "DROP DATABASE IF EXISTS \"${db_name}\";"
psql -h "${host}" -p "${port}" -U "${user}" -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE \"${db_name}\";"
psql -h "${host}" -p "${port}" -U "${user}" -d "${db_name}" -v ON_ERROR_STOP=1 -f "${schema_path}"
psql -h "${host}" -p "${port}" -U "${user}" -d "${db_name}" -v ON_ERROR_STOP=1 -f "${seed_path}"
