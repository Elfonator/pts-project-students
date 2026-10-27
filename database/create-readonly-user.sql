-- Run once as MariaDB admin (docker: docker compose exec db mariadb -uroot -p).
-- Change the password. Do not expose MariaDB to the public Internet.
-- Docker compose publishes 127.0.0.1:3307 -> container 3306; JDBC uses that host port.

CREATE USER IF NOT EXISTS 'testlab_reader'@'%'
    IDENTIFIED BY 'CHANGE_ME_READER_PASSWORD';

GRANT SELECT ON testlab_shop.workspaces TO 'testlab_reader'@'%';
GRANT SELECT ON testlab_shop.products TO 'testlab_reader'@'%';
GRANT SELECT ON testlab_shop.orders TO 'testlab_reader'@'%';
GRANT SELECT ON testlab_shop.order_items TO 'testlab_reader'@'%';

SHOW GRANTS FOR 'testlab_reader'@'%';

-- Intentionally no SELECT on users / api_tokens / login_limits.
-- Intentionally no INSERT, UPDATE, DELETE, CREATE, DROP, ALTER.
