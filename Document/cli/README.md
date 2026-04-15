# CLI

## 목적

이 폴더는 `Project-Bible` 공용 CLI 문서를 관리한다.

공용 CLI는 `Tools/cli`에 위치하며, 저장소 전체의 실행 대상 식별, 로컬 실행, 테스트 실행, DB 관련 명령을 하나의 진입점으로 통합하는 것을 목표로 한다.

## 위치

- CLI 패키지: `Tools/cli`
- 명령 파서: `Tools/cli/src/pbcli/main.py`
- 타깃 레지스트리: `Tools/cli/src/pbcli/config/projects.yaml`

## 현재 상태 요약

- `pb list`: 실제 동작
- `pb up <target>`: 실제 동작
- `pb down <target>`: 실제 동작
- `pb test <target>`: 실제 동작
- `pb db up`: 실제 동작
- `pb db down`: 실제 동작
- `pb db reset <engine> <domain>`: 실제 동작
- `pb doctor`: 실제 동작
- `pb gui`: 실제 동작

현재 단계에서 CLI는 `타깃 등록`, `새 PowerShell 창 실행`, `런타임 상태 추적`, `DB 재초기화`, `Tkinter GUI`까지 포함하는 baseline 역할을 한다.

## 문서 구성

- [01 Command Reference](./01_command-reference.md)
- [02 Setup And Target Registration](./02_setup-and-target-registration.md)
- [03 Runbook](./03_runbook.md)

## 타깃 식별 규칙

- 백엔드: 폴더명과 동일한 식별자 사용
- 프론트: `web-post`, `web-shop`
- DB: `db-postgresql`, `db-mysql`, `db-redis`, `db-elasticsearch`

백엔드 식별자 규칙:

`<domain>-<language>-<framework>-<build>-<dataaccess>-<db>`

예시:

- `post-java-springboot-maven-postgresql`
- `shop-typescript-nestjs-npm-mysql`
