# TeenyFinny Core Banking Mock (계정계)

TeenyFinny Core Banking Mock은 TeenyFinny 플랫폼의 **계정계(Core Banking System)** 역할을 담당하는 모의 서버 애플리케이션입니다. 이 시스템은 실제 금융권의 코어 뱅킹 시스템을 모사하여 계좌 원장 관리, 거래 처리, 투자 주문 체결 등의 핵심 금융 기능을 수행합니다.

## 👥 팀 소개

| <img alt="profile" src ="https://github.com/yes2489.png" width ="100" height ="100" style="border-radius: 50%; object-fit: cover;"> | <img alt="profile" src ="https://github.com/JBL28.png" width ="100" height ="100" style="border-radius: 50%; object-fit: cover;"> | <img alt="profile" src ="https://github.com/mingQ28.png" width ="100" height ="100" style="border-radius: 50%; object-fit: cover;"> | <img alt="profile" src ="https://github.com/hyojeongbae.png" width ="100" height ="100" style="border-radius: 50%; object-fit: cover;"> | <img alt="profile" src ="https://github.com/yangyanghyunjung.png" width ="100" height ="100" style="border-radius: 50%; object-fit: cover;"> | <img alt="profile" src ="https://github.com/CHICHIT.png" width ="100" height ="100" style="border-radius: 50%; object-fit: cover;"> |
| :---------------------------------------------------------------------------------------------------------------------------------: | :-------------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------: | :-------------------------------------------------------------------------------------------------------------------------------------: | :------------------------------------------------------------------------------------------------------------------------------------------: | :---------------------------------------------------------------------------------------------------------------------------------: |
|                                                             양은서 (PM)                                                             |                                                            이정복(PL)                                                             |                                                               박민서                                                                |                                                                 배효정                                                                  |                                                                    양현정                                                                    |                                                               이의섭                                                                |
|                                                [yes2489](https://github.com/yes2489)                                                |                                                 [JBL28](https://github.com/JBL28)                                                 |                                                [mingQ28](https://github.com/mingQ28)                                                |                                              [hyojeongbae](https://github.com/hyojeongbae)                                              |                                           [yangyanghyunjung](https://github.com/yangyanghyunjung)                                            |                                                [CHICHIT](https://github.com/CHICHIT)                                                |

---

## 🛠 기술 스택 (Tech Stack)

- **Language**: Java 17
- **Framework**: Spring Boot 3.5.7
- **Build Tool**: Gradle
- **Database**: MySQL (Production), H2 (Test)
- **Security**: Spring Security
- **Monitoring**: Spring Boot Actuator, Micrometer, Prometheus
- **Code Quality**: SonarQube, Jacoco

## 📂 프로젝트 구조 (Project Structure)

이 프로젝트는 핵심 금융 도메인을 중심으로 구성되어 있습니다:

- `account`: 입출금 계좌 관리 및 원장 기록
- `transaction`: 입금, 출금, 이체 등 거래 처리
- `investment`: 주식 매매, 포트폴리오 관리, 모의 투자 엔진
- `goal`: 목표 계좌(저금통) 원장 관리
- `user`: 사용자 식별 정보 관리 (채널계와 동기화)

## 🔗 채널계 연동 (Channel System Integration)

이 시스템은 **채널계(Channel System)**로부터 요청을 받아 처리하고 결과를 반환합니다.

- **역할**:
  - 금융 거래의 최종 승인 및 처리
  - 계좌 잔액 및 거래 내역의 원본 데이터(Source of Truth) 관리
  - 투자 주문의 체결 및 정산 시뮬레이션

## 🚀 시작하기 (Getting Started)

### 사전 요구사항 (Prerequisites)

- Java 17 이상
- Docker (선택 사항)
- MySQL

### 설치 (Installation)

1. 저장소 클론:
   ```bash
   git clone <repository-url>
   cd TeenyFinny_core_banking_mock
   ```

2. 프로젝트 빌드:
   ```bash
   ./gradlew clean build
   ```

### 애플리케이션 실행 (Running the Application)

```bash
./gradlew bootRun
```

애플리케이션은 기본적으로 `8080` 포트에서 실행됩니다.
(채널계와 동시에 실행 시 포트 충돌에 유의하세요. 필요 시 `server.port` 설정을 변경해야 합니다.)

### 설정 (Configuration)

주요 설정 파일: `src/main/resources/application.yml`

## 🧪 테스트 (Testing)

```bash
./gradlew test
```

## 🐳 Docker

```bash
docker build -t teenyfinny-core .
```

## 📊 모니터링 (Monitoring)

- Metrics: `/actuator/prometheus`
- Health: `/actuator/health`
