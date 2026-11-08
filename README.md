# Sempat

Sempat adalah aplikasi Android untuk menyalurkan makanan berlebih kepada orang yang membutuhkan. Donatur memotret makanannya, aplikasi mengenali jenis makanan itu dan mengisi formulir donasi, lalu penerima di sekitar bisa mengajukan pengambilan. Semua makanan dibagikan gratis. Tidak ada jual beli di aplikasi ini.

"Setiap Makanan Punya KeSEMPATan"

![Platform](https://img.shields.io/badge/platform-Android-3DDC84)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0%2B-7F52FF)
![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4)

Status: tahap rancangan. Repo ini baru berisi konsep dan rencana pengembangan. Kode aplikasinya menyusul.

## Latar belakang

Restoran, toko roti, katering, dan rumah tangga sering punya makanan yang masih layak tetapi tidak habis. Makanan itu biasanya berakhir di tempat sampah, padahal di lingkungan yang sama ada orang yang kesulitan makan hari itu.

Masalahnya jarang soal niat. Orang yang mau berbagi tidak tahu harus memberi ke siapa, dan mengumumkan donasi satu per satu itu merepotkan. Sempat dibuat supaya membagikan makanan cukup dengan satu foto.

## Cara kerja

### Mendaftar dengan KTP

Pengguna baru memotret KTP. Aplikasi membaca data di kartu itu dan mengisi formulir pendaftaran. Pengguna tinggal memeriksa hasilnya dan memperbaiki kalau ada yang salah baca. Yang diketik sendiri hanya nomor HP dan lokasi. Setelah itu pengguna masuk dengan nomor HP dan kode OTP, tanpa kata sandi.

Verifikasi ini ada supaya donatur dan penerima sama-sama orang sungguhan, dan satu orang tidak mendaftar berkali-kali.

### Membagikan makanan

1. Donatur memotret makanan yang mau dibagikan.
2. Aplikasi mengenali jenis makanannya dan mengisi nama serta kategori di formulir donasi.
3. Donatur melengkapi jumlah porsi, batas waktu pengambilan, dan lokasi, lalu mengirimnya.

### Menerima makanan

Penerima melihat daftar donasi yang masih tersedia, memilih yang lokasinya terjangkau, lalu mengajukan pengambilan. Makanan diambil langsung di lokasi donatur sebelum batas waktunya habis.

## AI berjalan di HP

Pembacaan KTP dan pengenalan makanan dijalankan di HP pengguna. Fotonya tidak dikirim ke server untuk diproses.

Ada dua alasan. Pertama, foto KTP adalah data pribadi, jadi lebih aman kalau tidak meninggalkan perangkat. Kedua, fitur ini tetap bisa dipakai saat sinyal buruk, dan itu sering terjadi justru di tempat orang paling membutuhkan aplikasi ini.

Rencana teknisnya:

| Fitur | Pendekatan |
| --- | --- |
| Baca KTP | Pengenalan teks ML Kit yang berjalan di perangkat, lalu pemilahan kolom KTP |
| Kenali makanan | Model klasifikasi gambar TensorFlow Lite |

Hasil AI selalu bisa diubah pengguna sebelum disimpan. Model bisa salah, terutama pada foto yang gelap atau buram.

## Tech stack

| Bagian | Teknologi |
| --- | --- |
| Bahasa | Kotlin 2.0+ |
| UI | Jetpack Compose, Material 3 |
| Arsitektur | MVVM |
| Kamera | CameraX |
| AI di perangkat | ML Kit Text Recognition, TensorFlow Lite |
| Penyimpanan lokal | Room, DataStore |
| Backend | Belum diputuskan |

## Struktur project

Folder sudah dibuat (masih kosong, diisi per fitur):

```
app/src/main/java/com/sempatapps/
├── core/
│   ├── designsystem/  Tema (warna, font dari Figma) dan komponen UI bersama
│   ├── navigation/    NavHost dan daftar route
│   └── di/            Modul Hilt
├── data/
│   ├── local/         Room, DataStore
│   ├── remote/        API (backend belum diputuskan)
│   └── repository/    Implementasi repository
├── domain/
│   ├── model/         Model data
│   └── usecase/       Logika bisnis
├── ai/
│   ├── ktp/           Pembaca KTP (ML Kit)
│   └── food/          Pengenal makanan (TFLite)
└── presentation/      Satu folder per fitur: XxxScreen.kt, XxxViewModel.kt, XxxUiState.kt
    ├── auth/          Pendaftaran dengan KTP dan masuk
    ├── home/          Daftar donasi yang tersedia
    ├── donation/      Buat donasi, ajukan pengambilan, status
    ├── camera/        Kamera dan pratinjau hasil AI
    └── profile/       Akun dan riwayat
```

Semua versi library ada di `gradle/libs.versions.toml`. Tambah library baru di sana, lalu panggil lewat `libs.xxx` di `app/build.gradle.kts`.

Build butuh JDK 17 ke atas. Kalau build dari terminal, pakai JDK bawaan Android Studio, misalnya `export JAVA_HOME=/opt/android-studio/jbr`.

## Rencana pengembangan

- [x] Konsep dan rancangan antarmuka
- [ ] Kerangka project Android (Gradle, Compose)
- [ ] Pendaftaran dengan foto KTP
- [ ] Kamera dan pengenalan makanan
- [ ] Daftar donasi dan halaman detail
- [ ] Pengajuan pengambilan dan serah terima
- [ ] Donasi terdekat berdasarkan lokasi

## Tim

Kelompok 6. Pembagian kerja per fitur ada di [PEMBAGIAN-TUGAS.md](PEMBAGIAN-TUGAS.md).
