package com.sansim.app

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sansim.app.data.model.Countries
import com.sansim.app.data.model.PhoneNumberRecord
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private data class SimMapPoint(
    val iso: String,
    val country: String,
    val flag: String,
    val total: Int,
    val esim: Int,
    val lon: Float,
    val lat: Float
)

private data class GlobeCountry(
    val iso: String,
    val name: String,
    val rings: List<List<LonLat>>,
    val label: LonLat,
    val hitRings: List<List<LonLat>> = emptyList()
)

private data class LonLat(val lon: Float, val lat: Float)

private data class GlobeHit(
    val country: GlobeCountry,
    val record: SimMapPoint?
)

private data class GlobeFrame(val center: Offset, val radius: Float)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimMapPage(records: List<PhoneNumberRecord>, onEdit: (PhoneNumberRecord) -> Unit = {}) {
    val scheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    val countries = remember { loadGlobeCountries(context) }
    var selectedIso by remember { mutableStateOf<String?>(null) }
    var detailIso by remember { mutableStateOf<String?>(null) }
    var coverageSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val countryByIso = remember(countries) { countries.associateBy { it.iso } }
    val points = remember(records, countryByIso) {
        records.groupBy { canonicalIso(isoForMapRecord(it)) }.mapNotNull { entry ->
            val iso = canonicalIso(entry.key)
            val pos = latLonForIso(iso) ?: countryByIso[iso]?.label ?: return@mapNotNull null
            val sample = entry.value.first()
            val country = Countries.list.firstOrNull { canonicalIso(it.iso) == iso }
            val technicalEsimCount = entry.value.count { isEsimMapRecord(it) }
            val coverageCount = technicalEsimCount.takeIf { it > 0 } ?: entry.value.size
            SimMapPoint(
                iso = iso,
                country = displayNameForIso(iso, sample.countryName.ifBlank { country?.name ?: iso }),
                flag = regionFlagForIso(iso, sample.flag),
                total = entry.value.size,
                esim = coverageCount,
                lon = pos.lon,
                lat = pos.lat
            )
        }.sortedWith(compareByDescending<SimMapPoint> { it.esim }.thenByDescending { it.total })
    }
    val pointMap = remember(points) { points.associateBy { it.iso } }
    val selectedCountry = selectedIso?.let { iso -> countries.firstOrNull { it.iso == iso } }
    val selectedPoint = selectedIso?.let { pointMap[it] }
    val detailCountry = detailIso?.let { iso -> countries.firstOrNull { it.iso == iso } }
    val detailPoint = detailIso?.let { pointMap[it] }
    val esimCountryCount = remember(points) { points.count { it.esim > 0 } }
    val esimTotal = remember(points) { points.sumOf { it.esim } }
    val detailRecords = remember(records, detailIso) {
        detailIso?.let { iso -> records.filter { canonicalIso(isoForMapRecord(it)) == iso } }.orEmpty()
    }
    val coveragePoints = remember(points) { points.filter { it.esim > 0 } }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp)
            .padding(top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = scheme.surfaceContainerHigh,
            tonalElevation = 3.dp,
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(scheme.surfaceContainerLow, scheme.surfaceContainerHighest)
                        )
                    )
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    InteractiveGlobeMap(
                        countries = countries,
                        points = points,
                        selectedIso = selectedIso,
                        onSelected = { hit -> selectedIso = hit?.country?.iso },
                        modifier = Modifier.fillMaxSize().padding(10.dp)
                    )
                    if (countries.isEmpty()) {
                        Column(
                            Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("地图数据未加载", style = MaterialTheme.typography.titleMedium, color = scheme.onSurface)
                            Text("请检查本地国家边界资源", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                        }
                    }
                }
                AnimatedVisibility(
                    visible = selectedCountry != null,
                    enter = fadeIn(animationSpec = tween(180)) +
                        slideInVertically(animationSpec = tween(360, easing = FastOutSlowInEasing)) { it / 2 },
                    exit = fadeOut(animationSpec = tween(140)) +
                        slideOutVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) { it / 2 }
                ) {
                    selectedCountry?.let { country ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(142.dp)
                                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 18.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            SelectedCountryOverlay(
                                country = country,
                                point = selectedPoint,
                                modifier = Modifier.fillMaxWidth(),
                                onClick = { detailIso = country.iso }
                            )
                        }
                    }
                }
            }
        }
        MapEsimCoverageCard(
            countryCount = esimCountryCount,
            esimCount = esimTotal,
            onClick = { coverageSheetOpen = true },
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .fillMaxWidth()
        )
    }
    if (detailCountry != null && !coverageSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { detailIso = null },
            sheetState = sheetState,
            containerColor = scheme.surface,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
        ) {
            CountryNumbersSheet(
                country = detailCountry,
                point = detailPoint,
                records = detailRecords,
                onNumberClick = { record ->
                    detailIso = null
                    onEdit(record)
                }
            )
        }
    }
    if (coverageSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { coverageSheetOpen = false },
            sheetState = sheetState,
            containerColor = scheme.surface,
            tonalElevation = 6.dp,
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
        ) {
            CoverageCountriesSheet(
                points = coveragePoints,
                onCountryClick = { point ->
                    selectedIso = point.iso
                    coverageSheetOpen = false
                }
            )
        }
    }
}

