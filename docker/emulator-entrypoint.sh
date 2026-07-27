#!/usr/bin/env bash
# Boots a headless Android emulator behind an X virtual framebuffer, shares that framebuffer
# over VNC, exposes it to a browser via noVNC on :6080, then installs and launches the app.
set -euo pipefail

APK_PATH="/apk/app-debug.apk"
PACKAGE_NAME="com.example.customerspendingdashboard"
AVD_NAME="spending_dashboard"

export DISPLAY=:1
Xvfb "$DISPLAY" -screen 0 1080x2280x24 &
sleep 2

x11vnc -display "$DISPLAY" -forever -shared -nopw -rfbport 5900 -bg -o /var/log/x11vnc.log
websockify --web=/usr/share/novnc/ 6080 localhost:5900 &

echo "Booting the Android emulator — this can take a few minutes, and considerably longer" \
     "without hardware acceleration (see README for the --device /dev/kvm flag on Linux hosts)."
emulator -avd "$AVD_NAME" \
    -no-audio \
    -no-boot-anim \
    -no-snapshot \
    -gpu swiftshader_indirect \
    -camera-back none \
    -memory 2048 &

adb wait-for-device
until [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; do
    sleep 2
done
echo "Emulator booted."

# The package manager can still be settling for a moment right after boot_completed flips.
for attempt in 1 2 3 4 5; do
    if adb install -r "$APK_PATH"; then
        break
    fi
    echo "adb install attempt $attempt failed, retrying..."
    sleep 5
done

adb shell am start -n "$PACKAGE_NAME/$PACKAGE_NAME.MainActivity"

echo ""
echo "Customer Spending Dashboard is running."
echo "Open http://localhost:6080/vnc.html in a browser and click Connect to view and interact with it."
echo ""

wait -n
