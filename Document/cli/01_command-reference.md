# 01 Command Reference

## 목적

이 문서는 공용 CLI `pb`의 명령어 계약을 정리한다.
현재 실제 구현된 명령을 기준으로 기록한다.

## 공통 규칙

- 엔트리포인트는 `pb`다.
- `target`은 `Tools/cli/src/pbcli/config/projects.yaml`에 등록된 `name` 값만 받는다.
- `engine`은 `postgresql`, `mysql`만 사용한다.
- `domain`은 `post`, `shop`만 사용한다.
- `pb up`은 새 PowerShell 창을 열어 대상 서버를 실행한다.
- `pb up`은 `--port`가 없으면 backend는 `8000`, frontend는 `3000`부터 자동 증가 포트를 할당한다.
- backend Swagger 기본 경로는 `/docs`다.
- `pb down`은 CLI 런타임 상태에 저장된 PID를 종료한다.
- `pb db up`, `pb db down`, `pb db reset`은 Docker 기반 DB 명령으로 동작한다.

## 명령어 표

| 명령 | 인자 | 설명 | 현재 상태 | 대상 그룹 | 예시 |
| --- | --- | --- | --- | --- | --- |
| `pb list` | 없음 | 등록된 백엔드, 프론트, DB 타깃 목록을 출력한다. | `implemented` | 공용 | `pb list` |
| `pb up <target> [--port N]` | `target`, `port` | 지정한 타깃을 새 PowerShell 창에서 기동한다. 포트를 주지 않으면 그룹 기준 자동 할당한다. | `implemented` | backend, frontend | `pb up post-java-springboot-maven-postgresql --port 9010` |
| `pb down <target>` | `target` | CLI가 추적 중인 대상 프로세스를 종료한다. | `implemented` | backend, frontend | `pb down web-shop` |
| `pb test <target>` | `target` | 지정한 타깃의 테스트 명령을 현재 콘솔에서 실행한다. | `implemented` | backend, frontend | `pb test shop-typescript-nestjs-npm-postgresql` |
| `pb db up` | 없음 | 공용 DB 인프라를 `docker compose up -d`로 기동한다. | `implemented` | database | `pb db up` |
| `pb db down` | 없음 | 공용 DB 인프라를 `docker compose down`으로 종료한다. | `implemented` | database | `pb db down` |
| `pb db reset <engine> <domain>` | `engine`, `domain` | 지정한 DB 엔진과 도메인 기준으로 DB를 drop/create 후 schema와 seed를 다시 적용한다. | `implemented` | database | `pb db reset postgresql post` |
| `pb doctor` | 없음 | 로컬 실행 환경과 필수 도구 상태를 점검한다. | `implemented` | 공용 | `pb doctor` |
| `pb gui` | 없음 | Tkinter 기반 로컬 GUI를 실행한다. | `implemented` | 공용 | `pb gui` |

## 현재 구현 상태

현재 `main.py` 기준 동작은 아래와 같다.

- `pb list`는 `projects.yaml`을 읽어 `backend`, `frontend`, `database` 그룹별 타깃을 출력한다.
- `pb up`은 등록된 `start_command`를 새 PowerShell 창에서 실행하고 PID와 할당 포트를 기록한다.
- `pb down`은 저장된 PID를 기준으로 대상 프로세스를 종료한다.
- `pb test`는 등록된 `test_command`를 해당 프로젝트 경로에서 실행한다.
- `pb db up`, `pb db down`은 `Database/docker/docker-compose.yml`을 기준으로 동작한다.
- `pb db reset`은 엔진별 SQL 파일을 컨테이너 내부 DB에 다시 적용한다.
- `pb doctor`는 Python, Node, npm, Java, Docker 존재 여부를 점검한다.
- `pb gui`는 Tkinter GUI를 띄워 같은 기능을 호출한다.

## 구성 메모

- 실제 실행 명령은 `projects.yaml`의 `start_command`, `test_command`를 따른다.
- frontend 실행 시 CLI는 `VITE_APP_PORT`, `VITE_API_BASE_URL`을 주입한다.
- backend 실행 시 CLI는 `SERVER_PORT`, `APP_PORT` 또는 `PORT`를 주입한다.
- Windows 기준 서버 실행은 새 PowerShell 창으로 고정한다.
- 런타임 상태는 `Tools/cli/.runtime/state.json`에 기록한다.

## 대표 출력 예시

### `pb list`

```text
[backend]
- post-java-springboot-maven-postgresql: Backend/post/post-java-springboot-maven-postgresql
- ...
[frontend]
- web-post: Frontend/web-post
- web-shop: Frontend/web-shop
[database]
- db-postgresql: Database/postgresql
- db-mysql: Database/mysql
- db-redis: Database/docker
- db-elasticsearch: Database/docker
```

### 대표 명령

```text
Started post-java-springboot-maven-postgresql in new PowerShell window (PID ..., port 8000)
Stopped web-shop (PID ..., port 3001)
```

## 오류 및 주의사항

- `target`이 `projects.yaml`에 없으면 명령은 실패한다.
- 자동 포트는 CLI가 이미 추적 중인 프로세스와 현재 점유 중인 포트를 피해서 선택한다.
- CLI 식별자와 실제 폴더명이 다르면 운영 규칙이 깨지므로 허용하지 않는다.
- `pb down`은 `pb up`으로 기록된 PID가 없으면 종료할 대상을 찾지 못한다.
