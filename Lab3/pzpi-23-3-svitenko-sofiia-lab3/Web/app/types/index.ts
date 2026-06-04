export interface LoginResponse {
  token: string; userId: number; email: string; role: string; expiresAt: string;
}
export interface PrintJob {
  id: number; status: string;
  estimatedMaterialInGrams: number; estimatedPrintTimeMinutes: number;
  actualMaterialInGrams: number;
  createdOn: string; startedAt: string | null; completedAt: string | null;
  errorMessage: string | null;
  requiredMaterial: { id: number; color: string; materialType: string };
  printer: { id: number; name: string; status: string };
  stlFilePath: string;
}
export interface Material {
  id: number; color: string; colorHex: string; materialType: string;
  densityInGramsPerCm3: number; diameterMm: number;
  availableOnPrinters: { printerId: number; printerName: string; quantityInG: number }[];
}
export interface AdminMaterial {
  id: number; color: string; colorHex: string; materialType: string;
  densityInGramsPerCm3: number; diameterMm: number;
  createdOn: string; printersLoadedOn: number; totalUsageGrams: number;
}
export interface Printer {
  id: number; name: string; status: string; ipAddress: string;
  lastPing: string; createdOn: string;
  loadedMaterials: { materialId: number; materialType: string; color: string; quantityInG: number }[];
  pendingJobsCount: number; completedJobsCount: number;
}
export interface User {
  id: number; email: string; role: string; createdOn: string;
}
export interface SystemStats {
  totalPrintJobs: number; completedJobs: number; failedJobs: number; pendingJobs: number;
  overallSuccessRate: number; totalMaterialUsedGrams: number;
  totalPrintTimeHours: number; onlinePrinters: number; totalUsers: number;
}
export interface SuccessRates {
  overallSuccessRate: number;
  successRateByMaterial: Record<string, number>;
  successRateByPrinter: Record<string, number>;
}
export interface MaterialConsumption {
  totalMaterialUsedGrams: number;
  consumptionByMaterialType: Record<string, number>;
  jobCountByMaterialType: Record<string, number>;
}
