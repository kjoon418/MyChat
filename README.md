# MyChat

JWT, OAuth, Spring을 통한 채팅 서버 구현 연습 목적의 토이 프로젝트

## API 명세서
|경로|METHOD|인증에 필요한 값|전달해야 할 값(name: value)|반환되는 값|설명|
|---|---|---|---|---|---|

## ER-다이어그램
[ERDCloud](https://www.erdcloud.com/d/wjeAJAgfieEpQtStm) 참고

## 진행 상황
<details>
  <summary>회원 정보 관련 기능</summary>
  <div markdown="1">
  
  - [x] Google OAuth를 이용한 회원가입/로그인
  - [x] 자체 회원가입/로그인
  - [x] 엑세스 토큰 재발급
  - [x] 회원 정보 수정
  - [ ] 회원 삭제
</details>
<details>
  <summary>친구 관련 기능</summary>
  <div markdown="1">
  
- [x] 친구 추가
- [x] 친구 삭제
- [x] 친구 조회(조건X)
- [x] 친구 검색(조건O)
- [x] 다른 사용자 검색(조건O)
- [x] 보낸 친구 신청 조회
- [x] 받은 친구 신청 조회
- [x] 친구 신청 거절
- [x] 유저 차단
- [x] 유저 차단 해제
</details>
<details>
  <summary>채팅 관련 기능</summary>
  <div markdown="1">
  
- [ ] 1:1 채팅방 생성
- [ ] 단체 채팅방 생성
- [ ] 채팅방 수정
- [ ] 채팅방 초대
- [ ] 채팅방 나가기
- [ ] 채팅 생성
- [ ] 채팅 조회(조건X)
- [ ] 채팅 검색(조건O)
- [ ] 채팅 삭제(5분 안에 삭제시 완전 삭제, 이후엔 '삭제된 메시지입니다' 표시)
</details>
