# 아이북조아 📚
> 🏆 본 프로젝트는 LG U+ 유레카 1기 백엔드 비대면 **최우수상** 종합 프로젝트입니다. <br />
> 개발기간: 2024.10.15 ~ 2024.11.03 (3주)

## _Intro._
프로젝트 주요 기능은 다음과 같습니다:
1. **자녀 성향 진단 시스템**: 자녀의 MBTI 성향을 진단 및 조회할 수 있습니다. 사용자의 삭제 요청에 따라 진단 데이터가 논리적으로 삭제되며, 한달 후 물리적으로 삭제됩니다.
2. **맞춤형 콘텐츠 추천 시스템**: 자녀의 성향에 맞는 도서 콘텐츠를 추천합니다. 사용자의 좋아요/싫어요 피드백에 따라 추천 책이 익일 새벽에 변경됩니다.
3. **선착순 이벤트 시스템**: 매일 오후 1시에 10만 요청을 처리할 수 있는 이벤트 시스템입니다.

<p align="center">
  <img src="https://github.com/user-attachments/assets/ea901338-0f76-457a-a046-7763ef2d8967">
</p>

<br />

## _Documents._
- [회의록](https://www.notion.so/1221822ed9d181789271e61afb3a5d78?v=1221822ed9d181faa86e000c9e597b39&pvs=4)
- [API 명세서](https://www.notion.so/API-1221822ed9d1816281e9c6233979f87f?pvs=4)
- [프론트 명세서](https://www.notion.so/1221822ed9d181688907d0395b07ff4f?pvs=4)
- [페어 프로그래밍하면서 학습/고민했던 내용](https://www.notion.so/1221822ed9d181598ccbd47d8bfa8b7c?v=1221822ed9d181efaa79000c2c20b94a&pvs=4)
- [Coding Convention](https://courageous-fluorine-f2d.notion.site/Coding-Convention-1221822ed9d1818495b9c87f3697c8bd?pvs=4)
- [Commit Convention](https://courageous-fluorine-f2d.notion.site/Commit-Convention-1221822ed9d18103b25dd975d1978b30?pvs=4)

<br />

## _ER Diagram._
![image](https://github.com/user-attachments/assets/c47587b7-bf58-49b5-b314-1a161768c2be)

## _Stack._
> Backend
- Java 17, SpringBoot 3.3.4
- JWT, Spring Security, OAuth 2.0
- Spring Data JPA, JDBC
- Spring Batch
- MySQL 8.0, Redis
- ChatGPT API
- JUnit5, Mockito, JMeter

> Frontend
- HTML, JavaScript

> Collaborations
- Jira
- Slack
- Notion

<br />

## _SW Architecture._
![image](https://github.com/user-attachments/assets/a02d128d-5c7a-4927-934f-4fda230a655e)

<br />

## _Member._
<div align="center">

|**김범수**|**박시은**|**이도림**|**이민수**|**이승희**|**이신지**|
|:------:|:------:|:------:|:------:|:------:|:------:|
| [<img src="https://avatars.githubusercontent.com/u/88920973?v=4" height=150 width=150> <br/> @KIMBUMSU123](https://github.com/KIMBUMSU123) | [<img src="https://avatars.githubusercontent.com/u/62862307?v=4" height=150 width=150> <br/> @ssIIIn](https://github.com/ssIIIn) | [<img src="https://avatars.githubusercontent.com/u/65598286?v=4" height=150 width=150> <br/> @LeeDoRim](https://github.com/LeeDoRim) | [<img src="https://avatars.githubusercontent.com/u/89891084?v=4" height=150 width=150> <br/> @99MinSu](https://github.com/99MinSu) | [<img src="https://avatars.githubusercontent.com/u/87460638?v=4" height=150 width=150> <br/> @leeseunghee00](https://github.com/leeseunghee00) | [<img src="https://avatars.githubusercontent.com/u/153038259?v=4" height=150 width=150> <br/> @kuma0112](https://github.com/kuma0112)  |

</div>

<br />

## _Role._
효율적인 협업과 코드 품질 향상을 위해 **페어 프로그래밍**으로 진행했습니다. <br />
개발 완료 시 코드 리뷰를 통해 오류를 최소화하고 안정적인 기능을 구현합니다.

- `인증/인가`: 김범수, 박시은
- `성향 진단`: 이도림, 이민수
- `피드백` & `추천`: 이승희, 이신지
- `응모 이벤트`: 박시은, 이도림, 이민수
- `AI` & `배치 성능테스트`: 김범수, 이승희, 이신지

<br />

## _Sequence Diagram._
#### 1. 일반 로그인 & 카카오 로그인
- 일반 로그인: 이메일과 비밀번호로 로그인 요청 → 존재하는 회원일 경우, 헤더에 AccessToken, 쿠키에 RefreshToken 담아서 전달
- 카카오 로그인: `/oauth2/authorization/kakao` 로 인증 요청 →  OAuth Success Handler 가 성공 요청을 인지하면 사용자 정보 획득 → 토큰 발급

![image](https://github.com/user-attachments/assets/d0a30449-e4df-4b35-92f2-c84f8948c9b8)

<br />

#### 2. 피드백 (좋아요 & 싫어요)
- 피드백: 좋아요/싫어요 데이터가 다량 발생할 것을 고려하여 Redis 에 저장 후, 익일 새벽에 배치 처리를 통해 MySQL 로 이관
- 피드백 배치: 총 4개의 Step 으로 구성하여 자녀 성향에 반영
  - Step1. Redis 에 임시 저장되어 있던 좋아요/싫어요를 MzySQL 에 이관
  - Step2. 오늘자 피드백을 읽어 성향에 반영될 점수 계산 및 누적된 성향 변화량 업데이트
  - Step3. 누적 변화량 ≥ 5 일 경우, 자녀 성향 레코드 생성
  - Step4. MBTI 변화가 감지될 경우, 새로운 MBTI 레코드 생성

![image](https://github.com/user-attachments/assets/9cc80f92-bd0d-4b3a-adb9-7355ab1c3bd1)

<br />

#### 3. 추천책 서비스
- 대량의 데이터를 페이징 방식으로 분할 처리하여 메모리 사용량을 최소화할 수 있는 JdbcPagingItemReader 사용

![image](https://github.com/user-attachments/assets/5473e261-ac59-4992-a207-a6a7ba9e9b3c)

<br />

## _Trouble Shooting._
#### 1. PasswordEncoder 순환 참조 문제
- 문제 원인: PasswordEncoder 가 빈으로 등록되어 있는 SecurityConfig 가 다시 MemberService 를 참조하면서 순환참조가 발생함을 인지
- 해결: PasswordEncoder 를 독립적인 Config 클래스로 분리하여 해결
- 정리한 문서: [순환 참조 문제 해결 과정](https://courageous-fluorine-f2d.notion.site/713843732919462caeaf4496ed955ad3?pvs=4)

<br />

#### 2. 카카오 로그인 성공 후, 메인 페이지 리다이렉트 시 헤어데 담은 AccessToken 이 전송되지 않는 문제
- 해결1: 소셜 로그인 성공 후 RefreshToken 은 쿠키에, AccessToken 은 리다이렉트 URL 에 담아서 전달. 
- 해결2: url 에 직접적으로 AccessToken 담는 방식은 토큰 탈취에 매우 취약하므로, OTT(One-Time Token) 을 사용하여 보안을 강화함.
- 정리한 문서: [토큰탈취 및 CSRF](https://courageous-fluorine-f2d.notion.site/CSRF-87471d32a44b4e0eb45d7fb9771a1ab1?pvs=4)

<br />

#### 3. 성향 삭제 시 delete 쿼리가 하나씩 날아가는 문제
- 문제 원인: MySQL IDENTITY 전략으로 인해 `deleteAll()` 을 실행했지만 실제 delete 쿼리가 하나씩 날아감.
- 해결: `deleteAllBatch()` 를 사용해 한 번의 delete 쿼리로 데이터를 삭제할 수 있도록 변경 → 부하를 줄임.

<br />

#### 4. 응모 이벤트 단시간 트래픽 관련 고려 사항
- 문제 원인: 초기 분산락을 고려했으나, 데드락 위험과 성능 오버헤드가 존재
- 해결: Lua Script 를 활용하여 Atomic 연산 보장

<br />

- 문제 원인: 등수 데이터를 저장하고 정렬하는 데 ZSET 자료구조를 사용할 수 있었으나, 모든 요소에 score가 추가됨에 따라 메모리 사용량이 증가하는 문제 발생
- 해결: `Set + CreatedAt`으로 필요한 정보만 최소한으로 저장 후, 이벤트 종료 시 시간 순으로 정렬하는 방식 채택 
→ INCR 를 통한 시간 복잡도 O(1) & 중복 체크 멱등성 보장

<br />

#### 5. 그 외 소소한 고민 정리
- [프로젝트에 적합한 OpenAI 모델 알아보기](https://courageous-fluorine-f2d.notion.site/AI-3792a9408f274579b58bff9ce9d9c608?pvs=4)
- [피드백 구현을 위한 Redis 구조 고민](https://courageous-fluorine-f2d.notion.site/Redis-8944120d10c442d2af83641b73ce8fe1?pvs=4)
- [도서 검색 성능 향상을 위한 Full-text Search 적용](https://courageous-fluorine-f2d.notion.site/Full-text-Search-5827ada6391f4b409107a0eaee9163fb?pvs=4)
- [Put vs. Patch](https://courageous-fluorine-f2d.notion.site/Put-VS-patch-7337a3bf39c04849aedbbf681c2157b5?pvs=4)
- [왜 구현체가 1개인데 service impl 해야 하나요?](https://courageous-fluorine-f2d.notion.site/1-service-impl-99983a0b0bab4083b4b30ee90d412738?pvs=4)
- [PK 2개 vs. PK 1개(feat.FK 2개)](https://courageous-fluorine-f2d.notion.site/PK-2-vs-PK-1-feat-FK-2-7448d2db112244dbbcccd1b12b8639fd?pvs=4)

<br />

## _Test._
모든 테스트는 **데이터 10만을 기준으로** 진행합니다.

#### 1. 피드백 배치
- 개선 배치: 초기 step 을 2개로 구성하였다가 **4개로 분리하여 순차적으로 테이블을 업데이트**하도록 변경 
- Step별 테스트 결과: 1m 1s → 6m 6s → 22s → 46s = 8m 5s 
- Job 테스트 결과: 7m 22s
- 개선 필요: 병목 지점인 step2 에서 자녀와 책 성향을 조회하는 쿼리가 하나씩 날아가는 문제 해결
  ![image](https://github.com/user-attachments/assets/ace42950-7299-4e5f-84f5-45da3b742680)

<br />

#### 2. 추천책 배치
- 기존 배치: JPA Identity 전략으로 인한 Bulk Insert 불가능
- 개선한 배치: 대량의 데이터를 한꺼번에 삽입후 INSERT 작업을 처리할 수 있도록 **임시 테이블 생성** & NamedParameterTemplate 으로 **Batch Insert 처리**
- 개선 필요: reader 와 processor 쿼리 최적화
  ![image](https://github.com/user-attachments/assets/2be55bc9-1bf5-4323-8295-1d356eebb34e)

<br />

#### 3. 이벤트 배치
- 2,000명의 유저가 중복해서 데이터를 신청하는 상황을 테스트 한 결과, 중복 방지가 작동됨을 확인
- **5분동안 총 92,3008건의 요청을 처리함**으로써, 분당 10만 요청 목표 달성! (1초당 약 1,667요청)
  ![image](https://github.com/user-attachments/assets/3ccb6066-78cd-4838-9b3b-709315e81531)

