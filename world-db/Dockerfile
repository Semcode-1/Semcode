FROM mysql:8.0

# MySQL imports this dataset when the database volume is first initialized.
COPY world.sql /docker-entrypoint-initdb.d/world.sql
