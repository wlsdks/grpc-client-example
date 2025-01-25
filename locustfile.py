from locust import HttpUser, task, between
import json


class GrpcVsFeignTest(HttpUser):
    wait_time = between(0.1, 0.5)
    token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqaW5hbkB0ZXN0LmNvbSIsImlhdCI6MTczNzc3MzIyOCwiZXhwIjoxNzM3Nzc2ODI4fQ.4jCCLyXzFOdPApfTSFYEALWeYXqd1C9XCYvhgJhbmjk"

    def on_start(self):
        self.client.headers = {"Authorization": self.token}


    @task(1)
    # @task(0)
    def test_grpc(self):
        self.client.get("/api/test/grpc", params={"memberId": "1"}, name="gRPC Call")


    @task(0)
    # @task(1)
    def test_feign(self):
        self.client.get("/api/test/feign", params={"memberId": "1"}, name="Feign Call")
