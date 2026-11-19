# Run from any directory; the project is the folder containing this script.
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & .\mvnw.cmd clean javadoc:javadoc
    if ($LASTEXITCODE -ne 0) {
        throw 'Javadoc generation failed. Fix the reported errors or warnings before publishing.'
    }
    $generated = Join-Path $PSScriptRoot 'target\reports\apidocs'
    foreach ($page in @('index.html', 'com\comp2042\HedgeLayouts.html', 'com\comp2042\SnakeRenderer.html')) {
        if (-not (Test-Path (Join-Path $generated $page))) {
            throw "Expected generated page is missing: $page"
        }
    }
    $destination = Join-Path $PSScriptRoot 'Javadoc'
    New-Item -ItemType Directory -Path $destination -Force | Out-Null
    Copy-Item (Join-Path $generated '*') -Destination $destination -Recurse -Force
    Write-Host 'Javadoc generated and copied to Javadoc\index.html.'
} finally {
    Pop-Location
}
