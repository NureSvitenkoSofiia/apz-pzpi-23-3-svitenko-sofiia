import { useTranslation } from "react-i18next";

const COLORS: Record<string, string> = {
  pending:   "bg-yellow-100 text-yellow-800",
  printing:  "bg-blue-100 text-blue-800",
  completed: "bg-green-100 text-green-800",
  failed:    "bg-red-100 text-red-800",
};

export default function StatusChip({ status }: { status: string }) {
  const { t } = useTranslation();
  const key = status.toLowerCase();
  const label = key === "completed" ? t("completed_s") : t(key, { defaultValue: status });
  return (
    <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold ${COLORS[key] ?? "bg-gray-100 text-gray-700"}`}>
      {label}
    </span>
  );
}
