# Customer Spending Dashboard

A production-grade Android app showing a customer's spending insights — overview, spend trend, category breakdown, and a searchable/filterable transaction list — built on **mocked, locally-generated data** (no backend). The point of this repo is the engineering, not the data: Clean Architecture, full Gradle modularization, offline-first data flow, Hilt DI, Compose UI, and a real test pyramid.

## Screens

- **Overview** — total spend/income for a selectable time range, a spend trend line, and a category breakdown.
- **Transactions** — searchable (debounced), filterable by category and time range, tap through to detail.
- **Transaction detail** — merchant, amount, category, date, note.

## Architecture

Clean Architecture (Presentation → Domain → Data) across **10 Gradle modules**, each with a single, explicit reason to exist:

```
app
 ├─ feature:dashboard   ─┐
 ├─ feature:transactions ┤  Compose UI + ViewModel (MVVM), StateFlow<UiState>
 │                        │
 ├─ domain               ── UseCases + repository interfaces (pure Kotlin, no Android)
 │                        │
 ├─ data                 ── Repository impl, mock "remote" data source, Room ↔ domain mapping
 │   └─ core:database    ── Room only (entities/DAO), knows nothing about domain models
 │
 ├─ core:designsystem    ── Material3 theme + reusable Compose components
 ├─ core:testing         ── Fakes, fixtures, JUnit5 dispatcher extension (testImplementation only)
 ├─ core:model           ── Domain models (Transaction, Category, SpendingSummary, …)
 └─ core:common          ── DispatcherProvider, DataResult, Money, TimeRange
```

**Dependency direction is strictly unidirectional** — `feature:dashboard` and `feature:transactions` never depend on each other; `:app` is the sole composition root that wires their nav graphs together. `domain`, `core:model`, and `core:common` are plain `kotlin("jvm")` modules (not Android libraries), which is *why* their unit tests run in milliseconds with no emulator, no Robolectric.

### Data flow (offline-first, single source of truth)

```
MockTransactionDataSourceImpl  →  Room (source of truth)  →  UseCases  →  StateFlow<UiState>  →  Compose
   (seeded generator,             (all reads come from        (filtering,
    simulated network delay)       here, never straight        aggregation,
                                    from "network")             trend bucketing)
```

There's no backend, so `RemoteTransactionDataSource` is a seam, not a real network client: `MockTransactionDataSourceImpl` deterministically generates ~9 months of realistic ZAR transactions (seeded `Random`, so it's reproducible, not different every run) with a simulated delay, standing in for an API response. `TransactionRepositoryImpl.refresh()` pulls from it and upserts into Room; **every read in the app comes from Room**, never directly from the mock source — the same pattern a real offline-first app would use for a real API. Swapping in a real backend later is one new class behind `RemoteTransactionDataSource`; nothing above the data layer changes.

## Navigation

Single-Activity, Navigation Compose, one flat `NavHost` graph in `app`'s composition root (`app/src/main/kotlin/.../navigation/AppNavHost.kt`). Both feature modules expose their destinations as `NavGraphBuilder` extension functions (`dashboardScreen { }`, `transactionsGraph()`); `:app` is the only module that wires them together, which is what lets `feature:dashboard` and `feature:transactions` stay independent of each other.

```
                        ┌─────────────────────┐
              ┌────────▶│   Overview (start)  │◀────────┐
              │         │  route: "dashboard" │         │
   bottom-nav │         └──────────┬───────────┘         │ bottom-nav
   "Overview" │                    │ tap a category      │ "Transactions"
              │                    ▼                     │
              │         ┌─────────────────────────────┐  │
              └─────────│         Transactions         │──┘
                        │ route: "transactions?category │
                        │        ={category}"           │
                        └──────────────┬────────────────┘
                                       │ tap a row
                                       ▼
                        ┌─────────────────────────────┐
                        │      Transaction detail      │
                        │ route: "transactions/{id}"   │
                        └───────────────────────────────┘
```

