#!/usr/bin/env bash
# Pasang deploy otomatis di CT (sekali saja). Jalankan sebagai root dari dalam repo:
#   sudo server/deploy/install.sh            # ikut branch main
#   sudo server/deploy/install.sh feat/xyz   # ikut branch lain
set -euo pipefail

BRANCH="${1:-main}"
SCRIPT="$(readlink -f "$(dirname "$0")/deploy.sh")"
REPO_DIR="$(git -C "$(dirname "$SCRIPT")" rev-parse --show-toplevel)"

apt-get install -y -qq curl jq git >/dev/null
chmod +x "$SCRIPT"

# Pastikan repo berada di branch yang diikuti.
git -C "$REPO_DIR" fetch -q origin "$BRANCH"
git -C "$REPO_DIR" checkout -q -B "$BRANCH" "origin/$BRANCH"

cat >/etc/systemd/system/sempat-deploy.service <<EOF
[Unit]
Description=Deploy otomatis server SEMPAT
After=network-online.target docker.service
Wants=network-online.target

[Service]
Type=oneshot
Environment=DEPLOY_BRANCH=$BRANCH
ExecStart=$SCRIPT
EOF

cat >/etc/systemd/system/sempat-deploy.timer <<EOF
[Unit]
Description=Cek update server SEMPAT tiap 2 menit

[Timer]
OnBootSec=1min
OnUnitActiveSec=2min

[Install]
WantedBy=timers.target
EOF

systemctl daemon-reload
systemctl enable --now sempat-deploy.timer
echo "Terpasang. Lihat log: journalctl -u sempat-deploy -f"
