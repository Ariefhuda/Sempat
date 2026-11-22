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
                                                    ├─ phpmyadmin    (lihat/ubah DB lewat browser)
                                                    └─ db            (MySQL, tertutup dari luar)
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
| `db` – MySQL 8.4 | database: `users`, `lokasi`, `kode_otp`, `donasi`, `pengajuan`, `notifikasi` | container |
| `api` – server Ktor (folder ini) | semua endpoint (lihat tabel Endpoint di bawah) | container |
| `phpmyadmin` | lihat & ubah isi database lewat browser | container |
| `cloudflared` | membuka `api` dan `phpmyadmin` ke internet lewat Cloudflare Tunnel | container |
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

Isi `DB_PASSWORD`, `DB_ROOT_PASSWORD`, `JWT_SECRET`, `NIK_SECRET` dengan string acak. Buat pakai:

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

   > Tunnel `sempat` yang sekarang dipakai dibuat lewat CLI, jadi hostname-nya diatur di
   > [`cloudflared/config.yml`](cloudflared/config.yml), bukan di dashboard. Token-nya:
   > `cloudflared tunnel token sempat` (dari laptop yang sudah `cloudflared login`).
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
docker compose exec db mysql -u sempat -p sempat            # buka database (isi DB_PASSWORD)
docker compose exec db sh -c 'mysqldump -u sempat -p"$MYSQL_PASSWORD" --no-tablespaces sempat' > backup-$(date +%F).sql   # backup
```

## Catatan keamanan

- `OTP_DEV_MODE=true` membuat kode OTP ikut dikirim ke app (terisi otomatis). Enak untuk tes,
  tapi **matikan (`false`) kalau dipakai orang sungguhan**, lalu sambungkan ke pengirim WhatsApp/SMS
  (lihat `TODO` di `AuthService.kirimOtp`).
- File `.env` berisi rahasia: jangan di-commit, jangan dibagikan di grup.
- NIK tidak disimpan utuh: hanya hash (untuk cek duplikat) dan versi tersamar `1271 •••• •••• 0001`.
- Password disimpan sebagai hash bcrypt. Foto KTP tidak pernah dikirim ke server.

## phpMyAdmin

Alamat: **https://db-sempat.ususbuntu.my.id** (lewat tunnel yang sama dengan API, aturannya di
[`cloudflared/config.yml`](cloudflared/config.yml)). Login pakai user `sempat` + `DB_PASSWORD` dari `.env`.
User `sempat` hanya bisa mengakses database `sempat`; jangan pakai `root` untuk kerja sehari-hari.

phpMyAdmin **wajib** ditutup dengan Cloudflare Access supaya halaman login-nya tidak bisa dibuka sembarang orang:

1. **Zero Trust → Access → Applications → Add an application → Self-hosted**
2. Application name: `phpMyAdmin SEMPAT`, Domain: `db-sempat.ususbuntu.my.id`
3. Policy: Action **Allow**, Include **Emails** → isi email anggota kelompok
4. Login method: **One-time PIN** (kode dikirim ke email, tidak perlu setup lain)

Tanpa internet / sebelum Access dipasang, buka lewat SSH tunnel dari laptop:

```bash
ssh -L 8081:localhost:8081 sempat-ct     # lalu buka http://localhost:8081
```

App Android **tidak** memakai phpMyAdmin dan tidak konek ke MySQL. App selalu lewat API.

## Endpoint

Route bertanda 🔒 wajib header `Authorization: Bearer <token>` (token dari `/auth/daftar` atau `/auth/masuk`).
Error selalu berbentuk `{ "pesan": "..." }`; `401` = belum/sesi habis, `403` = peran salah / bukan miliknya,
`404` = tidak ada, `409` = status tidak memungkinkan. Waktu memakai ISO-8601 (`2026-11-22T17:00:00+07:00`).

**Akun**

| Method | Path | Body | Hasil |
| --- | --- | --- | --- |
| GET | `/health` | – | `ok` |
| POST | `/auth/otp/kirim` | `{ "noHp" }` | kode dikirim; `409` kalau nomor sudah terdaftar, `429` kalau kirim ulang < 60 detik |
| POST | `/auth/otp/cek` | `{ "noHp", "kode" }` | `{ "tokenVerifikasi" }` (berlaku 15 menit) |
| POST | `/auth/daftar` | data KTP + lokasi + password + `tokenVerifikasi` | `{ "token", "user" }`; `409` kalau NIK sudah terdaftar |
| POST | `/auth/masuk` | `{ "noHp", "password" }` | `{ "token", "user" }`; `401` kalau salah |
| GET 🔒 | `/me` | – | profil + lokasi |
| PUT 🔒 | `/me/lokasi` | `{ "latitude", "longitude", "alamat", "patokan" }` | profil terbaru |

**Donasi** (D = hanya donatur, P = hanya penerima)

| Method | Path | Siapa | Body / query | Hasil |
| --- | --- | --- | --- | --- |
| POST 🔒 | `/donasi` | D | `{ nama, kategori, jumlah, beratGram, batasAmbil, catatan?, fotoUrl?, lokasi? }` | donasi baru (`TERSEDIA`); tanpa `lokasi` = lokasi profil |
| GET 🔒 | `/donasi` | semua | `?lat=&lng=&radiusKm=25` (opsional) | pangan `TERSEDIA` terdekat + `jarakKm` |
| GET 🔒 | `/donasi/saya` | D | `?status=TERSEDIA\|DIPROSES\|SELESAI\|DIBATALKAN` | donasi milik sendiri |
| GET 🔒 | `/donasi/saya/ringkasan` | D | – | `{ totalBeratGram, jumlahSelesai, jumlahAktif }` |
| GET 🔒 | `/donasi/{id}` | semua | – | detail donasi |
| POST 🔒 | `/donasi/{id}/batal` | D pemilik | – | donasi `DIBATALKAN`, pengajuan aktif ikut batal |
| GET 🔒 | `/donasi/{id}/pengajuan` | D pemilik | – | daftar pengajuan untuk donasi itu |
| POST 🔒 | `/donasi/{id}/pengajuan` | P | `{ catatan?, buktiUrl? }` | pengajuan `MENUNGGU` |

**Pengajuan**

| Method | Path | Siapa | Hasil |
| --- | --- | --- | --- |
| GET 🔒 | `/pengajuan/saya` | P | pengajuan milik sendiri + data donasinya |
| POST 🔒 | `/pengajuan/{id}/setujui` | D pemilik donasi | `DISETUJUI`, donasi `DIPROSES`, pengajuan lain otomatis `DITOLAK` |
| POST 🔒 | `/pengajuan/{id}/tolak` | D pemilik donasi | `DITOLAK` |
| POST 🔒 | `/pengajuan/{id}/diterima` | P pemilik | `DITERIMA`, donasi `SELESAI` |
| POST 🔒 | `/pengajuan/{id}/batal` | P pemilik | `DIBATALKAN`; kalau sudah disetujui, donasi kembali `TERSEDIA` |

`noHpKontak` di pengajuan (nomor pihak lain, untuk chat/telepon) baru terisi setelah `DISETUJUI`.

**Notifikasi** (dibuat otomatis saat ada pengajuan / disetujui / ditolak / diterima / dibatalkan)

| Method | Path | Hasil |
| --- | --- | --- |
| GET 🔒 | `/notifikasi` | 100 notifikasi terbaru |
| POST 🔒 | `/notifikasi/{id}/baca` | `204` |

Menjalankan test server: `./gradlew test` (pakai database H2 di memori, tidak butuh Docker).
