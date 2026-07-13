# servlets_template

A Maven-based Java servlet web app (Tomcat 9 / `javax.servlet`) with session-based login, role-based access (admin/user), and an embedded H2 file database used to log login history.

---

## Requirements

- Java 8
- Maven
- Tomcat 9 (deploy the built WAR to `webapps/`, or run via `mvn tomcat7:run`)

Login credentials (hardcoded, for demo purposes):

| Username | Password | Role  |
|----------|----------|-------|
| admin    | admin    | admin |
| user     | user     | user  |

---

## Project layout

```
src/main/java/com/name/app/
├── AppInitListener.java      → startup hook, creates DB tables on boot
├── HomeServlet.java          → GET /home
├── MessiServlet.java         → GET /messi
├── RonaldoServlet.java       → GET /ronaldo
├── HistoryServlet.java       → GET /history (admin only)
├── HelloWorldServlet.java    → GET /hello (standalone demo endpoint)
├── db/
│   └── DatabaseManager.java  → single place that opens H2 connections
└── auth/
    ├── LoginServlet.java     → GET/POST /login
    ├── LogoutServlet.java    → GET /logout
    └── AuthFilter.java       → gatekeeper filter for protected pages

src/main/webapp/
├── index.jsp                  → traffic router (redirects to /login or /home)
└── WEB-INF/
    ├── web.xml                 → H2 console servlet mapping
    └── views/                  → JSPs, NOT directly URL-accessible
        ├── login.jsp
        ├── home.jsp
        ├── messi.jsp
        ├── ronaldo.jsp
        └── history.jsp
```

**Why are the JSPs under `WEB-INF/views/`?** Anything inside `WEB-INF/` is invisible to direct browser requests — Tomcat blocks it. A user can't type `.../WEB-INF/views/home.jsp` and skip the servlet/auth check. The only way to render a JSP is via `request.getRequestDispatcher(...).forward(...)` from Java code, which forces every page through its servlet (and therefore through `AuthFilter`) first.

---

## The three layers of control

1. **Servlets** — one per page. Each is a thin controller that (optionally) does some work, then forwards to its JSP.
2. **`AuthFilter`** — registered on `/home`, `/messi`, `/ronaldo`, `/history` via `@WebFilter(urlPatterns = {...})`. Runs *before* any of those servlets and checks "is there a session with a `username`?" If not, redirects to `/login`.
3. **`HistoryServlet`**'s own role check — the filter only knows "logged in or not", not "which role". `HistoryServlet` additionally checks `role == "admin"` itself and returns `403 Forbidden` otherwise. Role enforcement lives with the resource that needs it, not just the generic filter.

> **Note:** `AuthFilter`'s `urlPatterns` is an explicit list of exact paths, not a wildcard. If you add a new protected page later, you must remember to add its path to that list — it isn't automatically protected.

---

## The H2 database

- File-based H2 DB, connection string `jdbc:h2:~/myapp-db/data` (file created automatically on first run, stored in the user's home directory, outside the project).
- `DatabaseManager` is a small static utility: it registers the H2 driver once (`Class.forName("org.h2.Driver")`, required to work around Tomcat's classloader isolation) and hands out a fresh `Connection` per call. There's no connection pool — each servlet opens a connection, does its query in a try-with-resources block, and closes it immediately. Fine for this demo; a real app would use a pool (e.g. HikariCP).
- `AppInitListener` runs once on Tomcat startup (`@WebListener` + `contextInitialized`) and creates two tables if they don't exist:
  - `messages (id, text)` — from the original Hello World DB demo
  - `login_history (id, username, role, login_time)` — logs every successful login
- The H2 web console is exposed at `/h2-console` (mapped in `web.xml`) so you can browse the DB directly in a browser. Connect with the same URL/user (`sa`, no password).

---

## Code execution flows

### 1. Fresh visitor hits `/`

```
Browser → GET /
   → index.jsp runs
      → session.getAttribute("username") is null
      → response.sendRedirect("/login")
   → Browser follows redirect → GET /login
      → AuthFilter does NOT intercept /login (not in its urlPatterns)
      → LoginServlet.doGet()
         → no session yet → forward to WEB-INF/views/login.jsp
   → Login form renders
```

### 2. User submits the login form

```
Browser → POST /login  (username=user, password=user)
   → AuthFilter not applied to /login → request goes straight to LoginServlet
   → LoginServlet.doPost()
      → authenticate("user","user") → matches → returns "user"
      → session = request.getSession(true)      // creates a new session
      → session.setAttribute("username", "user")
      → session.setAttribute("role", "user")
      → recordLogin("user","user")
           → DatabaseManager.getConnection()      // opens H2 connection
           → INSERT INTO login_history (username, role) VALUES ('user','user')
      → response.sendRedirect("/home")
   → Browser follows redirect → GET /home
```

If credentials are wrong: `authenticate()` returns `null`, `request.setAttribute("error", ...)` is set, and the request is **forwarded** (not redirected) back to `login.jsp`, which prints the error message.

### 3. `GET /home` (same pattern for `/messi`, `/ronaldo`)

```
Browser → GET /home
   → AuthFilter.doFilter() runs FIRST (registered on this path)
      → session = request.getSession(false)     // false = don't create one
      → loggedIn = session != null && session.getAttribute("username") != null
      → TRUE → chain.doFilter(req,res)  → passes control onward to HomeServlet
   → HomeServlet.doGet()
      → forward to WEB-INF/views/home.jsp
   → home.jsp renders:
      - reads session username/role directly (<%= session.getAttribute(...) %>)
      - always shows Messi / Ronaldo links
      - shows "Login History" link only if role == "admin"
```

If there's no valid session, `AuthFilter` short-circuits with `response.sendRedirect("/login")` and `HomeServlet` never runs.

### 4. Admin visits `/history`

```
Browser → GET /history
   → AuthFilter: session exists? → yes → chain.doFilter() → HistoryServlet
   → HistoryServlet.doGet()
      → role = session.getAttribute("role")
      → if role != "admin" → response.sendError(403) and STOP
      → else:
         → DatabaseManager.getConnection()
         → SELECT username, role, login_time FROM login_history ORDER BY login_time DESC
         → results wrapped into List<LoginRecord>, request.setAttribute("records", list)
         → forward to WEB-INF/views/history.jsp
   → history.jsp loops over the list and renders a table row per record
```

A logged-in `user` still passes the filter (they have a session) but gets a 403 from `HistoryServlet` itself, since role checks live in the servlet, not the filter.

### 5. Logout

```
Browser → GET /logout
   → AuthFilter doesn't protect /logout, request goes straight to LogoutServlet
   → LogoutServlet.doGet()
      → session = request.getSession(false)
      → if session exists → session.invalidate()   // wipes username/role, kills the session
      → response.sendRedirect("/login")
```

After this, hitting `/home` etc. again fails the `AuthFilter` check and bounces back to `/login`.

---

## Building & running

```bash
mvn clean package
cp target/servlets_template-1.0-SNAPSHOT.war $TOMCAT_HOME/webapps/app.war
```

Then visit `http://localhost:8080/app/`.

Or run locally with the embedded Tomcat plugin:

```bash
mvn tomcat7:run
```
