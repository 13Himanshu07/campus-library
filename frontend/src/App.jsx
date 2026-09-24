import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './context/AuthContext'
import Layout from './components/Layout'
import AdminMembersPage from './pages/AdminMembersPage'
import AdminNotificationsPage from './pages/AdminNotificationsPage'
import { Login, Register, Forgot, ResetPassword, Landing, Dashboard, BooksPage, BorrowedPage, HistoryPage, NotificationsPage, ProfilePage, MembersPage, TransactionsPage, ReportsPage } from './pages/Pages'
function Guard({ children, role }) { const { user, ready } = useAuth(); if (!ready) return <div className="loading-screen"><span className="spinner"/> Loading your library…</div>; if (!user) return <Navigate to="/login" replace/>; if (role && user.role !== role) return <Navigate to={user.role==='LIBRARIAN'?'/librarian/dashboard':'/member/dashboard'} replace/>; return children }
function Workspace({ role }) { return <Guard role={role}><Layout/></Guard> }
export default function App(){
 const {user,ready}=useAuth()
 const landing=<Landing/>
 return <Routes>
  <Route path="/" element={landing}/>
  <Route path="/login" element={!ready?<div className="loading-screen"><span className="spinner"/></div>:user?<Navigate to={user.role==='LIBRARIAN'?'/librarian/dashboard':'/member/dashboard'}/>:<Login/>}/>
  <Route path="/register" element={<Register/>}/><Route path="/forgot-password" element={<Forgot/>}/><Route path="/reset-password" element={<ResetPassword/>}/>
  <Route path="/books" element={<Navigate to={user?'/member/books':'/login'}/>}/>
  <Route path="/member" element={<Workspace role="MEMBER"/>}><Route path="dashboard" element={<Dashboard/>}/><Route path="books" element={<BooksPage/>}/><Route path="borrowed" element={<BorrowedPage/>}/><Route path="history" element={<HistoryPage/>}/><Route path="notifications" element={<NotificationsPage/>}/><Route path="profile" element={<ProfilePage/>}/></Route>
  <Route path="/librarian" element={<Workspace role="LIBRARIAN"/>}><Route path="dashboard" element={<Dashboard/>}/><Route path="books" element={<BooksPage/>}/><Route path="members" element={<AdminMembersPage/>}/><Route path="transactions" element={<TransactionsPage/>}/><Route path="notifications" element={<AdminNotificationsPage/>}/><Route path="reports" element={<ReportsPage/>}/></Route>
  <Route path="*" element={<div className="loading-screen">Page not found · <a href="/">Return home</a></div>}/>
 </Routes>
}
