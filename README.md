<img src="https://raw.githubusercontent.com/AndreaCioccarelli/MusicDownloader/master/media/launcher.png" height="100" width="100" align="right">


# MusicDownloader
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/36e37693034c45ef80c4758d256ffe81)](https://www.codacy.com/project/cioccarelliandrea01/MusicDownloader/dashboard)
[![Min sdk](https://img.shields.io/badge/platform-Android-00E676.svg)](https://github.com/AndreaCioccarelli/MusicDownloader/blob/master/app/build.gradle)
[![Min sdk](https://img.shields.io/badge/minsdk-21-yellow.svg)](https://github.com/AndreaCioccarelli/MusicDownloader/blob/master/app/build.gradle)
[![Language](https://img.shields.io/badge/language-kotlin-orange.svg)](https://github.com/AndreaCioccarelli/MusicDownloader/blob/master/app/build.gradle)
[![License](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/AndreaCioccarelli/MusicDownloader/blob/master/LICENSE)

A beautiful Kotlin material design mp3/mp4 files downloader using official YouTube APIs

# Screenshots
<img src="https://raw.githubusercontent.com/AndreaCioccarelli/MusicDownloader/master/media/carousel.jpg">

# Details
This app is a simple single-activity project, using a fragment for the bottom sheet dialog, a recyclerview to display the list of the results parsed from YouTube, a material dialog for the Checklist section.
An EditText view is available for the user to type text, and a request is performed to YouTube servers (This app uses API v3) to retrieve the matching results. Another service is user to download the related MP3/MP4 file.
Since the input is directly passed to Google YouTube APIs, you can also use search operators to filter the results.
This app is optimized for speed and lightness. The final apk size is less than 4MB and it's packed with many goodies and tweaks to make it clean and blazing-fast (e.g. image caching, checklist preferences, tablet support).
The whole project fits Google material design guidelines and uses some awesome 3rd-party libraries like [Alerter](https://github.com/Tapadoo/Alerter), [Toasty](https://github.com/GrenderG/Toasty) and [material-dialogs](https://github.com/afollestad/material-dialogs) to make everything flawless and smooth.

#### Redistributing this app as your own is NOT permitted.
