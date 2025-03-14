# gRPC vs REST 성능 비교 클라이언트

이 프로젝트는 **gRPC**와 **HTTP/REST(Feign)** 프로토콜의 성능을 실증적으로 비교하기 위한 클라이언트 애플리케이션입니다. 마이크로서비스 환경에서 서비스 간 통신 방식의 효율성과 성능 차이를 측정하고 분석할 수 있도록 설계되었습니다.

## 🌟 프로젝트 개요

본 클라이언트는 동일한 기능을 REST API와 gRPC 두 가지 방식으로 호출하고, 그 성능 차이를 측정하는 벤치마킹 도구로 활용할 수 있습니다. 실제 운영 환경과 유사한, 서킷 브레이커 패턴 등의 장애 허용 기능이 구현되어 있습니다.

## 📚 기술 스택

- **Java 21**
- **Spring Boot 3.3.7**
- **Spring Cloud OpenFeign**
- **gRPC 1.65.1**
- **Protocol Buffers 4.28.2**
- **Resilience4j** (서킷 브레이커)
- **JWT 인증**
- **Locust** (부하 테스트)

## 🏗️ 시스템 아키텍처

```
┌────────────────────────────────────────────────────────────────────┐
│                        Client Application                          │
│                                                                    │
│  ┌─────────────────┐          ┌────────────────────────────────┐   │
│  │                 │          │                                │   │
│  │  REST API       │◄────┐    │  Test Controllers              │   │
│  │  (Port: 8091)   │     │    │  (/api/test/*)                 │   │
│  │                 │     │    │                                │   │
│  └─────────────────┘     │    └────────────────────────────────┘   │
│                          │                     │                   │
│                          │                     │                   │
│                          │                     ▼                   │
│  ┌─────────────────┐     │    ┌────────────────────────────────┐   │
│  │                 │     │    │                                │   │
│  │  Test Tools     │─────┘    │  Service Layer                 │   │
│  │  (Hey/Locust)   │          │  (Circuit Breaker/Fallback)    │   │
│  │                 │          │                                │   │
│  └─────────────────┘          └────────────────────────────────┘   │
│                                                │                   │
│                                                │                   │
│                    ┌───────────────────────────┴─────┐             │
│                    │                                 │             │
│                    ▼                                 ▼             │
│  ┌─────────────────────────────────┐    ┌─────────────────────────┐│
│  │                                 │    │                         ││
│  │  Feign Client                   │    │  gRPC Client            ││
│  │  (OpenFeign + Resilience4j)     │    │  (Protocol Buffers)     ││
│  │                                 │    │                         ││
│  └─────────────────────────────────┘    └─────────────────────────┘│
│                    │                                 │             │
│                    │                                 │             │
│                    ▼                                 ▼             │
│  ┌─────────────────────────────────┐    ┌─────────────────────────┐│
│  │                                 │    │                         ││
│  │  HTTP/REST API                  │    │  gRPC Service           ││
│  │  (Remote Server - Port: 8090)   │    │  (Remote - Port: 50051) ││
│  │                                 │    │                         ││
│  └─────────────────────────────────┘    └─────────────────────────┘│
│                                                                    │
└────────────────────────────────────────────────────────────────────┘
```

### 통신 흐름도

```mermaid
sequenceDiagram
    participant TestTool as 테스트 도구 (Hey/Locust)
    participant ClientAPI as 클라이언트 API (8091)
    participant Service as 서비스 레이어
    participant Feign as Feign 클라이언트
    participant GRPC as gRPC 클라이언트
    participant CircuitBreaker as 서킷 브레이커
    participant RestServer as REST 서버 (8090)
    participant GRPCServer as gRPC 서버 (50051)
    
    alt REST 테스트 시나리오
        TestTool->>+ClientAPI: HTTP 요청 (/api/test/feign?memberId=1)
        ClientAPI->>+Service: Feign 테스트 요청
        Service->>CircuitBreaker: 서킷 브레이커 확인
        CircuitBreaker-->>Service: 상태 반환 (CLOSED/OPEN/HALF_OPEN)
        
        alt 서킷 브레이커 CLOSED
            Service->>+Feign: 회원 조회 요청
            Feign->>+RestServer: HTTP GET 요청
            RestServer-->>-Feign: HTTP 응답 (JSON)
            Feign-->>-Service: 응답 객체 반환
        else 서킷 브레이커 OPEN
            Service->>Service: Fallback 응답 생성
        end
        
        Service-->>-ClientAPI: 응답 반환
        ClientAPI-->>-TestTool: HTTP 응답
    
    else gRPC 테스트 시나리오
        TestTool->>+ClientAPI: HTTP 요청 (/api/test/grpc?memberId=1)
        ClientAPI->>+Service: gRPC 테스트 요청
        Service->>CircuitBreaker: 서킷 브레이커 확인
        CircuitBreaker-->>Service: 상태 반환
        
        alt 서킷 브레이커 CLOSED
            Service->>+GRPC: 회원 조회 요청
            GRPC->>+GRPCServer: gRPC 요청 (GetMemberById)
            GRPCServer-->>-GRPC: gRPC 응답 (Protobuf)
            GRPC-->>-Service: 응답 객체 반환
        else 서킷 브레이커 OPEN
            Service->>Service: Fallback 응답 생성
        end
        
        Service-->>-ClientAPI: 응답 반환
        ClientAPI-->>-TestTool: HTTP 응답
    end
```

