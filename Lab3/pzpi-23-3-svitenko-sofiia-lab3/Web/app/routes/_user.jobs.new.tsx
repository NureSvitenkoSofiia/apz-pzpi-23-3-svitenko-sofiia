import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router";
import { useTranslation } from "react-i18next";
import { api, unwrap } from "~/lib/api";
import type { Material } from "~/types";

export function meta() {
  return [{ title: "New Job — 3D Print Manager" }];
}

interface PrinterOption { id: number; name: string }

export default function NewJobPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [materials, setMaterials] = useState<Material[]>([]);
  const [printers, setPrinters] = useState<PrinterOption[]>([]);
  const [selectedMaterial, setSelectedMaterial] = useState("");
  const [selectedPrinter, setSelectedPrinter] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const userId = localStorage.getItem("userId");

  useEffect(() => {
    api.get("/api/printjobs/materials").then((r) => {
      const mats: Material[] = unwrap(r);
      setMaterials(mats);
      // extract unique printers from materials
      const map = new Map<number, string>();
      mats.forEach((m) => m.availableOnPrinters.forEach((p) => map.set(p.printerId, p.printerName)));
      setPrinters(Array.from(map, ([id, name]) => ({ id, name })));
    });
  }, []);

  // filter printers available for selected material
  const availablePrinters = selectedMaterial
    ? (materials.find((m) => String(m.id) === selectedMaterial)?.availableOnPrinters ?? [])
        .filter((p) => p.quantityInG > 0)
        .map((p) => ({ id: p.printerId, name: p.printerName }))
    : printers;

  const onDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    const f = e.dataTransfer.files[0];
    if (f) setFile(f);
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!file || !selectedMaterial || !selectedPrinter) return;
    setError("");
    setLoading(true);
    try {
      const form = new FormData();
      form.append("userId", userId!);
      form.append("requiredMaterialId", selectedMaterial);
      form.append("printerId", selectedPrinter);
      form.append("stlFile", file);
      await api.post("/api/printjobs", form, { headers: { "Content-Type": "multipart/form-data" } });
      navigate("/jobs");
    } catch {
      setError(t("error_network"));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-lg">
      <h1 className="text-xl font-bold text-gray-900 mb-6">{t("new_job")}</h1>

      <form onSubmit={submit} className="space-y-5">
        {/* Drag & Drop Zone */}
        <div
          onClick={() => fileInputRef.current?.click()}
          onDragOver={(e) => { e.preventDefault(); setIsDragging(true); }}
          onDragLeave={() => setIsDragging(false)}
          onDrop={onDrop}
          className={`border-2 border-dashed rounded-xl p-8 text-center cursor-pointer transition-colors ${
            isDragging ? "border-blue-500 bg-blue-50" : "border-gray-300 hover:border-blue-400 hover:bg-gray-50"
          }`}
        >
          <input
            ref={fileInputRef} type="file" accept=".stl" className="hidden"
            onChange={(e) => setFile(e.target.files?.[0] ?? null)}
          />
          {file ? (
            <div>
              <p className="font-medium text-gray-900">✓ {file.name}</p>
              <p className="text-sm text-gray-500 mt-1">{(file.size / 1024).toFixed(0)} KB</p>
            </div>
          ) : (
            <p className="text-sm text-gray-500">{t("drag_drop")}</p>
          )}
        </div>

        {/* Material */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">{t("select_material")}</label>
          <select
            value={selectedMaterial}
            onChange={(e) => { setSelectedMaterial(e.target.value); setSelectedPrinter(""); }}
            required
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">— {t("select_material")} —</option>
            {materials.map((m) => (
              <option key={m.id} value={m.id}>
                {m.color} {m.materialType} ({m.availableOnPrinters.length} printer{m.availableOnPrinters.length !== 1 ? "s" : ""})
              </option>
            ))}
          </select>
        </div>

        {/* Printer */}
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">{t("select_printer")}</label>
          <select
            value={selectedPrinter} onChange={(e) => setSelectedPrinter(e.target.value)}
            required
            className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="">— {t("select_printer")} —</option>
            {availablePrinters.map((p) => (
              <option key={p.id} value={p.id}>{p.name}</option>
            ))}
          </select>
        </div>

        {error && <p className="text-sm text-red-600">{error}</p>}

        <button
          type="submit"
          disabled={loading || !file || !selectedMaterial || !selectedPrinter}
          className="w-full bg-blue-600 text-white py-2.5 rounded-lg text-sm font-semibold hover:bg-blue-700 disabled:opacity-40 transition-colors"
        >
          {loading ? t("loading") : t("submit")}
        </button>
      </form>
    </div>
  );
}
