package tr.xip.wanikani.cards;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;

import java.text.SimpleDateFormat;

import tr.xip.wanikani.BroadcastIntents;
import tr.xip.wanikani.R;
import tr.xip.wanikani.api.WaniKaniApi;
import tr.xip.wanikani.managers.ApiManager;
import tr.xip.wanikani.managers.OfflineDataManager;
import tr.xip.wanikani.utils.Utils;

/**
 * Created by xihsa_000 on 3/13/14.
 */
public class ReviewsCard extends Fragment {

    WaniKaniApi api;
    ApiManager apiMan;
    OfflineDataManager dataMan;
    Utils utils;

    View rootView;

    TextView mNextReview;
    TextView mNextHour;
    TextView mNextDay;

    Period period;
    PeriodFormatter formatter;

    private BroadcastReceiver mDoLoad = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new LoadTask().execute();
        }
    };

    @Override
    public void onCreate(Bundle state) {
        api = new WaniKaniApi(getActivity());
        apiMan = new ApiManager(getActivity());
        dataMan = new OfflineDataManager(getActivity());
        utils = new Utils(getActivity());
        super.onCreate(state);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mDoLoad,
                new IntentFilter(BroadcastIntents.SYNC()));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.card_reviews, null);

        mNextReview = (TextView) rootView.findViewById(R.id.card_reviews_next_review);
        mNextHour = (TextView) rootView.findViewById(R.id.card_reviews_next_hour);
        mNextDay = (TextView) rootView.findViewById(R.id.card_reviews_next_day);

        setOldValues();

        return rootView;
    }

    private void setOldValues() {
        mNextHour.setText(dataMan.getReviewsAvailableNextHour() + "");
        mNextDay.setText(dataMan.getReviewsAvailableNextDay() + "");

        if (dataMan.getReviewsAvailable() != 0) {
            mNextReview.setText(getString(R.string.card_content_reviews_available_now));
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM HH:mm");
            mNextReview.setText(sdf.format(dataMan.getNextReviewDate()));
        }
    }

    private class LoadTask extends AsyncTask<String, Void, String> {
        long nextReview;
        int nextHour;
        int nextDay;
        boolean isVacationModeActive;
        int reviewsAvailable;

        @Override
        protected String doInBackground(String... strings) {
            try {
                nextReview = apiMan.getNextReviewDate();
                nextHour = apiMan.getReviewsAvailableNextHour();
                nextDay = apiMan.getReviewsAvailableNextDay();
                isVacationModeActive = apiMan.isVacationModeActive();
                reviewsAvailable = apiMan.getReviewsAvailable();
                return "success";
            } catch (Exception e) {
                e.printStackTrace();
                return "failure";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (!isVacationModeActive) {
                if (result.equals("success")) {
                    mNextHour.setText(nextHour + "");
                    mNextDay.setText(nextDay + "");

                    if (reviewsAvailable != 0) {
                        mNextReview.setText(R.string.card_content_reviews_available_now);
                    } else {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM HH:mm");
                        mNextReview.setText(sdf.format(nextReview));
                    }

                    Intent intent = new Intent(BroadcastIntents.FINISHED_SYNC_REVIEWS_CARD());
                    intent.putExtra("action", "show");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                } else {
                    Intent intent = new Intent(BroadcastIntents.FINISHED_SYNC_REVIEWS_CARD());
                    intent.putExtra("action", "hide");
                    LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                }
            }
        }
    }
}