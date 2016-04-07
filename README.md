# Radiople: android application

반갑습니다.  
이 프로젝트는 아직 출시예정 단계인 '라디오플'의 오픈소스 repository 입니다.  
실제 운용될 서비스 코드와는 차이가 있을 수 있습니다.

이 프로젝트는 라이브러리가 아닙니다.  
안드로이드 개발 관련 공부의 level로 생각해주시면 영광입니다.

Homepage: [http://radiople.com](http://radiople.com)

# Configuration

이 프로젝트를 여러분들이 공부하는데 도움이되도록 참고 하신다면  
아래 설정은 무시하셔도 괜찮습니다. 

/app/build.gradle에서 API, Image서버 정보를 수정해야합니다.

```
 buildTypes {
        release {
            minifyEnabled false

            buildConfigField "String", "API_SERVER_HOSTNAME", "\"https://api.radiople.com\""
            buildConfigField "int", "API_PORT", "80"

            buildConfigField "String", "IMAGE_SERVER_HOSTNAME", "\"https://image.radiople.com\""
            buildConfigField "int", "IMAGE_SERVER_PORTNAME", "80"
        }
```

# Thanks
관련 문제나 질문은 이슈 트래커, 이메일로 보내주시면 답변해드리겠습니다.

If you have any issue? please let me know [github issue tracker](https://github.com/leejaycoke/radiople-app/issues) or make a email for me leejaycoke@gmail.com.
