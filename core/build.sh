#!/bin/sh

boltffi pack all

mkdir -p module

rm -r module/src
cp -r dist/kmp/src/commonMain/kotlin/core module/src

rm -r module/src@jvm
cp -r dist/kmp/src/jvmMain/kotlin/core module/src@jvm

rm -r module/resources@jvm
cp -r dist/kmp/src/jvmMain/resources module/resources@jvm

rm -r module/src@android
cp -r dist/kmp/src/androidMain/kotlin/core module/src@android

rm module/module.yaml
cat > module/module.yaml <<EOL
product:
  type: kmp/lib
  platforms: [jvm]
EOL
