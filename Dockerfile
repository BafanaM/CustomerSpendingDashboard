# syntax=docker/dockerfile:1
#
# Builds and verifies the whole project the same way CI does: static analysis (ktlint,
# detekt), the full unit test suite across every module, then assembleDebug. The image build
# itself fails if any of those steps fail, so a successful `docker build` is the "it works"
# signal for this project — there is no server to run afterwards (see README for why).

FROM eclipse-temurin:17-jdk-jammy

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
