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

설치 없이 저장소 루트 래퍼로 실행:

```powershell
.\pb.cmd list
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

`post-java-springboot-maven-postgresql`를 CLI에 연결된 백엔드 타깃으로 유지하거나 추가한다.

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

- `pb_post` 덮어쓰기 여부를 `Y/N`으로 확인
- PostgreSQL 컨테이너 안에서 `pb_post`를 drop/create
- `Database/postgresql/post/init/01_schema.sql` 적용
- `Database/postgresql/post/seeds/01_seed.sql` 적용

`Y` 또는 `yes`를 입력하면 덮어쓰고, `N` 또는 Enter를 입력하면 중지한다.

자동화가 필요한 경우에만 확인을 생략한다.

```powershell
pb db reset postgresql post --yes
```

## 로컬 점검 시나리오

현재 가능한 최소 점검:

1. `pb list`가 정상 실행되는지 확인
2. `projects.yaml` 수정 후 출력 반영 여부 확인
3. `pb doctor`로 로컬 도구 상태를 확인
4. `pb up <target>` 후 `pb down <target>`으로 런타임 상태를 확인

예시:

```powershell
pb up web-post --port 3000
pb down web-post
pb up web-shop --port 3000
pb down --port 3000
pb test web-shop
pb doctor
```

현재 기대 결과:

- `pb test ...`: 대상 프로젝트의 테스트 스크립트 실행
- `pb doctor`: Python, Node, npm, Java, Docker 경로 출력

## 포트 충돌 처리 시나리오

특정 포트로 실행:

```powershell
pb up web-post --port 3000
```

이미 포트가 열려 있으면 CLI는 점유 중인 CLI 타깃 또는 PID를 출력한다.
이 경우 아래 중 하나로 먼저 종료한 뒤 다시 실행한다.

```powershell
pb down web-post
pb down --port 3000
pb up web-post --port 3000
```

주의:

- `pb down <target>`은 CLI가 띄운 프로세스를 타깃명 기준으로 종료한다.
- `pb down --port N`은 해당 포트를 점유 중인 프로세스를 기준으로 종료한다.
- CLI가 띄우지 않은 프로세스도 종료할 수 있으므로 포트 번호를 확인하고 사용한다.

## 흔한 실수

- `projects.yaml`의 `name`과 실제 폴더명을 다르게 적는 경우
- `path`를 절대 경로로 적는 경우
- `backend`, `frontend`, `database` 그룹 키를 바꾸는 경우
- 백엔드 이름에서 `springboot`, `nestjs`, `maven`, `gradle`, `npm`, `postgresql`, `mysql` 같은 고정 토큰을 임의로 변경하는 경우
- `pb db reset` 전에 DB 컨테이너를 올리지 않는 경우
- `pb db reset` 확인 프롬프트에서 실수로 `Y`를 입력해 기존 데이터를 덮어쓰는 경우
- `pb down` 호출 전에 `pb up` 기록이 없는 경우
- 이미 열려 있는 포트를 `pb up <target> --port N`으로 다시 지정하는 경우

## 운영 메모

- 현재 기준에서 `pb list`, `pb up`, `pb down`, `pb test`, `pb db up/down/reset`, `pb doctor`, `pb gui`가 구현되어 있다.
- 실행 어댑터와 앱 baseline이 추가되면 이 문서에 구현체별 세부 runbook을 이어서 확장한다.
