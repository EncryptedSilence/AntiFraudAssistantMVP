<#
    Stage 9 on-device alert-latency verification.

    Usage:
        .\scripts\verify-alert-latency.ps1 -AdbPath "C:\AndroidDev\platform-tools\adb.exe"

    The script issues a synthetic broadcast that triggers a critical alert in a debug
    build of the app, then tails logcat for the timestamped "ALERT_POSTED" tag and
    reports the wall-clock delta.

    Pre-conditions:
      - the device is attached and unlocked,
      - the app is installed (debug),
      - the user granted notifications, full-screen intent (Android 14+), and overlay.

    Limitation: the script only validates that the LATENCY claim is plausible. Per
    spec §4.4.2 the canonical verification is a real incoming-call test, which a
    headless script cannot reproduce. The dev runs the call test manually and
    cross-checks the script's numbers.
#>

param(
    [string]$AdbPath = "C:\AndroidDev\platform-tools\adb.exe",
    [string]$Package = "com.qalqan.antifraud"
)

if (-not (Test-Path $AdbPath)) {
    Write-Error "adb not found at $AdbPath"
    exit 1
}

& $AdbPath shell am broadcast -a com.qalqan.antifraud.alerts.SYNTH_CRITICAL `
    --es campaign_id "synth-$(Get-Random)" `
    -p $Package

Start-Sleep -Milliseconds 500

$lines = & $AdbPath logcat -d -t 50 AntiFraud:V `*:S
$lines | Select-String -Pattern "ALERT_POSTED" | ForEach-Object { Write-Output $_.Line }
