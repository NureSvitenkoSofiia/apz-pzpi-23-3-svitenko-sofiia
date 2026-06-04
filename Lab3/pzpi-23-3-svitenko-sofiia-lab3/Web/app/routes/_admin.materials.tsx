import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { api, unwrap } from "~/lib/api";
import type { AdminMaterial } from "~/types";

export function meta() { return [{ title: "Materials — Admin" }]; }

const EMPTY = { color: "", colorHex: "#000000", materialType: "PLA", densityInGramsPerCm3: 1.24, diameterMm: 1.75 };

export default function MaterialsPage() {
  const { t } = useTranslation();
  const [materials, setMaterials] = useState<AdminMaterial[]>([]);
  const [loading, setLoading] = useState(true);
  const [editId, setEditId] = useState<number | null>(null);
  const [form, setForm] = useState(EMPTY);
  const [creating, setCreating] = useState(false);

  const load = async () => {
    try { setMaterials(unwrap(await api.get("/api/admin/materials"))); }
    catch { setMaterials([]); }
    finally { setLoading(false); }
  };

  useEffect(() => { load(); }, []);

  const startEdit = (m: AdminMaterial) => {
    setEditId(m.id);
    setCreating(false);
    setForm({ color: m.color, colorHex: m.colorHex || "#000000", materialType: m.materialType,
              densityInGramsPerCm3: m.densityInGramsPerCm3, diameterMm: m.diameterMm });
  };

  const save = async () => {
    const payload = { ...form, colorHex: form.colorHex.replace(/^#/, "") };
    if (creating) {
      await api.post("/api/admin/materials", payload);
    } else if (editId) {
      await api.put(`/api/admin/materials/${editId}`, payload);
    }
    setEditId(null); setCreating(false);
    load();
  };

  const remove = async (id: number) => {
    if (!confirm(t("delete") + "?")) return;
    await api.delete(`/api/admin/materials/${id}`);
    load();
  };

  if (loading) return <p className="text-gray-500">{t("loading")}</p>;

  return (
    <div>
      <div className="flex items-center justify-between mb-5">
        <h1 className="text-xl font-bold text-gray-900">{t("materials")}</h1>
        <button onClick={() => { setCreating(true); setEditId(null); setForm(EMPTY); }}
          className="bg-blue-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-blue-700">
          + {t("create")}
        </button>
      </div>

      {/* Create form */}
      {creating && (
        <div className="bg-white rounded-xl border border-blue-200 p-4 mb-4">
          <MaterialForm form={form} onChange={setForm} t={t} />
          <div className="flex gap-2 mt-3">
            <button onClick={save} className="bg-blue-600 text-white px-4 py-1.5 rounded-lg text-sm">{t("save")}</button>
            <button onClick={() => setCreating(false)} className="text-gray-500 text-sm">{t("cancel")}</button>
          </div>
        </div>
      )}

      <div className="bg-white rounded-xl border border-gray-200 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-gray-50 text-gray-600">
            <tr>
              {["ID", t("color"), t("type"), "Density", "Diam.", "Printers", ""].map((h, i) => (
                <th key={i} className="px-4 py-3 text-left font-medium">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100">
            {materials.map((m) => (
              <tr key={m.id} className="hover:bg-gray-50">
                <td className="px-4 py-3 text-gray-500">{m.id}</td>
                <td className="px-4 py-3">
                  {editId === m.id ? (
                    <input value={form.color} onChange={(e) => setForm({ ...form, color: e.target.value })}
                      className="border rounded px-2 py-1 text-sm w-full" />
                  ) : (
                    <span className="flex items-center gap-2">
                      <span className="w-4 h-4 rounded-full border" style={{ background: m.colorHex }} />
                      {m.color}
                    </span>
                  )}
                </td>
                <td className="px-4 py-3 text-gray-600">
                  {editId === m.id ? (
                    <select value={form.materialType} onChange={(e) => setForm({ ...form, materialType: e.target.value })}
                      className="border rounded px-2 py-1 text-sm">
                      {["PLA", "ABS", "PETG", "TPU"].map((t) => <option key={t}>{t}</option>)}
                    </select>
                  ) : m.materialType}
                </td>
                <td className="px-4 py-3 text-gray-500">{m.densityInGramsPerCm3}</td>
                <td className="px-4 py-3 text-gray-500">{m.diameterMm}</td>
                <td className="px-4 py-3 text-gray-500">{m.printersLoadedOn}</td>
                <td className="px-4 py-3">
                  <div className="flex gap-2">
                    {editId === m.id ? (
                      <>
                        <button onClick={save} className="text-xs text-green-600 hover:underline">{t("save")}</button>
                        <button onClick={() => setEditId(null)} className="text-xs text-gray-400 hover:underline">{t("cancel")}</button>
                      </>
                    ) : (
                      <>
                        <button onClick={() => startEdit(m)} className="text-xs text-blue-600 hover:underline">{t("edit")}</button>
                        <button onClick={() => remove(m.id)} className="text-xs text-red-500 hover:underline">{t("delete")}</button>
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

function MaterialForm({ form, onChange, t }: { form: any; onChange: any; t: any }) {
  return (
    <div className="grid grid-cols-3 gap-3">
      <div>
        <label className="text-xs text-gray-600">{t("color")}</label>
        <input value={form.color} onChange={(e) => onChange({ ...form, color: e.target.value })}
          className="border rounded px-2 py-1 text-sm w-full mt-0.5" placeholder="Red" />
      </div>
      <div>
        <label className="text-xs text-gray-600">{t("type")}</label>
        <select value={form.materialType} onChange={(e) => onChange({ ...form, materialType: e.target.value })}
          className="border rounded px-2 py-1 text-sm w-full mt-0.5">
          {["PLA", "ABS", "PETG", "TPU"].map((t) => <option key={t}>{t}</option>)}
        </select>
      </div>
      <div>
        <label className="text-xs text-gray-600">Color Hex</label>
        <input type="color" value={form.colorHex} onChange={(e) => onChange({ ...form, colorHex: e.target.value })}
          className="border rounded w-full mt-0.5 h-8" />
      </div>
    </div>
  );
}
