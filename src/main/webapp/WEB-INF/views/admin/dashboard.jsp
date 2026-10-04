<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="Administrator overview"/><%@ include file="../shared/open.jspf" %>
<div class="page-header"><div><div class="eyebrow">ADMINISTRATION</div><h1>Library at a glance</h1><p>Review pending access and monitor the live catalog.</p></div><a class="btn btn-dark" href="${pageContext.request.contextPath}/admin/applications">Review applications <span>· ${stats.pendingLibrarians}</span></a></div>
<div class="stats-grid">
  <article class="stat-card"><span class="stat-icon">▤</span><div class="stat-content"><span>Titles in collection</span><strong>${stats.titles}</strong><small>${stats.copies} total copies</small></div></article>
  <article class="stat-card"><span class="stat-icon">▣</span><div class="stat-content"><span>Available copies</span><strong>${stats.available}</strong><small>Ready to borrow</small></div></article>
  <article class="stat-card"><span class="stat-icon">⇄</span><div class="stat-content"><span>Active loans</span><strong>${stats.openIssues}</strong><small>${stats.overdue} overdue</small></div></article>
  <article class="stat-card"><span class="stat-icon rose">✓</span><div class="stat-content"><span>Applications</span><strong>${stats.pendingLibrarians}</strong><small>Awaiting a decision</small></div></article>
</div>
<section class="panel table-panel"><div class="panel-heading"><div><h2>Recent transactions</h2><p>Latest activity from members</p></div><a class="text-link" href="${pageContext.request.contextPath}/admin/applications">Review applications →</a></div>
  <div class="table-scroll"><table><thead><tr><th>MEMBER</th><th>BOOK</th><th>ISSUED</th><th>DUE DATE</th><th>STATUS</th></tr></thead><tbody><c:forEach items="${recentIssues}" var="loan"><tr><td><c:out value="${loan.memberName}"/></td><td><c:out value="${loan.bookTitle}"/></td><td><c:out value="${loan.issueDate}"/></td><td><c:out value="${loan.dueDate}"/></td><td><span class="status ${loan.status == 'OVERDUE' ? 'overdue' : loan.status == 'RETURNED' ? 'returned' : 'issued'}"><c:out value="${loan.status}"/></span></td></tr></c:forEach></tbody></table></div>
  <c:if test="${empty recentIssues}"><div class="empty-inline">No transactions yet.</div></c:if>
</section>
<%@ include file="../shared/close.jspf" %>
