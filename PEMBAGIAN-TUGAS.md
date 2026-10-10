# Pembagian tugas

Sempat dikerjakan lima orang. Pembagiannya per fitur: tiap orang memegang layar dan logika fiturnya sendiri, dari desain sampai jalan di HP. Nama kelompok di bawah sama dengan nama section di halaman HighFidelity pada file Figma, jadi layar yang harus dibuat tinggal dilihat di section masing-masing.

## Ringkasan

| # | Fitur | Layar | Pop-up | Bagian yang paling berat |
| --- | --- | --- | --- | --- |
| 1 | Akun & KTP | 17 | 8 | Membaca KTP dari foto |
| 2 | Donasi & kenali makanan | 10 | 4 | Mengenali makanan dari foto |
| 3 | Kelola donasi (donatur) | 14 | 5 | Status donasi dan chat |
| 4 | Cari & minta bantuan (penerima) | 14 | 5 | Daftar pangan terdekat dan pengajuan |
| 5 | Bantuan saya (penerima) | 16 | 6 | Status bantuan sampai diterima |

Kelompok 2 paling sedikit layarnya, tetapi memegang model pengenal makanan. Kelompok 1 paling banyak layarnya, tetapi sebagian besar pendek (splash, perkenalan, pop-up). Tiga kelompok lain hampir sama besar.

## Rincian per orang

### 1. Akun & KTP

Layar: splash, tiga layar perkenalan, pilih peran, foto KTP, hasil baca KTP, lokasi donatur, lokasi penerima, masuk, kode OTP.

Yang dibuat:

- Kamera untuk memotret KTP.
- Pembaca teks di HP, lalu pemilahan NIK, nama, tanggal lahir, dan kota dari hasilnya.
- Layar hasil yang bisa diperbaiki pengguna, ditambah kolom nomor HP.
- Pilih lokasi di peta.
- Masuk dengan nomor HP dan kode OTP, lalu menyimpan sesi dan peran.

Selesai kalau: orang baru bisa mendaftar dengan memotret KTP, mengisi nomor HP dan lokasi, lalu sampai di beranda yang sesuai perannya.

### 2. Donasi & kenali makanan

Layar: beranda donatur, notifikasi donatur, ambil foto atau pilih dari galeri, hasil kenali makanan, form donasi, pilih penerima.

Yang dibuat:

- Model klasifikasi gambar yang berjalan di HP, beserta daftar jenis makanannya.
- Form donasi yang terisi dari hasil model dan tetap bisa diubah.
- Menyimpan donasi baru.
- Ringkasan di beranda (jumlah yang sudah didonasikan dan diambil).

Selesai kalau: donatur memotret makanan, nama dan jenisnya terisi, lalu donasi itu muncul di daftar.

### 3. Kelola donasi (donatur)

Layar: Pangan Saya (aktif, proses, selesai), detail donasi, ubah status, batalkan donasi, chat dan telepon penerima, profil donatur.

Yang dibuat:

- Daftar donasi milik donatur dengan saringan status.
- Perubahan status dari aktif ke proses ke selesai, termasuk pembatalan.
- Chat sederhana antara donatur dan penerima.
- Profil dan ubah alamat.

Selesai kalau: donatur bisa melihat siapa yang mengajukan, menyetujui, dan menandai donasi selesai.

### 4. Cari & minta bantuan (penerima)

Layar: beranda penerima, notifikasi penerima, pilih lokasi, ajukan bantuan dengan bukti, profil penerima, chatbot.

Yang dibuat:

- Daftar pangan terdekat berdasarkan lokasi, dengan sisa waktu pengambilan.
- Form pengajuan bantuan dan unggah foto bukti.
- Profil penerima.
- Chatbot berisi pertanyaan yang sudah disiapkan.

Selesai kalau: penerima bisa melihat pangan di sekitarnya dan mengirim pengajuan.

### 5. Bantuan saya (penerima)

Layar: daftar bantuan (proses, selesai), detail bantuan dan statusnya, konfirmasi sudah diterima, chat, telepon, lihat jarak di peta.

Yang dibuat:

- Daftar pengajuan milik penerima.
- Tampilan status dari disiapkan sampai selesai.
- Konfirmasi penerimaan, yang menutup donasi di sisi donatur.
- Peta lokasi pengambilan.

Selesai kalau: penerima bisa mengikuti satu pengajuan dari disetujui sampai dikonfirmasi diterima.

## Dikerjakan bersama lebih dulu

Empat hal ini dipakai semua orang, jadi diselesaikan sebelum masing-masing masuk ke fiturnya:

| Pekerjaan | Penanggung jawab |
| --- | --- |
| Kerangka project: Gradle, Compose, navigasi antar layar | Orang 1 |
| Tema: warna, huruf (Sora untuk judul, Figtree untuk teks), bentuk sudut | Orang 1 |
| Komponen bersama: tombol, kolom isian, kartu pangan, pop-up, navbar donatur dan penerima | Orang 3 |
| Model data dan sumber data: pengguna, donasi, pengajuan | Orang 2 dan orang 4 |

Backend belum diputuskan. Keputusan itu diambil bersama saat model data disusun.

## Supaya tidak saling menunggu

Fitur-fitur ini bergantung satu sama lain: donasi butuh akun, kelola donasi butuh donasi, bantuan saya butuh pengajuan. Kalau dikerjakan berurutan, orang 3 dan 5 menganggur di awal.

Jalan keluarnya: sepakati model data di minggu pertama, lalu tiap orang bekerja dengan data contoh. Orang 2 dan 4 memakai akun contoh, orang 3 memakai daftar donasi contoh, orang 5 memakai pengajuan contoh. Data sungguhan disambungkan setelah tiap fitur jalan sendiri.

## Yang boleh ditunda

Kalau waktu tidak cukup, bagian ini dikerjakan terakhir atau cukup berupa tampilan:

- Chatbot
- Telepon di dalam aplikasi
- Peta pengantaran oleh driver
- Notifikasi dari server

Yang tidak boleh ditunda adalah satu alur lengkap dari tiap peran: donatur mendaftar, mendonasikan, lalu menutup donasi; penerima mendaftar, mengajukan, lalu mengonfirmasi diterima.

## Aturan kerja di repo

- Satu branch per fitur, digabung ke `main` lewat pull request.
- Minimal satu orang lain membaca pull request sebelum digabung.
- Kunci API, `local.properties`, dan berkas keystore tidak di-commit.
