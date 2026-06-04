# MSE Server REST API 사용 설명서

Unity 등 HTTP 클라이언트에서 호출할 때의 규약과 엔드포인트를 정리합니다. 기본 응답 형식은 **JSON**이며, 문자 인코딩은 **UTF-8**입니다.

## 기본 URL

로컬 기본값은 `http://localhost:9090` 입니다. 배포 환경에서는 해당 서버의 베이스 URL로 바꿉니다. (운영 호스팅이 `PORT`를 주면 그 포트를 사용합니다.)

---

## 공통 규칙

### Content-Type

요청 본문이 있는 경우:

```http
Content-Type: application/json
```

### 인증 (JWT Bearer)

회원가입·로그인을 제외한 **대부분의 API**는 인증이 필요합니다.

1. `POST /api/auth/login`으로 `token`을 받습니다.
2. 이후 요청마다 아래 헤더를 붙입니다.

```http
Authorization: Bearer <로그인에서 받은 token>
```

토큰 만료 시간은 서버 설정 `mse.jwt.expiration-ms`에 따릅니다(기본값 예: 24시간).

### CORS

브라우저/WebGL 등에서 호출할 때는 서버의 `mse.cors.allowed-origin-patterns` 설정에 맞는 오리진만 허용됩니다. 로컬 개발은 기본적으로 `http://localhost:*`, `http://127.0.0.1:*` 패턴이 포함됩니다.

### CSRF

REST API는 **CSRF 비활성화** 상태입니다. 세션 쿠키가 아닌 JWT만 사용합니다.

---

## 에러 응답

애플리케이션에서 처리하는 오류는 아래와 같은 JSON 본문을 반환하는 경우가 많습니다.

| 필드      | 설명                    |
| --------- | ----------------------- |
| `code`    | 클라이언트가 분기할 코드 |
| `message` | 사람이 읽을 수 있는 설명 |

예시:

```json
{
  "code": "ROOM_FULL",
  "message": "Room is full: <roomId>"
}
```

### `code` 값 (주요)

| code               | HTTP 상태 | 설명 |
| ------------------ | --------- | ---- |
| `DUPLICATE_USER`   | 409       | 이미 존재하는 `userId`로 회원가입 시도 |
| `NOT_FOUND`        | 404       | 유저 또는 방을 찾을 수 없음 |
| `ROOM_FULL`        | 409       | 방 정원이 찼음 |
| `ROOM_NOT_OPEN`    | 400       | 방이 `OPEN`이 아니어서 신규 참가 불가 |
| `NOT_ROOM_PARTICIPANT` | 400   | 해당 방 참가자가 아닌데 나가기·시작 시도 |
| `NOT_ROOM_HOST`        | 403   | 방장만 가능한 동작을 비방장이 시도 |
| `BAD_CREDENTIALS`  | 401       | 로그인 ID/비밀번호 불일치 |
| `VALIDATION_ERROR` | 400       | 요청 검증 실패(필드 메시지 포함) |

검증 실패 시 `message`에는 첫 번째 필드 오류가 들어갈 수 있습니다. (예: `maxPlayers: must be less than or equal to 4`)

인증이 필요한 URL에 토큰 없이 접근하거나 토큰이 잘못된 경우, **Spring Security 기본 동작**에 따라 `401`/`403` 등이 반환될 수 있으며, 위와 같은 `code`/`message` 형식이 아닐 수 있습니다.

---

## 엔드포인트 목록

| 메서드 | 경로 | 인증 |
| ------ | ---- | ---- |
| POST   | `/api/auth/register` | 불필요 |
| POST   | `/api/auth/login`    | 불필요 |
| GET    | `/api/users/me`      | 필요 |
| PATCH  | `/api/users/me`      | 필요 |
| POST   | `/api/rooms`         | 필요 |
| GET    | `/api/rooms`         | 필요 |
| POST   | `/api/rooms/{roomId}/join` | 필요 |
| POST   | `/api/rooms/{roomId}/start` | 필요 |
| POST   | `/api/rooms/{roomId}/leave` | 필요 |

헬스체크(모니터링): `GET /actuator/health` (인증 불필요)

