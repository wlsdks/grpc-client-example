from locust import HttpUser, task, between
import json

class GrpcVsFeignTest(HttpUser):
    wait_time = between(0.1, 0.5)
    token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqaW5hbkB0ZXN0LmNvbSIsImlhdCI6MTczNzcyNjY1MiwiZXhwIjoxNzM3NzMwMjUyfQ.XytkkongdksyVUk6kHXGVMzZCwyCdOETv8pLbR23l3A"

    def on_start(self):
        self.client.headers = {"Authorization": self.token}

    @task
    def test_grpc(self):
        self.client.get("/api/test/grpc", params={"memberId": "1"}, name="gRPC Call")

    @task
    def test_feign(self):
        self.client.get("/api/test/feign", params={"memberId": "1"}, name="Feign Call")