# CI/CD SEMPAT

```
push ke GitHub
   │
   ├─► GitHub Actions (CI)
   │     ├─ "Server": test server + build image Docker   (kalau folder server/ berubah)
   │     └─ "App":    unit test + build APK debug         (kalau kode app berubah)
   │
   └─► CT Proxmox (CD), tiap 2 menit:
         ada commit baru di main?  → CI hijau?  → git pull + docker compose up --build
                                                 → /health gagal? → rollback otomatis
```

CD-nya **ditarik dari CT**, bukan didorong dari GitHub. Jadi tidak perlu buka SSH ke internet,
tidak perlu simpan password/kunci di GitHub, dan tidak pakai self-hosted runner (berbahaya untuk repo public).

## Pasang di CT (sekali saja)

Syarat: langkah 1–4 di [server/README.md](../README.md) sudah jalan (Docker terpasang, `.env` terisi,
`docker compose up -d --build` sukses, `curl localhost:8080/health` menjawab `ok`).

```bash
cd ~/Sempat
sudo server/deploy/install.sh          # ikut branch main
```

Selesai. Mulai sekarang setiap merge/push ke `main` yang CI-nya hijau akan ter-deploy sendiri dalam ±2 menit
setelah CI selesai.

## Sehari-hari

```bash
journalctl -u sempat-deploy -f            # lihat log deploy
sudo systemctl start sempat-deploy        # paksa cek sekarang, tidak usah tunggu 2 menit
sudo systemctl stop sempat-deploy.timer   # matikan deploy otomatis sementara
sudo systemctl start sempat-deploy.timer  # nyalakan lagi
```

## Aturan main

- **Kerja di branch, merge ke `main` lewat Pull Request.** Yang di-deploy hanya `main`.
- Commit di `main` yang CI-nya merah **tidak** di-deploy. Perbaiki dengan commit baru.
- Kalau server baru gagal menjawab `/health`, CT otomatis kembali ke versi sebelumnya.
  Commit yang gagal itu tidak dicoba lagi; commit berikutnya akan dicoba seperti biasa.
- Jangan edit file di CT langsung (kecuali `server/.env`): deploy memakai `git reset --hard`,
  jadi perubahan lokal di CT akan hilang. `.env` aman karena di-ignore git.
- Hanya perubahan di folder `server/` yang memicu build ulang container.

## APK dari CI

Setiap run "App" menyimpan APK debug: buka tab **Actions** → pilih run → bagian **Artifacts**.

Supaya APK itu langsung mengarah ke server di CT (bukan ke laptop), isi sekali di GitHub:
**Settings → Secrets and variables → Actions → Variables → New repository variable**

- Name: `SEMPAT_API_URL`
- Value: `https://api.domainkamu.com/`

## Opsional: token GitHub

Pengecekan status CI memakai API GitHub tanpa login (batas 60 request/jam, cukup karena hanya dicek
saat ada commit baru). Kalau repo nanti dijadikan private, buat token read-only lalu tambahkan
`Environment=GITHUB_TOKEN=...` di `/etc/systemd/system/sempat-deploy.service`.
