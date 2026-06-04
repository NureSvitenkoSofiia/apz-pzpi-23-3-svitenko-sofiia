import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { api, unwrap } from "~/lib/api";
import type { User } from "~/types";

export function meta() { return [{ title: "Users — Admin" }]; }

export default function UsersPage() {
  const { t, i18n } = useTranslation();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [editId, setEditId] = useState<number | null>(null);
  const [newRole, setNewRole] = useState("user");

  const load = async () => {
    try { setUsers(unwrap(await api.get("/api/admin/users"))); }
    catch { setUsers([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const saveRole = async (id: number) => {
    await api.put(`/api/admin/users/${id}/role`, { role: newRole });
    setEditId(null);
    load();
  };

  const fmt = (d: string) =>
    new Intl.DateTimeFormat(i18n.language, { dateStyle: "medium" }).format(new Date(d));

  if (loading) return <p className="text-gray-500">{t("loading")}</p>;

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-900 mb-5">{t("users")}</h1>
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600">
            <tr>
              {["ID", t("email"), t("role"), t("created"), ""].map((h, i) => (
                <th key={i} className="px-4 py-3 text-left font-medium">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {users.map((u) => (
              <tr key={u.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 text-gray-500">{u.id}</td>
                <td className="px-4 py-3 font-medium text-gray-900">{u.email}</td>
                <td className="px-4 py-3">
                  {editId === u.id ? (
                    <select value={newRole} onChange={(e) => setNewRole(e.target.value)}
                      className="border rounded px-2 py-1 text-sm">
                      <option value="user">user</option>
                      <option value="admin">admin</option>
                    </select>
                  ) : (
                    <span className={`px-2 py-0.5 rounded text-xs font-medium ${
                      u.role === "admin" ? "bg-purple-100 text-purple-700" : "bg-gray-100 text-gray-600"
                    }`}>{u.role}</span>
                  )}
                </td>
                <td className="px-4 py-3 text-gray-500">{fmt(u.createdOn)}</td>
                <td className="px-4 py-3">
                  {editId === u.id ? (
                    <div className="flex gap-2">
                      <button onClick={() => saveRole(u.id)} className="text-xs text-green-600 hover:underline">{t("save")}</button>
                      <button onClick={() => setEditId(null)} className="text-xs text-gray-400 hover:underline">{t("cancel")}</button>
                    </div>
                  ) : (
                    <button onClick={() => { setEditId(u.id); setNewRole(u.role); }}
                      className="text-xs text-blue-600 hover:underline">{t("edit")} {t("role")}</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
