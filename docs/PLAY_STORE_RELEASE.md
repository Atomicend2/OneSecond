# Releasing One Second on Google Play

The code is built to be releasable. These are the remaining steps that only the app owner can do.

## 0. Free ways to share the app today
You do not need to pay anything to test or share One Second: every GitHub build produces an APK you can install directly, and you can attach it to a GitHub Release for testers to download. Google Play is the only step with a cost.

## 1. Google Play developer account
- One-time registration fee (currently US$25) at https://play.google.com/console.
- Personal accounts created recently must run a **closed test with at least 12 testers for 14 days** before production access. Plan for this — start recruiting testers early.

## 2. Create a signing key (once)
Do this on any computer or phone with Java (`keytool`). Keep the file and passwords safe — if you lose them you cannot update the app.

```bash
keytool -genkeypair -v -keystore release.jks -alias onesecond \
  -keyalg RSA -keysize 2048 -validity 10000
```

Play App Signing is recommended: Google holds the final signing key, and this key becomes your *upload key*.

## 3. Add the signing secrets to GitHub
Repository → Settings → Secrets and variables → Actions → New repository secret:

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | output of `base64 -w0 release.jks` |
| `KEYSTORE_PASSWORD` | the keystore password |
| `KEY_ALIAS` | `onesecond` (or the alias you chose) |
| `KEY_PASSWORD` | the key password |

Next push produces **OneSecond-Play-Bundle** (`app-release.aab`) in the run's Artifacts.

## 4. Decide the final application ID
The app currently uses `com.onesecond.app`. The application ID is permanent once published. If you own a domain or want a different ID, change `applicationId` in `app/build.gradle.kts` **before** the first upload.

## 5. Store listing
- Icon: `store/play-store-icon-512.png`
- Listing text: `store/STORE_LISTING.md`
- Phone screenshots (at least 2): install the debug APK, take screenshots of Today, Memories, a Montage and Capture.
- Feature graphic (1024×500): still needed.

## 6. Policies
- **Privacy policy URL is required.** Draft in `docs/PRIVACY_POLICY.md`. Host it on a public URL (for example GitHub Pages) and enter the URL in Play Console. Fill in the contact email.
- **Data safety form:** the app collects no data and shares no data. Answer accordingly.
- **Content rating questionnaire:** no user-generated content is shared with other users; no violence, etc.
- **Target audience:** 13+ / general audience (not designed for children).
- **Ads:** none.

## 7. Before you press publish
- Install the release build on at least one real phone and run through: onboarding, photo, video, library import, note, replace today, delete, calendar, story playback, month card share, reminder on/off.
- Check on both an older Android (8–10) and a recent one (13+) if possible.
