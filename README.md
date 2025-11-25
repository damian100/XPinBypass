# XPinBypass

**XPinBypass** 는 Android에서 **OkHttp의 SSL/TLS certificate pinning을 Xposed/LSPosed로 우회하는 데모 앱**입니다.  
연구·학습용으로, 하나의 앱 안에서 **“핀닝 적용 → 실패 → Xposed 모듈로 우회”** 흐름을 직접 확인할 수 있습니다.

> ⚠️ 이 프로젝트는 **오직 본인이 소유한 테스트 환경에서의 보안 연구/리버스 엔지니어링 학습**만을 목적으로 합니다.  
> 제3자의 서비스, 상용 앱 등에 대한 무단 후킹·우회는 법적 문제를 일으킬 수 있습니다.

---

## Features

- OkHttp 4.x + `CertificatePinner` 를 이용한 **SSL/TLS pinning 데모**
- Xposed/LSPosed 모듈 (`HookMain`) 로
    - `okhttp3.CertificatePinner.check`
    - `okhttp3.CertificatePinner.check$okhttp`  
      메서드를 후킹하여 pinning 검사 우회
- 모듈 ON/OFF 에 따라:
    - OFF → `SSLPeerUnverifiedException: Certificate pinning failure!`
    - ON → 동일 코드에서 예외 없이 통과
- 아주 단순한 UI:
    - 버튼 1개: `"핀닝 테스트 요청 보내기"`
    - 결과 TextView에 성공/실패 로그 출력

---

## How it works

### 1. App side (pinning)

- 패키지: `com.damian.xpinbypass`
- `PinnedClient.kt` 에서 OkHttp 클라이언트 구성:

    - `HOST = "example.com"`
    - 의도적으로 **잘못된 pin 값**을 넣어 둠

  ```kotlin
  private val certificatePinner = CertificatePinner.Builder()
      .add(HOST, "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
      .build()

  private val client = OkHttpClient.Builder()
      .certificatePinner(certificatePinner)
      .build()
  ```

- `MainActivity` 의 버튼을 누르면 `https://example.com/` 으로 요청을 보내고,
    - pin이 맞지 않기 때문에 원래는 `SSLPeerUnverifiedException` 이 발생합니다.

### 2. Xposed side (bypass)

- `assets/xposed_init`:

  ```text
  com.damian.xpinbypass.HookMain
  ```

- `HookMain` 은 `IXposedHookLoadPackage` 를 구현하고,
    - `com.damian.xpinbypass` 패키지에만 동작하도록 필터링
    - `okhttp3.CertificatePinner` 의 `check` / `check$okhttp` 메서드를 후킹

  ```kotlin
  val hook = object : XC_MethodHook() {
      override fun beforeHookedMethod(param: MethodHookParam) {
          val host = param.args.getOrNull(0)
          XposedBridge.log("XPinBypass: bypassing pinning for host=$host")
          param.result = null // void 메서드 → 검증을 그냥 성공 처리
      }
  }
  ```

- 모듈을 활성화하면 `CertificatePinner.check*` 가 실제 검증을 수행하기 전에
  훅이 개입하여 **예외 없이 통과**하게 됩니다.

---

## Requirements

- Android Studio / Gradle 기반 빌드 환경
- 루팅된 기기 또는 루팅된 에뮬레이터
- Magisk + LSPosed (또는 다른 Xposed 구현)
- 테스트용 네트워크 연결이 가능한 환경

---

## Build & Install

1. 이 리포지토리 클론

   ```bash
   git clone https://github.com/<YOUR_ID>/XPinBypass.git
   cd XPinBypass
   ```

2. Android Studio 로 열기
3. 필요하면 `applicationId` / `namespace` 등을 수정
4. Debug 빌드 후 기기/에뮬레이터에 설치

---

## Enable module in LSPosed

1. LSPosed Manager 실행
2. **Modules** 탭에서 `XPinBypass` 모듈 활성화
3. Scope 에서 **`com.damian.xpinbypass`** 체크
4. 기기/에뮬레이터 재부팅

---

## Usage

1. **모듈 비활성화 상태**에서:
    - 앱 실행 → `"핀닝 테스트 요청 보내기"` 버튼 클릭
    - `Pinned request failed` 및 `SSLPeerUnverifiedException: Certificate pinning failure!` 로그 출력

2. **모듈 활성화 + 재부팅 후**:
    - 다시 앱 실행 → 같은 버튼 클릭
    - `XPinBypass: bypassing pinning for host=example.com` 로그가 찍히고,
    - pinning failure 없이 요청이 통과 (또는 다른 네트워크 에러)

이 과정을 통해 **“핀닝이 어떻게 동작하는지”와 “Xposed 후킹으로 어떻게 우회되는지”** 를 한 눈에 볼 수 있습니다.

---

## Disclaimer

이 프로젝트는 **교육 및 연구 목적**으로만 제공됩니다.  
본 리포지토리의 코드를 이용하여 제3자의 서비스/앱에 대한 무단 후킹, 디컴파일, 인증 우회 등을 수행하는 것은 사용자의 책임이며, 저자는 그 어떤 법적/윤리적 책임도 지지 않습니다.
