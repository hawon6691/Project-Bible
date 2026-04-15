# 02 Setup And Target Registration

## 목적

이 문서는 공용 CLI의 설치 방법, 실행 방법, 타깃 등록 방식, 새 프로젝트 연결 절차를 정리한다.

현재 단계에서 말하는 “연결”은 `레지스트리 등록 + 실행 명령 메타데이터 + CLI 서비스 연결`까지를 의미한다.

## 로컬 설치 방법

CLI 패키지 위치:

`Tools/cli`

Python 요구 버전:

`>=3.10`

설치 예시:

```powershell
cd Tools/cli
python -m pip install -e .
```

설치 후에는 `pyproject.toml`의 엔트리포인트 설정에 따라 `pb` 명령을 사용할 수 있다.

엔트리포인트:

`pb = "pbcli.main:main"`

## 직접 실행 방법

패키지 설치 없이 직접 실행할 수도 있다.

```powershell
cd Tools/cli
$env:PYTHONPATH='src'
python -m pbcli.main list
```

## 타깃 등록 구조

설정 파일:

`Tools/cli/src/pbcli/config/projects.yaml`

최상위 그룹:

- `backend`
- `frontend`
- `database`

각 항목 필드:

- `name`: CLI 식별자
- `path`: 저장소 루트 기준 상대 경로
- `kind`: `backend`, `frontend`, `database`
- `domain`: `post`, `shop`, `shared`
- `runtime`: `java`, `node`, `docker`
- `framework`
- `build`
- `db`
- `port`
- `start_command`
- `test_command`
- `env_file`

예시:

```yaml
backend:
  - name: post-java-springboot-maven-postgresql
    path: Backend/post/post-java-springboot-maven-postgresql
    kind: backend
    domain: post
    runtime: java
    framework: springboot
    build: maven
    db: postgresql
    port: 8000
    start_command: ".\\mvnw.cmd spring-boot:run"
    test_command: ".\\mvnw.cmd test"
    env_file: .env.example
```

## 새 백엔드 연결 절차

1. `Backend/post` 또는 `Backend/shop` 아래에 실제 프로젝트 폴더를 만든다.
2. 폴더명은 아래 규칙을 반드시 따른다.

`<domain>-<language>-<framework>-<build>-<dataaccess>-<db>`

3. `projects.yaml`의 `backend` 그룹에 `name`, `path`를 추가한다.
4. `start_command`, `test_command`, `port`, `env_file`를 함께 등록한다.
5. backend는 기본 포트 시작값 `8000`, frontend는 기본 포트 시작값 `3000`을 사용한다.
6. 여러 앱을 동시에 띄우면 CLI가 같은 그룹 안에서 사용 가능한 다음 포트를 자동 할당한다.
7. 수동 지정이 필요하면 `pb up <target> --port <number>`를 사용한다.
8. `pb list` 또는 `python -m pbcli.main list`로 등록 여부를 확인한다.
9. `pb up <target>` 또는 `pb test <target>`로 실제 연결을 검증한다.

예시:

```yaml
- name: shop-java-springboot-gradle-jdbc-mysql
  path: Backend/shop/shop-java-springboot-gradle-jdbc-mysql
```

## 새 프론트 연결 절차

1. `Frontend` 아래에 새 앱 폴더를 만든다.
2. 프론트 식별자는 현재 `web-post`, `web-shop` 규칙을 사용한다.
3. `projects.yaml`의 `frontend` 그룹에 등록한다.
4. `pb list`로 출력 여부를 확인한다.

예시:

```yaml
- name: web-post
  path: Frontend/web-post
```

## 새 DB 타깃 연결 절차

1. 실제 저장 위치를 `Database` 아래에 만든다.
2. DB 타깃명은 `db-...` 접두어를 사용한다.
3. `projects.yaml`의 `database` 그룹에 등록한다.
4. 향후 `pb db up`, `pb db down`, `pb db reset` 어댑터가 이 식별자를 참조한다.

현재 등록 기준:

- `db-postgresql`
- `db-mysql`
- `db-redis`
- `db-elasticsearch`

## 검증 절차

등록 후 최소 검증 순서:

1. `cd Tools/cli`
2. `python -m pip install -e .`
3. `pb list`
4. 추가한 항목의 `name`과 `path`가 출력되는지 확인

## 유지보수 규칙

- `name`은 실제 폴더명과 다르게 쓰지 않는다.
- `path`는 저장소 루트 기준 상대 경로만 사용한다.
- 백엔드 식별자는 [post 04_language](../post/04_language.md), [shop 04_language](../shop/04_language.md) 규칙과 동일해야 한다.
- 현재 CLI는 실행 서비스 계층과 GUI까지 포함하므로, 새 타깃을 추가할 때는 명령 메타데이터까지 함께 유지해야 한다.
- frontend는 하드코딩된 포트를 넣지 않고 CLI가 주입하는 `VITE_APP_PORT`를 따르는 구성을 유지한다.
- backend는 `/docs` Swagger 경로를 유지하고 CLI가 주입하는 실행 포트를 우선 사용한다.

## 현재 한계

- 모든 실행은 Windows PowerShell 새 창 기준으로 설계되어 있다.
- `pb down`은 CLI 상태 파일에 기록된 PID만 종료할 수 있다.
- `pb db reset`은 Docker 컨테이너가 기동 중이어야 정상 동작한다.