@Composable
private fun MapEsimCoverageCard(
    countryCount: Int,
    esimCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = scheme.surface.copy(alpha = .92f),
        tonalElevation = 4.dp,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = .54f)),
        modifier = modifier
            .animateContentSize(animationSpec = tween(260, easing = FastOutSlowInEasing))
            .motionClickable(pressedScale = .985f) { onClick() }
    ) {
        Row(
            Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer
            ) {
                Text(
                    "eSIM",
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 9.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "eSIM 覆盖",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = scheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (countryCount == 0) "暂无 eSIM 国家/地区" else "$countryCount 个国家/地区 · $esimCount 张 eSIM",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = scheme.tertiaryContainer,
                contentColor = scheme.onTertiaryContainer
            ) {
                Text(
                    countryCount.toString(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp
                )
            }
        }
    }
}

@Composable
private fun CoverageCountriesSheet(
    points: List<SimMapPoint>,
    onCountryClick: (SimMapPoint) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(.58f)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer
            ) {
                Text(
                    "eSIM",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
            Column(Modifier.weight(1f)) {
                Text(
                    "覆盖国家/地区",
                    style = MaterialTheme.typography.headlineSmall,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (points.isEmpty()) "暂无 eSIM 国家/地区" else "${points.size} 个国家/地区",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
        }

        if (points.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("还没有记录 eSIM 覆盖国家/地区", color = scheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(points, key = { it.iso }, contentType = { "coverage-country" }) { point ->
                    CoverageCountryRow(point = point, onClick = { onCountryClick(point) })
                }
            }
        }
    }
}

@Composable
private fun CoverageCountryRow(point: SimMapPoint, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val flag = regionFlagForIso(point.iso, point.flag)
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = scheme.surfaceContainerLow,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = .72f)),
        modifier = Modifier.fillMaxWidth().motionClickable(pressedScale = .985f) { onClick() }
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer
            ) {
                Box(Modifier.width(54.dp).height(54.dp), contentAlignment = Alignment.Center) {
                    Text(
                        flag.ifBlank { point.iso },
                        fontSize = if (flag.isBlank()) 14.sp else 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    point.country,
                    style = MaterialTheme.typography.titleMedium,
                    color = scheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${point.esim} 张 eSIM · ${point.total} 张卡",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = scheme.secondaryContainer,
                contentColor = scheme.onSecondaryContainer
            ) {
                Text(
                    point.iso,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SelectedCountryOverlay(
    country: GlobeCountry,
    point: SimMapPoint?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val iso = canonicalIso(country.iso)
    val hasEsim = (point?.esim ?: 0) > 0
    val flag = regionFlagForIso(iso, point?.flag.orEmpty())
    val title = displayNameForIso(iso, country.name.ifBlank { point?.country ?: iso })
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (hasEsim) scheme.tertiaryContainer else scheme.surface.copy(alpha = .9f),
        tonalElevation = 4.dp,
        shadowElevation = 3.dp,
        modifier = modifier.fillMaxWidth().motionClickable(pressedScale = .985f) { onClick() }
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (flag.isNotBlank()) {
                Text(flag, fontSize = 28.sp, modifier = Modifier.width(44.dp), textAlign = TextAlign.Center)
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = scheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    when {
                        point == null -> "未记录 SIM"
                        hasEsim -> "${point.esim} 张 eSIM · ${point.total} 张卡"
                        else -> "${point.total} 张普通 SIM"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (hasEsim) scheme.tertiary else scheme.primaryContainer,
                contentColor = if (hasEsim) scheme.onTertiary else scheme.onPrimaryContainer
            ) {
                Text(
                    if (hasEsim) "eSIM" else iso,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CountryNumbersSheet(
    country: GlobeCountry,
    point: SimMapPoint?,
    records: List<PhoneNumberRecord>,
    onNumberClick: (PhoneNumberRecord) -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    val iso = canonicalIso(country.iso)
    val flag = regionFlagForIso(iso, point?.flag.orEmpty())
    val countryName = displayNameForIso(iso, country.name.ifBlank { point?.country ?: iso })

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(.54f)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (flag.isNotBlank()) {
                Text(flag, fontSize = 30.sp, modifier = Modifier.width(48.dp), textAlign = TextAlign.Center)
            }
            Column(Modifier.weight(1f)) {
                Text(countryName, style = MaterialTheme.typography.headlineSmall, color = scheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (records.isEmpty()) "暂无号码" else "${records.size} 个号码",
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(17.dp),
                color = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer
            ) {
                Text(iso, modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp), fontWeight = FontWeight.Bold)
            }
        }

        if (records.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("这个国家还没有保存号码", color = scheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records, key = { it.id }, contentType = { "map-number" }) { record ->
                    MapNumberRow(record) { onNumberClick(record) }
                }
            }
        }
    }
}

@Composable
private fun MapNumberRow(record: PhoneNumberRecord, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val numberText = if (record.number.isBlank()) "未填写号码" else "${record.countryCode} ${formatNumber(record.number)}"
    val carrier = record.operator.ifBlank { record.countryName.ifBlank { record.countryCode } }
    val days = daysUntil(record.expireDate)
    val expireText = when {
        record.longTerm -> "长期号码"
        days == null -> "到期 ${record.expireDate.ifBlank { "未设置" }}"
        days < 0 -> "已过期 ${-days} 天"
        days == 0L -> "今天到期"
        else -> "剩余 ${days} 天"
    }
    val typeText = if (isEsimMapRecord(record)) "eSIM" else "普通 SIM"
    val meta = listOf(
        typeText,
        expireText,
        record.balance.ifBlank { null }
    ).filterNotNull().joinToString(" · ")

    val rowShape = RoundedCornerShape(24.dp)
    Surface(
        shape = rowShape,
        color = scheme.surfaceContainerLow,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, scheme.outlineVariant.copy(alpha = .72f)),
        modifier = Modifier.fillMaxWidth().motionClickable(pressedScale = .985f) { onClick() }
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = scheme.primaryContainer,
                contentColor = scheme.onPrimaryContainer
            ) {
                Box(Modifier.width(56.dp).height(56.dp), contentAlignment = Alignment.Center) {
                    Text(record.flag.ifBlank { "SIM" }, fontSize = if (record.flag.isBlank()) 13.sp else 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(carrier, style = MaterialTheme.typography.titleMedium, color = scheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(numberText, style = MaterialTheme.typography.bodyLarge, color = scheme.onSurface, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(meta, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Surface(
                shape = RoundedCornerShape(15.dp),
                color = scheme.secondaryContainer,
                contentColor = scheme.onSecondaryContainer
            ) {
                Text("编辑", modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InteractiveGlobeMap(
    countries: List<GlobeCountry>,
    points: List<SimMapPoint>,
    selectedIso: String?,
    onSelected: (GlobeHit?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val pointMap = remember(points) { points.associateBy { it.iso } }
    val countryMap = remember(countries) { countries.associateBy { it.iso } }
    val recordedIso = remember(points) { points.map { it.iso }.toSet() }
    val esimIso = remember(points) { points.filter { it.esim > 0 }.map { it.iso }.toSet() }
    var centerLon by remember { mutableStateOf(104f) }
    var centerLat by remember { mutableStateOf(22f) }
    var zoom by remember { mutableStateOf(1.12f) }
    // No infinite animation: continuous pulse forced full-canvas redraw and caused lag.
    var globeSize by remember { mutableStateOf(IntSize.Zero) }

    androidx.compose.runtime.LaunchedEffect(selectedIso, pointMap, countryMap) {
        val focus = selectedIso?.let { iso ->
            pointMap[iso]?.let { LonLat(it.lon, it.lat) } ?: countryMap[iso]?.label
        }
        if (focus != null) {
            centerLon += shortestLonDelta(centerLon, focus.lon)
            centerLat = focus.lat.coerceIn(-58f, 58f)
            // Zoom in further when focusing a country
            zoom = zoom.coerceAtLeast(2.35f)
        }
    }

    Box(modifier) {
        Canvas(
            Modifier
                .fillMaxSize()
                .onSizeChanged { globeSize = it }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downPosition = down.position
                        var transformed = false
                        var maxPointers = 1
                        var accumulatedPan = Offset.Zero
                        var accumulatedZoom = 1f
                        do {
                            val event = awaitPointerEvent()
                            val pressedCount = event.changes.count { it.pressed }
                            maxPointers = maxOf(maxPointers, pressedCount)
                            if (pressedCount > 0) {
                                val panChange = event.calculatePan()
                                val zoomChange = event.calculateZoom()
                                accumulatedPan += panChange
                                accumulatedZoom *= zoomChange
                                val passedSlop = accumulatedPan.getDistance() > viewConfiguration.touchSlop ||
                                    abs(1f - accumulatedZoom) > .02f ||
                                    maxPointers > 1
                                if (passedSlop) {
                                    if (!transformed) onSelected(null)
                                    transformed = true
                                    val nextZoom = clampGlobeZoom(zoom * zoomChange)
                                    // Pan slows at high zoom so fine control remains usable
                                    val zoomForPan = nextZoom.coerceIn(0.9f, 3.2f)
                                    centerLon = normalizeLon(centerLon - panChange.x * (.22f / zoomForPan))
                                    centerLat = (centerLat + panChange.y * (.15f / zoomForPan)).coerceIn(-64f, 64f)
                                    zoom = nextZoom
                                    event.changes.forEach { it.consume() }
                                }
                            }
                        } while (event.changes.any { it.pressed })
                        if (!transformed && maxPointers == 1) {
                            val lonLat = screenToLonLat(downPosition, globeSize, centerLon, centerLat, zoom)
                            val country = lonLat?.let { pickCountry(countries, it) }
                            onSelected(country?.let { GlobeHit(it, pointMap[it.iso]) })
                        }
                    }
                }
        ) {
            val frame = globeFrame(size.width, size.height, zoom)
            // Draw at most a few large outer rings per country (already simplified on load)
            val projectedCountries = countries.mapNotNull { country ->
                val paths = country.rings.mapNotNull { ring ->
                    buildProjectedPath(ring, frame, centerLon, centerLat)
                }
                if (paths.isEmpty()) null
                else {
                    val depth = projectLonLat(country.label.lon, country.label.lat, frame, centerLon, centerLat)?.second ?: 0f
                    Triple(country, paths, depth)
                }
            }.sortedBy { it.third }

            drawCircle(
                color = Color.Black.copy(alpha = .12f),
                radius = frame.radius * 1.01f,
                center = Offset(frame.center.x, frame.center.y + frame.radius * .1f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        scheme.primaryContainer.copy(alpha = 1f),
                        scheme.secondaryContainer.copy(alpha = .92f),
                        scheme.primary.copy(alpha = .16f),
                        scheme.surfaceVariant.copy(alpha = .94f)
                    ),
                    center = Offset(frame.center.x - frame.radius * .36f, frame.center.y - frame.radius * .42f),
                    radius = frame.radius * 1.35f
                ),
                radius = frame.radius,
                center = frame.center
            )

            // Lighter graticule when zoomed out
            if (zoom < 2.4f) {
                drawGraticule(frame, centerLon, centerLat, scheme.outline.copy(alpha = .28f), coarse = zoom < 1.4f)
            }

            projectedCountries.forEach { (country, paths, depth) ->
                val iso = country.iso
                val hasEsim = iso in esimIso
                val hasRecord = iso in recordedIso
                val selected = selectedIso == iso
                val depthAlpha = (.38f + depth.coerceIn(0f, 1f) * .5f)
                val fill = when {
                    hasEsim -> scheme.tertiary.copy(alpha = .78f * depthAlpha)
                    hasRecord -> scheme.primary.copy(alpha = .28f * depthAlpha)
                    else -> scheme.surface.copy(alpha = .14f * depthAlpha)
                }
                val stroke = when {
                    selected -> scheme.onSurface
                    hasEsim -> scheme.onTertiaryContainer.copy(alpha = .94f)
                    hasRecord -> scheme.primary.copy(alpha = .82f)
                    else -> scheme.outline.copy(alpha = .48f)
                }
                paths.forEach { path ->
                    drawPath(path, fill)
                    drawPath(
                        path,
                        stroke,
                        style = Stroke(
                            width = when {
                                selected -> 3.2f
                                hasEsim -> 2.5f
                                hasRecord -> 1.9f
                                else -> 1.0f
                            }
                        )
                    )
                }
            }

            // Static markers (no pulse) — only for recorded / eSIM countries
            points.forEach { point ->
                val projected = projectLonLat(point.lon, point.lat, frame, centerLon, centerLat) ?: return@forEach
                val hot = point.esim > 0
                val dotRadius = if (hot) (7f + point.esim * 1.6f).coerceAtMost(16f) else 5.5f
                if (hot) {
                    drawCircle(scheme.tertiaryContainer.copy(alpha = .5f), dotRadius + 6f, projected.first)
                    drawCircle(scheme.tertiary, dotRadius, projected.first)
                    drawCircle(Color.White.copy(alpha = .9f), dotRadius * .4f, projected.first)
                } else {
                    drawCircle(scheme.primary.copy(alpha = .55f), dotRadius, projected.first)
                    drawCircle(Color.White.copy(alpha = .85f), dotRadius * .38f, projected.first)
                }
            }

            drawCircle(
                color = scheme.outline.copy(alpha = .22f),
                radius = frame.radius,
                center = frame.center,
                style = Stroke(width = 2f)
            )
        }

    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGraticule(
    frame: GlobeFrame,
    centerLon: Float,
    centerLat: Float,
    color: Color,
    coarse: Boolean = true
) {
    fun drawProjectedPath(samples: List<LonLat>, alpha: Float, width: Float) {
        var path = Path()
        var active = false
        samples.forEach { sample ->
            val projected = projectLonLat(sample.lon, sample.lat, frame, centerLon, centerLat)
            if (projected == null) {
                if (active) drawPath(path, color.copy(alpha = color.alpha * alpha), style = Stroke(width = width))
                path = Path()
                active = false
            } else {
                val point = projected.first
                if (!active) {
                    path.moveTo(point.x, point.y)
                    active = true
                } else {
                    path.lineTo(point.x, point.y)
                }
            }
        }
        if (active) drawPath(path, color.copy(alpha = color.alpha * alpha), style = Stroke(width = width))
    }

    val latStep = if (coarse) 45 else 30
    val lonStep = if (coarse) 45 else 30
    val sampleStep = if (coarse) 10 else 6
    (-60..60 step latStep).forEach { lat ->
        drawProjectedPath((-180..180 step sampleStep).map { LonLat(it.toFloat(), lat.toFloat()) }, .85f, 1f)
    }
    (-180..180 step lonStep).forEach { lon ->
        drawProjectedPath((-70..70 step sampleStep).map { LonLat(lon.toFloat(), it.toFloat()) }, .7f, .9f)
    }
}

private fun globeFrame(width: Float, height: Float, zoom: Float = 1f): GlobeFrame {
    val radius = min(width * .49f, height * .42f) * zoom
    return GlobeFrame(Offset(width * .5f, height * .52f), radius)
}

private fun projectLonLat(
    lon: Float,
    lat: Float,
    frame: GlobeFrame,
    centerLon: Float,
    centerLat: Float
): Pair<Offset, Float>? {
    val lambda = Math.toRadians((lon - centerLon).toDouble())
    val phi = Math.toRadians(lat.toDouble())
    val phi0 = Math.toRadians(centerLat.toDouble())
    val x = cos(phi) * sin(lambda)
    val y = cos(phi0) * sin(phi) - sin(phi0) * cos(phi) * cos(lambda)
    val z = sin(phi0) * sin(phi) + cos(phi0) * cos(phi) * cos(lambda)
    if (z < -0.02) return null
    val edgeScale = .95f + (.05f * z.toFloat())
    return Offset(
        frame.center.x + (x * frame.radius * edgeScale).toFloat(),
        frame.center.y - (y * frame.radius * edgeScale).toFloat()
    ) to z.toFloat()
}

private fun screenToLonLat(
    tap: Offset,
    size: IntSize,
    centerLon: Float,
    centerLat: Float,
    zoom: Float
): LonLat? {
    if (size.width <= 0 || size.height <= 0) return null
    val frame = globeFrame(size.width.toFloat(), size.height.toFloat(), zoom)
    val x = ((tap.x - frame.center.x) / frame.radius).toDouble()
    val y = (-(tap.y - frame.center.y) / frame.radius).toDouble()
    val rho = sqrt(x * x + y * y)
    if (rho > 1.02) return null
    if (rho < 1e-6) return LonLat(normalizeLon(centerLon), centerLat)
    val c = asin(rho.coerceIn(0.0, 1.0))
    val phi0 = Math.toRadians(centerLat.toDouble())
    val lambda0 = Math.toRadians(centerLon.toDouble())
    val lat = asin(cos(c) * sin(phi0) + y * sin(c) * cos(phi0) / rho)
    val lon = lambda0 + atan2(
        x * sin(c),
        rho * cos(phi0) * cos(c) - y * sin(phi0) * sin(c)
    )
    return LonLat(normalizeLon(Math.toDegrees(lon).toFloat()), Math.toDegrees(lat).toFloat())
}

private fun buildProjectedPath(
    ring: List<LonLat>,
    frame: GlobeFrame,
    centerLon: Float,
    centerLat: Float
): Path? {
    if (ring.size < 3) return null
    val projected = ring.mapNotNull { point -> projectLonLat(point.lon, point.lat, frame, centerLon, centerLat)?.first }
    if (projected.size < 3 || projected.size < ring.size * .28f) return null
    return Path().apply {
        projected.forEachIndexed { index, point ->
            if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
        }
        close()
    }
}

private fun pickCountry(countries: List<GlobeCountry>, point: LonLat): GlobeCountry? {
    return countries.asReversed().firstOrNull { country ->
        country.rings.any { ring -> containsLonLat(ring, point) } ||
            country.hitRings.any { ring -> containsLonLat(ring, point) }
    }
}

private fun containsLonLat(ring: List<LonLat>, point: LonLat): Boolean {
    if (ring.size < 3) return false
    var inside = false
    var j = ring.lastIndex
    for (i in ring.indices) {
        val a = ring[i]
        val b = ring[j]
        val crosses = (a.lat > point.lat) != (b.lat > point.lat)
        if (crosses) {
            val lonAtLat = (b.lon - a.lon) * (point.lat - a.lat) / ((b.lat - a.lat).takeIf { it != 0f } ?: .0001f) + a.lon
            if (point.lon < lonAtLat) inside = !inside
        }
        j = i
    }
    return inside
}

private fun loadGlobeCountries(context: Context): List<GlobeCountry> {
    return runCatching {
        // Prefer lightweight 110m data — 50m is ~3MB and freezes Canvas on pan/zoom.
        val raw = listOf(
            "map/ne_110m_admin_0_countries.geojson",
            "map/ne_50m_admin_0_countries.geojson"
        ).firstNotNullOfOrNull { asset ->
            runCatching {
                context.assets.open(asset).bufferedReader(Charsets.UTF_8).use { it.readText() }
            }.getOrNull()
        } ?: return@runCatching regionalFallbackCountries
        val features = JSONObject(raw).getJSONArray("features")
        val parsed = (0 until features.length()).mapNotNull { index ->
            val feature = features.getJSONObject(index)
            val properties = feature.getJSONObject("properties")
            val rawIso = properties.optString("ISO_A2")
                .takeIf { it.length == 2 && it != "-99" }
                ?: properties.optString("ISO_A2_EH").takeIf { it.length == 2 && it != "-99" }
                ?: return@mapNotNull null
            val iso = canonicalIso(rawIso)
            val geometry = feature.optJSONObject("geometry") ?: return@mapNotNull null
            val rings = simplifyCountryRings(parseCountryRings(geometry))
            if (rings.isEmpty()) return@mapNotNull null
            GlobeCountry(
                iso = iso,
                name = displayNameForIso(iso, localizedCountryName(properties, iso)),
                rings = rings,
                label = labelForCountry(properties, rings)
            )
        }
        val grouped = parsed.groupBy { it.iso }.map { (iso, group) ->
            val extraHitRings = smallCountryHitRings[iso].orEmpty()
            // Keep only the largest few rings per country (drop tiny islands that kill FPS)
            val allRings = group.flatMap { it.rings }
                .sortedByDescending { ringAreaApprox(it) }
                .take(if (iso in setOf("ID", "PH", "CA", "US", "RU", "JP", "GB", "NO", "GR")) 6 else 3)
            GlobeCountry(
                iso = iso,
                name = displayNameForIso(iso, group.firstOrNull { it.name.isNotBlank() }?.name ?: iso),
                rings = allRings,
                label = countryCenters[iso] ?: group.first().label,
                hitRings = group.flatMap { it.hitRings } + extraHitRings
            )
        }
        val existing = grouped.map { it.iso }.toSet()
        grouped + regionalFallbackCountries.filterNot { it.iso in existing }
    }.getOrElse { regionalFallbackCountries }
}

/** Approximate polygon area for ranking rings (lon/lat plane). */
private fun ringAreaApprox(ring: List<LonLat>): Float {
    if (ring.size < 3) return 0f
    var a = 0f
    var j = ring.lastIndex
    for (i in ring.indices) {
        a += ring[j].lon * ring[i].lat - ring[i].lon * ring[j].lat
        j = i
    }
    return abs(a) * .5f
}

/** Downsample rings so Canvas path count stays small during gestures. */
private fun simplifyCountryRings(rings: List<List<LonLat>>): List<List<LonLat>> {
    return rings.mapNotNull { ring ->
        if (ring.size < 4) return@mapNotNull null
        val maxPts = when {
            ring.size > 400 -> 48
            ring.size > 200 -> 56
            ring.size > 100 -> 64
            else -> 80
        }
        downsampleLonLatRing(ring, maxPts)
    }.filter { it.size >= 3 }
}

private fun downsampleLonLatRing(ring: List<LonLat>, maxPts: Int): List<LonLat> {
    if (ring.size <= maxPts) return ring
    val closed = ring.size > 2 &&
        abs(ring.first().lon - ring.last().lon) < 1e-4f &&
        abs(ring.first().lat - ring.last().lat) < 1e-4f
    val body = if (closed) ring.dropLast(1) else ring
    if (body.size <= maxPts) return ring
    val step = (body.size.toFloat() / maxPts).coerceAtLeast(1f)
    val out = ArrayList<LonLat>(maxPts + 1)
    var i = 0f
    while (i < body.size) {
        out.add(body[i.toInt().coerceIn(0, body.lastIndex)])
        i += step
    }
    if (out.last() != body.last()) out.add(body.last())
    if (closed) out.add(out.first())
    return out
}

private fun localizedCountryName(properties: JSONObject, iso: String): String {
    listOf("NAME_ZH", "NAME", "NAME_EN", "ADMIN").forEach { key ->
        val value = properties.optString(key)
        if (value.isNotBlank() && value != "-99") return value
    }
    return iso
}

private fun parseCountryRings(geometry: JSONObject): List<List<LonLat>> {
    val type = geometry.optString("type")
    val coordinates = geometry.optJSONArray("coordinates") ?: return emptyList()
    return when (type) {
        "Polygon" -> parsePolygon(coordinates)
        "MultiPolygon" -> {
            buildList {
                for (i in 0 until coordinates.length()) {
                    addAll(parsePolygon(coordinates.getJSONArray(i)))
                }
            }
        }
        else -> emptyList()
    }
}

private fun parsePolygon(polygon: JSONArray): List<List<LonLat>> {
    return buildList {
        for (ringIndex in 0 until polygon.length()) {
            val ring = polygon.getJSONArray(ringIndex)
            val points = buildList {
                for (pointIndex in 0 until ring.length()) {
                    val coord = ring.getJSONArray(pointIndex)
                    add(LonLat(coord.getDouble(0).toFloat(), coord.getDouble(1).toFloat()))
                }
            }
            if (points.size >= 3) add(points)
        }
    }
}

private fun labelForCountry(properties: JSONObject, rings: List<List<LonLat>>): LonLat {
    val labelLon = properties.optDouble("LABEL_X", Double.NaN)
    val labelLat = properties.optDouble("LABEL_Y", Double.NaN)
    if (!labelLon.isNaN() && !labelLat.isNaN()) return LonLat(labelLon.toFloat(), labelLat.toFloat())
    val flat = rings.flatten()
    if (flat.isEmpty()) return LonLat(0f, 0f)
    return LonLat(
        flat.map { it.lon }.average().toFloat(),
        flat.map { it.lat }.average().toFloat()
    )
}

private fun daysUntil(expireDate: String): Long? =
    runCatching { LocalDate.parse(expireDate).toEpochDay() - LocalDate.now().toEpochDay() }.getOrNull()

private fun isEsimMapRecord(r: PhoneNumberRecord): Boolean =
    r.cardType.contains("esim", ignoreCase = true) ||
        r.eid.isNotBlank() ||
        r.smdp.isNotBlank() ||
        r.activationCode.isNotBlank()

private fun isoForMapRecord(r: PhoneNumberRecord): String =
    Countries.list.firstOrNull { it.code == r.countryCode && it.name == r.countryName }?.iso
        ?: Countries.list.firstOrNull { it.code == r.countryCode }?.iso
        ?: r.countryName.uppercase().take(2).ifBlank { "UN" }

private fun latLonForIso(iso: String): LonLat? =
    countryCenters[canonicalIso(iso)]

private fun normalizeLon(lon: Float): Float {
    var value = lon
    while (value > 180f) value -= 360f
    while (value < -180f) value += 360f
    return value
}

private fun shortestLonDelta(from: Float, to: Float): Float =
    normalizeLon(to - normalizeLon(from))

/** Higher max zoom so users can pinch in much closer on the in-app globe. */
private fun clampGlobeZoom(value: Float): Float =
    value.coerceIn(.68f, 5.2f)

private fun canonicalIso(iso: String): String =
    iso.uppercase()

private fun displayNameForIso(iso: String, fallback: String): String = when (canonicalIso(iso)) {
    "CN" -> "中国"
    "TW" -> "中国台湾省"
    "HK" -> "香港"
    "MO" -> "澳门"
    else -> fallback
}

private fun regionFlagForIso(iso: String, fallback: String): String {
    val code = canonicalIso(iso)
    if (code == "TW") return ""
    return fallback.ifBlank { flagEmojiForIso(code) }
}

private fun flagEmojiForIso(iso: String): String {
    val code = canonicalIso(iso)
    if (code == "TW") return ""
    if (code.length != 2 || code.any { it !in 'A'..'Z' }) return code
    return buildString {
        code.forEach { appendCodePoint(0x1F1E6 + (it.code - 'A'.code)) }
    }
}

private fun regionBox(iso: String, center: LonLat, halfLon: Float, halfLat: Float): GlobeCountry =
    GlobeCountry(
        iso = iso,
        name = displayNameForIso(iso, iso),
        rings = listOf(
            listOf(
                LonLat(center.lon - halfLon, center.lat - halfLat),
                LonLat(center.lon + halfLon, center.lat - halfLat),
                LonLat(center.lon + halfLon, center.lat + halfLat),
                LonLat(center.lon - halfLon, center.lat + halfLat)
            )
        ),
        label = center
    )

private val smallCountryHitBoxes = listOf(
    regionBox("HK", LonLat(114.17f, 22.32f), .52f, .36f),
    regionBox("MO", LonLat(113.55f, 22.16f), .34f, .28f),
    regionBox("TW", LonLat(121f, 24f), 1.25f, 2.25f),
    regionBox("AD", LonLat(1.47f, 42.55f), .28f, .2f),
    regionBox("MC", LonLat(7.42f, 43.74f), .12f, .1f),
    regionBox("SM", LonLat(12.46f, 43.94f), .14f, .13f),
    regionBox("VA", LonLat(12.45f, 41.9f), .08f, .08f),
    regionBox("LI", LonLat(9.56f, 47.17f), .18f, .16f),
    regionBox("MT", LonLat(14.38f, 35.94f), .28f, .16f),
    regionBox("GI", LonLat(-5.35f, 36.14f), .13f, .13f),
    regionBox("FO", LonLat(-6.91f, 61.89f), .8f, .45f),
    regionBox("LU", LonLat(6.13f, 49.82f), .28f, .25f),
    regionBox("SG", LonLat(103.82f, 1.35f), .25f, .18f),
    regionBox("BH", LonLat(50.56f, 26.07f), .25f, .2f),
    regionBox("QA", LonLat(51.18f, 25.35f), .55f, .5f),
    regionBox("MV", LonLat(73.22f, 3.2f), 1f, 3.5f),
    regionBox("CY", LonLat(33.43f, 35.12f), .75f, .4f),
    regionBox("BB", LonLat(-59.54f, 13.19f), .28f, .22f),
    regionBox("TT", LonLat(-61.22f, 10.54f), .62f, .5f),
    regionBox("BS", LonLat(-77.39f, 25.03f), 2.2f, 2.2f),
    regionBox("JM", LonLat(-77.3f, 18.1f), .75f, .25f),
    regionBox("AG", LonLat(-61.8f, 17.06f), .35f, .3f),
    regionBox("DM", LonLat(-61.37f, 15.41f), .28f, .27f),
    regionBox("LC", LonLat(-60.98f, 13.91f), .28f, .25f),
    regionBox("VC", LonLat(-61.2f, 13.25f), .35f, .35f),
    regionBox("GD", LonLat(-61.68f, 12.12f), .32f, .32f),
    regionBox("KN", LonLat(-62.78f, 17.36f), .28f, .25f),
    regionBox("SX", LonLat(-63.05f, 18.04f), .16f, .12f),
    regionBox("AW", LonLat(-69.97f, 12.52f), .28f, .18f),
    regionBox("CW", LonLat(-68.99f, 12.17f), .35f, .2f),
    regionBox("BQ", LonLat(-68.25f, 12.2f), .45f, .35f),
    regionBox("BM", LonLat(-64.75f, 32.3f), .35f, .2f),
    regionBox("KY", LonLat(-81.25f, 19.31f), .75f, .35f),
    regionBox("TC", LonLat(-71.8f, 21.7f), .8f, .45f),
    regionBox("AI", LonLat(-63.07f, 18.22f), .3f, .15f),
    regionBox("VG", LonLat(-64.64f, 18.42f), .45f, .25f),
    regionBox("VI", LonLat(-64.9f, 18.34f), .45f, .25f),
    regionBox("PR", LonLat(-66.59f, 18.22f), .9f, .35f),
    regionBox("GP", LonLat(-61.55f, 16.25f), .45f, .4f),
    regionBox("MQ", LonLat(-61.02f, 14.64f), .35f, .3f),
    regionBox("BL", LonLat(-62.83f, 17.9f), .16f, .12f),
    regionBox("MF", LonLat(-63.05f, 18.08f), .18f, .13f),
    regionBox("RE", LonLat(55.54f, -21.12f), .35f, .25f),
    regionBox("MU", LonLat(57.55f, -20.25f), .35f, .3f),
    regionBox("SC", LonLat(55.45f, -4.62f), .8f, .8f),
    regionBox("KM", LonLat(43.33f, -11.65f), .65f, .6f),
    regionBox("CV", LonLat(-23.6f, 15.1f), .9f, .8f),
    regionBox("ST", LonLat(6.73f, .19f), .6f, .45f),
    regionBox("YT", LonLat(45.16f, -12.83f), .35f, .25f),
    regionBox("NC", LonLat(165.62f, -21.3f), 1.2f, .7f),
    regionBox("PF", LonLat(-149.4f, -17.65f), 4f, 3f),
    regionBox("GU", LonLat(144.79f, 13.44f), .3f, .25f),
    regionBox("MP", LonLat(145.75f, 15.1f), 1f, 1.8f),
    regionBox("AS", LonLat(-170.7f, -14.3f), .6f, .4f),
    regionBox("CK", LonLat(-159.78f, -21.23f), 2f, 2f),
    regionBox("NU", LonLat(-169.87f, -19.05f), .5f, .35f),
    regionBox("TK", LonLat(-171.85f, -9.2f), .7f, .45f),
    regionBox("WF", LonLat(-176.2f, -13.3f), .6f, .4f),
    regionBox("WS", LonLat(-172.1f, -13.76f), .7f, .45f),
    regionBox("TO", LonLat(-175.2f, -21.18f), .9f, .8f),
    regionBox("TV", LonLat(178.68f, -7.1f), .7f, .9f),
    regionBox("NR", LonLat(166.93f, -.52f), .35f, .3f),
    regionBox("KI", LonLat(-157.36f, 1.87f), 4f, 3f),
    regionBox("MH", LonLat(171.18f, 7.13f), 2.5f, 2f),
    regionBox("FM", LonLat(158.25f, 6.9f), 3f, 2f),
    regionBox("PW", LonLat(134.58f, 7.5f), .8f, .65f)
).associateBy { it.iso }

private val smallCountryHitRings = smallCountryHitBoxes.mapValues { it.value.rings }

private val regionalFallbackCountries = smallCountryHitBoxes.values.toList()

private val countryCenters = mapOf(
    "US" to LonLat(-98f, 39f), "CA" to LonLat(-105f, 58f), "MX" to LonLat(-102f, 23f),
    "BR" to LonLat(-52f, -10f), "AR" to LonLat(-64f, -35f), "CL" to LonLat(-71f, -30f),
    "CO" to LonLat(-74f, 5f), "PE" to LonLat(-75f, -10f), "GB" to LonLat(-2f, 54f),
    "FR" to LonLat(2f, 47f), "DE" to LonLat(10f, 51f), "IT" to LonLat(12f, 43f),
    "ES" to LonLat(-4f, 40f), "NL" to LonLat(5f, 52f), "BE" to LonLat(4f, 51f),
    "CH" to LonLat(8f, 47f), "SE" to LonLat(15f, 62f), "NO" to LonLat(9f, 62f),
    "FI" to LonLat(26f, 64f), "PL" to LonLat(20f, 52f), "RU" to LonLat(90f, 60f),
    "TR" to LonLat(35f, 39f), "EG" to LonLat(30f, 27f), "ZA" to LonLat(24f, -29f),
    "NG" to LonLat(8f, 9f), "KE" to LonLat(38f, 0f), "MA" to LonLat(-7f, 31f),
    "AE" to LonLat(54f, 24f), "SA" to LonLat(45f, 24f), "IL" to LonLat(35f, 31f),
    "IN" to LonLat(78f, 22f), "CN" to LonLat(104f, 35f), "HK" to LonLat(114f, 22f),
    "MO" to LonLat(113f, 22f), "TW" to LonLat(121f, 24f), "JP" to LonLat(138f, 37f), "KR" to LonLat(128f, 36f),
    "SG" to LonLat(104f, 1f), "MY" to LonLat(102f, 4f), "TH" to LonLat(101f, 15f),
    "VN" to LonLat(106f, 16f), "PH" to LonLat(122f, 13f), "ID" to LonLat(118f, -3f),
    "AU" to LonLat(134f, -25f), "NZ" to LonLat(172f, -42f)
)
