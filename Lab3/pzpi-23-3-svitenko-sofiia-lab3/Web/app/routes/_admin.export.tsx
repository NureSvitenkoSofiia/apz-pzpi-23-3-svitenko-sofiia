import { useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { api } from "~/lib/api";

export function meta() { return [{ title: "Export / Import — Admin" }]; }

export default function ExportPage() {
  const { t } = useTranslation();
  const fileRef = useRef<HTMLInputElement>(null);
  const [exporting, setExporting] = useState(false);
  const [importing, setImporting] = useState(false);
  const [msg, setMsg] = useState("");

  const exportJson = async () => {
    setExporting(true);
    setMsg("");
    try {
      const res = await api.post("/api/system/export/all", {}, { responseType: "blob" });
      const url = URL.createObjectURL(res.data);
      const a = document.createElement("a");
      a.href = url;
      a.download = `3dprint_export_${new Date().toISOString().slice(0, 10)}.json`;
      a.click();
      URL.revokeObjectURL(url);
    } catch {
      setMsg(t("error_network"));
    } finally {
      setExporting(false);
    }
  };

  const importData = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setImporting(true);
    setMsg("");
    try {
      const form = new FormData();
      form.append("file", file);
      await api.post("/api/system/import", form, { headers: { "Content-Type": "multipart/form-data" } });
      setMsg(t("save_ok"));
    } catch {
      setMsg(t("error_network"));
    } finally {
      setImporting(false);
      if (fileRef.current) fileRef.current.value = "";
    }
  };

  return (
    <div>
      <h1 className="text-xl font-bold text-gray-900 mb-6">{t("export_import")}</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-2xl">
        {/* Export */}
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h2 className="font-semibold text-gray-900 mb-2">{t("export_json")}</h2>
          <p className="text-sm text-gray-500 mb-4">{t("export_desc")}</p>
          <button
            onClick={exportJson} disabled={exporting}
            className="w-full bg-blue-600 text-white py-2 rounded-lg text-sm font-medium hover:bg-blue-700 disabled:opacity-50"
          >
            {exporting ? t("loading") : `↓ ${t("export_json")}`}
          </button>
        </div>

        {/* Import */}
        <div className="bg-white rounded-xl border border-gray-200 p-6">
          <h2 className="font-semibold text-gray-900 mb-2">{t("import_data")}</h2>
          <p className="text-sm text-gray-500 mb-4">Upload a JSON export file to restore data</p>
          <input ref={fileRef} type="file" accept=".json" onChange={importData} className="hidden" />
          <button
            onClick={() => fileRef.current?.click()} disabled={importing}
            className="w-full border-2 border-dashed border-gray-300 py-2 rounded-lg text-sm text-gray-600 hover:border-blue-400 hover:text-blue-600 disabled:opacity-50 transition-colors"
          >
            {importing ? t("loading") : `↑ ${t("import_data")}`}
          </button>
        </div>
      </div>

      {msg && (
        <p className={`mt-4 text-sm ${msg === t("save_ok") ? "text-green-600" : "text-red-600"}`}>{msg}</p>
      )}
    </div>
  );
}
