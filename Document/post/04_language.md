# 04 Language

## 목적

이 문서는 `post` 서비스에서 실제로 생성해야 하는 백엔드 구현 프로젝트 수와 폴더명 규칙, CLI 식별 규칙을 정의하는 기준 문서다.
기존의 일반 기술 비교 문서가 아니라, 현재 저장소 구조에 맞는 실제 생성 기준을 고정하는 것이 목적이다.

`post`와 `shop`은 동일한 기술 매트릭스를 사용하며, 이 문서는 그중 `post` 도메인에 대한 기준만 다룬다.

## 서비스 기준 생성 프로젝트 수

`post` 서비스는 아래 기술 축을 기준으로 백엔드 프로젝트를 생성한다.

- 언어: `Java`, `TypeScript`
- 프레임워크: `Spring Boot`, `NestJS`
- 빌드 도구:
  - Java: `Maven`, `Gradle`
  - TypeScript: `npm`
- DB 엔진: `postgresql`, `mysql`

프로젝트 수 계산:

| 구분 | 계산식 | 결과 |
| --- | --- | --- |
| Java | `2 build tools x 2 db` | `4개` |
| TypeScript | `1 build tool x 2 db` | `2개` |
| 합계 | `4 + 2` | `6개` |

## 폴더명 규칙

실제 저장소 폴더명과 CLI 인자는 아래 규칙으로 통일한다.

- Java: `<domain>-java-springboot-<build>-<db>`
- TypeScript: `<domain>-typescript-nestjs-npm-<db>`

작성 규칙:

- 전부 소문자를 사용한다.
- 구분자는 `-`만 사용한다.
- 도메인은 맨 앞에 둔다.
- 프레임워크 표기는 `springboot`, `nestjs`를 사용한다.
- TypeScript 프로젝트는 식별자에 `npm`을 포함한다.
- DB 엔진은 `postgresql`, `mysql`을 사용한다.

예시:

- `post-java-springboot-maven-postgresql`
- `post-java-springboot-gradle-mysql`
- `post-typescript-nestjs-npm-postgresql`
- `post-typescript-nestjs-npm-mysql`

## 백엔드 폴더 배치 규칙

`post` 도메인 구현체는 아래 경로 아래에 배치한다.

`Backend/post/<project-folder>`

예시:

- `Backend/post/post-java-springboot-maven-postgresql`
- `Backend/post/post-java-springboot-gradle-mysql`
- `Backend/post/post-typescript-nestjs-npm-postgresql`
- `Backend/post/post-typescript-nestjs-npm-mysql`

## 언어별 생성 프로젝트 기준

### Java 생성 프로젝트

| 번호 | 프로젝트 폴더명 |
| --- | --- |
| 1 | `post-java-springboot-maven-postgresql` |
| 2 | `post-java-springboot-maven-mysql` |
| 3 | `post-java-springboot-gradle-postgresql` |
| 4 | `post-java-springboot-gradle-mysql` |

### TypeScript 생성 프로젝트

| 번호 | 프로젝트 폴더명 |
| --- | --- |
| 1 | `post-typescript-nestjs-npm-postgresql` |
| 2 | `post-typescript-nestjs-npm-mysql` |

## 공용 CLI 위치와 역할

공용 Python CLI는 `post` 전용이 아니라 저장소 전체를 제어하는 공용 도구로 둔다.

CLI 위치:

`Tools/cli`

CLI 역할:

- 실행 대상 프로젝트 선택
- 환경 기동
- 테스트 실행
- DB 초기화/검증
- 서비스와 구현체 조합별 실행 명령 통합

CLI 식별 기준:

- CLI는 프로젝트 폴더명을 기준 식별자로 사용한다.
- `post` 서비스 실행 대상은 `post-...` 접두어를 가진 프로젝트명으로 지정한다.

예시 인자:

- `post-java-springboot-maven-postgresql`
- `post-typescript-nestjs-npm-mysql`

## 결론

이 문서 기준으로 `post` 서비스는 총 `6개`의 백엔드 구현 프로젝트를 가진다.

- Java: `4개`
- TypeScript: `2개`

공용 Python CLI는 `Tools/cli`에 `1개`만 두며, 실제 실행 대상은 위 폴더명 규칙을 따른 프로젝트명으로 선택한다.
