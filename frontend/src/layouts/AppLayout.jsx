import { Navigate, Outlet } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import TopBar from '../components/TopBar';
import ConfirmModal from '../components/ConfirmModal';
import CreateTaskModal from '../components/CreateTaskModal';
import TaskDrawer from '../components/TaskDrawer';
import MobileTabBar from '../components/MobileTabBar';
import { AppShellSkeleton } from '../components/skeletons';
import { useCurrentUser } from '../hooks/useCurrentUser';
import { useAppKeyboardShortcuts } from '../hooks/useAppKeyboardShortcuts';

export default function AppLayout() {
  const { data: user, isLoading, isError } = useCurrentUser();
  useAppKeyboardShortcuts();

  if (isError) {
    return <Navigate to="/login" replace />;
  }

  if (isLoading) {
    return <AppShellSkeleton />;
  }

  const isAdmin = user?.role === 'ADMIN';

  return (
    <div className="min-h-screen bg-slate-50 md:flex">
      <Sidebar isAdmin={isAdmin} />
      <div className="flex min-h-screen min-w-0 flex-1 flex-col pb-20 md:pb-0">
        <TopBar user={user} />
        <main className="flex-1 px-4 py-6 md:px-6 lg:px-8">
          <Outlet context={{ user }} />
        </main>
      </div>
      <MobileTabBar isAdmin={isAdmin} />
      <ConfirmModal />
      <CreateTaskModal user={user} />
      <TaskDrawer />
    </div>
  );
}
