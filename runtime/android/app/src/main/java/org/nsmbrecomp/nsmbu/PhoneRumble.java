package org.nsmbrecomp.nsmbu;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.provider.Settings;

/**
 * Short, non-repeating leases: a stalled or killed game cannot leave the phone vibrating.
 *
 * VibrationAttributes (and Vibrator.vibrate(effect, VibrationAttributes)) start at Android 13. Android 12
 * (API 31/32) describes the same "media" usage with AudioAttributes, so each version gets its own small
 * helper class: the other one is never loaded.
 */
final class PhoneRumble {
    private final Context context;
    private final Vibrator vibrator;
    private final Object attributes = Build.VERSION.SDK_INT >= 33 ? Modern.attributes() : Legacy.attributes();
    private boolean active, playing;

    PhoneRumble(Context context) {
        this.context = context.getApplicationContext();
        Vibrator found = null;
        try {
            VibratorManager manager = this.context.getSystemService(VibratorManager.class);
            if (manager != null) found = manager.getDefaultVibrator();
            if (found != null && !found.hasVibrator()) found = null;
        } catch (RuntimeException ignored) {}
        vibrator = found;
    }

    synchronized void setActive(boolean active) {
        this.active = active;
        if (!active) stop();
    }

    synchronized void update(int level) {
        if (vibrator == null) return;
        try {
            if (!active || level <= 0 || Settings.System.getInt(context.getContentResolver(),
                    Settings.System.HAPTIC_FEEDBACK_ENABLED, 1) == 0) {
                stop();
                return;
            }
            int amplitude = vibrator.hasAmplitudeControl()
                    ? Math.max(1, Math.min(255, Math.round(Math.min(level, 65535) * 255f / 65535f)))
                    : VibrationEffect.DEFAULT_AMPLITUDE;
            VibrationEffect effect = VibrationEffect.createOneShot(150, amplitude);
            if (Build.VERSION.SDK_INT >= 33) Modern.vibrate(vibrator, effect, attributes);
            else Legacy.vibrate(vibrator, effect, attributes);
            playing = true;
        } catch (RuntimeException ignored) { stop(); }
    }

    private void stop() {
        if (!playing || vibrator == null) return;
        playing = false;
        try { vibrator.cancel(); } catch (RuntimeException ignored) {}
    }

    /** Android 13 and newer. */
    private static final class Modern {
        static Object attributes() {
            return new VibrationAttributes.Builder().setUsage(VibrationAttributes.USAGE_MEDIA).build();
        }
        static void vibrate(Vibrator vibrator, VibrationEffect effect, Object attributes) {
            vibrator.vibrate(effect, (VibrationAttributes) attributes);
        }
    }

    /** Android 12 (API 31/32): the AudioAttributes overload exists since Android 8 and is only deprecated later. */
    private static final class Legacy {
        static Object attributes() {
            return new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build();
        }
        @SuppressWarnings("deprecation")
        static void vibrate(Vibrator vibrator, VibrationEffect effect, Object attributes) {
            vibrator.vibrate(effect, (AudioAttributes) attributes);
        }
    }
}
