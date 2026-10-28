const form = document.querySelector("#login-form");
const error = document.querySelector("#login-error");

form.addEventListener("submit", async (event) => {
  event.preventDefault();
  error.hidden = true;
  await fetch("/csrf", { credentials: "same-origin" });
  const token = document.cookie.split("; ").find((cookie) => cookie.startsWith("XSRF-TOKEN="))?.split("=").slice(1).join("=");
  const body = new URLSearchParams(new FormData(form));
  const response = await fetch("/login", { method: "POST", credentials: "same-origin", headers: { "Content-Type": "application/x-www-form-urlencoded", "X-XSRF-TOKEN": decodeURIComponent(token || "") }, body });
  if (response.redirected && !response.url.includes("login.html")) window.location.assign("/");
  else error.hidden = false;
});

if (new URLSearchParams(location.search).has("erro")) error.hidden = false;
