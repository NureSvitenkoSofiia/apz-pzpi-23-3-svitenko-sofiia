import { type RouteConfig, index, route, layout } from "@react-router/dev/routes";

export default [
  index("routes/home.tsx"),
  route("login", "routes/login.tsx"),

  layout("routes/_user.tsx", [
    route("jobs",     "routes/_user.jobs.tsx"),
    route("jobs/new", "routes/_user.jobs.new.tsx"),
  ]),

  layout("routes/_admin.tsx", [
    route("admin",             "routes/_admin.dashboard.tsx"),
    route("admin/printers",   "routes/_admin.printers.tsx"),
    route("admin/materials",  "routes/_admin.materials.tsx"),
    route("admin/users",      "routes/_admin.users.tsx"),
    route("admin/logs",       "routes/_admin.logs.tsx"),
    route("admin/export",     "routes/_admin.export.tsx"),
  ]),
] satisfies RouteConfig;
