# Safe File Deleter

A small full-stack demo application that deletes files inside a restricted directory (`allowed_root`) while refusing symlinks, non-regular files, and any path that escapes the root. The React frontend lets you paste a list of relative paths and shows which were deleted and which were refused (with reasons).

## Stack

Spring Boot 3.2 / Java 17 / React 18 / Vite 5

## Layout

```
.
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/example/demo/
│           │   ├── DemoApplication.java
│           │   └── controller/ApiController.java
│           └── resources/application.properties
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── App.jsx
        ├── App.css
        └── main.jsx
```

## How to run

Backend (port 8080):

```
cd backend
mvn spring-boot:run
```

Frontend (port 5173, proxies `/api` to the backend):

```
cd frontend
npm install
npm run dev
```

Then open http://localhost:5173.

## API

### `POST /api/delete`

Accepts a JSON array of relative paths (relative to `app.allowed-root`) and attempts to delete each one.

**Request body**

```json
["file1.txt", "subdir/file2.txt"]
```

**Response body**

```json
{
  "deleted": ["file1.txt"],
  "refused": [
    { "path": "subdir/file2.txt", "reason": "does not exist" }
  ]
}
```

Each path is refused if it resolves outside `allowed_root`, is a symbolic link, does not exist, is not a regular file, or if deletion fails. Returns HTTP 500 with `{ "error": "..." }` if `allowed_root` itself is not accessible.

## Notes

- `app.allowed-root` defaults to `./allowed_root` (relative to the backend working directory). Create this directory before running, or override it in `application.properties`.
- Paths are resolved with `normalize()` and checked with `startsWith(root)` after `toRealPath()` on the root, but the target itself is not canonicalized — symlinked parent directories inside the root are not explicitly rejected (only the final path component is checked for being a symlink).
- Only regular files are deleted; directories are always refused.
- There is no authentication or authorization — this is a demo and must not be exposed to untrusted networks.
- The frontend assumes the Vite dev proxy; for production you would build the frontend (`npm run build`) and serve the output from Spring Boot's static resources.