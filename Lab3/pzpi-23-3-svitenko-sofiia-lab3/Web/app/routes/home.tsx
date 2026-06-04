import { redirect } from "react-router";

export async function clientLoader() {
  const token = localStorage.getItem("token");
  const role = localStorage.getItem("role");
  if (!token) throw redirect("/login");
  throw redirect(role === "admin" ? "/admin" : "/jobs");
}

export default function Home() {
  return null;
}