- **Bottom nav bar** (`app`'s `AppBottomBar`) switches between the two top-level destinations, Overview and Transactions, and is hidden on the detail screen. Both tab taps go through `navigateToTopLevel()`, the standard `popUpTo(start) { saveState = true } + launchSingleTop + restoreState` pattern — each tab keeps its own scroll/filter state when you switch away and back.
- **Top bar** swaps between the "Spending Dashboard" title and a back-arrow "Transaction details" title via a `Crossfade` keyed on the current route, so it can't be transiently wrong mid-transition.
- **Cross-feature deep link**: tapping a category on the Overview screen calls `navigateToTransactions(category)`, which navigates to `feature:transactions`'s route with an optional `category` query arg — `Category` is the only thing that crosses the module boundary, passed as a nav arg rather than a shared ViewModel, so `feature:dashboard` still never depends on `feature:transactions`. `TransactionsViewModel` reads it once out of `SavedStateHandle` to seed the initial category filter. This entry point also pops up to the start destination (same as a bottom-nav tap) so there's always exactly one Transactions entry on the back stack, keeping "Overview" always a single tap away regardless of how you arrived at Transactions.
- **Transaction detail** is pushed as a normal (non-top-level) destination — a plain `navigate("transactions/$id")` — so the back arrow and system back button both just pop it off.

## Testing strategy

| Layer | Framework | Why |
|---|---|---|
| `domain` use cases | JUnit5 + Turbine | Pure Kotlin, no Android — milliseconds per test |
| `data` repository | JUnit5 + MockK + Turbine | DAO/remote source are interfaces; no Room instance needed |
| `core:database` DAO | **JUnit4** + Robolectric | Needs a real (in-memory) Room DB — Robolectric's `@RunWith` integration is most stable on JUnit4, so this layer deliberately stays there rather than mixing frameworks silently |
| `feature` ViewModels | JUnit5 + MockK + Turbine + `kotlinx-coroutines-test` | `core:testing`'s `MainDispatcherExtension` swaps `Dispatchers.Main` |
| Compose UI | **JUnit4** (androidTest) | AGP's instrumentation runner requires it |
| Static analysis | detekt + ktlint | Run in CI and in the Dockerfile build; a clean run is a merge gate |

Both JUnit4 and JUnit5 run through the same `useJUnitPlatform()` test task via the JUnit Vintage engine — no separate build step.

## Build and run

**Prerequisites**: JDK 17 and the Android SDK (`platforms;android-36`, `build-tools;36.0.0`) — Android Studio installs both for you.

### Option A — Android Studio (recommended for actually running the app)

1. Open the repo root in Android Studio and let Gradle sync (first sync downloads dependencies, takes a few minutes).
2. Pick the **`app`** run configuration in the toolbar dropdown.
3. Connect a device (USB debugging on) or start an emulator (Device Manager → create/launch a virtual device).
4. Click ▶ Run. The app installs and launches on the selected device/emulator.

### Option B — command line

```bash
./gradlew assembleDebug        # build the debug APK → app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug         # install it on a connected device/emulator
adb shell am start -n com.example.customerspendingdashboard/.MainActivity   # launch it
```

A device/emulator must already be connected and visible to `adb devices` before `installDebug`.

### Running tests and checks

```bash
./gradlew test                 # unit tests, every module (fast, no device needed)
./gradlew connectedAndroidTest # instrumented/Compose UI tests (needs a device/emulator)
./gradlew ktlintCheck detekt   # static analysis
./gradlew ktlintFormat         # auto-fix formatting
```

## Running the Dockerfile

The Dockerfile has two stages — **always pass `--target` explicitly**, since Docker defaults to the *last* stage (`runtime`, the heavy one) when it's omitted. `builder` is a **build-and-verify container**: it fails the build if ktlint, detekt, any unit test, or `assembleDebug` fails, so a successful build is a reproducible, works-on-any-machine confirmation that the project passes every check — **this is the one to run in every environment, including macOS/Windows.** `runtime` gives you an **actual running app** on Linux hosts with real hardware virtualization (see the important limitation below before relying on it): it boots a real Android emulator inside the container and streams its screen to a browser over noVNC, so `docker run` launches something you can click through interactively — no local Android SDK, emulator, or device required.

### Verify the build (`builder` — fast, this is the CI-equivalent check)

```bash
docker build --target builder -t customer-spending-dashboard .
```

Runs `ktlintCheck detekt test assembleDebug` inside the container — the same sequence CI runs. Takes a few minutes the first time (downloading the JDK base image, Android SDK components, and Gradle dependencies); later builds reuse Docker's layer cache. `docker build` exits non-zero and prints the failing task's output if any check failed; exit `0` means everything passed.

Extract the built APK if you want the artifact itself, not just the pass/fail signal:

```bash
docker create --name csd-extract customer-spending-dashboard
docker cp csd-extract:/workspace/app/build/outputs/apk/debug/app-debug.apk .
docker rm csd-extract
```

### Run the app (`runtime` — an interactive emulator in your browser)

```bash
docker build --target runtime -t customer-spending-dashboard-emulator .
docker run --rm -p 6080:6080 --device /dev/kvm customer-spending-dashboard-emulator
```

Wait for the container to log `Customer Spending Dashboard is running.`, then open **http://localhost:6080/vnc.html** in a browser and click **Connect** — the emulator's screen, with the app already installed and launched, appears right there in the page. Standard mouse clicks/drags on the streamed screen act as taps/swipes. Stop it with `Ctrl+C` (the `--rm` flag cleans up the container).

**⚠️ This only works on a Linux host with `/dev/kvm` available (bare metal, a cloud VM with nested virtualization enabled, or a Linux CI runner) — it does not work on Docker Desktop for macOS or Windows, on any host CPU, Intel or Apple Silicon.** This isn't a performance caveat, it's a hard requirement:

- Current Android emulator releases refuse to boot an x86_64 system image at all without KVM/HVF hardware acceleration (`ERROR: x86_64 emulation currently requires hardware acceleration!`) — there's no slow-but-working software fallback anymore. Docker Desktop's own Linux VM on macOS/Windows doesn't expose hardware virtualization into the containers running inside it, so this error is unavoidable there, regardless of `--device /dev/kvm` (which only exists as a real device node on Linux hosts in the first place).
- Switching to an ARM64 system image doesn't help on Apple Silicon either: Google doesn't publish a Linux ARM64 build of the emulator binary at all, so `sdkmanager` has nothing to install for that combination. (Apple Silicon support in Android Studio itself works by running a native *macOS* ARM64 emulator — a different thing entirely from what's possible inside a Linux container.)

**If you're on macOS/Windows (or any host without KVM), use the `builder` stage instead**: run it to verify the project passes every check, extract the APK, and install/run it on your own emulator or device — see the "Verify the build" section above. On a Linux host with KVM, `runtime` works as described and gives you the full interactive experience without needing your own Android SDK.

**First run is slow even with KVM.** This stage installs the emulator + a system image (multiple GB) and boots a full Android device from cold, which takes a couple of minutes even with hardware acceleration.

**Debugging a stuck container**: `docker logs <container>` shows exactly where it's stuck (Xvfb/VNC startup, emulator boot, or the `adb install`/launch step).

## CI

`.github/workflows/ci.yml` runs ktlint, detekt, the full unit test suite, and `assembleDebug` on every push/PR, uploading the APK and test reports as artifacts.