## 🛣️ 통신 경로 비교 (REST vs gRPC)

```mermaid
graph TB
    subgraph "REST 경로"
        A[외부 클라이언트] --> B[HTTP API] --> C[Feign Client] --> D[REST Server] --> E[DB 조회] --> F[HTTP 응답]
    end
```

```mermaid
graph TB
    subgraph "gRPC 경로"
        G[외부 클라이언트] --> H[HTTP API] --> I[gRPC Client] --> J[gRPC Server] --> K[DB 조회] --> L[gRPC 응답]
    end
```

## 🚀 주요 기능

### 1. 성능 비교 엔드포인트

| 메서드 | 경로 | 설명 | 내부 호출 방식 |
|--------|------|------|-----------|
| GET | `/api/test/feign?memberId={id}` | Feign 기반 회원 조회 | REST API |
| GET | `/api/test/grpc?memberId={id}` | gRPC 기반 회원 조회 | gRPC |

### 2. 시스템 신뢰성 기능

#### 서킷 브레이커 패턴 (Circuit Breaker)

- **목적**: 장애 전파 방지 및 시스템 복원력 향상
- **구현**: Resilience4j 라이브러리 사용
- **상태 변이**:
   - `CLOSED` → `OPEN`: 실패율 50% 이상 시 (최근 10회 기준)
   - `OPEN` → `HALF_OPEN`: 5초 후 자동 전환
   - `HALF_OPEN` → `CLOSED`: 테스트 호출 성공률 기준

#### Fallback 메커니즘

- 서비스 장애 시 기본 응답 제공
- 사용자 경험 유지 및 시스템 안정성 확보

#### 모니터링 엔드포인트

- `/api/circuit-breaker`: 서킷 브레이커 상태 및 통계 정보 제공

### 3. 인증 및 보안

#### JWT 토큰 인증

- 사용자 인증을 위한 JWT 검증
- 토큰 전파 메커니즘 (HTTP → gRPC)

#### 서버 간 인증

- 마이크로서비스 간 안전한 통신을 위한 서버 토큰
- Feign 및 gRPC 인터셉터를 통한 자동 토큰 주입

## 📊 성능 테스트 도구

### 내장 테스트 도구

#### 1. Hey

```bash
# Feign(REST) 호출 테스트
hey -n 1000 -c 50 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  "http://localhost:8091/api/test/feign?memberId=1"

# gRPC 호출 테스트
hey -n 1000 -c 50 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..." \
  "http://localhost:8091/api/test/grpc?memberId=1"
```

#### 2. Locust

```python
from locust import HttpUser, task, between

class GrpcVsFeignTest(HttpUser):
    wait_time = between(0.1, 0.5)
    token = "Bearer eyJhbGciOiJIUzI1NiJ9..."

    def on_start(self):
        self.client.headers = {"Authorization": self.token}

    @task(1)
    def test_grpc(self):
        self.client.get("/api/test/grpc", params={"memberId": "1"})

    @task(1)
    def test_feign(self):
        self.client.get("/api/test/feign", params={"memberId": "1"})
```

실행 방법:
```bash
docker-compose up -d --scale worker=3
# 웹 인터페이스: http://localhost:8089
```

## 📈 성능 비교 분석

### 성능 비교 (블로그에 자세한 내용 포스팅)
https://curiousjinan.tistory.com/entry/msa-spring-grpc-feign-test

### 네트워크 효율성

- **REST/JSON**: 더 큰 페이로드 크기, 더 많은 헤더 오버헤드
- **gRPC/Protobuf**: 압축된 바이너리 데이터, 적은 헤더 오버헤드

### 리소스 사용

- **메모리 사용량**: 유사 (Protocol Buffers 파싱 비용 vs. JSON 파싱 비용)

## 🔄 서킷 브레이커 상태 흐름