개발 프로파일에서만 H2 콘솔: `GET /h2-console` (인증 불필요, 브라우저용)

---

## 인증 API

### 회원가입

`POST /api/auth/register`

**본문**

| 필드       | 타입   | 제약 |
| ---------- | ------ | ---- |
| `userId`   | string | 3~64자, 공백 불가 |
| `password` | string | 8~128자, 공백 불가 |

**성공:** `201 Created`, 본문 없음.

**실패:** `409` + `DUPLICATE_USER` (동일 `userId` 존재).

**예시**

```json
{
  "userId": "player01",
  "password": "mypassword"
}
```

---

### 로그인

`POST /api/auth/login`

**본문**

| 필드       | 타입   | 제약 |
| ---------- | ------ | ---- |
| `userId`   | string | 최대 64자, 공백 불가 |
| `password` | string | 최대 128자, 공백 불가 |

**성공:** `200 OK`

```json
{
  "token": "<JWT 문자열>"
}
```

**실패:** `401` + `BAD_CREDENTIALS`.

---

## 유저 API

### 내 프로필 조회

`GET /api/users/me`

**성공:** `200 OK`

```json
{
  "userId": "player01",
  "gameProgress": {
    "1": 500,
    "2": 1200
  },
  "currency": 0,
  "ownedItems": [10, 20, 30]
}
```

- `gameProgress`: 키는 **스테이지 번호**, 값은 해당 스테이지 **최고 점수**.
- `ownedItems`: 보유 아이템 **ID** 목록(순서 유지).
- JSON에서 맵 키는 숫자여도 문자열로 직렬화될 수 있으므로, 클라이언트 파서에 맞게 처리합니다.

---

### 내 프로필 부분 수정

`PATCH /api/users/me`

**본문** — 모두 선택 사항입니다. **보낸 필드만** 갱신합니다.

| 필드           | 타입                | 제약 |
| -------------- | ------------------- | ---- |
| `currency`     | number (integer)    | 생략 가능, 있으면 0 이상 |
| `gameProgress` | object (map)        | 생략 가능,내면 **전체 맵으로 덮어씀** |
| `ownedItems`   | array of number     | 생략 가능,내면 **전체 목록으로 덮어씀** |

**성공:** `200 OK` — 응답 형식은 `GET /api/users/me`와 동일한 `UserResponse`.

**예시** (재화만 변경)

```json
{
  "currency": 100
}
```

**예시** (진행도·아이템 일괄 반영)

```json
{
  "gameProgress": { "1": 999, "3": 100 },
  "ownedItems": [1, 2, 5]
}
```

---

## 방 API

방은 **메타데이터만** 서버에 저장합니다. 실제 게임 세션·매치 로직은 클라이언트/별도 서비스에서 다루는 것을 전제로 합니다.

### 방 생성

`POST /api/rooms`

호출한 유저가 **방장**이 되며, 참가자 목록에 자동으로 포함됩니다.

**본문**

| 필드         | 타입   | 제약 |
| ------------ | ------ | ---- |
| `title`      | string | 1~128자, 공백 불가 |
| `stage`      | number | **1 이상** (플레이할 스테이지 번호) |
| `maxPlayers` | number | **2 이상 4 이하** (최대 4명) |

**성공:** `200 OK` — `RoomResponse` (아래 형식).

**예시**

```json
{
  "title": "아주대 로비",
  "stage": 3,
  "maxPlayers": 4
}
```

---

### 열린 방 목록

`GET /api/rooms`

`status`가 `OPEN`인 방만, 생성일 **내림차순**으로 반환합니다.

**성공:** `200 OK` — `RoomResponse` 배열.

---

### 방 참가

`POST /api/rooms/{roomId}/join`

- 이미 해당 방에 들어가 있는 경우: 성공으로 간주되며, 갱신된 `RoomResponse`를 돌려줍니다.
- 정원이 찼는데 아직 참가자가 아닌 경우: `409` + `ROOM_FULL`.
- 방이 `OPEN`이 아닌데 **새로** 들어가려는 경우: `400` + `ROOM_NOT_OPEN`.
- `roomId`가 없으면: `404` + `NOT_FOUND`.

