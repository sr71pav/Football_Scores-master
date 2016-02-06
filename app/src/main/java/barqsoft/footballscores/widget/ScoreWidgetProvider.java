package barqsoft.footballscores.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import barqsoft.footballscores.scoresAdapter;

/**
 * Created by johnpavlicek on 1/18/16.
 */
public class ScoreWidgetProvider extends AppWidgetProvider
{

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, ScoreWidgetIntentService.class));
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
        if (scoresAdapter.ACTION_DATA_UPDATED.equals(intent.getAction()))
        {
            context.startService(new Intent(context, ScoreWidgetIntentService.class));
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, ScoreWidgetIntentService.class));
    }
}
