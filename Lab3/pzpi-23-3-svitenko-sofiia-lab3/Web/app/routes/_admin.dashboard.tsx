import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { BarChart, Bar, XAxis, YAxis, Tooltip, PieChart, Pie, Cell, Legend, ResponsiveContainer } from "recharts";
import { api, unwrap } from "~/lib/api";
import type { SystemStats, SuccessRates, MaterialConsumption } from "~/types";


function StatCard({ label, value }: { label: string; value: string | number }) {
  return (
    <div className="bg-white rounded-xl border border-gray-200 p-5">
      <p className="text-xs text-gray-500 uppercase tracking-wide">{label}</p>
      <p className="text-2xl font-bold text-gray-900 mt-1">{value}</p>
    </div>
  );
}

export function meta() {
  return [{ title: "Dashboard — Admin" }];
}

export default function DashboardPage() {
  const { t } = useTranslation();
  const [stats, setStats] = useState<SystemStats | null>(null);
  const [rates, setRates] = useState<SuccessRates | null>(null);
  const [consumption, setConsumption] = useState<MaterialConsumption | null>(null);

  useEffect(() => {
    api.get("/api/admin/analytics/statistics/system").then((r) => setStats(unwrap(r))).catch(() => {});
    api.get("/api/admin/analytics/success-rates").then((r) => setRates(unwrap(r))).catch(() => {});
    api.get("/api/admin/analytics/material-consumption").then((r) => setConsumption(unwrap(r))).catch(() => {});
  }, []);

  const statusData = stats
    ? [
        { name: t("completed_s"), value: stats.completedJobs, color: "#22c55e" },
        { name: t("failed"),      value: stats.failedJobs,    color: "#ef4444" },
        { name: t("pending"),     value: stats.pendingJobs,   color: "#3b82f6" },
      ].filter((d) => d.value > 0)
    : [];

  const matData = consumption
    ? Object.entries(consumption.consumptionByMaterialType).map(([materialType, totalGrams]) => ({
        materialType,
        totalGrams,
        jobCount: consumption.jobCountByMaterialType[materialType] ?? 0,
      }))
    : [];

  return (
    <div className="space-y-6">
      <h1 className="text-xl font-bold text-gray-900">{t("dashboard")}</h1>

      {/* Stat cards */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label={t("total_jobs")} value={stats?.totalPrintJobs ?? "—"} />
        <StatCard label={t("success_rate")} value={stats ? `${stats.overallSuccessRate.toFixed(1)}%` : "—"} />
        <StatCard label={t("active_printers")} value={stats?.onlinePrinters ?? "—"} />
        <StatCard label={t("registered_users")} value={stats?.totalUsers ?? "—"} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Jobs by status — Pie */}
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <p className="text-sm font-semibold text-gray-700 mb-4">{t("jobs_by_status")}</p>
          <ResponsiveContainer width="100%" height={220}>
            <PieChart>
              <Pie data={statusData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                {statusData.map((d, i) => <Cell key={i} fill={d.color} />)}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        </div>

        {/* Material consumption — Bar */}
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <p className="text-sm font-semibold text-gray-700 mb-4">{t("material_consumption")}</p>
          <ResponsiveContainer width="100%" height={220}>
            <BarChart data={matData}>
              <XAxis dataKey="materialType" tick={{ fontSize: 12 }} />
              <YAxis tick={{ fontSize: 12 }} />
              <Tooltip />
              <Bar dataKey="totalGrams" fill="#3b82f6" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Success rate by printer */}
      {rates && Object.keys(rates.successRateByPrinter).length > 0 && (() => {
        const printerData = Object.entries(rates.successRateByPrinter).map(([name, rate]) => ({
          printerName: name,
          successRate: rate,
        }));
        return (
          <div className="bg-white rounded-xl border border-gray-200 p-5">
            <p className="text-sm font-semibold text-gray-700 mb-4">Success Rate by Printer</p>
            <ResponsiveContainer width="100%" height={200}>
              <BarChart data={printerData}>
                <XAxis dataKey="printerName" tick={{ fontSize: 12 }} />
                <YAxis tickFormatter={(v) => `${v.toFixed(0)}%`} tick={{ fontSize: 12 }} />
                <Tooltip formatter={(v) => typeof v === "number" ? `${v.toFixed(1)}%` : v} />
                <Bar dataKey="successRate" fill="#22c55e" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        );
      })()}
    </div>
  );
}
