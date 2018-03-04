package com.codecamp.bitfit.fragments;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codecamp.bitfit.MainActivity;
import com.codecamp.bitfit.R;
import com.codecamp.bitfit.database.PushUps;
import com.codecamp.bitfit.database.Squat;
import com.codecamp.bitfit.database.Run;
import com.codecamp.bitfit.database.User;
import com.codecamp.bitfit.database.Workout;
import com.codecamp.bitfit.util.DBQueryHelper;
import com.codecamp.bitfit.util.Util;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.tolstykh.textviewrichdrawable.TextViewRichDrawable;

import net.steamcrafted.materialiconlib.MaterialIconView;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {
    // initialized user
    private User user = DBQueryHelper.findUser();

    private double bmi;

    // bmi share to Facebook
    private MaterialIconView shareBMIButton;
    private int genderUrlFormat = 1;

    // highsore pushups share to Facebook
    private MaterialIconView shareHighScorePushUpsButton;
    private CardView highscorePushupsCardview;

    // highscore Squat share to Facebook
    private MaterialIconView shareHighScoreSquatsButton;
    private CardView highscoreSquatsCardview;

    // highscore Runnin share to Facebook
    private MaterialIconView shareHighScoreRunButton;
    private CardView highscoreRunCardview;

    // share last activity to Facebook
    private MaterialIconView shareLastActivityButton;
    private CardView lastActivityCardview;

    public static HomeFragment getInstance() {
        HomeFragment fragment = new HomeFragment();

        return fragment;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // get last activity
        lastActivity();

        // inflates highscore cards
        highscorePushups();
        highscoreSquats();
        highscoreRun();

        // inflates bmi card with values
        bmi();

        // initialized Facebook share buttons
        shareBMIToFacebook();
        shareHighScorePushUpsToFacebook();
        shareHighScoreSquatToFacebook();
        shareHighScoreRunToFacebook();
        shareLastActivityToFacebook();
    }

    private void lastActivity() {
        // get last activity from shared prefs
        Workout lastActivity = DBQueryHelper.getLastWorkout(getContext());
        // initialize lastActivity view
        TextView lastActivityDuration = getView().findViewById(R.id.textview_lastActivity_duration);
        TextView lastActivityCalories = getView().findViewById(R.id.textview_lastActivity_calories);
        TextViewRichDrawable lastActivityPerMinOrSpeed = getView().findViewById(R.id.textview_lastActivity_perMin_or_speed);
        TextViewRichDrawable lastActivityRepeatsOrDistance = getView().findViewById(R.id.textview_lastActivity_repeats_or_distance);
        TextView lastActivityDate = getView().findViewById(R.id.textview_lastActivity_date);
        TextView noActivityText = getView().findViewById(R.id.text_last_activity_no_activity);
        View content = getView().findViewById(R.id.cardview_last_activity_constraintlayout);
        ImageView lastActivityIcon = getView().findViewById(R.id.imageview_lastActivity_icon);

        // set card content
        if(lastActivity == null) {
            content.setVisibility(View.INVISIBLE);
            lastActivityDate.setVisibility(View.INVISIBLE);
            noActivityText.setVisibility(View.VISIBLE);
        }
        else if(lastActivity instanceof PushUps) {
            PushUps pushUps = (PushUps) lastActivity;
            lastActivityCalories.setText(
                    String.format(" %s kcal", String.valueOf(pushUps.getCalories())));
            lastActivityDuration.setText(
                    String.format(" %s min", Util.getMillisAsTimeString(pushUps.getDurationInMillis())));
            lastActivityPerMinOrSpeed.setText(
                    String.format(" %s P/min", String.valueOf(pushUps.getPushPerMin())));
            lastActivityRepeatsOrDistance.setText(
                    String.format(" %s Push-Up(s)", String.valueOf(pushUps.getRepeats())));
            lastActivityDate.setText(String.format(" %s", pushUps.getCurrentDate()));
            lastActivityIcon.setVisibility(View.VISIBLE);
            lastActivityIcon.setImageResource(R.drawable.icon_pushup_color);
        }
        else  if(lastActivity instanceof  Squat) {
            Squat squat = (Squat) lastActivity;
            lastActivityCalories.setText(
                    String.format(" %s kcal", String.valueOf(squat.getCalories())));
            lastActivityDuration.setText(
                    String.format(" %s min", Util.getMillisAsTimeString(squat.getDurationInMillis())));
            lastActivityPerMinOrSpeed.setText(
                    String.format(" %s S/min", String.valueOf(squat.getSquatPerMin())));
            lastActivityRepeatsOrDistance.setText(
                    String.format(" %s Squat(s)", String.valueOf(squat.getRepeats())));
            lastActivityDate.setText(
                    String.format(" %s", squat.getCurrentDate()));
            lastActivityIcon.setVisibility(View.VISIBLE);
            lastActivityIcon.setImageResource(R.drawable.icon_squat_color);
        }
        else if (lastActivity instanceof Run) {
            Run run = (Run) lastActivity;
            lastActivityCalories.setText(
                    String.format(" %s kcal", String.valueOf(run.getCalories())));
            lastActivityDuration.setText(
                    String.format("%s Stunden", Util.getMillisAsTimeString(run.getDurationInMillis())));
            lastActivityPerMinOrSpeed.setText(
                    String.format(" %s km/h", String.valueOf(run.getSpeed())));
            lastActivityPerMinOrSpeed.setDrawableStartVectorId(R.drawable.icon_speed);
            lastActivityRepeatsOrDistance.setText(
                    String.format(" %s km", String.valueOf(run.getDistance())));
            lastActivityRepeatsOrDistance.setDrawableStartVectorId(R.drawable.icon_distance);
            lastActivityDate.setText(
                    String.format(" %s", run.getCurrentDate()));
            lastActivityIcon.setVisibility(View.VISIBLE);
            lastActivityIcon.setImageResource(R.drawable.icon_run_color);
        }
    }

    private void bmi() {
        // initialize bmi views
        TextView bmiHeight = getView().findViewById(R.id.textview_bmi_height);
        TextView bmiWeight = getView().findViewById(R.id.textview_bmi_weight);
        TextView bmiValue = getView().findViewById(R.id.textview_bmi_bmivalue);

        // get user data from database
        if (user != null) {
            // calculate bim
            bmi = calculateBMI(user);

            // set text in textviews
            bmiHeight.setText(String.format(" %s m", String.valueOf(Util.getHeightInMeters(user.getSize()))));
            bmiWeight.setText(String.format(" %s kg", String.valueOf(user.getWeight())));
            bmiValue.setText(String.format("BMI: %.2f", bmi));
        }
    }

    private double calculateBMI(User user) {
        return user.getWeight() / Math.pow(Util.getHeightInMeters(user.getSize()), 2);
    }

    private void highscorePushups() {
        // initialize highscore pushups textviews
        TextView highscorePushupsCalories = getView().findViewById(R.id.textview_highscore_pushup_calories);
        TextView highscorePushupsDate = getView().findViewById(R.id.textview_highscore_pushup_date);
        TextView highscorePushupsDuration = getView().findViewById(R.id.textview_highscore_pushup_duration);
        TextView highscorePushupsPPM = getView().findViewById(R.id.textview_highscore_pushup_ppm);
        TextView highscorePushupsRepeats = getView().findViewById(R.id.textview_highscore_pushup_repeats);

        // find pushup highscore
        PushUps highscorePushups = DBQueryHelper.findHighscorePushup();

        if (highscorePushups != null) {
            // set text in textviews
            highscorePushupsCalories.setText(
                    String.format(" %s kcal", String.valueOf(highscorePushups.getCalories())));
            highscorePushupsRepeats.setText(
                    String.format(" %s Push-Up(s)", String.valueOf(highscorePushups.getRepeats())));
            highscorePushupsPPM.setText(
                    String.format(" %s P/min", String.valueOf(highscorePushups.getPushPerMin())));
            highscorePushupsDate.setText(
                    String.format(" %s", highscorePushups.getCurrentDate()));
            highscorePushupsDuration.setText(
                    String.format(" %s min", Util.getMillisAsTimeString(highscorePushups.getDurationInMillis())));
        }
    }

    private void highscoreSquats() {
        // initialize highscore pushups textviews
        TextView highscoreSquatsCalories = getView().findViewById(R.id.textview_highscore_squats_calories);
        TextView highscoreSquatsDate = getView().findViewById(R.id.textview_highscore_squats_date);
        TextView highscoreSquatsDuration = getView().findViewById(R.id.textview_highscore_squats_duration);
        TextView highscoreSquatsSPM = getView().findViewById(R.id.textview_highscore_squats_spm);
        TextView highscoreSquatsRepeats = getView().findViewById(R.id.textview_highscore_squats_repeats);

        // find pushup highscore
        Squat highscoreSquats = DBQueryHelper.findHighscoreSquat();

        if (highscoreSquats != null) {
            // set text in textviews
            highscoreSquatsCalories.setText(
                    String.format(" %s kcal", String.valueOf(highscoreSquats.getCalories())));
            highscoreSquatsRepeats.setText(
                    String.format(" %s Squat(s)", String.valueOf(highscoreSquats.getRepeats())));
            highscoreSquatsSPM.setText(
                    String.format(" %s S/min", String.valueOf(highscoreSquats.getSquatPerMin())));
            highscoreSquatsDate.setText(
                    String.format(" %s", highscoreSquats.getCurrentDate()));
            highscoreSquatsDuration.setText(
                    String.format(" %s min", Util.getMillisAsTimeString(highscoreSquats.getDurationInMillis())));
        }
    }

    private void highscoreRun() {

        // initialize highscore run
        TextView highscoreRunCalories = getView().findViewById(R.id.textview_highscore_run_calories);
        TextView highscoreRunDate = getView().findViewById(R.id.textview_highscore_run_date);
        TextView highscoreRunDuration = getView().findViewById(R.id.textview_highscore_run_duration);
        TextView highscoreRunSpeed = getView().findViewById(R.id.textview_highscore_run_speed);
        TextView highscoreRunDistance = getView().findViewById(R.id.textview_highscore_run_distance);

        Run highScoreRun = DBQueryHelper.findHighScoreRun();

        if (highScoreRun != null) {
            highscoreRunCalories.setText(
                    String.format(" %s kcal", String.valueOf(highScoreRun.getCalories()))
            );
            highscoreRunDate.setText(
                    String.format(" %s", String.valueOf(highScoreRun.getCurrentDate()))
            );
            highscoreRunDuration.setText(
                    String.format(" %s min", String.valueOf(highScoreRun.getDurationInMillis()))
            );
            highscoreRunDistance.setText(
                    String.format(" %s m", String.valueOf(highScoreRun.getDistance()))
            );
            highscoreRunSpeed.setText(
                    String.format(" %s km/h", String.valueOf(highScoreRun.getSpeed()))
            );
        }
    }

    // Share stuff to Facebook
    private void shareBMIToFacebook() {

        shareBMIButton = getView().findViewById(R.id.button_share_bmi);
        shareBMIButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (user.getGender() != "männlich") {
                    genderUrlFormat = 0;
                }

                String urlToBmiCalculator = "https://de.smartbmicalculator.com/ergebnis.html?unit=0&hc=" + user.getSize() + "&wk=" + user.getWeight() + "&us=" + genderUrlFormat + "&ua=" + user.getAge() + "&gk=";
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse(urlToBmiCalculator))
                        .setQuote(user.getName() + " BMI: " + Double.toString(Math.rint(bmi * 100) / 100))
                        .build();

                ShareDialog.show(getActivity(), content);
                shareBMIButton.setColor(Color.parseColor("#DDDDDD"));
            }
        });

    }

    private void shareHighScorePushUpsToFacebook() {
        shareHighScorePushUpsButton = getView().findViewById(R.id.button_share_highscore_pushups);
        highscorePushupsCardview = getView().findViewById(R.id.cardview_highscore_pushups);

        shareHighScorePushUpsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = viewToBitmap(highscorePushupsCardview);

                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(bitmap)
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();

                ShareDialog.show(getActivity(), content);
                shareHighScorePushUpsButton.setColor(Color.parseColor("#DDDDDD"));
            }
        });
    }

    private void shareHighScoreSquatToFacebook() {
        shareHighScoreSquatsButton = getView().findViewById(R.id.button_share_highscore_squats);
        highscoreSquatsCardview = getView().findViewById(R.id.cardview_highscore_squats);

        shareHighScoreSquatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = viewToBitmap(highscoreSquatsCardview);

                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(bitmap)
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();

                ShareDialog.show(getActivity(), content);
                shareHighScoreSquatsButton.setColor(Color.parseColor("#DDDDDD"));
            }
        });
    }

    private void shareHighScoreRunToFacebook() {
        shareHighScoreRunButton = getView().findViewById(R.id.button_share_highscore_run);
        highscoreRunCardview = getView().findViewById(R.id.cardview_highscore_run);

        shareHighScoreRunButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = viewToBitmap(highscoreRunCardview);

                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(bitmap)
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();

                ShareDialog.show(getActivity(), content);
                shareHighScoreRunButton.setColor(Color.parseColor("#DDDDDD"));
            }
        });
    }

    private void shareLastActivityToFacebook() {

        shareLastActivityButton = getView().findViewById(R.id.button_share_last_activity);
        lastActivityCardview = getView().findViewById(R.id.cardview_last_activity);

        shareLastActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = viewToBitmap(lastActivityCardview);

                SharePhoto photo = new SharePhoto.Builder()
                        .setBitmap(bitmap)
                        .build();
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();

                ShareDialog.show(getActivity(), content);
                shareLastActivityButton.setColor(Color.parseColor("#DDDDDD"));
            }
        });
    }

    public Bitmap viewToBitmap(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Set title bar
        ((MainActivity) getActivity())
                .setActionBarTitle(getString(R.string.home));
    }
}
