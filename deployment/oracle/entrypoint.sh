#!/bin/sh
# Tomcat production container entrypoint. The CA is trust material, not a private key.
set -eu
if [ "${APP_ENV:-development}" = "production" ]; then
  : "${MYSQL_TLS_CA_FILE:?MYSQL_TLS_CA_FILE must point at mounted MySQL CA certificate}"
  keytool -importcert -noprompt -alias campus-library-mysql-ca -file "$MYSQL_TLS_CA_FILE" \
    -keystore /tmp/campus-library-mysql-truststore.p12 -storetype PKCS12 -storepass changeit
fi
exec "$@"
