#!/usr/bin/env bash

# running script as a part of checking service healthy before starting dependent services
set -e

echo "Running checker"
#!/bin/sh
until docker container exec -it mysql_flowkind mysqladmin ping -P 3306 -proot | grep "mysqld is alive" ; do
  >&2 echo "MySQL is unavailable - waiting for it... 😴"
  sleep 5
done

#until "/usr/bin/mysql --user=jatinder --password=root --execute \"SHOW DATABASES;\""; do
#  >&2 echo "mysql is unavailable"
#  sleep 1
#done

>&2 echo "mysql is up - executing command"
exec "$@"