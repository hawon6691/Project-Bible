#!/bin/sh
set -eu

mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS pb_post CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS pb_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "GRANT ALL PRIVILEGES ON pb_post.* TO '$MYSQL_USER'@'%';"
mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "GRANT ALL PRIVILEGES ON pb_shop.* TO '$MYSQL_USER'@'%';"
mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "FLUSH PRIVILEGES;"

mysql -uroot -p"$MYSQL_ROOT_PASSWORD" pb_post < /workspace/mysql/post/init/01_schema.sql
mysql -uroot -p"$MYSQL_ROOT_PASSWORD" pb_post < /workspace/mysql/post/seeds/01_seed.sql

mysql -uroot -p"$MYSQL_ROOT_PASSWORD" pb_shop < /workspace/mysql/shop/init/01_schema.sql
mysql -uroot -p"$MYSQL_ROOT_PASSWORD" pb_shop < /workspace/mysql/shop/seeds/01_seed.sql
