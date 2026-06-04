# run-tests.ps1 — навантажувальні тести для 1, 2, 4 реплік
# Використання: .\load-tests\run-tests.ps1
# Передумова: minikube запущений, система розгорнута, pip install locust

$ErrorActionPreference = "Stop"

$NS         = "printing-system"
$LOCAL_PORT = 8080
$HOST_URL   = "http://localhost:$LOCAL_PORT"
$USERS      = 50
$SPAWN_RATE = 5
$RUN_TIME   = "60s"
$RESULTS    = "$PSScriptRoot\results"

New-Item -ItemType Directory -Force -Path $RESULTS | Out-Null

# Піднімаємо port-forward у фоні (Windows Docker driver не дає прямого NodePort)
Write-Host "Starting port-forward svc/api-3d -> localhost:$LOCAL_PORT ..." -ForegroundColor Gray
$pfJob = Start-Job {
    kubectl port-forward svc/api-3d 8080:80 -n printing-system
}
Start-Sleep -Seconds 4

# Перевірка що API доступний
try {
    $null = Invoke-RestMethod "$HOST_URL/health" -TimeoutSec 5
    Write-Host "API reachable at $HOST_URL" -ForegroundColor Green
} catch {
    Stop-Job $pfJob; Remove-Job $pfJob
    Write-Error "Cannot reach $HOST_URL/health. Is the deployment running? (kubectl get pods -n $NS)"
}

function Run-Test {
    param([int]$Replicas, [string]$Label)

    Write-Host ""
    Write-Host "======================================" -ForegroundColor Cyan
    Write-Host "Testing with $Replicas replica(s)..." -ForegroundColor Cyan
    Write-Host "======================================" -ForegroundColor Cyan

    kubectl scale deployment/api-3d --replicas=$Replicas -n $NS
    kubectl rollout status deployment/api-3d -n $NS --timeout=90s

    Write-Host "Warming up (5s)..." -ForegroundColor Gray
    Start-Sleep -Seconds 5

    Write-Host "Running Locust ($Label, $USERS users, $RUN_TIME)..." -ForegroundColor Yellow

    python -m locust `
        --headless `
        -u $USERS `
        -r $SPAWN_RATE `
        --run-time $RUN_TIME `
        --html "$RESULTS\$Label.html" `
        --csv  "$RESULTS\$Label" `
        -H $HOST_URL `
        -f "$PSScriptRoot\locustfile.py"

    Write-Host "Saved: $RESULTS\$Label.html" -ForegroundColor Green
}

Run-Test -Replicas 1 -Label "1_pod"
Run-Test -Replicas 2 -Label "2_pods"
Run-Test -Replicas 4 -Label "4_pods"

# Повернути до 2 реплік
kubectl scale deployment/api-3d --replicas=2 -n $NS

# Зупинити port-forward
Stop-Job $pfJob; Remove-Job $pfJob
Write-Host "Port-forward stopped." -ForegroundColor Gray

Write-Host ""
Write-Host "All tests done. Results in $RESULTS\" -ForegroundColor Green
Write-Host "  1 pod  -> $RESULTS\1_pod.html"
Write-Host "  2 pods -> $RESULTS\2_pods.html"
Write-Host "  4 pods -> $RESULTS\4_pods.html"
