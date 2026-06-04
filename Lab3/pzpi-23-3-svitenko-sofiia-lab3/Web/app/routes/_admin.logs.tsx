import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { api, unwrap } from "~/lib/api";

interface LogEntry { id: number; level: string; message: string; createdOn: string; source?: string; }

const LEVEL_COLORS: Record<string, string> = {
  information: "text-blue-600", warning: "text-yellow-600",
  error: "text-red-600", debug: "text-gray-500",
};

export function meta() { return [{ title: "Logs — Admin" }]; }

export default function LogsPage() {
  const { t, i18n } = useTranslation();
  const [logs, setLogs] = useState<LogEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [clearing, setClearing] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const res = await api.get("/api/admin/logs");
      setLogs(unwrap(res));
    } catch {
      // try system endpoint
      try { setLogs(unwrap(await api.get("/api/system/logs"))); } catch { setLogs([]); }
    } finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const clearLogs = async () => {
    if (!confirm(t("confirm_clear"))) return;
    setClearing(true);
    try { await api.post("/api/system/logs/clear"); load(); }
    finally { setClearing(false); }
  };

  const fmt = (d: string) =>
    new Intl.DateTimeFormat(i18n.language, { dateStyle: "short", timeStyle: "medium" }).format(new Date(d));

  return (
    <div>
      <div className="flex items-center justify-between mb-5">
        <h1 className="text-xl font-bold text-gray-900">{t("logs")}</h1>
        <button onClick={clearLogs} disabled={clearing}
          className="bg-red-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-red-700 disabled:opacity-50">
          {clearing ? t("loading") : t("clear_logs")}
        </button>
      </div>

      {loading ? (
        <p className="text-gray-500">{t("loading")}</p>
      ) : (
        <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-gray-50 text-gray-600">
              <tr>
                {[t("timestamp"), t("level"), t("message")].map((h, i) => (
                  <th key={i} className="px-4 py-3 text-left font-medium">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100">
              {logs.length === 0 ? (
                <tr><td colSpan={3} className="px-4 py-8 text-center text-gray-400">No logs</td></tr>
              ) : logs.map((l) => (
                <tr key={l.id} className="hover:bg-gray-50">
                  <td className="px-4 py-2 text-gray-400 text-xs font-mono whitespace-nowrap">{fmt(l.createdOn)}</td>
                  <td className="px-4 py-2">
                    <span className={`text-xs font-semibold uppercase ${LEVEL_COLORS[l.level?.toLowerCase()] ?? "text-gray-600"}`}>
                      {l.level}
                    </span>
                  </td>
                  <td className="px-4 py-2 text-gray-700 text-xs">{l.message}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
