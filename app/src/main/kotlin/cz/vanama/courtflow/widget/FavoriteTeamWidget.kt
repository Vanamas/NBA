package cz.vanama.courtflow.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import cz.vanama.courtflow.MainActivity
import cz.vanama.courtflow.R
import org.koin.core.context.GlobalContext

/**
 * Home-screen Glance widget for the favorite team's latest score. Rendering is
 * a pure function of [WidgetUiModel] produced by [WidgetDataLoader]; tapping the
 * widget opens the team detail through the existing `courtflow://team/{id}`
 * deep link (or just opens the app on [WidgetUiModel.Error]).
 */
class FavoriteTeamWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val loader = GlobalContext.get().get<WidgetDataLoader>()
        val model = loader.load()
        provideContent { WidgetBody(context, model) }
    }
}

@Composable
private fun WidgetBody(context: Context, model: WidgetUiModel) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp)
            .clickable(openIntent(context, model)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = context.getString(R.string.widget_favorite_team_title),
            style = TextStyle(color = ColorProvider(Color.Gray)),
        )
        WidgetContent(context, model)
    }
}

@Composable
private fun WidgetContent(context: Context, model: WidgetUiModel) {
    when (model) {
        is WidgetUiModel.Score -> {
            Text(text = model.scoreLine, style = TextStyle(color = ColorProvider(Color.Black)))
            Text(
                text = context.getString(R.string.widget_score_caption),
                style = TextStyle(color = ColorProvider(Color.Gray)),
            )
        }
        is WidgetUiModel.NoRecentGame ->
            Text(
                text = context.getString(R.string.widget_no_recent_game),
                style = TextStyle(color = ColorProvider(Color.Black)),
            )
        WidgetUiModel.Error ->
            Text(
                text = context.getString(R.string.widget_error),
                style = TextStyle(color = ColorProvider(Color.Black)),
            )
    }
}

/**
 * Click action: a VIEW intent to [MainActivity] carrying the `courtflow://team/{id}`
 * deep link for the resolved team, or the bare app launch on [WidgetUiModel.Error].
 * [MainActivity] already parses `intent.data` via `DeepLink.initialBackStack`.
 */
private fun openIntent(context: Context, model: WidgetUiModel) =
    actionStartActivity(
        Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = teamUri(model)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        },
    )

private fun teamUri(model: WidgetUiModel): Uri? =
    when (model) {
        is WidgetUiModel.Score -> Uri.parse("courtflow://team/${model.teamId}")
        is WidgetUiModel.NoRecentGame -> Uri.parse("courtflow://team/${model.teamId}")
        WidgetUiModel.Error -> null
    }
