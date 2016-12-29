package org.softeg.slartus.forpdaplus.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.animation.AccelerateInterpolator;

import com.plattysoft.leonids.ParticleSystem;

import org.softeg.slartus.forpdaplus.R;

import java.util.Locale;

/**
 * Created by isanechek on 28.12.16.
 */

public class NewYear extends Activity {

    public static void check(Context ctx) {
        Intent intent = new Intent(ctx, NewYear.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ctx.startActivity(intent);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_yaer);

    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        new ParticleSystem(this, 80, R.drawable.confeti2, 10000)
                .setSpeedModuleAndAngleRange(0f, 0.1f, 180, 180)
                .setRotationSpeed(144)
                .setAcceleration(0.000017f, 90)
                .emit(findViewById(R.id.emiter_top_right), 8);

        new ParticleSystem(this, 80, R.drawable.confeti3, 10000)
                .setSpeedModuleAndAngleRange(0f, 0.1f, 0, 0)
                .setRotationSpeed(144)
                .setAcceleration(0.000017f, 90)
                .emit(findViewById(R.id.emiter_top_left), 8);



//        new ParticleSystem(this, 100, R.drawable.star_white_border, 800)
//                .setScaleRange(0.7f, 1.3f)
//                .setSpeedRange(0.1f, 0.25f)
//                .setAcceleration(0.0001f, 90)
//                .setRotationSpeedRange(90, 180)
//                .setFadeOut(200, new AccelerateInterpolator())
//                .oneShot(findViewById(R.id.tv_ny), 100);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
