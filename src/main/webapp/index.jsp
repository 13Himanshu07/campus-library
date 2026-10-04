<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Campus Library · Find your next great read</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
</head>
<body>
  <div class="landing">
    <header class="landing-nav">
      <a class="brand" href="${pageContext.request.contextPath}/"><span class="brand-icon" aria-hidden="true">▤</span><b>Campus Library</b></a>
      <div>
        <a href="${pageContext.request.contextPath}/login" class="btn btn-quiet">Sign in</a>
        <a href="${pageContext.request.contextPath}/register" class="btn btn-dark">Join library <span aria-hidden="true">→</span></a>
      </div>
    </header>
    <main class="hero">
      <div class="hero-copy">
        <div class="eyebrow"><span class="eyebrow-line"></span> A PLACE FOR EVERY QUESTION</div>
        <h1>Find your next<br><em>great read.</em></h1>
        <p>Explore the campus collection, borrow books, and keep your learning moving — all in one calm, simple space.</p>
        <div class="hero-actions">
          <a class="btn btn-dark btn-lg" href="${pageContext.request.contextPath}/register">Create your account <span aria-hidden="true">→</span></a>
          <a class="text-link" href="${pageContext.request.contextPath}/login">Already a member? Sign in</a>
        </div>
        <div class="hero-note"><span class="hero-avatars" aria-hidden="true"><i>R</i><i>A</i><i>M</i></span><span>Made for a community of readers</span></div>
      </div>
      <div class="hero-art" role="img" aria-label="Three illustrated books on a shelf">
        <div class="art-sun"></div>
        <div class="art-book art-book-one"><span>THE ART OF<br>THINKING</span><small>AN INTRODUCTION</small></div>
        <div class="art-book art-book-two"><span>quiet<br>moments</span><small>ESSAYS &amp; STORIES</small></div>
        <div class="art-book art-book-three"><span>THE<br>BOTANICAL<br>WORLD</span><small>FIELD NOTES · VOL. 2</small></div>
        <div class="art-shelf"></div>
        <div class="art-caption"><span aria-hidden="true">✦</span> Your next chapter starts here</div>
      </div>
    </main>
    <footer class="landing-footer"><span>READ MORE. DISCOVER MORE.</span><span>OPEN TO EVERY MEMBER <b aria-hidden="true">→</b></span></footer>
  </div>
</body>
</html>
