#!/usr/bin/env sh
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
for tool in java javac jar; do command -v "$tool" >/dev/null; done
# Fixed paths inside this project; never follow a linked output directory.
for dir in target target/classes target/test-classes; do
  if [ -L "$dir" ]; then echo "Refusing symbolic link: $dir" >&2; exit 1; fi
done
rm -rf -- target/classes target/test-classes
mkdir -p target/classes target/test-classes target/test-reports .deps
find src/main/java -name '*.java' | sort > target/main-sources.txt
javac --release 17 -encoding UTF-8 -d target/classes @target/main-sources.txt
junit=.deps/junit-platform-console-standalone-1.11.4.jar
if [ ! -f "$junit" ]; then
  curl -fL --retry 3 -o "$junit" https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar
fi
expected=b016ef6b1c3454d6d7c2c88ce081dabf289699686af6622d6e4e2e1b54b4a2fc
if command -v sha256sum >/dev/null 2>&1; then
  actual=$(sha256sum "$junit" | cut -d ' ' -f 1)
else
  actual=$(shasum -a 256 "$junit" | cut -d ' ' -f 1)
fi
[ "$actual" = "$expected" ] || { echo 'JUnit checksum mismatch' >&2; exit 1; }
find src/test/java -name '*.java' | sort > target/test-sources.txt
javac --release 17 -encoding UTF-8 -cp "target/classes:$junit" -d target/test-classes @target/test-sources.txt
java -jar "$junit" execute --class-path target/classes:target/test-classes --scan-class-path --fail-if-no-tests --disable-ansi-colors --details=tree --reports-dir target/test-reports
jar --create --file target/smart-delivery.jar --main-class kz.delivery.Main -C target/classes .
echo 'Built target/smart-delivery.jar (Java 17 bytecode)'
if [ "${1:-}" = '--run' ]; then java -jar target/smart-delivery.jar; fi
