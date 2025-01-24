# gRPC 성능 테스트 클라이언트

이 프로젝트는 **gRPC**와 **HTTP(Feign)** 프로토콜의 성능을 비교 테스트하기 위한 **클라이언트 애플리케이션**입니다.  
두 프로토콜의 실제 성능 차이를 측정하고 분석할 수 있도록 설계되었습니다.

## 프로젝트 설정

- **Java 21**
- **Spring Boot 3.3.7**
- **Gradle** 기반 빌드

### 사전 요구사항

1. **Java 21** 이상이 설치되어 있어야 합니다.
2. **테스트 대상**이 되는 **서버 애플리케이션**이 실행 중이어야 합니다.

- gRPC 서버 예시 프로젝트: 깃허브
  저장소 [https://github.com/wlsdks/grpc-server-example](https://github.com/wlsdks/grpc-server-example)
    - **HTTP 엔드포인트**: <http://localhost:8090>
    - **gRPC 엔드포인트**: <http://localhost:50051>

위와 같은 서버가 로컬 환경에서 구동 중이거나, 네트워크로 연결 가능한 상태여야 합니다.

## 아키텍처 개요

```bash
[1] User (hey 등 테스트툴)
    |
    | HTTP GET (예: /api/test/feign?memberId=1 또는 /api/test/grpc?memberId=1)
    v
[2] ClientApp (gRPC Client, 포트 8091)                                            
    |  -- (내부 로직) -----------------------------|
    
    --> (Case A) Feign --------------------------+
    |                                            |
    |    [REST: HTTP/JSON, 포트 8090]             v
    |    gRPCServer (원격 서버, HTTP) --------> 응답(JSON)
    |                                            ^
    +--------------------------------------------+

    --> (Case B) gRPC ---------------------------+
    |                                            |
    |     [gRPC: Protobuf, 포트 50051]            v
    |     gRPCServer (원격 서버, gRPC) -- ----> 응답(Protobuf)
    |                                            ^
    +--------------------------------------------+

    |
    | 최종 결과(HTTP Response)
    v
    
[3] User (테스트툴) - 결과 수신
```

- 사용자/테스터가 이 프로젝트(클라이언트)의 HTTP 엔드포인트(8091) 로 요청을 보냄
- 클라이언트 애플리케이션 내부에서 Feign(REST) 또는 gRPC 호출을 통해 원격 서버에 접근
- 원격 gRPC 서버(또는 그 서버 안의 HTTP Controller)가 DB 조회 등 비즈니스 로직을 처리한 뒤 응답
- 클라이언트 애플리케이션(8091) 이 응답을 받아 최종 HTTP 응답으로 반환

## 프로젝트 빌드

```bash
./gradlew clean build
```

빌드가 완료되면 `build/libs` 디렉터리에 다음과 같은 JAR 파일이 생성됩니다.

1. **grpc-client-0.0.1.jar**
    - 실행 가능한 JAR 파일 (Spring Boot 애플리케이션)
    - 내부적으로 애플리케이션 실행에 필요한 모든 의존성을 포함
    - 실제 구동 시 사용

2. **grpc-client-0.0.1-plain.jar**
    - 일반 JAR 파일
    - 실행 불가, 다른 프로젝트에서 라이브러리로 사용할 수 있음

## 애플리케이션 실행

```bash
java -jar build/libs/grpc-client-0.0.1.jar
```

- 기본적으로 **localhost:8091** 포트에서 애플리케이션이 실행됩니다.
- 실행 후, `http://localhost:8091` 로 접속이 가능해야 합니다.

## JWT 인증 처리 (예시)

만약 **JWT 토큰**을 사용하는 환경이라면, 보통 다음 흐름을 따릅니다:

1. **로그인**(예: `/api/auth/login`) 엔드포인트로 ID/PW 전송
2. **서버**에서 **JWT 토큰** 발급하여 응답
3. **클라이언트** 측에서 **Authorization 헤더**에 `Bearer <JWT 토큰>` 을 담아 재요청
4. 토큰은 위에 적어둔 gRPC 서버 github 저장소의 프로젝트를 실행해서 "회원 가입 -> 로그인" API를 호출하여 발급받을 수 있습니다.

`Authorization: Bearer <발급받은 토큰>` 형태로 설정해야 클라이언트가 올바르게 인증 정보를 전송할 수 있습니다.

아래 실제 부하 테스트에서 **헤더**를 추가하는 예를 확인하실 수 있습니다.

---

## 주요 엔드포인트

이 애플리케이션에는 **HTTP 요청**을 받았을 때, 내부적으로 **Feign**(HTTP) 혹은 **gRPC** 호출을 수행하고 결과를 반환하는 컨트롤러가 있습니다.

- **Feign 테스트**
    - 내부적으로 **Feign Client**를 이용해 **원격 서버(8090)** 의 `GET /api/members/{id}` 호출

```
GET /api/test/feign?memberId={id}
```

- **gRPC 테스트**
    - 내부적으로 **gRPC Client**를 이용해 **원격 gRPC 서버(50051)** 의 `GetMemberById` 호출

```
GET /api/test/grpc?memberId={id}
```

- 호출예시

```
GET http://localhost:8091/api/test/feign?memberId=1
GET http://localhost:8091/api/test/grpc?memberId=1
```

요청에는 **JWT**가 필요하니 위 엔드포인트 호출 시에 **Authorization** 헤더를 포함해야 합니다.

- 예: `curl` 을 사용하는 경우

```bash
curl -v \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6Ikp..." \
  "http://localhost:8091/api/test/feign?memberId=1"

curl -v \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6Ikp..." \
  "http://localhost:8091/api/test/grpc?memberId=1"
```

---

## 성능 테스트 시나리오

이제 실제로 **부하 테스트**를 진행해보는 방법을 안내합니다.  
예시로, [hey](<https://github.com/rakyll/hey>) 툴을 사용합니다.  
(물론 JMeter, wrk, ab 등 다른 툴을 사용해도 됩니다.)

### 1. Feign (HTTP) 호출 성능 측정

```bash
hey -n 1000 -c 50 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqaW5hbkB0ZXN0LmNvbSIsImlhdCI6MTczNzcyNjY1MiwiZXhwIjoxNzM3NzMwMjUyfQ.XytkkongdksyVUk6kHXGVMzZCwyCdOETv8pLbR23l3A" \
  "http://localhost:8091/api/test/feign?memberId=1"
```

- **-n 1000**: 총 1000번 요청
- **-c 50**: 동시에 50개의 연결(Concurrent)
- **-H**: **Authorization** 헤더 추가하여 JWT 토큰 포함
- 내부적으로는 **클라이언트 애플리케이션(8091)** 이 **Feign**을 통해 **원격 서버(8090)** 로 REST 요청을 보냄
- 최종 결과를 hey가 콘솔에 출력해주며, **TPS**, **응답 시간(최대/최소/평균)**, **에러율** 등을 확인 가능

예시 결과 (콘솔 출력):

```
Summary:
  Total:	0.3676 secs
  Slowest:	0.0723 secs
  Fastest:	0.0013 secs
  Average:	0.0172 secs
  Requests/sec:	2720.5409
  

Response time histogram:
  0.001 [1]	|
  0.008 [268]	|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
  0.015 [247]	|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
  0.023 [194]	|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
  0.030 [152]	|■■■■■■■■■■■■■■■■■■■■■■■
  0.037 [77]	|■■■■■■■■■■■
  0.044 [29]	|■■■■
  0.051 [22]	|■■■
  0.058 [4]	|■
  0.065 [4]	|■
  0.072 [2]	|

...

Status code distribution:
  [200]	1000 responses
```

### 2. gRPC 호출 성능 측정

```bash
hey -n 1000 -c 50 \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqaW5hbkB0ZXN0LmNvbSIsImlhdCI6MTczNzcyNjY1MiwiZXhwIjoxNzM3NzMwMjUyfQ.XytkkongdksyVUk6kHXGVMzZCwyCdOETv8pLbR23l3A" \
  "http://localhost:8091/api/test/grpc?memberId=1"
```

- 마찬가지로 1000번 요청, 동시 50개
- **-H**로 **JWT 토큰**을 포함
- 이번에는 **클라이언트 애플리케이션(8091)** 이 **gRPC Client**를 통해 **원격 gRPC 서버(50051)** 에 직접 gRPC를 호출
- 결과적으로 **HTTP → gRPC** 구조를 거쳐 최종 응답을 받음

결과 예시:

```
Summary:
  Total:	0.2320 secs
  Slowest:	0.0502 secs
  Fastest:	0.0009 secs
  Average:	0.0114 secs
  Requests/sec:	4310.6320
  

Response time histogram:
  0.001 [1]	|
  0.006 [300]	|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
  0.011 [350]	|■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■
  0.016 [113]	|■■■■■■■■■■■■■
  0.021 [94]	|■■■■■■■■■■■
  0.026 [71]	|■■■■■■■■
  0.030 [27]	|■■■
  0.035 [2]	|
  0.040 [0]	|
  0.045 [12]	|■
  0.050 [30]	|■■■

...

Status code distribution:
  [200]	1000 responses
```

> 실제 값은 서버 성능, 네트워크 상태, DB 부하 등에 따라 달라집니다.

---

## 추가 참고 사항

1. **헤더 설정**
    - JWT 말고도, `Content-Type`, `Accept` 등 필요에 따라 추가 설정이 있을 수 있습니다.
    - `hey`에서 헤더 추가 시에는 `-H "키: 값"` 형식으로 여러 번 지정 가능합니다.

2. **동시성 & 확장성**
    - `hey` 툴로도 몇 천~몇 만 건 정도 테스트 가능하지만,  
      더 큰 규모나 복잡한 시뮬레이션이
      필요하면 [JMeter](https://jmeter.apache.org/), [Gatling](https://gatling.io/), [Locust](https://locust.io/) 등을 고려할 수
      있습니다.

3. **직접 gRPC 부하 테스트 (ghz)**
    - 순수 gRPC 서버(50051)에 대해 **직접** 부하를 주고 싶다면, [ghz](https://ghz.sh/) 사용을 고려하세요.
    - 예:
      ```bash
      ghz --insecure \
          --proto ./path/to/member.proto \
          --call com.test.member.grpc.MemberService.GetMemberById \
          -d '{"id":1}' \
          -n 1000 -c 50 \
          127.0.0.1:50051
      ```
    - 이 경우, **HTTP 레이어**(8091)는 거치지 않으므로 “**HTTP → gRPC**”가 아닌 **순수 gRPC** 성능만 확인 가능합니다.

4. **프로덕션 고려**
    - 실제 운영 환경에서는 스레드 풀, Netty 설정, TLS(SSL), DB connection pool, 인증/인가 등의 변수가 많아집니다.
    - 따라서 실무 부하 테스트는 이러한 요소들도 함께 고려하시길 권장합니다.

---

## FAQ

1. **Q**: “테스트 중 `401 Unauthorized` 에러가 발생합니다. 어떻게 해결하나요?”  
   **A**: 발급받은 **JWT 토큰**이 만료되었거나, 잘못된 토큰을 사용했을 가능성이 있습니다.
    - 먼저 서버 측 `/api/auth/login`(또는 `/api/auth/refresh`) 등을 통해 새로운 토큰을 발급받으세요.
    - 요청 시 `-H "Authorization: Bearer <유효한 토큰>"` 형태로 보내는지 확인하세요.

<br/>

2. **Q**: “Feign vs. gRPC 중 어느 쪽이 더 빠른가요?”  
   **A**: 일반적으로 **gRPC(HTTP/2 + Protobuf 직렬화)**가 **REST(HTTP/1.1 + JSON)** 대비 오버헤드가 적은 편입니다.  
   그러나 실제로는 네트워크 환경, 서버 스펙, DB 부하, TLS 설정 등 다양한 요소가 영향을 미치므로,  
   **직접 부하 테스트**를 통해 **각 환경별, 시나리오별 실측 데이터를 확보**하시길 권장합니다.

<br/>

3. **Q**: “gRPC 서버 주소(포트)를 바꾸려면 어떻게 해야 하나요?”  
   **A**: 보통 `application.yml`의 `grpc.client.member-service.address` 부분(혹은 `bootstrap.yml`)에서  
   `localhost:50051` 등을 수정하면 됩니다.
    - 소스 코드상 `@GrpcClient("member-service")` 에 매핑된 설정을 확인하세요.

<br/>

4. **Q**: “회원 데이터를 사전에 생성해두려면 어떻게 하나요?”  
   **A**: 원격 서버(8090)에 `POST /api/members` 호출로 테스트용 회원(예: ID=1)만 미리 만들어 두십시오.
    - 이후 `GET /api/test/feign?memberId=1` or `GET /api/test/grpc?memberId=1` 로 조회 요청 시,  
      해당 회원 데이터가 정상 조회되어야 합니다.
