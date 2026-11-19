# BRAST OF FYTING — Version 1.0.8
A Devo Studios Android FPS starter project.

## Version 1.0.8 changes
- App version is set to 1.0.8 (versionCode 108).
- On launch, a saved player session is refreshed automatically.
- Backend connection details are not shown in the game UI.
- Official Server is the only server option and is selected by default.
- Custom IP server controls are hidden in this version.
- Training scene remains available while the live dedicated game server is being built.

## Current scope
- Native Android app, forced landscape orientation.
- OpenGL ES 2.0 training-range renderer with placeholder geometry.
- Online email sign-up/sign-in and saved refresh-session restoration.
- Default Official Server lobby entry.
- GitHub Actions workflow builds a debug APK and uploads it as an artifact.

This is still an early technical prototype, not a complete online shooter. The training scene does not yet implement player movement, firing, hit detection, bots, matchmaking, voice chat, anti-cheat, or a multiplayer game-server protocol. The Official Server is shown and selected in the UI, but the dedicated live match server has not been deployed yet.

## Backend setup (one time)
In GitHub, open Settings -> Secrets and variables -> Actions and add these repository secrets:
- `BACKEND_URL`: project API URL.
- `BACKEND_PUBLIC_KEY`: publishable client key.

The app uses these values to reach the account service automatically. They are not displayed in the game UI. A publishable key is designed for client use; it is not an administrative secret. Values embedded in an APK can be extracted by a determined person, so access must be protected by Row Level Security and backend authorization. Never place a privileged admin key, database password, or server-side token in the APK or GitHub build.

The current linked backend project was inspected and is healthy. Its existing `profiles`, `projects`, `project_updates`, `forum_posts`, `forum_comments`, and `notifications` tables were preserved. No game-specific schema was added or existing table modified in this update.

## Build APK
1. Create a GitHub repository named `brast-of-fyting`.
2. Upload the contents of this ZIP to the repository root (not the ZIP file itself).
3. Add the two GitHub Actions secrets listed above.
4. Open Actions and run **Android APK Build** (or push to `main`).
5. Download artifact `brast-of-fyting-v1.0.8-debug-apk` from the successful workflow run.
6. Extract the artifact ZIP to get `app-debug.apk`.

## Project layout
- `app/`: Android application source.
- `GameRenderer.java`: OpenGL ES training scene.
- `AuthClient.java`: account login, registration, refresh, and sign-out requests.
- `MainActivity.java`: account screen, automatic session restore, Official Server lobby.
- `.github/workflows/android-build.yml`: cloud APK build.