**성공:** `200 OK` — `RoomResponse`.

---

### 게임 시작 (상태 → IN_PROGRESS)

`POST /api/rooms/{roomId}/start`

**방장**만 호출할 수 있습니다. `OPEN`인 방을 `IN_PROGRESS`로 바꿉니다. 이미 `IN_PROGRESS`이면 같은 상태로 `200 OK`를 반환합니다(재호출 허용).

**성공:** `200 OK` — `status`가 `IN_PROGRESS`인 `RoomResponse`.

**실패**

- 방장이 아님: `403` + `NOT_ROOM_HOST`
- 참가자가 아님: `400` + `NOT_ROOM_PARTICIPANT`
- `CLOSED` 등 `OPEN`/`IN_PROGRESS`가 아닌 상태: `400` + `ROOM_NOT_OPEN`
- `roomId` 없음: `404` + `NOT_FOUND`

시작 후에는 `GET /api/rooms` 목록에 **나타나지 않으며**, 신규 `join`은 `ROOM_NOT_OPEN`으로 거절됩니다.

---

### 방 나가기

`POST /api/rooms/{roomId}/leave`

인증된 유저 본인을 기준으로 처리합니다.

| 나가는 유저 | 동작 |
| ----------- | ---- |
| **방장** (`hostUserId`와 동일) | 방을 DB에서 **삭제** (참가자·방 메타 포함). 다른 유저는 해당 `roomId`로 더 이상 접근 불가. |
| **그 외 참가자** | `participantUserIds`에서 본인만 제거. 방은 유지되며 `OPEN`이면 이후 **재입장 가능**. |

**성공**

- 방장이 나간 경우: `204 No Content` (본문 없음)
- 일반 참가자가 나간 경우: `200 OK` — 갱신된 `RoomResponse`

**실패**

- `roomId` 없음: `404` + `NOT_FOUND`
- 참가자가 아님: `400` + `NOT_ROOM_PARTICIPANT`

---

### RoomResponse (방 단건·목록 공통)

| 필드                   | 타입           | 설명 |
| ---------------------- | -------------- | ---- |
| `roomId`               | string (UUID)  | 방 ID |
| `hostUserId`           | string         | 방장 유저 ID |
| `title`                | string         | 방 제목 |
| `stage`                | number         | 플레이 스테이지 번호 |
| `maxPlayers`           | number         | 최대 인원(2~4) |
| `currentPlayerCount`   | number         | 현재 참가자 수 |
| `status`               | string         | `OPEN`, `IN_PROGRESS`, `CLOSED` 중 하나 |
| `createdAt`            | string (ISO-8601 instant) | 생성 시각 |
| `participantUserIds`   | array of string | 참가자 유저 ID 목록(순서 포함) |

예시:

```json
{
  "roomId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "hostUserId": "player01",
  "title": "4인 팟",
  "stage": 3,
  "maxPlayers": 4,
  "currentPlayerCount": 2,
  "status": "OPEN",
  "createdAt": "2026-05-15T10:00:00Z",
  "participantUserIds": ["player01", "player02"]
}
```

> 참고: `CLOSED`로 바꾸는 전용 엔드포인트는 아직 없습니다.

---

## Unity (`UnityWebRequest`) 호출 예시 개념

1. `POST`로 `/api/auth/login`에 JSON 본문 전송.
2. 응답 JSON에서 `token` 파싱 후 저장.
3. 이후 요청에 `SetRequestHeader("Authorization", "Bearer " + token)` 설정.
4. `downloadHandler.text`로 응답 JSON 파싱.

에디터/빌드에서 서버 주소만 환경별로 바꾸면 됩니다.

---

## 로컬 실행 후 빠른 수동 테스트

1. 서버 기동: `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`)
2. 회원가입 → 로그인으로 토큰 확보
3. Postman/Insomnia 등에서 `Authorization: Bearer ...`로 `/api/rooms` 등 호출

---

문서 버전: 코드베이스 기준 스냅샷입니다. 엔드포인트가 바뀌면 이 파일도 함께 갱신하는 것을 권장합니다.
