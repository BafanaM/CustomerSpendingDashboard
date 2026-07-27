# syntax=docker/dockerfile:1
#
# Two stages — always pass --target explicitly; Docker defaults to the LAST stage (runtime,
# the heavy one) when --target is omitted, which is not what you want for a quick verify build:
#   docker build --target builder -t customer-spending-dashboard .            (fast, CI-equivalent)
#   docker build --target runtime -t customer-spending-dashboard-emulator .   (heavy, interactive)
#
#   builder — builds and verifies the whole project the same way CI does: static analysis
#            (ktlint, detekt), the full unit test suite across every module, then assembleDebug.
#            The image build fails if any of those steps fail, so a successful build is the
#            "it works" signal for this project.
#   runtime — takes the APK the builder stage produced, boots a real Android emulator inside
#            the container behind a VNC/noVNC web viewer, and installs + launches the app on
#            it, so `docker run` gives reviewers an actual running, interactive app in a
#            browser tab. See README for full instructions on both.
#
# Both stages are pinned to --platform=linux/amd64: the Android SDK's Linux command-line tools
# (aapt2 inside build-tools especially) only ship x86_64 binaries, so on an arm64 host (e.g. an
# Apple Silicon Mac) letting Docker build natively for arm64 leaves those binaries unable to
# run at all. Pinning to amd64 makes every host build the exact same way (Apple Silicon does it
# via Docker Desktop's Rosetta-backed emulation). See the `runtime` stage comment further down
# for why an arm64 system image/emulator is not a usable alternative here.

FROM --platform=linux/amd64 eclipse-temurin:17-jdk-jammy AS builder

ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    GRADLE_OPTS="-Dorg.gradle.daemon=false" \
    LANG=en_US.UTF-8

ARG CMDLINE_TOOLS_VERSION=9862592
ARG PLATFORM_VERSION=36
ARG BUILD_TOOLS_VERSION=36.0.0

RUN apt-get update && \
    apt-get install -y --no-install-recommends unzip curl && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    curl -sSL "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip" -o /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

ENV PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}"

RUN yes | sdkmanager --licenses > /dev/null && \
    sdkmanager \
        "platform-tools" \
        "platforms;android-${PLATFORM_VERSION}" \
        "build-tools;${BUILD_TOOLS_VERSION}"

WORKDIR /workspace

# Copy just the files Gradle needs to resolve dependencies first, so this layer (the slow
# part — downloading every dependency) is cached across rebuilds that only touch source code.
COPY gradlew gradlew.bat gradle.properties settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY config ./config
COPY app/build.gradle.kts app/build.gradle.kts
COPY core/common/build.gradle.kts core/common/build.gradle.kts
COPY core/model/build.gradle.kts core/model/build.gradle.kts
COPY core/database/build.gradle.kts core/database/build.gradle.kts
COPY core/designsystem/build.gradle.kts core/designsystem/build.gradle.kts
COPY core/testing/build.gradle.kts core/testing/build.gradle.kts
COPY domain/build.gradle.kts domain/build.gradle.kts
COPY data/build.gradle.kts data/build.gradle.kts
COPY feature/dashboard/build.gradle.kts feature/dashboard/build.gradle.kts
COPY feature/transactions/build.gradle.kts feature/transactions/build.gradle.kts

RUN chmod +x gradlew && ./gradlew --version

# Now the actual source.
COPY . .

RUN ./gradlew ktlintCheck detekt test assembleDebug --no-daemon --console=plain

# The built, verified APK — extract it with:
#   docker create --name csd-extract <image> && \
#   docker cp csd-extract:/workspace/app/build/outputs/apk/debug/app-debug.apk . && \
#   docker rm csd-extract

# ---------------------------------------------------------------------------------------------
# runtime — an Android emulator you can actually see and use, streamed to a browser via noVNC.
# Build:   docker build --target runtime -t customer-spending-dashboard-emulator .
# Run:     docker run --rm -p 6080:6080 customer-spending-dashboard-emulator
# View:    open http://localhost:6080/vnc.html in a browser once it logs the "running" message
#
# This is also what plain `docker build .` (no --target) produces, since it's the last stage —
# be explicit with --target if that's not what you meant.
#
# This stage only ever runs an x86_64 system image, deliberately, even on arm64 hosts:
#   - x86_64 system images require KVM/HVF hardware acceleration just to boot at all (current
#     emulator releases hard-error with "x86_64 emulation currently requires hardware
#     acceleration!" otherwise) — so this only actually works on a Linux Docker host with
#     /dev/kvm available (see --device /dev/kvm below). It will NOT boot under Docker Desktop
#     on macOS or Windows, on any host CPU — there is no accelerated virtualization path from a
#     container into either OS's hypervisor.
#   - Switching to an arm64-v8a system image doesn't help on macOS either: Google does not
#     publish a Linux ARM64 build of the emulator binary at all (Apple Silicon support in
#     Android Studio itself runs a native *macOS* arm64 emulator, not a Linux one — irrelevant
#     inside a Linux container), so `sdkmanager "emulator"` simply has nothing to install on an
#     arm64 Linux host, accelerated or not.
# In short: this target is for Linux hosts with real KVM. Elsewhere, use the `builder` stage's
# extracted APK with your own emulator/device instead — see README.
# ---------------------------------------------------------------------------------------------
FROM --platform=linux/amd64 eclipse-temurin:17-jdk-jammy AS runtime

ENV ANDROID_HOME=/opt/android-sdk \
    ANDROID_SDK_ROOT=/opt/android-sdk \
    LANG=en_US.UTF-8

ARG CMDLINE_TOOLS_VERSION=9862592
ARG SYSTEM_IMAGE_API=34
ARG SYSTEM_IMAGE="system-images;android-${SYSTEM_IMAGE_API};google_apis;x86_64"

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        unzip curl \
        xvfb x11vnc novnc websockify \
        libgl1 libpulse0 \
        libnss3 libxcomposite1 libxcursor1 libxdamage1 libxi6 libxtst6 libxrandr2 \
        libasound2 libatk1.0-0 libatk-bridge2.0-0 libcups2 libdrm2 libgbm1 \
        libxkbcommon0 libpango-1.0-0 libcairo2 \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    curl -sSL "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_VERSION}_latest.zip" -o /tmp/cmdline-tools.zip && \
    unzip -q /tmp/cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm /tmp/cmdline-tools.zip

ENV PATH="${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/emulator:${PATH}"

RUN yes | sdkmanager --licenses > /dev/null && \
    sdkmanager \
        "platform-tools" \
        "emulator" \
        "platforms;android-${SYSTEM_IMAGE_API}" \
        "${SYSTEM_IMAGE}" && \
    echo "no" | avdmanager create avd \
        --name spending_dashboard \
        --package "${SYSTEM_IMAGE}" \
        --device "pixel_5"

WORKDIR /apk
COPY --from=builder /workspace/app/build/outputs/apk/debug/app-debug.apk /apk/app-debug.apk

COPY docker/emulator-entrypoint.sh /usr/local/bin/emulator-entrypoint.sh
RUN chmod +x /usr/local/bin/emulator-entrypoint.sh

EXPOSE 6080
ENTRYPOINT ["/usr/local/bin/emulator-entrypoint.sh"]
