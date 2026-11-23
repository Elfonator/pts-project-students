# Homework – Mobile project setup

## Goal
Get the prepared Android app for TestLab Shop, open it in IntelliJ IDEA with the Android plugin, connect it to the same PHP backend you already use for web tests, and smoke-check the app on an emulator.

You do not need a separate Android Studio install. If you already use Android Studio, follow the README in the mobile repo instead.

Do the steps in order: plugin and SDK first, then the Gradle wrapper script, then Sync.

---

## 1. Get the mobile project

Clone with Git:

```bash
git clone https://github.com/Elfonator/pts-mobile-project.git
```

or download the ZIP from https://github.com/Elfonator/pts-mobile-project and unpack it.

---

## 2. Enable the Android plugin in IntelliJ

Open IntelliJ IDEA.

Settings → Plugins → search Android → install or enable → restart if asked.

---

## 3. Open the project root

File → Open → select the project folder that contains `settings.gradle` (and `prepare-project.sh` / `prepare-project.bat`).
Do not open only the inner `app/` folder.

If IntelliJ asks for a Gradle JDK, choose JDK 17 or JDK 21.

Gradle Sync may complain until the SDK and wrapper steps below are done - that is fine for now.

---

## 4. Android SDK – first install may default to API 36

Initial SDK setup through wizard often locks latest platforms (Android 16 / API 36) and you cannot pick 35 yet. That is ok.

1. Set Android SDK to preferred location (not the filesystem root - an empty or wrong path greys out all platforms).
2. Accept the default API 36 install and finish the wizard.

IntelliJ writes `local.properties` with `sdk.dir` pointing at that SDK folder.

---

## 5. Install API 35

This app uses compileSdk / targetSdk 35.

1. Settings → Languages & Frameworks → Android SDK (or SDK Manager).
2. Tab SDK Platforms → enable Show Package Details.
3. Check Android 15.0 (API 35) → Apply / OK.
4. Tab SDK Tools: install Android SDK Platform-Tools, Android Emulator, and Build-Tools 35.0.0 (or a compatible 35.x).

Having API 36 installed is fine. You still need API 35 for this project.

---

## 6. Prepare the Gradle wrapper

The project does not include the binary Gradle wrapper JAR. Download and verify it from the project folder.

Use IntelliJ’s Terminal, or any system terminal in that folder.

Windows (cmd or PowerShell):

```bat
prepare-project.bat
```

Linux / macOS:

```bash
chmod +x prepare-project.sh
./prepare-project.sh
```

Expected message:

```text
Gradle wrapper is ready (SHA-256 verified).
```

Needs network access to GitHub. If download or checksum fails, fix the network and run the script again.

---

## 7. Gradle Sync

Now that the SDK and wrapper are ready:

- View → Tool Windows → Gradle
- Click Reload All Gradle Projects (circular arrows)

Or use the Load Gradle project / Sync balloon if IntelliJ shows one.

Wait until the bottom progress bar finishes without errors.

A `.gradle/` folder may appear in the project (local cache; do not commit).

---

## 8. Start the PHP backend (web project – Docker)

The Android app uses the same TestLab Shop backend as the web GUI tests.

Prefer Docker Compose in the web project (same as previous weeks). Do not start PHP inside the Android repo.

In your web application repo (`pts-web-project`), with `.env` already filled:

```bash
cd /path/to/your-web-project
docker compose up -d --build
```

If this volume was never installed or seeded:

```bash
docker compose exec app php bin/install.php
docker compose exec app php bin/seed.php
```

Check from the host:

```text
http://127.0.0.1:8080/api/index.php?route=/health
```

Expect JSON with `data.status = "ok"` and `data.application = "TestLab Shop"`. Leave Compose running while you use the emulator.

Optional without Docker: from the web project root, `php -S 127.0.0.1:8080 -t public` with MariaDB as in that project’s `.env`. Still not from `pts-mobile-project`.

---

## 9. Create an emulator and run the app

1. Open Device Manager in IntelliJ.
2. Create a virtual phone with system image API 35.
3. Start the AVD and wait for the home screen.
4. In the top toolbar select module `app`, build variant debug, and your running emulator.
5. Click the green Run button (Run ‘app’).

Do not choose “Tests in …app” - that runs unit tests, not the shop UI.

The TestLab Shop screen should appear on the emulator.

---

## 10. Set the server address and smoke-check

Bottom bar: Produkty, Košík, Objednávky, Účet.

Server settings: button labeled Server in the top bar (not in the bottom nav).

1. Open Server.
2. Tap Predvoľba pre Android emulátor, or enter `http://10.0.2.2:8080/`.
3. Tap Otestovať spojenie - expect something like `Spojenie OK · TestLab Shop 1.0.0`.
4. Tap Uložiť adresu servera if needed.
5. Open Účet and log in with the demo seed account:
   - email: `student@example.test`
   - password: `Student123!`
6. Open Produkty and confirm the catalog loads (search `KB-001`).

Why `10.0.2.2`? On a standard Android emulator that address is the host computer. `127.0.0.1` inside the emulator is the emulator itself, not your Docker PHP on the PC.

---

No submission or commit is required for this setup exercise.

Keep `local.properties`, `.gradle/`, and other machine-specific files out of Git.
