package ir.matin.application.weather.Ui;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;

public class ScaleBtn implements View.OnTouchListener {
    private final Context context;
    private ObjectAnimator scaleDownX, scaleDownY, scaleUpX, scaleUpY;

    public ScaleBtn(Context context) {
        this.context = context;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (scaleUpX != null && scaleUpX.isRunning()) scaleUpX.cancel();
                if (scaleUpY != null && scaleUpY.isRunning()) scaleUpY.cancel();

                scaleDownX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.8f);
                scaleDownY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.8f);
                scaleDownX.setDuration(150);
                scaleDownY.setDuration(150);
                scaleDownX.start();
                scaleDownY.start();

                // سایه بیشتر
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.setElevation(12f);
                }

                SharedPreferences hapticPref ;
                hapticPref = context.getSharedPreferences("haptic_setting",Context.MODE_PRIVATE);
                if (hapticPref.getBoolean("is_on",true) == true){
                    vibrate(20);
                }


                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (scaleDownX != null && scaleDownX.isRunning()) scaleDownX.cancel();
                if (scaleDownY != null && scaleDownY.isRunning()) scaleDownY.cancel();

                scaleUpX = ObjectAnimator.ofFloat(v, "scaleX", v.getScaleX(), 1f);
                scaleUpY = ObjectAnimator.ofFloat(v, "scaleY", v.getScaleY(), 1f);
                scaleUpX.setDuration(150);
                scaleUpY.setDuration(150);
                scaleUpX.start();
                scaleUpY.start();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    v.setElevation(6f);
                }
                break;
        }
        return false;
    }

    private void vibrate(int ms) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(ms);
            }
        }
    }
}
