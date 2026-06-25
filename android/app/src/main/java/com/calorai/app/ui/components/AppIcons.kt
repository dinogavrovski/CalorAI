package com.calorai.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AppIcons {

    val Home: ImageVector get() = ImageVector.Builder(
        name = "Home", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(3f, 10.5f); lineTo(12f, 3f); lineTo(21f, 10.5f)
            lineTo(21f, 20f); curveTo(21f, 20.55f, 20.55f, 21f, 20f, 21f)
            lineTo(15f, 21f); lineTo(15f, 15f); lineTo(9f, 15f); lineTo(9f, 21f)
            lineTo(4f, 21f); curveTo(3.45f, 21f, 3f, 20.55f, 3f, 20f); close()
        }
    }.build()

    val Friends: ImageVector get() = ImageVector.Builder(
        name = "Friends", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(9f, 8f); arcTo(3.2f, 3.2f, 0f, false, false, 9f, 14.4f)
            arcTo(3.2f, 3.2f, 0f, false, false, 9f, 8f); close()
        }
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(2f, 20f); curveTo(2f, 16.686f, 5.134f, 14f, 9f, 14f)
            curveTo(12.866f, 14f, 16f, 16.686f, 16f, 20f)
        }
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(17.5f, 8f); arcTo(2.4f, 2.4f, 0f, false, false, 17.5f, 12.8f)
            arcTo(2.4f, 2.4f, 0f, false, false, 17.5f, 8f); close()
        }
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(20f, 20f); curveTo(20f, 17.8f, 18.3f, 16f, 16.5f, 15.3f)
        }
    }.build()

    val BarChart: ImageVector get() = ImageVector.Builder(
        name = "BarChart", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(3f, 12f); lineTo(3f, 21f)
            arcToRelative(1.2f, 1.2f, 0f, false, false, 1.2f, 0f)
            lineTo(7f, 21f); lineTo(7f, 12f); arcToRelative(1.2f, 1.2f, 0f, false, false, -1.2f, 0f); close()
        }
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(10f, 7f); lineTo(10f, 21f)
            arcToRelative(1.2f, 1.2f, 0f, false, false, 1.2f, 0f)
            lineTo(14f, 21f); lineTo(14f, 7f); arcToRelative(1.2f, 1.2f, 0f, false, false, -1.2f, 0f); close()
        }
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(17f, 3f); lineTo(17f, 21f)
            arcToRelative(1.2f, 1.2f, 0f, false, false, 1.2f, 0f)
            lineTo(21f, 21f); lineTo(21f, 3f); arcToRelative(1.2f, 1.2f, 0f, false, false, -1.2f, 0f); close()
        }
    }.build()

    val Profile: ImageVector get() = ImageVector.Builder(
        name = "Profile", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        // Head: cx=12, cy=7.5, r=3 — ends at y=10.5, clear gap before body at y=12.5
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(15f, 7.5f)
            arcTo(3f, 3f, 0f, false, true, 9f, 7.5f)
            arcTo(3f, 3f, 0f, false, true, 15f, 7.5f)
            close()
        }
        // Body: shoulders arc starting at y=12.5, well below head bottom (y=10.5)
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(4f, 20.5f)
            curveTo(4f, 16.5f, 7.582f, 13.5f, 12f, 13.5f)
            curveTo(16.418f, 13.5f, 20f, 16.5f, 20f, 20.5f)
        }
    }.build()

    val Add: ImageVector get() = ImageVector.Builder(
        name = "Add", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 2.2f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(12f, 5f); lineTo(12f, 19f)
            moveTo(5f, 12f); lineTo(19f, 12f)
        }
    }.build()

    val Flame: ImageVector get() = ImageVector.Builder(
        name = "Flame", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color(0xFFFF5C00)), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(12f, 2f)
            curveTo(12f, 2f, 14f, 6f, 13f, 9f)
            curveTo(15f, 7f, 16f, 5f, 16f, 5f)
            curveTo(16f, 5f, 19f, 9f, 18f, 14f)
            curveTo(17.3f, 17.5f, 14.8f, 20f, 12f, 20f)
            curveTo(9.2f, 20f, 6.7f, 17.5f, 6f, 14f)
            curveTo(5f, 9f, 9f, 5f, 9f, 5f)
            curveTo(9f, 5f, 9.5f, 8f, 11f, 9f)
            curveTo(10f, 6f, 12f, 2f, 12f, 2f); close()
        }
        path(
            stroke = SolidColor(Color(0xFFFF5C00)), strokeLineWidth = 1.4f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(12f, 20f)
            curveTo(10.5f, 20f, 9.5f, 18.5f, 10f, 17f)
            curveTo(10.5f, 15.5f, 12f, 15f, 12f, 15f)
            curveTo(12f, 15f, 13.5f, 15.5f, 14f, 17f)
            curveTo(14.5f, 18.5f, 13.5f, 20f, 12f, 20f); close()
        }
    }.build()

    val Bell: ImageVector get() = ImageVector.Builder(
        name = "Bell", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(6f, 10f)
            curveTo(6f, 7.24f, 8.24f, 5f, 11f, 5f)
            lineTo(13f, 5f)
            curveTo(15.76f, 5f, 18f, 7.24f, 18f, 10f)
            lineTo(18f, 15f); lineTo(20f, 17f); lineTo(4f, 17f); lineTo(6f, 15f); close()
        }
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(10f, 17f)
            curveTo(10f, 18.105f, 10.895f, 19f, 12f, 19f)
            curveTo(13.105f, 19f, 14f, 18.105f, 14f, 17f)
        }
    }.build()

    val Energy: ImageVector get() = ImageVector.Builder(
        name = "Energy", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color(0xFFFF5C00)), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(15f, 3f); lineTo(7f, 15f); lineTo(13f, 15f); lineTo(11f, 23f)
            lineTo(19f, 11f); lineTo(13f, 11f); close()
        }
    }.build()

    val Bowl: ImageVector get() = ImageVector.Builder(
        name = "Bowl", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 26f, viewportHeight = 26f
    ).apply {
        path(
            stroke = SolidColor(Color(0xFFFF5C00)), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(4f, 11f); lineTo(22f, 11f)
            curveTo(22f, 17f, 17.523f, 21f, 13f, 21f)
            curveTo(8.477f, 21f, 4f, 17f, 4f, 11f); close()
        }
        path(
            stroke = SolidColor(Color(0xFFFF5C00)), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(2f, 11f); lineTo(24f, 11f)
        }
        path(
            stroke = SolidColor(Color(0xFFFF5C00)), strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(9f, 8f); curveTo(9f, 8f, 10f, 6f, 13f, 6f); curveTo(16f, 6f, 17f, 8f, 17f, 8f)
        }
    }.build()

    val Steak: ImageVector get() = ImageVector.Builder(
        name = "Steak", defaultWidth = 24.dp, defaultHeight = 16.dp,
        viewportWidth = 64f, viewportHeight = 44f
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.8f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(14f, 6f)
            curveTo(14f, 6f, 8f, 6f, 6f, 11f)
            curveTo(4f, 16f, 6f, 26f, 8f, 30f)
            curveTo(10f, 34f, 14f, 38f, 22f, 38f)
            curveTo(30f, 38f, 48f, 38f, 52f, 36f)
            curveTo(56f, 34f, 58f, 30f, 58f, 24f)
            curveTo(58f, 18f, 55f, 12f, 50f, 10f)
            curveTo(46f, 8f, 42f, 8f, 38f, 9f)
            curveTo(34f, 10f, 30f, 6f, 24f, 5f)
            curveTo(20f, 4f, 16f, 5f, 14f, 6f); close()
        }
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.4f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(15f, 11f)
            curveTo(15f, 11f, 10f, 11f, 9f, 15f)
            curveTo(8f, 19f, 9f, 27f, 11f, 30f)
            curveTo(13f, 33f, 16f, 35f, 22f, 35f)
            curveTo(28f, 35f, 46f, 35f, 49f, 33f)
            curveTo(52f, 31f, 54f, 28f, 54f, 23f)
            curveTo(54f, 18f, 51f, 14f, 47f, 12f)
            curveTo(43.5f, 10.5f, 40f, 11f, 36f, 12f)
            curveTo(32f, 13f, 28f, 10f, 23f, 9.5f)
            curveTo(19.5f, 9f, 16.5f, 10f, 15f, 11f); close()
        }
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent),
            pathFillType = PathFillType.NonZero
        ) {
            // circle cx=43,cy=24,r=5
            moveTo(48f, 24f)
            arcTo(5f, 5f, 0f, false, true, 38f, 24f)
            arcTo(5f, 5f, 0f, false, true, 48f, 24f)
            close()
        }
    }.build()

    val Wheat: ImageVector get() = ImageVector.Builder(
        name = "Wheat", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 32f, viewportHeight = 32f
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(16f, 27f); lineTo(16f, 11f)
        }
        // tip grain: cx=16,cy=8,rx=2,ry=3.5
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.5f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(18f, 8f)
            arcTo(2f, 3.5f, 0f, false, true, 14f, 8f)
            arcTo(2f, 3.5f, 0f, false, true, 18f, 8f)
            close()
        }
        // left grain top: cx=12.5,cy=14.5,rx=1.8,ry=3.2
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.4f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(14.3f, 14.5f)
            arcTo(1.8f, 3.2f, 0f, false, true, 10.7f, 14.5f)
            arcTo(1.8f, 3.2f, 0f, false, true, 14.3f, 14.5f)
            close()
        }
        // left grain bottom: cx=12,cy=19.5,rx=1.8,ry=3.2
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.4f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(13.8f, 19.5f)
            arcTo(1.8f, 3.2f, 0f, false, true, 10.2f, 19.5f)
            arcTo(1.8f, 3.2f, 0f, false, true, 13.8f, 19.5f)
            close()
        }
        // right grain top: cx=19.5,cy=14.5,rx=1.8,ry=3.2
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.4f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(21.3f, 14.5f)
            arcTo(1.8f, 3.2f, 0f, false, true, 17.7f, 14.5f)
            arcTo(1.8f, 3.2f, 0f, false, true, 21.3f, 14.5f)
            close()
        }
        // right grain bottom: cx=20,cy=19.5,rx=1.8,ry=3.2
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.4f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(21.8f, 19.5f)
            arcTo(1.8f, 3.2f, 0f, false, true, 18.2f, 19.5f)
            arcTo(1.8f, 3.2f, 0f, false, true, 21.8f, 19.5f)
            close()
        }
    }.build()

    val Drop: ImageVector get() = ImageVector.Builder(
        name = "Drop", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 32f, viewportHeight = 32f
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(16f, 5f)
            curveTo(16f, 5f, 8f, 14f, 8f, 20f)
            curveTo(8f, 24.418f, 11.582f, 28f, 16f, 28f)
            curveTo(20.418f, 28f, 24f, 24.418f, 24f, 20f)
            curveTo(24f, 14f, 16f, 5f, 16f, 5f); close()
        }
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.4f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(11.5f, 22f); curveTo(11.5f, 22f, 12.5f, 25f, 16f, 25f)
        }
    }.build()

    val MoreDots: ImageVector get() = ImageVector.Builder(
        name = "MoreDots", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        // dot 1: cx=5,cy=12,r=1.5
        path(fill = SolidColor(Color.White), pathFillType = PathFillType.NonZero) {
            moveTo(6.5f, 12f)
            arcTo(1.5f, 1.5f, 0f, false, true, 3.5f, 12f)
            arcTo(1.5f, 1.5f, 0f, false, true, 6.5f, 12f)
            close()
        }
        // dot 2: cx=12,cy=12,r=1.5
        path(fill = SolidColor(Color.White), pathFillType = PathFillType.NonZero) {
            moveTo(13.5f, 12f)
            arcTo(1.5f, 1.5f, 0f, false, true, 10.5f, 12f)
            arcTo(1.5f, 1.5f, 0f, false, true, 13.5f, 12f)
            close()
        }
        // dot 3: cx=19,cy=12,r=1.5
        path(fill = SolidColor(Color.White), pathFillType = PathFillType.NonZero) {
            moveTo(20.5f, 12f)
            arcTo(1.5f, 1.5f, 0f, false, true, 17.5f, 12f)
            arcTo(1.5f, 1.5f, 0f, false, true, 20.5f, 12f)
            close()
        }
    }.build()

    val Back: ImageVector get() = ImageVector.Builder(
        name = "Back", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(19f, 12f); lineTo(5f, 12f)
            moveTo(5f, 12f); lineTo(11f, 6f)
            moveTo(5f, 12f); lineTo(11f, 18f)
        }
    }.build()

    val Heart: ImageVector get() = ImageVector.Builder(
        name = "Heart", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(12f, 20f)
            curveTo(12f, 20f, 4f, 15f, 4f, 9f)
            curveTo(4f, 6.79f, 5.79f, 5f, 8f, 5f)
            curveTo(9.5f, 5f, 10.8f, 5.8f, 12f, 7f)
            curveTo(13.2f, 5.8f, 14.5f, 5f, 16f, 5f)
            curveTo(18.21f, 5f, 20f, 6.79f, 20f, 9f)
            curveTo(20f, 15f, 12f, 20f, 12f, 20f); close()
        }
    }.build()

    val Comment: ImageVector get() = ImageVector.Builder(
        name = "Comment", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(4f, 4f); lineTo(20f, 4f)
            curveTo(20.55f, 4f, 21f, 4.45f, 21f, 5f)
            lineTo(21f, 15f)
            curveTo(21f, 15.55f, 20.55f, 16f, 20f, 16f)
            lineTo(7f, 16f); lineTo(3f, 20f); lineTo(3f, 5f)
            curveTo(3f, 4.45f, 3.45f, 4f, 4f, 4f); close()
        }
    }.build()

    val Share: ImageVector get() = ImageVector.Builder(
        name = "Share", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(22f, 2f); lineTo(11f, 13f)
            moveTo(22f, 2f); lineTo(15f, 22f); lineTo(11f, 13f); lineTo(2f, 9f); close()
        }
    }.build()

    val Camera: ImageVector get() = ImageVector.Builder(
        name = "Camera", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(3f, 7f)
            lineTo(3f, 21f); arcToRelative(2.5f, 2.5f, 0f, false, false, 2.5f, 0f)
            lineTo(21f, 21f); arcToRelative(2.5f, 2.5f, 0f, false, false, 0f, -2.5f)
            lineTo(21f, 7f); arcToRelative(2.5f, 2.5f, 0f, false, false, -2.5f, 0f)
            lineTo(5.5f, 7f); arcToRelative(2.5f, 2.5f, 0f, false, false, -2.5f, 0f); close()
        }
        // lens: cx=12,cy=14,r=3.5
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent),
            pathFillType = PathFillType.NonZero
        ) {
            moveTo(15.5f, 14f)
            arcTo(3.5f, 3.5f, 0f, false, true, 8.5f, 14f)
            arcTo(3.5f, 3.5f, 0f, false, true, 15.5f, 14f)
            close()
        }
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(9f, 7f); lineTo(10.5f, 4f); lineTo(13.5f, 4f); lineTo(15f, 7f)
        }
    }.build()

    val Edit: ImageVector get() = ImageVector.Builder(
        name = "Edit", defaultWidth = 24.dp, defaultHeight = 24.dp,
        viewportWidth = 24f, viewportHeight = 24f
    ).apply {
        path(
            stroke = SolidColor(Color.White), strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round,
            fillAlpha = 0f, fill = SolidColor(Color.Transparent)
        ) {
            moveTo(16f, 4f); lineTo(20f, 8f); lineTo(8f, 20f); lineTo(4f, 20f); lineTo(4f, 16f); close()
            moveTo(13f, 7f); lineTo(17f, 11f)
        }
    }.build()
}
