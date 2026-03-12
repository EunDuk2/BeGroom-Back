# BeGroom

국내 이커머스 서비스를 벤치마킹하여 구현하고,  
주문/결제 기능의 안정성 확보와 성능 개선까지 함께 진행한 팀 프로젝트입니다.

<img width="1576" height="1480" alt="begroom-randing" src="https://github.com/user-attachments/assets/32b69a62-13fb-41b1-ba16-559dfcf73fd8" />


## 프로젝트 소개

- **프로젝트명**: BeGroom
- **주제**: 이커머스 서비스 구현
- **형태**: 팀 프로젝트
- **인원**: 4명 (프론트엔드 1명, 백엔드 3명)
- **기간**: 2025.12 ~ 2026.02 (이후 개인 프로젝트로 성능 개선 진행중)
- **개요**: 국내 이커머스 서비스를 벤치마킹하여 구현하고, 안정적인 주문/결제 처리와 성능 개선까지 함께 고민한 프로젝트

## 기술 스택

<table>
  <tr>
    <td width="50%" valign="top">

### Backend

- JDK 25
- Spring Boot 4.0.1
- MySQL 8.0
- JUnit 5

    </td>
    <td width="50%" valign="top">

### Infra / DevOps

- Docker
- On-Premise(12Core, 16GB)
  (팀원별 환경은 달랐으며, 저는 On-Premise 환경에서 진행했습니다.)

    </td>
  </tr>
</table>

## 조은성 담당 역할
- 주문/결제 기능 구현
- 주문/결제 안정화
- 성능 최적화

## 주요 내용

### [주문→결제 부하 테스트(VUs 400+) 안정화: Nginx 연결 한도 조정으로 실패율 20% → 0%](https://www.notion.so/VUs-400-Nginx-20-0-318b1da1d9f980de891eebd81644c793?pvs=21)

- VUs 400 동시 요청에서 worker_connections 한도로 연결이 포화되어 Nginx가 close()/shutdown()로 일부 연결을 강제 종료 → **‘EOF/connection reset’로 약 20% 실패 발생**
- worker_connections와 프로세스 FD limit을 함께 상향 조정해 강제 종료를 제거하고, VUs 400+에서도 요청 **실패율 0%로 안정화**

### [결제 동시성 이슈(Deadlock) 안정화: S Lock 경합 제거](https://www.notion.so/Deadlock-S-Lock-316b1da1d9f980eba53ecd563d2a72f4?pvs=21)

- IDENTITY 전략으로 INSERT가 즉시 실행되며 **S Lock이 먼저 걸려 X Lock 승격이 충돌**
- FOR UPDATE로 주문 레코드 X Lock 선점 → **동일 주문 결제 로직 직렬화로 데드락 제거**

### [주문 서비스 비대화 해소: 절차 지향 설계 → 책임 주도 설계(RDD) 전환](https://www.notion.so/RDD-318b1da1d9f980789c4aff28f7cd6d84?pvs=21)

- OrderService가 도메인 데이터를 직접 판단·연산하며 **흐름을 중앙에서 통제**(변경 영향 확산)
- RDD로 책임을 도메인에 이동 → 객체 협력으로 주문 흐름 구성 → **변경, 테스트 용이성 개선**

### [On-Premise 운영 환경 구축](https://www.notion.so/On-Premise-31fb1da1d9f9802390d5c6d00ac136b1?pvs=21)

- Ubuntu/Linux(12core·16GB) 단일 서버에 Nginx/Application/DB를 Docker Compose로 컨테이너화해 운영 환경을 표준화
- DuckDNS로 도메인–공용 IP를 연결하고, Nginx에서 HTTPS(TLS) 종료 및 Path 기반 라우팅으로 서비스 요청을 분기

### TDD/BDD 기반으로 개발·테스트를 병행하며 요구사항을 시나리오 중심으로 검증

- 단위/통합 테스트를 구축해 핵심 흐름을 자동화하고, 다양한 엣지 케이스를 테스트에 반영해 안정성을 확보
