<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Library overview"/><%@ include file="../shared/open.jspf" %>
<div class="page-header"><div><div class="eyebrow">LIBRARIAN OVERVIEW</div><h1>Library at a glance</h1><p>A clear view of the collection, its people, and what is being read.</p></div><a class="btn btn-dark" href="${pageContext.request.contextPath}/librarian/books/add">Add a book <span>→</span></a></div>
<div class="stats-grid">
  <article class="stat-card"><span class="stat-icon">▤</span><div class="stat-content"><span>Titles in collection</span><strong>${stats.titles}</strong><small>Active catalog records</small></div></article>
  <article class="stat-card"><span class="stat-icon">▣</span><div class="stat-content"><span>Available copies</span><strong>${stats.available}</strong><small>${stats.copies} total copies</small></div></article>
  <article class="stat-card"><span class="stat-icon">⇄</span><div class="stat-content"><span>Books on loan</span><strong>${stats.openIssues}</strong><small>${stats.overdue} overdue</small></div></article>
  <article class="stat-card"><span class="stat-icon rose">♙</span><div class="stat-content"><span>Active students</span><strong>${stats.students}</strong><small>₹${stats.unpaidFinesCents / 100} in unpaid fines</small></div></article>
</div>
<div class="dashboard-grid member-grid">
  <section class="panel table-panel"><div class="panel-heading"><div><h2>Recent transactions</h2><p>Latest circulation activity</p></div><a class="text-link" href="${pageContext.request.contextPath}/librarian/issues">View all →</a></div>
    <div class="table-scroll"><table><thead><tr><th>MEMBER</th><th>BOOK</th><th>ISSUED</th><th>DUE DATE</th><th>STATUS</th></tr></thead><tbody><c:forEach items="${recentIssues}" var="loan"><tr><td><div class="table-person"><span class="avatar tiny">♙</span><div><b><c:out value="${loan.memberName}"/></b><small class="table-sub"><c:out value="${loan.memberEmail}"/></small></div></div></td><td><c:out value="${loan.bookTitle}"/></td><td><c:out value="${loan.issueDate}"/></td><td><c:out value="${loan.dueDate}"/></td><td><span class="status ${loan.status == 'OVERDUE' ? 'overdue' : loan.status == 'RETURNED' ? 'returned' : 'issued'}"><c:out value="${loan.status}"/></span></td></tr></c:forEach></tbody></table></div>
    <c:if test="${empty recentIssues}"><div class="empty-inline">No transactions yet.</div></c:if>
  </section>
  <section class="welcome-card"><span class="welcome-spark">✦</span><div class="eyebrow">THE CIRCULATION DESK</div><h2>Every return makes room for another story.</h2><p>Keep the collection moving with timely check-ins and a thoughtful welcome for every reader.</p><a class="text-link" href="${pageContext.request.contextPath}/librarian/issues">Manage transactions →</a><div class="welcome-line"></div></section>
</div>
<%@ include file="../shared/close.jspf" %>
