"""
Locust load test for 3D Print Manager API
Usage:
  locust --headless -u 50 -r 5 --run-time 60s \
         --html results/1_pod.html \
         -H http://localhost:30080

Or with Docker Desktop NodePort:
  locust -H http://localhost:30080 --web-port 8089
  Then open http://localhost:8089
"""

from locust import HttpUser, task, between
import json
import random


class PrintingUser(HttpUser):
    """Звичайний користувач: перегляд завдань і матеріалів."""
    wait_time = between(0.5, 2)
    token: str = ""
    user_id: int = -1

    def on_start(self):
        resp = self.client.post(
            "/api/auth/login",
            json={"email": "test@example.com", "password": "Test1234!"},
            name="POST /auth/login"
        )
        if resp.status_code == 200:
            data = resp.json()
            self.token = data.get("token", "")
            self.user_id = data.get("userId", -1)

    def _auth(self):
        return {"Authorization": f"Bearer {self.token}"}

    @task(5)
    def get_my_jobs(self):
        if self.user_id > 0:
            self.client.get(
                f"/api/users/{self.user_id}/jobs",
                headers=self._auth(),
                name="GET /users/{id}/jobs"
            )

    @task(3)
    def get_materials(self):
        self.client.get(
            "/api/printjobs/materials",
            headers=self._auth(),
            name="GET /printjobs/materials"
        )

    @task(1)
    def health_check(self):
        self.client.get("/health", name="GET /health")


class AdminUser(HttpUser):
    """Адміністратор: аналітика і управління."""
    wait_time = between(1, 3)
    token: str = ""

    def on_start(self):
        resp = self.client.post(
            "/api/auth/login",
            json={"email": "admin@example.com", "password": "Admin1234!"},
            name="POST /auth/login (admin)"
        )
        if resp.status_code == 200:
            self.token = resp.json().get("token", "")

    def _auth(self):
        return {"Authorization": f"Bearer {self.token}"}

    @task(4)
    def get_system_stats(self):
        self.client.get(
            "/api/admin/analytics/statistics/system",
            headers=self._auth(),
            name="GET /analytics/statistics/system"
        )

    @task(2)
    def get_printers(self):
        self.client.get(
            "/api/admin/printers",
            headers=self._auth(),
            name="GET /admin/printers"
        )

    @task(2)
    def get_materials(self):
        self.client.get(
            "/api/admin/materials",
            headers=self._auth(),
            name="GET /admin/materials"
        )

    @task(1)
    def get_success_rates(self):
        self.client.get(
            "/api/admin/analytics/success-rates",
            headers=self._auth(),
            name="GET /analytics/success-rates"
        )

    @task(1)
    def get_users(self):
        self.client.get(
            "/api/admin/users",
            headers=self._auth(),
            name="GET /admin/users"
        )
