#!/bin/sh
set -eu

psql -U "$POSTGRES_USER" -d postgres -c "CREATE DATABASE pb_post;" || true
psql -U "$POSTGRES_USER" -d postgres -c "CREATE DATABASE pb_shop;" || true

psql -U "$POSTGRES_USER" -d pb_post -f /workspace/postgresql/post/init/01_schema.sql
psql -U "$POSTGRES_USER" -d pb_post -f /workspace/postgresql/post/seeds/01_seed.sql

psql -U "$POSTGRES_USER" -d pb_shop -f /workspace/postgresql/shop/init/01_schema.sql
psql -U "$POSTGRES_USER" -d pb_shop -f /workspace/postgresql/shop/seeds/01_seed.sql
