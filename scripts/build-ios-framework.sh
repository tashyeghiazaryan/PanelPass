#!/bin/bash
# Builds the shared framework for iOS. Run from project root.
set -e
cd "$(dirname "$0")/.."
./gradlew :shared:linkReleaseFrameworkIosArm64 :shared:linkReleaseFrameworkIosSimulatorArm64
echo "Frameworks built. Add shared/build/bin/iosArm64/releaseFramework/shared.framework to your Xcode project."
