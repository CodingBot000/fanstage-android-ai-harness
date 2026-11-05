# UX-007 시각 자산 목록

2026-11-05, Codex 내장 imagegen. 인물은 가상이며 원본은 `$CODEX_HOME/generated_images/01a1ab5e-1eb3-7552-9209-f2169c41a3dc/`에 보관한다. 1차 무드보드 직접 참조 결과는 세 그룹의 인물이 반복되어 미채택했고, 재생성 중 1회 출력 안전 필터 차단 후 의복을 명시한 별도 요청으로 생성했다. 후속 이미지는 각 그룹 기준 이미지를 참조했다. 초상 동일성을 보증하지 않으며 스타일·장면 중심으로 선정했다.

공통 프롬프트: original fictional artist, editorial music photography, no text, logo or watermark. 커버는 4:5 세로, 피드는 정사각형을 요청했다. 완전한 권리 판단을 주장하지 않는다.

| Resource key / 역할 프롬프트 | 원본 파일 및 SHA-256 | 최종 WebP SHA-256 | 바이트 |
|---|---|---|---:|
| `paper_lantern_hero` — indie band rehearsal, lilac stage and paper lanterns | `exec-2c936008-c0b7-4000-b613-6ae6724d0907.png` `c0041eaf85b8f923a7e5f309c900967cf93dacd0c5b26c51cc0d7b1fe706d31c` | `ec65c670fa64df76ca7c92fe67a0239919aee4ab9dcebc77877b529d967204a6` | 82094 |
| `blue_pencil_hero` — art-pop artist, blue atelier and sketchbooks | `exec-6c24a984-735b-4f75-8437-b6d61f641ba4.png` `9cb127df0e2a53cda73d54281470239295d2be57aafe333237de01bf86b56942` | `dac6b286795b2d714de2217cabda1cf90191b5b3cab7156080f5d8085f18d0eb` | 160814 |
| `dice_garden_hero` — synth-pop musician, emerald garden and glowing dice | `exec-ef8cbf55-ceab-4921-9d45-ac3f9ccb38ad.png` `9473b6cfdac3f8df11965aa07fc212215ae6adb858abc9f7c3cf457f2740d33d` | `6430958a13c5eeefb2da50ba14c9e1a3c26adccd2c0e93dddb241298a55c7d7e` | 120852 |
| `paper_lantern_cover` — portrait cover derived from Paper Lantern master | `exec-7c58f484-661d-4528-9628-2f9f6d8e3fb7.png` `d1dd0b90ae8b3684e69fb5880563f5d48bc4bb606ec32bc40d664c5e2c184f4c` | `ea0e750d6270544ec7a300dca0c8d5514f1e6366e92f1c9e89b8317dd9080ba5` | 129318 |
| `paper_lantern_feed_01` — backstage scene derived from Paper Lantern master | `exec-3c8970be-7bb0-4b64-98c7-c632cba65eef.png` `88eaf9427584d853fb99996a5372b02e59dd7bdd832cf8bbf798dd08ff215254` | `f54c10cc52773c4be88cfd1f93cbcc940e92fe558ba4cc593163aa7c9b8ca13b` | 132198 |
| `blue_pencil_cover` — portrait cover derived from Blue Pencil master | `exec-afe8537d-1b98-4af7-9819-b52235485c9d.png` `9dd92737cbf225e3a5936fca8d1af4393060fe8adda05f4ff21c120858c743c4` | `9a68700e8a94f9efabb6fb1154a23a3926b258aa6d5d44099e07e1af91661568` | 183126 |
| `blue_pencil_feed_01` — studio scene derived from Blue Pencil master | `exec-6494422c-72e6-4ec7-96c3-6f73294a84b7.png` `7cfb2a908f0f35ac8cbd970da1dd1752e991c824c7b58546f08528b7690dc1df` | `ae4f10a1b29d55afe7e9b65f8135d35479ef2b962fb938c05fe490b372523cc8` | 176974 |
| `dice_garden_cover` — portrait cover derived from Dice Garden master | `exec-549edafb-0430-4ccb-825e-378bf08625f8.png` `a3cb2f3c3497974d0e91d8e3525f07a5b2b5a60fe2f217ea489053ac0b922e8d` | `cdf592406c78d0c8a61f8a59dd028e1b78fb9683c28e5ff4913cc7a4b4c65a7c` | 148136 |
| `dice_garden_feed_01` — keyboard scene derived from Dice Garden master | `exec-09e779b2-70cc-4a94-af2d-732b5050e655.png` `45a6994edfd7e67eb4a4675891960c1873ea098dd8dc3ebdebc42a5a51e8f84a` | `293a8607b51e4cab423cbfb8b0ee837f1ebdb09e73a19aead0452c217ddb5ec6` | 117644 |

