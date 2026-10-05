# SatView: your own Copernicus satellite app for Android

SatView shows free Copernicus satellite images on a map, for any place and date:

## 📲 Download the app (no Android Studio needed)

GitHub builds the app automatically every time the code changes.

1. On your Android phone, open this repository's **Releases** page (the "latest" release).
2. Download **SatView.apk** and open it.
3. If Android warns about unknown apps, allow your browser to install apps. This is normal for apps outside the Play Store.
4. Open SatView. **Demo mode works immediately, with no login.** The live layers below need free Copernicus keys (Step 1).

## Modes

| Mode | Satellite | What it shows |
|---|---|---|
| 🌍 Demo (no login) | Sentinel-2 cloudless 2024 by EOX | A ready-made, cloud-free satellite map of the whole Earth |
| 🛰 True colour | Sentinel-2 | A natural photo from space |
| 🌱 Crop & vegetation health | Sentinel-2 | NDVI: red = bare or dry, green = healthy plants |
| 💧 Water bodies | Sentinel-2 | Rivers, lakes, ponds in blue |
| 🌊 Floods (radar) | Sentinel-1 | Water and flooding, even through clouds |
| 🏭 Air quality | Sentinel-5P | NO₂ pollution (from traffic, industry, burning) |

---

## Step 1: Get your free Copernicus keys (about 10 minutes)

1. Go to **https://dataspace.copernicus.eu** and click **Register**. Confirm your email.
2. Open the **Sentinel Hub Dashboard**: https://shapps.dataspace.copernicus.eu/dashboard and log in.
3. Go to **User settings → OAuth clients → + Create**.
4. Give it a name (for example "SatView") and click **Create**.
5. **Copy the Client ID and the Client secret right away.** The secret is shown only once. Save both in a note.

The free account includes a monthly processing quota, which is plenty for personal use.

---

**Steps 2–4 are only needed if you want to change the code and build it yourself on your PC.** Otherwise, just edit files on GitHub, and a new APK appears on the Releases page a few minutes later (watch progress in the **Actions** tab).

## Step 2: Install Android Studio (one time)

1. Download it free from **https://developer.android.com/studio** (Windows, Mac or Linux).
2. Install it with the default options. The first launch downloads extra parts, so use good internet (2–3 GB).

## Step 3: Open the project

1. Unzip `SatView.zip` somewhere easy, such as your Documents folder.
2. In Android Studio choose **File → Open** and select the `SatView` folder (the one containing `settings.gradle.kts`).
3. Wait while it says "Gradle sync…" at the bottom. The first time can take 5–15 minutes.
   - If it asks to **"Use Gradle wrapper"** or to **upgrade** something, click **OK** or **Use**.

## Step 4: Run it on your phone

1. On your phone: **Settings → About phone →** tap **Build number** 7 times. This turns on Developer options.
2. **Settings → Developer options →** turn on **USB debugging**.
3. Connect the phone to the PC with a USB cable and tap **Allow** on the phone.
4. In Android Studio, pick your phone at the top, then press the green **▶ Run** button.

The app installs and opens. On first launch it asks for your Client ID and secret from Step 1.

**To get an APK file to share:** **Build → Build App Bundle(s) / APK(s) → Build APK(s)**, then click **locate**. Copy that `.apk` to any Android phone to install it.

## Using the app

1. Choose a mode at the top.
2. Move and zoom the map to your area, or tap 📍 for your location.
3. Tap **Get satellite image of this view**.
4. Use the opacity slider to compare with the map underneath.

**Tips**
- Image empty or patchy? Make the date range longer or allow more cloud cover. Sentinel-2 passes every ~5 days, but clouds often hide the ground in the rainy seasons.
- During cloudy rainy seasons, use **Floods (radar)**, which sees through clouds.
- Air quality works best zoomed out (whole regions). Its pixels are about 5 km wide.

## How the project is organised (for learning)

```
app/src/main/
├── AndroidManifest.xml                  app name, permissions (internet, location)
├── assets/index.html                    the screen: map, buttons, and satellite "recipes"
└── java/tz/satview/
    ├── MainActivity.kt                  opens the screen
    └── SentinelBridge.kt                logs in to Copernicus and downloads images
```

**Want a new layer?** Add an entry to `MODES` in `index.html`. Each one has an `evalscript`, a short script that turns satellite bands into colours. You can find hundreds of ready-made ones at https://custom-scripts.sentinel-hub.com, such as moisture, burned areas, and soil. Copy the script into a new mode.

## Credits

- Satellite data: Copernicus Sentinel missions (EU / ESA), via the Copernicus Data Space Ecosystem.
- Demo map: Sentinel-2 cloudless (https://s2maps.eu) by EOX IT Services GmbH, CC BY-NC-SA 4.0 (non-commercial use).
- Map: © OpenStreetMap contributors, Leaflet.

## Troubleshooting

| Problem | Fix |
|---|---|
| "Login failed (401)" | Client ID or secret is wrong. Tap ⚙ and paste them again. |
| "Copernicus error (400)" | The area is too big or the dates are odd. Zoom in or change the dates. |
| Map is blank | The phone has no internet. The app needs it. |
| Gradle sync error | **File → Sync Project with Gradle Files**. If it mentions a version, accept Android Studio's suggested upgrade. |
