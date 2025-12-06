# 제출 파일 가이드

## 제출해야 할 파일 목록

### ✅ 필수 포함 파일

#### 1. 소스 코드
```
backend/
├── src/
│   └── main/
│       ├── java/com/bugshot/          # 모든 .java 파일
│       └── resources/                  # 설정 파일
│           ├── application.yml
│           ├── application-local.yml
│           └── application-prod.yml
├── build.gradle
├── settings.gradle
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
└── README_SUBMISSION.md (또는 README.md)
```

#### 2. 실행 파일
```
backend/build/libs/bugshot-0.0.1-SNAPSHOT.jar
```

#### 3. 문서
- 최종 보고서 (PDF 또는 Word)
- 발표 파일 (PPT 또는 PDF)
- README.md (실행 가이드)

### ❌ 제외할 파일/폴더

- `build/` 폴더 전체 (단, `build/libs/*.jar`는 포함)
- `.gradle/` 폴더
- `bin/` 폴더
- `src/test/` 폴더 (테스트 코드)
- `.idea/`, `.vscode/` 등 IDE 설정
- `.git/` 폴더
- `*.iml`, `*.ipr` 등 IDE 파일

## ZIP 파일 생성 방법

### Windows (PowerShell)
```powershell
# backend 폴더에서 실행
Compress-Archive -Path `
  src,`
  build.gradle,`
  settings.gradle,`
  gradle,`
  gradlew,`
  gradlew.bat,`
  README_SUBMISSION.md,`
  build/libs/bugshot-0.0.1-SNAPSHOT.jar `
  -DestinationPath ../bugshot_submission.zip
```

### Linux/Mac
```bash
cd backend
zip -r ../bugshot_submission.zip \
  src \
  build.gradle \
  settings.gradle \
  gradle \
  gradlew \
  gradlew.bat \
  README_SUBMISSION.md \
  build/libs/bugshot-0.0.1-SNAPSHOT.jar \
  -x "*.git*" \
  -x "build/classes/*" \
  -x "build/generated/*" \
  -x ".gradle/*"
```

### 수동 선택 방법
1. `backend` 폴더에서 다음 폴더/파일 선택:
   - `src/` 폴더
   - `gradle/` 폴더
   - `build.gradle`
   - `settings.gradle`
   - `gradlew`
   - `gradlew.bat`
   - `README_SUBMISSION.md`
   - `build/libs/bugshot-0.0.1-SNAPSHOT.jar`

2. ZIP으로 압축

## 최종 제출물 체크리스트

- [ ] 소스 코드 파일들 (src/main/java/**/*.java)
- [ ] 설정 파일들 (application.yml 등)
- [ ] 빌드 파일들 (build.gradle, gradlew 등)
- [ ] 실행 파일 (bugshot-0.0.1-SNAPSHOT.jar)
- [ ] README.md (실행 가이드)
- [ ] 최종 보고서 (PDF)
- [ ] 발표 파일 (PPT)
- [ ] 모든 파일을 하나의 ZIP 파일로 압축

## 파일 크기 확인

제출 전 ZIP 파일 크기를 확인하세요:
- 예상 크기: 10-50MB (소스 코드 + JAR 파일)
- 100MB 이상이면 불필요한 파일이 포함된 것일 수 있습니다

