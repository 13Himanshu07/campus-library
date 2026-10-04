# Tomcat + MySQL on an Oracle Cloud VM

This Compose deployment runs the Java web application on Apache Tomcat 10.1, MySQL 8, and Caddy for HTTPS. Tomcat and MySQL are private to the Compose network; only ports 80 and 443 are published.

## Prepare the VM

- Use an Ubuntu 24.04 VM and install Docker Engine plus the Docker Compose plugin.
- Allow inbound TCP 22 from your own IP, and TCP 80/443 for web traffic. Keep 3306 and 8080 closed externally.
- Point a DNS `A` record for a domain you control at the VM's public IPv4 address.
- Copy the project to the VM. Build the WAR before building the container image.

## Configure and start

From the project root:

```bash
mvn clean package
cp deployment/oracle/.env.example deployment/oracle/.env
```

Edit `deployment/oracle/.env` with your DNS name and strong unique MySQL passwords. Then run:

```bash
docker compose --env-file deployment/oracle/.env -f deployment/oracle/compose.yaml up -d --build
```

The database container initializes from `database/schema.sql` and loads the local demo catalog/accounts from `database/sample_data.sql` on its first start. The site is available at `https://<PUBLIC_DOMAIN>/`. The included demo accounts must be disabled or have their passwords changed before exposing the site to the public.

View Tomcat and proxy logs with:

```bash
docker compose --env-file deployment/oracle/.env -f deployment/oracle/compose.yaml logs -f api proxy
```

## Updates and backup

After copying an updated source revision to the VM, rebuild the WAR and image with the commands above. Back up the `mysql_data` volume regularly. To apply schema changes to an existing database, back it up and apply a reviewed migration; initialization scripts run only when MySQL creates a new data directory.
