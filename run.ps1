param([switch]$Test, [switch]$BuildOnly)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    New-Item -ItemType Directory -Force -Path 'out' | Out-Null
    $sourceFiles = @(Get-ChildItem -LiteralPath 'src' -Recurse -Filter '*.java' | ForEach-Object FullName)
    if ($Test) { $sourceFiles += @(Get-ChildItem -LiteralPath 'test' -Recurse -Filter '*.java' | ForEach-Object FullName) }
    & javac -encoding UTF-8 -d out @sourceFiles
    if ($LASTEXITCODE -ne 0) { throw 'Java compilation failed.' }
    if ($Test) { & java '-Djava.awt.headless=true' -cp out tidekeeper.PatternTests }
    elseif (-not $BuildOnly) { & java -cp out tidekeeper.Main }
    if ($LASTEXITCODE -ne 0) { throw 'Java execution failed.' }
} finally { Pop-Location }
