<img src="https://raw.githubusercontent.com/AndreaCioccarelli/MusicDownloader/master/media/launcher.png" height="100" width="100" align="right">


# MusicDownloader
[![APK](https://img.shields.io/badge/download-APK-E53935.svg)](https://github.com/AndreaCioccarelli/MusicDownloader/blob/master/bin/music-downloader.apk?raw=true)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/36e37693034c45ef80c4758d256ffe81)](https://www.codacy.com/project/cioccarelliandrea01/MusicDownloader/dashboard)
[![Min sdk](https://img.shields.io/badge/platform-Android-00E676.svg)](https://github.com/AndreaCioccarelli/MusicDownloader/blob/master/app/build.gradle)
[![Min sdk](https://img.shields.io/badge/minsdk-21-yellow.svg)](https://github.com/AndreaCioccarelli/MusicDownloader/blob/master/app/build.gradle)
[![Language](https://img.shields.io/badge/language-kotlin-orange.svg)](https://github.com/AndreaCioccarelli/MusicDownloader/blob/master/app/build.gradle)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/AndreaCioccarelli/MusicDownloader/blob/master/LICENSE)

Minimal Youtube MP3/MP4 downloader.

# Screenshots
<img src="https://raw.githubusercontent.com/cioccarellia/MusicDownloader/master/media/carousel.jpg">

# Backgrounding
Today's music services are reaching our every corner; there is an overwhelming offer on any kind of market around Music.
Apple Music, Spotify, Amazon Music, YouTube Music just to quote some.
They are all based on a client-server architecture where the user can acces millions of songs within a second, have them synchronized on the Cloud, get auto-tagged and good looking thumbnailed music, and so on.
Then, on the other hand, there are people like me, that does not really care about paying for music and that just wants to have it to listen on their phones, free. But not for a 3 months trial or as long as I don't try to download it offline, free as long as I have my music on my storage, and that's what this app is about.

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

#### Redistributing this app as your own is NOT permitted.
