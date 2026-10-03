# One Second

**Keep a little piece of today.**

One Second is a private daily-moment app for Android. Each day you keep one small piece of your life — a photo, a short video or a few words — and at the end of the month those moments become a story you can play back and share.

It is calm, private and human. No accounts, no feed, no followers, no ads, no tracking.

## What the app does today (version 1.0)

| Area | What works |
|---|---|
| Onboarding | Three-screen introduction, shown once |
| Today | Real date, a rotating daily prompt, today's moment, monthly count, and a quiet "days in a row" stat that only appears from 2 days |
| Capture | Photo (system camera), video up to 60 s (system camera), photo/video from the library (system photo picker), or a written note. Optional caption and mood |
| Daily rule | One moment per day. Saving again on the same day replaces it. Past days are locked (they can be deleted, never silently changed) |
| Memories | Real calendar per month with thumbnails, previous/next month, tappable days, and a list for the month |
| Moment page | Full photo, playable video or note; share or delete |
| Montages | One story per month that has moments. Play it as a full-screen story (tap right/left to move) or share a finished "month card" image |
| Reminder | Optional daily notification at a time you choose, skipped automatically if you already kept today; survives reboot |
| Privacy | All data is stored privately on the phone. In-app privacy page, per-moment delete, and "Delete all my data" |
| Two looks | **Light** = clean "vanilla" interface. **Dark** = glassmorphic interface (deep gradient, frosted translucent cards). Chosen in onboarding, changeable in Settings |
| Loading | Skeleton shimmer placeholders for images, video and the capture/save step. No spinners |
| App icon | Adaptive launcher icon + Play Store icon from the supplied icon pack |

Everything listed is functional. There are no placeholder buttons.

## Deliberate product decisions

- **Zero running cost.** Local-first means no backend, no hosting, no API bills and no credit card needed to build or test. GitHub Actions is free for this project size.
- **Accounts will use a free tier.** When accounts arrive, they use Firebase Authentication on the free Spark plan (no credit card), and backup stays optional.
- **No accounts in 1.0.** A fake sign-in is worse than none, and a real one needs a backend (see "Roadmap"). Local-first also makes the privacy story simple and honest.
- **No camera or storage permissions.** Capture uses the system camera app and the system photo picker, which is both safer and easier to get through Play review.
- **No cloud backup (`allowBackup=false`).** Moments are private to the device until a real, opt-in sync exists.
- **Montage = story player + shareable month card in 1.0.** Rendering a finished MP4 with music is the next big feature, not something to rush.

## Roadmap — where this is headed

### Next: Montage video (v1.1)
- Export the monthly story as a real MP4 (images and clips, transitions, captions).
- A small royalty-free music library (needs licensed tracks — see "What we need from you").
- Share the video directly.

### Then: Accounts and backup (v1.2)
- Optional sign-in (email with verification, Google) using a real provider such as Firebase Authentication.
- Optional encrypted cloud backup and restore, so a new phone keeps your memories.
- Account deletion flow (required by Google Play once accounts exist).
- The app keeps working fully without an account.

### After that: Refinement from tester feedback
- Fix confusing flows, tune spacing, typography and motion.
- Accessibility pass (TalkBack, font scaling, contrast).
- Performance pass on older phones.
- Remove what people don't use; add only what strengthens the daily-moment → monthly-story loop.

### Later, only if customers want it
- Yearly time capsule, longer or higher-quality montages, more styles and music, shared private circles, advanced export. Possible premium tier. Nothing is charged until there is real value.

## Product rules (kept from the original vision)

- One primary moment per day. Missing a day is allowed — no guilt mechanics.
- Streaks are secondary and subtle.
- Private by default. No public feed or follower counts.
- No decorative emojis, no developer/admin panels, no fake claims about connected services.
- Two looks: Light (vanilla) and Dark (glass), both rounded and restrained. All colours live in `ui/Theme.kt`.
- Free only: no paywall, no ads, no paid services. Everything runs on-device, so there is no server bill.

## Project layout

```
app/src/main/java/com/onesecond/app/
  MainActivity.kt, OneSecondApp.kt, AppViewModel.kt
  data/     Moment model, local store (JSON index + private files), settings
  media/    Photo/video processing, month-card image renderer
  notify/   Daily reminder scheduling and receivers
  ui/       Theme, components, and every screen
store/      Play Store icon and listing text
docs/       Release checklist and privacy policy draft
.github/workflows/android.yml   Cloud build
```

Stack: Kotlin, Jetpack Compose (Material 3), minSdk 26, targetSdk 35. No third-party libraries beyond AndroidX.

## Build in the cloud (no PC needed)

Push to `main` and GitHub Actions builds the app.

1. Repository → **Actions** → latest **Build Android** run.
2. Under **Artifacts**, download **OneSecond-APK**, unzip it, and install `app-debug.apk` on your phone (allow "install from unknown sources" when asked).
3. When signing secrets are added (see `docs/PLAY_STORE_RELEASE.md`), the same run also produces **OneSecond-Play-Bundle** (`.aab`) — the file you upload to Google Play.

## Push from Termux

```bash
git init
git branch -M main
git add .
git commit -m "One Second 1.0"
git remote add origin https://github.com/YOUR_USERNAME/OneSecond.git
git push -u origin main
```

Use a GitHub personal access token as the password.
