# Server API SEMPAT

Server ini yang menyimpan data akun (register) ke database. App Android **tidak** konek
langsung ke database. Alurnya:

```
HP (app SEMPAT)
   │  https://api.domainkamu.com
   ▼
Cloudflare  ──(Cloudflare Tunnel, terenkripsi)──►  container Proxmox
                                                    ├─ cloudflared   (ujung tunnel)
                                                    ├─ api           (Ktor, port 8080)
                                                    └─ db            (PostgreSQL, tertutup dari luar)
```

Kenapa begini:
- **Password database tidak ikut di APK.** APK bisa dibongkar siapa saja; kalau app konek langsung ke
  database, password-nya ketahuan.
- **Tidak perlu buka port di router / IP publik.** `cloudflared` yang "menelepon keluar" ke Cloudflare,
  jadi server di rumah/kampus tetap bisa diakses dari mana saja.
- **HTTPS otomatis** dari Cloudflare.

## Service yang dibutuhkan

| Service | Fungsinya | Jalan di mana |
| --- | --- | --- |
| Proxmox LXC (Debian) | "komputer" kecil tempat semua jalan | Proxmox |
| Docker + Docker Compose | menjalankan 3 container di bawah dengan satu perintah | di dalam LXC |
| `db` – PostgreSQL 17 | database: tabel `users`, `lokasi`, `kode_otp` | container |
| `api` – server Ktor (folder ini) | endpoint `/auth/otp/kirim`, `/auth/otp/cek`, `/auth/daftar` | container |
| `cloudflared` | membuka `api` ke internet lewat Cloudflare Tunnel | container |
| Akun Cloudflare + 1 domain | alamat publik, mis. `api.sempat.my.id` | cloudflare.com (gratis) |

## Langkah 1 – Buat container (LXC) di Proxmox

1. Di Proxmox: **local → CT Templates → Templates**, download `debian-12-standard` (atau 13).
2. **Create CT**:
   - Hostname: `sempat-server`
   - Template: debian yang tadi
   - Disk: 16 GB, CPU: 2 core, Memory: 2048 MB
   - Network: DHCP (atau IP statis kalau mau)
   - Biarkan **Unprivileged container** tercentang
3. Sebelum dinyalakan: pilih container → **Options → Features** → centang **nesting** dan **keyctl**.
   (Docker butuh dua ini kalau jalan di dalam LXC.)
4. Start, lalu buka **Console** dan login sebagai root.

## Langkah 2 – Pasang Docker

```bash
apt update && apt install -y curl git
curl -fsSL https://get.docker.com | sh
docker compose version   # cek: harus muncul versi
```

## Langkah 3 – Ambil kode dan isi rahasia

```bash
git clone https://github.com/Ariefhuda/Sempat.git
cd Sempat/server
cp .env.example .env
nano .env
```

Isi `DB_PASSWORD`, `JWT_SECRET`, `NIK_SECRET` dengan string acak. Buat pakai:

```bash
openssl rand -hex 32
```

> `NIK_SECRET` jangan diganti setelah ada user terdaftar: NIK disimpan dalam bentuk hash,
> kalau kuncinya berubah, pengecekan "NIK sudah terdaftar" jadi tidak cocok lagi.

## Langkah 4 – Jalankan (tanpa Cloudflare dulu)

```bash
docker compose up -d --build
curl http://localhost:8080/health      # harus menjawab: ok
docker compose logs -f api             # lihat log; kode OTP muncul di sini
```

Sampai sini server sudah bisa dicoba dari HP/laptop **di jaringan yang sama**:
`http://IP-container:8080/` (IP lihat dengan `ip a`).

## Langkah 5 – Cloudflare Tunnel (supaya bisa diakses dari mana saja)

1. Daftar di cloudflare.com, **Add a domain**, ikuti petunjuk ganti nameserver di tempat beli domain.
   (Domain `.my.id` murah dan cukup untuk tugas.)
2. Buka **Zero Trust → Networks → Tunnels → Create a tunnel → Cloudflared**. Beri nama `sempat`.
3. Di langkah "Install connector", pilih **Docker**, salin **token**-nya saja (teks panjang setelah `--token`).
   Tempel ke `.env`: `CLOUDFLARE_TUNNEL_TOKEN=...`
4. Di tab **Public Hostname** tambahkan:
   - Subdomain: `api`, Domain: domainmu
   - Service: **HTTP** → `api:8080`
     (`api` = nama service di docker-compose, karena cloudflared ada di jaringan Docker yang sama)
5. Jalankan dengan profil tunnel:

```bash
docker compose --profile tunnel up -d
curl https://api.domainkamu.com/health   # dari mana saja, harus: ok
```

Belum punya domain? Untuk coba-coba cepat bisa pakai tunnel sementara (alamatnya acak dan berubah
tiap restart):

```bash
docker run --rm --network server_default cloudflare/cloudflared:latest tunnel --url http://api:8080
```

## Langkah 6 – Arahkan app ke server

Di laptop yang build app, tambahkan di `local.properties` (file ini tidak ikut di-commit):

```
sempat.apiUrl=https://api.domainkamu.com/
```

Lalu build ulang app. Tanpa baris ini app memakai `http://10.0.2.2:8080/`
(= server di laptop sendiri, kalau app dijalankan di emulator).

## Deploy otomatis (CI/CD)

Setelah server jalan, pasang deploy otomatis supaya setiap merge ke `main` langsung ter-update di CT:
lihat [deploy/README.md](deploy/README.md).

## Sehari-hari

```bash
git pull && docker compose --profile tunnel up -d --build   # update server
docker compose logs -f api                                  # lihat log
docker compose exec db psql -U sempat -d sempat             # buka database
docker compose exec db pg_dump -U sempat sempat > backup-$(date +%F).sql   # backup
```

## Catatan keamanan

- `OTP_DEV_MODE=true` membuat kode OTP ikut dikirim ke app (terisi otomatis). Enak untuk tes,
  tapi **matikan (`false`) kalau dipakai orang sungguhan**, lalu sambungkan ke pengirim WhatsApp/SMS
  (lihat `TODO` di `AuthService.kirimOtp`).
- File `.env` berisi rahasia: jangan di-commit, jangan dibagikan di grup.
- NIK tidak disimpan utuh: hanya hash (untuk cek duplikat) dan versi tersamar `1271 •••• •••• 0001`.
- Password disimpan sebagai hash bcrypt. Foto KTP tidak pernah dikirim ke server.

## Endpoint

| Method | Path | Body | Hasil |
| --- | --- | --- | --- |
| GET | `/health` | – | `ok` |
| POST | `/auth/otp/kirim` | `{ "noHp" }` | kode dikirim; `409` kalau nomor sudah terdaftar, `429` kalau kirim ulang < 60 detik |
| POST | `/auth/otp/cek` | `{ "noHp", "kode" }` | `{ "tokenVerifikasi" }` (berlaku 15 menit) |
| POST | `/auth/daftar` | data KTP + lokasi + password + `tokenVerifikasi` | `{ "token", "user" }`; `409` kalau NIK sudah terdaftar |

Menjalankan test server: `./gradlew test` (pakai database H2 di memori, tidak butuh Docker).
