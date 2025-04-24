## HttpSession vs Redis

    세션 관리 방식을 어떻게 할지 결정할 때, 자주 비교되는 대상.

### 1. HttpSession (기본 세션 방식)

    서버 내부 메모리에 세션 데이터를 저장함, Java EE or Spring Boot 가 기본 제공.

    ● 특징
    - 세션 데이터는 WAS 메모리에 저장된 (Map 형태)
    - 클라이언트는 JESSIOND 쿠키로 세션 식별
    - WAS 가 꺼지거나 재시작되면 세션 정보 사라짐
    

    ● 장점

    ● 단점


### 프로젝트 로직 (Order 주문, HttpSession 사용)    

### 2. Redis 세션 저장소 (분산 캐시 기반)

    세션 정보를 Redis 같은 외부 저장소에 저장, Spring Session + Redis 조합이 대표적

    ● 특징

    ● 장점

    ● 단점

### 프로젝트 사용 로직 (Order 주문, Redis 사용)