#!/bin/sh
set -eu
out="$(CDPATH= cd -- "$(dirname -- "$0")/../android/app/src/androidTest/assets/player001" && pwd)"
for size in 640x360 1280x720; do
  ffmpeg -hide_banner -loglevel error -y -f lavfi -i "testsrc2=size=$size:rate=15" -f lavfi -i "sine=frequency=440:sample_rate=48000" -t 6 -c:v libx264 -preset ultrafast -pix_fmt yuv420p -g 30 -sc_threshold 0 -c:a aac -b:a 64k -hls_time 2 -hls_playlist_type vod -hls_segment_filename "$out/${size}_%02d.ts" "$out/$size.m3u8"
done
ffmpeg -hide_banner -loglevel error -y -f lavfi -i 'testsrc2=size=360x640:rate=15' -t 3 -c:v libx264 -preset ultrafast -pix_fmt yuv420p -an "$out/portrait.mp4"
cat > "$out/subtitles.vtt" <<'VTT'
WEBVTT
X-TIMESTAMP-MAP=LOCAL:00:00:00.000,MPEGTS:126000

00:00:00.000 --> 00:00:05.000
Synthetic test caption
VTT
cat > "$out/subtitles.m3u8" <<'VTT'
#EXTM3U
#EXT-X-TARGETDURATION:6
#EXT-X-VERSION:3
#EXT-X-MEDIA-SEQUENCE:0
#EXTINF:6.000,
subtitles.vtt
#EXT-X-ENDLIST
VTT
cat > "$out/master.m3u8" <<'HLS'
#EXTM3U
#EXT-X-VERSION:3
#EXT-X-MEDIA:TYPE=SUBTITLES,GROUP-ID="subs",NAME="English",LANGUAGE="en",DEFAULT=NO,AUTOSELECT=YES,URI="subtitles.m3u8"
#EXT-X-STREAM-INF:BANDWIDTH=1200000,RESOLUTION=640x360,SUBTITLES="subs",CLOSED-CAPTIONS=NONE
640x360.m3u8
#EXT-X-STREAM-INF:BANDWIDTH=4000000,RESOLUTION=1280x720,SUBTITLES="subs",CLOSED-CAPTIONS=NONE
1280x720.m3u8
HLS
