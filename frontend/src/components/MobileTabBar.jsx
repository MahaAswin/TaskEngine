import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Globe, Users, Lock, MessageCircle, Settings } from 'lucide-react';

const itemClass = ({ isActive }) =>
  `flex flex-1 flex-col items-center justify-center gap-0.5 py-2 text-[10px] font-medium sm:text-xs ${
    isActive ? 'text-accent' : 'text-slate-500'
  }`;

export default function MobileTabBar() {
  return (
    <nav
      className="fixed bottom-0 left-0 right-0 z-30 flex border-t border-slate-200 bg-white/95 pb-[env(safe-area-inset-bottom)] backdrop-blur md:hidden"
      aria-label="Primary"
    >
      <NavLink to="/dashboard" className={itemClass} aria-label="Dashboard">
        <LayoutDashboard className="h-5 w-5" aria-hidden />
        Home
      </NavLink>
      <NavLink to="/global" className={itemClass} aria-label="Global board">
        <Globe className="h-5 w-5" aria-hidden />
        Global
      </NavLink>
      <NavLink to="/teams" className={itemClass} aria-label="Teams">
        <Users className="h-5 w-5" aria-hidden />
        Teams
      </NavLink>
      <NavLink to="/private" className={itemClass} aria-label="Private tasks">
        <Lock className="h-5 w-5" aria-hidden />
        Private
      </NavLink>
      <NavLink to="/chat" className={itemClass} aria-label="Messages">
        <MessageCircle className="h-5 w-5" aria-hidden />
        Chat
      </NavLink>
      <NavLink to="/settings" className={itemClass} aria-label="Settings">
        <Settings className="h-5 w-5" aria-hidden />
        Settings
      </NavLink>
    </nav>
  );
}
