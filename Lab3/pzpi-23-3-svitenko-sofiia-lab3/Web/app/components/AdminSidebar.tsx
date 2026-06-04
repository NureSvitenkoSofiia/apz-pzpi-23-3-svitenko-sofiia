import { NavLink, useNavigate } from "react-router";
import { useTranslation } from "react-i18next";
import LanguageSwitcher from "./LanguageSwitcher";

const links = [
  { to: "/admin",           key: "dashboard" },
  { to: "/admin/printers",  key: "printers" },
  { to: "/admin/materials", key: "materials" },
  { to: "/admin/users",     key: "users" },
  { to: "/admin/logs",      key: "logs" },
  { to: "/admin/export",    key: "export_import" },
];

export default function AdminSidebar() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const logout = () => { localStorage.clear(); navigate("/login"); };

  return (
    <aside className="w-56 bg-gray-900 text-white flex flex-col min-h-screen shrink-0">
      <div className="px-5 py-4 border-b border-gray-700">
        <p className="font-bold text-blue-400">3D Print Manager</p>
        <p className="text-xs text-gray-400 mt-0.5">Admin Panel</p>
      </div>

      <nav className="flex-1 py-4 space-y-1 px-2">
        {links.map((l) => (
          <NavLink
            key={l.to}
            to={l.to}
            end={l.to === "/admin"}
            className={({ isActive }) =>
              `block px-3 py-2 rounded-md text-sm transition-colors ${
                isActive ? "bg-blue-600 text-white" : "text-gray-300 hover:bg-gray-800"
              }`
            }
          >
            {t(l.key)}
          </NavLink>
        ))}
      </nav>

      <div className="px-4 pb-4 space-y-2 border-t border-gray-700 pt-3">
        <LanguageSwitcher />
        <button onClick={logout} className="text-xs text-gray-400 hover:text-red-400 w-full text-left transition-colors">
          {t("logout")}
        </button>
      </div>
    </aside>
  );
}
