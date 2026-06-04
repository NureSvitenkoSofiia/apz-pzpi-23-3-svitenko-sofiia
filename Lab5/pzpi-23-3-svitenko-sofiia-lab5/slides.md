---
marp: true
theme: default
paginate: true
backgroundColor: #f8fafc
color: #1e293b
style: |
  section {
    font-family: 'Segoe UI', Arial, sans-serif;
    font-size: 20px;
    padding: 40px 56px;
  }
  h1 { color: #2563eb; font-size: 42px; margin-bottom: 12px; }
  h2 { color: #2563eb; font-size: 28px; border-bottom: 2px solid #2563eb; padding-bottom: 8px; }
  table { font-size: 17px; width: 100%; border-collapse: collapse; }
  th { background: #2563eb; color: white; padding: 8px 12px; }
  td { padding: 6px 12px; border-bottom: 1px solid #e2e8f0; }
  tr:nth-child(even) td { background: #f1f5f9; }
  code { background: #e2e8f0; padding: 2px 6px; border-radius: 4px; font-size: 16px; }
  .cols { display: flex; gap: 32px; align-items: flex-start; }
  .col { flex: 1; }
  section.title { text-align: center; justify-content: center; }
  section.title h1 { font-size: 52px; }
  img[alt~="center"] { display: block; margin: 0 auto; }
---

<!-- _class: title -->
<!-- slide-001 -->

# PrintMaster
### Система управління 3D-друком

**Лабораторна робота №5 — Презентація готового проекту**

Світенко Софія · ПЗПІ-23-3 · 2026

---

<!-- slide-002 -->
## Зміст

1. Проблема та рішення
2. Загальна архітектура системи
3. Мобільний застосунок Android — Лаб 2
4. Компонентна діаграма Mobile
5. Веб-застосунок — Лаб 3
6. Компонентна діаграма Web
7. Серверна частина + Kubernetes — Лаб 4
8. UML-діаграма розгортання
9. Навантажувальне тестування Locust
10. Рольова модель
11. Заплановано та виконано
12. Підсумок

---

<!-- slide-003 -->
## Проблема та рішення

**Проблема:** підготовка 3D-завдань — ручний процес.
Локальний слайсер → ручне перенесення файлів → відстеження прогресу окремо на кожному пристрої.

**PrintMaster** автоматизує весь цикл:

| Крок | Без PrintMaster | З PrintMaster |
|---|---|---|
| Конвертація STL | Ручний запуск слайсера | Автоматично на сервері |
| Передача G-code | USB-флешка / SD-карта | API → ESP32 по мережі |
| Статус друку | Фізично біля принтера | Web + Android, real-time |
| Аналітика | Відсутня | Дашборд адміністратора |

> *«Від завантаження файлу до готової деталі — без єдиного термінального вікна»*

---

<!-- slide-004 -->
## Загальна архітектура системи

![h:490 center](./diagrams/deployment.png)

---

<!-- slide-005 -->
## Мобільний застосунок Android — Лаб 2

**Kotlin · Jetpack Compose · Ktor · SharedPreferences**

<div class="cols">
<div class="col">

**Екрани:**
- `LoginScreen` — JWT-автентифікація
- `JobListScreen` — список завдань + `StatusChip`
- `NewJobScreen` — STL picker + dropdowns
- `PrinterMapScreen` — Google Maps

</div>
<div class="col">

**Ключові рішення:**
- Ktor Auth Bearer — автоматичний JWT header
- Офлайн-кеш SharedPreferences (fallback без мережі)
- Локалізація EN / UK через `strings.xml`
- 4-шарова архітектура: UI → ViewModel → Repository → Data

</div>
</div>

---

<!-- slide-006 -->
## Компонентна діаграма — Android

![h:490 center](./diagrams/component-mobile.png)

---

<!-- slide-007 -->
## Веб-застосунок — Лаб 3

**React Router 7 · TypeScript · Tailwind v4 · Recharts · i18next**

<div class="cols">
<div class="col">

**Роль користувача:**
- `JobsPage` — список завдань зі `StatusChip`
- `NewJobPage` — drag-and-drop STL (Drag Events API)

**Роль адміністратора:**
- `DashboardPage` — PieChart + BarChart (Recharts)
- `PrintersPage` / `MaterialsPage` — inline CRUD
- `UsersPage`, `LogsPage`, `ExportImportPage`

</div>
<div class="col">

**Ключові рішення:**
- `clientLoader` у `_user.tsx` / `_admin.tsx` — рольові guards
- `lib/api.ts` — axios + interceptors + 401 auto-logout
- i18next + `LanguageDetector` (EN/UK)

</div>
</div>

---

<!-- slide-008 -->
## Компонентна діаграма — Web

![h:490 center](./diagrams/component-web.png)

---

<!-- slide-009 -->
## Серверна частина — Kubernetes — Лаб 4

**ASP.NET Core 10 · PostgreSQL 17 StatefulSet · HPA**

| Ресурс | Конфігурація |
|---|---|
| `Deployment api-3d` | 2 репліки, RollingUpdate (maxUnavailable: 0) |
| `readinessProbe` | `GET /health` → `SELECT 1` → PostgreSQL |
| `StatefulSet postgres-0` | PostgreSQL 17 + PVC 5 Gi |
| `HPA api-3d-hpa` | min: 1, max: 6 · CPU > 70% · Memory > 80% |
| `PVC api-data-pvc` | 10 Gi для STL та G-code файлів |
| `Service` | NodePort :30080 |

**Dockerfile:** двоетапна збірка `sdk:10.0` → `aspnet:10.0` + PrusaSlicer binary

---

<!-- slide-010 -->
## UML-діаграма розгортання

![h:490 center](./diagrams/deployment.png)

---

<!-- slide-011 -->
## Навантажувальне тестування Locust

**50 users · 5/s spawn · 60s · PrintingUser + AdminUser**

![h:390 center](./diagrams/load-test.png)

**1 → 2 репліки:** avg −90%, p95 −93% — усунуто CPU-bottleneck bcrypt при логіні  
**2 → 4 репліки:** плато — bottleneck PostgreSQL (єдиний StatefulSet)

---

<!-- slide-012 -->
## Рольова модель системи

![h:490 center](./diagrams/role-model.png)

---

<!-- slide-013 -->
## Заплановано та виконано

<div class="cols">
<div class="col">

![h:430](./diagrams/plan-done.png)

</div>
<div class="col">

**Виконано (14/17):**
- Vision & Scope, IoT ESP32
- Серверна: 11 контролерів, JWT, PrusaSlicer
- Android (Kotlin + Compose + Ktor)
- Web (React Router 7 + Recharts)
- Kubernetes + HPA + Locust

**Не виконано (3/17):**
- G-code парсинг у бекенді
- ReadWriteMany PVC (NFS / MinIO)
- ШІ-модуль підбору параметрів

</div>
</div>

---

<!-- slide-014 -->
## Підсумок

<div class="cols">
<div class="col">

**Реалізовано:**
- 4 компоненти системи
- 11 REST-контролерів
- Горизонтальне масштабування k8s
- Avg response time −90% при 1→2 репліки
- Zero downtime deployment

</div>
<div class="col">

**Наступні релізи:**
- NFS / MinIO для ReadWriteMany
- PgBouncer при > 4 репліках
- ШІ-модуль підбору матеріалу та параметрів за STL

</div>
</div>

---

<!-- _class: title -->
<!-- slide-015 -->

# Дякую за увагу

**PrintMaster** · Світенко Софія · ПЗПІ-23-3