import { Outlet, redirect } from "react-router";
import AdminSidebar from "~/components/AdminSidebar";

export async function clientLoader() {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");
  if (!token) throw redirect("/login");
  if (role !== "admin") throw redirect("/jobs");
  return null;
}

export default function AdminLayout() {
  return (
    <div className="flex min-h-screen">
      <AdminSidebar />
      <main className="flex-1 bg-gray-50 p-6 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
