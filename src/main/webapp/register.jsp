<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Join the library · Campus Library</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
  <main class="auth-page">
    <a class="auth-brand" href="${pageContext.request.contextPath}/"><span class="brand-icon" aria-hidden="true">▤</span><span>Campus Library</span></a>
    <section class="auth-card auth-card-wide">
      <div class="eyebrow">YOUR CAMPUS, YOUR LIBRARY</div>
      <h1>Join the library</h1>
      <p class="subtle">Create a member account or apply for librarian access.</p>
      <c:if test="${not empty errorMessage}"><div class="notice"><span><c:out value="${errorMessage}"/></span></div></c:if>
      <form method="post" action="${pageContext.request.contextPath}/register" class="form-stack">
        <div class="form-row">
          <label>Full name<input name="name" required maxlength="120" placeholder="Your name" autocomplete="name"/></label>
          <label>Membership ID <span class="optional">(optional)</span><input name="membershipId" maxlength="40" placeholder="Issued by your college"/></label>
        </div>
        <label>Email address<input type="email" name="email" required maxlength="190" placeholder="you@college.edu" autocomplete="email"/></label>
        <div class="form-row">
          <label>Phone number <span class="optional">(optional)</span><input name="phone" maxlength="40" placeholder="+91 98765 43210" autocomplete="tel"/></label>
          <label>Password<input type="password" name="password" required minlength="10" autocomplete="new-password" placeholder="At least 10 characters"/><small>Use at least 10 characters.</small></label>
        </div>
        <label class="librarian-request"><input type="checkbox" name="librarianApplication"/><span>Apply for librarian access<small>Requires administrator approval before you can sign in as a librarian.</small></span></label>
        <button class="btn btn-dark btn-full">Create account <span aria-hidden="true">→</span></button>
        <small class="form-hint">Member access is active immediately. An administrator account is set up privately by the deployment owner.</small>
      </form>
      <div class="auth-foot">Already registered? <a href="${pageContext.request.contextPath}/login">Sign in</a></div>
    </section>
    <div class="auth-bottom">Thoughtfully made for curious minds <span>·</span> College Library Portal</div>
  </main>
</body>
</html>
