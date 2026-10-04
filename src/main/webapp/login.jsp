<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Sign in · Campus Library</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
  <main class="auth-page">
    <a class="auth-brand" href="${pageContext.request.contextPath}/"><span class="brand-icon" aria-hidden="true">▤</span><span>Campus Library</span></a>
    <section class="auth-card">
      <div class="eyebrow">WELCOME BACK</div>
      <h1>Sign in</h1>
      <p class="subtle">Sign in to pick up where you left off.</p>
      <c:if test="${not empty errorMessage}"><div class="notice"><span><c:out value="${errorMessage}"/></span></div></c:if>
      <c:if test="${not empty sessionScope.flashMessage}"><div class="notice success"><span><c:out value="${sessionScope.flashMessage}"/></span></div><c:remove var="flashMessage" scope="session"/></c:if>
      <form method="post" action="${pageContext.request.contextPath}/login" class="form-stack">
        <label>Email address<input type="email" name="email" required autocomplete="username" placeholder="you@college.edu" value="<c:out value='${param.email}'/>"/></label>
        <label>Password<input type="password" name="password" required autocomplete="current-password" placeholder="Your password"/></label>
        <div class="form-between"><span></span><span class="form-link-muted">Forgot password?</span></div>
        <button class="btn btn-dark btn-full">Sign in <span aria-hidden="true">→</span></button>
      </form>
      <div class="auth-foot">New to the library? <a href="${pageContext.request.contextPath}/register">Create an account</a></div>
    </section>
    <div class="auth-bottom">Thoughtfully made for curious minds <span>·</span> College Library Portal</div>
  </main>
</body>
</html>