최종 파일은 `android/core/artwork/src/main/res/drawable-nodpi/`에 있다. Hero는 폭 1200px/품질 82, 커버·피드는 폭 900px/품질 80 WebP다. 실제 화면에서 Hero와 프로필을 확인했으며 커버·피드의 모든 크롭은 UX-009에서 추가 확인한다.

## 생성 프롬프트

기준 이미지:

1. `A fictional indie band rehearses on a small softly lit concert stage, all musicians wearing everyday long-sleeved stage clothes, paper lanterns, lilac haze and flowers; original music editorial photograph, wide framing, no text, no logo, no famous people.`
2. `A fictional art-pop musician in a long-sleeved blue work shirt paints in a bright blue-and-ivory studio surrounded by sketchbooks, candid music editorial photograph, wide framing, no text, no logo, no famous people.`
3. `A fictional synth-pop musician in a dark tailored suit at a keyboard in a deep emerald garden with glowing geometric dice and subtle purple flowers, music editorial photography, wide framing, no text, no logo, no famous people.`

후속 이미지(각 기준 이미지를 입력으로 사용):

1. `Vertical 4:5 portrait cover of a fictional indie-pop band in an atmospheric rehearsal room with paper lanterns and lilac flowers. Preserve the reference's clothing and overall band identity; sophisticated concert editorial photo. No text, logo, or watermark.`
2. `Square candid backstage scene of the fictional indie-pop band in the reference arranging paper lanterns and instruments, muted lilac and plum, original editorial photography. Modest everyday clothing, no text, logo, or watermark.`
3. `Vertical 4:5 cover portrait of the fictional art-pop musician from the reference in a bright blue and ivory creative atelier with sketchbooks. Editorial photo, modest long sleeves, no text, logo, or watermark.`
4. `Square candid music editorial of the fictional artist from the reference arranging sketchbooks and blue paint beside an audio keyboard in a bright atelier, no text, logo, or watermark.`
5. `Vertical 4:5 editorial portrait of the fictional synth-pop artist from the reference wearing a dark suit in an emerald night garden with violet flowers and glowing geometric dice; no text, logo, or watermark.`
6. `Square candid editorial scene of the fictional synth-pop artist from the reference preparing a keyboard among emerald foliage, violet flowers and softly glowing geometric dice; no text, logo, or watermark.`

## UX-012 앱 아이콘 (2026-11-06)

- 도구: Codex 내장 imagegen. 프롬프트: `Create a polished original Android app launcher icon for FanStage, a premium music and fan community app. A single simple stylized white star formed by a stage spotlight and fan-shaped arcs, centered on a deep plum-to-violet background, crisp geometric vector-like shapes, strong contrast, readable at tiny size. Square canvas, full bleed, no lettering, no logos, no watermark.`
- 원본: `$CODEX_HOME/generated_images/01a1ab5e-1eb3-7552-9209-f2169c41a3dc/exec-a138ed08-1215-4379-acfe-573304acc61c.png`, SHA-256 `f3a7f3baf1b9cb5bee1f639d0d8eba552ad6f338cfebd75c342a919e6c6c3444`.
- 앱 리소스: `android/app/src/main/res/drawable-nodpi/fanstage_launcher.webp`, 512×512, 11,398 bytes, SHA-256 `e3b9bae4401795c16b24e6ec633166c9f813cdd1b418afd345872cc5c46c353c`. Android manifest의 icon/roundIcon에서 참조한다.
