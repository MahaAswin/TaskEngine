import { NavLink } from 'react-router-dom';
import { LayoutDashboard, ListTodo, Users, Settings } from 'lucide-react';

const itemClass = ({ isActive }) =>
  `flex flex-1 flex-col items-center justify-center gap-0.5 py-2 text-[10px] font-medium sm:text-xs ${
    isActive ? 'text-accent' : 'text-slate-500'
  }`;

export default function MobileTabBar({ isAdmin }) {
  return (
    <nav
      className="fixed bottom-0 left-0 right-0 z-30 flex border-t border-slate-200 bg-white/95 pb-[env(safe-area-inset-bottom)] backdrop-blur md:hidden"
      aria-label="Primary"
    >
      <NavLink to="/dashboard" className={itemClass} aria-label="Dashboard">
        <LayoutDashboard className="h-5 w-5" aria-hidden />
        Home
      </NavLink>
      <NavLink to="/tasks" className={itemClass} aria-label="Tasks">
        <ListTodo className="h-5 w-5" aria-hidden />
        Tasks
      </NavLink>
      {isAdmin && (
        <NavLink to="/team" className={itemClass} aria-label="Team">
          <Users className="h-5 w-5" aria-hidden />
          Team
        </NavLink>
      )}
      <NavLink to="/settings" className={itemClass} aria-label="Settings">
        <Settings className="h-5 w-5" aria-hidden />
        Settings
      </NavLink>
    </nav>
  );
}
