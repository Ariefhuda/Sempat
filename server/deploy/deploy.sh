#!/usr/bin/env bash
# Deploy otomatis server SEMPAT. Jalan di CT Proxmox lewat systemd timer (lihat install.sh).
#
# Alurnya tiap kali jalan:
#   1. git fetch branch DEPLOY_BRANCH (default: main)
#   2. kalau ada commit baru, cek hasil GitHub Actions untuk commit itu
#        - masih jalan  -> tunggu, coba lagi di putaran berikutnya
#        - ada yang gagal -> tidak di-deploy
#        - semua hijau   -> lanjut
#   3. pindah ke commit baru; kalau folder server/ berubah, build ulang container
#   4. cek /health; kalau gagal, balik ke commit sebelumnya (rollback)
#
# Bisa juga dijalankan manual: sudo server/deploy/deploy.sh
set -euo pipefail

BRANCH="${DEPLOY_BRANCH:-main}"
STATE_DIR="${STATE_DIR:-/var/lib/sempat-deploy}"
HEALTH_URL="${HEALTH_URL:-http://localhost:8080/health}"

REPO_DIR="$(git -C "$(dirname "$(readlink -f "$0")")" rev-parse --show-toplevel)"
SERVER_DIR="$REPO_DIR/server"
mkdir -p "$STATE_DIR"

log() { echo "[deploy] $*"; }

# owner/repo dari URL remote, mis. https://github.com/Ariefhuda/Sempat.git -> Ariefhuda/Sempat
github_repo() {
    git -C "$REPO_DIR" remote get-url origin \
        | sed -E 's#^(https://github\.com/|git@github\.com:)##; s#\.git$##'
}

# Hasil: success | pending | failure
ci_status() {
    local sha="$1" server_changed="$2" auth=() json
    [ -n "${GITHUB_TOKEN:-}" ] && auth=(-H "Authorization: Bearer $GITHUB_TOKEN")
    if ! json="$(curl -fsS "${auth[@]}" -H "Accept: application/vnd.github+json" \
        "https://api.github.com/repos/$(github_repo)/actions/runs?head_sha=$sha&per_page=50")"; then
        echo pending   # GitHub tidak bisa dihubungi: coba lagi nanti
        return
    fi
    jq -r --argjson butuhServer "$server_changed" '
        .workflow_runs as $runs
        | if any($runs[]; .status != "completed") then "pending"
          elif any($runs[]; (.conclusion | IN("success", "skipped")) | not) then "failure"
          elif $butuhServer and (any($runs[]; .name == "Server") | not) then "pending"
          else "success" end' <<<"$json"
}

compose() {
    local profile=()
    # Tunnel ikut dinyalakan hanya kalau token Cloudflare sudah diisi di .env
    grep -Eq '^CLOUDFLARE_TUNNEL_TOKEN=.+' "$SERVER_DIR/.env" && profile=(--profile tunnel)
    docker compose --project-directory "$SERVER_DIR" "${profile[@]}" "$@"
}

healthy() {
    for _ in $(seq 1 60); do
        curl -fsS "$HEALTH_URL" >/dev/null 2>&1 && return 0
        sleep 2
    done
    return 1
}

main() {
    [ -f "$SERVER_DIR/.env" ] || { log "server/.env belum ada, isi dulu (lihat server/README.md)"; exit 1; }

    git -C "$REPO_DIR" fetch -q origin "$BRANCH"
    local lama baru
    lama="$(git -C "$REPO_DIR" rev-parse HEAD)"
    baru="$(git -C "$REPO_DIR" rev-parse "origin/$BRANCH")"
    [ "$lama" = "$baru" ] && exit 0

    # Commit yang sudah pernah gagal tidak dicoba terus-menerus.
    if [ "$(cat "$STATE_DIR/gagal" 2>/dev/null)" = "$baru" ]; then exit 0; fi

    local server_changed=false
    git -C "$REPO_DIR" diff --quiet "$lama" "$baru" -- server/ || server_changed=true

    case "$(ci_status "$baru" "$server_changed")" in
        pending) log "CI untuk ${baru:0:7} belum selesai, tunggu"; exit 0 ;;
        failure) log "CI untuk ${baru:0:7} gagal, tidak di-deploy"; echo "$baru" >"$STATE_DIR/gagal"; exit 0 ;;
        success) ;;
        *) log "status CI tidak terbaca, coba lagi nanti"; exit 0 ;;
    esac

    log "update ${lama:0:7} -> ${baru:0:7}"
    # .env aman: file itu di-ignore git, jadi tidak tersentuh reset.
    git -C "$REPO_DIR" reset -q --hard "$baru"

    if [ "$server_changed" = false ]; then
        log "folder server/ tidak berubah, tidak perlu build ulang"
        exit 0
    fi

    compose up -d --build --remove-orphans
    if healthy; then
        log "berhasil, server jalan di ${baru:0:7}"
        docker image prune -f >/dev/null
        return
    fi

    log "health check GAGAL, rollback ke ${lama:0:7}"
    compose logs --tail 50 api || true
    echo "$baru" >"$STATE_DIR/gagal"
    git -C "$REPO_DIR" reset -q --hard "$lama"
    compose up -d --build --remove-orphans
    healthy && log "rollback berhasil" || log "rollback juga gagal, cek manual: docker compose logs api"
    exit 1
}

main "$@"
