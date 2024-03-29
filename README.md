<img src="https://raw.githubusercontent.com/cioccarellia/MusicDownloader/master/media/launcher.png" height="100" width="100" align="right">


# MusicDownloader
[![APK](https://img.shields.io/badge/download-APK-E53935.svg)](https://github.com/cioccarellia/MusicDownloader/blob/master/bin/music-downloader.apk?raw=true)
[![Min sdk](https://img.shields.io/badge/platform-Android-00E676.svg)](https://github.com/cioccarellia/MusicDownloader/blob/master/app/build.gradle)
[![Min sdk](https://img.shields.io/badge/minsdk-21-yellow.svg)](https://github.com/cioccarellia/MusicDownloader/blob/master/app/build.gradle)
[![Language](https://img.shields.io/badge/language-kotlin-orange.svg)](https://github.com/cioccarellia/MusicDownloader/blob/master/app/build.gradle)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/cioccarellia/MusicDownloader/blob/master/LICENSE)

Minimal Youtube MP3/MP4 downloader.

# Screenshots
<img src="https://raw.githubusercontent.com/cioccarellia/MusicDownloader/master/media/carousel.jpg">

# Details
This project is a simple single-activity app, it makes use of:
- AndroidX and Jetpack architecture components
- Room Persistence Library
- Glide for image loading
- Gson for JSON parsing
- OkHttp for network requests
- CryptoPrefs to wrap shared preferences

The UI is composed by:
- A RecyclerView to display the YouTube search results
- A fragment for the BottomSheetDialog
- A dialog for the Download Checklist


## Discontinued
After a few bad experiences and advice from lawyers, joined with the fact that I really don't wish to be ceased and desisted by Google, the project is discontinued and here only for Android-architecture / UI / UX expositional purposes. While I dont think it would be hard to make the html scraper inside the app parameterizable and creating a working version with a programmable module, to allow a unified version of the app to download videos off different websites (and potentially automatically checking which downloaders are up and relying on these), this wont be happening (here)

