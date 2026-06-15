package cz.vanama.courtflow.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/** Broadcast receiver that hosts [FavoriteTeamWidget] on the home screen. */
class FavoriteTeamWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = FavoriteTeamWidget()
}
