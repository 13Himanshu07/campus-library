<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Your reading desk"/><%@ include file="../shared/open.jspf" %>
<div class="page-header"><div><div class="eyebrow">YOUR READING SPACE</div><h1>Good to see you, <c:out value="${sessionScope.userName}"/>.</h1><p>Welcome back. Your next good read is only a few steps away.</p></div><a class="btn btn-dark" href="${pageContext.request.contextPath}/app/books">Browse books <span>→</span></a></div>
<div class="stats-grid">
  <article class="stat-card"><span class="stat-icon">▤</span><div class="stat-content"><span>Titles in collection</span><strong>${stats.titles}</strong><small>Across all subjects</small></div></article>
  <article class="stat-card"><span class="stat-icon">▣</span><div class="stat-content"><span>Available copies</span><strong>${stats.available}</strong><small>Ready to borrow</small></div></article>
  <article class="stat-card"><span class="stat-icon">◷</span><div class="stat-content"><span>Currently borrowed</span><strong>${activeIssues.size()}</strong><small>Books with you</small></div></article>
  <article class="stat-card"><span class="stat-icon rose">₹</span><div class="stat-content"><span>Unpaid fines</span><strong>₹${stats.unpaidFinesCents / 100}</strong><small>Calculated at return</small></div></article>
</div>
<div class="dashboard-grid member-grid">
  <section class="panel">
    <div class="panel-heading"><div><h2>On your reading desk</h2><p>Books you currently have borrowed</p></div><a class="text-link" href="${pageContext.request.contextPath}/app/my-books">See all →</a></div>
    <c:choose><c:when test="${not empty activeIssues}"><div class="book-list"><c:forEach items="${activeIssues}" var="loan" varStatus="loop"><c:if test="${loop.index < 4}"><div class="borrow-row"><span class="book-cover-mini">▤</span><div class="borrow-info"><b><c:out value="${loan.bookTitle}"/></b><span><c:out value="${loan.author}"/></span></div><div class="borrow-due"><small>DUE DATE</small><b><c:out value="${loan.dueDate}"/></b></div></div></c:if></c:forEach></div></c:when><c:otherwise><div class="empty-state"><span class="empty-icon">▤</span><b>Your reading desk is clear</b><span>Find something interesting in the collection.</span><a href="${pageContext.request.contextPath}/app/books" class="btn btn-outline">Browse books</a></div></c:otherwise></c:choose>
  </section>
  <section class="welcome-card"><span class="welcome-spark">✦</span><div class="eyebrow">A LITTLE INSPIRATION</div><h2>Every book is a door to a new idea.</h2><p>Take your time, follow your curiosity, and see where the pages take you.</p><a class="text-link" href="${pageContext.request.contextPath}/app/books">Explore the shelves →</a><div class="welcome-line"></div></section>
</div>
<%@ include file="../shared/close.jspf" %>
