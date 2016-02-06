package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;

/**
 * Created by johnpavlicek on 1/18/16.
 */
public class ScoreWidgetIntentService extends IntentService
{
    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL
    };
    // these indices must match the projection
    private static final int INDEX_DATE = 0;
    private static final int INDEX_HOME = 1;
    private static final int INDEX_HOME_GOALS = 2;
    private static final int INDEX_AWAY = 3;
    private static final int INDEX_AWAY_GOALS = 4;

    public ScoreWidgetIntentService()
    {
        super("ScoreWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                ScoreWidgetProvider.class));


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-DD");
        Date date = new Date();

        String dateStr = simpleDateFormat.format(date);

        // Get today's data from the ContentProvider
        Uri scoreWithDateUri = DatabaseContract.scores_table.buildScoreWithDate();
        Cursor data = getContentResolver().query(scoreWithDateUri, SCORE_COLUMNS, null,
                new String[] {dateStr}, DatabaseContract.scores_table.DATE_COL + " DESC");
        if (data == null)
        {
            return;
        }
        if (!data.moveToFirst())
        {
            data.close();
            return;
        }

        String homeTeam = data.getString(INDEX_HOME);
        String awayTeam = data.getString(INDEX_AWAY);
        String homeScore = data.getString(INDEX_HOME_GOALS);
        String awayScore = data.getString(INDEX_AWAY_GOALS);

        data.close();

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds)
        {

            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);

            //Add default dimensions
            int defaultWidth = getResources().getDimensionPixelSize(R.dimen.widget_score_default_width);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_score_default_width);
            int layoutId = R.layout.widget_score_view;

            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            views.setTextViewText(R.id.widget_home_team, homeTeam);
            views.setTextViewText(R.id.widget_away_team, awayTeam);
            views.setTextViewText(R.id.widget_home_score, homeScore);
            views.setTextViewText(R.id.widget_away_score, awayScore);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
        {
            return getResources().getDimensionPixelSize(R.dimen.widget_score_default_width);
        }
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId)
    {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH))
        {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return getResources().getDimensionPixelSize(R.dimen.widget_score_default_width);
    }
}