import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { api, unwrap } from "~/lib/api";
import StatusChip from "~/components/StatusChip";
import type { Printer } from "~/types";

export function meta() { return [{ title: "Printers — Admin" }]; }

export default function PrintersPage() {
  const { t } = useTranslation();
  const [printers, setPrinters] = useState<Printer[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState({ name: "", ipAddress: "" });

  const load = async () => {
    try { setPrinters(unwrap(await api.get("/api/admin/printers"))); }
    catch { setPrinters([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const startEdit = (p: Printer) => { setEditingId(p.id); setForm({ name: p.name, ipAddress: p.ipAddress }); };

  const save = async () => {
    if (!editingId) return;
    await api.put(`/api/admin/printers/${editingId}`, form);
    setEditingId(null);
    load();
  };

  const remove = async (id: number) => {
    if (!confirm(t("delete") + "?")) return;
    await api.delete(`/api/admin/printers/${id}`);
    load();
  };

  if (loading) return <p className="text-gray-500">{t("loading")}</p>;

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-900 mb-5">{t("printers")}</h1>
      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600">
            <tr>
              {["ID", t("name"), t("ip"), t("status"), t("pending_jobs"), t("completed_jobs"), ""].map((h, i) => (
                <th key={i} className="px-4 py-3 text-left font-medium">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {printers.map((p) => (
              <tr key={p.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 text-gray-500">{p.id}</td>
                <td className="px-4 py-3 font-medium text-gray-900">
                  {editingId === p.id ? (
                    <input value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
                      className="border rounded px-2 py-1 text-sm w-full" />
                  ) : p.name}
                </td>
                <td className="px-4 py-3 text-gray-500 font-mono text-xs">
                  {editingId === p.id ? (
                    <input value={form.ipAddress} onChange={(e) => setForm({ ...form, ipAddress: e.target.value })}
                      className="border rounded px-2 py-1 text-sm font-mono w-full" />
                  ) : p.ipAddress}
                </td>
                <td className="px-4 py-3"><StatusChip status={p.status} /></td>
                <td className="px-4 py-3 text-gray-600">{p.pendingJobsCount}</td>
                <td className="px-4 py-3 text-gray-600">{p.completedJobsCount}</td>
                <td className="px-4 py-3">
                  <div className="flex gap-2">
                    {editingId === p.id ? (
                      <>
                        <button onClick={save} className="text-xs text-green-600 hover:underline">{t("save")}</button>
                        <button onClick={() => setEditingId(null)} className="text-xs text-gray-400 hover:underline">{t("cancel")}</button>
                      </>
                    ) : (
                      <>
                        <button onClick={() => startEdit(p)} className="text-xs text-blue-600 hover:underline">{t("edit")}</button>
                        <button onClick={() => remove(p.id)} className="text-xs text-red-500 hover:underline">{t("delete")}</button>
                      </>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
