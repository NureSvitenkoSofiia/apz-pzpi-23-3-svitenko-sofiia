import { Link, useNavigate } from "react-router";
import { useTranslation } from "react-i18next";
import LanguageSwitcher from "./LanguageSwitcher";

export default function Navbar() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const logout = () => {
    localStorage.clear();
    navigate("/login");
  };

  return (
    <nav className="bg-white border-b border-gray-200 px-6 py-3 flex items-center justify-between">
      <div className="flex items-center gap-6">
        <span className="font-bold text-blue-600 text-lg">3D Print Manager</span>
        <Link to="/jobs" className="text-sm text-gray-600 hover:text-blue-600 transition-colors">
          {t("my_jobs")}
        </Link>
        <Link to="/jobs/new" className="text-sm text-gray-600 hover:text-blue-600 transition-colors">
          {t("new_job")}
        </Link>
      </div>
      <div className="flex items-center gap-3">
        <LanguageSwitcher />
        <button onClick={logout} className="text-sm text-gray-500 hover:text-red-600 transition-colors">
          {t("logout")}
        </button>
      </div>
    </nav>
  );
}
