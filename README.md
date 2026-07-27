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

## Build, run, test

Requires JDK 17 and the Android SDK (`platforms;android-36`, `build-tools;36.0.0`).

```bash
./gradlew assembleDebug        # build the debug APK
./gradlew installDebug         # install on a connected device/emulator
./gradlew test                 # unit tests, every module
./gradlew connectedAndroidTest # instrumented/Compose UI tests (needs a device/emulator)
./gradlew ktlintCheck detekt   # static analysis
./gradlew ktlintFormat         # auto-fix formatting
```

Android Studio: open the root folder, let Gradle sync, run the `app` configuration.

## Docker

```bash
docker build -t customer-spending-dashboard .
```

This runs the **same** `ktlintCheck detekt test assembleDebug` sequence as CI, inside a container with JDK 17 + the Android SDK — the image build fails if any step fails, so a successful `docker build` is the correctness signal (there's no server to `docker run` afterwards; this is a native Android app, not a web service, so "runnable" here means "builds and verifies reproducibly in a clean room"). Extract the built APK with:

```bash
docker create --name csd-extract customer-spending-dashboard
docker cp csd-extract:/workspace/app/build/outputs/apk/debug/app-debug.apk .
docker rm csd-extract
```

## CI

`.github/workflows/ci.yml` runs ktlint, detekt, the full unit test suite, and `assembleDebug` on every push/PR, uploading the APK and test reports as artifacts.

