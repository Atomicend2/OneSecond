# One Second

**Keep a little piece of today.**

This is the customer-facing Android product build of One Second. It is intentionally not labeled with an internal version number in the app or customer-facing copy.

## Product vision

One Second lets a person capture one small piece of their life each day — a photo, a short video, a note, or a simple mood — and later turn those pieces into a beautiful personal time capsule.

The product should feel calm, premium, private and human. It should not feel like an AI dashboard, developer demo, social-media clone, or generic template.

## Core customer experience

1. First launch → simple onboarding.
2. Account → email signup/sign-in and Google sign-in entry.
3. Today → one clear daily prompt and one primary moment.
4. Capture → camera, gallery or note; optional mood and caption.
5. Memories → calendar/archive/timeline showing saved days and missed days without guilt.
6. Month → automatic monthly story/montage from the month's moments.
7. Share → save/share a finished monthly story.
8. Profile → privacy, notifications, account and basic settings.

## Product rules

- One primary moment per day. It can be replaced during the day, then the day becomes a memory.
- Missing a day is allowed. No guilt mechanics.
- Streaks are secondary and subtle.
- The core memory experience stays private by default.
- No public follower-count/social-feed experience in the core product.
- No random decorative emojis.
- No customer-facing developer/admin panels.
- No fake claims that a backend/auth provider is connected when it is not.
- The interface should remain polished, dark, rounded, glass-like and restrained, with cyan/violet accents rather than an orange/black AI aesthetic.

## Full product roadmap

### Product build / testing stage

The current source is the polished UI foundation. The remaining production integrations are deliberately documented below so the next implementation pass follows the same product logic instead of turning into unrelated features.

### Functional integration stage

- Real local persistence with Room/DataStore.
- Real media storage and media permissions.
- Short-video capture/import.
- Daily replacement/lock logic.
- Calendar with actual saved/missed dates.
- Timeline with real media thumbnails.
- Monthly montage generation.
- Royalty-safe music library.
- Share/export of montages.
- Real notifications with user controls.
- Real account creation and sign-in.
- Email verification.
- Google authentication.
- Cloud sync/backup as an optional account feature.

### Feedback/touch-up stage

After testers use the app, feedback is applied here:

- Fix confusing flows.
- Adjust spacing, typography, animations and navigation.
- Add requested customer-facing features that fit the product.
- Remove features people dislike or never use.
- Improve accessibility and performance.
- Fix crashes and edge cases.
- Refine onboarding and first-time-user experience.
- Refine montage quality and sharing.

This stage is intentionally a refinement pass, not a redesign of the whole product.

### Release preparation

- Production application ID/signing configuration.
- Privacy policy and terms.
- Data deletion/account deletion flow.
- Store listing screenshots and description.
- Content rating.
- Production authentication/backend configuration.
- Release build and Play Console testing tracks.
- Crash reporting and performance monitoring.
- Final privacy/security review.

## Monetization direction

The initial product should be free to test with no forced subscription and no ads in the core memory experience.

Possible later premium features:

- Longer/high-quality video montages.
- More montage styles/music.
- Yearly time capsules.
- Larger cloud backup.
- Shared private circles.
- Advanced export options.

Nothing is charged until there is a real customer value proposition.

## Architecture direction

- Android-first.
- Kotlin + Jetpack Compose.
- Material 3 with a custom visual system.
- Room/DataStore for local persistence.
- Media stored locally first.
- Backend/auth/cloud sync added only where it provides customer value.
- Montage generation should be efficient on real phones and avoid unnecessary server cost.

## Important authentication note

The current UI contains the customer-facing authentication flow, but real email verification and Google authentication require a real authentication service connection. The app must never pretend that a fake local sign-in is production authentication.

For the free testing stage, local/demo state can be used to test the rest of the product. Before public release, connect the real auth provider and verify the complete account lifecycle.

## Cloud APK build

This repository includes `.github/workflows/android.yml`.

It builds the debug APK on GitHub Actions, so the user's old PC does not need to compile the Android project.

After the workflow completes:

**GitHub → repository → Actions → Build Android APK → workflow run → Artifacts → OneSecond-APK**

Download the artifact ZIP, extract it, and install the APK on the Android test phone.

## Termux Git setup

From the extracted project directory on a device with Git installed:

```bash
git init
git branch -M main
git add .
git commit -m "Build One Second product"
git remote add origin https://github.com/YOUR_USERNAME/one-second.git
git push -u origin main
```

If GitHub asks for authentication, use a GitHub personal access token rather than your account password.

## What should never be added just because it is technically possible

- Developer dashboards exposed to customers.
- Fake AI features with no useful purpose.
- Public social feed before the private-memory experience is excellent.
- Heavy gamification that makes users feel bad for missing days.
- Paid infrastructure that is not justified by customers.
- Random features that do not strengthen the daily-memory → monthly-story loop.
