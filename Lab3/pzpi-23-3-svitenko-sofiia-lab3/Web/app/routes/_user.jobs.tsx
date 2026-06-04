import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { api, unwrap } from "~/lib/api";
import StatusChip from "~/components/StatusChip";
import type { PrintJob } from "~/types";

function fmt(date: string | null, lang: string) {
  if (!date) return "—";
  return new Intl.DateTimeFormat(lang, { dateStyle: "medium", timeStyle: "short" }).format(new Date(date));
}

export function meta() {
  return [{ title: "My Jobs — 3D Print Manager" }];
}

export default function JobsPage() {
  const { t, i18n } = useTranslation();
  const [jobs, setJobs] = useState<PrintJob[]>([]);
  const [loading, setLoading] = useState(true);

  const userId = localStorage.getItem("userId");

  const load = async () => {
    try {
      const res = await api.get(`/api/users/${userId}/jobs`);
      setJobs(unwrap(res));
    } catch {
      setJobs([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  if (loading) return <p className="text-gray-500">{t("loading")}</p>;

  return (
    <div>
      <div className="flex items-center justify-between mb-5">
        <h1 className="text-xl font-bold text-gray-900">{t("my_jobs")}</h1>
        <button onClick={load} className="text-sm text-blue-600 hover:underline">{t("loading") === t("loading") ? "↻ Refresh" : "↻"}</button>
      </div>

      {jobs.length === 0 ? (
        <div className="bg-white rounded-xl border border-gray-200 p-12 text-center text-gray-500">
          {t("no_jobs")}
        </div>
      ) : (
        <div className="space-y-3">
          {jobs.map((job) => (
            <div key={job.id} className="bg-white rounded-xl border border-gray-200 p-5">
              <div className="flex items-start justify-between">
                <div>
                  <p className="font-semibold text-gray-900">{t("job_id", { id: job.id })}</p>
                  <p className="text-sm text-gray-600 mt-0.5">
                    {job.requiredMaterial.color} {job.requiredMaterial.materialType} · {job.printer.name}
                  </p>
                </div>
                <StatusChip status={job.status} />
              </div>
              <div className="grid grid-cols-3 gap-4 mt-4 text-sm text-gray-500">
                <div>
                  <p className="font-medium text-gray-700">{t("created")}</p>
                  <p>{fmt(job.createdOn, i18n.language)}</p>
                </div>
                <div>
                  <p className="font-medium text-gray-700">{t("est_time")}</p>
                  <p>{job.estimatedPrintTimeMinutes > 0 ? `${Math.round(job.estimatedPrintTimeMinutes)} min` : "—"}</p>
                </div>
                <div>
                  <p className="font-medium text-gray-700">{t("est_material")}</p>
                  <p>{job.estimatedMaterialInGrams > 0 ? `${job.estimatedMaterialInGrams.toFixed(1)} g` : "—"}</p>
                </div>
              </div>
              {job.errorMessage && (
                <p className="mt-3 text-sm text-red-600 bg-red-50 rounded-lg px-3 py-2">
                  {t("error")}: {job.errorMessage}
                </p>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
