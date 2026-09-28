# Card Box POS - معلومات التوقيع الرسمي (Release Keystore & CI/CD Secrets)

تم إعداد وتجهيز ملف التوقيع الرقمي الرسمي الكامل لتطبيق **Card Box POS** بنجاح.

---

## 🔑 1. بيانات مفتاح التوقيع (Keystore Credentials)

| الحقل (Property) | القيمة (Value) |
| :--- | :--- |
| **اسم الملف (Keystore File)** | `cardbox-release-key.jks` |
| **الملف المشفر (Base64 File)** | `cardbox-release-key.base64` |
| **اسم الاسم المستعار (Key Alias)** | `cardbox-pos-key` |
| **كلمة سر المفتاح (Key Password)** | `CardBoxPOS@2026` |
| **كلمة سر المخزن (Store Password)** | `CardBoxPOS@2026` |
| **صلاحية المفتاح (Validity)** | حتى عام **2053** (صالح لأكثر من 27 سنة) |
| **معمارية التشفير (Algorithm)** | RSA 2048-bit (SHA384withRSA) |

---

## 🛡️ 2. بصمات الشهادة الرقمية (Certificate Fingerprints)

> تحتاج هذه البصمات عند ربط Google Play Console أو خدمات Google Cloud / Firebase:

- **SHA-256 Fingerprint:**
  ```text
  5A:00:6F:61:03:C0:BE:1C:70:D9:B9:63:83:EF:01:19:6F:DB:58:5E:0C:FD:A8:CF:48:A7:01:4A:05:B2:8B:3B
  ```

- **SHA-1 Fingerprint:**
  ```text
  D1:F0:EE:1B:57:1C:98:72:BE:2C:9B:98:9E:11:25:69:1E:C6:4B:4A
  ```

---

## 🚀 3. المتغيرات السرية في GitHub Actions (Repository Secrets)

في مستودع GitHub الخاص بك، اذهب إلى:
**Settings** ➔ **Secrets and variables** ➔ **Actions** ➔ **New repository secret**

أضف الأسرار التالية:

| اسم السر (Secret Name) | القيمة السرية (Secret Value) |
| :--- | :--- |
| `RELEASE_KEYSTORE_BASE64` | محتوى ملف `cardbox-release-key.base64` كاملاً |
| `KEY_ALIAS` | `cardbox-pos-key` |
| `KEY_PASSWORD` | `CardBoxPOS@2026` |
| `STORE_PASSWORD` | `CardBoxPOS@2026` |

---

## 📦 4. نموذج خطوة البناء في GitHub Actions Workflow (`.github/workflows/build.yml`)

```yaml
name: Build Signed Release APK & AAB

on:
  push:
    branches: [ main, master ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'zulu'
          java-version: '17'

      - name: Decode Release Keystore
        run: |
          echo "${{ secrets.RELEASE_KEYSTORE_BASE64 }}" | base64 -d > cardbox-release-key.jks

      - name: Build Release APK and Bundle (AAB)
        env:
          KEYSTORE_PATH: ${{ github.workspace }}/cardbox-release-key.jks
          KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
          STORE_PASSWORD: ${{ secrets.STORE_PASSWORD }}
        run: |
          chmod +x gradlew
          ./gradlew assembleRelease bundleRelease

      - name: Upload Release APK
        uses: actions/upload-artifact@v4
        with:
          name: CardBox-POS-Release-APK
          path: app/build/outputs/apk/release/*.apk

      - name: Upload Release AAB (Google Play Bundle)
        uses: actions/upload-artifact@v4
        with:
          name: CardBox-POS-Release-AAB
          path: app/build/outputs/bundle/release/*.aab
```
