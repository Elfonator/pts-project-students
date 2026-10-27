## Homework – Web GUI and read-only database checks

### Goal
Understand how a GUI test and MariaDB can verify the same order independently: 
- create the order in the browser, take the UUID from the page, then `SELECT` that row over JDBC. R
- un the provided **EXAMPLE**, then complete the **HOMEWORK** (order items, warehouse stock, cancellation).

Do not look up “the latest order” with `ORDER BY created_at DESC LIMIT 1`. Always use the UUID from your own GUI run.

---

### What is new in the project

| Path | Role |
|------|------|
| `src/test/java/sk/testlab/db/DbConfig.java` | Loads `db.url` / `db.user` / `db.password` from `config/test.properties` (separate from `TestConfig`, so normal GUI/API tests do not need a DB account). |
| `src/test/java/sk/testlab/db/OrderRecord.java` | Simple DTOs for one `orders` row and one `order_items` row. |
| `src/test/java/sk/testlab/db/OrderDbReader.java` | Read-only JDBC helper: `workspaceId`, `findOrder`, `findItems`, `stock`. Uses parameterized `SELECT` only; `setReadOnly(true)`. |
| `src/test/java/sk/testlab/db/GuiDatabaseOrderTest.java` | **EXAMPLE** – GUI create → UUID → assert `orders` (including `workspace_id`). |
| `src/test/java/sk/testlab/db/GuiDatabaseItemsTaskTest.java` | **HOMEWORK** – TODOs for items, stock, and GUI cancel. |
| `database/create-readonly-user.sql` | Template SQL for a SELECT-only MariaDB user (`testlab_reader`). |
| `config/test.example.properties` | New `db.*` keys (copy into `test.properties`). |
| `pom.xml` | Test dependency `mariadb-java-client`. |

Reuse what you already know: `OrderWorkflow`, page objects, `Artifacts`, `ApiFixtureHelper` / fixtures.

---

### 1. Expose MariaDB to the host (web project `compose.yaml`)

JDBC runs on your machine. The PHP app talks to MariaDB inside Docker on port `3306`. From the host you need a published port.

In the **web application** repo (`pts-web-project`), under the `db` service, ensure:

```yaml
services:
  db:
    # ...
    ports:
      - "127.0.0.1:3307:3306"
```

Meaning: host `127.0.0.1:3307` → container `3306`. Prefer binding to `127.0.0.1` only (not all interfaces).

If the `db` container was created before this port mapping existed, recreate it so Docker applies the publish:

Check that `PORTS` shows something like `127.0.0.1:3307->3306/tcp`. Then start (or restart) the app if needed:

```bash
docker compose up -d
```

---

### 2. Create the read-only JDBC user

We won't use the application user from `.env` (`testlab_app`) for these tests. That account can write. Tests should only observe data.

Using docker compose open a MariaDB client as root (password = `DB_ROOT_PASSWORD` from the web project `.env`):

```bash
cd /path/to/your-web-project
docker compose exec db mariadb -uroot -p
```

Enter the root password when prompted. Then run the statements from `database/create-readonly-user.sql` in root, but replace `CHANGE_ME_READER_PASSWORD` with a password you choose (example below uses `readerpass` - pick your own):

```sql
CREATE USER IF NOT EXISTS 'testlab_reader'@'%'
    IDENTIFIED BY 'readerpass';

GRANT SELECT ON testlab_shop.workspaces TO 'testlab_reader'@'%';
GRANT SELECT ON testlab_shop.products TO 'testlab_reader'@'%';
GRANT SELECT ON testlab_shop.orders TO 'testlab_reader'@'%';
GRANT SELECT ON testlab_shop.order_items TO 'testlab_reader'@'%';

FLUSH PRIVILEGES;
SHOW GRANTS FOR 'testlab_reader'@'%';
```

If the user already existed with another password:

```sql
ALTER USER 'testlab_reader'@'%' IDENTIFIED BY 'readerpass';
FLUSH PRIVILEGES;
```

Exit with `exit` or Ctrl+D.

Optional: in IntelliJ, add a MariaDB data source (`127.0.0.1`, port `3307`, database `testlab_shop`, user `testlab_reader`) so SQL table names resolve in the IDE. That is optional for running tests.

---

### 3. Configure the test project

In the file config/test.properties set those:

```properties
db.url=jdbc:mariadb://127.0.0.1:3307/testlab_shop?connectTimeout=5000&socketTimeout=10000
db.user=testlab_reader
db.password=readerpass
```

Use the same password you set in MariaDB. Keep `config/test.properties` out of Git.

Also keep your usual fixture settings working (`fixtures.mode=api` with `test.api.key`, or `manual` with `test.email` / `test.password` / `test.workspace`). The DB tests acquire a workspace the same way as other API-backed GUI tests.

Reload Maven if IntelliJ has not picked up `mariadb-java-client` yet.

---

### 4. Run the EXAMPLE

Open and run:

```text
src/test/java/sk/testlab/db/GuiDatabaseOrderTest.java
```

Expected idea of the flow:

1. Acquire a fresh workspace (fixture).
2. Create the reference order through the GUI (`OrderWorkflow`).
3. Read the order UUID from the detail page.
4. `OrderDbReader.findOrder(uuid)` and assert `orders` (totals, coupon, delivery, status `created`, **`workspace_id` matches fixture**).
5. `@AfterEach` cancels **only that UUID** via GUI when `cleanup.order=true`, so stock is not left reduced.

You should see a PASS line in the console and evidence under `target/artifacts/`.

---

### 5. HOMEWORK – complete `GuiDatabaseItemsTaskTest`

File:

```text
src/test/java/sk/testlab/db/GuiDatabaseItemsTaskTest.java
```

Setup (config, fixture, reader, browser, page objects) is already there. Implement **TODOs 1–7**, then remove the `UnsupportedOperationException`.

| TODO | What you must do |
|------|------------------|
| 1 | Read starting stock for `KB-001` and `MS-001` (`db.stock`). Fresh fixture → `100` / `100`. |
| 2 | Create the reference order via `OrderWorkflow`; store UUID in `orderId`; set `orderSubmitted = true`; write `order-id.txt`. |
| 3 | Assert GUI: status `created`, total `8563` (same idea as the EXAMPLE). |
| 4 | JDBC `order_items` for that UUID: two lines; KB `3490×2=6980`; MS `1990×1=1990`; sum of lines `8970`. |
| 5 | JDBC stock after create: KB `before - 2`, MS `before - 1`. |
| 6 | Cancel **through the GUI** (`OrderCancelPage`); set `cancelledInTest = true`; assert GUI `cancelled`. |
| 7 | JDBC after cancel: status `cancelled`, `cancelled_at` set, total still `8563`, stock restored, items unchanged. |

Rules:

- Look up by UUID only.
- JDBC stays read-only (no `INSERT` / `UPDATE` / `DELETE` from tests).
- Cancel only via the GUI, never by “fixing” the DB.
- Failure cleanup in `@AfterEach` already cancels your UUID if you created an order but did not cancel it in the test body.

Reference cart (same as `OrderWorkflow`): KB-001 × 2, MS-001 × 1, coupon `STUDENT10`, courier → **8563** cents.

---

### Checklist

- [ ] `compose.yaml` publishes `127.0.0.1:3307:3306`; containers recreated / up
- [ ] `testlab_reader` created with SELECT on the four shop tables
- [ ] `config/test.properties` has matching `db.*` values
- [ ] `GuiDatabaseOrderTest` (EXAMPLE) passes
- [ ] `GuiDatabaseItemsTaskTest` TODOs implemented and pass
