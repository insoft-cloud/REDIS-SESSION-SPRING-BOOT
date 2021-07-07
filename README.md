Redis를 활용한 Session Clustering 구현
---
## Spec
- SpringBoot 2.5.2
- Gradle 6.8.3
- spring-session-data-redis 2.5.0

## 개요
- Redis를 활용해서 Session Clustering(다중화된 WAS 환경에서 Session을 통합)을 구성
- SpringBoot에서는 Redis에서 간단하게 Session을 관리할 수 있도록 해준다.
- 본 프로젝트는 Redis 서버를 로컬에 설치하여 사용하였고, 세션 클러스터링이 이루어지는 지 확인하기 위하여 포트만 다른 아주 간단한 동일 프로젝트를 2개 생성하여 WAS가 2개인 환경에서 실행 및 테스트하였다.


### 1. build.gradle 에 Dependency 추가
- SpringBoot와 Redis를 연동하고, redis session 관련 기능을 사용하기 위한 필수 라이브러리 추가
```
[build.gradle]

implementation 'org.springframework.boot:spring-boot-starter-web'

// Redis Session library
implementation group: 'org.springframework.boot', name: 'spring-boot-starter-data-redis', version: '2.5.0'
implementation group: 'org.springframework.session', name: 'spring-session-data-redis', version: '2.5.0'
```

### 2. Redis Server 접근 정보 정의
- resources 아래 application.yml 에 Redis 연동을 위한 정보를 설정한다.
- 아래는 예제 이므로 각 시스템에 맞게 설정하여야 한다.
```
spring:
  redis:
    host: localhost          #redis server host ip
    port: 6379               #redis server port
    password: ''             #redis server password(local에 설치한 redis의 기본 password는 없다. 변경 가능)
```

### 3. Bean 등록
```
* com.insoft.springboot.redis.session > config > RedisConfig.java 클래스 참고
```
- @EnableRedisHttpSession을 추가함으로써 Redis Session Cluster 환경을 구성
    - maxInactiveIntervalInSeconds : 세션 만료시간. 초 단위이며 600일 경우 600초, 즉 10분을 뜻한다.


### 4. 구성
```
* com.insoft.springboot.redis.session > controller > SampleController.java 클래스 참고
```
1) 사용자가 웹 페이지에 접속하면 미리 static으로 미리 정의해놓은 문자열 key(name), value(piglet) 값을 가지고 Session의 key 에 해당하는 attribute 값이 value와 동일한 지 판별
2) 동일한 경우 새로 세션을 생성하지 않고 기존의 세션 정보를 화면(view)로 전달하고, 다를 경우 세션이 없다고 판단하여 새로 세션을 생성하고 생성된 새 세션의 정보를 화면(view)에 전달한다.
3) 'http://localhost:7777(SPRING-BOOT-REDIS-SESSION)'과 'http://localhost:7778(SPRING-BOOT-REDIS-SESSION2)' 실행하여 접속
(세션이 만료되기 전까지 session id 값이 같음을 확인. 크롬의 시크릿 모드 창을 하나 더 열 경우에는 세션을 공유하지 않으므로 새로운 session id가 보여진다.)
![session_cluster]

4) Redis에 저장된 세션의 정보를 확인하는 방법은 2가지가 있다.
    1) redis client tool을 통해 확인
    ![session_confirm]
    2) redis cli를 통해 확인
        ```
       127.0.0.1:6379> keys *
        1) "01"
        2) "spring:session:sessions:c72fa465-3b86-4fcc-9403-ee872f21f6c7"
        3) "admm"
        4) "spring:session:expirations:1625579280000"
        5) "hihi"
        6) "oh1"
        7) "spring:session:sessions:expires:c72fa465-3b86-4fcc-9403-ee872f21f6c7"
       
       ---
       
       127.0.0.1:6379> hgetall spring:session:sessions:c72fa465-3b86-4fcc-9403-ee872f21f6c7
       1) "maxInactiveInterval"
       2) "\xac\xed\x00\x05sr\x00\x11java.lang.Integer\x12\xe2\xa0\xa4\xf7\x81\x878\x02\x00\x01I\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x02X"
       3) "lastAccessedTime"
       4) "\xac\xed\x00\x05sr\x00\x0ejava.lang.Long;\x8b\xe4\x90\xcc\x8f#\xdf\x02\x00\x01J\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x01z|\t\xe2\x01"
       5) "sessionAttr:name"
       6) "\xac\xed\x00\x05t\x00\x06piglet"
       7) "creationTime"
       8) "\xac\xed\x00\x05sr\x00\x0ejava.lang.Long;\x8b\xe4\x90\xcc\x8f#\xdf\x02\x00\x01J\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b\x02\x00\x00xp\x00\x00\x01z|\t\xe0&"
       ```

[session_cluster]: ./images/session_cluster.PNG
[session_confirm]: ./images/session_confirm.PNG
