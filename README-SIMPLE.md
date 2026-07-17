# How This App Works (Simple Version)

This is a beginner-friendly explanation of the login project — no prior Java or web knowledge assumed. If you want the full technical version, see `README.md`.

---

## The big picture

Think of this app like a **building with a front desk**:

- You can't just walk into any room. First you check in at the front desk (login page).
- The front desk gives you a **wristband** (this is called a "session") that says who you are and whether you're a VIP (admin) or a regular guest (user).
- Every room checks your wristband before letting you in.
- If you don't have a wristband, you get sent back to the front desk.
- One special room (Login History) only lets in VIPs — even if you have a wristband, the room checks if it says "VIP" on it.

---

## The main pieces, explained like a recipe

| File | What it actually does | Think of it as... |
|---|---|---|
| `LoginServlet.java` | Checks if username/password match, gives out the "wristband" (session) | The front desk clerk |
| `AuthFilter.java` | Checks every visitor has a wristband before they enter certain rooms | The security guard standing at each door |
| `HomeServlet.java`, `MessiServlet.java`, `RonaldoServlet.java` | Show a page once you're let in | The room itself |
| `HistoryServlet.java` | Same as above, but ALSO checks your wristband says "VIP" | A VIP lounge with its own bouncer |
| `LogoutServlet.java` | Takes back your wristband | Checking out at the front desk |
| `DatabaseManager.java` | Opens a connection to the little database file (H2) | The building's filing cabinet |
| The `.jsp` files | The actual HTML you see on screen | The decoration/furniture in each room |

---

## What is a "session"?

When you log in, the server needs to remember "this browser belongs to admin" for every page you visit afterward — otherwise it would forget who you are the moment you click a link.

It does this with a **session**: a little bit of memory tied to your browser (using a cookie behind the scenes) that stores things like:

```
username = "admin"
role = "admin"
```

Every page can peek into this session to check who's asking.

---

## Walking through what happens when you use the app

### 1. You open the website for the first time

- The very first page (`index.jsp`) checks: "Do you already have a wristband (session)?"
- No wristband yet → you get sent to the **login page**.

### 2. You type in a username and password and click "Log In"

- `LoginServlet` checks: does `admin`/`admin` or `user`/`user` match what you typed?
- ✅ **Match** → you get a wristband (session) saying your username and role. The clerk also **writes your visit down in a logbook** (the `login_history` table in the database) — just like a hotel keeps a guest log.
- ❌ **No match** → you're sent right back to the login page with a "wrong password" message.

### 3. You land on the Home page

- Before `HomeServlet` even runs, the **security guard** (`AuthFilter`) checks your wristband.
- Got one? → Guard lets you through, you see the Home page with a menu.
- No wristband? → Guard sends you straight back to the login page. `HomeServlet` never even gets to run.

### 4. You click "Messi" or "Ronaldo"

- Exact same idea as Home: security guard checks your wristband first, then the servlet shows the page.

### 5. You (as admin) click "Login History"

- The security guard checks: got a wristband? Yes → lets you through the door.
- But THIS room has its **own extra bouncer** inside: it checks "does your wristband say VIP (admin)?"
  - If you're `admin` → you get in, and see a table listing everyone who has ever logged in, pulled from the database logbook.
  - If you're a regular `user` → you get blocked with a "Forbidden" message, even though you had a valid wristband.

This is why a regular user with a valid session still can't see the History page — the wristband works for the front door, but this particular room checks more than just "do you have one," it checks *what it says*.

### 6. You click "Log out"

- `LogoutServlet` **rips up your wristband** (invalidates the session).
- You're sent back to the login page. If you try to revisit Home/Messi/Ronaldo/History now, the guard stops you again since you have no wristband anymore.

---

## Where does the data (login logbook) actually live?

There's a tiny database called **H2** running inside the app — it's just a file on disk, no separate database server needed. It's like a spreadsheet file that Java code can read and write to using SQL commands (`INSERT`, `SELECT`).

Every time someone logs in successfully, this line of code runs:

```sql
INSERT INTO login_history (username, role) VALUES ('admin', 'admin')
```

And when the admin opens the History page, this runs:

```sql
SELECT username, role, login_time FROM login_history ORDER BY login_time DESC
```

...which just means "give me every row, newest first."

---

## Quick glossary

- **Servlet** — a Java class that handles a web request (like `/home`) and decides what to show.
- **JSP** — a file that mixes HTML with little bits of Java, used to actually draw the page.
- **Filter** — code that runs *before* a servlet, usually to check permissions.
- **Session** — server-side memory tied to your browser, used to remember you're logged in.
- **Forward vs Redirect** — `forward` quietly hands the request to another page on the server (URL in the browser doesn't change). `redirect` tells the *browser* "go fetch this other URL yourself" (URL changes, and it's a whole new request).
- **SQL** — the language used to talk to a database (`SELECT`, `INSERT`, etc).

---

## TL;DR

1. `index.jsp` → routes you to login or home
2. `LoginServlet` → checks password, hands out a session, logs it to H2
3. `AuthFilter` → guards Home/Messi/Ronaldo/History — no session, no entry
4. `HistoryServlet` → guards itself further — no admin role, no entry
5. `LogoutServlet` → destroys the session
