# 03 Runbook

## 목적

이 문서는 공용 CLI를 실제로 사용할 때의 기본 흐름을 예시 중심으로 설명한다.
현재 가능한 시나리오와 앞으로 연결될 시나리오를 구분해서 기록한다.

## 초기 설치 시나리오

CLI 설치:

```powershell
cd Tools/cli
python -m pip install -e .
```

설치 확인:

```powershell
pb list
```

기대 결과:

- `backend`
- `frontend`
- `database`

세 그룹이 출력되고, 각 그룹 아래에 등록된 타깃 목록이 표시된다.

## 새 백엔드 타깃 등록 시나리오

예시 목표:

`post-java-springboot-maven-jpa-postgresql`를 CLI에 연결된 백엔드 타깃으로 유지하거나 추가한다.

절차:

1. 실제 폴더가 존재하는지 확인한다.
2. `Tools/cli/src/pbcli/config/projects.yaml`의 `backend` 그룹에 등록한다.
3. 아래 명령으로 목록을 확인한다.

```powershell
cd Tools/cli
$env:PYTHONPATH='src'
python -m pbcli.main list
```

4. 출력에 해당 타깃이 나타나면 등록은 완료다.

주의:

- CLI 식별자와 실제 폴더명이 다르면 안 된다.
- `post`와 `shop`의 도메인 접두어를 섞으면 안 된다.

## 새 프론트 타깃 확인 시나리오

현재 기본 프론트 타깃:

- `web-post`
- `web-shop`

확인 방법:

```powershell
pb list
```

출력에서 아래 항목이 보여야 한다.

```text
[frontend]
- web-post: Frontend/web-post
- web-shop: Frontend/web-shop
```

## DB 사용 시나리오

현재 DB 관련 명령은 실제로 구현되어 있다.

예시:

```powershell
pb db reset postgresql post
```

현재 기대 동작:

- PostgreSQL 컨테이너 안에서 `pb_post`를 drop/create
- `Database/postgresql/post/init/01_schema.sql` 적용
- `Database/postgresql/post/seeds/01_seed.sql` 적용

## 로컬 점검 시나리오

현재 가능한 최소 점검:

1. `pb list`가 정상 실행되는지 확인
2. `projects.yaml` 수정 후 출력 반영 여부 확인
3. `pb doctor`로 로컬 도구 상태를 확인
4. `pb up <target>` 후 `pb down <target>`으로 런타임 상태를 확인

예시:

```powershell
pb test web-shop
pb doctor
```

현재 기대 결과:

- `pb test ...`: 대상 프로젝트의 테스트 스크립트 실행
- `pb doctor`: Python, Node, npm, Java, Docker 경로 출력

## 흔한 실수

- `projects.yaml`의 `name`과 실제 폴더명을 다르게 적는 경우
- `path`를 절대 경로로 적는 경우
- `backend`, `frontend`, `database` 그룹 키를 바꾸는 경우
- 백엔드 이름에서 `springboot`, `nestjs`, `jpa`, `jdbc`, `typeorm`, `knex` 같은 고정 토큰을 임의로 변경하는 경우
- `pb db reset` 전에 DB 컨테이너를 올리지 않는 경우
- `pb down` 호출 전에 `pb up` 기록이 없는 경우

## 운영 메모

- 현재 기준에서 `pb list`, `pb up`, `pb down`, `pb test`, `pb db up/down/reset`, `pb doctor`, `pb gui`가 구현되어 있다.
- 실행 어댑터와 앱 baseline이 추가되면 이 문서에 구현체별 세부 runbook을 이어서 확장한다.
