import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  ListTodo,
  Users,
  Settings,
} from 'lucide-react';
const linkClass = ({ isActive }) =>
  `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition ${
    isActive
      ? 'bg-sidebar-active text-white border-l-[3px] border-accent pl-[9px]'
      : 'border-l-[3px] border-transparent text-slate-300 hover:bg-white/5 hover:text-white'
  }`;

export default function Sidebar({ isAdmin }) {
  const content = (
    <>
      <div className="px-4 py-6 md:px-6">
        <div className="flex items-center gap-2">
          <span className="flex h-9 w-9 items-center justify-center rounded-lg bg-accent text-sm font-bold text-white">
            TE
          </span>
          <span className="text-lg font-semibold tracking-tight text-white">TaskEngine</span>
        </div>
      </div>
      <nav className="flex flex-1 flex-col gap-1 px-3 pb-6">
        <NavLink to="/dashboard" className={linkClass}>
          <LayoutDashboard className="h-5 w-5 shrink-0" />
          Dashboard
        </NavLink>
        <NavLink to="/tasks" className={linkClass}>
          <ListTodo className="h-5 w-5 shrink-0" />
          Tasks
        </NavLink>
        {isAdmin && (
          <NavLink to="/team" className={linkClass}>
            <Users className="h-5 w-5 shrink-0" />
            Team
          </NavLink>
        )}
        <NavLink to="/settings" className={linkClass}>
          <Settings className="h-5 w-5 shrink-0" />
          Settings
        </NavLink>
      </nav>
    </>
  );

  return (
    <aside className="hidden w-64 shrink-0 flex-col bg-sidebar text-white shadow-xl md:flex">
      {content}
    </aside>
  );
}
