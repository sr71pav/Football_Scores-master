package barqsoft.footballscores.widget;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import barqsoft.footballscores.DatabaseContract;
import barqsoft.footballscores.R;

/**
 * Created by johnpavlicek on 2/6/16.
 */
public class DetailWidgetRemoteViewsService extends RemoteViewsService
{
    private static final String NO_SCORE = "-";

    private static final String[] SCORE_COLUMNS = {
            DatabaseContract.scores_table.DATE_COL,
            DatabaseContract.scores_table.HOME_COL,
            DatabaseContract.scores_table.HOME_GOALS_COL,
            DatabaseContract.scores_table.AWAY_COL,
            DatabaseContract.scores_table.AWAY_GOALS_COL,
            DatabaseContract.scores_table.MATCH_ID
    };
    // these indices must match the projection
    private static final int INDEX_DATE = 0;
    private static final int INDEX_HOME = 1;
    private static final int INDEX_HOME_GOALS = 2;
    private static final int INDEX_AWAY = 3;
    private static final int INDEX_AWAY_GOALS = 4;
    private static final int INDEX_MATCH_ID = 5;

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent)
    {
        return new RemoteViewsFactory()
        {
            private Cursor data = null;
            @Override
            public void onCreate()
            {

            }

            @Override
            public void onDataSetChanged()
            {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                Uri matchesUri = DatabaseContract.BASE_CONTENT_URI;

                String sqlQry = SQLiteQueryBuilder.buildQueryString(false,
                        "TEST",
                        SCORE_COLUMNS,
                        "NOT " + DatabaseContract.scores_table.HOME_GOALS_COL + "='-1'",
                        null,
                        null,
                        DatabaseContract.scores_table.DATE_COL + " DESC",
                        null);

                Log.i("FOPOTBALL SCORES", sqlQry);

                data = getContentResolver().query(matchesUri,
                        SCORE_COLUMNS,
                        null,
                        null,
                        DatabaseContract.scores_table.DATE_COL + " DESC");
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy()
            {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount()
            {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position)
            {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                String homeTeam = data.getString(INDEX_HOME);
                String awayTeam = data.getString(INDEX_AWAY);
                int homeScore = data.getInt(INDEX_HOME_GOALS);
                int awayScore = data.getInt(INDEX_AWAY_GOALS);

                views.setTextViewText(R.id.widget_home_team, homeTeam);
                views.setTextViewText(R.id.widget_away_team, awayTeam);

                if (homeScore < 0)
                {
                    views.setTextViewText(R.id.widget_home_score, NO_SCORE);
                    views.setTextViewText(R.id.widget_away_score, NO_SCORE);
                }
                else
                {
                    views.setTextViewText(R.id.widget_home_score, String.valueOf(homeScore));
                    views.setTextViewText(R.id.widget_away_score, String.valueOf(awayScore));
                }

                return views;
            }

            @Override
            public RemoteViews getLoadingView()
            {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount()
            {
                return 1;
            }

            @Override
            public long getItemId(int position)
            {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_MATCH_ID);
                return position;
            }

            @Override
            public boolean hasStableIds()
            {
                return true;
            }
        };
    }
}