```mermaid
stateDiagram-v2
    [*] --> CLOSED
    CLOSED --> OPEN: 실패율 50% 초과
    OPEN --> HALF_OPEN: 5초 경과
    HALF_OPEN --> CLOSED: 성공률 70% 이상
    HALF_OPEN --> OPEN: 성공률 70% 미만
```

## 🛠️ 설치 및 실행

### 사전 요구사항

- Java 21 이상
- 원격 gRPC 서버 (포트: 50051)
- 원격 REST 서버 (포트: 8090)

### 애플리케이션 빌드 및 실행

```bash
# 프로젝트 빌드
./gradlew clean build

# 애플리케이션 실행
java -jar build/libs/grpc-client-0.0.1.jar
```

## 👨‍💻 개발자 가이드

### Feign 클라이언트 추가하기

```java
@FeignClient(name = "member-service", url = "http://localhost:8090")
public interface MemberFeignClient {
    @GetMapping("/api/members/{memberId}")
    ResponseEntity<ResponseMemberDTO> getMemberById(@PathVariable("memberId") Long memberId);
}
```

### gRPC 클라이언트 추가하기

```java
@Component
public class GrpcMemberClient {
    @GrpcClient("member-service")
    private Channel channel;
    
    private MemberServiceGrpc.MemberServiceBlockingStub stub;
    
    @PostConstruct
    public void init() {
        stub = MemberServiceGrpc.newBlockingStub(channel);
    }
    
    public MemberProto.MemberResponse getMemberById(Long memberId) {
        MemberProto.MemberIdRequest request = MemberProto.MemberIdRequest.newBuilder()
                .setId(memberId)
                .build();
        return stub.getMemberById(request);
    }
}
```

### 서킷 브레이커 구성 (2가지중 하나를 선택해서 사용)

application.yml:
```yaml
resilience4j:
  circuitbreaker:
    instances:
      member-service:
        slidingWindowType: COUNT_BASED
        slidingWindowSize: 10
        failureRateThreshold: 50
        waitDurationInOpenState: 5s
        permittedNumberOfCallsInHalfOpenState: 5
        automaticTransitionFromOpenToHalfOpenEnabled: true
```

Resilience4jConfig:
```java
@Configuration
public class Resilience4jConfig {

    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                // 호출 횟수 기반 슬라이딩 윈도우 (최근 10회 호출 기준)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                // 실패율 50% 이상이면 OPEN 상태로 전환
                .failureRateThreshold(50)
                // OPEN 상태에서 5초 동안 호출 차단 후 HALF_OPEN 상태로 전환
                .waitDurationInOpenState(java.time.Duration.ofSeconds(5))
                // HALF_OPEN 상태에서 최대 5개 호출 허용
                .permittedNumberOfCallsInHalfOpenState(5)
                // 최근 10회 호출을 기준으로 통계 집계
                .slidingWindowSize(10)
                // OPEN 상태에서 HALF_OPEN으로 자동 전환 활성화
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                // FeignException, ConnectException, RuntimeException을 실패 예외로 기록
                .recordExceptions(FeignException.class, ConnectException.class, RuntimeException.class)
                .build();
    }

    // 위 설정을 기반으로 CircuitBreakerRegistry를 생성
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig config) {
        return CircuitBreakerRegistry.of(config);
    }

}
```

## 🧪 테스트 시나리오 가이드

### 1. 기본 성능 테스트

낮은 부하에서 REST와 gRPC의 기본 성능 비교:
```bash
hey -n 100 -c 10 "http://localhost:8091/api/test/feign?memberId=1"
hey -n 100 -c 10 "http://localhost:8091/api/test/grpc?memberId=1"
```

### 2. 고부하 테스트

높은 동시 연결에서의 성능 한계 테스트:
```bash
hey -n 10000 -c 200 "http://localhost:8091/api/test/feign?memberId=1"
hey -n 10000 -c 200 "http://localhost:8091/api/test/grpc?memberId=1"
```

### 3. 장애 복구 테스트

https://curiousjinan.tistory.com/entry/spring-msa-circuit-breaker (블로그에 자세히 정리)

서버 장애 시 서킷 브레이커 동작 테스트:
1. 클라이언트 애플리케이션 실행
2. 원격 서버 중지
3. API 호출 실행 (서킷 브레이커 OPEN)
4. 상태 확인: `GET /api/circuit-breaker`
5. 원격 서버 재시작
6. 서킷 브레이커 복구 확인

## 🔗 연계 프로젝트

이 클라이언트와 함께 사용할 수 있는 서버 프로젝트:
- [gRPC 서버 예제](https://github.com/wlsdks/grpc-server-example) - REST와 gRPC를 모두 제공하는 서버