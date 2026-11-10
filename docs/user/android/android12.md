# Android 12 and low-memory phones

The app installs on Android 12 (API 31 and 32).
This page lists what is different there, and what to try on a phone like the Moto G60
(Snapdragon 732G, Adreno 618, 4 GB of memory).

These notes are from reading the code. They have not been checked on a real Android 12 phone yet.
If you try it, please open an issue with your device, Android version, GPU and the shared log.

## What changes on Android 12

| Part | Android 13 and newer | Android 12 |
| --- | --- | --- |
| Install | normal | normal (`minSdk 31`) |
| Notifications | the app asks for permission | no question, notifications are on by default |
| Phone vibration | `VibrationAttributes` | `AudioAttributes` (same "media" usage) |
| Performance hint session | used | not available: the game runs without that scheduling hint |
| Thermal headroom (heat guard) | used | used (it exists since Android 11) |
| Game mode settings (`game_mode_config.xml`) | read by the system | partly ignored by the system |

Everything else (setup, the compiler that runs on the phone, the Vulkan renderer, touch controls) is the same code.

## Graphics driver

The renderer needs Vulkan 1.1, and it uses dynamic rendering when the driver offers it
(Vulkan 1.3 or `VK_KHR_dynamic_rendering`) and classic render passes when it does not.
The stock Adreno 618 driver of Android 12 reports Vulkan 1.1.
It may be enough to start, but it is old and may have bugs.
If the game shows broken graphics or stops at the title, install a Mesa Turnip driver package
(AdrenoTools format) in **Settings > Graphics**: Turnip supports Adreno 6xx GPUs and gives a newer Vulkan.
See [Android GPU support](../../developer/android/gpu.md).

## 4 GB of memory

- Close other apps before **Start**. Setup compiles with fewer parallel compiler processes when free memory is low
  (the app chooses this itself), so it takes longer but should finish.
- Keep the phone plugged in and cool. Setup pauses by itself when the phone is warm or the battery is low.
- Leave a few GB of storage free. Setup needs room for the extracted game and the compiled code.
- If Android closes the app during setup, open it again and tap **Start**: finished pieces are reused.
- In the game, keep **Frame rate** at 60. Higher rates add in-between frames and use more heat and memory.
