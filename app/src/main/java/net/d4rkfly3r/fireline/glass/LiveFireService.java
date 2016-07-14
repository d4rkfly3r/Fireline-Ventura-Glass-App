package net.d4rkfly3r.fireline.glass;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link Service} that publishes a {@link LiveCard} in the timeline.
 */
public class LiveFireService extends Service {

    private static final String LIVE_CARD_TAG = "LiveFireService";

    private LiveCard mLiveCard;
    private RemoteViews mRemoteViews;
    private AtomicInteger counter = new AtomicInteger(0);
    ScheduledExecutorService mScheduler = Executors.newSingleThreadScheduledExecutor();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            mRemoteViews = new RemoteViews(getPackageName(), R.layout.live_fire);
            mLiveCard.setViews(mRemoteViews);

            startWatcher();

            // Display the options menu when the live card is tapped.
            Intent menuIntent = new Intent(this, LiveCardMenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.publish(PublishMode.REVEAL);
        } else {
            startWatcher();
            mLiveCard.navigate();
        }
        return START_STICKY;
    }

    private void startWatcher() {
        mScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                mRemoteViews.setTextViewText(R.id.main_content, "Loop #" + counter.get());
                System.err.println("Loop #" + counter.getAndIncrement());
                mLiveCard.setViews(mRemoteViews);
            }
        }, 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }
}
