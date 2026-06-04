import { Outlet, redirect } from "react-router";
import Navbar from "~/components/Navbar";

export async function clientLoader() {
  const token = localStorage.getItem("token");
  if (!token) throw redirect("/login");
  return null;
}

export default function UserLayout() {
  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-5xl mx-auto px-4 py-6">
        <Outlet />
      </main>
    </div>
  );
}
