param([switch]$Run, [switch]$SkipTests)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
Push-Location $PSScriptRoot
try {
    foreach ($tool in @('java', 'javac', 'jar')) { $null = Get-Command $tool -ErrorAction Stop }
    $projectRoot = [IO.Path]::GetFullPath($PSScriptRoot)
    $targetRoot = [IO.Path]::GetFullPath((Join-Path $projectRoot 'target'))
    if ((Split-Path -Parent $targetRoot) -ne $projectRoot) { throw 'Invalid output directory' }
    if ((Test-Path -LiteralPath $targetRoot) -and
        ((Get-Item -LiteralPath $targetRoot).Attributes -band [IO.FileAttributes]::ReparsePoint)) {
        throw 'Output directory must not be a symbolic link'
    }
    foreach ($relative in @('target/classes', 'target/test-classes')) {
        $output = [IO.Path]::GetFullPath((Join-Path $projectRoot $relative))
        if ((Split-Path -Parent $output) -ne $targetRoot) { throw 'Invalid build path' }
        if (Test-Path -LiteralPath $output) {
            if ((Get-Item -LiteralPath $output).Attributes -band [IO.FileAttributes]::ReparsePoint) {
                throw 'Build directory must not be a symbolic link'
            }
            Remove-Item -LiteralPath $output -Recurse -Force
        }
        $null = New-Item -ItemType Directory -Path $output -Force
    }
    $utf8 = New-Object System.Text.UTF8Encoding($false)
    $mainSources = @(Get-ChildItem -LiteralPath 'src/main/java' -Recurse -Filter '*.java' |
        ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
    [IO.File]::WriteAllLines((Join-Path $targetRoot 'main-sources.txt'), $mainSources, $utf8)
    & javac --release 17 -encoding UTF-8 -d target/classes '@target/main-sources.txt'
    if ($LASTEXITCODE -ne 0) { throw 'Main compilation failed' }
    if (-not $SkipTests) {
        $null = New-Item -ItemType Directory -Path '.deps', 'target/test-reports' -Force
        $junit = '.deps/junit-platform-console-standalone-1.11.4.jar'
        $expectedHash = 'B016EF6B1C3454D6D7C2C88CE081DABF289699686AF6622D6E4E2E1B54B4A2FC'
        if (-not (Test-Path -LiteralPath $junit)) {
            Invoke-WebRequest -UseBasicParsing -Uri 'https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-console-standalone/1.11.4/junit-platform-console-standalone-1.11.4.jar' -OutFile $junit
        }
        if ((Get-FileHash -LiteralPath $junit -Algorithm SHA256).Hash -ne $expectedHash) {
            throw 'JUnit checksum mismatch. Remove the cached JAR and retry.'
        }
        $testSources = @(Get-ChildItem -LiteralPath 'src/test/java' -Recurse -Filter '*.java' |
            ForEach-Object { '"' + $_.FullName.Replace('\', '/') + '"' })
        [IO.File]::WriteAllLines((Join-Path $targetRoot 'test-sources.txt'), $testSources, $utf8)
        & javac --release 17 -encoding UTF-8 -cp "target/classes;$junit" -d target/test-classes '@target/test-sources.txt'
        if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed' }
        & java -jar $junit execute --class-path 'target/classes;target/test-classes' --scan-class-path --fail-if-no-tests --disable-ansi-colors --details=tree --reports-dir target/test-reports
        if ($LASTEXITCODE -ne 0) { throw 'Tests failed' }
    }
    & jar --create --file target/smart-delivery.jar --main-class kz.delivery.Main -C target/classes .
    if ($LASTEXITCODE -ne 0) { throw 'JAR packaging failed' }
    Write-Host 'Built target/smart-delivery.jar (Java 17 bytecode)'
    if ($Run) {
        & java -jar target/smart-delivery.jar
        if ($LASTEXITCODE -ne 0) { throw 'Demo failed' }
    }
} finally { Pop-Location }
