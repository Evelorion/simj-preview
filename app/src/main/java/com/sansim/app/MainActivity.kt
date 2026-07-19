
package com.sansim.app

import com.sansim.app.data.model.Country
import com.sansim.app.data.model.Countries
import com.sansim.app.data.model.OperatorInfo
import com.sansim.app.data.model.OperatorDatabase
import com.sansim.app.data.model.PhoneNumberRecord
import com.sansim.app.data.model.App设置
import com.sansim.app.data.model.DEFAULT_SIMJ_CLOUD_URL
import com.sansim.app.i18n.tr
import com.sansim.app.i18n.dayText
import com.sansim.app.i18n.laterText
import com.sansim.app.i18n.expireText
import com.sansim.app.i18n.cycleText
import com.sansim.app.util.LocalAppLanguage
import com.sansim.app.util.L
import com.sansim.app.util.LT



import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.widget.Toast
import java.io.File
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SimCard
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.SSLSocketFactory
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.core.content.FileProvider
import com.sansim.app.esim.EsimScreen
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import kotlin.concurrent.thread
import kotlin.math.roundToInt
import com.sansim.app.update.UpdateInfo
import com.sansim.app.update.UpdateChecker
import com.sansim.app.update.UpdateDialog
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

val LocalIsDark = compositionLocalOf { false }
@Composable private fun dk(dark: Color, light: Color): Color = if(LocalIsDark.current) dark else light

private val SimJLightColors = lightColorScheme(
    primary = Color(0xFF0B57D0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E2FF),
    onPrimaryContainer = Color(0xFF001B3F),
    secondary = Color(0xFF006D43),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA7F2C7),
    onSecondaryContainer = Color(0xFF002111),
    tertiary = Color(0xFF7C4DFF),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8DDFF),
    onTertiaryContainer = Color(0xFF25005A),
    background = Color(0xFFF8FAFF),
    onBackground = Color(0xFF171B23),
    surface = Color(0xFFFEFBFF),
    onSurface = Color(0xFF171B23),
    surfaceVariant = Color(0xFFE1E5EF),
    onSurfaceVariant = Color(0xFF424753),
    outline = Color(0xFF727784),
    outlineVariant = Color(0xFFC2C7D3),
    error = Color(0xFFB3261E)
)

private val SimJDarkColors = darkColorScheme(
    primary = Color(0xFFAFC6FF),
    onPrimary = Color(0xFF002E6D),
    primaryContainer = Color(0xFF174EA6),
    onPrimaryContainer = Color(0xFFD7E2FF),
    secondary = Color(0xFF8DD9AE),
    onSecondary = Color(0xFF00391F),
    secondaryContainer = Color(0xFF005230),
    onSecondaryContainer = Color(0xFFA7F2C7),
    tertiary = Color(0xFFD2BCFF),
    onTertiary = Color(0xFF3F008F),
    tertiaryContainer = Color(0xFF5D2FD1),
    onTertiaryContainer = Color(0xFFE8DDFF),
    background = Color(0xFF10141C),
    onBackground = Color(0xFFE3E7F0),
    surface = Color(0xFF11151D),
    onSurface = Color(0xFFE3E7F0),
    surfaceVariant = Color(0xFF424753),
    onSurfaceVariant = Color(0xFFC2C7D3),
    outline = Color(0xFF8C919D),
    outlineVariant = Color(0xFF424753),
    error = Color(0xFFFFB4AB)
)

private val SimJTypography = Typography(
    displaySmall = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, lineHeight = 42.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp)
)

private val SimJShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(38.dp)
)

@Composable
private fun SimJMaterialTheme(dark: Boolean, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (dark) SimJDarkColors else SimJLightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = SimJTypography,
        shapes = SimJShapes,
        content = content
    )
}

const val CHANNEL_ID = "san_sim_reminders"
const val PREF = "san_sim_data"

class SanSimApplication: Application() { override fun onCreate(){ super.onCreate(); NotificationHelper.createChannel(this) } }








object DataStore {
    fun load设置(ctx:Context):App设置 {
        val p=ctx.getSharedPreferences(PREF,0); val o=JSONObject(p.getString("settings","{}")!!)
        return App设置(o.optBoolean("dark"),o.optInt("remind天",7),o.optString("trafficUrl","https://speed.cloudflare.com/__down?bytes=10485760"),o.optDouble("trafficKb",1.0),o.optBoolean("tgEnabled"),o.optString("botToken"),o.optString("chatId"),o.optString("keepCycle","月"),o.optString("backgroundUri",""),o.optDouble("backgroundAlpha",0.72).toFloat(),o.optBoolean("reminderEnabled",true),o.optBoolean("notificationEnabled",true),o.optInt("remindHour",9),o.optInt("remindMinute",0),o.optString("language","简体中文"),o.optBoolean("emailQuickEnabled",true),o.optBoolean("smtpEnabled",false),o.optString("smtpHost",""),o.optInt("smtpPort",465),o.optString("smtpUser",""),o.optString("smtpPass",""),o.optString("smtpFrom",""),o.optString("smtpTo",""),o.optBoolean("cloudEnabled",false),cleanBundledCloudUrl(o.optString("cloudUrl","")),o.optString("cloudApiKey",""),o.optBoolean("cloudTelegramEnabled",true),o.optBoolean("cloudEmailEnabled",true),o.optBoolean("cloudAutoSync",false),o.optBoolean("showFlag",true),o.optBoolean("bankCardStyle",false),o.optString("cloudToken",""),o.optString("cloudUsername",""),o.optString("cloudDeviceId",""))
    }
    fun save设置(ctx:Context,s:App设置){
        val o=JSONObject().put("dark",s.dark).put("remind天",s.remind天).put("trafficUrl",s.trafficUrl).put("trafficKb",s.trafficKb).put("tgEnabled",s.tgEnabled).put("botToken",s.botToken).put("chatId",s.chatId).put("keepCycle",s.keepCycle).put("backgroundUri",s.backgroundUri).put("backgroundAlpha",s.backgroundAlpha.toDouble()).put("reminderEnabled",s.reminderEnabled).put("notificationEnabled",s.notificationEnabled).put("remindHour",s.remindHour).put("remindMinute",s.remindMinute).put("language",s.language).put("emailQuickEnabled",s.emailQuickEnabled).put("smtpEnabled",s.smtpEnabled).put("smtpHost",s.smtpHost).put("smtpPort",s.smtpPort).put("smtpUser",s.smtpUser).put("smtpPass",s.smtpPass).put("smtpFrom",s.smtpFrom).put("smtpTo",s.smtpTo).put("cloudEnabled",s.cloudEnabled).put("cloudUrl",cleanBundledCloudUrl(s.cloudUrl)).put("cloudApiKey",s.cloudApiKey).put("cloudTelegramEnabled",s.cloudTelegramEnabled).put("cloudEmailEnabled",s.cloudEmailEnabled).put("cloudAutoSync",s.cloudAutoSync).put("showFlag",s.showFlag).put("bankCardStyle",s.bankCardStyle).put("cloudToken",s.cloudToken).put("cloudUsername",s.cloudUsername).put("cloudDeviceId",s.cloudDeviceId)
        ctx.getSharedPreferences(PREF,0).edit().putString("settings",o.toString()).apply(); ReminderScheduler.schedule全部(ctx)
    }
    fun normalizeLongTerm(r:PhoneNumberRecord):PhoneNumberRecord{
        if(!r.longTerm) return r
        val today=LocalDate.now(); var exp=runCatching{LocalDate.parse(r.expireDate)}.getOrNull() ?: return r
        val step=r.cycleDays.coerceIn(1,3650)
        while(exp.isBefore(today)) exp=exp.plusDays(step.toLong())
        return if(exp.toString()!=r.expireDate) r.copy(expireDate=exp.toString()) else r
    }
    fun loadRecords(ctx:Context):List<PhoneNumberRecord>{
        val arr=JSONArray(ctx.getSharedPreferences(PREF,0).getString("records","[]"))
        return (0 until arr.length()).mapIndexed{ idx,it-> val o=arr.getJSONObject(it)
            normalizeLongTerm(PhoneNumberRecord(
                id=o.optString("id",UUID.randomUUID().toString()), countryCode=o.optString("countryCode","+86"), countryName=o.optString("countryName","中国"), flag=o.optString("flag","🇨🇳"), number=o.optString("number"), operator=o.optString("operator"), expireDate=o.optString("expireDate",LocalDate.now().plusDays(30).toString()), note=o.optString("note"),
                balance=o.optString("balance"), eid=o.optString("eid"), smdp=o.optString("smdp"), activationCode=o.optString("activationCode"), startDate=o.optString("startDate",LocalDate.now().toString()), createdAt=o.optString("createdAt",LocalDate.now().toString()), activatedAt=o.optString("activatedAt"), longTerm=o.optBoolean("longTerm",false), cycleDays=o.optInt("cycleDays",30), signalStatus=o.optString("signalStatus","在线"), tags=o.optString("tags",""), transactionNotes=o.optString("transactionNotes",""), customPrompt=o.optString("customPrompt",""), websiteURL=o.optString("websiteURL",""), cyclePaymentMinorUnits=o.optInt("cyclePaymentMinorUnits",0), currencyCode=o.optString("currencyCode",""), cardBackgroundAssetName=o.optString("cardBackgroundAssetName",""), cardColorHex=o.optString("cardColorHex",""), cardType=o.optString("cardType","prepaid"), sortOrder=o.optInt("sortOrder",(idx+1)*10)
            ))
        }
    }
    fun recordJson(r:PhoneNumberRecord)=JSONObject()
        .put("id",r.id).put("countryCode",r.countryCode).put("countryName",r.countryName).put("flag",r.flag)
        .put("number",r.number).put("operator",r.operator).put("expireDate",r.expireDate).put("note",r.note)
        .put("balance",r.balance).put("eid",r.eid).put("smdp",r.smdp).put("activationCode",r.activationCode)
        .put("startDate",r.startDate).put("createdAt",r.createdAt).put("activatedAt",r.activatedAt)
        .put("longTerm",r.longTerm).put("cycleDays",r.cycleDays).put("signalStatus",r.signalStatus)
        .put("tags",r.tags).put("transactionNotes",r.transactionNotes).put("customPrompt",r.customPrompt)
        .put("websiteURL",r.websiteURL).put("cyclePaymentMinorUnits",r.cyclePaymentMinorUnits)
        .put("currencyCode",r.currencyCode).put("cardBackgroundAssetName",r.cardBackgroundAssetName)
        .put("cardColorHex",r.cardColorHex).put("cardType",r.cardType).put("sortOrder",r.sortOrder)
    fun saveRecords(ctx:Context,list:List<PhoneNumberRecord>){ val arr=JSONArray(); list.forEach{ arr.put(recordJson(it)) }; ctx.getSharedPreferences(PREF,0).edit().putString("records",arr.toString()).apply(); ReminderScheduler.schedule全部(ctx) }
}

object NotificationHelper {
    fun createChannel(ctx:Context){ if(Build.VERSION.SDK_INT>=26){ val nm=ctx.getSystemService(NotificationManager::class.java); nm.createNotificationChannel(NotificationChannel(CHANNEL_ID,"DsimJ 到期提醒",NotificationManager.IMPORTANCE_HIGH)) } }
    fun notify(ctx:Context,id:Int,title:String,text:String,emailIntent:Intent?=null){
        val b=Notification.Builder(ctx,CHANNEL_ID).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).setContentText(text).setStyle(Notification.BigTextStyle().bigText(text)).setAutoCancel(true)
        if(emailIntent!=null){
            val pi=PendingIntent.getActivity(ctx,id+900000,emailIntent,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            b.addAction(android.R.drawable.ic_dialog_email,"发邮件",pi)
        }
        ctx.getSystemService(NotificationManager::class.java).notify(id,b.build())
    }
}
object ReminderScheduler {
    fun schedule全部(ctx:Context){
        val am=ctx.getSystemService(AlarmManager::class.java) ?: return
        val settings=DataStore.load设置(ctx)
        if(!settings.reminderEnabled && !settings.smtpEnabled) return
        val canExact = if (Build.VERSION.SDK_INT >= 31) am.canScheduleExactAlarms() else true
        DataStore.loadRecords(ctx).forEach{ r->
            val date=runCatching{LocalDate.parse(r.expireDate)}.getOrNull()?:return@forEach
            val time=date.minusDays(settings.remind天.toLong()).atTime(settings.remindHour.coerceIn(0,23),settings.remindMinute.coerceIn(0,59)).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            if(time>System.currentTimeMillis()){
                val pi=PendingIntent.getBroadcast(ctx,r.id.hashCode(),Intent(ctx,ReminderReceiver::class.java).putExtra("id",r.id),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                runCatching{
                    if(canExact) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,pi)
                    else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,time,pi)
                }
            }
        }
    }
}
class ReminderReceiver: BroadcastReceiver(){ override fun onReceive(ctx:Context,intent:Intent){
    val id=intent.getStringExtra("id"); val r=DataStore.loadRecords(ctx).firstOrNull{it.id==id}?:return
    val s=DataStore.load设置(ctx)
    val subject="DsimJ 号码到期提醒：${r.operator.ifBlank{r.countryName}} ${r.countryCode} ${formatNumber(r.number)}"
    val body=buildEmailBody(r,s)
    val msg="${r.flag} ${r.countryCode} ${formatNumber(r.number)} 将于 ${r.expireDate} 到期"
    if(s.notificationEnabled){
        val emailIntent=if(s.emailQuickEnabled) makeEmailIntent(s.smtpTo,subject,body) else null
        NotificationHelper.notify(ctx,r.id.hashCode(),"号码即将到期",msg,emailIntent)
    }
    if(s.tgEnabled) sendTelegram(s.botToken,s.chatId,"⏰ DsimJ 到期提醒\n$msg")
    if(s.smtpEnabled) sendSmtpMail(s,subject,body)
} }
class BootReceiver: BroadcastReceiver(){ override fun onReceive(ctx:Context,intent:Intent){ ReminderScheduler.schedule全部(ctx) } }
fun sendTelegram(token:String,chatId:String,text:String){ if(token.isBlank()||chatId.isBlank()) return; thread { runCatching{ val u="https://api.telegram.org/bot$token/sendMessage?chat_id=${URLEncoder.encode(chatId,"UTF-8")}&text=${URLEncoder.encode(text,"UTF-8")}"; (URL(u).openConnection() as HttpURLConnection).apply{connectTimeout=8000;readTimeout=8000}.inputStream.close() } } }
fun buildEmailBody(r:PhoneNumberRecord,s:App设置):String = """
Sim Jiang 到期提醒

号码：${r.countryCode} ${formatNumber(r.number)}
国家/地区：${r.countryName}
运营商：${r.operator.ifBlank{r.countryName}}
到期日期：${r.expireDate}
套餐余额：${r.balance.ifBlank{"未填写"}}
EID：${r.eid.ifBlank{"未填写"}}
备注：${r.note.ifBlank{"无"}}

请及时保号、充值或刷流量。
""".trimIndent()
fun makeEmailIntent(to:String,subject:String,body:String):Intent = Intent(Intent.ACTION_SENDTO).apply{
    data=Uri.parse("mailto:")
    if(to.isNotBlank()) putExtra(Intent.EXTRA_EMAIL,arrayOf(to))
    putExtra(Intent.EXTRA_SUBJECT,subject)
    putExtra(Intent.EXTRA_TEXT,body)
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}
fun smtpB64(s:String)=Base64.encodeToString(s.toByteArray(Charsets.UTF_8),Base64.NO_WRAP)
fun sendSmtpMail(s:App设置,subject:String,body:String,onResult:((Boolean,String)->Unit)?=null){
    fun done(ok:Boolean,msg:String){ onResult?.let{ Handler(Looper.getMainLooper()).post{ it(ok,msg) } } }
    if(!s.smtpEnabled || s.smtpHost.isBlank() || s.smtpUser.isBlank() || s.smtpPass.isBlank() || s.smtpTo.isBlank()) { done(false,"SMTP 未配置完整"); return }
    thread{
        val res=runCatching{
            val port=s.smtpPort.coerceIn(1,65535)
            val socket=(SSLSocketFactory.getDefault() as SSLSocketFactory).createSocket(s.smtpHost,port)
            val reader=socket.getInputStream().bufferedReader(Charsets.UTF_8)
            val writer=socket.getOutputStream().bufferedWriter(Charsets.UTF_8)
            fun readResp():String = reader.readLine() ?: ""
            fun cmd(x:String):String{ writer.write(x+"\r\n"); writer.flush(); return readResp() }
            readResp()
            cmd("EHLO simjiang.local")
            cmd("AUTH LOGIN")
            cmd(smtpB64(s.smtpUser))
            cmd(smtpB64(s.smtpPass))
            val from=s.smtpFrom.ifBlank{s.smtpUser}
            cmd("MAIL FROM:<$from>")
            cmd("RCPT TO:<${s.smtpTo}>")
            cmd("DATA")
            val mail="From: $from\r\nTo: ${s.smtpTo}\r\nSubject: =?UTF-8?B?${smtpB64(subject)}?=\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n$body\r\n."
            cmd(mail)
            cmd("QUIT")
            socket.close()
            true
        }
        res.onSuccess{ done(true,"测试邮件已提交 SMTP") }.onFailure{ done(false,"发送失败：${it.javaClass.simpleName}: ${it.message}") }
    }
}

class MainActivity: ComponentActivity(){ private val req=registerForActivityResult(ActivityResultContracts.RequestPermission()){}; override fun onCreate(b:Bundle?){ super.onCreate(b); if(Build.VERSION.SDK_INT>=33) req.launch(Manifest.permission.POST_NOTIFICATIONS); setContent{ App(this) } } }

@Composable fun App(ctx:Context){
    var settings by remember{ mutableStateOf(DataStore.load设置(ctx)) }
    var records by remember{ mutableStateOf(DataStore.loadRecords(ctx)) }
    var screen by remember{ mutableStateOf("home") }
    var edit by remember{ mutableStateOf<PhoneNumberRecord?>(null) }
    var trafficTarget by remember{ mutableStateOf<PhoneNumberRecord?>(null) }
    var toolMessage by remember{ mutableStateOf<String?>(null) }
    var exportDialog by remember{ mutableStateOf<Pair<String,String>?>(null) }
    var filter by remember{ mutableStateOf("全部") }
    var sortMode by remember{ mutableStateOf("自定义") }
    var search by remember{ mutableStateOf("") }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }
    val currentVersion = try { ctx.packageManager.getPackageInfo(ctx.packageName,0).versionName ?: "0.0.0" } catch(_:Exception) { "0.0.0" }
    val lang = settings.language
    fun tx(key:String)=tr(lang,key)
    fun autoCloudSync(rs:List<PhoneNumberRecord>, st:App设置){
        if(st.cloudEnabled && st.cloudAutoSync && st.cloudToken.isNotBlank() && cleanCloudApiKey(st.cloudApiKey).isNotBlank() && rs.isNotEmpty()){
            cloudGet(st,"/api/sync"){ok,msg->
                if(ok){
                    val pull=analyzeCloudSyncResponse(msg,st)
                    val cannotReadCloud=pull.hasEncryptedVault && pull.records.isEmpty() && (pull.decryptError!=null || pull.cloudRecordHint>0)
                    if(!cannotReadCloud){
                        val merged=mergeRecords(pull.records,rs)
                        val mergedSettings=mergeCloudSettings(st,pull.settings)
                        cloudPost(mergedSettings,"/api/sync",cloudEncryptedPayload(merged,mergedSettings)){_,_->}
                    }
                }else if(msg.contains("404") || msg.contains("暂无") || msg.contains("no cloud data",true)){
                    cloudPost(st,"/api/sync",cloudEncryptedPayload(rs,st)){_,_->}
                }
            }
        }
    }
    fun normalizeSortOrder(list:List<PhoneNumberRecord>)=list.mapIndexed{idx,r->r.copy(sortOrder=(idx+1)*10)}
    fun reorderRecordsById(orderedIds:List<String>){
        val map=records.associateBy{it.id}
        val ordered=orderedIds.mapNotNull{map[it]}
        val rest=records.filterNot{orderedIds.contains(it.id)}
        records=normalizeSortOrder(ordered+rest)
        DataStore.saveRecords(ctx,records)
        autoCloudSync(records,settings)
    }
    SimJMaterialTheme(settings.dark){
    LaunchedEffect(Unit) {
        val now = System.currentTimeMillis()
        val prefs = ctx.getSharedPreferences("update_prefs",0)
        val last = prefs.getLong("last_check",0L)
        if (now - last > 86400000L) {
            val info = runCatching { UpdateChecker.check(currentVersion) }.getOrNull()
            if (info != null) updateInfo = info
            prefs.edit().putLong("last_check", now).apply()
        }
    }
        CompositionLocalProvider(LocalLayoutDirection provides if(settings.language=="阿拉伯语") LayoutDirection.Rtl else LayoutDirection.Ltr, LocalAppLanguage provides settings.language, LocalIsDark provides settings.dark){
        run{ val editing = edit!=null
            if(editing && edit!=null){
                Full编辑Screen(init=edit!!, onDismiss={edit=null}, onSave={r->
                    val resolvedIso=countryIsoFor(r.countryCode,r.countryName.ifBlank{r.flag})
                    val c=Countries.list.firstOrNull{it.iso==resolvedIso} ?: Countries.list.firstOrNull{it.code==r.countryCode && it.name==r.countryName} ?: Countries.list.firstOrNull{it.code==r.countryCode} ?: Countries.list.first()
                    val nr0=r.copy(countryCode=c.code,countryName=c.name,flag=c.flag,operator= if(r.operator.isBlank()) guessOperator(r.number,c.iso) else r.operator, createdAt=if(r.createdAt.isBlank()) LocalDate.now().toString() else r.createdAt, activatedAt=if(r.activatedAt.isBlank() && (r.smdp.isNotBlank() || r.activationCode.isNotBlank())) LocalDate.now().toString() else r.activatedAt)
                    val nr=if(records.any{it.id==nr0.id}) nr0 else nr0.copy(sortOrder=((records.maxOfOrNull{it.sortOrder}?:0)+10).coerceAtLeast((records.size+1)*10))
                    records= if(records.any{it.id==nr.id}) records.map{if(it.id==nr.id)nr else it} else records+nr
                    DataStore.saveRecords(ctx,records); autoCloudSync(records,settings)
                    edit=null
                }, onDelete={r->
                    records=records.filter{it.id!=r.id}
                    DataStore.saveRecords(ctx,records); autoCloudSync(records,settings)
                    edit=null
                })
            } else {
                Scaffold(
                    modifier=Modifier.fillMaxSize(),
                    containerColor=MaterialTheme.colorScheme.background,
                    bottomBar={ SimHubBottomNav(screen){ screen=it } },
                    floatingActionButton={ if(screen=="home") ExpressiveAddFab{ edit=PhoneNumberRecord() } }
                ){ innerPadding ->
                val layoutDirection = LocalLayoutDirection.current
                val contentModifier = if (screen == "home") {
                    Modifier
                        .fillMaxSize()
                        .padding(
                            start = innerPadding.calculateStartPadding(layoutDirection),
                            end = innerPadding.calculateEndPadding(layoutDirection),
                            bottom = innerPadding.calculateBottomPadding()
                        )
                        .background(MaterialTheme.colorScheme.background)
                } else {
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                }
                Column(contentModifier){
                    if (screen != "home" && screen != "esim" && screen != "map") {
                        SimHubTopBar(screen,settings.dark,{ settings=settings.copy(dark=!settings.dark); DataStore.save设置(ctx,settings) },search,{ search=it }){ target->
                            when(target){
                                "add" -> edit=PhoneNumberRecord()
                                "export" -> toolMessage=tx("导出数据已准备")+"："+tx("当前共有")+" ${records.size} "+tx("个号码")+"。\n"+tx("数据已保存在本机应用存储中，后续可接入文件导出。")
                                "grid" -> screen="countries"
                                else -> screen=target
                            }
                        }
                    }
                    Box(Modifier.weight(1f).fillMaxWidth()){
                        when(screen){
                            "home"->Home(ctx,records,settings,search,{search=it},filter,sortMode,{filter=it},{sortMode=it},{edit=PhoneNumberRecord()},{edit=it},{r->records=records.filter{it.id!=r.id};DataStore.saveRecords(ctx,records); autoCloudSync(records,settings)},{dial(ctx,it)},{trafficTarget=it},{r,months->val nr=r.copy(expireDate=(runCatching{LocalDate.parse(r.expireDate)}.getOrNull()?:LocalDate.now()).plusDays(months.toLong()).toString());records=records.map{if(it.id==r.id)nr else it};DataStore.saveRecords(ctx,records); autoCloudSync(records,settings)},{ids->reorderRecordsById(ids)})
                            "keep"->KeepPage(records,{r,m-> val nr=r.copy(expireDate=(runCatching{LocalDate.parse(r.expireDate)}.getOrNull()?:LocalDate.now()).plusDays(m.toLong()).toString()); records=records.map{if(it.id==r.id)nr else it}; DataStore.saveRecords(ctx,records); autoCloudSync(records,settings)})
                            "tools"->ToolsPage(ctx,settings,records,{trafficTarget=it},{dial(ctx,it)},{ exportDialog="json" to exportRecordsJson(records,settings) },{ exportDialog="csv" to exportRecordsCsv(records) },{ text-> val (imported,importedSettings)=parseRecordsAndSettings(text); if(imported.isNotEmpty()){ records=imported; DataStore.saveRecords(ctx,records); if(importedSettings!=null){ settings=importedSettings; DataStore.save设置(ctx,settings) }; autoCloudSync(records,settings); toolMessage=tx("导入完成")+"：${records.size} "+tx("个号码")+(if(importedSettings!=null) " + "+tx("配置已恢复") else "") } else toolMessage=tx("导入失败：未识别 JSON/CSV 数据") })
                            "settings"->{
                                设置Page(ctx,settings,records,currentVersion=currentVersion,onUpdateCheck={
                                    kotlin.concurrent.thread {
                                        val info = runCatching { kotlinx.coroutines.runBlocking { UpdateChecker.check(currentVersion) } }.getOrNull()
                                        if (info != null) { updateInfo = info }
                                    }
                                },on={s->settings=s;DataStore.save设置(ctx,s); autoCloudSync(records,s)},onTraffic={trafficTarget=it},onDial={dial(ctx,it)},onExportJson={exportDialog="json" to exportRecordsJson(records,settings)},onExportCsv={exportDialog="csv" to exportRecordsCsv(records)},onImportText={text-> val (imported,importedSettings)=parseRecordsAndSettings(text); if(imported.isNotEmpty()){ records=imported; DataStore.saveRecords(ctx,records); if(importedSettings!=null){ settings=importedSettings; DataStore.save设置(ctx,settings) }; autoCloudSync(records,settings); toolMessage=tx("导入完成")+"：${records.size} "+tx("个号码")+(if(importedSettings!=null) " + "+tx("配置已恢复") else "") } else toolMessage=tx("导入失败：未识别 JSON/CSV 数据") },onCloudRestore={imported,importedSettings-> if(imported.isNotEmpty()){ records=imported; DataStore.saveRecords(ctx,records); if(importedSettings!=null){ settings=importedSettings; DataStore.save设置(ctx,settings) }; toolMessage=tx("云端恢复完成")+"：${records.size} "+tx("个号码") } },onImportSimHub={imported->records=imported;DataStore.saveRecords(ctx,records);autoCloudSync(records,settings);toolMessage=tx("SimHub 导入完成")+"：${records.size} "+tx("个号码")})
                            }
                            "countries"->CountryPage()
                            "esim"->EsimScreen()
                            "map"->SimMapPage(records){ edit=it }
                        }
                        updateInfo?.let { info -> UpdateDialog(currentVersion = currentVersion, updateInfo = info, onDismiss = { updateInfo = null }) }
                    }
                }
                }
            }
        }
        }
    }
    if(trafficTarget!=null) TrafficDialog(ctx,trafficTarget!!,settings,{trafficTarget=null})
    toolMessage?.let { msg ->
        IOSInfoDialog(L("操作结果"),msg){toolMessage=null}
    }
    exportDialog?.let { item -> ExportDataDialog(ctx,item.first,item.second){exportDialog=null} }
}

@Composable fun Header(screen:String,on:(String)->Unit){ SimHubTopBar(screen,false,{},"",{},on) }

@Composable fun IOSInfoDialog(title:String,message:String,onDismiss:()->Unit){
    Dialog(onDismissRequest=onDismiss){ Surface(shape=RoundedCornerShape(24.dp),color=Color(0xFFF2F3F7)){ Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp),horizontalAlignment=Alignment.CenterHorizontally){ Text(title,fontSize=20.sp,fontWeight=FontWeight.Bold,color=Color(0xFF111827)); Text(message,fontSize=13.sp,color=Color(0xFF374151)); Button(onDismiss,modifier=Modifier.fillMaxWidth().height(48.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF007AFF))){Text(L("好"))} } } }
}

@Composable fun ExportDataDialog(ctx:Context,type:String,content:String,onDismiss:()->Unit){
    val clipboard=LocalClipboardManager.current
    val ext=if(type=="csv") "csv" else "json"
    val title=if(type=="csv") L("导出 CSV") else L("导出 JSON")
    val exportFileTitle=L("导出文件")
    Dialog(onDismissRequest=onDismiss){
        Surface(shape=RoundedCornerShape(26.dp),color=Color(0xFFF2F3F7),modifier=Modifier.fillMaxWidth()){
            Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                Text(title,fontSize=21.sp,fontWeight=FontWeight.Bold,color=Color(0xFF111827))
                Text(L("可以复制到剪贴板，也可以生成文件并调用系统分享。"),fontSize=13.sp,color=Color(0xFF8A94A6))
                Box(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).background(Color.White.copy(alpha=.86f)).border(.7.dp,Color.White,RoundedCornerShape(16.dp)).verticalScroll(rememberScrollState()).padding(12.dp)){
                    Text(content,fontSize=11.sp,color=Color(0xFF374151),lineHeight=16.sp)
                }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    Button({ clipboard.setText(AnnotatedString(content)) },modifier=Modifier.weight(1f).height(46.dp),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color.White,contentColor=Color(0xFF007AFF))){Text(L("复制"))}
                    Button({ shareExportFile(ctx,"DsimJ-export-${System.currentTimeMillis()}.$ext",if(ext=="csv") "text/csv" else "application/json",content,exportFileTitle) },modifier=Modifier.weight(1f).height(46.dp),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF007AFF))){Text(L("导出文件"))}
                }
                TextButton(onDismiss,modifier=Modifier.align(Alignment.CenterHorizontally)){Text(L("关闭"))}
            }
        }
    }
}

fun shareExportFile(ctx:Context,fileName:String,mime:String,content:String,title:String="导出文件"){
    runCatching{
        val dir=File(ctx.cacheDir,"exports"); dir.mkdirs()
        val f=File(dir,fileName); f.writeText(content,Charsets.UTF_8)
        val uri=FileProvider.getUriForFile(ctx,ctx.packageName+".fileprovider",f)
        val intent=Intent(Intent.ACTION_SEND).setType(mime).putExtra(Intent.EXTRA_STREAM,uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        ctx.startActivity(Intent.createChooser(intent,title))
    }
}




@Composable fun ExpressiveSimHubTopBar(screen:String,dark:Boolean,onToggleDark:()->Unit,search:String,onSearch:(String)->Unit,on:(String)->Unit){
    val scheme=MaterialTheme.colorScheme
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val home = screen=="home"
    val container = if(home) scheme.primaryContainer else scheme.surfaceContainerHigh
    val content = if(home) scheme.onPrimaryContainer else scheme.onSurface
    val topBrush = if(home) Brush.linearGradient(listOf(scheme.primaryContainer,scheme.secondaryContainer,scheme.tertiaryContainer.copy(alpha=.72f))) else Brush.verticalGradient(listOf(scheme.surfaceContainerHigh,scheme.surfaceContainer))
    Surface(
        modifier=Modifier.fillMaxWidth(),
        color=container,
        contentColor=content,
        tonalElevation=4.dp,
        shadowElevation=1.dp,
        shape=RoundedCornerShape(bottomStart=28.dp,bottomEnd=28.dp)
    ){
        Column(
            Modifier
                .fillMaxWidth()
                .background(topBrush)
                .padding(start=20.dp,end=20.dp,top=10.dp,bottom=10.dp),
            verticalArrangement=Arrangement.spacedBy(8.dp)
        ){
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(0.dp)){
                    Text(if(home) "DsimJ" else when(screen){"tools"->"Tools";"settings"->"Settings";"esim"->"eSIM";else->"SIM"},style=MaterialTheme.typography.titleLarge,color=content,maxLines=1,overflow=TextOverflow.Ellipsis)
                    Text(when(screen){"home"->"SIM 号码库";"tools"->"常用工具";"settings"->"外观";"esim"->"eSIM 管理";else->"号码"},style=MaterialTheme.typography.labelMedium,color=content.copy(alpha=.68f),maxLines=1,overflow=TextOverflow.Ellipsis)
                }
                if(home){
                    FilledTonalIconButton(onClick={on("grid")},shape=RoundedCornerShape(18.dp),colors=IconButtonDefaults.filledTonalIconButtonColors(containerColor=scheme.secondaryContainer,contentColor=scheme.onSecondaryContainer)){
                        Icon(Icons.Rounded.Public,contentDescription=null)
                    }
                }
                FilledTonalIconButton(onClick=onToggleDark,shape=RoundedCornerShape(18.dp),colors=IconButtonDefaults.filledTonalIconButtonColors(containerColor=scheme.surface.copy(alpha=.72f),contentColor=content)){
                    Icon(if(dark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,contentDescription=null)
                }
            }
            if(false && home){
                TextField(
                    value=search,
                    onValueChange=onSearch,
                    modifier=Modifier.fillMaxWidth().height(48.dp),
                    singleLine=true,
                    shape=RoundedCornerShape(23.dp),
                    placeholder={Text("搜索运营商、国家或号码",maxLines=1,overflow=TextOverflow.Ellipsis)},
                    leadingIcon={Icon(Icons.Rounded.Search,contentDescription=null)},
                    trailingIcon={ if(search.isNotBlank()) TextButton(onClick={onSearch("")}){Text("Clear")} },
                    textStyle=MaterialTheme.typography.bodyLarge,
                    colors=TextFieldDefaults.colors(
                        focusedContainerColor=scheme.surface,
                        unfocusedContainerColor=scheme.surface.copy(alpha=.92f),
                        focusedIndicatorColor=Color.Transparent,
                        unfocusedIndicatorColor=Color.Transparent,
                        cursorColor=scheme.primary,
                        focusedTextColor=scheme.onSurface,
                        unfocusedTextColor=scheme.onSurface,
                        focusedLeadingIconColor=scheme.primary,
                        unfocusedLeadingIconColor=scheme.onSurfaceVariant,
                        focusedPlaceholderColor=scheme.onSurfaceVariant,
                        unfocusedPlaceholderColor=scheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable fun ExpressiveAddFab(onClick:()->Unit){
    ExtendedFloatingActionButton(
        onClick=onClick,
        icon={Icon(Icons.Rounded.Add,contentDescription=null)},
        text={Text("添加 SIM",fontWeight=FontWeight.Bold)},
        shape=RoundedCornerShape(26.dp),
        containerColor=MaterialTheme.colorScheme.tertiaryContainer,
        contentColor=MaterialTheme.colorScheme.onTertiaryContainer,
        expanded=true
    )
}

@Composable fun SimHubTopBar(screen:String,dark:Boolean,onToggleDark:()->Unit,search:String,onSearch:(String)->Unit,on:(String)->Unit){
    ExpressiveSimHubTopBar(screen,dark,onToggleDark,search,onSearch,on)
    return
    val scheme=MaterialTheme.colorScheme
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    Column(
        Modifier
            .fillMaxWidth()
            .background(scheme.surface)
            .padding(start=20.dp,end=20.dp,top=statusBarTop+12.dp,bottom=10.dp),
        verticalArrangement=Arrangement.spacedBy(10.dp)
    ){
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(1.dp)){
                Text(if(screen=="home") "DsimJ" else when(screen){"tools"->L("工具");"settings"->L("设置");"esim"->"eSIM";else->L("号码")},style=MaterialTheme.typography.titleLarge,color=scheme.onSurface)
                Text(when(screen){"home"->L("号码");"tools"->L("常用工具");"settings"->L("外观");"esim"->L("eSIM 管理");else->L("号码")},style=MaterialTheme.typography.labelMedium,color=scheme.onSurfaceVariant)
            }
            IconCircle(if(dark) "☀" else "☾",onToggleDark)
        }
        if(screen=="home"){
            TextField(value=search,onValueChange=onSearch,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),singleLine=true,shape=RoundedCornerShape(28.dp),
                    placeholder={Text(L("搜索运营商、国家或号码"),fontSize=13.sp,color=Color(0xFF8E8E93),maxLines=1,overflow=TextOverflow.Ellipsis)},leadingIcon={Canvas(Modifier.size(16.dp)){drawCircle(Color(0xFF8E8E93),radius=size.width/2-1.dp.toPx(),style=Stroke(1.5.dp.toPx()));drawLine(Color(0xFF8E8E93),Offset(size.width*.65f,size.height*.65f),Offset(size.width*.85f,size.height*.85f),strokeWidth=1.5.dp.toPx())}},
                    colors=TextFieldDefaults.colors(focusedContainerColor=scheme.surfaceContainerHigh,unfocusedContainerColor=scheme.surfaceContainerHigh,focusedIndicatorColor=Color.Transparent,unfocusedIndicatorColor=Color.Transparent,cursorColor=scheme.primary,focusedTextColor=scheme.onSurface,unfocusedTextColor=scheme.onSurface,focusedPlaceholderColor=scheme.onSurfaceVariant,unfocusedPlaceholderColor=scheme.onSurfaceVariant))
        }
    }
}

@Composable fun IconCircle(text:String,onClick:()->Unit){
    val scheme=MaterialTheme.colorScheme
    Box(Modifier.size(42.dp).clip(RoundedCornerShape(21.dp)).background(scheme.secondaryContainer).border(.8.dp,scheme.outlineVariant.copy(alpha=.55f),RoundedCornerShape(21.dp)).motionClickable{onClick()},contentAlignment=Alignment.Center){Text(text,fontSize=17.sp,fontWeight=FontWeight.SemiBold,color=scheme.onSecondaryContainer)}
}

@Composable fun ExpressiveFilterToolRow(filter:String,sortMode:String,onFilter:(String)->Unit,onSort:()->Unit,count:Int){
    val scheme=MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){
        FilterChip(
            selected=filter!="全部",
            onClick={onFilter(when(filter){"全部"->"正常";"正常"->"即将到期";"即将到期"->"已过期";else->"全部"})},
            label={Text(filter,maxLines=1,overflow=TextOverflow.Ellipsis)},
            leadingIcon={Text("≡",fontWeight=FontWeight.Bold)},
            shape=RoundedCornerShape(18.dp),
            colors=FilterChipDefaults.filterChipColors(containerColor=scheme.surfaceContainerHigh,labelColor=scheme.onSurface,iconColor=scheme.primary,selectedContainerColor=scheme.primaryContainer,selectedLabelColor=scheme.onPrimaryContainer,selectedLeadingIconColor=scheme.onPrimaryContainer)
        )
        FilterChip(
            selected=sortMode!="自定义",
            onClick=onSort,
            label={Text(when(sortMode){"自定义"->"自定义";"到期近"->"近到远";else->"远到近"},maxLines=1,overflow=TextOverflow.Ellipsis)},
            leadingIcon={Text("↕",fontWeight=FontWeight.Bold)},
            shape=RoundedCornerShape(18.dp),
            colors=FilterChipDefaults.filterChipColors(containerColor=scheme.surfaceContainerHigh,labelColor=scheme.onSurface,iconColor=scheme.secondary,selectedContainerColor=scheme.secondaryContainer,selectedLabelColor=scheme.onSecondaryContainer,selectedLeadingIconColor=scheme.onSecondaryContainer)
        )
        Spacer(Modifier.weight(1f))
        Surface(shape=RoundedCornerShape(18.dp),color=scheme.tertiaryContainer,contentColor=scheme.onTertiaryContainer,tonalElevation=2.dp){
            Text(count.toString(),style=MaterialTheme.typography.labelLarge,modifier=Modifier.padding(horizontal=14.dp,vertical=8.dp))
        }
    }
}

@Composable fun FilterToolRow(filter:String,sortMode:String,onFilter:(String)->Unit,onSort:()->Unit,count:Int){
    ExpressiveFilterToolRow(filter,sortMode,onFilter,onSort,count)
    return
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){
            FilterTool("≡",filter,Modifier.height(30.dp)){onFilter(when(filter){"全部"->"正常";"正常"->"即将到期";"即将到期"->"已过期";else->"全部"})}
            FilterTool("↕",when(sortMode){"自定义"->"自定义";"到期近"->L("近到远");else->L("远到近")},Modifier.height(30.dp)){onSort()}
        }
        val scheme=MaterialTheme.colorScheme
        Box(Modifier.height(32.dp).clip(RoundedCornerShape(16.dp)).background(scheme.tertiaryContainer).padding(horizontal=12.dp),contentAlignment=Alignment.Center){Text("$count",fontSize=12.sp,fontWeight=FontWeight.Bold,color=scheme.onTertiaryContainer)}
    }
}

@Composable fun FilterTool(icon:String,text:String,m:Modifier,onClick:()->Unit){
    val scheme=MaterialTheme.colorScheme
    Row(m.height(32.dp).clip(RoundedCornerShape(16.dp)).background(scheme.primaryContainer).motionClickable{onClick()}.padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center){Text(icon,fontSize=12.sp,color=scheme.onPrimaryContainer);Spacer(Modifier.width(5.dp));Text(text,fontSize=12.sp,fontWeight=FontWeight.SemiBold,color=scheme.onPrimaryContainer,maxLines=1)}
}


@Composable fun ExpressiveSimCard(r:PhoneNumberRecord,onEdit:(PhoneNumberRecord)->Unit,onDel:(PhoneNumberRecord)->Unit,onTraffic:(PhoneNumberRecord)->Unit,onKeep:(PhoneNumberRecord,Int)->Unit,days:Long?,remindDays:Int,sorting:Boolean=false,onStartSort:()->Unit={},dragModifier:Modifier=Modifier,isDragging:Boolean=false,showFlag:Boolean=true,bankCardStyle:Boolean=false){
    val scheme=MaterialTheme.colorScheme
    val countryIso=countryIsoFor(r.countryCode,r.countryName)
    val palette=flagPaletteFor(countryIso,r.countryCode,r.countryName)
    val editActionColor=usableFlagActionColor(palette.primary,palette.secondary)
    val keepActionColor=usableFlagActionColor(palette.primary,palette.secondary)
    val trafficActionColor=usableFlagActionColor(palette.secondary,palette.primary)
    val decorated=showFlag || bankCardStyle
    val progress=when{days==null->.28f; days<0->.05f; else->(days.coerceIn(0,120).toFloat()/120f).coerceIn(.08f,.98f)}
    val statusContainer=when{days==null->scheme.surfaceVariant; days<0->scheme.errorContainer; days<=remindDays->palette.secondary.copy(alpha=.18f); else->palette.soft}
    val statusContent=when{days==null->scheme.onSurfaceVariant; days<0->scheme.onErrorContainer; else->palette.primary}
    val statusText=when{days==null->"无到期"; days<0->"已过期 ${-days} 天"; days<=remindDays->"${days} 天后到期"; else->"剩余 ${days} 天"}
    var hidden by remember{ mutableStateOf(true) }
    var del by remember{ mutableStateOf(false) }
    var keep by remember{ mutableStateOf(false) }
    var menu by remember{ mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    ElevatedCard(
        shape=RoundedCornerShape(topStart=34.dp,topEnd=20.dp,bottomStart=22.dp,bottomEnd=34.dp),
        colors=CardDefaults.elevatedCardColors(containerColor=if(decorated) Color.Transparent else scheme.surfaceContainerLow),
        elevation=CardDefaults.elevatedCardElevation(defaultElevation=if(isDragging)10.dp else 2.dp),
        modifier=Modifier
            .fillMaxWidth()
            .graphicsLayer{ scaleX=if(isDragging)1.018f else 1f; scaleY=if(isDragging)1.018f else 1f }
            .then(if(sorting) dragModifier else Modifier)
    ){
        Box(Modifier.fillMaxWidth()){
            if(decorated){
                FlagArtPanel(r,Modifier.matchParentSize(),bankCardStyle)
            }
        Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            ListItem(
                colors=ListItemDefaults.colors(containerColor=Color.Transparent),
                leadingContent={
                    Box(Modifier.size(62.dp),contentAlignment=Alignment.Center){
                        OperatorLogo44(r.operator.ifBlank{r.countryName}, Countries.list.firstOrNull{it.code==r.countryCode && it.name==r.countryName}?.iso ?: Countries.list.firstOrNull{it.code==r.countryCode}?.iso)
                    }
                },
                overlineContent={Text(r.countryName.ifBlank{r.countryCode},style=MaterialTheme.typography.labelMedium,color=palette.primary,maxLines=1,overflow=TextOverflow.Ellipsis)},
                headlineContent={Text(r.operator.ifBlank{r.countryName},style=MaterialTheme.typography.titleLarge,color=scheme.onSurface,maxLines=1,overflow=TextOverflow.Ellipsis)},
                supportingContent={Text(planLine(r).ifBlank{"预付费 SIM"},style=MaterialTheme.typography.bodyMedium,color=scheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)},
                trailingContent={
                    if(sorting){
                        Icon(Icons.Rounded.DragIndicator,contentDescription=null,tint=scheme.onSurfaceVariant)
                    }else{
                        Box{
                            IconButton(onClick={menu=true},modifier=Modifier.size(42.dp)){Icon(Icons.Rounded.MoreVert,contentDescription=null)}
                            DropdownMenu(expanded=menu,onDismissRequest={menu=false}){
                                DropdownMenuItem(text={Text("编辑")},leadingIcon={Icon(Icons.Rounded.Edit,contentDescription=null)},onClick={menu=false;onEdit(r)})
                                DropdownMenuItem(text={Text("复制号码")},leadingIcon={Icon(Icons.Rounded.ContentCopy,contentDescription=null)},onClick={menu=false;clipboardManager.setText(AnnotatedString(r.number))})
                                DropdownMenuItem(text={Text("排序")},leadingIcon={Icon(Icons.Rounded.DragIndicator,contentDescription=null)},onClick={menu=false;onStartSort()})
                                DropdownMenuItem(text={Text("删除")},leadingIcon={Icon(Icons.Rounded.Delete,contentDescription=null)},onClick={menu=false;del=true})
                            }
                        }
                    }
                }
            )
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){
                AssistChip(onClick={},label={Text(statusText,maxLines=1)},colors=AssistChipDefaults.assistChipColors(containerColor=statusContainer,labelColor=statusContent),border=null)
                AssistChip(onClick={},label={Text(r.balance.ifBlank{estimateBalance(r)},maxLines=1,overflow=TextOverflow.Ellipsis)},colors=AssistChipDefaults.assistChipColors(containerColor=palette.soft.copy(alpha=.82f),labelColor=palette.ink),border=null)
            }
            Surface(shape=RoundedCornerShape(24.dp),color=Color.White.copy(alpha=if(decorated) .58f else .92f),contentColor=palette.ink,modifier=Modifier.fillMaxWidth().border(1.dp,palette.primary.copy(alpha=.10f),RoundedCornerShape(24.dp))){
                Row(Modifier.padding(horizontal=14.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
                    Text("${r.countryCode} ${if(hidden) "---- ${r.number.takeLast(4)}" else formatNumber(r.number)}",style=MaterialTheme.typography.titleMedium,color=palette.ink,maxLines=1,overflow=TextOverflow.Ellipsis,modifier=Modifier.weight(1f))
                    IconButton(onClick={hidden=!hidden},modifier=Modifier.size(38.dp)){Icon(if(hidden) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,contentDescription=null,tint=palette.primary)}
                }
            }
            FlagProgressBar(
                progress=progress,
                palette=palette,
                expired=days!=null && days<0,
                warning=days!=null && days<=remindDays,
                modifier=Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp),verticalAlignment=Alignment.CenterVertically){
                FilledTonalIconButton(onClick={onEdit(r)},shape=RoundedCornerShape(18.dp),colors=IconButtonDefaults.filledTonalIconButtonColors(containerColor=editActionColor,contentColor=readableOnFlagColor(editActionColor))){Icon(Icons.Rounded.Edit,contentDescription=null)}
                FilledTonalButton(onClick={keep=true},shape=RoundedCornerShape(18.dp),modifier=Modifier.weight(1f),colors=ButtonDefaults.filledTonalButtonColors(containerColor=keepActionColor,contentColor=readableOnFlagColor(keepActionColor))){Icon(Icons.Rounded.Refresh,contentDescription=null);Spacer(Modifier.width(6.dp));Text("保号")}
                FilledTonalButton(onClick={onTraffic(r)},shape=RoundedCornerShape(18.dp),modifier=Modifier.weight(1f),colors=ButtonDefaults.filledTonalButtonColors(containerColor=trafficActionColor,contentColor=readableOnFlagColor(trafficActionColor))){Icon(Icons.Rounded.Phone,contentDescription=null);Spacer(Modifier.width(6.dp));Text("流量")}
                IconButton(onClick={del=true},modifier=Modifier.size(48.dp)){Icon(Icons.Rounded.Delete,contentDescription=null,tint=scheme.error)}
            }
        }
    }
    }
    if(keep) KeepCycleDialog(r,onKeep){keep=false}
    if(del) IOSConfirmDialog("删除 SIM？","删除 ${r.countryCode} ${formatNumber(r.number)} 后不可恢复。",true,{del=false},{del=false;onDel(r)})
}

@OptIn(ExperimentalFoundationApi::class)
@Composable fun CompactSimCard(r:PhoneNumberRecord,on编辑:(PhoneNumberRecord)->Unit,onDel:(PhoneNumberRecord)->Unit,onTraffic:(PhoneNumberRecord)->Unit,onKeep:(PhoneNumberRecord,Int)->Unit,days:Long?,remindDays:Int,showFlag:Boolean=true,dark:Boolean=false,bankCardStyle:Boolean=false,sorting:Boolean=false,onStartSort:()->Unit={},dragModifier:Modifier=Modifier,isDragging:Boolean=false){
    ExpressiveSimCard(r,on编辑,onDel,onTraffic,onKeep,days,remindDays,sorting,onStartSort,dragModifier,isDragging,showFlag,bankCardStyle)
    return
    val progress=when{days==null->.35f; days<0->.04f; else->(days.coerceIn(0,120).toFloat()/120f).coerceIn(.08f,.98f)}
    var hidden by remember{ mutableStateOf(true) }
    var del by remember{ mutableStateOf(false) }
    var keep by remember{ mutableStateOf(false) }
    var showMenu by remember{ mutableStateOf(false) }
    val scheme=MaterialTheme.colorScheme
    val decorated=showFlag || bankCardStyle
    val cardBg=if(decorated) scheme.surface.copy(alpha=if(dark) .42f else .36f) else scheme.surfaceContainerHigh
    val cardBorder=if(decorated) scheme.outlineVariant.copy(alpha=.48f) else scheme.outlineVariant.copy(alpha=.80f)
    val txtPrimary=if(decorated) Color.White else scheme.onSurface
    val txtSecondary=if(decorated) Color.White.copy(alpha=.82f) else scheme.onSurfaceVariant
    val txtBody=if(decorated) Color.White.copy(alpha=.9f) else scheme.onSurfaceVariant
    val accent=scheme.primary
    val success=Color(0xFF34A853)
    val clipboardManager = LocalClipboardManager.current
    Card(shape=RoundedCornerShape(28.dp),colors=CardDefaults.cardColors(containerColor=cardBg),elevation=CardDefaults.cardElevation(if(isDragging) 10.dp else 1.5.dp),modifier=Modifier.fillMaxWidth().then(if(bankCardStyle) Modifier.aspectRatio(1.60f) else Modifier.height(156.dp)).graphicsLayer{ scaleX=if(isDragging) 1.025f else 1f; scaleY=if(isDragging) 1.025f else 1f; shadowElevation=if(isDragging) 22f else 0f }.border(1.dp,cardBorder,RoundedCornerShape(28.dp)).then(if(sorting) dragModifier else Modifier.combinedClickable(onClick={},onLongClick={showMenu=true}))){
        Box(Modifier.fillMaxSize()){
            val glass=if(decorated) listOf(Color.White.copy(alpha=.18f),Color.White.copy(alpha=.07f),Color.Black.copy(alpha=.10f)) else listOf(scheme.surfaceContainerHigh,scheme.surfaceContainer,scheme.surfaceContainerHigh)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(glass)).clip(RoundedCornerShape(28.dp)))
            if(showFlag || bankCardStyle){
                FlagArtPanel(r,Modifier.fillMaxSize(),bankCardStyle)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha=.16f),Color.Black.copy(alpha=.34f)))))
            }
            Column(Modifier.fillMaxSize().padding(start=12.dp,end=12.dp,top=9.dp,bottom=8.dp),verticalArrangement=Arrangement.SpaceBetween){
                Row(verticalAlignment=Alignment.CenterVertically){
                    OperatorLogo44(r.operator.ifBlank{r.countryName}, Countries.list.firstOrNull{it.code==r.countryCode && it.name==r.countryName}?.iso ?: Countries.list.firstOrNull{it.code==r.countryCode}?.iso)
                    Spacer(Modifier.width(8.dp))
                    Text(r.operator.ifBlank{r.countryName},fontSize=16.sp,fontWeight=FontWeight.Bold,color=txtPrimary,maxLines=1,overflow=TextOverflow.Ellipsis,modifier=Modifier.weight(1f))
                    if(r.longTerm) Text("Long-term",fontSize=9.sp,fontWeight=FontWeight.Bold,color=scheme.onSecondaryContainer,modifier=Modifier.clip(RoundedCornerShape(999.dp)).background(scheme.secondaryContainer).padding(horizontal=7.dp,vertical=3.dp))
                    else Text("∞",fontSize=11.sp,fontWeight=FontWeight.Bold,color=scheme.onPrimaryContainer,modifier=Modifier.clip(RoundedCornerShape(999.dp)).background(scheme.primaryContainer).padding(horizontal=7.dp,vertical=3.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(r.countryName,fontSize=12.sp,color=txtSecondary,maxLines=1,overflow=TextOverflow.Ellipsis,modifier=Modifier.widthIn(max=72.dp))
                    if(sorting){ Spacer(Modifier.width(6.dp)); Text("≡",fontSize=20.sp,fontWeight=FontWeight.Bold,color=txtSecondary) }
                }
                Row(verticalAlignment=Alignment.CenterVertically){
                    Text(planLine(r)+" · ",fontSize=12.sp,color=txtBody,maxLines=1,overflow=TextOverflow.Ellipsis)
                    Box(Modifier.size(14.dp).clip(RoundedCornerShape(99.dp)).background(success),contentAlignment=Alignment.Center){Text("✓",fontSize=8.sp,color=Color.White)}
                    Spacer(Modifier.width(4.dp))
                    Text("${formatDateByLang(r.expireDate, LocalAppLanguage.current)} · ${expireText(LocalAppLanguage.current,days)}",fontSize=12.sp,color=if(decorated) Color(0xFFB9F6CA) else success,fontWeight=FontWeight.SemiBold,maxLines=1,overflow=TextOverflow.Ellipsis)
                }
                Row(verticalAlignment=Alignment.CenterVertically){
                    Text("☎ ${r.countryCode} ${if(hidden) "•••• ${r.number.takeLast(4)}" else formatNumber(r.number)}",fontSize=15.sp,fontWeight=FontWeight.Medium,color=txtPrimary,maxLines=1,overflow=TextOverflow.Ellipsis,modifier=Modifier.weight(1f))
                    Text(r.balance.ifBlank{estimateBalance(r)},fontSize=14.sp,fontWeight=FontWeight.SemiBold,color=if(decorated) Color(0xFFD7E2FF) else accent,maxLines=1)
                    Spacer(Modifier.width(8.dp))
                    Text(if(hidden)"◉" else "◎",fontSize=16.sp,color=txtBody,modifier=Modifier.clickable{hidden=!hidden})
                }
                Row(verticalAlignment=Alignment.CenterVertically){Text("EID ${r.eid.ifBlank{fakeEidForCard(r)}}",fontSize=10.sp,color=txtSecondary,maxLines=1,overflow=TextOverflow.Ellipsis,modifier=Modifier.weight(1f)); Text(signalIcon(r.signalStatus)+" "+r.signalStatus,fontSize=10.sp,color=if(decorated) Color(0xFFB9F6CA) else success,maxLines=1)}
                Box(Modifier.fillMaxWidth(.82f).height(5.dp).clip(RoundedCornerShape(3.dp)).background(if(decorated) Color.White.copy(alpha=.28f) else scheme.surfaceVariant)){Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(success))}
                Spacer(Modifier.height(2.dp))

            }
            if(showMenu){
                val mEdit=L("编辑"); val mCopy=L("复制号码"); val mKeep=L("保号"); val mTraffic=L("刷流量"); val mSort="排序"; val mDel=L("删除")
                Popup(alignment=Alignment.Center,onDismissRequest={showMenu=false}){
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=0.35f)).clickable{showMenu=false},contentAlignment=Alignment.Center){
                        Card(shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=scheme.surfaceContainerHigh),elevation=CardDefaults.cardElevation(12.dp),modifier=Modifier.widthIn(min=220.dp,max=280.dp)){
                            Column(Modifier.padding(vertical=4.dp)){
                                data class MenuItem(val label:String, val isDel:Boolean=false, val action:()->Unit)
                                val items=listOf(
                                    MenuItem(mEdit){showMenu=false;on编辑(r)},
                                    MenuItem(mCopy){showMenu=false;clipboardManager.setText(AnnotatedString(r.number))},
                                    MenuItem(mKeep){showMenu=false;keep=true},
                                    MenuItem(mTraffic){showMenu=false;onTraffic(r)},
                                    MenuItem(mSort){showMenu=false;onStartSort()},
                                    MenuItem(mDel,isDel=true){showMenu=false;del=true},
                                )
                                items.forEachIndexed{idx,item->
                                    Box(Modifier.fillMaxWidth().motionClickable(pressedScale=.985f){item.action()}.padding(horizontal=20.dp,vertical=13.dp)){
                                        Text(item.label,fontSize=15.sp,fontWeight=FontWeight.Normal,color=if(item.isDel) scheme.error else scheme.onSurface)
                                    }
                                    if(idx<items.size-1) Box(Modifier.padding(horizontal=20.dp).fillMaxWidth().height(0.5.dp).background(scheme.outlineVariant))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if(keep) KeepCycleDialog(r,onKeep){keep=false}
    if(del) IOSConfirmDialog(L("删除号码？"),L("删除")+" ${r.countryCode} ${formatNumber(r.number)} "+L("删除后不可恢复"),true,{del=false},{del=false;onDel(r)})
}

@Composable fun MiniAction(text:String,color:Color,onClick:()->Unit){
    Box(Modifier.widthIn(min=48.dp,max=62.dp).height(28.dp).clip(RoundedCornerShape(16.dp)).background(color.copy(alpha=.10f)).motionClickable{onClick()},contentAlignment=Alignment.Center){Text(text,fontSize=11.sp,fontWeight=FontWeight.SemiBold,color=color,maxLines=1)}
}

@Composable fun CardIconAction(type:String,color:Color,onClick:()->Unit){
    Box(Modifier.width(30.dp).height(40.dp).clip(RoundedCornerShape(13.dp)).background(color.copy(alpha=.14f)).motionClickable{onClick()},contentAlignment=Alignment.Center){
        Canvas(Modifier.width(16.dp).height(26.dp)){
            val w=size.width; val h=size.height; val st=Stroke(width=1.9f)
            when(type){
                "keep"->{ // shield / 保号
                    drawLine(color,Offset(w*.5f,h*.16f),Offset(w*.82f,h*.30f),strokeWidth=1.9f)
                    drawLine(color,Offset(w*.82f,h*.30f),Offset(w*.82f,h*.54f),strokeWidth=1.9f)
                    drawLine(color,Offset(w*.82f,h*.54f),Offset(w*.5f,h*.86f),strokeWidth=1.9f)
                    drawLine(color,Offset(w*.5f,h*.86f),Offset(w*.18f,h*.54f),strokeWidth=1.9f)
                    drawLine(color,Offset(w*.18f,h*.54f),Offset(w*.18f,h*.30f),strokeWidth=1.9f)
                    drawLine(color,Offset(w*.18f,h*.30f),Offset(w*.5f,h*.16f),strokeWidth=1.9f)
                    drawLine(color,Offset(w*.36f,h*.50f),Offset(w*.46f,h*.62f),strokeWidth=1.9f)
                    drawLine(color,Offset(w*.46f,h*.62f),Offset(w*.66f,h*.38f),strokeWidth=1.9f)
                }
                "traffic"->{ // bars / 刷流量
                    drawLine(color,Offset(w*.24f,h*.80f),Offset(w*.24f,h*.56f),strokeWidth=2.4f)
                    drawLine(color,Offset(w*.5f,h*.80f),Offset(w*.5f,h*.36f),strokeWidth=2.4f)
                    drawLine(color,Offset(w*.76f,h*.80f),Offset(w*.76f,h*.18f),strokeWidth=2.4f)
                }
                "edit"->{ // pencil / 编辑
                    drawLine(color,Offset(w*.26f,h*.74f),Offset(w*.70f,h*.30f),strokeWidth=2.0f)
                    drawLine(color,Offset(w*.70f,h*.30f),Offset(w*.82f,h*.42f),strokeWidth=2.0f)
                    drawLine(color,Offset(w*.82f,h*.42f),Offset(w*.38f,h*.86f),strokeWidth=2.0f)
                    drawLine(color,Offset(w*.38f,h*.86f),Offset(w*.20f,h*.86f),strokeWidth=2.0f)
                    drawLine(color,Offset(w*.20f,h*.86f),Offset(w*.26f,h*.74f),strokeWidth=2.0f)
                }
                else->{ // trash / 删除
                    drawLine(color,Offset(w*.22f,h*.30f),Offset(w*.78f,h*.30f),strokeWidth=2.0f)
                    drawLine(color,Offset(w*.40f,h*.30f),Offset(w*.42f,h*.18f),strokeWidth=2.0f)
                    drawLine(color,Offset(w*.42f,h*.18f),Offset(w*.58f,h*.18f),strokeWidth=2.0f)
                    drawLine(color,Offset(w*.58f,h*.18f),Offset(w*.60f,h*.30f),strokeWidth=2.0f)
                    drawLine(color,Offset(w*.28f,h*.30f),Offset(w*.32f,h*.84f),strokeWidth=2.0f)
                    drawLine(color,Offset(w*.32f,h*.84f),Offset(w*.68f,h*.84f),strokeWidth=2.0f)
                    drawLine(color,Offset(w*.68f,h*.84f),Offset(w*.72f,h*.30f),strokeWidth=2.0f)
                    drawLine(color,Offset(w*.5f,h*.40f),Offset(w*.5f,h*.74f),strokeWidth=1.6f)
                }
            }
        }
    }
}

@Composable fun OperatorLogo44(name:String, iso:String?=null){
    val info=remember(name, iso){ OperatorDatabase.find(name, iso) }
    val display=info?.carrierName ?: name
    val localLogo = remember(display, name, iso){ OperatorLogoAssets.assetFor(display, iso).ifBlank { OperatorLogoAssets.assetFor(name, iso) } }
    val onlineLogo = if(localLogo.isBlank()) (info?.logoUrl ?: "") else ""
    val assetPath = localLogo.removePrefix("file:///android_asset/")
    val assetBitmap = rememberAssetBitmap(assetPath)
    val op=display.uppercase()
    val label=when{
        "移动" in display || "CHINA MOBILE" in op -> "CM"
        "联通" in display || "UNICOM" in op -> "CU"
        "电信" in display || "TELECOM" in op -> "CT"
        "广电" in display -> "CB"
        "GIFFGAFF" in op -> "giff"
        "3HK" in op || "THREE" in op -> "3"
        "HKT" in op || "CSL" in op -> "HKT"
        "SMARTONE" in op -> "ST"
        "CMHK" in op || "CHINA MOBILE HONG KONG" in op -> "CMHK"
        "RAKUTEN" in op -> "R"
        "SOFTBANK" in op -> "SB"
        "DOCOMO" in op -> "doc"
        "AIS" in op -> "AIS"
        "TRUE" in op -> "TRUE"
        "DTAC" in op -> "dtac"
        "VODAFONE" in op -> "V"
        "T-MOBILE" in op -> "T"
        "AT&T" in op -> "AT&T"
        "VERIZON" in op -> "VZ"
        info!=null -> display.split(" ").filter{it.isNotBlank()}.take(2).joinToString(""){it.first().uppercase()}.ifBlank{"SIM"}
        else -> "SIM"
    }
    val bg=when(label){"CM"->Color(0xFF0085D0);"CU"->Color(0xFFE60012);"CT"->Color(0xFF005BAC);"AIS"->Color(0xFF78BE20);"R"->Color(0xFFBF0000);"V"->Color(0xFFE60000);"T"->Color(0xFFE20074);else->Color(0xFF111827)}
    when{
        assetBitmap!=null -> Image(bitmap=assetBitmap,contentDescription=display,contentScale=ContentScale.Fit,modifier=Modifier.size(54.dp))
        onlineLogo.isNotBlank() -> AsyncImage(model=onlineLogo,contentDescription=display,contentScale=ContentScale.Fit,modifier=Modifier.size(54.dp))
        else -> {
            Box(Modifier.size(44.dp).clip(RoundedCornerShape(13.dp)).background(bg),contentAlignment=Alignment.Center){
                Text(label,fontSize=if(label.length>3) 8.sp else 12.sp,fontWeight=FontWeight.Bold,color=Color.White,maxLines=1)
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable fun SubscriptionCard(r:PhoneNumberRecord,on编辑:(PhoneNumberRecord)->Unit,onDel:(PhoneNumberRecord)->Unit,onTraffic:(PhoneNumberRecord)->Unit,onKeep:(PhoneNumberRecord,Int)->Unit,days:Long?,remindDays:Int){
    val status=if(days==null)L("未知") else if(days<0)L("已过期") else if(days<=remindDays)L("即将到期") else L("预付费")
    val theme=countryTheme(r.countryCode,r.countryName)
    val progress=when{days==null->.25f; days<0->1f; else->(1f-(days.coerceIn(0,365).toFloat()/365f)).coerceIn(.08f,.92f)}
    var del by remember{ mutableStateOf(false) }; var keep by remember{ mutableStateOf(false) }; var hidden by remember{ mutableStateOf(false) }
    Card(shape=RoundedCornerShape(24.dp),elevation=CardDefaults.cardElevation(3.dp),modifier=Modifier.fillMaxWidth().motionClickable(pressedScale=.985f){on编辑(r)}){
        Box(Modifier.background(Brush.linearGradient(theme)).padding(15.dp)){
            Column(verticalArrangement=Arrangement.spacedBy(9.dp)){
                Row(verticalAlignment=Alignment.CenterVertically){
                    OperatorLogo(r.operator.ifBlank{guessOperator(r.number, Countries.list.firstOrNull{it.code==r.countryCode}?.iso ?: r.countryName)})
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f)){
                        Row(verticalAlignment=Alignment.CenterVertically){ Text(r.operator.ifBlank{r.countryName},fontSize=18.sp,fontWeight=FontWeight.Bold,color=Color.White,maxLines=1,overflow=TextOverflow.Ellipsis); Spacer(Modifier.width(6.dp)); Text("CO",fontSize=9.sp,color=Color.White,modifier=Modifier.clip(RoundedCornerShape(5.dp)).background(Color(0xFF007AFF).copy(alpha=.75f)).padding(horizontal=4.dp,vertical=1.dp)) }
                        Text(r.countryName,fontSize=12.sp,color=Color.White.copy(alpha=.85f),maxLines=1,overflow=TextOverflow.Ellipsis)
                    }
                    Text(if(hidden)"◉" else "◎",fontSize=19.sp,color=Color.White.copy(alpha=.9f),modifier=Modifier.clickable{hidden=!hidden})
                }
                Row(verticalAlignment=Alignment.CenterVertically){ Text("✓",fontSize=12.sp,color=Color.White); Spacer(Modifier.width(5.dp)); Text(status,fontSize=12.sp,color=Color.White.copy(alpha=.92f)); Spacer(Modifier.width(7.dp)); Text("${r.expireDate} · ${if(days==null)"未知" else if(days<0)"已过期 ${-days} 天" else "还有 ${days} 天"}",fontSize=12.sp,color=Color.White.copy(alpha=.92f),maxLines=1,overflow=TextOverflow.Ellipsis) }
                Text(if(hidden) maskNumber(formatNumber(r.number)) else "${r.countryCode} ${maskNumber(formatNumber(r.number))}",fontSize=20.sp,fontWeight=FontWeight.SemiBold,color=Color.White,maxLines=1,overflow=TextOverflow.Ellipsis)
                Text(r.note.ifBlank{L("预付费 / 保号套餐")},fontSize=12.sp,color=Color.White.copy(alpha=.82f),maxLines=1,overflow=TextOverflow.Ellipsis)
                Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(99.dp)).background(Color.White.copy(alpha=.28f))){ Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(Color(0xFF34C759))) }
                FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
                    WhitePill(L("保号")){keep=true}
                    WhitePill(L("刷流量")){onTraffic(r)}
                    WhitePill(L("删除"),danger=true){del=true}
                }
            }
        }
    }
    if(keep) KeepCycleDialog(r,onKeep){keep=false}
    if(del) IOSConfirmDialog(L("删除号码？"),L("删除")+" ${r.countryCode} ${formatNumber(r.number)} "+L("删除后不可恢复"),true,{del=false},{del=false;onDel(r)})
}

@Composable fun WhitePill(text:String,danger:Boolean=false,onClick:()->Unit){
    val c=if(danger) Color(0xFFFF3B30) else Color(0xFF007AFF)
    Text(text,fontSize=12.sp,fontWeight=FontWeight.SemiBold,color=c,modifier=Modifier.clip(RoundedCornerShape(99.dp)).background(Color.White.copy(alpha=.92f)).motionClickable{onClick()}.padding(horizontal=10.dp,vertical=5.dp))
}

private fun countryLookupKey(value:String):String {
    if(value.isBlank()) return ""
    val normalized = java.text.Normalizer.normalize(value.trim(), java.text.Normalizer.Form.NFKD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase(java.util.Locale.ROOT)
        .replace("&", "and")
    return normalized.replace(Regex("[^\\p{L}\\p{N}]+"), "")
}

private val countryNameIsoAliases = mapOf(
    "america" to "US",
    "usa" to "US",
    "unitedstatesofamerica" to "US",
    "uk" to "GB",
    "britain" to "GB",
    "greatbritain" to "GB",
    "taiwanprovinceofchina" to "TW",
    "chinesetaiwan" to "TW",
    "hongkongsarchina" to "HK",
    "hongkongsar" to "HK",
    "hongkongchina" to "HK",
    "macaosarchina" to "MO",
    "macaosar" to "MO",
    "macausarchina" to "MO",
    "macausar" to "MO",
    "macau" to "MO",
    "southkorea" to "KR",
    "republicofkorea" to "KR",
    "korea" to "KR",
    "russia" to "RU",
    "russianfederation" to "RU",
    "northmacedonia" to "MK",
    "macedonia" to "MK",
    "iran" to "IR",
    "iranislamicrepublicof" to "IR",
    "syria" to "SY",
    "syrianarabrepublic" to "SY",
    "laos" to "LA",
    "laopeoplesdemocraticrepublic" to "LA",
    "brunei" to "BN",
    "bruneidarussalam" to "BN",
    "bolivia" to "BO",
    "boliviaplurinationalstateof" to "BO",
    "venezuela" to "VE",
    "venezuelabolivarianrepublicof" to "VE",
    "tanzania" to "TZ",
    "tanzaniaunitedrepublicof" to "TZ",
    "reunion" to "RE"
)

private val localeCountryIsoAliases:Map<String,String> by lazy {
    val map = mutableMapOf<String,String>()
    fun add(label:String, iso:String) {
        val key = countryLookupKey(label)
        if(key.isNotBlank() && !map.containsKey(key)) map[key] = iso
    }
    Countries.list.forEach { c ->
        val locale = java.util.Locale("", c.iso)
        add(c.iso, c.iso)
        add(c.flag, c.iso)
        add(c.name, c.iso)
        add(locale.getDisplayCountry(java.util.Locale.ENGLISH), c.iso)
        add(locale.getDisplayCountry(java.util.Locale.SIMPLIFIED_CHINESE), c.iso)
        add(locale.getDisplayCountry(java.util.Locale.TRADITIONAL_CHINESE), c.iso)
    }
    countryNameIsoAliases.forEach { (key, iso) -> map[key] = iso }
    map
}

fun countryIsoFor(code:String,name:String):String {
    val raw = name.trim()
    val upper = raw.uppercase(java.util.Locale.ROOT)
    if(upper.length == 2 && Countries.list.any { it.iso == upper }) return upper
    localeCountryIsoAliases[countryLookupKey(raw)]?.let { return it }
    return Countries.list.firstOrNull { it.code == code && (it.name == raw || it.iso.equals(raw, true) || it.flag == raw) }?.iso
        ?: Countries.list.firstOrNull { it.code == code }?.iso
        ?: ""
}

fun countryTheme(code:String,name:String):List<Color> =
    flagColorsForIso(countryIsoFor(code,name))


data class FlagPalette(
    val primary:Color,
    val secondary:Color,
    val accent:Color,
    val soft:Color,
    val ink:Color
)

fun flagPaletteFor(code:String,name:String):FlagPalette =
    flagPaletteFor(countryIsoFor(code,name),code,name)

fun flagPaletteFor(iso:String,code:String,name:String):FlagPalette{
    val colors=flagColorsForIso(iso.ifBlank{countryIsoFor(code,name)})
    val primary=colors.getOrElse(0){Color(0xFF2563EB)}
    val secondary=colors.getOrElse(1){Color(0xFF0EA5E9)}
    val accent=colors.getOrElse(2){secondary}
    val soft=mixFlagColor(mixFlagColor(primary,secondary,.34f),Color.White,.78f)
    val ink=if(flagColorBrightness(soft)>.55f) Color(0xFF172033) else Color.White
    return FlagPalette(primary,secondary,accent,soft,ink)
}

private fun flagColorsForIso(iso:String):List<Color>{
    val code=iso.uppercase()
    return countryFlagColors[code] ?: generatedFlagColors(code)
}

private fun generatedFlagColors(iso:String):List<Color>{
    val seed=iso.fold(0){acc,c->acc*31+c.code}
    val hue=((seed % 360)+360)%360
    val primary=Color(android.graphics.Color.HSVToColor(floatArrayOf(hue.toFloat(),.78f,.70f)))
    val secondary=Color(android.graphics.Color.HSVToColor(floatArrayOf(((hue+42)%360).toFloat(),.72f,.84f)))
    val accent=Color(android.graphics.Color.HSVToColor(floatArrayOf(((hue+176)%360).toFloat(),.58f,.86f)))
    return listOf(primary,secondary,accent)
}

private fun mixFlagColor(a:Color,b:Color,t:Float):Color{
    val x=t.coerceIn(0f,1f)
    return Color(
        red=a.red+(b.red-a.red)*x,
        green=a.green+(b.green-a.green)*x,
        blue=a.blue+(b.blue-a.blue)*x,
        alpha=a.alpha+(b.alpha-a.alpha)*x
    )
}

private val countryFlagColors=mapOf(
    "CN" to listOf(Color(0xFFDE2910),Color(0xFFFFDE00),Color(0xFFFFF0B3)),
    "HK" to listOf(Color(0xFFDE2910),Color.White,Color(0xFFFFD6D6)),
    "MO" to listOf(Color(0xFF00785E),Color(0xFFFFD348),Color.White),
    "TW" to listOf(Color(0xFFFE0000),Color(0xFF000095),Color.White),
    "US" to listOf(Color(0xFF3C3B6E),Color(0xFFB22234),Color.White),
    "CA" to listOf(Color(0xFFD52B1E),Color.White,Color(0xFFFFD6D0)),
    "GB" to listOf(Color(0xFF012169),Color(0xFFC8102E),Color.White),
    "DE" to listOf(Color(0xFF111111),Color(0xFFDD0000),Color(0xFFFFCE00)),
    "FR" to listOf(Color(0xFF0055A4),Color(0xFFEF4135),Color.White),
    "IT" to listOf(Color(0xFF009246),Color(0xFFCE2B37),Color.White),
    "ES" to listOf(Color(0xFFC60B1E),Color(0xFFFFC400),Color(0xFFAA151B)),
    "NL" to listOf(Color(0xFF21468B),Color(0xFFAE1C28),Color.White),
    "BE" to listOf(Color(0xFF111111),Color(0xFFED2939),Color(0xFFFFD90C)),
    "CH" to listOf(Color(0xFFFF0000),Color.White,Color(0xFFFFCFCF)),
    "AT" to listOf(Color(0xFFED2939),Color.White,Color(0xFFFFD6D6)),
    "SE" to listOf(Color(0xFF006AA7),Color(0xFFFECC00),Color(0xFFE5F4FF)),
    "NO" to listOf(Color(0xFF00205B),Color(0xFFBA0C2F),Color.White),
    "DK" to listOf(Color(0xFFC60C30),Color.White,Color(0xFFFFD7DF)),
    "FI" to listOf(Color(0xFF002F6C),Color.White,Color(0xFFE5F0FF)),
    "IS" to listOf(Color(0xFF02529C),Color(0xFFDC1E35),Color.White),
    "IE" to listOf(Color(0xFF169B62),Color(0xFFFF883E),Color.White),
    "PT" to listOf(Color(0xFF006600),Color(0xFFFF0000),Color(0xFFFFCC00)),
    "GR" to listOf(Color(0xFF0D5EAF),Color.White,Color(0xFFE8F2FF)),
    "PL" to listOf(Color(0xFFDC143C),Color.White,Color(0xFFFFD9E1)),
    "CZ" to listOf(Color(0xFF11457E),Color(0xFFD7141A),Color.White),
    "SK" to listOf(Color(0xFF0B4EA2),Color(0xFFEE1C25),Color.White),
    "HU" to listOf(Color(0xFF436F4D),Color(0xFFCD2A3E),Color.White),
    "RO" to listOf(Color(0xFF002B7F),Color(0xFFFCD116),Color(0xFFCE1126)),
    "BG" to listOf(Color(0xFF00966E),Color(0xFFD62612),Color.White),
    "HR" to listOf(Color(0xFF171796),Color(0xFFFF0000),Color.White),
    "SI" to listOf(Color(0xFF005DA4),Color(0xFFFF0000),Color.White),
    "RS" to listOf(Color(0xFF0C4076),Color(0xFFC6363C),Color.White),
    "BA" to listOf(Color(0xFF002395),Color(0xFFFECB00),Color.White),
    "ME" to listOf(Color(0xFFC40308),Color(0xFFD3AE3B),Color(0xFF1D5E33)),
    "MK" to listOf(Color(0xFFD20000),Color(0xFFFFD700),Color(0xFFFFA400)),
    "AL" to listOf(Color(0xFFE41E20),Color(0xFF111111),Color(0xFFFFD9D9)),
    "LT" to listOf(Color(0xFF006A44),Color(0xFFFDB913),Color(0xFFC1272D)),
    "LV" to listOf(Color(0xFF9E3039),Color.White,Color(0xFFFFE0E4)),
    "EE" to listOf(Color(0xFF0072CE),Color(0xFF111111),Color.White),
    "MD" to listOf(Color(0xFF003DA5),Color(0xFFFFD100),Color(0xFFCE1126)),
    "BY" to listOf(Color(0xFFD22730),Color(0xFF00AF66),Color.White),
    "UA" to listOf(Color(0xFF0057B7),Color(0xFFFFD700),Color(0xFFE8F2FF)),
    "RU" to listOf(Color(0xFF0039A6),Color(0xFFD52B1E),Color.White),
    "KZ" to listOf(Color(0xFF00AFCA),Color(0xFFFFD100),Color(0xFFE8FBFF)),
    "GE" to listOf(Color(0xFFFF0000),Color.White,Color(0xFFFFDADA)),
    "AM" to listOf(Color(0xFFD90012),Color(0xFF0033A0),Color(0xFFF2A800)),
    "AZ" to listOf(Color(0xFF0098C3),Color(0xFFE00034),Color(0xFF00AE65)),
    "TR" to listOf(Color(0xFFE30A17),Color.White,Color(0xFFFFD8DA)),
    "IL" to listOf(Color(0xFF0038B8),Color.White,Color(0xFFE8F0FF)),
    "AE" to listOf(Color(0xFF00732F),Color(0xFFFF0000),Color(0xFF111111)),
    "SA" to listOf(Color(0xFF006C35),Color.White,Color(0xFFE3F7EA)),
    "QA" to listOf(Color(0xFF8A1538),Color.White,Color(0xFFFFDCE6)),
    "KW" to listOf(Color(0xFF007A3D),Color(0xFFCE1126),Color(0xFF111111)),
    "BH" to listOf(Color(0xFFCE1126),Color.White,Color(0xFFFFD9DE)),
    "OM" to listOf(Color(0xFFC8102E),Color(0xFF007A3D),Color.White),
    "JO" to listOf(Color(0xFF007A3D),Color(0xFFCE1126),Color(0xFF111111)),
    "LB" to listOf(Color(0xFFED1C24),Color(0xFF00843D),Color.White),
    "EG" to listOf(Color(0xFFCE1126),Color(0xFF111111),Color.White),
    "MA" to listOf(Color(0xFFC1272D),Color(0xFF006233),Color(0xFFFFD8D8)),
    "TN" to listOf(Color(0xFFE70013),Color.White,Color(0xFFFFD7DA)),
    "DZ" to listOf(Color(0xFF006233),Color(0xFFD21034),Color.White),
    "NG" to listOf(Color(0xFF008753),Color.White,Color(0xFFE4FFF3)),
    "KE" to listOf(Color(0xFF006600),Color(0xFFBB0000),Color(0xFF111111)),
    "ZA" to listOf(Color(0xFF007A4D),Color(0xFFFFB612),Color(0xFF002395)),
    "JP" to listOf(Color(0xFFBC002D),Color.White,Color(0xFFFFDEE6)),
    "KR" to listOf(Color(0xFFCD2E3A),Color(0xFF0047A0),Color.White),
    "SG" to listOf(Color(0xFFEF3340),Color.White,Color(0xFFFFDCE0)),
    "MY" to listOf(Color(0xFF010066),Color(0xFFCC0001),Color(0xFFFFCC00)),
    "TH" to listOf(Color(0xFF2D2A4A),Color(0xFFA51931),Color.White),
    "VN" to listOf(Color(0xFFDA251D),Color(0xFFFFD700),Color(0xFFFFE7A6)),
    "PH" to listOf(Color(0xFF0038A8),Color(0xFFCE1126),Color(0xFFFCD116)),
    "ID" to listOf(Color(0xFFCE1126),Color.White,Color(0xFFFFD9DE)),
    "KH" to listOf(Color(0xFF032EA1),Color(0xFFE00025),Color.White),
    "LA" to listOf(Color(0xFF002868),Color(0xFFCE1126),Color.White),
    "MM" to listOf(Color(0xFF34B233),Color(0xFFFFC400),Color(0xFFEA2839)),
    "BN" to listOf(Color(0xFFF7E017),Color(0xFF111111),Color(0xFFCF1126)),
    "IN" to listOf(Color(0xFFFF9933),Color(0xFF138808),Color(0xFF000080)),
    "PK" to listOf(Color(0xFF01411C),Color.White,Color(0xFFE4F7EA)),
    "BD" to listOf(Color(0xFF006A4E),Color(0xFFF42A41),Color(0xFFE5F7F0)),
    "LK" to listOf(Color(0xFFFFB700),Color(0xFF8D153A),Color(0xFF00534E)),
    "NP" to listOf(Color(0xFFDC143C),Color(0xFF003893),Color.White),
    "MV" to listOf(Color(0xFF007E3A),Color(0xFFD21034),Color.White),
    "AU" to listOf(Color(0xFF00008B),Color(0xFFE4002B),Color.White),
    "NZ" to listOf(Color(0xFF00247D),Color(0xFFCC142B),Color.White),
    "FJ" to listOf(Color(0xFF68BFE5),Color(0xFF002868),Color(0xFFCE1126)),
    "BR" to listOf(Color(0xFF009B3A),Color(0xFFFFDF00),Color(0xFF002776)),
    "AR" to listOf(Color(0xFF74ACDF),Color(0xFFF6B40E),Color.White),
    "CL" to listOf(Color(0xFF0039A6),Color(0xFFD52B1E),Color.White),
    "CO" to listOf(Color(0xFFFCD116),Color(0xFF003893),Color(0xFFCE1126)),
    "PE" to listOf(Color(0xFFD91023),Color.White,Color(0xFFFFD9DE)),
    "MX" to listOf(Color(0xFF006847),Color(0xFFCE1126),Color.White),
    "UY" to listOf(Color(0xFF0038A8),Color(0xFFFFD100),Color.White),
    "PY" to listOf(Color(0xFF0038A8),Color(0xFFD52B1E),Color.White),
    "BO" to listOf(Color(0xFF007934),Color(0xFFFCD116),Color(0xFFD52B1E)),
    "EC" to listOf(Color(0xFFFFDD00),Color(0xFF034EA2),Color(0xFFED1C24)),
    "VE" to listOf(Color(0xFFFFCC00),Color(0xFF00247D),Color(0xFFCF142B)),
    "CR" to listOf(Color(0xFF002B7F),Color(0xFFCE1126),Color.White),
    "PA" to listOf(Color(0xFF005293),Color(0xFFD21034),Color.White),
    "GT" to listOf(Color(0xFF4997D0),Color.White,Color(0xFF2E7D32)),
    "DO" to listOf(Color(0xFF002D62),Color(0xFFCE1126),Color.White),
    "JM" to listOf(Color(0xFF009B3A),Color(0xFFFED100),Color(0xFF111111)),
    "LU" to listOf(Color(0xFF00A1DE),Color(0xFFEF3340),Color.White),
    "MT" to listOf(Color(0xFFCF142B),Color.White,Color(0xFFFFD9DE)),
    "CY" to listOf(Color(0xFFD57800),Color(0xFF4E5B31),Color.White),
    "MC" to listOf(Color(0xFFCE1126),Color.White,Color(0xFFFFD9DE)),
    "LI" to listOf(Color(0xFF002B7F),Color(0xFFCE1126),Color(0xFFFFD83D)),
    "AD" to listOf(Color(0xFF10069F),Color(0xFFFFCD00),Color(0xFFD50032)),
    "SM" to listOf(Color(0xFF5EB6E4),Color.White,Color(0xFFF3C300)),
    "XK" to listOf(Color(0xFF244AA5),Color(0xFFFFD500),Color.White),
    "GI" to listOf(Color(0xFFDA000C),Color.White,Color(0xFFFFC400)),
    "FO" to listOf(Color(0xFF0065BD),Color(0xFFED2939),Color.White),
    "IQ" to listOf(Color(0xFFCE1126),Color(0xFF007A3D),Color(0xFF111111)),
    "IR" to listOf(Color(0xFF239F40),Color(0xFFDA0000),Color.White),
    "SY" to listOf(Color(0xFFCE1126),Color(0xFF007A3D),Color(0xFF111111)),
    "YE" to listOf(Color(0xFFCE1126),Color(0xFF111111),Color.White),
    "PS" to listOf(Color(0xFF007A3D),Color(0xFFCE1126),Color(0xFF111111)),
    "AF" to listOf(Color(0xFF111111),Color(0xFFD32011),Color(0xFF007A36)),
    "UZ" to listOf(Color(0xFF1EB1E7),Color(0xFF009739),Color(0xFFCE1126)),
    "KG" to listOf(Color(0xFFE8112D),Color(0xFFFFD100),Color(0xFFFFE7A3)),
    "TJ" to listOf(Color(0xFF006747),Color(0xFFCC0000),Color(0xFFF8C300)),
    "TM" to listOf(Color(0xFF00843D),Color(0xFFD22630),Color.White),
    "MN" to listOf(Color(0xFFC4272F),Color(0xFF015197),Color(0xFFFFD900)),
    "BT" to listOf(Color(0xFFFFCC33),Color(0xFFFF4E12),Color.White),
    "TL" to listOf(Color(0xFFFFC726),Color(0xFFDC241F),Color(0xFF111111)),
    "PG" to listOf(Color(0xFFCE1126),Color(0xFF111111),Color(0xFFFFD100)),
    "WS" to listOf(Color(0xFFCE1126),Color(0xFF002B7F),Color.White),
    "TO" to listOf(Color(0xFFC10000),Color.White,Color(0xFFFFD9D9)),
    "VU" to listOf(Color(0xFF009543),Color(0xFFD21034),Color(0xFFFFCE00)),
    "SB" to listOf(Color(0xFF0051BA),Color(0xFF215B33),Color(0xFFFFD100)),
    "NC" to listOf(Color(0xFF009543),Color(0xFFED4135),Color(0xFF0035AD)),
    "PF" to listOf(Color(0xFFCE1126),Color.White,Color(0xFFFFD9DE)),
    "GU" to listOf(Color(0xFF003893),Color(0xFFEF3340),Color(0xFFFFD100)),
    "GH" to listOf(Color(0xFF006B3F),Color(0xFFFCD116),Color(0xFFCE1126)),
    "CI" to listOf(Color(0xFFF77F00),Color(0xFF009E60),Color.White),
    "SN" to listOf(Color(0xFF00853F),Color(0xFFFDEF42),Color(0xFFE31B23)),
    "CM" to listOf(Color(0xFF007A5E),Color(0xFFFCD116),Color(0xFFCE1126)),
    "ET" to listOf(Color(0xFF078930),Color(0xFFFCDD09),Color(0xFFDA121A)),
    "TZ" to listOf(Color(0xFF1EB53A),Color(0xFF00A3DD),Color(0xFFFCD116)),
    "UG" to listOf(Color(0xFFFFD100),Color(0xFFDA0000),Color(0xFF111111)),
    "ZM" to listOf(Color(0xFF198A00),Color(0xFFDE2010),Color(0xFFFF8200)),
    "ZW" to listOf(Color(0xFF319208),Color(0xFFFFD200),Color(0xFFE4002B)),
    "AO" to listOf(Color(0xFFCC092F),Color(0xFF111111),Color(0xFFFFCB00)),
    "MZ" to listOf(Color(0xFF009739),Color(0xFFFFD100),Color(0xFFD21034)),
    "MU" to listOf(Color(0xFF00A551),Color(0xFFEA2839),Color(0xFF1A206D)),
    "MG" to listOf(Color(0xFF007E3A),Color(0xFFFC3D32),Color.White),
    "RE" to listOf(Color(0xFF0055A4),Color(0xFFEF4135),Color(0xFFFFD100)),
    "LY" to listOf(Color(0xFF239E46),Color(0xFFE70013),Color(0xFF111111)),
    "SD" to listOf(Color(0xFF007229),Color(0xFFD21034),Color(0xFF111111)),
    "CD" to listOf(Color(0xFF007FFF),Color(0xFFF7D618),Color(0xFFCE1021)),
    "CG" to listOf(Color(0xFF009543),Color(0xFFFCD116),Color(0xFFDC241F)),
    "RW" to listOf(Color(0xFF00A1DE),Color(0xFF20603D),Color(0xFFFAD201)),
    "BW" to listOf(Color(0xFF75AADB),Color(0xFF111111),Color.White),
    "NA" to listOf(Color(0xFF003580),Color(0xFFD21034),Color(0xFF009543)),
    "BB" to listOf(Color(0xFF00267F),Color(0xFFFFC726),Color(0xFF111111)),
    "TT" to listOf(Color(0xFFDA1A35),Color(0xFF111111),Color.White),
    "BS" to listOf(Color(0xFF00ABC9),Color(0xFFFCD116),Color(0xFF111111)),
    "CU" to listOf(Color(0xFF002A8F),Color(0xFFCF142B),Color.White),
    "HN" to listOf(Color(0xFF00BCE4),Color.White,Color(0xFFE6FAFF)),
    "NI" to listOf(Color(0xFF0067C6),Color.White,Color(0xFFE7F2FF)),
    "SV" to listOf(Color(0xFF0047AB),Color.White,Color(0xFFE7F0FF)),
    "BZ" to listOf(Color(0xFF003F87),Color(0xFFD90F19),Color.White),
    "GY" to listOf(Color(0xFF009E49),Color(0xFFFCD116),Color(0xFFCE1126)),
    "SR" to listOf(Color(0xFF377E3F),Color(0xFFB40A2D),Color(0xFFFCD116))
)

fun flagColorBrightness(color:Color):Float = color.red*.299f + color.green*.587f + color.blue*.114f
fun readableOnFlagColor(color:Color):Color = if(flagColorBrightness(color)>.62f) Color(0xFF172033) else Color.White
fun usableFlagActionColor(color:Color,fallback:Color):Color = if(flagColorBrightness(color)>.88f) fallback else color

@Composable fun FlagProgressBar(progress:Float,palette:FlagPalette,expired:Boolean=false,warning:Boolean=false,modifier:Modifier=Modifier){
    val animated by animateFloatAsState(
        targetValue=progress.coerceIn(.04f,.98f),
        animationSpec=spring(),
        label="flagProgress"
    )
    val fillStart=when{
        expired -> Color(0xFFE53935)
        warning -> palette.secondary
        else -> palette.primary
    }
    val fillEnd=when{
        expired -> Color(0xFFFF8A80)
        warning -> palette.accent
        else -> palette.secondary
    }
    Box(
        modifier
            .fillMaxWidth()
            .height(16.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(Brush.horizontalGradient(listOf(Color.White.copy(alpha=.56f),palette.soft.copy(alpha=.72f),Color.White.copy(alpha=.38f))))
            .border(0.8.dp,Color.White.copy(alpha=.64f),RoundedCornerShape(99.dp))
            .padding(3.dp)
    ){
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(RoundedCornerShape(99.dp))
                .background(Brush.horizontalGradient(listOf(fillStart,fillEnd)))
        ){
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.White.copy(alpha=.32f),Color.Transparent))))
        }
        Canvas(Modifier.matchParentSize()){
            val h=size.height
            listOf(.25f,.5f,.75f).forEach{ mark->
                val x=size.width*mark
                drawCircle(Color.White.copy(alpha=.70f),radius=h*.16f,center=Offset(x,h*.50f))
                drawCircle(palette.primary.copy(alpha=.24f),radius=h*.09f,center=Offset(x,h*.50f))
            }
            val knobX=(size.width*animated).coerceIn(h*.45f,size.width-h*.45f)
            drawCircle(Color.White.copy(alpha=.94f),radius=h*.40f,center=Offset(knobX,h*.50f))
            drawCircle(fillEnd.copy(alpha=.90f),radius=h*.23f,center=Offset(knobX,h*.50f))
        }
    }
}

@Composable fun FlagArtPanel(r:PhoneNumberRecord,m:Modifier,bankCardStyle:Boolean=false){
    val iso = countryIsoFor(r.countryCode,r.countryName)
    val palette = flagPaletteFor(iso,r.countryCode,r.countryName)
    val colors=listOf(palette.primary,palette.secondary)
    val preferredAsset = cardBackgroundPath(r, iso, bankCardStyle)
    val assetJpg = if(iso.isBlank()) "" else "flag_backgrounds/${iso.lowercase()}.jpg"
    val assetPng = if(iso.isBlank()) "" else "flag_backgrounds/${iso.lowercase()}.png"
    val flagBitmap = rememberAssetBitmap(preferredAsset, bankCardStyle) ?: rememberAssetBitmap(assetJpg, bankCardStyle) ?: rememberAssetBitmap(assetPng, bankCardStyle)
    Box(m.background(Brush.linearGradient(colors)),contentAlignment=Alignment.Center){
        if(flagBitmap != null){
            Image(bitmap=flagBitmap,contentDescription=r.countryName,contentScale=ContentScale.Crop,modifier=Modifier.fillMaxSize().graphicsLayer(alpha=.92f))
        }else{
            when{
                r.countryCode=="+86" || r.countryName.contains("中国") -> Box(Modifier.fillMaxSize()){
                    Text("★",fontSize=46.sp,color=Color(0xFFFFD21F).copy(alpha=.92f),modifier=Modifier.align(Alignment.TopStart).padding(start=20.dp,top=16.dp).graphicsLayer(rotationZ=-8f))
                    Text("★",fontSize=15.sp,color=Color(0xFFFFD21F).copy(alpha=.88f),modifier=Modifier.align(Alignment.TopStart).padding(start=70.dp,top=13.dp).graphicsLayer(rotationZ=18f))
                    Text("★",fontSize=15.sp,color=Color(0xFFFFD21F).copy(alpha=.88f),modifier=Modifier.align(Alignment.TopStart).padding(start=84.dp,top=31.dp).graphicsLayer(rotationZ=36f))
                    Text("★",fontSize=15.sp,color=Color(0xFFFFD21F).copy(alpha=.88f),modifier=Modifier.align(Alignment.TopStart).padding(start=83.dp,top=53.dp).graphicsLayer(rotationZ=10f))
                    Text("★",fontSize=15.sp,color=Color(0xFFFFD21F).copy(alpha=.88f),modifier=Modifier.align(Alignment.TopStart).padding(start=68.dp,top=70.dp).graphicsLayer(rotationZ=-18f))
                }
                else -> {
                    Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(palette.primary.copy(alpha=.36f),palette.secondary.copy(alpha=.24f),palette.accent.copy(alpha=.18f)))))
                }
            }
        }
        TexturedFlagGlassOverlay(palette,Modifier.matchParentSize())
    }
}

@Composable fun TexturedFlagGlassOverlay(palette:FlagPalette,m:Modifier=Modifier){
    Canvas(m){
        val w=size.width
        val h=size.height
        if(w<=0f || h<=0f) return@Canvas
        drawRect(
            brush=Brush.horizontalGradient(
                colors=listOf(
                    Color.White.copy(alpha=.56f),
                    Color.White.copy(alpha=.35f),
                    Color.White.copy(alpha=.13f),
                    Color.Transparent,
                    palette.primary.copy(alpha=.18f),
                    Color.Black.copy(alpha=.22f)
                ),
                startX=0f,
                endX=w
            )
        )
        drawRect(
            brush=Brush.linearGradient(
                colors=listOf(Color.White.copy(alpha=.36f),Color.White.copy(alpha=.10f),Color.Transparent),
                start=Offset(-w*.12f,-h*.08f),
                end=Offset(w*.62f,h*.58f)
            )
        )
        drawRect(
            brush=Brush.radialGradient(
                colors=listOf(Color.White.copy(alpha=.26f),Color.Transparent),
                center=Offset(w*.14f,h*.12f),
                radius=w*.72f
            )
        )
        drawRect(
            brush=Brush.radialGradient(
                colors=listOf(palette.secondary.copy(alpha=.24f),Color.Transparent),
                center=Offset(w*.92f,h*.18f),
                radius=w*.56f
            )
        )
        repeat(7){ i->
            val y=h*(.10f+i*.14f)
            val crest=Path().apply{
                moveTo(-w*.12f,y)
                cubicTo(w*.16f,y-h*.15f,w*.44f,y+h*.17f,w*1.12f,y-h*.08f)
            }
            drawPath(crest,Color.White.copy(alpha=if(i%2==0).12f else .07f),style=Stroke(width=h*(if(i%2==0).030f else .018f)))
            val shadow=Path().apply{
                moveTo(-w*.10f,y+h*.028f)
                cubicTo(w*.18f,y-h*.10f,w*.48f,y+h*.22f,w*1.10f,y-h*.02f)
            }
            drawPath(shadow,Color.Black.copy(alpha=.045f),style=Stroke(width=h*.018f))
        }
        repeat(8){ i->
            val base=-w*.20f+i*w*.19f
            val streak=Path().apply{
                moveTo(base,-h*.22f)
                cubicTo(base+w*.20f,h*.18f,base+w*.06f,h*.55f,base+w*.30f,h*1.20f)
            }
            drawPath(streak,Color.White.copy(alpha=if(i%3==0).16f else .085f),style=Stroke(width=w*(if(i%3==0).010f else .005f)))
        }
        drawRect(
            brush=Brush.linearGradient(
                colors=listOf(Color.White.copy(alpha=.22f),Color.Transparent,Color.Transparent),
                start=Offset(0f,0f),
                end=Offset(w*.36f,h)
            )
        )
        drawRect(
            brush=Brush.radialGradient(
                colors=listOf(Color.Black.copy(alpha=.30f),Color.Transparent),
                center=Offset(w*1.05f,h*1.02f),
                radius=w*.88f
            )
        )
        drawRect(
            brush=Brush.verticalGradient(
                colors=listOf(Color.White.copy(alpha=.11f),Color.Transparent,Color.Black.copy(alpha=.12f)),
                startY=0f,
                endY=h
            )
        )
        drawRect(
            brush=Brush.linearGradient(
                colors=listOf(Color.White.copy(alpha=.18f),Color.Transparent,Color.White.copy(alpha=.08f)),
                start=Offset(w*.05f,0f),
                end=Offset(w*.95f,h)
            )
        )
    }
}

object AssetBitmapCache {
    private val cache = java.util.concurrent.ConcurrentHashMap<String, ImageBitmap>()
    fun cached(path:String): ImageBitmap? = cache[path]
    fun decode(ctx:Context, path:String): ImageBitmap? {
        if(path.isBlank()) return null
        cache[path]?.let { return it }
        val bmp = runCatching { ctx.assets.open(path).use { BitmapFactory.decodeStream(it)?.asImageBitmap() } }.getOrNull()
        if(bmp != null) cache[path] = bmp
        return bmp
    }
}

@Composable
fun rememberAssetBitmap(path:String, extraKey:Any? = null): ImageBitmap? {
    val ctx = LocalContext.current
    // 命中缓存直接同步返回，避免闪烁；未命中则后台线程解码，主线程不阻塞
    return produceState<ImageBitmap?>(initialValue = AssetBitmapCache.cached(path), key1 = path, key2 = extraKey) {
        if(path.isBlank()) { value = null; return@produceState }
        AssetBitmapCache.cached(path)?.let { value = it; return@produceState }
        value = withContext(Dispatchers.IO) { AssetBitmapCache.decode(ctx, path) }
    }.value
}

fun parseLpa(text:String):Pair<String,String>{
    val raw=text.trim()
    if(raw.isBlank()) return "" to ""
    val lpa=raw.removePrefix("LPA:1$").removePrefix("lpa:1$")
    val parts=lpa.split("$")
    if(parts.size>=2 && parts[0].contains('.')) return parts[0].trim() to parts[1].trim()
    val smdp=Regex("""(?i)(SM-DP\+?|SMDP|服务器|地址)[:：\s]+([^\s,;，；]+)""").find(raw)?.groupValues?.getOrNull(2)?.trim().orEmpty()
    val code=Regex("""(?i)(激活码|Activation\s*Code|code|AC)[:：\s]+([^\s,;，；]+)""").find(raw)?.groupValues?.getOrNull(2)?.trim().orEmpty()
    if(smdp.isNotBlank() || code.isNotBlank()) return smdp to code
    return "" to raw
}
fun formatChineseDate(s:String):String = runCatching{ val d=LocalDate.parse(s); "${d.year}年${d.monthValue}月${d.dayOfMonth}日" }.getOrElse{s}
fun formatDateByLang(s:String,lang:String):String = runCatching{ val d=LocalDate.parse(s); when(lang){"English"->"${d.year}-${d.monthValue.toString().padStart(2,'0')}-${d.dayOfMonth.toString().padStart(2,'0')}";"日本語"->"${d.year}年${d.monthValue}月${d.dayOfMonth}日";"阿拉伯语"->"${d.dayOfMonth}/${d.monthValue}/${d.year}";else->"${d.year}年${d.monthValue}月${d.dayOfMonth}日"} }.getOrElse{s}
fun estimateBalance(r:PhoneNumberRecord):String = when{
    r.countryCode=="+81" -> "250.00 CNY"
    r.countryCode=="+1" -> "4.50 USD"
    r.countryCode=="+49" -> "0.01 USD"
    r.countryCode=="+66" -> "2.40 CNY"
    else -> "--"
}
fun signalIcon(s:String)=when{ s.contains("离线")||s.contains("无") -> "○"; s.contains("弱") -> "▂"; s.contains("强") -> "▂▄▆"; else -> "▂▄" }

@Composable fun ExpressiveBottomNav(screen:String,on:(String)->Unit){
    val scheme=MaterialTheme.colorScheme
    Surface(
        modifier=Modifier.fillMaxWidth(),
        color=scheme.surface.copy(alpha=.98f),
        tonalElevation=4.dp,
        shadowElevation=10.dp,
        shape=RoundedCornerShape(topStart=28.dp,topEnd=28.dp)
    ){
        Row(
            modifier=Modifier
                .fillMaxWidth()
                .height(86.dp)
                .padding(horizontal=14.dp,vertical=8.dp),
            horizontalArrangement=Arrangement.SpaceBetween,
            verticalAlignment=Alignment.CenterVertically
        ){
            listOf("home" to "SIM","esim" to "eSIM","map" to "地图","settings" to "设置").forEach{ item->
                ExpressiveNavItem(
                    type=item.first,
                    label=item.second,
                    selected=screen==item.first,
                    modifier=Modifier.weight(1f)
                ){ on(item.first) }
            }
        }
    }
}

@Composable fun SimHubBottomNav(screen:String,on:(String)->Unit){
    ExpressiveBottomNav(screen,on)
    return
    val scheme=MaterialTheme.colorScheme
    NavigationBar(
        containerColor=scheme.surfaceContainer,
        tonalElevation=6.dp,
        modifier=Modifier.fillMaxWidth()
    ){
        listOf("home" to L("号码"),"esim" to "eSIM","settings" to L("设置")).forEach{ item->
            val sel=screen==item.first
            val scale by animateFloatAsState(targetValue=if(sel)1.05f else 1f,animationSpec=tween(120),label="navScale")
            NavigationBarItem(
                selected=sel,
                onClick={on(item.first)},
                icon={Box(Modifier.graphicsLayer(scaleX=scale,scaleY=scale)){BottomLineIcon(item.first,if(sel) scheme.onSecondaryContainer else scheme.onSurfaceVariant)}},
                label={Text(item.second,fontSize=11.sp,fontWeight=if(sel)FontWeight.SemiBold else FontWeight.Normal)},
                colors=NavigationBarItemDefaults.colors(
                    selectedIconColor=scheme.onSecondaryContainer,
                    selectedTextColor=scheme.onSurface,
                    indicatorColor=scheme.secondaryContainer,
                    unselectedIconColor=scheme.onSurfaceVariant,
                    unselectedTextColor=scheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable fun ExpressiveNavItem(type:String,label:String,selected:Boolean,modifier:Modifier=Modifier,onClick:()->Unit){
    val scheme=MaterialTheme.colorScheme
    val scale by animateFloatAsState(
        targetValue=if(selected) 1.04f else 1f,
        animationSpec=spring(),
        label="bottomNavItemScale"
    )
    val iconColor=if(selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant
    val textColor=if(selected) scheme.onSurface else scheme.onSurfaceVariant
    Column(
        modifier=modifier
            .height(70.dp)
            .clip(RoundedCornerShape(24.dp))
            .motionClickable(pressedScale=.94f){ onClick() }
            .padding(top=4.dp),
        horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.Center
    ){
        Surface(
            shape=RoundedCornerShape(20.dp),
            color=if(selected) scheme.primaryContainer else Color.Transparent,
            contentColor=iconColor,
            tonalElevation=if(selected) 3.dp else 0.dp,
            shadowElevation=if(selected) 1.dp else 0.dp,
            modifier=Modifier.graphicsLayer(scaleX=scale,scaleY=scale)
        ){
            Box(
                Modifier
                    .width(if(selected) 62.dp else 46.dp)
                    .height(34.dp),
                contentAlignment=Alignment.Center
            ){
                when(type){
                    "home" -> Icon(Icons.Rounded.SimCard,contentDescription=null,modifier=Modifier.size(21.dp),tint=iconColor)
                    "esim" -> Icon(Icons.Rounded.CreditCard,contentDescription=null,modifier=Modifier.size(21.dp),tint=iconColor)
                    "map" -> Icon(Icons.Rounded.Public,contentDescription=null,modifier=Modifier.size(22.dp),tint=iconColor)
                    else -> Icon(Icons.Rounded.Settings,contentDescription=null,modifier=Modifier.size(21.dp),tint=iconColor)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize=11.sp,
            lineHeight=12.sp,
            fontWeight=if(selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color=textColor,
            maxLines=1,
            overflow=TextOverflow.Ellipsis
        )
    }
}

@Composable fun BottomLineIcon(type:String,color:Color){
    Canvas(Modifier.size(22.dp)){
        val w=size.width; val h=size.height
        val stroke=Stroke(width=2.2f)
        when(type){
            "home"->{
                drawRoundRect(color,topLeft=Offset(w*.22f,h*.16f),size=Size(w*.56f,h*.68f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.09f,w*.09f),style=stroke)
                drawLine(color,Offset(w*.34f,h*.30f),Offset(w*.66f,h*.30f),strokeWidth=2.2f)
                drawCircle(color,radius=w*.045f,center=Offset(w*.50f,h*.70f))
            }
            "tools"->{
                drawRoundRect(color,topLeft=Offset(w*.18f,h*.34f),size=Size(w*.64f,h*.42f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.08f,w*.08f),style=stroke)
                drawLine(color,Offset(w*.38f,h*.34f),Offset(w*.38f,h*.24f),strokeWidth=2.2f)
                drawLine(color,Offset(w*.62f,h*.34f),Offset(w*.62f,h*.24f),strokeWidth=2.2f)
                drawLine(color,Offset(w*.38f,h*.24f),Offset(w*.62f,h*.24f),strokeWidth=2.2f)
            }
            "esim"->{
                drawRoundRect(color,topLeft=Offset(w*.18f,h*.20f),size=Size(w*.64f,h*.60f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.08f,w*.08f),style=stroke)
                drawLine(color,Offset(w*.32f,h*.48f),Offset(w*.68f,h*.48f),strokeWidth=2.2f)
                drawLine(color,Offset(w*.32f,h*.56f),Offset(w*.58f,h*.56f),strokeWidth=1.8f)
            }
            "map"->{
                drawRoundRect(color,topLeft=Offset(w*.12f,h*.18f),size=Size(w*.76f,h*.62f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.08f,w*.08f),style=stroke)
                drawLine(color,Offset(w*.36f,h*.22f),Offset(w*.36f,h*.76f),strokeWidth=1.8f)
                drawLine(color,Offset(w*.62f,h*.22f),Offset(w*.62f,h*.76f),strokeWidth=1.8f)
                drawCircle(color,radius=w*.07f,center=Offset(w*.52f,h*.45f))
            }
            else->{
                drawCircle(color,radius=w*.26f,center=Offset(w*.5f,h*.5f),style=stroke)
                drawCircle(color,radius=w*.075f,center=Offset(w*.5f,h*.5f))
                for(a in listOf(0f,90f,180f,270f)){
                    val rad=Math.toRadians(a.toDouble()).toFloat()
                    val x1=w*.5f+kotlin.math.cos(rad)*w*.34f; val y1=h*.5f+kotlin.math.sin(rad)*h*.34f
                    val x2=w*.5f+kotlin.math.cos(rad)*w*.43f; val y2=h*.5f+kotlin.math.sin(rad)*h*.43f
                    drawLine(color,Offset(x1,y1),Offset(x2,y2),strokeWidth=2.0f)
                }
            }
        }
    }
}

object OperatorLogoAssets {
    val map: Map<String,String> = mapOf(
        "CN|中国移动" to "file:///android_asset/operator_logos/cn_10086.png",
        "CN|中国联通" to "file:///android_asset/operator_logos/cn_10010.png",
        "CN|中国电信" to "file:///android_asset/operator_logos/cn_189.png",
        "CN|中国广电" to "file:///android_asset/operator_logos/cn_10099.png",
        "HK|csl" to "file:///android_asset/operator_logos/hk_csl.png",
        "HK|3hk" to "file:///android_asset/operator_logos/hk_3hk.png",
        "HK|Clubsim" to "file:///android_asset/operator_logos/hk_club_sim.png",
        "HK|smartone" to "file:///android_asset/operator_logos/hk_smartone.png",
        "HK|china mobile hong kong" to "file:///android_asset/operator_logos/hk_china_mobile_hong_kong.png",
        "MO|ctm" to "file:///android_asset/operator_logos/mo_ctm.png",
        "MO|3 macau" to "file:///android_asset/operator_logos/mo_3_macau.png",
        "TW|中华电信" to "file:///android_asset/operator_logos/tw_cht.png",
        "TW|台湾大哥大" to "file:///android_asset/operator_logos/tw_taiwanmobile.png",
        "TW|远传电信" to "file:///android_asset/operator_logos/tw_fetnet.png",
        "TW|台湾之星" to "file:///android_asset/operator_logos/tw_tstartel.png",
        "US|t-mobile" to "file:///android_asset/operator_logos/us_t_mobile.png",
        "US|at&t" to "file:///android_asset/operator_logos/us_at_t.png",
        "US|verizon" to "file:///android_asset/operator_logos/us_verizon.png",
        "US|us mobile" to "file:///android_asset/operator_logos/us_us_mobile.png",
        "US|mint mobile" to "file:///android_asset/operator_logos/us_mint_mobile.png",
        "US|visible" to "file:///android_asset/operator_logos/us_visible.png",
        "US|google fi" to "file:///android_asset/operator_logos/us_google_fi.png",
        "US|boost mobile" to "file:///android_asset/operator_logos/us_boost_mobile.png",
        "US|cricket wireless" to "file:///android_asset/operator_logos/us_cricket_wireless.png",
        "US|redpcket" to "file:///android_asset/operator_logos/us_redpocket_mobile.png",
        "CA|rogers" to "file:///android_asset/operator_logos/ca_rogers.png",
        "CA|bell" to "file:///android_asset/operator_logos/ca_bell.png",
        "CA|telus" to "file:///android_asset/operator_logos/ca_telus.png",
        "CA|freedom mobile" to "file:///android_asset/operator_logos/ca_freedom_mobile.png",
        "CA|fido" to "file:///android_asset/operator_logos/ca_fido.png",
        "GB|ee" to "file:///android_asset/operator_logos/gb_ee.png",
        "GB|o2 uk" to "file:///android_asset/operator_logos/gb_o2_uk.png",
        "GB|vodafone uk" to "file:///android_asset/operator_logos/gb_vodafone_uk.png",
        "GB|three uk" to "file:///android_asset/operator_logos/gb_three_uk.png",
        "GB|giffgaff" to "file:///android_asset/operator_logos/gb_giffgaff.png",
        "GB|CTEXCEL" to "file:///android_asset/operator_logos/gb_ctexcel.png",
        "DE|deutsche telekom" to "file:///android_asset/operator_logos/de_deutsche_telekom.png",
        "DE|vodafone germany" to "file:///android_asset/operator_logos/de_vodafone_germany.png",
        "DE|o2 germany" to "file:///android_asset/operator_logos/de_o2_germany.png",
        "DE|1&1" to "file:///android_asset/operator_logos/de_1_1.png",
        "FR|orange" to "file:///android_asset/operator_logos/fr_orange.png",
        "FR|sfr" to "file:///android_asset/operator_logos/fr_sfr.png",
        "FR|bouygues telecom" to "file:///android_asset/operator_logos/fr_bouygues_telecom.png",
        "FR|free mobile" to "file:///android_asset/operator_logos/fr_free_mobile.png",
        "IT|tim" to "file:///android_asset/operator_logos/it_tim.png",
        "IT|vodafone italy" to "file:///android_asset/operator_logos/it_vodafone_italy.png",
        "IT|windtre" to "file:///android_asset/operator_logos/it_windtre.png",
        "IT|iliad italy" to "file:///android_asset/operator_logos/it_iliad_italy.png",
        "ES|movistar" to "file:///android_asset/operator_logos/es_movistar.png",
        "ES|orange spain" to "file:///android_asset/operator_logos/es_orange_spain.png",
        "ES|vodafone spain" to "file:///android_asset/operator_logos/es_vodafone_spain.png",
        "ES|yoigo" to "file:///android_asset/operator_logos/es_yoigo.png",
        "NL|kpn" to "file:///android_asset/operator_logos/nl_kpn.png",
        "NL|vodafone netherlands" to "file:///android_asset/operator_logos/nl_vodafone_netherlands.png",
        "NL|odido" to "file:///android_asset/operator_logos/nl_odido.png",
        "BE|proximus" to "file:///android_asset/operator_logos/be_proximus.png",
        "BE|orange belgium" to "file:///android_asset/operator_logos/be_orange_belgium.png",
        "BE|base" to "file:///android_asset/operator_logos/be_base.png",
        "CH|swisscom" to "file:///android_asset/operator_logos/ch_swisscom.png",
        "CH|sunrise" to "file:///android_asset/operator_logos/ch_sunrise.png",
        "CH|salt" to "file:///android_asset/operator_logos/ch_salt.png",
        "AT|a1" to "file:///android_asset/operator_logos/at_a1.png",
        "AT|magenta telekom" to "file:///android_asset/operator_logos/at_magenta_telekom.png",
        "AT|drei austria" to "file:///android_asset/operator_logos/at_drei_austria.png",
        "SE|telia sweden" to "file:///android_asset/operator_logos/se_telia_sweden.png",
        "SE|tele2 sweden" to "file:///android_asset/operator_logos/se_tele2_sweden.png",
        "SE|telenor sweden" to "file:///android_asset/operator_logos/se_telenor_sweden.png",
        "NO|telenor norway" to "file:///android_asset/operator_logos/no_telenor_norway.png",
        "NO|telia norway" to "file:///android_asset/operator_logos/no_telia_norway.png",
        "NO|ice" to "file:///android_asset/operator_logos/no_ice.png",
        "DK|tdc net" to "file:///android_asset/operator_logos/dk_tdc_net.png",
        "DK|telenor denmark" to "file:///android_asset/operator_logos/dk_telenor_denmark.png",
        "DK|3 denmark" to "file:///android_asset/operator_logos/dk_3_denmark.png",
        "FI|elisa" to "file:///android_asset/operator_logos/fi_elisa.png",
        "FI|dna" to "file:///android_asset/operator_logos/fi_dna.png",
        "FI|telia finland" to "file:///android_asset/operator_logos/fi_telia_finland.png",
        "IS|síminn" to "file:///android_asset/operator_logos/is_s_minn.png",
        "IS|vodafone iceland" to "file:///android_asset/operator_logos/is_vodafone_iceland.png",
        "IS|nova" to "file:///android_asset/operator_logos/is_nova.png",
        "IE|vodafone ireland" to "file:///android_asset/operator_logos/ie_vodafone_ireland.png",
        "IE|three ireland" to "file:///android_asset/operator_logos/ie_three_ireland.png",
        "IE|eir" to "file:///android_asset/operator_logos/ie_eir.png",
        "PT|meo" to "file:///android_asset/operator_logos/pt_meo.png",
        "PT|nos" to "file:///android_asset/operator_logos/pt_nos.png",
        "PT|vodafone portugal" to "file:///android_asset/operator_logos/pt_vodafone_portugal.png",
        "GR|cosmote" to "file:///android_asset/operator_logos/gr_cosmote.png",
        "GR|vodafone greece" to "file:///android_asset/operator_logos/gr_vodafone_greece.png",
        "GR|nova greece" to "file:///android_asset/operator_logos/gr_nova_greece.png",
        "PL|orange poland" to "file:///android_asset/operator_logos/pl_orange_poland.png",
        "PL|play" to "file:///android_asset/operator_logos/pl_play.png",
        "PL|plus" to "file:///android_asset/operator_logos/pl_plus.png",
        "PL|t-mobile poland" to "file:///android_asset/operator_logos/pl_t_mobile_poland.png",
        "CZ|o2 czech republic" to "file:///android_asset/operator_logos/cz_o2_czech_republic.png",
        "CZ|t-mobile czech republic" to "file:///android_asset/operator_logos/cz_t_mobile_czech_republic.png",
        "CZ|vodafone czech republic" to "file:///android_asset/operator_logos/cz_vodafone_czech_republic.png",
        "SK|orange slovakia" to "file:///android_asset/operator_logos/sk_orange_slovakia.png",
        "SK|slovak telekom" to "file:///android_asset/operator_logos/sk_slovak_telekom.png",
        "SK|o2 slovakia" to "file:///android_asset/operator_logos/sk_o2_slovakia.png",
        "HU|magyar telekom" to "file:///android_asset/operator_logos/hu_magyar_telekom.png",
        "HU|vodafone hungary" to "file:///android_asset/operator_logos/hu_vodafone_hungary.png",
        "HU|yettel hungary" to "file:///android_asset/operator_logos/hu_yettel_hungary.png",
        "RO|orange romania" to "file:///android_asset/operator_logos/ro_orange_romania.png",
        "RO|vodafone romania" to "file:///android_asset/operator_logos/ro_vodafone_romania.png",
        "RO|digi romania" to "file:///android_asset/operator_logos/ro_digi_romania.png",
        "BG|a1 bulgaria" to "file:///android_asset/operator_logos/bg_a1_bulgaria.png",
        "BG|yettel bulgaria" to "file:///android_asset/operator_logos/bg_yettel_bulgaria.png",
        "BG|vivacom" to "file:///android_asset/operator_logos/bg_vivacom.png",
        "BG|保加利亚 Telecom" to "file:///android_asset/operator_logos/bg_vivacom.png",
        "HR|t-mobile croatia" to "file:///android_asset/operator_logos/hr_t_mobile_croatia.png",
        "HR|a1 croatia" to "file:///android_asset/operator_logos/hr_a1_croatia.png",
        "HR|telemach croatia" to "file:///android_asset/operator_logos/hr_telemach_croatia.png",
        "SI|a1 slovenia" to "file:///android_asset/operator_logos/si_a1_slovenia.png",
        "SI|telekom slovenije" to "file:///android_asset/operator_logos/si_telekom_slovenije.png",
        "SI|telemach slovenia" to "file:///android_asset/operator_logos/si_telemach_slovenia.png",
        "RS|mts serbia" to "file:///android_asset/operator_logos/rs_mts_serbia.png",
        "RS|telenor serbia" to "file:///android_asset/operator_logos/rs_telenor_serbia.png",
        "RS|a1 serbia" to "file:///android_asset/operator_logos/rs_a1_serbia.png",
        "BA|bh telecom" to "file:///android_asset/operator_logos/ba_bh_telecom.png",
        "BA|m:tel" to "file:///android_asset/operator_logos/ba_m_tel.png",
        "BA|eronet" to "file:///android_asset/operator_logos/ba_eronet.png",
        "ME|t-mobile montenegro" to "file:///android_asset/operator_logos/me_t_mobile_montenegro.png",
        "ME|m:tel montenegro" to "file:///android_asset/operator_logos/me_m_tel_montenegro.png",
        "MK|t-mobile macedonia" to "file:///android_asset/operator_logos/mk_t_mobile_macedonia.png",
        "MK|a1 macedonia" to "file:///android_asset/operator_logos/mk_a1_macedonia.png",
        "AL|vodafone albania" to "file:///android_asset/operator_logos/al_vodafone_albania.png",
        "AL|one albania" to "file:///android_asset/operator_logos/al_one_albania.png",
        "LT|telia lithuania" to "file:///android_asset/operator_logos/lt_telia_lithuania.png",
        "LT|bitė lithuania" to "file:///android_asset/operator_logos/lt_bit__lithuania.png",
        "LT|tele2 lithuania" to "file:///android_asset/operator_logos/lt_tele2_lithuania.png",
        "LV|lmt" to "file:///android_asset/operator_logos/lv_lmt.png",
        "LV|tele2 latvia" to "file:///android_asset/operator_logos/lv_tele2_latvia.png",
        "LV|bite latvia" to "file:///android_asset/operator_logos/lv_bite_latvia.png",
        "EE|telia estonia" to "file:///android_asset/operator_logos/ee_telia_estonia.png",
        "EE|elisa estonia" to "file:///android_asset/operator_logos/ee_elisa_estonia.png",
        "EE|tele2 estonia" to "file:///android_asset/operator_logos/ee_tele2_estonia.png",
        "MD|orange moldova" to "file:///android_asset/operator_logos/md_orange_moldova.png",
        "MD|moldcell" to "file:///android_asset/operator_logos/md_moldcell.png",
        "BY|mts belarus" to "file:///android_asset/operator_logos/by_mts_belarus.png",
        "BY|a1 belarus" to "file:///android_asset/operator_logos/by_a1_belarus.png",
        "BY|life:) Belarus" to "file:///android_asset/operator_logos/by_life___belarus.png",
        "UA|kyivstar" to "file:///android_asset/operator_logos/ua_kyivstar.png",
        "UA|vodafone ukraine" to "file:///android_asset/operator_logos/ua_vodafone_ukraine.png",
        "UA|lifecell" to "file:///android_asset/operator_logos/ua_lifecell.png",
        "RU|mts russia" to "file:///android_asset/operator_logos/ru_mts_russia.png",
        "RU|beeline russia" to "file:///android_asset/operator_logos/ru_beeline_russia.png",
        "RU|megafon" to "file:///android_asset/operator_logos/ru_megafon.png",
        "KZ|kcell" to "file:///android_asset/operator_logos/kz_kcell.png",
        "KZ|beeline kazakhstan" to "file:///android_asset/operator_logos/kz_beeline_kazakhstan.png",
        "KZ|tele2 kazakhstan" to "file:///android_asset/operator_logos/kz_tele2_kazakhstan.png",
        "GE|magti" to "file:///android_asset/operator_logos/ge_magti.png",
        "GE|geocell" to "file:///android_asset/operator_logos/ge_geocell.png",
        "GE|beeline georgia" to "file:///android_asset/operator_logos/ge_beeline_georgia.png",
        "AM|viva-mts" to "file:///android_asset/operator_logos/am_viva_mts.png",
        "AM|beeline armenia" to "file:///android_asset/operator_logos/am_beeline_armenia.png",
        "AM|team telecom" to "file:///android_asset/operator_logos/am_team_telecom.png",
        "AZ|azercell" to "file:///android_asset/operator_logos/az_azercell.png",
        "AZ|bakcell" to "file:///android_asset/operator_logos/az_bakcell.png",
        "AZ|nar mobile" to "file:///android_asset/operator_logos/az_nar_mobile.png",
        "TR|turkcell" to "file:///android_asset/operator_logos/tr_turkcell.png",
        "TR|vodafone turkey" to "file:///android_asset/operator_logos/tr_vodafone_turkey.png",
        "TR|turk telekom" to "file:///android_asset/operator_logos/tr_turk_telekom.png",
        "IL|partner" to "file:///android_asset/operator_logos/il_partner.png",
        "IL|cellcom" to "file:///android_asset/operator_logos/il_cellcom.png",
        "IL|pelephone" to "file:///android_asset/operator_logos/il_pelephone.png",
        "AE|etisalat uae" to "file:///android_asset/operator_logos/ae_etisalat_uae.png",
        "AE|du" to "file:///android_asset/operator_logos/ae_du.png",
        "AE|virgin mobile uae" to "file:///android_asset/operator_logos/ae_virgin_mobile_uae.png",
        "SA|stc" to "file:///android_asset/operator_logos/sa_stc.png",
        "SA|mobily" to "file:///android_asset/operator_logos/sa_mobily.png",
        "SA|zain ksa" to "file:///android_asset/operator_logos/sa_zain_ksa.png",
        "QA|ooredoo qatar" to "file:///android_asset/operator_logos/qa_ooredoo_qatar.png",
        "QA|vodafone qatar" to "file:///android_asset/operator_logos/qa_vodafone_qatar.png",
        "KW|zain kuwait" to "file:///android_asset/operator_logos/kw_zain_kuwait.png",
        "KW|ooredoo kuwait" to "file:///android_asset/operator_logos/kw_ooredoo_kuwait.png",
        "KW|stc kuwait" to "file:///android_asset/operator_logos/kw_stc_kuwait.png",
        "BH|batelco" to "file:///android_asset/operator_logos/bh_batelco.png",
        "BH|zain bahrain" to "file:///android_asset/operator_logos/bh_zain_bahrain.png",
        "BH|stc bahrain" to "file:///android_asset/operator_logos/bh_stc_bahrain.png",
        "OM|omantel" to "file:///android_asset/operator_logos/om_omantel.png",
        "OM|ooredoo oman" to "file:///android_asset/operator_logos/om_ooredoo_oman.png",
        "JO|zain jordan" to "file:///android_asset/operator_logos/jo_zain_jordan.png",
        "JO|orange jordan" to "file:///android_asset/operator_logos/jo_orange_jordan.png",
        "JO|umniah" to "file:///android_asset/operator_logos/jo_umniah.png",
        "LB|alfa" to "file:///android_asset/operator_logos/lb_alfa.png",
        "LB|touch lebanon" to "file:///android_asset/operator_logos/lb_touch_lebanon.png",
        "EG|vodafone egypt" to "file:///android_asset/operator_logos/eg_vodafone_egypt.png",
        "EG|orange egypt" to "file:///android_asset/operator_logos/eg_orange_egypt.png",
        "EG|etisalat egypt" to "file:///android_asset/operator_logos/eg_etisalat_egypt.png",
        "MA|maroc telecom" to "file:///android_asset/operator_logos/ma_maroc_telecom.png",
        "MA|orange morocco" to "file:///android_asset/operator_logos/ma_orange_morocco.png",
        "MA|inwi morocco" to "file:///android_asset/operator_logos/ma_inwi_morocco.png",
        "TN|tunisie telecom" to "file:///android_asset/operator_logos/tn_tunisie_telecom.png",
        "TN|ooredoo tunisia" to "file:///android_asset/operator_logos/tn_ooredoo_tunisia.png",
        "TN|orange tunisia" to "file:///android_asset/operator_logos/tn_orange_tunisia.png",
        "DZ|djezzy" to "file:///android_asset/operator_logos/dz_djezzy.png",
        "DZ|ooredoo algeria" to "file:///android_asset/operator_logos/dz_ooredoo_algeria.png",
        "DZ|mobilis" to "file:///android_asset/operator_logos/dz_mobilis.png",
        "NG|mtn nigeria" to "file:///android_asset/operator_logos/ng_mtn_nigeria.png",
        "NG|airtel nigeria" to "file:///android_asset/operator_logos/ng_airtel_nigeria.png",
        "NG|glo nigeria" to "file:///android_asset/operator_logos/ng_glo_nigeria.png",
        "KE|safaricom" to "file:///android_asset/operator_logos/ke_safaricom.png",
        "KE|airtel kenya" to "file:///android_asset/operator_logos/ke_airtel_kenya.png",
        "ZA|vodacom" to "file:///android_asset/operator_logos/za_vodacom.png",
        "ZA|mtn south africa" to "file:///android_asset/operator_logos/za_mtn_south_africa.png",
        "ZA|cell c" to "file:///android_asset/operator_logos/za_cell_c.png",
        "JP|ntt docomo" to "file:///android_asset/operator_logos/jp_ntt_docomo.png",
        "JP|softbank" to "file:///android_asset/operator_logos/jp_softbank.png",
        "JP|au kddi" to "file:///android_asset/operator_logos/jp_au_kddi.png",
        "JP|rakuten mobile" to "file:///android_asset/operator_logos/jp_rakuten_mobile.png",
        "KR|sk telecom" to "file:///android_asset/operator_logos/kr_sk_telecom.png",
        "KR|kt" to "file:///android_asset/operator_logos/kr_kt.png",
        "KR|lg u+" to "file:///android_asset/operator_logos/kr_lg_u.png",
        "SG|singtel" to "file:///android_asset/operator_logos/sg_singtel.png",
        "SG|starhub" to "file:///android_asset/operator_logos/sg_starhub.png",
        "SG|m1" to "file:///android_asset/operator_logos/sg_m1.png",
        "SG|simba" to "file:///android_asset/operator_logos/sg_simba.png",
        "MY|maxis" to "file:///android_asset/operator_logos/my_maxis.png",
        "MY|celcomdigi" to "file:///android_asset/operator_logos/my_celcomdigi.png",
        "MY|u mobile" to "file:///android_asset/operator_logos/my_u_mobile.png",
        "MY|yes" to "file:///android_asset/operator_logos/my_yes.png",
        "MY|unifi mobile" to "file:///android_asset/operator_logos/my_unifi_mobile.png",
        "TH|ais" to "file:///android_asset/operator_logos/th_ais.png",
        "TH|true" to "file:///android_asset/operator_logos/th_true.png",
        "TH|dtac" to "file:///android_asset/operator_logos/th_dtac.png",
        "VN|viettel" to "file:///android_asset/operator_logos/vn_viettel.png",
        "VN|mobifone" to "file:///android_asset/operator_logos/vn_mobifone.png",
        "VN|vinaphone" to "file:///android_asset/operator_logos/vn_vinaphone.png",
        "PH|globe" to "file:///android_asset/operator_logos/ph_globe.png",
        "PH|smart" to "file:///android_asset/operator_logos/ph_smart.png",
        "PH|dito" to "file:///android_asset/operator_logos/ph_dito.png",
        "ID|telkomsel" to "file:///android_asset/operator_logos/id_telkomsel.png",
        "ID|indosat" to "file:///android_asset/operator_logos/id_indosat.png",
        "ID|xl axiata" to "file:///android_asset/operator_logos/id_xl_axiata.png",
        "ID|smartfren" to "file:///android_asset/operator_logos/id_smartfren.png",
        "KH|smart axiata" to "file:///android_asset/operator_logos/kh_smart_axiata.png",
        "KH|cellcard" to "file:///android_asset/operator_logos/kh_cellcard.png",
        "LA|lao telecom" to "file:///android_asset/operator_logos/la_lao_telecom.png",
        "LA|unitel laos" to "file:///android_asset/operator_logos/la_unitel_laos.png",
        "MM|mpt" to "file:///android_asset/operator_logos/mm_mpt.png",
        "MM|ooredoo myanmar" to "file:///android_asset/operator_logos/mm_ooredoo_myanmar.png",
        "BN|dst" to "file:///android_asset/operator_logos/bn_dst.png",
        "BN|progresif" to "file:///android_asset/operator_logos/bn_progresif.png",
        "IN|jio" to "file:///android_asset/operator_logos/in_jio.png",
        "IN|airtel india" to "file:///android_asset/operator_logos/in_airtel_india.png",
        "IN|vi india" to "file:///android_asset/operator_logos/in_vi_india.png",
        "PK|jazz" to "file:///android_asset/operator_logos/pk_jazz.png",
        "PK|zong" to "file:///android_asset/operator_logos/pk_zong.png",
        "PK|telenor pakistan" to "file:///android_asset/operator_logos/pk_telenor_pakistan.png",
        "BD|grameenphone" to "file:///android_asset/operator_logos/bd_grameenphone.png",
        "BD|banglalink" to "file:///android_asset/operator_logos/bd_banglalink.png",
        "BD|robi bangladesh" to "file:///android_asset/operator_logos/bd_robi_bangladesh.png",
        "LK|dialog" to "file:///android_asset/operator_logos/lk_dialog.png",
        "LK|mobitel sri lanka" to "file:///android_asset/operator_logos/lk_mobitel_sri_lanka.png",
        "NP|ncell" to "file:///android_asset/operator_logos/np_ncell.png",
        "NP|ntc nepal" to "file:///android_asset/operator_logos/np_ntc_nepal.png",
        "MV|dhiraagu" to "file:///android_asset/operator_logos/mv_dhiraagu.png",
        "MV|ooredoo maldives" to "file:///android_asset/operator_logos/mv_ooredoo_maldives.png",
        "AU|telstra" to "file:///android_asset/operator_logos/au_telstra.png",
        "AU|optus" to "file:///android_asset/operator_logos/au_optus.png",
        "AU|vodafone au" to "file:///android_asset/operator_logos/au_vodafone_au.png",
        "NZ|spark" to "file:///android_asset/operator_logos/nz_spark.png",
        "NZ|one nz" to "file:///android_asset/operator_logos/nz_one_nz.png",
        "NZ|2degrees" to "file:///android_asset/operator_logos/nz_2degrees.png",
        "FJ|vodafone fiji" to "file:///android_asset/operator_logos/fj_vodafone_fiji.png",
        "FJ|digicel fiji" to "file:///android_asset/operator_logos/fj_digicel_fiji.png",
        "BR|vivo brazil" to "file:///android_asset/operator_logos/br_vivo_brazil.png",
        "BR|claro brazil" to "file:///android_asset/operator_logos/br_claro_brazil.png",
        "BR|tim brazil" to "file:///android_asset/operator_logos/br_tim_brazil.png",
        "AR|personal" to "file:///android_asset/operator_logos/ar_personal.png",
        "AR|claro argentina" to "file:///android_asset/operator_logos/ar_claro_argentina.png",
        "AR|movistar argentina" to "file:///android_asset/operator_logos/ar_movistar_argentina.png",
        "CL|entel chile" to "file:///android_asset/operator_logos/cl_entel_chile.png",
        "CL|movistar chile" to "file:///android_asset/operator_logos/cl_movistar_chile.png",
        "CL|wom chile" to "file:///android_asset/operator_logos/cl_wom_chile.png",
        "CO|claro colombia" to "file:///android_asset/operator_logos/co_claro_colombia.png",
        "CO|movistar colombia" to "file:///android_asset/operator_logos/co_movistar_colombia.png",
        "CO|tigo colombia" to "file:///android_asset/operator_logos/co_tigo_colombia.png",
        "PE|claro peru" to "file:///android_asset/operator_logos/pe_claro_peru.png",
        "PE|movistar peru" to "file:///android_asset/operator_logos/pe_movistar_peru.png",
        "PE|bitel peru" to "file:///android_asset/operator_logos/pe_bitel_peru.png",
        "MX|telcel" to "file:///android_asset/operator_logos/mx_telcel.png",
        "MX|at&t mexico" to "file:///android_asset/operator_logos/mx_at_t_mexico.png",
        "MX|movistar mexico" to "file:///android_asset/operator_logos/mx_movistar_mexico.png",
        "UY|antel" to "file:///android_asset/operator_logos/uy_antel.png",
        "UY|movistar uruguay" to "file:///android_asset/operator_logos/uy_movistar_uruguay.png",
        "PY|tigo paraguay" to "file:///android_asset/operator_logos/py_tigo_paraguay.png",
        "PY|claro paraguay" to "file:///android_asset/operator_logos/py_claro_paraguay.png",
        "PY|personal paraguay" to "file:///android_asset/operator_logos/py_personal_paraguay.png",
        "BO|entel bolivia" to "file:///android_asset/operator_logos/bo_entel_bolivia.png",
        "BO|tigo bolivia" to "file:///android_asset/operator_logos/bo_tigo_bolivia.png",
        "EC|claro ecuador" to "file:///android_asset/operator_logos/ec_claro_ecuador.png",
        "EC|movistar ecuador" to "file:///android_asset/operator_logos/ec_movistar_ecuador.png",
        "EC|cnt ecuador" to "file:///android_asset/operator_logos/ec_cnt_ecuador.png",
        "VE|movistar venezuela" to "file:///android_asset/operator_logos/ve_movistar_venezuela.png",
        "VE|digitel" to "file:///android_asset/operator_logos/ve_digitel.png",
        "CR|ice costa rica" to "file:///android_asset/operator_logos/cr_ice_costa_rica.png",
        "CR|claro costa rica" to "file:///android_asset/operator_logos/cr_claro_costa_rica.png",
        "PA|cable & wireless panama" to "file:///android_asset/operator_logos/pa_cable___wireless_panama.png",
        "PA|claro panama" to "file:///android_asset/operator_logos/pa_claro_panama.png",
        "PA|movistar panama" to "file:///android_asset/operator_logos/pa_movistar_panama.png",
        "GT|claro guatemala" to "file:///android_asset/operator_logos/gt_claro_guatemala.png",
        "GT|tigo guatemala" to "file:///android_asset/operator_logos/gt_tigo_guatemala.png",
        "DO|claro dominican republic" to "file:///android_asset/operator_logos/do_claro_dominican_republic.png",
        "DO|altice dominican republic" to "file:///android_asset/operator_logos/do_altice_dominican_republic.png",
        "DO|viva dominican republic" to "file:///android_asset/operator_logos/do_viva_dominican_republic.png",
        "JM|digicel jamaica" to "file:///android_asset/operator_logos/jm_digicel_jamaica.png",
        "JM|flow jamaica" to "file:///android_asset/operator_logos/jm_flow_jamaica.png",
        "AD|Andorra Telecom" to "file:///android_asset/operator_logos/ad_andorra_telecom.png",
        "AF|Afghan Wireless" to "file:///android_asset/operator_logos/af_afghan_wireless.png",
        "AO|Unitel" to "file:///android_asset/operator_logos/ao_unitel.png",
        "BB|Digicel" to "file:///android_asset/operator_logos/bb_digicel.png",
        "BS|BTC" to "file:///android_asset/operator_logos/bs_btc.png",
        "BS|redpcket" to "file:///android_asset/operator_logos/us_redpocket_mobile.png",
        "BT|TashiCell" to "file:///android_asset/operator_logos/bt_b_mobile.png",
        "BW|Mascom" to "file:///android_asset/operator_logos/bw_mascom.png",
        "BZ|Digi" to "file:///android_asset/operator_logos/bz_digi.png",
        "CD|Vodacom" to "file:///android_asset/operator_logos/cd_vodacom.png",
        "CG|MTN Congo" to "file:///android_asset/operator_logos/cg_mtn_congo.png",
        "CI|Orange" to "file:///android_asset/operator_logos/ci_orange.png",
        "CM|MTN Cameroon" to "file:///android_asset/operator_logos/cm_mtn_cameroon.png",
        "CU|Cubacel" to "file:///android_asset/operator_logos/cu_cubacel.png",
        "CY|Cyta" to "file:///android_asset/operator_logos/cy_cyta.png",
        "ET|Ethio Telecom" to "file:///android_asset/operator_logos/et_ethio_telecom.png",
        "FO|Føroya Tele" to "file:///android_asset/operator_logos/fo_foroya_tele.png",
        "GH|MTN" to "file:///android_asset/operator_logos/gh_mtn.png",
        "GI|Gibtelecom" to "file:///android_asset/operator_logos/gi_gibtelecom.png",
        "GU|GTA" to "file:///android_asset/operator_logos/gu_gta.png",
        "GY|Digicel" to "file:///android_asset/operator_logos/gy_digicel.png",
        "HN|Tigo" to "file:///android_asset/operator_logos/hn_tigo.png",
        "IQ|Zain" to "file:///android_asset/operator_logos/iq_zain.png",
        "IR|Irancell" to "file:///android_asset/operator_logos/ir_irancell.png",
        "KG|Beeline" to "file:///android_asset/operator_logos/kg_beeline.png",
        "LI|FL1" to "file:///android_asset/operator_logos/li_fl1.png",
        "LU|POST Luxembourg" to "file:///android_asset/operator_logos/lu_post_luxembourg.png",
        "LY|Libyana" to "file:///android_asset/operator_logos/ly_libyana.png",
        "MC|Monaco Telecom" to "file:///android_asset/operator_logos/mc_monaco_telecom.png",
        "MG|Telma" to "file:///android_asset/operator_logos/mg_telma.png",
        "MN|Mobicom" to "file:///android_asset/operator_logos/mn_mobicom.png",
        "MT|GO" to "file:///android_asset/operator_logos/mt_go.png",
        "MU|my.t" to "file:///android_asset/operator_logos/mu_myt.png",
        "MZ|Vodacom" to "file:///android_asset/operator_logos/mz_vodacom.png",
        "NA|MTC" to "file:///android_asset/operator_logos/na_mtc.png",
        "NC|OPT-NC" to "file:///android_asset/operator_logos/nc_opt_nc.png",
        "NI|Claro" to "file:///android_asset/operator_logos/ni_claro.png",
        "PF|Vini" to "file:///android_asset/operator_logos/pf_vini.png",
        "PG|Digicel" to "file:///android_asset/operator_logos/pg_digicel.png",
        "PS|Jawwal" to "file:///android_asset/operator_logos/ps_jawwal.png",
        "RE|SFR" to "file:///android_asset/operator_logos/re_sfr.png",
        "RW|MTN" to "file:///android_asset/operator_logos/rw_mtn.png",
        "SB|Our Telekom" to "file:///android_asset/operator_logos/sb_our_telekom.png",
        "SD|Zain" to "file:///android_asset/operator_logos/sd_zain.png",
        "SM|SMT" to "file:///android_asset/operator_logos/sm_smt.png",
        "SN|Orange" to "file:///android_asset/operator_logos/sn_orange.png",
        "SR|Telesur" to "file:///android_asset/operator_logos/sr_telesur.png",
        "SV|Tigo" to "file:///android_asset/operator_logos/sv_tigo.png",
        "SY|Syriatel" to "file:///android_asset/operator_logos/sy_syriatel.png",
        "TJ|Tcell" to "file:///android_asset/operator_logos/tj_tcell.png",
        "TL|Timor Telecom" to "file:///android_asset/operator_logos/tl_timor_telecom.png",
        "TM|TM Cell" to "file:///android_asset/operator_logos/tm_tm_cell.png",
        "TO|Digicel" to "file:///android_asset/operator_logos/to_digicel.png",
        "TT|TSTT" to "file:///android_asset/operator_logos/tt_tstt.png",
        "TZ|Vodacom" to "file:///android_asset/operator_logos/tz_vodacom.png",
        "UG|MTN" to "file:///android_asset/operator_logos/ug_mtn.png",
        "UZ|Ucell" to "file:///android_asset/operator_logos/uz_ucell.png",
        "VU|Vodafone" to "file:///android_asset/operator_logos/vu_vodafone.png",
        "WS|Digicel" to "file:///android_asset/operator_logos/ws_digicel.png",
        "XK|Vala" to "file:///android_asset/operator_logos/xk_vala.png",
        "YE|Yemen Mobile" to "file:///android_asset/operator_logos/ye_yemen_mobile.png",
        "ZM|MTN" to "file:///android_asset/operator_logos/zm_mtn.png",
        "ZW|Econet" to "file:///android_asset/operator_logos/zw_econet.png",
    )
    fun assetFor(name:String, iso:String?=null):String {
        val q=name.trim().lowercase()
        if(q.isBlank()) return ""
        fun norm(x:String)=x.lowercase().replace("&","and").replace("+","plus").replace(Regex("[^a-z0-9一-龥]+"),"")
        val nq=norm(q)
        if(iso!=null) {
            val prefix="${iso.uppercase()}|"
            map[prefix+q]?.let{return it}
            map.entries.firstOrNull{ it.key.startsWith(prefix) && norm(it.key.substringAfter("|"))==nq }?.let{return it.value}
        }
        map.entries.firstOrNull{
            val k=it.key.substringAfter("|")
            it.key.endsWith("|$q") || norm(k)==nq || nq.contains(norm(k)) || norm(k).contains(nq)
        }?.let{return it.value}
        return ""
    }
}

@Composable fun AppBackground(settings:App设置){
    val scheme=MaterialTheme.colorScheme
    if(settings.backgroundUri.isNotBlank()){
        AsyncImage(model=settings.backgroundUri,contentDescription=null,contentScale=ContentScale.Crop,modifier=Modifier.fillMaxSize().background(scheme.background))
        Box(Modifier.fillMaxSize().background((if(settings.dark) Color.Black else scheme.surface).copy(alpha=(1f-settings.backgroundAlpha).coerceIn(.18f,.82f))))
    }else Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(scheme.background,scheme.surfaceContainerLowest,scheme.surfaceContainerLow))))
}

@Composable fun ExpressiveEmptyState(){
    val scheme=MaterialTheme.colorScheme
    Column(horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)){
        Surface(shape=RoundedCornerShape(topStart=34.dp,topEnd=18.dp,bottomStart=18.dp,bottomEnd=34.dp),color=scheme.primaryContainer,contentColor=scheme.onPrimaryContainer,tonalElevation=4.dp){
            Box(Modifier.size(104.dp).background(Brush.linearGradient(listOf(scheme.primaryContainer,scheme.tertiaryContainer.copy(alpha=.72f)))),contentAlignment=Alignment.Center){
                Icon(Icons.Rounded.SimCard,contentDescription=null,modifier=Modifier.size(44.dp))
            }
        }
        Text("暂无号码",style=MaterialTheme.typography.titleLarge,color=scheme.onSurface)
        Text("SIM 状态会显示在这里",style=MaterialTheme.typography.bodyMedium,color=scheme.onSurfaceVariant)
    }
}

@Composable fun ExpressiveInlineSearch(
    search:String,
    onSearch:(String)->Unit,
    count:Int,
    filter:String,
    sortMode:String,
    onFilter:(String)->Unit,
    onSort:(String)->Unit
){
    val scheme=MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(search.isNotBlank()) }
    val active = expanded || search.isNotBlank()
    AnimatedContent(
        targetState=active,
        modifier=Modifier.fillMaxWidth().height(52.dp),
        transitionSpec={
            (fadeIn(tween(180)) + slideInHorizontally { if(targetState) -it / 5 else it / 5 }) togetherWith
                (fadeOut(tween(120)) + slideOutHorizontally { if(targetState) it / 5 else -it / 5 })
        },
        label="HomeSearchHeader"
    ){ expandedSearch ->
        Row(Modifier.fillMaxSize(),verticalAlignment=Alignment.CenterVertically){
        if(expandedSearch){
            TextField(
                value=search,
                onValueChange=onSearch,
                modifier=Modifier.fillMaxWidth().height(52.dp),
                singleLine=true,
                shape=RoundedCornerShape(26.dp),
                placeholder={Text("搜索运营商、国家或号码",maxLines=1,overflow=TextOverflow.Ellipsis)},
                leadingIcon={Icon(Icons.Rounded.Search,contentDescription=null)},
                trailingIcon={
                    IconButton(onClick={
                        if(search.isBlank()) expanded=false else onSearch("")
                    }){
                        Icon(Icons.Rounded.Close,contentDescription=null)
                    }
                },
                textStyle=MaterialTheme.typography.bodyLarge,
                colors=TextFieldDefaults.colors(
                    focusedContainerColor=scheme.surfaceContainerHigh,
                    unfocusedContainerColor=scheme.surfaceContainerHigh,
                    focusedIndicatorColor=Color.Transparent,
                    unfocusedIndicatorColor=Color.Transparent,
                    cursorColor=scheme.primary,
                    focusedTextColor=scheme.onSurface,
                    unfocusedTextColor=scheme.onSurface,
                    focusedLeadingIconColor=scheme.primary,
                    unfocusedLeadingIconColor=scheme.onSurfaceVariant,
                    focusedTrailingIconColor=scheme.onSurfaceVariant,
                    unfocusedTrailingIconColor=scheme.onSurfaceVariant,
                    focusedPlaceholderColor=scheme.onSurfaceVariant,
                    unfocusedPlaceholderColor=scheme.onSurfaceVariant
                )
            )
        }else{
            FilledTonalIconButton(
                onClick={ expanded=true },
                modifier=Modifier.size(48.dp),
                shape=RoundedCornerShape(24.dp),
                colors=IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor=scheme.surfaceContainerHigh,
                    contentColor=scheme.onSurfaceVariant
                )
            ){
                Icon(Icons.Rounded.Search,contentDescription=null,modifier=Modifier.size(24.dp))
            }
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.padding(start=8.dp,end=8.dp),
                verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.spacedBy(6.dp)
            ){
                HomeTopCountChip(count)
                HomeTopStatusFilterButton(filter,onFilter)
                HomeTopSortButton(sortMode,onSort)
            }
            Text(
                "DsimJ",
                style=MaterialTheme.typography.headlineSmall,
                color=scheme.primary,
                fontWeight=FontWeight.ExtraBold,
                maxLines=1,
                overflow=TextOverflow.Ellipsis
            )
        }
        }
    }
    return
    TextField(
        value=search,
        onValueChange=onSearch,
        modifier=Modifier.fillMaxWidth().height(52.dp),
        singleLine=true,
        shape=RoundedCornerShape(26.dp),
        placeholder={Text("搜索运营商、国家或号码",maxLines=1,overflow=TextOverflow.Ellipsis)},
        leadingIcon={Icon(Icons.Rounded.Search,contentDescription=null)},
        trailingIcon={ if(search.isNotBlank()) TextButton(onClick={onSearch("")}){Text("清除")} },
        textStyle=MaterialTheme.typography.bodyLarge,
        colors=TextFieldDefaults.colors(
            focusedContainerColor=scheme.surfaceContainerHigh,
            unfocusedContainerColor=scheme.surfaceContainerHigh,
            focusedIndicatorColor=Color.Transparent,
            unfocusedIndicatorColor=Color.Transparent,
            cursorColor=scheme.primary,
            focusedTextColor=scheme.onSurface,
            unfocusedTextColor=scheme.onSurface,
            focusedLeadingIconColor=scheme.primary,
            unfocusedLeadingIconColor=scheme.onSurfaceVariant,
            focusedPlaceholderColor=scheme.onSurfaceVariant,
            unfocusedPlaceholderColor=scheme.onSurfaceVariant
        )
    )
}

@Composable fun HomeTopSortButton(sortMode:String,onSort:(String)->Unit){
    val scheme=MaterialTheme.colorScheme
    var open by remember { mutableStateOf(false) }
    val options=listOf(
        "自定义" to "自定义",
        "添加日期" to "添加日期",
        "快要到期" to "快要到期",
        "首字母排序" to "首字母",
        "保号周期" to "保号周期"
    )
    val isDueSoon = sortMode=="快要到期" || sortMode=="到期近"
    val label=when(sortMode){
        "添加日期" -> "日期"
        "快要到期","到期近" -> "到期"
        "首字母" -> "字母"
        "保号周期" -> "周期"
        else -> "排序"
    }
    Box{
        HomeTopFilterChip(
            text=label,
            selected=sortMode!="自定义",
            accent=scheme.secondary
        ){ open=true }
        DropdownMenu(
            expanded=open,
            onDismissRequest={open=false},
            containerColor=scheme.surfaceContainerHigh,
            tonalElevation=6.dp,
            shadowElevation=10.dp,
            shape=RoundedCornerShape(22.dp)
        ){
            options.forEach{ item->
                val selected = sortMode==item.second || (isDueSoon && item.second=="快要到期")
                DropdownMenuItem(
                    text={
                        Text(
                            item.first,
                            fontWeight=if(selected) FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                    },
                    leadingIcon={
                        if(selected) Icon(Icons.Rounded.Check,contentDescription=null,tint=scheme.secondary)
                    },
                    onClick={
                        open=false
                        onSort(item.second)
                    }
                )
            }
        }
    }
}

@Composable fun HomeTopStatusFilterButton(filter:String,onFilter:(String)->Unit){
    val scheme=MaterialTheme.colorScheme
    var open by remember { mutableStateOf(false) }
    val options=listOf(
        "全部" to "全部",
        "正常" to "正常",
        "快要到期" to "即将到期",
        "已到期" to "已过期"
    )
    val label=when(filter){
        "正常" -> "正常"
        "即将到期" -> "快到期"
        "已过期" -> "已到期"
        else -> "筛选"
    }
    Box{
        HomeTopFilterChip(
            text=label,
            selected=filter!="全部"
        ){ open=true }
        DropdownMenu(
            expanded=open,
            onDismissRequest={open=false},
            containerColor=scheme.surfaceContainerHigh,
            tonalElevation=6.dp,
            shadowElevation=10.dp,
            shape=RoundedCornerShape(22.dp)
        ){
            options.forEach{ item->
                DropdownMenuItem(
                    text={
                        Text(
                            item.first,
                            fontWeight=if(filter==item.second) FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                    },
                    leadingIcon={
                        if(filter==item.second) Icon(Icons.Rounded.Check,contentDescription=null,tint=scheme.primary)
                    },
                    onClick={
                        open=false
                        onFilter(item.second)
                    }
                )
            }
        }
    }
}

@Composable fun HomeTopCountChip(count:Int){
    val scheme=MaterialTheme.colorScheme
    Box(
        Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(scheme.tertiaryContainer)
            .padding(horizontal=11.dp),
        contentAlignment=Alignment.Center
    ){
        Text("${count}张",fontSize=12.sp,fontWeight=FontWeight.ExtraBold,color=scheme.onTertiaryContainer,maxLines=1)
    }
}

@Composable fun HomeTopFilterChip(
    text:String,
    selected:Boolean,
    accent:Color=MaterialTheme.colorScheme.primary,
    onClick:()->Unit
){
    val scheme=MaterialTheme.colorScheme
    val shape=RoundedCornerShape(17.dp)
    val container=if(selected) accent.copy(alpha=.18f) else scheme.surfaceContainerHigh.copy(alpha=.9f)
    val content=if(selected) accent else scheme.onSurfaceVariant
    Box(
        Modifier
            .height(34.dp)
            .clip(shape)
            .background(container)
            .border(.8.dp,if(selected) accent.copy(alpha=.48f) else scheme.outlineVariant.copy(alpha=.52f),shape)
            .motionClickable(pressedScale=.95f){onClick()}
            .padding(horizontal=11.dp),
        contentAlignment=Alignment.Center
    ){
        Text(text,fontSize=12.sp,fontWeight=FontWeight.Bold,color=content,maxLines=1,overflow=TextOverflow.Ellipsis)
    }
}

@Composable fun Home(ctx:Context,records:List<PhoneNumberRecord>,settings:App设置,search:String,onSearch:(String)->Unit,filter:String,sortMode:String,on筛选:(String)->Unit,on排序:(String)->Unit,onAdd:()->Unit,on编辑:(PhoneNumberRecord)->Unit,onDel:(PhoneNumberRecord)->Unit,onDial:(PhoneNumberRecord)->Unit,onTraffic:(PhoneNumberRecord)->Unit,onKeep:(PhoneNumberRecord,Int)->Unit,onReorder:(List<String>)->Unit={}){
    val today=LocalDate.now()
    fun daysOf(r:PhoneNumberRecord)=runCatching{LocalDate.parse(r.expireDate).toEpochDay()-today.toEpochDay()}.getOrNull()
    val q=search.trim().lowercase()
    var sorting by remember{ mutableStateOf(false) }
    var orderedIds by remember(records){ mutableStateOf(records.sortedBy{ if(it.sortOrder>0) it.sortOrder else Int.MAX_VALUE }.map{it.id}) }
    val filtered=records.filter{ r->
        val d=daysOf(r)
        val ok=when(filter){"正常"->d!=null && d>settings.remind天;"即将到期"->d!=null && d in 0..settings.remind天;"已过期"->d!=null && d<0;else->true}
        ok && (q.isEmpty() || (r.number+r.operator+r.countryName+r.countryCode+r.note).lowercase().contains(q))
    }
    val customSorted=filtered.sortedWith(compareBy<PhoneNumberRecord>{ val i=orderedIds.indexOf(it.id); if(i>=0) i else Int.MAX_VALUE }.thenBy{ if(it.sortOrder>0) it.sortOrder else Int.MAX_VALUE })
    val titleCollator = remember { java.text.Collator.getInstance(java.util.Locale.CHINA) }
    fun createdDay(r:PhoneNumberRecord)=runCatching{LocalDate.parse(r.createdAt).toEpochDay()}.getOrDefault(Long.MIN_VALUE)
    fun sortTitle(r:PhoneNumberRecord)=r.operator.ifBlank{r.countryName}.ifBlank{r.number}.ifBlank{r.countryCode}
    val shown=if(sorting) customSorted else when(sortMode){
        "添加日期"->filtered.sortedByDescending{ createdDay(it) }
        "快要到期","到期近"->filtered.sortedBy{ daysOf(it) ?: Long.MAX_VALUE }
        "到期远"->filtered.sortedByDescending{ daysOf(it) ?: Long.MIN_VALUE }
        "首字母"->filtered.sortedWith{ a,b ->
            val primary = titleCollator.compare(sortTitle(a),sortTitle(b))
            if(primary!=0) primary else createdDay(b).compareTo(createdDay(a))
        }
        "保号周期"->filtered.sortedWith(compareBy<PhoneNumberRecord>{ if(it.longTerm) Int.MAX_VALUE else it.cycleDays.coerceAtLeast(0) }.thenBy{ sortTitle(it) })
        else->customSorted
    }
    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        if(!sorting) return@rememberReorderableLazyListState
        val visible=customSorted.map{it.id}.toMutableList()
        val fromIndex=(from.index-1).coerceIn(0, visible.lastIndex)
        val toIndex=(to.index-1).coerceIn(0, visible.lastIndex)
        if(fromIndex==toIndex) return@rememberReorderableLazyListState
        visible.add(toIndex, visible.removeAt(fromIndex))
        val rest=orderedIds.filterNot{visible.contains(it)}
        orderedIds=visible+rest
    }
    Box(Modifier.fillMaxSize()){
        AppBackground(settings)
        val topPadding = (WindowInsets.statusBars.asPaddingValues().calculateTopPadding() - 12.dp).coerceAtLeast(12.dp)
        LazyColumn(state=lazyListState,modifier=Modifier.fillMaxSize().padding(horizontal=20.dp),contentPadding=PaddingValues(top=topPadding),verticalArrangement=Arrangement.spacedBy(12.dp)){
            item{
                ExpressiveInlineSearch(search,onSearch,shown.size,filter,sortMode,on筛选,on排序)
            }
            if(false) item{
                HomeSummaryHeader(records,settings)
            }
            if(sorting) item{
                Surface(
                    shape=RoundedCornerShape(22.dp),
                    color=MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha=.94f),
                    tonalElevation=3.dp,
                    shadowElevation=2.dp,
                    border=BorderStroke(.8.dp,MaterialTheme.colorScheme.outlineVariant.copy(alpha=.55f)),
                    modifier=Modifier.fillMaxWidth()
                ){
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=10.dp),
                        verticalAlignment=Alignment.CenterVertically
                    ){
                        Column(Modifier.weight(1f)){
                            Text("排序中",fontSize=15.sp,fontWeight=FontWeight.ExtraBold,color=MaterialTheme.colorScheme.onSurface)
                            Text("拖动卡片调整顺序",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("取消",fontSize=14.sp,fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.motionClickable{
                            sorting=false
                            orderedIds=records.sortedBy{ if(it.sortOrder>0) it.sortOrder else Int.MAX_VALUE }.map{it.id}
                        }.padding(horizontal=10.dp,vertical=8.dp))
                        Text("完成",fontSize=14.sp,fontWeight=FontWeight.ExtraBold,color=MaterialTheme.colorScheme.primary,modifier=Modifier.motionClickable{
                            sorting=false
                            onReorder(orderedIds)
                        }.padding(horizontal=10.dp,vertical=8.dp))
                    }
                }
            }
            if(shown.isEmpty()) item{ Box(Modifier.fillMaxWidth().height(300.dp),contentAlignment=Alignment.Center){ ExpressiveEmptyState() } }
            else items(shown,key={it.id},contentType={"sim-card"}){ r->
                if(sorting){
                    ReorderableItem(reorderableLazyListState, key=r.id) { isDragging ->
                        CompactSimCard(r,on编辑,onDel,onTraffic,onKeep,daysOf(r),settings.remind天,settings.showFlag,settings.dark,settings.bankCardStyle,true,{},Modifier.longPressDraggableHandle(),isDragging)
                    }
                }else{
                    CompactSimCard(r,on编辑,onDel,onTraffic,onKeep,daysOf(r),settings.remind天,settings.showFlag,settings.dark,settings.bankCardStyle,false,{ sorting=true; orderedIds=records.sortedBy{ if(it.sortOrder>0) it.sortOrder else Int.MAX_VALUE }.map{it.id} })
                }
            }
            item{ Spacer(Modifier.height(90.dp)) }
        }
        if(false && !sorting){
            Box(Modifier.align(Alignment.BottomEnd).padding(end=20.dp,bottom=90.dp)){
                ExtendedFloatingActionButton(
                    onClick=onAdd,
                    containerColor=MaterialTheme.colorScheme.primaryContainer,
                    contentColor=MaterialTheme.colorScheme.onPrimaryContainer,
                    shape=RoundedCornerShape(22.dp),
                    icon={Text("＋",fontSize=22.sp,fontWeight=FontWeight.Medium)},
                    text={Text(L("号码"),fontWeight=FontWeight.SemiBold)}
                )
            }
        }
    }
}

@Composable fun CompactExpressiveHomeSummary(records:List<PhoneNumberRecord>,settings:App设置){
    val scheme=MaterialTheme.colorScheme
    val today=LocalDate.now()
    fun daysOf(r:PhoneNumberRecord)=runCatching{LocalDate.parse(r.expireDate).toEpochDay()-today.toEpochDay()}.getOrNull()
    val expired=records.count{ (daysOf(it) ?: Long.MAX_VALUE) < 0 }
    val dueSoon=records.count{ val d=daysOf(it); d!=null && d in 0..settings.remind天}
    val normal=(records.size-expired-dueSoon).coerceAtLeast(0)
    Surface(
        shape=RoundedCornerShape(topStart=30.dp,topEnd=18.dp,bottomStart=18.dp,bottomEnd=30.dp),
        color=Color.Transparent,
        tonalElevation=3.dp,
        shadowElevation=1.dp,
        modifier=Modifier.fillMaxWidth().padding(top=8.dp)
    ){
        Row(
            Modifier
                .background(Brush.linearGradient(listOf(scheme.primaryContainer,scheme.tertiaryContainer.copy(alpha=.72f),scheme.secondaryContainer.copy(alpha=.84f))))
                .padding(horizontal=14.dp,vertical=12.dp),
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(8.dp)
        ){
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(0.dp)){
                Text("号码概览",style=MaterialTheme.typography.titleMedium,color=scheme.onPrimaryContainer,maxLines=1,overflow=TextOverflow.Ellipsis)
                Text("共 ${records.size} 张 SIM",style=MaterialTheme.typography.labelMedium,color=scheme.onPrimaryContainer.copy(alpha=.70f),maxLines=1,overflow=TextOverflow.Ellipsis)
            }
            CompactMetric("正常",normal,scheme.secondary,Modifier.weight(.72f))
            CompactMetric("临近",dueSoon,scheme.tertiary,Modifier.weight(.72f))
            CompactMetric("过期",expired,scheme.error,Modifier.weight(.72f))
        }
    }
}

@Composable fun CompactMetric(label:String,value:Int,color:Color,modifier:Modifier=Modifier){
    Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surface.copy(alpha=.72f),modifier=modifier){
        Column(Modifier.padding(horizontal=8.dp,vertical=8.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(0.dp)){
            Text(value.toString(),fontSize=18.sp,fontWeight=FontWeight.ExtraBold,color=color,maxLines=1)
            Text(label,fontSize=10.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
        }
    }
}

@Composable fun ExpressiveHomeSummary(records:List<PhoneNumberRecord>,settings:App设置){
    CompactExpressiveHomeSummary(records,settings)
    return
    val scheme=MaterialTheme.colorScheme
    val today=LocalDate.now()
    fun daysOf(r:PhoneNumberRecord)=runCatching{LocalDate.parse(r.expireDate).toEpochDay()-today.toEpochDay()}.getOrNull()
    val expired=records.count{ (daysOf(it) ?: Long.MAX_VALUE) < 0 }
    val dueSoon=records.count{ val d=daysOf(it); d!=null && d in 0..settings.remind天}
    val normal=(records.size-expired-dueSoon).coerceAtLeast(0)
    val summaryBrush = Brush.linearGradient(listOf(scheme.secondaryContainer,scheme.surfaceContainerHighest,scheme.tertiaryContainer.copy(alpha=.78f),scheme.primaryContainer))
    Surface(
        shape=RoundedCornerShape(topStart=42.dp,topEnd=24.dp,bottomStart=26.dp,bottomEnd=42.dp),
        color=Color.Transparent,
        tonalElevation=4.dp,
        shadowElevation=1.dp,
        modifier=Modifier.fillMaxWidth().padding(top=12.dp)
    ){
        Column(Modifier.background(summaryBrush).padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(14.dp)){
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(4.dp)){
                    Text("SIM 号码库",style=MaterialTheme.typography.labelLarge,color=scheme.primary)
                    Text("号码概览",style=MaterialTheme.typography.titleLarge,color=scheme.onSurface,maxLines=1,overflow=TextOverflow.Ellipsis)
                    Text("续费周期、运营商和余额",style=MaterialTheme.typography.bodyMedium,color=scheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
                }
                Surface(shape=RoundedCornerShape(26.dp),color=scheme.primaryContainer,contentColor=scheme.onPrimaryContainer){
                    Text(records.size.toString(),style=MaterialTheme.typography.displaySmall,modifier=Modifier.padding(horizontal=18.dp,vertical=10.dp))
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                SummaryMetric("正常",normal.toString(),scheme.secondary,Modifier.weight(1f))
                SummaryMetric("临近",dueSoon.toString(),scheme.tertiary,Modifier.weight(1f))
                SummaryMetric("过期",expired.toString(),scheme.error,Modifier.weight(1f))
            }
        }
    }
}

@Composable fun HomeSummaryHeader(records:List<PhoneNumberRecord>,settings:App设置){
    ExpressiveHomeSummary(records,settings)
    return
    val scheme=MaterialTheme.colorScheme
    val today=LocalDate.now()
    fun daysOf(r:PhoneNumberRecord)=runCatching{LocalDate.parse(r.expireDate).toEpochDay()-today.toEpochDay()}.getOrNull()
    val expired=records.count{ (daysOf(it) ?: Long.MAX_VALUE) < 0 }
    val dueSoon=records.count{ val d=daysOf(it); d!=null && d in 0..settings.remind天 }
    val normal=(records.size-expired-dueSoon).coerceAtLeast(0)
    Surface(
        shape=RoundedCornerShape(30.dp),
        color=scheme.primaryContainer,
        tonalElevation=2.dp,
        modifier=Modifier.fillMaxWidth().padding(top=12.dp)
    ){
        Box(Modifier.background(Brush.linearGradient(listOf(scheme.primaryContainer,scheme.tertiaryContainer,scheme.secondaryContainer))).padding(18.dp)){
            Column(verticalArrangement=Arrangement.spacedBy(14.dp)){
                Row(verticalAlignment=Alignment.CenterVertically){
                    Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)){
                        Text(L("号码"),style=MaterialTheme.typography.headlineSmall,color=scheme.onPrimaryContainer)
                        Text(L("搜索运营商、国家或号码"),style=MaterialTheme.typography.bodyMedium,color=scheme.onPrimaryContainer.copy(alpha=.72f),maxLines=1,overflow=TextOverflow.Ellipsis)
                    }
                    Box(Modifier.size(52.dp).clip(RoundedCornerShape(18.dp)).background(scheme.surface.copy(alpha=.64f)),contentAlignment=Alignment.Center){
                        Text(records.size.toString(),fontSize=22.sp,fontWeight=FontWeight.Bold,color=scheme.primary)
                    }
                }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    SummaryMetric(L("全部"),records.size.toString(),scheme.primary,Modifier.weight(1f))
                    SummaryMetric(L("正常"),normal.toString(),scheme.secondary,Modifier.weight(1f))
                    SummaryMetric(L("即将到期"),dueSoon.toString(),Color(0xFFB06000),Modifier.weight(1f))
                    SummaryMetric(L("已过期"),expired.toString(),scheme.error,Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable fun SummaryMetric(label:String,value:String,color:Color,modifier:Modifier=Modifier){
    Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surface.copy(alpha=.72f),modifier=modifier){
        Column(Modifier.padding(horizontal=10.dp,vertical=9.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(2.dp)){
            Text(value,fontSize=18.sp,fontWeight=FontWeight.Bold,color=color,maxLines=1)
            Text(label,fontSize=10.sp,fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)
        }
    }
}

@Composable fun SmallActionPill(text:String,color:Color=Color(0xFF007AFF),onClick:()->Unit){
    val source=remember{ MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if(pressed).96f else 1f,animationSpec=tween(120),label="pillPress")
    Text(text,fontSize=12.sp,fontWeight=FontWeight.SemiBold,color=color,modifier=Modifier.graphicsLayer(scaleX=scale,scaleY=scale).clip(RoundedCornerShape(999.dp)).background(color.copy(alpha=.08f)).clickable(interactionSource=source,indication=null){onClick()}.padding(horizontal=9.dp,vertical=5.dp))
}

@Composable fun TinyActionButton(text:String,color:Color=Color(0xFF007AFF),onClick:()->Unit){ SmallActionPill(text,color,onClick) }

@OptIn(ExperimentalLayoutApi::class)
@Composable fun DefaultNumberCard(r:PhoneNumberRecord,on编辑:(PhoneNumberRecord)->Unit,onDel:(PhoneNumberRecord)->Unit,onDial:(PhoneNumberRecord)->Unit,onTraffic:(PhoneNumberRecord)->Unit,onKeep:(PhoneNumberRecord,Int)->Unit){
    val today=LocalDate.now(); val days=runCatching{LocalDate.parse(r.expireDate).toEpochDay()-today.toEpochDay()}.getOrNull()
    val color=when{days==null->Color(0xFF8A94A6);days<0->Color(0xFFFF3B30);days<=7->Color(0xFFFF9500);else->Color(0xFF34C759)}
    var confirmDelete by remember{ mutableStateOf(false) }
    var keepDlg by remember{ mutableStateOf(false) }
    Card(shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=Color(0xF7FFFFFF)),elevation=CardDefaults.cardElevation(defaultElevation=6.dp),modifier=Modifier.fillMaxWidth().padding(horizontal=4.dp,vertical=2.dp).border(1.dp,Color.White.copy(alpha=.75f),RoundedCornerShape(24.dp))){
        Column(Modifier.padding(horizontal=16.dp,vertical=14.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
            Row(verticalAlignment=Alignment.CenterVertically){
                Box(Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(Color.White),contentAlignment=Alignment.Center){Text(r.flag,fontSize=27.sp)}
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){
                    Text(L("默认保号号码"),fontSize=11.sp,color=Color(0xFF007AFF),fontWeight=FontWeight.Bold)
                    Text(r.operator.ifBlank{r.countryName},fontSize=18.sp,fontWeight=FontWeight.Bold,color=Color(0xFF111827),maxLines=1,overflow=TextOverflow.Ellipsis)
                    Text("${r.countryCode} ${maskNumber(formatNumber(r.number))}",fontSize=13.sp,color=Color(0xFF4B5563))
                    Text(L("到期")+"：${r.expireDate} · "+expireText(LocalAppLanguage.current,days),fontSize=12.sp,color=color)
                    Text(L("备注")+"：${r.note.ifBlank{L("预付费 / 保号套餐")}}",fontSize=10.sp,color=Color(0xFF6B7280),maxLines=1,overflow=TextOverflow.Ellipsis)
                }
                Box(Modifier.size(36.dp).clip(RoundedCornerShape(18.dp)).background(Color(0xFF007AFF)).motionClickable{on编辑(r)},contentAlignment=Alignment.Center){Text("›",color=Color.White,fontSize=25.sp)}
            }
            FlowRow(horizontalArrangement=Arrangement.spacedBy(8.dp),verticalArrangement=Arrangement.spacedBy(6.dp),modifier=Modifier.fillMaxWidth()){
                SmallActionPill(L("保号"),Color(0xFF007AFF)){keepDlg=true}
                SmallActionPill(L("刷流量"),Color(0xFF007AFF)){onTraffic(r)}
                SmallActionPill(L("删除"),Color(0xFFFF3B30)){confirmDelete=true}
            }
        }
    }
    if(keepDlg) KeepCycleDialog(r,onKeep){keepDlg=false}
    if(confirmDelete) IOSConfirmDialog(L("删除号码？"),L("删除")+" ${r.countryCode} ${formatNumber(r.number)} "+L("删除后不可恢复"),true,{confirmDelete=false},{confirmDelete=false;onDel(r)})
}


@Composable fun OperatorLogo(name:String){
    val op=name.uppercase()
    val label=when{
        "移动" in name || "CHINA MOBILE" in op -> "CM"
        "联通" in name || "UNICOM" in op -> "CU"
        "电信" in name || "TELECOM" in op -> "CT"
        "广电" in name -> "CB"
        "US MOBILE" in op -> "USM"
        "3HK" in op || "THREE" in op -> "3"
        "HKT" in op || "CSL" in op -> "HKT"
        "SMARTONE" in op -> "ST"
        "CMHK" in op -> "CMHK"
        "CTM" in op -> "CTM"
        "RAKUTEN" in op -> "R"
        "SOFTBANK" in op -> "SB"
        "DOCOMO" in op -> "doc"
        "AIS" in op -> "AIS"
        "TRUE" in op -> "TRUE"
        "DTAC" in op -> "dtac"
        "VODAFONE" in op -> "V"
        "T-MOBILE" in op -> "T"
        "AT&T" in op -> "AT&T"
        "VERIZON" in op -> "VZ"
        else -> name.take(3).ifBlank{"SIM"}
    }
    val bg=when(label){"CM"->Color(0xFF22C55E);"CU"->Color(0xFFE11D48);"CT"->Color(0xFF2563EB);"AIS"->Color(0xFF16A34A);"R"->Color(0xFFE91E63);"V"->Color(0xFFE60000);"USM"->Color(0xFF2563EB);"3"->Color(0xFF7C3AED);else->Color(0xFF111827)}
    Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(bg),contentAlignment=Alignment.Center){Text(label,fontSize=if(label.length>3) 9.sp else 14.sp,fontWeight=FontWeight.Bold,color=Color.White,maxLines=1)}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun NumberListRow(r:PhoneNumberRecord,on编辑:(PhoneNumberRecord)->Unit,onDel:(PhoneNumberRecord)->Unit,onTraffic:(PhoneNumberRecord)->Unit,onKeep:(PhoneNumberRecord,Int)->Unit,days:Long?){
    val status=if(days==null)L("未知") else if(days<0)L("已过期") else if(days<=7)L("即将到期") else L("正常")
    val statusColor=when{days!=null && days<0->Color(0xFFFF3B30);days!=null && days<=7->Color(0xFFFF9500);else->Color(0xFF007AFF)}
    var confirmDelete by remember{ mutableStateOf(false) }
    var keepDlg by remember{ mutableStateOf(false) }
    Card(shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=Color.White),elevation=CardDefaults.cardElevation(5.dp),modifier=Modifier.fillMaxWidth()){
        Column(Modifier.padding(horizontal=14.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
            Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.motionClickable(pressedScale=.985f){on编辑(r)}){
                Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(Color(0xFFF1F5FA)),contentAlignment=Alignment.Center){Text(r.flag,fontSize=25.sp)}
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(2.dp)){
                    Text(r.operator.ifBlank{r.countryName},fontSize=16.sp,fontWeight=FontWeight.Bold,maxLines=1,overflow=TextOverflow.Ellipsis)
                    Text("${r.countryCode} ${maskNumber(formatNumber(r.number))}",fontSize=12.sp,color=Color(0xFF6B7280),maxLines=1,overflow=TextOverflow.Ellipsis)
                    Text(L("到期")+"：${r.expireDate} · "+expireText(LocalAppLanguage.current,days),fontSize=12.sp,color=statusColor,maxLines=1,overflow=TextOverflow.Ellipsis)
                }
                Text(status,fontSize=10.sp,color=statusColor,modifier=Modifier.clip(RoundedCornerShape(8.dp)).background(statusColor.copy(alpha=.10f)).padding(horizontal=7.dp,vertical=4.dp))
                Spacer(Modifier.width(6.dp)); Text("›",fontSize=22.sp,color=Color(0xFF9CA3AF))
            }
            FlowRow(horizontalArrangement=Arrangement.spacedBy(7.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){
                TinyActionButton(L("保号")){keepDlg=true}
                TinyActionButton(L("刷流量")){onTraffic(r)}
                TinyActionButton(L("删除"),Color(0xFFFF3B30)){confirmDelete=true}
            }
        }
    }
    if(keepDlg) KeepCycleDialog(r,onKeep){keepDlg=false}
    if(confirmDelete) IOSConfirmDialog(L("删除号码？"),L("删除")+" ${r.countryCode} ${formatNumber(r.number)} "+L("删除后不可恢复"),true,{confirmDelete=false},{confirmDelete=false;onDel(r)})
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun KeepCycleDialog(r:PhoneNumberRecord,onKeep:(PhoneNumberRecord,Int)->Unit,onDismiss:()->Unit){
    var days by remember{ mutableStateOf(30) }
    Dialog(onDismissRequest=onDismiss){
        Surface(shape=RoundedCornerShape(30.dp),color=Color(0xFFF2F3F7),modifier=Modifier.fillMaxWidth(.92f).widthIn(max=360.dp)){
            Column(Modifier.fillMaxWidth().padding(horizontal=22.dp,vertical=24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(22.dp)){
                Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    Text(L("延长保号"),fontSize=22.sp,fontWeight=FontWeight.Bold,color=Color(0xFF111827))
                    Text("${r.countryCode} ${formatNumber(r.number)}",fontSize=15.sp,color=Color(0xFF8A94A6),maxLines=1,overflow=TextOverflow.Ellipsis)
                }
                Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)){
                    Text(L("选择周期"),fontSize=13.sp,color=Color(0xFF8A94A6),modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center)
                    FlowRow(modifier=Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),verticalArrangement=Arrangement.spacedBy(12.dp)){
                        listOf(7 to "7",15 to "15",30 to "30",90 to "90",180 to "180",365 to "365").forEach{(d,label)-> IOSChip(label,days==d,Modifier.width(68.dp).height(44.dp)){days=d} }
                    }
                }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(14.dp)){
                    Button(onClick=onDismiss,modifier=Modifier.weight(1f).height(54.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=Color.White,contentColor=Color(0xFF374151))){Text(L("取消"),fontSize=16.sp)}
                    Button(onClick={onKeep(r,days);onDismiss()},modifier=Modifier.weight(1f).height(54.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF007AFF),contentColor=Color.White)){Text(L("确认延长"),fontSize=16.sp,fontWeight=FontWeight.SemiBold)}
                }
            }
        }
    }
}

@Composable fun KeepPage(records:List<PhoneNumberRecord>,onKeep:(PhoneNumberRecord,Int)->Unit){
    var selectedId by remember{ mutableStateOf(records.firstOrNull()?.id ?: "") }; var months by remember{ mutableStateOf(30) }
    val r=records.firstOrNull{it.id==selectedId} ?: records.firstOrNull()
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        if(r==null){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(L("暂无号码"))}} else {
            IOSSection(L("选择号码")){ records.forEach{ item-> KeepChoice(item.operator.ifBlank{item.countryName}+"  "+item.countryCode+" "+maskNumber(formatNumber(item.number)), selectedId==item.id){selectedId=item.id} } }
            IOSSection(L("选择保号周期")){ listOf(7 to "7",15 to "15",30 to "30",90 to "90",180 to "180",365 to "365").forEach{(m,label)-> KeepChoice(label, months==m){months=m} } }
            Button(onClick={onKeep(r,months)},modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF007AFF))){Text(L("确认延长"))}
        }
    }
}
@Composable fun KeepChoice(text:String,selected:Boolean,onClick:()->Unit){ Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if(selected) dk(Color(0xFF1C2333),Color(0xFFEAF3FF)) else dk(Color(0xFF1C1C1E),Color.White)).motionClickable(pressedScale=.985f){onClick()}.padding(14.dp),verticalAlignment=Alignment.CenterVertically){Text(if(selected)"●" else "○",color=Color(0xFF007AFF));Spacer(Modifier.width(10.dp));Text(text,fontSize=16.sp,fontWeight=if(selected)FontWeight.Bold else FontWeight.Normal)} }


@Composable fun ToolsPage(ctx:Context,settings:App设置,records:List<PhoneNumberRecord>,onTraffic:(PhoneNumberRecord)->Unit,onDial:(PhoneNumberRecord)->Unit,onExportJson:()->Unit,onExportCsv:()->Unit,onImportText:(String)->Unit,onImportSimHub:(List<PhoneNumberRecord>) ->Unit={_->}){
    var pickTraffic by remember{ mutableStateOf(false) }
    var pickDial by remember{ mutableStateOf(false) }
    var importDlg by remember{ mutableStateOf(false) }
    var exportSimHub by remember{ mutableStateOf(false) }
    var importSimHub by remember{ mutableStateOf(false) }
    var importText by remember{ mutableStateOf("") }
    Box(Modifier.fillMaxSize()){
        AppBackground(settings)
        LazyColumn(Modifier.fillMaxSize().padding(horizontal=18.dp,vertical=16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
            item{
                IOSSection(L("常用工具")){
                    ToolRow("traffic",L("刷流量"),L("选择一个号码执行真实下载流量测试")){ pickTraffic=true }
                    ToolRow("dial",L("拨号测试"),L("选择号码并打开系统拨号器")){ pickDial=true }
                    ToolRow("export_json",L("导出 JSON"),L("生成完整 JSON 备份文本")){ onExportJson() }
                    ToolRow("export_csv",L("导出 CSV"),L("生成 CSV 表格文本")){ onExportCsv() }
                    ToolRow("import",L("导入数据"),L("粘贴 JSON 或 CSV 恢复号码列表")){ importDlg=true }
            var exportSimHub by remember{ mutableStateOf(false) }
            var importSimHub by remember{ mutableStateOf(false) }
            ToolRow("export_json",L("导出 SimHub"),L("导出为 SimHub JSON 兼容格式")){ exportSimHub=true }
            ToolRow("import",L("导入 SimHub"),L("从 SimHub JSON 文件导入号码")){ importSimHub=true }
                    ToolRow("export_json",L("导出 SimHub"),L("导出为 SimHub JSON 兼容格式")){ exportSimHub=true }
                    ToolRow("import",L("导入 SimHub"),L("从 SimHub JSON 文件导入号码")){ importSimHub=true }
                }
            }
        }
    }
    if(pickTraffic) NumberPickerDialog(L("选择刷流量号码"),records,{pickTraffic=false}){ pickTraffic=false; onTraffic(it) }
    if(pickDial) NumberPickerDialog(L("选择拨号号码"),records,{pickDial=false}){ pickDial=false; onDial(it) }
    if(importDlg) IOSImportDialog(importText,{importText=it},{importDlg=false},{onImportText(importText);importDlg=false},ctx)
    if(exportSimHub){
        val json = com.sansim.app.util.SimHubCompat.exportToJson(records)
        AlertDialog(onDismissRequest={exportSimHub=false},
            title={Text(L("导出 SimHub JSON"))},
            text={Text(L("已生成")+" ${records.size} "+L("个号码的 SimHub 兼容 JSON"))},
            confirmButton={
                Row{
                    val exportTitle=L("导出 SimHub"); Button({shareExportFile(ctx,"simj-simhub-export.json","application/json",json,exportTitle)},colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF007AFF))){Text(L("分享"))}
                    Spacer(Modifier.width(8.dp))
                    Button({exportSimHub=false},colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF8E8E93))){Text(L("关闭"))}
                }
            })
    }
    val simHubImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){ uri->
        if(uri!=null){
            val imported = com.sansim.app.util.SimHubCompat.importFromJson(ctx,uri)
            if(imported.isNotEmpty()){
                onImportSimHub(imported)
            }
        }
    }
    LaunchedEffect(importSimHub){ if(importSimHub){ simHubImportLauncher.launch("application/json"); importSimHub=false } }
}

@Composable fun ToolRow(iconType:String,title:String,sub:String,onClick:()->Unit){
    val scheme=MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).motionClickable(pressedScale=.985f){onClick()}.padding(horizontal=8.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically){
        Box(Modifier.size(42.dp).clip(RoundedCornerShape(14.dp)).background(scheme.primaryContainer),contentAlignment=Alignment.Center){ ToolLineIcon(iconType,scheme.onPrimaryContainer) }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)){Text(title,fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=scheme.onSurface);Text(sub,fontSize=12.sp,color=scheme.onSurfaceVariant,maxLines=1,overflow=TextOverflow.Ellipsis)}
        Text("›",fontSize=24.sp,color=scheme.onSurfaceVariant)
    }
}

@Composable fun ToolLineIcon(type:String,color:Color){
    Canvas(Modifier.size(22.dp)){
        val w=size.width; val h=size.height
        val stroke=Stroke(width=2.1f)
        when(type){
            "traffic"->{
                drawRoundRect(color,topLeft=Offset(w*.18f,h*.22f),size=Size(w*.64f,h*.56f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.08f,w*.08f),style=stroke)
                drawLine(color,Offset(w*.30f,h*.62f),Offset(w*.30f,h*.44f),strokeWidth=2.1f)
                drawLine(color,Offset(w*.50f,h*.62f),Offset(w*.50f,h*.34f),strokeWidth=2.1f)
                drawLine(color,Offset(w*.70f,h*.62f),Offset(w*.70f,h*.50f),strokeWidth=2.1f)
            }
            "dial"->{
                drawLine(color,Offset(w*.34f,h*.26f),Offset(w*.66f,h*.26f),strokeWidth=2.1f)
                drawArc(color,180f,180f,false,topLeft=Offset(w*.20f,h*.18f),size=Size(w*.60f,h*.42f),style=stroke)
                drawLine(color,Offset(w*.28f,h*.66f),Offset(w*.40f,h*.54f),strokeWidth=2.1f)
                drawLine(color,Offset(w*.72f,h*.66f),Offset(w*.60f,h*.54f),strokeWidth=2.1f)
            }
            "export_json"->{
                drawRoundRect(color,topLeft=Offset(w*.24f,h*.18f),size=Size(w*.52f,h*.60f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.07f,w*.07f),style=stroke)
                drawLine(color,Offset(w*.50f,h*.28f),Offset(w*.50f,h*.58f),strokeWidth=2.1f)
                drawLine(color,Offset(w*.40f,h*.48f),Offset(w*.50f,h*.58f),strokeWidth=2.1f)
                drawLine(color,Offset(w*.60f,h*.48f),Offset(w*.50f,h*.58f),strokeWidth=2.1f)
            }
            "export_csv"->{
                drawRoundRect(color,topLeft=Offset(w*.24f,h*.18f),size=Size(w*.52f,h*.60f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.07f,w*.07f),style=stroke)
                drawLine(color,Offset(w*.34f,h*.34f),Offset(w*.66f,h*.34f),strokeWidth=2.0f)
                drawLine(color,Offset(w*.34f,h*.46f),Offset(w*.66f,h*.46f),strokeWidth=2.0f)
                drawLine(color,Offset(w*.34f,h*.58f),Offset(w*.58f,h*.58f),strokeWidth=2.0f)
            }
            else->{
                drawRoundRect(color,topLeft=Offset(w*.24f,h*.18f),size=Size(w*.52f,h*.60f),cornerRadius=androidx.compose.ui.geometry.CornerRadius(w*.07f,w*.07f),style=stroke)
                drawLine(color,Offset(w*.50f,h*.58f),Offset(w*.50f,h*.28f),strokeWidth=2.1f)
                drawLine(color,Offset(w*.40f,h*.38f),Offset(w*.50f,h*.28f),strokeWidth=2.1f)
                drawLine(color,Offset(w*.60f,h*.38f),Offset(w*.50f,h*.28f),strokeWidth=2.1f)
            }
        }
    }
}

@Composable fun NumberPickerDialog(title:String,records:List<PhoneNumberRecord>,onDismiss:()->Unit,onPick:(PhoneNumberRecord)->Unit){
    Dialog(onDismissRequest=onDismiss){
        Surface(shape=RoundedCornerShape(26.dp),color=dk(Color(0xFF1C1C1E),Color(0xFFF2F3F7)),modifier=Modifier.fillMaxWidth()){
            Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
                Row(verticalAlignment=Alignment.CenterVertically){
                    Text(title,fontSize=20.sp,fontWeight=FontWeight.Bold,color=dk(Color(0xFFE5E5E7),Color(0xFF111827)),modifier=Modifier.weight(1f))
                    TextButton(onDismiss){Text(L("取消"),color=Color(0xFF007AFF))}
                }
                if(records.isEmpty()) Box(Modifier.fillMaxWidth().height(120.dp),contentAlignment=Alignment.Center){Text(L("暂无号码，请先添加号码。"),color=Color(0xFF8A94A6))}
                else LazyColumn(Modifier.heightIn(max=420.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    items(records){ r ->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(dk(Color(0xFF1C1C1E),Color.White)).motionClickable(pressedScale=.985f){onPick(r)}.padding(13.dp),verticalAlignment=Alignment.CenterVertically){
                            Text(r.flag,fontSize=25.sp); Spacer(Modifier.width(10.dp)); Column(Modifier.weight(1f)){Text(r.operator.ifBlank{r.countryName},fontWeight=FontWeight.SemiBold,color=dk(Color(0xFFE5E5E7),Color(0xFF111827))); Text("${r.countryCode} ${maskNumber(formatNumber(r.number))}",fontSize=12.sp,color=Color(0xFF6B7280))}; Text("›",fontSize=24.sp,color=dk(Color(0xFF48484A),Color(0xFFC7C7CC)))
                        }
                    }
                }
            }
        }
    }
}

@Composable fun IOSImportDialog(value:String,onValue:(String)->Unit,onDismiss:()->Unit,onImport:()->Unit,ctx:Context){
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if(uri!=null){
            val text = runCatching{ ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.use{it.readText()} }?.getOrNull()
            if(!text.isNullOrBlank()) onValue(text)
        }
    }
    Dialog(onDismissRequest=onDismiss){
        Surface(shape=RoundedCornerShape(26.dp),color=dk(Color(0xFF1C1C1E),Color(0xFFF2F3F7)),modifier=Modifier.fillMaxWidth()){
            Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
                Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.fillMaxWidth()){
                    Text(L("导入数据"),fontSize=20.sp,fontWeight=FontWeight.Bold,color=dk(Color(0xFFE5E5E7),Color(0xFF111827)))
                    Text(L("支持 JSON / CSV，JSON 可同时恢复号码和配置。导入前建议先导出备份。"),fontSize=13.sp,color=Color(0xFF8A94A6),lineHeight=18.sp)
                }
                Button(onClick={filePicker.launch("*/*")},modifier=Modifier.fillMaxWidth().height(44.dp),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF34C759),contentColor=Color.White)){Text(L("从文件导入"))}
                TextField(value=value,onValueChange=onValue,modifier=Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(18.dp)),placeholder={Text(L("或粘贴 JSON / CSV 数据"))},minLines=5,colors=TextFieldDefaults.colors(focusedContainerColor=dk(Color(0xFF2C2C2E),Color.White),unfocusedContainerColor=dk(Color(0xFF2C2C2E),Color.White),focusedIndicatorColor=Color.Transparent,unfocusedIndicatorColor=Color.Transparent))
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
                    Button(onClick=onDismiss,modifier=Modifier.weight(1f).height(48.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=dk(Color(0xFF2C2C2E),Color.White),contentColor=dk(Color(0xFFD1D5DB),Color(0xFF374151)))){Text(L("取消"))}
                    Button(onClick=onImport,modifier=Modifier.weight(1f).height(48.dp),shape=RoundedCornerShape(16.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF007AFF),contentColor=Color.White)){Text(L("导入"))}
                }
            }
        }
    }
}

@Composable fun SimHubStat(t:String,v:String,c:Color,m:Modifier){ Card(m,shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surface),elevation=CardDefaults.cardElevation(1.5.dp)){Column(Modifier.padding(vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(v,fontSize=22.sp,fontWeight=FontWeight.Bold,color=c);Text(t,fontSize=12.sp,color=dk(Color(0xFF8E8E93),Color(0xFF8A94A6)))}} }

@Composable fun SoftStat(t:String,v:String,c:Color,m:Modifier){ SimHubStat(t,v,c,m) }

@Composable fun QQStat(t:String,v:String,c:Color,m:Modifier){ SimHubStat(t,v,c,m) }

@Composable fun Stat(t:String,v:String,m:Modifier){ SimHubStat(t,v,Color(0xFF007AFF),m) }
@Composable fun NumberCard(r:PhoneNumberRecord,on编辑:(PhoneNumberRecord)->Unit,onDel:(PhoneNumberRecord)->Unit,onDial:(PhoneNumberRecord)->Unit,onTraffic:(PhoneNumberRecord)->Unit,onKeep:(PhoneNumberRecord,Int)->Unit){ SimHubCard(r,on编辑,onDel,onDial,onTraffic,onKeep) }

@Composable fun SimHubCard(r:PhoneNumberRecord,on编辑:(PhoneNumberRecord)->Unit,onDel:(PhoneNumberRecord)->Unit,onDial:(PhoneNumberRecord)->Unit,onTraffic:(PhoneNumberRecord)->Unit,onKeep:(PhoneNumberRecord,Int)->Unit){
    val exp=runCatching{LocalDate.parse(r.expireDate)}.getOrNull()
    val today=LocalDate.now()
    val days=exp?.toEpochDay()?.minus(today.toEpochDay())
    val progress=if(days==null) 0f else (days.coerceIn(0,90)/90f).coerceIn(0f,1f)
    val longTerm = days!=null && days>60
    var menu by remember{ mutableStateOf(false) }
    var confirm删除 by remember{ mutableStateOf(false) }
    Card(shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=dk(Color(0xF81C1C1E),Color(0xF8FFFFFF))),elevation=CardDefaults.cardElevation(defaultElevation=6.dp),modifier=Modifier.fillMaxWidth().padding(vertical=2.dp).border(1.dp,dk(Color(0xFF2C2C2E).copy(alpha=.70f),Color.White.copy(alpha=.70f)),RoundedCornerShape(24.dp))){
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.Top){
                Box(Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(dk(Color(0xFF2C2C2E),Color(0xFFF1F5FA))),contentAlignment=Alignment.Center){Text(r.flag,fontSize=25.sp)}
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){
                    Row(verticalAlignment=Alignment.CenterVertically){
                        Text(r.operator.ifBlank{r.countryName},fontSize=18.sp,fontWeight=FontWeight.Bold,color=dk(Color(0xFFE5E5E7),Color(0xFF111827)),maxLines=1,overflow=TextOverflow.Ellipsis,modifier=Modifier.weight(1f,false))
                        if(longTerm){ Spacer(Modifier.width(6.dp)); Text(L("长期号码"),fontSize=11.sp,color=Color.White,fontWeight=FontWeight.Bold,modifier=Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF007AFF)).padding(horizontal=6.dp,vertical=2.dp)) }
                    }
                    Row(verticalAlignment=Alignment.CenterVertically){Text(planLine(r).ifBlank{ if(r.note.isBlank()) L("预付费") else r.note },fontSize=12.sp,color=Color(0xFF6B7280),maxLines=1,overflow=TextOverflow.Ellipsis); Spacer(Modifier.width(7.dp)); Text(if(days==null) "无到期日" else if(days<0) "❌ "+expireText(LocalAppLanguage.current,days) else "✅ ${r.expireDate} · "+expireText(LocalAppLanguage.current,days),fontSize=13.sp,color=if(days!=null&&days<0) Color(0xFFFF3B30) else Color(0xFF34C759),maxLines=1,overflow=TextOverflow.Ellipsis)}
                }
            }
            LinearProgressIndicator(progress={progress},modifier=Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(5.dp)),color=Color(0xFF34C759),trackColor=dk(Color(0xFF2C2C2E),Color(0xFFE9EDF3)))
            Row(verticalAlignment=Alignment.CenterVertically){Text("☎",fontSize=16.sp); Spacer(Modifier.width(7.dp)); Text("${r.countryCode} ${maskNumber(formatNumber(r.number))}",fontSize=13.sp,color=Color(0xFF4B5563),modifier=Modifier); Text("👁",fontSize=15.sp)}
            Row(verticalAlignment=Alignment.CenterVertically){Text("#",fontSize=14.sp,fontWeight=FontWeight.Bold); Spacer(Modifier.width(7.dp)); Text("EID: ${fakeEidForCard(r)}",fontSize=12.sp,color=dk(Color(0xFF8E8E93),Color(0xFF6B7280)),maxLines=1,overflow=TextOverflow.Ellipsis)}
            TextButton(onClick={menu=!menu},contentPadding=PaddingValues(0.dp)){Text(if(menu) L("隐藏详情") else "⌄ "+L("显示二维码"),color=Color(0xFF007AFF),fontSize=14.sp)}
        }
    }
    if(menu){
        Card(shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=dk(Color(0xFF1C1C1E),Color.White)),elevation=CardDefaults.cardElevation(0.dp),modifier=Modifier.fillMaxWidth().padding(top=8.dp)){
            Column{
                MenuRow("✎",L("编辑"),Color(0xFF374151)){on编辑(r)}
                Divider(color=dk(Color(0xFF2C2C2E),Color(0xFFE5E7EB)),modifier=Modifier.padding(start=48.dp))
                MenuRow("⧉",L("复制号码"),Color(0xFF374151)){onDial(r)}
                Divider(color=dk(Color(0xFF2C2C2E),Color(0xFFE5E7EB)),modifier=Modifier.padding(start=48.dp))
                MenuRow("🗑",L("删除"),Color(0xFFFF3B30)){confirm删除=true}
            }
        }
    }
    if(confirm删除) IOSConfirmDialog(L("删除号码？"),L("删除")+" ${r.countryCode} ${formatNumber(r.number)} "+L("删除后不可恢复"),true,{confirm删除=false},{confirm删除=false;onDel(r)})
}

@Composable fun MenuRow(icon:String,title:String,color:Color,onClick:()->Unit){ Row(Modifier.fillMaxWidth().motionClickable(pressedScale=.985f){onClick()}.padding(horizontal=16.dp,vertical=14.dp),verticalAlignment=Alignment.CenterVertically){Text(icon,fontSize=18.sp); Spacer(Modifier.width(10.dp)); Text(title,fontSize=16.sp,color=color,fontWeight=if(color==Color(0xFFFF3B30)) FontWeight.Bold else FontWeight.Normal)} }
fun maskNumber(n:String):String{ val ds=n.filter{it.isDigit()}; return if(ds.length<=4) n else ds.take(4)+" •••• "+ds.takeLast(4) }
fun fakeEidForCard(r:PhoneNumberRecord):String{ val seed=(r.id+r.number).hashCode().toString().filter{it.isDigit()}.padEnd(24,'0').take(24); return "89044000 ${seed.chunked(8).joinToString(" ")}" }

fun cardTypeLabel(type:String):String = when(type){
    "monthly" -> "月租卡"
    "postpaid" -> "后付费"
    "data" -> "流量卡"
    "iot" -> "物联卡"
    "keep" -> "保号卡"
    "other" -> "自定义"
    else -> "预付费"
}
fun paymentCycleText(r:PhoneNumberRecord):String{
    if(r.cyclePaymentMinorUnits<=0 || r.currencyCode.isBlank()) return ""
    val major=r.cyclePaymentMinorUnits/100
    val minor=r.cyclePaymentMinorUnits%100
    val amount=if(minor==0) major.toString() else "${major}.${String.format("%02d",minor)}"
    val unit=when(r.cycleDays){7->"/周";30->"/月";90->"/季";365->"/年";else->"/${r.cycleDays}天"}
    return "$amount ${r.currencyCode}$unit"
}
fun planLine(r:PhoneNumberRecord):String = listOf(cardTypeLabel(r.cardType), paymentCycleText(r).ifBlank{null}).filterNotNull().joinToString(" · ")
fun bankCardSemanticIndex(r:PhoneNumberRecord):Int{
    // Look up ISO code from dial code using Countries list
    val iso = Countries.list.firstOrNull { it.code == r.countryCode }?.iso ?: ""
    val bankCardIsoMap = mapOf(
        "AE" to 176,
        "AL" to 268,
        "AR" to 24,
        "AT" to 165,
        "AU" to 234,
        "BA" to 232,
        "BD" to 111,
        "BE" to 254,
        "BG" to 139,
        "BN" to 94,
        "CA" to 34,
        "CH" to 188,
        "CL" to 83,
        "CN" to 84,
        "CO" to 183,
        "CZ" to 206,
        "DE" to 260,
        "DK" to 19,
        "EE" to 61,
        "EG" to 122,
        "ES" to 201,
        "FI" to 28,
        "FR" to 124,
        "GB" to 204,
        "GR" to 107,
        "HK" to 134,
        "HR" to 180,
        "HU" to 58,
        "ID" to 167,
        "IE" to 87,
        "IL" to 213,
        "IN" to 82,
        "IS" to 214,
        "IT" to 195,
        "JO" to 229,
        "JP" to 144,
        "KE" to 123,
        "KH" to 216,
        "KP" to 160,
        "KR" to 15,
        "KW" to 212,
        "KZ" to 60,
        "LA" to 226,
        "LK" to 220,
        "LT" to 265,
        "LV" to 189,
        "MA" to 269,
        "ME" to 37,
        "MM" to 65,
        "MO" to 174,
        "MV" to 238,
        "MX" to 178,
        "MY" to 137,
        "NG" to 64,
        "NL" to 10,
        "NP" to 244,
        "NZ" to 262,
        "OM" to 105,
        "PE" to 225,
        "PH" to 168,
        "PK" to 115,
        "PL" to 228,
        "PT" to 6,
        "QA" to 68,
        "RO" to 184,
        "RS" to 27,
        "RU" to 221,
        "SA" to 191,
        "SE" to 109,
        "SG" to 43,
        "SI" to 66,
        "SK" to 203,
        "TH" to 223,
        "TR" to 209,
        "UA" to 222,
        "US" to 38,
        "VN" to 257,
        "ZA" to 245,
    )
    return bankCardIsoMap[iso] ?: run {
        val allIndices = listOf(6, 10, 15, 19, 24, 27, 28, 34, 37, 38, 43, 58, 60, 61, 64, 65, 66, 68, 82, 83, 84, 87, 94, 105, 107, 109, 111, 115, 122, 123, 124, 134, 137, 139, 144, 160, 165, 167, 168, 174, 176, 178, 180, 183, 184, 188, 189, 191, 195, 201, 203, 204, 206, 209, 212, 213, 214, 216, 220, 221, 222, 223, 225, 226, 228, 229, 232, 234, 238, 244, 245, 254, 257, 260, 262, 265, 268, 269)
        val raw=(r.id+r.number+r.countryCode).hashCode()
        val safe=if(raw==Int.MIN_VALUE) 0 else kotlin.math.abs(raw)
        allIndices[safe % allIndices.size]
    }
}
fun cardBackgroundPath(r:PhoneNumberRecord, iso:String, bankCardStyle:Boolean=false):String{
    val bg=r.cardBackgroundAssetName.trim()
    if(bg.isNotBlank()){
        return when {
            bg.startsWith("bank_card_") -> "bank_card_backgrounds/$bg"
            bg.endsWith("-lighttrail.jpg") -> "card_backgrounds/$bg"
            else -> "flag_backgrounds/$bg"
        }
    }
    if(bankCardStyle){
        val idx=bankCardSemanticIndex(r)
        return "bank_card_backgrounds/bank_card_${String.format("%03d",idx)}.jpg"
    }
    return if(iso.isBlank()) "" else "flag_backgrounds/${iso.lowercase()}.jpg"
}


@OptIn(ExperimentalLayoutApi::class)
@Composable fun Full编辑Screen(init:PhoneNumberRecord,onDismiss:()->Unit,onSave:(PhoneNumberRecord)->Unit,onDelete:(PhoneNumberRecord)->Unit={}){
    BackHandler { onDismiss() }
    var r by remember { mutableStateOf(init) }
    var countryDlg by remember { mutableStateOf(false) }
    var qrText by remember { mutableStateOf("") }
    var qrDlg by remember { mutableStateOf(false) }
    var qrInput by remember { mutableStateOf("") }
    var showDel by remember { mutableStateOf(false) }
    val editLang = LocalAppLanguage.current
    val albumLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if(uri!=null) {
            qrText = tr(editLang,"已选择相册图片：")+"${uri}"
        }
    }
    // Cycle payment display
    val paymentDisplay = if(r.cyclePaymentMinorUnits > 0 && r.currencyCode.isNotBlank()) {
        val major = r.cyclePaymentMinorUnits / 100
        val minor = r.cyclePaymentMinorUnits % 100
        "${major}.${String.format("%02d",minor)} ${r.currencyCode}"
    } else ""

    Box(Modifier.fillMaxSize().background(dk(Color(0xFF0B0F17),Color(0xFFF2F3F7)))){
        Column(Modifier.fillMaxSize()){
            val editStatusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
            val editScheme=MaterialTheme.colorScheme
            val editHeaderShape=RoundedCornerShape(24.dp)
            Surface(
                shape=editHeaderShape,
                color=editScheme.surface.copy(alpha=.9f),
                tonalElevation=2.dp,
                shadowElevation=1.dp,
                modifier=Modifier
                    .fillMaxWidth()
                    .padding(start=18.dp,end=18.dp,top=editStatusBarTop+6.dp,bottom=10.dp)
                    .border(.8.dp,editScheme.outlineVariant.copy(alpha=.48f),editHeaderShape)
            ){
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(start=8.dp,end=8.dp),
                    horizontalArrangement=Arrangement.SpaceBetween,
                    verticalAlignment=Alignment.CenterVertically
                ){
                    FilledTonalIconButton(
                        onClick=onDismiss,
                        modifier=Modifier.size(42.dp),
                        shape=RoundedCornerShape(16.dp),
                        colors=IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor=editScheme.surfaceContainerHighest,
                            contentColor=editScheme.primary
                        )
                    ){
                        Icon(Icons.Rounded.Close,contentDescription=L("关闭"),modifier=Modifier.size(20.dp))
                    }
                    Text(
                        if(init.number.isBlank()) L("新增 eSIM") else L("编辑 eSIM"),
                        fontSize=18.sp,
                        lineHeight=22.sp,
                        fontWeight=FontWeight.ExtraBold,
                        color=editScheme.onSurface,
                        maxLines=1,
                        overflow=TextOverflow.Ellipsis,
                        modifier=Modifier
                            .weight(1f)
                            .padding(horizontal=12.dp),
                        textAlign=TextAlign.Center
                    )
                    Button(
                        onClick={onSave(r)},
                        modifier=Modifier.height(42.dp),
                        shape=RoundedCornerShape(16.dp),
                        contentPadding=PaddingValues(start=13.dp,end=15.dp),
                        colors=ButtonDefaults.buttonColors(
                            containerColor=editScheme.primary,
                            contentColor=editScheme.onPrimary
                        )
                    ){
                        Icon(Icons.Rounded.Check,contentDescription=null,modifier=Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(L("完成"),fontSize=14.sp,lineHeight=18.sp,fontWeight=FontWeight.Bold)
                    }
                }
            }
            LazyColumn(Modifier.fillMaxSize().padding(horizontal=18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){

                // ── 运营商 ──
                item{
                    SettingsSection(L("运营商")){
                        val currentIso = remember(r.countryCode, r.countryName){ Countries.list.firstOrNull{it.code==r.countryCode && it.name==r.countryName}?.iso ?: Countries.list.firstOrNull{it.code==r.countryCode}?.iso ?: r.countryName }
                        Row(verticalAlignment=Alignment.CenterVertically){
                            OperatorLogo44(r.operator.ifBlank{r.countryName}, currentIso)
                            Spacer(Modifier.width(10.dp))
                            Column{
                                Text(r.operator.ifBlank{r.countryName},fontSize=17.sp,fontWeight=FontWeight.SemiBold,color=dk(Color(0xFFE5E5E7),Color(0xFF111827)))
                                Text(L("卡片将优先显示这个 App 图标"),fontSize=11.sp,color=Color(0xFF8A94A6))
                            }
                        }
                        IOSDividerLine()
                        IOSValueRow(L("更换运营商预设"),L("也可以继续手动输入")){ countryDlg=true }
                        IOSDividerLine()
                        IOSField(L("运营商名称"),r.operator,{r=r.copy(operator=it)},L("如 AIS / Vodafone / 中国移动"))
                        val detectedOperator = remember(r.number, currentIso){ guessOperator(r.number,currentIso) }
                        if(detectedOperator.isNotBlank()){
                            Text(L("当前识别")+": "+detectedOperator,fontSize=11.sp,color=Color(0xFF8A94A6))
                        }
                        val suggestions = remember(currentIso){ OperatorDatabase.byCountry(currentIso).take(8) }
                        if(suggestions.isNotEmpty()){
                            Text(L("推荐运营商"),fontSize=13.sp,fontWeight=FontWeight.SemiBold,color=Color(0xFF6B7280),modifier=Modifier.padding(top=6.dp))
                            FlowRow(horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                                suggestions.forEach{ op ->
                                    val active = r.operator.equals(op.carrierName,true)
                                    IOSChip(op.carrierName,active){ r=r.copy(operator=op.carrierName) }
                                }
                            }
                        }
                    }
                }

                // ── 套餐信息 ──
                item{
                    SettingsSection(L("套餐信息")){
                        Text("卡类型",fontSize=13.sp,color=Color(0xFF8A94A6),modifier=Modifier.padding(start=2.dp))
                        FlowRow(horizontalArrangement=Arrangement.spacedBy(7.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
                            listOf("prepaid" to "预付费", "monthly" to "月租卡", "postpaid" to "后付费", "data" to "流量卡", "iot" to "物联卡", "keep" to "保号卡", "other" to "自定义").forEach{ item ->
                                IOSChip(item.second,r.cardType==item.first){ r=r.copy(cardType=item.first) }
                            }
                        }
                        IOSDividerLine()
                        IOSField(L("套餐周期（天）"),r.cycleDays.toString(),{v-> val d=v.filter{it.isDigit()}.toIntOrNull()?:0; r=r.copy(cycleDays=d)},L(""))
                        IOSDividerLine()
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){
                            listOf(7,15,30).forEach{ d-> IOSChip(cycleText(editLang,d),r.cycleDays==d,Modifier.weight(1f)){ r=r.copy(cycleDays=d,expireDate=runCatching{LocalDate.parse(r.startDate).plusDays(d.toLong()).toString()}.getOrElse{LocalDate.now().plusDays(d.toLong()).toString()}) } }
                        }
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){
                            listOf(70,90,180,365).forEach{ d-> IOSChip(cycleText(editLang,d),r.cycleDays==d,Modifier.weight(1f)){ r=r.copy(cycleDays=d,expireDate=runCatching{LocalDate.parse(r.startDate).plusDays(d.toLong()).toString()}.getOrElse{LocalDate.now().plusDays(d.toLong()).toString()}) } }
                        }
                        IOSDividerLine()
                        IOSField(L("每周期付款"),if(r.cyclePaymentMinorUnits>0) (r.cyclePaymentMinorUnits/100).toString() else "",{v-> val amt=v.filter{it.isDigit()}.toIntOrNull()?:0; r=r.copy(cyclePaymentMinorUnits=amt*100)},L("6"))
                        IOSDividerLine()
                        IOSField(L("货币代码"),r.currencyCode,{r=r.copy(currencyCode=it.uppercase())},L("HKD / CNY / USD"))
                    }
                }

                // ── 电话号码 ──
                item{
                    SettingsSection(L("电话号码")){
                        IOSField(L("手机号码"),r.number,{r=r.copy(number=it.filter{c->c.isDigit()})},L("输入手机号码"))
                        if(r.number.isNotBlank()){
                            IOSDividerLine()
                            Text("${r.countryCode} · ${r.countryName}",fontSize=13.sp,color=Color(0xFF8A94A6))
                        }
                    }
                }

                // ── 国家/地区 ──
                item{
                    SettingsSection(L("国家/地区")){
                        IOSValueRow(L("国家/地区"),"${r.flag} ${r.countryName} ${r.countryCode}"){ countryDlg=true }
                        Text(L("支持搜索名称、代码或区号"),fontSize=11.sp,color=Color(0xFF8A94A6))
                    }
                }

                // ── 卡片背景 ──
                item{
                    SettingsSection("卡片背景"){
                        com.sansim.app.ui.CardBackgroundPicker(r.cardBackgroundAssetName){ r=r.copy(cardBackgroundAssetName=it) }
                    }
                }

                // ── 官网链接 ──
                item{
                    SettingsSection(L("官网链接")){
                        IOSField(L("官网链接"),r.websiteURL,{r=r.copy(websiteURL=it)},L("例如：https://www.example.com"))
                    }
                }

                // ── 当前余额 ──
                item{
                    SettingsSection(L("当前余额")){
                        IOSField(L("余额"),r.balance,{r=r.copy(balance=it)},L("如 30.00 HKD / 100 CNY"))
                    }
                }

                // ── 流水记录 ──
                item{
                    SettingsSection(L("流水记录")){
                        IOSField(L("流水记录"),r.transactionNotes,{r=r.copy(transactionNotes=it)},L("在这里写充值、扣费或消费记录"),singleLine=false,minLines=3)
                    }
                }

                // ── 自定义提示词 ──
                item{
                    SettingsSection(L("自定义提示词")){
                        IOSField(L("自定义提示词"),r.customPrompt,{r=r.copy(customPrompt=it)},L("在这里写给自己的提醒或备注"),singleLine=false,minLines=3)
                    }
                }

                // ── 标签 ──
                item{
                    SettingsSection(L("标签")){
                        com.sansim.app.ui.TagSelector(r.tags){ r=r.copy(tags=it) }
                    }
                }

                // ── eSIM 激活信息 ──
                item{
                    SettingsSection(L("eSIM 激活信息")){
                        IOSField("SM-DP+",r.smdp,{r=r.copy(smdp=it)},L("服务器地址"))
                        IOSDividerLine()
                        IOSField(L("激活码"),r.activationCode,{r=r.copy(activationCode=it)},"Activation Code")
                        IOSDividerLine()
                        IOSField(L("确认码（选填）"),"",{},L("Confirmation Code"))
                        IOSDividerLine()
                        Text(L("扫描二维码"),fontSize=14.sp,color=Color(0xFF007AFF),modifier=Modifier.motionClickable{qrDlg=true})
                        Text(L("从相册读取二维码"),fontSize=14.sp,color=Color(0xFF007AFF),modifier=Modifier.motionClickable{albumLauncher.launch("image/*")})
                        if(qrText.isNotBlank()){
                            IOSDividerLine()
                            Text("✅ "+L("激活信息已填写"),fontSize=13.sp,color=Color(0xFF34C759))
                        }
                    }
                }

                // ── EID 信息 ──
                item{
                    SettingsSection(L("EID 信息（选填）")){
                        IOSField(L("EID"),r.eid,{r=r.copy(eid=it)},L("输入 32 位 EID（可选）"))
                    }
                }

                // ── 到期时间 ──
                item{
                    SettingsSection(L("到期时间")){
                        Text(L("套餐开始日期"),fontSize=12.sp,color=Color(0xFF8A94A6)); DateOnlyEditor(r.startDate.ifBlank{LocalDate.now().toString()}){r=r.copy(startDate=it)}
                        IOSDividerLine()
                        Text(L("套餐时长（从开始日期计算）"),fontSize=12.sp,color=Color(0xFF8A94A6))
                        IOSDividerLine()
                        IOSSwitchRow(L("长期保号"),r.longTerm){r=r.copy(longTerm=it)}
                        if(!r.longTerm){
                            IOSDividerLine()
                            Text(L("精确到期日期"),fontSize=12.sp,color=Color(0xFF8A94A6)); DateOnlyEditor(r.expireDate){r=r.copy(expireDate=it)}
                            if(r.startDate.isNotBlank()){
                                Text("${L("开始")}: ${r.startDate} → ${L("到期")}: ${r.expireDate}",fontSize=11.sp,color=Color(0xFF8A94A6))
                            }
                        }
                    }
                }

                // ── 记录信息 ──
                item{
                    SettingsSection(L("记录信息")){
                        IOSInfoRow(L("创建时间"),r.createdAt.ifBlank{LocalDate.now().toString()})
                        IOSDividerLine()
                        IOSInfoRow(L("激活时间"),r.activatedAt.ifBlank{L("未记录")})
                    }
                }

                // ── 删除按钮 ──
                item{ Spacer(Modifier.height(14.dp)) }
                item{
                    Button(onClick={showDel=true},modifier=Modifier.fillMaxWidth().height(50.dp),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFF3B30)),contentPadding=PaddingValues(horizontal=16.dp)){
                        Text(L("删除"),fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=Color.White)
                    }
                    if(showDel){
                        Dialog(onDismissRequest={showDel=false}){
                            Surface(shape=RoundedCornerShape(20.dp),color=Color.White){
                                Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp),horizontalAlignment=Alignment.CenterHorizontally){
                                    Text(L("确认删除"),fontSize=20.sp,fontWeight=FontWeight.Bold,color=Color(0xFF111827))
                                    Text(L("删除后无法恢复，确定要删除这个号码吗？"),fontSize=14.sp,color=Color(0xFF6B7280))
                                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)){
                                        Button(onClick={showDel=false},modifier=Modifier.weight(1f).height(48.dp),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFF2F3F7))){Text(L("取消"),color=Color(0xFF007AFF),fontSize=16.sp)}
                                        Button(onClick={onDelete(r)},modifier=Modifier.weight(1f).height(48.dp),shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFF3B30))){Text(L("删除"),color=Color.White,fontSize=16.sp,fontWeight=FontWeight.SemiBold)}
                                    }
                                }
                            }
                        }
                    }
                }
                item{ Spacer(Modifier.height(80.dp)) }
            }
        }
    }
    if(qrDlg) IOSQrInputDialog(qrInput,{qrInput=it},{qrDlg=false}){
        qrText=qrInput.ifBlank{tr(editLang,"已手动触发扫码入口")}
        if(qrInput.isNotBlank()){
            val parts=parseLpa(qrInput)
            r=r.copy(smdp=parts.first.ifBlank{r.smdp},activationCode=parts.second.ifBlank{r.activationCode})
        }
        qrDlg=false
    }
    if(countryDlg) CountryDialog({countryDlg=false}){c->r=r.copy(countryCode=c.code,countryName=c.name,flag=c.flag);countryDlg=false}
}
@Composable fun IOSDividerLine(){ Box(Modifier.fillMaxWidth().height(.7.dp).background(dk(Color(0xFF2C2C2E),Color(0xFFE5E7EB)))) }

@Composable fun IOSInfoRow(title:String,value:String){
    Row(Modifier.fillMaxWidth().padding(vertical=2.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
        Text(title,fontSize=15.sp,color=dk(Color(0xFFE5E5E7),Color(0xFF111827))); Text(value,fontSize=14.sp,color=Color(0xFF8A94A6),maxLines=1,overflow=TextOverflow.Ellipsis)
    }
}

@Composable fun IOSValueRow(title:String,value:String,onClick:()->Unit){
    ModernIOSValueRow(title,value,onClick)
    return
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).motionClickable(pressedScale=.985f){onClick()}.padding(vertical=4.dp),verticalAlignment=Alignment.CenterVertically){
        Text(title,fontSize=15.sp,color=dk(Color(0xFFE5E5E7),Color(0xFF111827)),modifier=Modifier.width(92.dp)); Text(value,fontSize=15.sp,color=dk(Color(0xFFD1D5DB),Color(0xFF374151)),modifier=Modifier.weight(1f),maxLines=1,overflow=TextOverflow.Ellipsis); Text("›",fontSize=24.sp,color=dk(Color(0xFF48484A),Color(0xFFC7C7CC)))
    }
}

@Composable fun IOSField(label:String,value:String,onValue:(String)->Unit,placeholder:String,singleLine:Boolean=true,minLines:Int=1){
    ModernIOSField(label,value,onValue,placeholder,singleLine,minLines)
    return
    Column(verticalArrangement=Arrangement.spacedBy(5.dp)){
        Text(label,fontSize=13.sp,color=Color(0xFF8A94A6),modifier=Modifier.padding(start=2.dp))
        TextField(value=value,onValueChange=onValue,modifier=Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)),singleLine=singleLine,minLines=minLines,placeholder={Text(placeholder,fontSize=13.sp,color=dk(Color(0xFF636366),Color(0xFFB0B7C3)),maxLines=1,overflow=TextOverflow.Ellipsis)},colors=TextFieldDefaults.colors(focusedContainerColor=dk(Color(0xFF1C1C1E),Color(0xFFF7F8FA)),unfocusedContainerColor=dk(Color(0xFF1C1C1E),Color(0xFFF7F8FA)),focusedIndicatorColor=Color.Transparent,unfocusedIndicatorColor=Color.Transparent),textStyle=androidx.compose.ui.text.TextStyle(fontSize=15.sp,color=dk(Color(0xFFE5E5E7),Color(0xFF111827))))
    }
}

@Composable fun IOSQrInputDialog(value:String,onValue:(String)->Unit,onDismiss:()->Unit,onSave:()->Unit){
    Dialog(onDismissRequest=onDismiss){
        Surface(shape=RoundedCornerShape(26.dp),color=dk(Color(0xFF1C1C1E),Color(0xFFF2F3F7)),modifier=Modifier.fillMaxWidth()){
            Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp),horizontalAlignment=Alignment.CenterHorizontally){
                Text(L("填写二维码内容"),fontSize=19.sp,fontWeight=FontWeight.Bold,color=dk(Color(0xFFE5E5E7),Color(0xFF111827)))
                Text(L("可粘贴 LPA、SM-DP+ 或激活码，保存后自动解析。"),fontSize=13.sp,color=Color(0xFF8A94A6),lineHeight=18.sp)
                TextField(value=value,onValueChange=onValue,modifier=Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(16.dp)),placeholder={Text(L("LPA:1\$SM-DP+\$激活码"))},minLines=5,colors=TextFieldDefaults.colors(focusedContainerColor=dk(Color(0xFF2C2C2E),Color.White),unfocusedContainerColor=dk(Color(0xFF2C2C2E),Color.White),focusedIndicatorColor=Color.Transparent,unfocusedIndicatorColor=Color.Transparent))
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
                    Button(onClick=onDismiss,modifier=Modifier.weight(1f).height(46.dp),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=dk(Color(0xFF2C2C2E),Color.White),contentColor=dk(Color(0xFFD1D5DB),Color(0xFF374151)))){Text(L("取消"))}
                    Button(onClick=onSave,modifier=Modifier.weight(1f).height(46.dp),shape=RoundedCornerShape(15.dp),colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF007AFF),contentColor=Color.White)){Text(L("保存"))}
                }
            }
        }
    }
}

@Composable fun CompactDateEditor(value:String,onChange:(String)->Unit){
    val parsed=runCatching{LocalDate.parse(value)}.getOrElse{LocalDate.now().plusDays(30)}
    var y by remember(value){ mutableStateOf(parsed.year.toString()) }
    var m by remember(value){ mutableStateOf(parsed.monthValue.toString().padStart(2,'0')) }
    var d by remember(value){ mutableStateOf(parsed.dayOfMonth.toString().padStart(2,'0')) }
    fun commit(){
        if(y.isBlank() || m.isBlank() || d.isBlank()) return
        val yy=(y.toIntOrNull() ?: parsed.year).coerceIn(1970,9999)
        val mm=(m.toIntOrNull() ?: parsed.monthValue).coerceIn(1,12)
        val maxDay=java.time.YearMonth.of(yy,mm).lengthOfMonth()
        val dd=(d.toIntOrNull() ?: parsed.dayOfMonth).coerceIn(1,maxDay)
        onChange(runCatching{LocalDate.of(yy,mm,dd)}.getOrElse{LocalDate.now().plusDays(30)}.toString())
    }
    Column(verticalArrangement=Arrangement.spacedBy(9.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            DateBox(L("年"),y,{ v-> y=v.filter{c->c.isDigit()}.takeLast(4); commit() },Modifier.weight(1.25f))
            DateBox(L("月"),m,{ v-> m=v.filter{c->c.isDigit()}.takeLast(2); commit() },Modifier.weight(.85f))
            DateBox(L("日"),d,{ v-> d=v.filter{c->c.isDigit()}.takeLast(2); commit() },Modifier.weight(.85f))
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){
            listOf(0,7,30,90).forEach{ n-> DateQuick(laterText(LocalAppLanguage.current,n),Modifier.weight(1f)){onChange(LocalDate.now().plusDays(n.toLong()).toString())} }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){
            listOf(7,15,30,90,180,365).forEach{ n-> DateQuick(cycleText(LocalAppLanguage.current,n),Modifier.weight(1f)){onChange(LocalDate.now().plusDays(n.toLong()).toString())} }
        }
    }
}

@Composable fun DateOnlyEditor(value:String,onChange:(String)->Unit){
    val parsed=runCatching{LocalDate.parse(value)}.getOrElse{LocalDate.now()}
    var y by remember{ mutableStateOf(parsed.year.toString()) }
    var m by remember{ mutableStateOf(parsed.monthValue.toString()) }
    var d by remember{ mutableStateOf(parsed.dayOfMonth.toString()) }
    // 仅当外部（如周期按钮）改变日期且与当前输入不一致时才同步，输入过程中不回填
    LaunchedEffect(value){
        val cur=runCatching{ LocalDate.of(y.toIntOrNull()?:0, m.toIntOrNull()?:0, d.toIntOrNull()?:0).toString() }.getOrNull()
        if(cur!=value){
            y=parsed.year.toString(); m=parsed.monthValue.toString(); d=parsed.dayOfMonth.toString()
        }
    }
    fun commit(){
        val yy=y.toIntOrNull() ?: return
        val mm=m.toIntOrNull() ?: return
        val dd=d.toIntOrNull() ?: return
        if(mm !in 1..12) return
        val maxDay=runCatching{ java.time.YearMonth.of(yy,mm).lengthOfMonth() }.getOrElse{31}
        if(dd !in 1..maxDay) return
        onChange(runCatching{LocalDate.of(yy,mm,dd)}.getOrNull()?.toString() ?: return)
    }
    Column(verticalArrangement=Arrangement.spacedBy(9.dp)){
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
            DateBox(L("年"),y,{ v-> y=v.filter{c->c.isDigit()}.take(4); commit() },Modifier.weight(1.25f))
            DateBox(L("月"),m,{ v-> m=v.filter{c->c.isDigit()}.take(2); commit() },Modifier.weight(.85f))
            DateBox(L("日"),d,{ v-> d=v.filter{c->c.isDigit()}.take(2); commit() },Modifier.weight(.85f))
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){
            listOf(0,7,30,90).forEach{ n-> DateQuick(laterText(LocalAppLanguage.current,n),Modifier.weight(1f)){ val nd=LocalDate.now().plusDays(n.toLong()); y=nd.year.toString(); m=nd.monthValue.toString(); d=nd.dayOfMonth.toString(); onChange(nd.toString()) } }
        }
    }
}

@Composable fun DateBox(label:String,value:String,onValue:(String)->Unit,m:Modifier){
    val scheme=MaterialTheme.colorScheme
    val shape=RoundedCornerShape(16.dp)
    Column(m,verticalArrangement=Arrangement.spacedBy(8.dp)){
        Text(label,fontSize=13.sp,lineHeight=16.sp,fontWeight=FontWeight.Bold,color=scheme.onSurfaceVariant,modifier=Modifier.padding(start=2.dp))
        TextField(
            value=value,
            onValueChange=onValue,
            singleLine=true,
            modifier=Modifier
                .fillMaxWidth()
                .height(58.dp)
                .border(1.dp,scheme.outlineVariant.copy(alpha=.54f),shape)
                .clip(shape),
            colors=TextFieldDefaults.colors(
                focusedContainerColor=scheme.surface,
                unfocusedContainerColor=scheme.surface,
                focusedIndicatorColor=Color.Transparent,
                unfocusedIndicatorColor=Color.Transparent,
                cursorColor=scheme.primary,
                focusedTextColor=scheme.onSurface,
                unfocusedTextColor=scheme.onSurface
            ),
            textStyle=TextStyle(fontSize=16.sp,lineHeight=22.sp,fontWeight=FontWeight.Medium,color=scheme.onSurface)
        )
    }
    return
    Column(m,verticalArrangement=Arrangement.spacedBy(4.dp)){
        Text(label,fontSize=11.sp,color=Color(0xFF8A94A6))
        OutlinedTextField(value=value,onValueChange=onValue,singleLine=true,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp),shape=RoundedCornerShape(12.dp),textStyle=androidx.compose.ui.text.TextStyle(fontSize=14.sp),colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=dk(Color(0xFF38383A),Color(0xFFD1D5DB)),unfocusedBorderColor=dk(Color(0xFF38383A),Color(0xFFD1D5DB)),focusedContainerColor=dk(Color(0xFF1C1C1E),Color.White),unfocusedContainerColor=dk(Color(0xFF1C1C1E),Color.White)))
    }
}

@Composable fun DateQuick(text:String,m:Modifier,onClick:()->Unit){
    val scheme=MaterialTheme.colorScheme
    val shape=RoundedCornerShape(16.dp)
    Box(
        m
            .height(40.dp)
            .clip(shape)
            .background(scheme.surfaceContainerHighest)
            .border(1.dp,scheme.outlineVariant.copy(alpha=.62f),shape)
            .motionClickable{onClick()},
        contentAlignment=Alignment.Center
    ){
        Text(text,fontSize=13.sp,lineHeight=16.sp,fontWeight=FontWeight.Bold,color=scheme.primary,maxLines=1,overflow=TextOverflow.Ellipsis)
    }
    return
    Box(m.height(34.dp).clip(RoundedCornerShape(11.dp)).background(dk(Color(0xFF1C2333),Color(0xFFF4F5F8))).motionClickable{onClick()},contentAlignment=Alignment.Center){Text(text,fontSize=12.sp,color=Color(0xFF007AFF),maxLines=1)}
}

@Composable fun FormSection(title:String, content:@Composable ColumnScope.()->Unit){
    Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
        Text(title,fontSize=13.sp,color=Color(0xFF8A94A6),modifier=Modifier.padding(start=4.dp))
        Card(shape=RoundedCornerShape(18.dp),colors=CardDefaults.cardColors(containerColor=dk(Color(0xFF1C1C1E),Color.White)),elevation=CardDefaults.cardElevation(0.dp),modifier=Modifier.fillMaxWidth()){
            Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){ content() }
        }
    }
}

@Composable fun 编辑Dialog(init:PhoneNumberRecord,onDismiss:()->Unit,onSave:(PhoneNumberRecord)->Unit){ Full编辑Screen(init,onDismiss,onSave) }
@Composable fun DateFields(value:String,on:(String)->Unit){ var y by remember(value){mutableStateOf(value.split("-").getOrNull(0)?:LocalDate.now().year.toString())}; var m by remember(value){mutableStateOf(value.split("-").getOrNull(1)?:"01")}; var d by remember(value){mutableStateOf(value.split("-").getOrNull(2)?:"01")}; fun emit(){ val mm=m.padStart(2,'0'); val dd=d.padStart(2,'0'); on("$y-$mm-$dd")}; Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){OutlinedTextField(y,{y=it.take(4).filter(Char::isDigit);emit()},Modifier,label={Text("年")});OutlinedTextField(m,{m=it.take(2).filter(Char::isDigit);emit()},Modifier,label={Text("月")});OutlinedTextField(d,{d=it.take(2).filter(Char::isDigit);emit()},Modifier,label={Text("日")})}; Row{ listOf("今天" to 0,"7天后" to 7,"30天后" to 30,"90天后" to 90).forEach{TextButton({on(LocalDate.now().plusDays(it.second.toLong()).toString())}){Text(it.first)}} } }
@Composable fun CountryDialog(onDismiss:()->Unit,onPick:(Country)->Unit){
    var q by remember{mutableStateOf("")}
    Dialog(onDismissRequest=onDismiss){
        Surface(shape=RoundedCornerShape(26.dp),color=dk(Color(0xFF1C1C1E),Color(0xFFF2F3F7)),modifier=Modifier.fillMaxWidth()){
            Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
                Row(verticalAlignment=Alignment.CenterVertically){
                    Text(L("选择国家区号"),fontSize=20.sp,fontWeight=FontWeight.Bold,color=dk(Color(0xFFE5E5E7),Color(0xFF111827)),modifier=Modifier.weight(1f))
                    TextButton(onDismiss){Text(L("取消"),color=Color(0xFF007AFF))}
                }
                TextField(value=q,onValueChange={q=it},modifier=Modifier.fillMaxWidth().heightIn(min=36.dp).clip(RoundedCornerShape(12.dp)),singleLine=true,placeholder={Text(L("搜索国家 / 区号 / ISO"))},leadingIcon={Canvas(Modifier.size(16.dp)){drawCircle(Color(0xFF8E8E93),radius=size.width/2-1.dp.toPx(),style=Stroke(1.5.dp.toPx()));drawLine(Color(0xFF8E8E93),Offset(size.width*.65f,size.height*.65f),Offset(size.width*.85f,size.height*.85f),strokeWidth=1.5.dp.toPx())}},colors=TextFieldDefaults.colors(focusedContainerColor=dk(Color(0xFF2C2C2E),Color.White),unfocusedContainerColor=dk(Color(0xFF2C2C2E),Color.White),focusedIndicatorColor=Color.Transparent,unfocusedIndicatorColor=Color.Transparent))
                LazyColumn(Modifier.heightIn(max=460.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
                    items(Countries.list.filter{it.name.contains(q,true)||it.code.contains(q)||it.iso.contains(q,true)}){ c->
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(dk(Color(0xFF1C1C1E),Color.White)).motionClickable(pressedScale=.985f){onPick(c)}.padding(horizontal=13.dp,vertical=12.dp),verticalAlignment=Alignment.CenterVertically){Text(c.flag,fontSize=24.sp);Spacer(Modifier.width(10.dp));Text(c.name,fontSize=16.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f));Text("${c.code}  ${c.iso}",fontSize=13.sp,color=Color(0xFF8A94A6));Spacer(Modifier.width(4.dp));Text("›",fontSize=22.sp,color=Color(0xFFC7C7CC))}
                    }
                }
            }
        }
    }
}

@Composable fun CountryPage(){
    LazyColumn(Modifier.fillMaxSize().background(dk(Color(0xFF0B0F17),Color(0xFFF2F3F7))).padding(horizontal=18.dp,vertical=14.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
        item{ Text(L("国家区号库"),fontSize=28.sp,fontWeight=FontWeight.Bold,color=dk(Color(0xFFE5E5E7),Color(0xFF111827)),modifier=Modifier.padding(horizontal=4.dp,vertical=4.dp)) }
        item{
            IOSSection(L("全部国家 / 地区")){
                Countries.list.forEach{ c->
                    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).padding(horizontal=4.dp,vertical=8.dp),verticalAlignment=Alignment.CenterVertically){Text(c.flag,fontSize=24.sp);Spacer(Modifier.width(10.dp));Text(c.name,fontSize=16.sp,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f));Text("${c.code}  ${c.iso}",fontSize=13.sp,color=Color(0xFF8A94A6))}
                }
            }
        }
        item{Spacer(Modifier.height(80.dp))}
    }
}

@OptIn(ExperimentalLayoutApi::class)

fun recordToJson(r:PhoneNumberRecord)=JSONObject()
    .put("id",r.id).put("countryCode",r.countryCode).put("countryName",r.countryName).put("flag",r.flag)
    .put("number",r.number).put("operator",r.operator).put("expireDate",r.expireDate).put("note",r.note)
    .put("balance",r.balance).put("eid",r.eid).put("smdp",r.smdp).put("activationCode",r.activationCode)
    .put("startDate",r.startDate).put("createdAt",r.createdAt).put("activatedAt",r.activatedAt)
    .put("longTerm",r.longTerm).put("cycleDays",r.cycleDays).put("signalStatus",r.signalStatus)
    .put("tags",r.tags).put("transactionNotes",r.transactionNotes).put("customPrompt",r.customPrompt)
    .put("websiteURL",r.websiteURL).put("cyclePaymentMinorUnits",r.cyclePaymentMinorUnits)
    .put("currencyCode",r.currencyCode).put("cardBackgroundAssetName",r.cardBackgroundAssetName)
    .put("cardColorHex",r.cardColorHex).put("cardType",r.cardType).put("sortOrder",r.sortOrder)

/**
 * Clean pasted secrets. Do NOT take a short suffix match — that corrupts full keys.
 * Vault crypto uses password-derived secrets only; private keys are only for password reset.
 */
fun cleanCloudApiKey(raw:String):String {
    val t=raw.trim().replace(Regex("[\r\n\t ]+"),"")
    if(t.isBlank()) return ""
    // whole string is already a key / secret
    if(Regex("^[A-Za-z0-9_-]{24,128}$").matches(t)) return t
    // pasted with junk around it — take the longest token
    return Regex("[A-Za-z0-9_-]{24,128}").findAll(t).map{ it.value }.maxByOrNull{ it.length } ?: t
}
fun isValidCloudApiKey(key:String):Boolean = Regex("^[A-Za-z0-9_-]{24,128}$").matches(cleanCloudApiKey(key))
fun cleanCloudUrl(raw:String):String = raw.trim().trimEnd('/')
fun cleanBundledCloudUrl(raw:String):String {
    val url=cleanCloudUrl(raw)
    if(url.isBlank()) return DEFAULT_SIMJ_CLOUD_URL
    return url
}
fun isPublicCloudPath(path:String):Boolean =
    path.startsWith("/api/status") ||
    path.startsWith("/api/public-settings") ||
    path.startsWith("/api/account/register") ||
    path.startsWith("/api/account/login") ||
    path.startsWith("/api/account/reset-password")

fun cloudPayload(records:List<PhoneNumberRecord>,s:App设置):String{
    val settings=settingsToJson(s)
        .put("remindDays",s.remind天)
        .put("cloudApiKey","")
        .put("cloudToken","")
        .put("cloudDeviceId","")
    val arr=JSONArray(); records.forEach{arr.put(recordToJson(it))}
    return JSONObject().put("settings",settings).put("records",arr).toString()
}

private const val SIMJ_CLOUD_E2EE_ITERATIONS = 310000
private val SIMJ_CLOUD_AAD = "simj:e2ee:v1".toByteArray(Charsets.UTF_8)

fun b64u(bytes:ByteArray):String = Base64.encodeToString(bytes,Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
fun b64ud(text:String):ByteArray = Base64.decode(text,Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
fun simjCloudSalt(username:String):ByteArray = MessageDigest.getInstance("SHA-256").digest("simj:e2ee:v1:${username.trim().lowercase()}".toByteArray(Charsets.UTF_8)).copyOf(16)
fun deriveSimjCloudSecret(username:String,password:String):String{
    val spec=PBEKeySpec(password.toCharArray(),simjCloudSalt(username),SIMJ_CLOUD_E2EE_ITERATIONS,256)
    val raw=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    return b64u(raw)
}
fun deriveSimjCloudSecretWithSalt(password:String,saltBytes:ByteArray,iterations:Int=SIMJ_CLOUD_E2EE_ITERATIONS):String{
    val spec=PBEKeySpec(password.toCharArray(),saltBytes,iterations.coerceAtLeast(10000),256)
    val raw=SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    return b64u(raw)
}
/**
 * All password-based vault key candidates for this account.
 * Private key is NEVER used for vault crypto — only for server-side password reset.
 */
fun passwordVaultSecrets(username:String,password:String,envelope:JSONObject?=null):List<String>{
    if(password.isBlank()) return emptyList()
    val user=username.trim()
    val out=LinkedHashSet<String>()
    // 1) current: PBKDF2(password, salt=SHA256("simj:e2ee:v1:"+user)[:16], 310000)
    runCatching{ out.add(deriveSimjCloudSecret(user,password)) }
    runCatching{ out.add(deriveSimjCloudSecret(user.lowercase(),password)) }
    // 2) salt from envelope (if an older build wrote a different salt)
    val saltB64=envelope?.optString("salt","") ?: ""
    val iter=envelope?.optInt("iter",SIMJ_CLOUD_E2EE_ITERATIONS)?.takeIf{ it>0 } ?: SIMJ_CLOUD_E2EE_ITERATIONS
    if(saltB64.isNotBlank()){
        runCatching{
            out.add(deriveSimjCloudSecretWithSalt(password,b64ud(saltB64),iter))
        }
    }
    // 3) very old mistaken schemes (password hash as key material)
    runCatching{
        out.add(b64u(MessageDigest.getInstance("SHA-256").digest("${user.lowercase()}:$password".toByteArray(Charsets.UTF_8))))
    }
    runCatching{
        out.add(b64u(MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))))
    }
    return out.filter{ it.isNotBlank() }
}
/**
 * Data vault is encrypted ONLY with a key derived from account password.
 * Login with username+password must decrypt and auto-restore.
 * privateKey is ONLY for password-reset identity on server — never vault crypto.
 */
fun cloudEncryptPayload(plain:String,s:App设置):JSONObject{
    val secret=cleanCloudApiKey(s.cloudApiKey)
    if(secret.isBlank()) throw IllegalStateException("password-derived vault key missing — login with password first")
    val keyBytes=simjAesKeyBytes(secret)
    val nonce=ByteArray(12).also{ SecureRandom().nextBytes(it) }
    val cipher=Cipher.getInstance("AES/GCM/NoPadding")
    cipher.init(Cipher.ENCRYPT_MODE,SecretKeySpec(keyBytes,"AES"),GCMParameterSpec(128,nonce))
    cipher.updateAAD(SIMJ_CLOUD_AAD)
    val sealed=cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
    val tagSize=16
    val cipherText=sealed.copyOfRange(0,sealed.size-tagSize)
    val tag=sealed.copyOfRange(sealed.size-tagSize,sealed.size)
    return JSONObject()
        .put("v",2)
        .put("mode","app-e2ee-pwd") // password-derived AES key ONLY
        .put("alg","AES-256-GCM")
        .put("kdf","PBKDF2-HMAC-SHA256")
        .put("iter",SIMJ_CLOUD_E2EE_ITERATIONS)
        .put("salt",b64u(simjCloudSalt(s.cloudUsername)))
        .put("nonce",b64u(nonce))
        .put("ciphertext",b64u(cipherText))
        .put("tag",b64u(tag))
        .put("updatedAt",System.currentTimeMillis()/1000L)
}
/** Normalize secret into a 16/24/32-byte AES key. Prefer raw base64 decode of 32-byte material. */
fun simjAesKeyBytes(secret:String):ByteArray{
    val cleaned=cleanCloudApiKey(secret)
    if(cleaned.isBlank()) throw IllegalStateException("cloud secret missing")
    val raw=runCatching{ b64ud(cleaned) }.getOrElse{ cleaned.toByteArray(Charsets.UTF_8) }
    return when{
        raw.size==16 || raw.size==24 || raw.size==32 -> raw
        raw.size>32 -> raw.copyOf(32)
        raw.isEmpty() -> throw IllegalStateException("cloud secret empty after decode")
        else -> MessageDigest.getInstance("SHA-256").digest(raw)
    }
}
private fun cloudDecryptWithSecret(envelope:JSONObject,secret:String):String{
    val keyBytes=simjAesKeyBytes(secret)
    val nonce=b64ud(envelope.optString("nonce",""))
    val cipherTextText=envelope.optString("ciphertext",envelope.optString("cipherText",""))
    val cipherBytes=b64ud(cipherTextText)
    val tagText=envelope.optString("tag","")
    val sealed=if(tagText.isNotBlank()) cipherBytes + b64ud(tagText) else cipherBytes
    // with AAD (current)
    try{
        val cipher=Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE,SecretKeySpec(keyBytes,"AES"),GCMParameterSpec(128,nonce))
        cipher.updateAAD(SIMJ_CLOUD_AAD)
        return String(cipher.doFinal(sealed),Charsets.UTF_8)
    }catch(_:Exception){ }
    // without AAD (legacy)
    val c2=Cipher.getInstance("AES/GCM/NoPadding")
    c2.init(Cipher.DECRYPT_MODE,SecretKeySpec(keyBytes,"AES"),GCMParameterSpec(128,nonce))
    return String(c2.doFinal(sealed),Charsets.UTF_8)
}
fun cloudDecryptPayload(envelope:JSONObject,s:App设置,extraSecrets:List<String> = emptyList()):String{
    val candidates=(listOf(cleanCloudApiKey(s.cloudApiKey)) + extraSecrets.map{ cleanCloudApiKey(it) })
        .filter{ it.isNotBlank() }
        .distinct()
    if(candidates.isEmpty()) throw IllegalStateException("password-derived vault key missing")
    var last:Exception?=null
    for(secret in candidates){
        try{ return cloudDecryptWithSecret(envelope,secret) }catch(e:Exception){ last=e }
    }
    throw (last ?: IllegalStateException("cloud decrypt failed"))
}
/** Result of pulling /api/sync — distinguishes "no data" vs "has ciphertext but wrong key". */
data class CloudPullResult(
    val records:List<PhoneNumberRecord>,
    val settings:App设置?,
    val cloudRecordHint:Int,
    val hasEncryptedVault:Boolean,
    val decryptError:String?,
    val recoveredVia:String // vault | coverage | none
)
fun recordsFromLegacyPayload(obj:JSONObject):Pair<List<PhoneNumberRecord>,App设置?>{
    return runCatching{
        val payload=if(obj.has("payload")) obj.optJSONObject("payload") else null
        when{
            payload!=null -> parseRecordsJson(payload.toString())
            obj.has("records") && obj.opt("records") is JSONArray -> parseRecordsJson(obj.toString())
            else -> Pair(emptyList(),null)
        }
    }.getOrElse{ Pair(emptyList(),null) }
}
fun recordsFromCoverageJson(coverage:JSONObject?):List<PhoneNumberRecord>{
    if(coverage==null) return emptyList()
    val countries=coverage.optJSONArray("countries") ?: return emptyList()
    val out=ArrayList<PhoneNumberRecord>()
    for(i in 0 until countries.length()){
        val c=countries.optJSONObject(i) ?: continue
        val countryName=c.optString("name","")
        val samples=c.optJSONArray("samples") ?: continue
        for(j in 0 until samples.length()){
            val s=samples.optJSONObject(j) ?: continue
            val number=s.optString("number",s.optString("num","")).trim()
            if(number.isBlank()) continue // last4-only samples cannot restore full numbers
            val code=s.optString("code","")
            val op=s.optString("op",s.optString("operator",""))
            val id=s.optString("id","").ifBlank{ UUID.randomUUID().toString() }
            val isEsim=s.optBoolean("esim",false)
            out.add(
                PhoneNumberRecord(
                    id=id,
                    countryCode=code.ifBlank{"+"},
                    countryName=s.optString("name",countryName),
                    flag=s.optString("flag",""),
                    number=number,
                    operator=op,
                    expireDate=s.optString("expire",s.optString("expireDate","")),
                    note=s.optString("note",""),
                    balance=s.optString("balance",""),
                    cardType=s.optString("cardType",if(isEsim) "esim" else "prepaid"),
                    signalStatus=s.optString("signal","在线")
                )
            )
        }
    }
    return out
}
/**
 * Pull + decrypt vault. [password] if provided is used to derive vault keys (preferred).
 * Private key is never used here.
 */
fun analyzeCloudSyncResponse(
    text:String,
    s:App设置,
    extraSecrets:List<String> = emptyList(),
    password:String=""
):CloudPullResult{
    return try{
        val obj=JSONObject(text)
        val encrypted=obj.optJSONObject("encryptedVault") ?: obj.optJSONObject("envelope")
        val coverage=obj.optJSONObject("coverage")
        val hintFromRoot=obj.optInt("records",0)
        val hintFromCov=coverage?.optInt("records",0) ?: 0
        val hint=when{
            hintFromRoot>0 -> hintFromRoot
            hintFromCov>0 -> hintFromCov
            else -> 0
        }
        val hasVault=encrypted!=null && (
            encrypted.optString("mode").startsWith("app-e2ee") ||
            encrypted.has("tag") || encrypted.has("ciphertext") || encrypted.has("cipherText")
        )
        if(hasVault){
            val vault=encrypted ?: JSONObject()
            val allPwd=if(password.isNotBlank()) passwordVaultSecrets(s.cloudUsername,password,vault) else emptyList()
            val candidates=(allPwd + extraSecrets + listOf(s.cloudApiKey)).map{ cleanCloudApiKey(it) }.filter{ it.isNotBlank() }.distinct()
            try{
                val plain=cloudDecryptPayload(vault,s,candidates)
                val parsed=parseRecordsJson(plain)
                if(parsed.first.isNotEmpty()){
                    return CloudPullResult(parsed.first,parsed.second,hint.coerceAtLeast(parsed.first.size),true,null,"vault")
                }
                val fromCov=recordsFromCoverageJson(coverage)
                if(fromCov.isNotEmpty()){
                    return CloudPullResult(fromCov,parsed.second,hint.coerceAtLeast(fromCov.size),true,null,"coverage")
                }
                return CloudPullResult(emptyList(),parsed.second,hint,true,null,"none")
            }catch(e:Exception){
                val fromCov=recordsFromCoverageJson(coverage)
                if(fromCov.isNotEmpty()){
                    return CloudPullResult(fromCov,null,hint.coerceAtLeast(fromCov.size),true,e.message,"coverage")
                }
                val legacy=recordsFromLegacyPayload(obj)
                if(legacy.first.isNotEmpty()){
                    return CloudPullResult(legacy.first,legacy.second,hint.coerceAtLeast(legacy.first.size),true,e.message,"legacy")
                }
                return CloudPullResult(emptyList(),null,hint,true,e.message ?: e.javaClass.simpleName,"none")
            }
        }
        val payload=if(obj.has("payload")) obj.getJSONObject("payload") else obj
        val parsed=parseRecordsJson(payload.toString())
        if(parsed.first.isNotEmpty()){
            CloudPullResult(parsed.first,parsed.second,hint.coerceAtLeast(parsed.first.size),false,null,"vault")
        }else{
            val fromCov=recordsFromCoverageJson(coverage)
            CloudPullResult(fromCov,null,hint.coerceAtLeast(fromCov.size),false,null,if(fromCov.isNotEmpty()) "coverage" else "none")
        }
    }catch(e:Exception){
        CloudPullResult(emptyList(),null,0,false,e.message,"none")
    }
}
fun countryIsoForRecord(r:PhoneNumberRecord):String{
    countryIsoFor(r.countryCode,r.countryName).ifBlank { countryIsoFor(r.countryCode,r.flag) }.let {
        if(it.isNotBlank()) return it
    }
    val country=Countries.list.firstOrNull{ it.name==r.countryName || it.flag==r.flag || (it.code==r.countryCode && r.countryName.isBlank()) }
        ?: Countries.list.firstOrNull{ it.code==r.countryCode && it.name==r.countryName }
    return country?.iso ?: ""
}
fun cloudCountryName(iso:String,fallback:String):String = when(iso.uppercase()){
    "TW" -> "中国台湾省"
    "HK" -> "中国香港"
    "MO" -> "中国澳门"
    else -> fallback.ifBlank{iso.uppercase()}
}
fun isCloudEsimRecord(r:PhoneNumberRecord):Boolean{
    val text=listOf(r.cardType,r.eid,r.smdp,r.activationCode,r.note,r.tags).joinToString(" ").lowercase()
    return text.contains("esim") || r.eid.isNotBlank() || r.smdp.isNotBlank() || r.activationCode.isNotBlank()
}
fun cloudCoverage(records:List<PhoneNumberRecord>):JSONObject{
    val map=linkedMapOf<String,JSONObject>()
    records.forEach{ r->
        val iso=countryIsoForRecord(r).uppercase()
        if(iso.isBlank()) return@forEach
        val item=map.getOrPut(iso){
            JSONObject()
                .put("iso",iso)
                .put("name",cloudCountryName(iso,r.countryName))
                .put("records",0)
                .put("esims",0)
                .put("samples",JSONArray())
        }
        item.put("records",item.optInt("records",0)+1)
        val isEsim=isCloudEsimRecord(r)
        if(isEsim) item.put("esims",item.optInt("esims",0)+1)
        // Coverage is server-readable metadata only; full numbers stay inside encryptedVault.
        val samples=item.optJSONArray("samples") ?: JSONArray().also{ item.put("samples",it) }
        if(samples.length()<120){
            val digits=r.number.filter{ it.isDigit() }
            val last4=if(digits.length>=4) digits.takeLast(4) else digits.ifBlank{"????"}
            val mask=if(last4!="????") "**** $last4" else "****"
            val op=r.operator.ifBlank{ r.countryName }.take(40)
            samples.put(
                JSONObject()
                    .put("id",r.id)
                    .put("last4",last4)
                    .put("mask",mask)
                    .put("op",op)
                    .put("esim",isEsim)
                    .put("code",r.countryCode)
                    .put("name",r.countryName)
                    .put("flag",r.flag)
                    .put("expire",r.expireDate)
                    .put("balance",r.balance)
                    .put("cardType",r.cardType)
                    .put("signal",r.signalStatus)
                    .put("note",r.note.take(80))
            )
        }
    }
    val items=map.values.sortedWith(compareByDescending<JSONObject>{it.optInt("esims",0)}.thenByDescending{it.optInt("records",0)}.thenBy{it.optString("iso")})
    val arr=JSONArray(); items.forEach{ arr.put(it) }
    return JSONObject()
        .put("countries",arr)
        .put("countryCount",items.size)
        .put("records",items.sumOf{it.optInt("records",0)})
        .put("esims",items.sumOf{it.optInt("esims",0)})
        .put("updatedAt",System.currentTimeMillis()/1000L)
}
fun cloudEncryptedPayload(records:List<PhoneNumberRecord>,s:App设置):String{
    val coverage=cloudCoverage(records)
    return JSONObject()
        .put("encryptedVault",cloudEncryptPayload(cloudPayload(records,s),s))
        .put("coverage",coverage)
        .put("records",records.size)
        .put("deviceId",s.cloudDeviceId.ifBlank{"android"})
        .toString()
}
fun cloudRequest(s:App设置,path:String,method:String="POST",body:String="{}",lang:String="简体中文",onResult:(Boolean,String)->Unit){
    val token=s.cloudToken.trim()
    val cloudUrl=cleanBundledCloudUrl(s.cloudUrl)
    if(cloudUrl.isBlank()){onResult(false,tr(lang,"云端地址未填写"));return}
    val needsAuth=!isPublicCloudPath(path)
    if(needsAuth&&token.isBlank()){onResult(false,"登录已过期，请重新登录云同步账户");return}
    thread{
        val res=runCatching{
            val fullUrl=cloudUrl.trimEnd('/')+path
            val c=(URL(fullUrl).openConnection() as HttpURLConnection)
            c.instanceFollowRedirects=true
            c.requestMethod=method.uppercase(); c.connectTimeout=15000; c.readTimeout=25000
            // Avoid keep-alive reuse bugs with short-lived Python HTTP/1.x responses
            c.setRequestProperty("Connection","close")
            c.setRequestProperty("Accept","application/json")
            c.setRequestProperty("User-Agent","DsimJ-Android/3.0.24")
            if(needsAuth){
                c.setRequestProperty("Authorization","Bearer $token")
            }
            if(c.requestMethod=="POST"){
                c.doOutput=true
                c.setRequestProperty("Content-Type","application/json; charset=utf-8")
                val bytes=body.toByteArray(Charsets.UTF_8)
                // Prefer Content-Length over chunked; fixed streaming can confuse some servers
                c.setRequestProperty("Content-Length", bytes.size.toString())
                c.outputStream.use{ out ->
                    out.write(bytes)
                    out.flush()
                }
            }
            val respCode=try{ c.responseCode }catch(e:Exception){
                // Retry once on truncated stream (common with HTTP/1.0 close)
                throw e
            }
            fun readFully(stream:java.io.InputStream?):String{
                if(stream==null) return ""
                return stream.use{ ins ->
                    val bos=java.io.ByteArrayOutputStream()
                    val buf=ByteArray(8*1024)
                    while(true){
                        val n=try{ ins.read(buf) }catch(_:Exception){ -1 }
                        if(n<=0) break
                        bos.write(buf,0,n)
                    }
                    bos.toByteArray().toString(Charsets.UTF_8)
                }
            }
            val stream=if(respCode in 200..299) c.inputStream else (c.errorStream ?: c.inputStream)
            val respBody=readFully(stream)
            try{ c.disconnect() }catch(_:Exception){}
            if(respCode !in 200..299){
                // Prefer server JSON message when present
                val serverMsg=runCatching{ JSONObject(respBody).optString("message","") }.getOrDefault("")
                if(serverMsg.isNotBlank()) "HTTP $respCode: $serverMsg" else "HTTP $respCode: $respBody"
            }else respBody
        }.recoverCatching{ first ->
            // One automatic retry for ProtocolException / truncated response
            val m0=first.message?:""
            if(!m0.contains("end of stream", ignoreCase=true) &&
               !m0.contains("ProtocolException", ignoreCase=true) &&
               first.javaClass.simpleName!="ProtocolException") throw first
            Thread.sleep(350)
            val fullUrl=cloudUrl.trimEnd('/')+path
            val c=(URL(fullUrl).openConnection() as HttpURLConnection)
            c.instanceFollowRedirects=true
            c.requestMethod=method.uppercase(); c.connectTimeout=15000; c.readTimeout=25000
            c.setRequestProperty("Connection","close")
            c.setRequestProperty("Accept","application/json")
            c.setRequestProperty("User-Agent","DsimJ-Android/3.0.24")
            if(needsAuth){
                c.setRequestProperty("Authorization","Bearer $token")
            }
            if(c.requestMethod=="POST"){
                c.doOutput=true
                c.setRequestProperty("Content-Type","application/json; charset=utf-8")
                val bytes=body.toByteArray(Charsets.UTF_8)
                c.setRequestProperty("Content-Length", bytes.size.toString())
                c.outputStream.use{ out -> out.write(bytes); out.flush() }
            }
            val respCode=c.responseCode
            val stream=if(respCode in 200..299) c.inputStream else (c.errorStream ?: c.inputStream)
            val respBody=stream?.use{ it.readBytes().toString(Charsets.UTF_8) } ?: ""
            try{ c.disconnect() }catch(_:Exception){}
            if(respCode !in 200..299){
                val serverMsg=runCatching{ JSONObject(respBody).optString("message","") }.getOrDefault("")
                if(serverMsg.isNotBlank()) "HTTP $respCode: $serverMsg" else "HTTP $respCode: $respBody"
            }else respBody
        }.fold(
            {it},
            { e ->
                val m=e.message ?: ""
                when{
                    m.contains("Cleartext", ignoreCase=true) ->
                        "失败: 系统拦截了 HTTP 明文访问。建议使用 HTTPS，或确认 App 已允许自建服务的 HTTP 地址。"
                    m.contains("Unable to resolve host", ignoreCase=true) || m.contains("UnknownHost", ignoreCase=true) ->
                        "失败: 无法解析服务器域名/地址，请检查服务地址与网络"
                    m.contains("Connection refused", ignoreCase=true) || m.contains("ECONNREFUSED", ignoreCase=true) ->
                        "失败: 连接被拒绝，请确认云服务已启动（端口 8787）"
                    m.contains("timeout", ignoreCase=true) || m.contains("Timed out", ignoreCase=true) ->
                        "失败: 连接超时，请检查网络或服务地址 $cloudUrl"
                    m.contains("end of stream", ignoreCase=true) || e.javaClass.simpleName=="ProtocolException" ->
                        "失败: 与云端通信中断，请确认服务地址格式正确，例如 https://your-domain.example 或 http://<your-server-ip>:8787"
                    else -> "失败: ${e.javaClass.simpleName}: $m"
                }
            }
        )
        val ok=res.isNotBlank() && !res.startsWith("失败") && !res.startsWith("HTTP ")
        Handler(Looper.getMainLooper()).post{onResult(ok,res)}
    }
}
fun cloudPost(s:App设置,path:String,body:String,lang:String="简体中文",onResult:(Boolean,String)->Unit)=cloudRequest(s,path,"POST",body,lang,onResult)
fun cloudGet(s:App设置,path:String,lang:String="简体中文",onResult:(Boolean,String)->Unit)=cloudRequest(s,path,"GET","{}",lang,onResult)

fun summarizeCloudCheckResponse(text:String):String{
    return runCatching{
        val o=JSONObject(text)
        val direct=o.optString("message","")
        if(direct.isNotBlank()) return@runCatching direct
        val st=o.optJSONObject("stats") ?: o
        "已检查：${st.optInt("records",0)} 个号码，${st.optInt("due",0)} 个进入提醒区间，邮件 ${st.optInt("mail",0)} 条，TG ${st.optInt("tg",0)} 条，重复跳过 ${st.optInt("duplicate",0)} 条"
    }.getOrDefault(text.ifBlank{"已完成"})
}

fun formatTsShort(ts:Long):String = if(ts>0) java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ts*1000L)) else "-"

fun parseCloudPayloadResponse(text:String,s:App设置?=null,extraSecrets:List<String> = emptyList()):Pair<List<PhoneNumberRecord>,App设置?>{
    if(s==null){
        return runCatching{
            val obj=JSONObject(text)
            val payload=if(obj.has("payload")) obj.getJSONObject("payload") else obj
            parseRecordsJson(payload.toString())
        }.getOrElse{ Pair(emptyList(),null) }
    }
    val r=analyzeCloudSyncResponse(text,s,extraSecrets)
    return Pair(r.records,r.settings)
}

fun recordMergeKey(r:PhoneNumberRecord):String{
    val n=r.number.filter{it.isDigit()}
    val cc=r.countryCode.filter{it.isDigit()}
    return when{
        r.id.isNotBlank() -> "id:${r.id}"
        n.isNotBlank() -> "num:$cc:$n"
        else -> "tmp:${r.countryCode}:${r.operator}:${r.expireDate}"
    }
}
fun recordAltNumberKey(r:PhoneNumberRecord):String{
    val n=r.number.filter{it.isDigit()}
    val cc=r.countryCode.filter{it.isDigit()}
    return if(n.isNotBlank()) "num:$cc:$n" else ""
}
fun recordFreshScore(r:PhoneNumberRecord):String = listOf(r.activatedAt,r.createdAt,r.expireDate).filter{it.isNotBlank()}.maxOrNull() ?: ""
fun chooseFreshRecord(a:PhoneNumberRecord,b:PhoneNumberRecord):PhoneNumberRecord = if(recordFreshScore(b)>=recordFreshScore(a)) b else a
fun mergeRecords(cloud:List<PhoneNumberRecord>,local:List<PhoneNumberRecord>):List<PhoneNumberRecord>{
    val out=linkedMapOf<String,PhoneNumberRecord>()
    val numberIndex=mutableMapOf<String,String>()
    fun add(r:PhoneNumberRecord){
        val primary=recordMergeKey(r)
        val numKey=recordAltNumberKey(r)
        val existingKey=when{
            out.containsKey(primary) -> primary
            numKey.isNotBlank() && numberIndex.containsKey(numKey) -> numberIndex[numKey]
            else -> null
        }
        if(existingKey!=null){
            out[existingKey]=chooseFreshRecord(out[existingKey]!!,r)
        }else{
            out[primary]=r
            if(numKey.isNotBlank()) numberIndex[numKey]=primary
        }
    }
    cloud.forEach{add(it)}
    local.forEach{add(it)}
    return out.values.toList()
}
fun mergeCloudSettings(current:App设置,cloud:App设置?):App设置{
    if(cloud==null) return current
    val keepKey=cleanCloudApiKey(current.cloudApiKey).ifBlank{ cleanCloudApiKey(cloud.cloudApiKey) }
    val keepUrl=cleanBundledCloudUrl(current.cloudUrl).ifBlank{ cleanBundledCloudUrl(cloud.cloudUrl) }
    return cloud.copyMut{ cloudApiKey=keepKey; cloudUrl=keepUrl; cloudToken=current.cloudToken; cloudUsername=current.cloudUsername; cloudDeviceId=current.cloudDeviceId; cloudEnabled=true }
}

fun restoreCloudBackupById(st:App设置, backupId:Int, onResult:(Boolean,String)->Unit){
    cloudPost(st,"/api/restore-backup",JSONObject().put("backupId",backupId).toString()){ok,msg->onResult(ok,msg)}
}

@OptIn(ExperimentalLayoutApi::class)
@Composable fun 设置Page(ctx:Context,s:App设置,records:List<PhoneNumberRecord>,currentVersion:String="0.0.0",onUpdateCheck:(()->Unit)?=null,on:(App设置)->Unit,onTraffic:(PhoneNumberRecord)->Unit={},onDial:(PhoneNumberRecord)->Unit={},onExportJson:()->Unit={},onExportCsv:()->Unit={},onImportText:(String)->Unit={},onCloudRestore:(List<PhoneNumberRecord>,App设置?)->Unit={_,_->},onImportSimHub:(List<PhoneNumberRecord>)->Unit={_->}){
    var st by remember{s.mutableState()}
    var cloudMsg by remember{ mutableStateOf("") }
    var cloudSyncChoice by remember{ mutableStateOf<Pair<List<PhoneNumberRecord>,App设置?>?>(null) }
    var cloudRestoreChoice by remember{ mutableStateOf<Pair<List<PhoneNumberRecord>,App设置?>?>(null) }
    var cloudOverwriteConfirm by remember{ mutableStateOf(false) }
    var keyGenerateConfirm by remember{ mutableStateOf(false) }
    var registerUsername by remember{ mutableStateOf("") }
    var registerPassword by remember{ mutableStateOf("") }
    var registerPasswordAgain by remember{ mutableStateOf("") }
    var privateKeyInput by remember{ mutableStateOf("") }
    var showPrivateKeyOnce by remember{ mutableStateOf<String?>(null) }
    var resetPasswordMode by remember{ mutableStateOf(false) }
    var registerBusy by remember{ mutableStateOf(false) }

    var cloudOverviewKeyRecords by remember{ mutableStateOf(-1) }
    var cloudOverviewUpdatedAt by remember{ mutableStateOf(0L) }
    var cloudOverviewHasSettings by remember{ mutableStateOf(false) }
    var cloudOverviewKeyTail by remember{ mutableStateOf("") }
    var cloudReminderLastCheck by remember{ mutableStateOf(0L) }
    var cloudReminderNextCheck by remember{ mutableStateOf(0L) }
    var cloudReminderDueNow by remember{ mutableStateOf(-1) }
    var cloudReminderStats by remember{ mutableStateOf("") }
    var cloudBackups by remember{ mutableStateOf<List<JSONObject>>(emptyList()) }
    var cloudBackupsTotal by remember{ mutableStateOf(0) }
    var cloudBackupsLimit by remember{ mutableStateOf(20) }
    var cloudBackupLoading by remember{ mutableStateOf(false) }
    var cloudCleanKeepText by remember{ mutableStateOf("20") }
    var showCloudCleanDialog by remember{ mutableStateOf(false) }
    var showCloudBackupRestoreConfirm by remember{ mutableStateOf<JSONObject?>(null) }
    var showCloudBackupDetailId by remember{ mutableStateOf<Int?>(null) }
    var cloudBackupDetailLoading by remember{ mutableStateOf(false) }
    var cloudBackupDetailBackup by remember{ mutableStateOf<JSONObject?>(null) }
    var cloudBackupDetailSummary by remember{ mutableStateOf<JSONObject?>(null) }
    val pageLang = LocalAppLanguage.current
    fun S(key:String)=tr(pageLang,key)
    fun showCloudMsg(msg:String){ cloudMsg=msg; Toast.makeText(ctx,msg,Toast.LENGTH_SHORT).show() }
    fun loadCloudOverview(){
        fun applyOverview(r:JSONObject){
            cloudOverviewKeyRecords=r.optInt("records",-1)
            val ts=r.optLong("updatedAt",0).let{ if(it>0) it else r.optJSONObject("coverage")?.optLong("updatedAt",0)?:0 }
            cloudOverviewUpdatedAt=if(ts>1_000_000_000_000L) ts else ts*1000L
            cloudOverviewHasSettings=r.optBoolean("hasData",r.optBoolean("hasSettings",false))
            cloudOverviewKeyTail=r.optString("privateKeyTail",r.optString("apiKeyTail",r.optString("username","")))
        }
        if(st.cloudToken.isNotBlank()){
            cloudGet(st,"/api/account/me"){ok,msg->
                if(ok){
                    try{ applyOverview(JSONObject(msg)) }catch(_:Exception){}
                }
            }
            return
        }
        cloudGet(st,"/api/key-info"){ok,msg->
            if(ok){
                try{ applyOverview(JSONObject(msg)) }catch(_:Exception){}
            }else{
                cloudGet(st,"/api/meta"){ok2,msg2->
                    if(ok2){
                        try{ applyOverview(JSONObject(msg2)) }catch(_:Exception){}
                    }
                }
            }
        }
    }
    fun applyPulledCloud(pull:CloudPullResult,base:App设置,autoMigrate:Boolean=true){
        if(pull.records.isEmpty()) return
        val mergedSettings=mergeCloudSettings(base,pull.settings)
        if(records.isEmpty()){
            st=mergedSettings; on(mergedSettings)
            onCloudRestore(pull.records,mergedSettings)
            showCloudMsg("已从云端恢复：${pull.records.size} 个号码" + if(pull.recoveredVia=="coverage") "（来自同步卡片）" else "")
            if(autoMigrate){
                runCatching{
                    cloudPost(mergedSettings,"/api/sync",cloudEncryptedPayload(pull.records,mergedSettings)){ok,_->
                        if(ok) loadCloudOverview()
                    }
                }
            }
        }else{
            cloudRestoreChoice=Pair(pull.records,pull.settings)
            showCloudMsg("云端有 ${pull.records.size} 个号码，请选择合并或覆盖本地")
        }
    }
    fun loadCloudReminderStatus(){
        if(st.cloudToken.isBlank()) return
        cloudGet(st,"/api/reminder-status"){ok,msg->
            if(ok){
                try{
                    val r=JSONObject(msg)
                    cloudReminderLastCheck=r.optLong("lastCheckAt",0)
                    cloudReminderNextCheck=r.optLong("nextCheckAt",0)
                    cloudReminderDueNow=r.optInt("dueNow",-1)
                    val ls=r.optJSONObject("lastStats")
                    cloudReminderStats=if(ls!=null) "最近：到期 ${ls.optInt("due",0)}，邮件 ${ls.optInt("mail",0)}，TG ${ls.optInt("tg",0)}，重复 ${ls.optInt("duplicate",0)}" else ""
                }catch(_:Exception){}
            }
        }
    }
    fun loadCloudBackups(limit:Int=cloudBackupsLimit){
        if(st.cloudToken.isBlank()){
            cloudBackups=emptyList(); cloudBackupsTotal=0; cloudBackupsLimit=limit
            return
        }
        cloudBackupLoading=true
        cloudBackupsLimit=limit
        cloudGet(st,"/api/backups?limit=$limit"){ok,msg->
            cloudBackupLoading=false
            if(ok){
                try{
                    val r=JSONObject(msg)
                    cloudBackupsTotal=r.optInt("total",0)
                    val arr=r.optJSONArray("backups") ?: JSONArray()
                    val list=mutableListOf<JSONObject>()
                    for(i in 0 until arr.length()) list.add(arr.getJSONObject(i))
                    cloudBackups=list
                }catch(_:Exception){}
            }else showCloudMsg(msg)
        }
    }
    fun loadCloudBackupDetail(bid:Int){
        if(bid<=0) return
        showCloudBackupDetailId=bid
        cloudBackupDetailLoading=true
        cloudBackupDetailBackup=null
        cloudBackupDetailSummary=null
        cloudGet(st,"/api/backups/$bid"){ok,msg->
            cloudBackupDetailLoading=false
            if(ok){
                try{
                    val r=JSONObject(msg)
                    cloudBackupDetailBackup=r.optJSONObject("backup")
                    cloudBackupDetailSummary=r.optJSONObject("summary")
                }catch(_:Exception){}
            }else showCloudMsg(msg)
        }
    }
    fun restoreCloudBackup(item:JSONObject){
        val bid=item.optInt("id",0)
        if(bid<=0){ showCloudMsg("备份 ID 无效"); return }
        restoreCloudBackupById(st,bid){ok,msg->
            if(ok){
                try{
                    cloudGet(st,"/api/sync"){ok2,msg2->
                        if(ok2){
                            val pull=analyzeCloudSyncResponse(msg2,st)
                            if(pull.records.isNotEmpty()){
                                val ns=mergeCloudSettings(st,pull.settings)
                                st=ns
                                onCloudRestore(pull.records,ns)
                                showCloudMsg("已恢复指定备份：${pull.records.size} 个号码，配置已同步")
                            }else if(pull.hasEncryptedVault || pull.cloudRecordHint>0){
                                showCloudMsg("已恢复指定备份，但当前密码密钥解不开密文；请退出后用正确密码重新登录")
                            }else showCloudMsg("已恢复指定备份")
                        }else showCloudMsg("已恢复指定备份")
                        loadCloudOverview()
                        loadCloudReminderStatus()
                        loadCloudBackups()
                    }
                }catch(_:Exception){
                    showCloudMsg("已恢复指定备份")
                    loadCloudOverview()
                    loadCloudBackups()
                }
            }else showCloudMsg(msg)
        }
    }
    val bgPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> if(uri!=null){ st=st.copyMut{backgroundUri=uri.toString()}; on(st) } }
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState()).padding(horizontal=18.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
    LaunchedEffect(Unit){
        loadCloudOverview()
    }
        SettingsSection(L("外观")){
            IOSSwitchRow(L("深色模式"),st.dark){ st=st.copyMut{dark=it}; on(st) }
            IOSSwitchRow(L("显示首页卡片国旗"),st.showFlag){ st=st.copyMut{showFlag=it}; on(st) }
            IOSSwitchRow("银行卡风格卡片显示",st.bankCardStyle){ st=st.copyMut{bankCardStyle=it}; on(st) }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button({bgPicker.launch("image/*")},shape=RoundedCornerShape(14.dp)){Text(L("更改背景图片"))}
                TextButton({st=st.copyMut{backgroundUri=""};on(st)}){Text(L("清除"))}
            }
            if(st.backgroundUri.isNotBlank()){
                Text(L("已设置自定义背景"),fontSize=11.sp,color=Color(0xFF007AFF))
                Text(L("背景遮罩透明度")+"：${(st.backgroundAlpha*100).roundToInt()}%",fontSize=12.sp,color=Color(0xFF8A94A6))
                Slider(value=st.backgroundAlpha,onValueChange={v->st=st.copyMut{backgroundAlpha=v};on(st)},valueRange=0f..1f)
            }
        }
        SettingsSection(L("提醒设置")){
            IOSSwitchRow(L("开启到期提醒"),st.reminderEnabled){ st=st.copyMut{reminderEnabled=it}; on(st) }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){ listOf(1,3,7).forEach{ d-> IOSChip(L("提前")+cycleText(LocalAppLanguage.current,d),st.remind天==d,Modifier.weight(1f)){ st=st.copyMut{remind天=d}; on(st) } } }
            OutlinedTextField(st.remind天.toString(),{st=st.copyMut{remind天=it.toIntOrNull()?:7};on(st)},modifier=Modifier.fillMaxWidth(),label={Text(L("自定义提前天数"))},singleLine=true)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                OutlinedTextField(st.remindHour.toString(),{st=st.copyMut{remindHour=(it.toIntOrNull()?:9).coerceIn(0,23)};on(st)},modifier=Modifier.weight(1f),label={Text(L("小时"))},singleLine=true)
                OutlinedTextField(st.remindMinute.toString(),{st=st.copyMut{remindMinute=(it.toIntOrNull()?:0).coerceIn(0,59)};on(st)},modifier=Modifier.weight(1f),label={Text(L("分钟"))},singleLine=true)
            }
        }
        TrafficInterfaceSettings(st,{ ns-> st=ns; on(st) })

        // 登录态：只要有 token 即可；vault 密钥由账号密码派生，私钥仅用于重置密码。
        val cloudSignedIn=st.cloudToken.isNotBlank()
        val hasLocalVaultKey=cleanCloudApiKey(st.cloudApiKey).isNotBlank()
        SettingsSection(S("云端提醒")){
            IOSSwitchRow(S("启用云端提醒"),st.cloudEnabled){ st=st.copyMut{cloudEnabled=it}; on(st) }
            PlainInput(S("服务地址"),st.cloudUrl){ st=st.copyMut{cloudUrl=it}; on(st) }
            Text(S("服务地址说明"),fontSize=11.sp,color=Color(0xFF8A94A6),lineHeight=16.sp)
            IOSSwitchRow(S("自动同步"),st.cloudAutoSync){ st=st.copyMut{cloudAutoSync=it}; on(st) }
            Text("号码加解密只使用账号密码。登录成功即自动恢复云端数据。私钥只用于忘记密码时重置，与解密无关。",fontSize=11.sp,color=Color(0xFF8A94A6),lineHeight=16.sp)
            if(!cloudSignedIn){
                PlainInput("云同步账号",registerUsername.ifBlank{st.cloudUsername}){ registerUsername=it.trim().take(64) }
                OutlinedTextField(
                    value=registerPassword,
                    onValueChange={ registerPassword=it },
                    modifier=Modifier.fillMaxWidth(),
                    singleLine=true,
                    visualTransformation=PasswordVisualTransformation(),
                    label={ Text(if(resetPasswordMode) "新密码" else "云同步密码") },
                    shape=RoundedCornerShape(18.dp)
                )
                OutlinedTextField(
                    value=registerPasswordAgain,
                    onValueChange={ registerPasswordAgain=it },
                    modifier=Modifier.fillMaxWidth(),
                    singleLine=true,
                    visualTransformation=PasswordVisualTransformation(),
                    label={ Text(if(resetPasswordMode) "确认新密码" else "确认密码（仅注册需要；登录可留空）") },
                    shape=RoundedCornerShape(18.dp)
                )
                // 私钥仅在「忘记密码重置」时需要填写；登录不要求私钥
                if(resetPasswordMode){
                    OutlinedTextField(
                        value=privateKeyInput,
                        onValueChange={ privateKeyInput=it.trim() },
                        modifier=Modifier.fillMaxWidth(),
                        singleLine=true,
                        visualTransformation=PasswordVisualTransformation(),
                        label={ Text("私钥（重置密码必填）") },
                        shape=RoundedCornerShape(18.dp),
                        placeholder={ Text("粘贴注册时保存的私钥") }
                    )
                }
                TextButton(onClick={ resetPasswordMode=!resetPasswordMode; if(!resetPasswordMode) privateKeyInput="" }){ Text(if(resetPasswordMode) "返回登录" else "忘记密码？用私钥重置") }
            }else{
                Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceContainerHighest,modifier=Modifier.fillMaxWidth()){
                    Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(4.dp)){
                        Text("已登录：${st.cloudUsername.ifBlank{"DsimJ 账户"}}",fontSize=15.sp,fontWeight=FontWeight.Bold,color=MaterialTheme.colorScheme.onSurface)
                        Text(
                            if(hasLocalVaultKey)
                                "已登录，云端号码用密码加解密。换机只要账号密码即可自动恢复。"
                            else
                                "请退出后用账号密码重新登录，登录后会自动用密码解密并恢复。",
                            fontSize=12.sp,
                            color=MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight=17.sp
                        )
                    }
                }
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button({
                    val username=registerUsername.ifBlank{st.cloudUsername}.trim()
                    val password=registerPassword
                    when{
                        cloudSignedIn -> cloudGet(st,"/api/account/me"){ok,msg-> if(ok){ showCloudMsg("云同步账户正常"); loadCloudOverview() } else showCloudMsg(msg) }
                        resetPasswordMode -> {
                            val pkey=cleanCloudApiKey(privateKeyInput)
                            when{
                                username.length<3 -> showCloudMsg("账号至少 3 位")
                                password.length<8 -> showCloudMsg("新密码至少 8 位")
                                password!=registerPasswordAgain -> showCloudMsg("两次密码不一致")
                                !isValidCloudApiKey(pkey) -> showCloudMsg("请填写正确的私钥")
                                else -> {
                                    registerBusy=true
                                    val body=JSONObject().put("username",username).put("privateKey",pkey).put("newPassword",password).toString()
                                    cloudPost(st,"/api/account/reset-password",body){ok,msg->
                                        registerBusy=false
                                        if(ok){
                                            resetPasswordMode=false
                                            // 重置后数据密钥改由新密码派生；私钥只用于身份校验
                                            val newSecret=runCatching{ deriveSimjCloudSecret(username,password) }.getOrDefault("")
                                            val ns=st.copyMut{
                                                cloudUsername=username
                                                if(newSecret.isNotBlank()) cloudApiKey=newSecret
                                            }
                                            st=ns; on(ns)
                                            privateKeyInput=""
                                            registerPassword=""; registerPasswordAgain=""
                                            showCloudMsg("密码已重置。请用新密码登录；若云端是旧密码加密的数据，请在原设备重新「同步到云端」一次。")
                                        }else showCloudMsg(msg)
                                    }
                                }
                            }
                        }
                        username.length<3 -> showCloudMsg("账号至少 3 位")
                        password.length<8 -> showCloudMsg("密码至少 8 位")
                        else -> {
                            // 登录：账号+密码。vault 只按密码解密，私钥不参与。
                            val pwdSecret=runCatching{ deriveSimjCloudSecret(username,password) }.getOrDefault("")
                            registerBusy=true
                            val body=JSONObject().put("username",username).put("password",password).toString()
                            cloudPost(st,"/api/account/login",body){ok,msg->
                                registerBusy=false
                                if(ok){
                                    val token=runCatching{ JSONObject(msg).optString("token","") }.getOrDefault("")
                                    if(token.isBlank()) showCloudMsg("登录成功但服务器未返回令牌")
                                    else if(pwdSecret.isBlank()) showCloudMsg("登录成功但无法从密码派生数据密钥")
                                    else {
                                        val ns=st.copyMut{
                                            cloudUrl=cleanBundledCloudUrl(st.cloudUrl)
                                            cloudUsername=username
                                            cloudToken=token
                                            cloudApiKey=pwdSecret // ONLY password-derived vault key
                                            cloudDeviceId=cloudDeviceId.ifBlank{UUID.randomUUID().toString()}
                                            cloudEnabled=true
                                            cloudAutoSync=true
                                        }
                                        st=ns; on(ns)
                                        val loginPassword=password
                                        registerPassword=""; registerPasswordAgain=""; privateKeyInput=""
                                        showCloudMsg("登录成功，正在用密码解密云端…")
                                        loadCloudOverview()
                                        cloudGet(ns,"/api/sync"){ok2,msg2->
                                            if(!ok2){
                                                if(msg2.contains("404") || msg2.contains("暂无") || msg2.contains("no cloud data",true))
                                                    showCloudMsg("登录成功 · 云端暂无数据，可直接同步本地号码")
                                                else showCloudMsg("登录成功，但拉取云端失败：$msg2")
                                                return@cloudGet
                                            }
                                            val pull=analyzeCloudSyncResponse(msg2,ns,emptyList(),password=loginPassword)
                                            if(pull.records.isNotEmpty()){
                                                applyPulledCloud(pull,ns,autoMigrate=true)
                                            }else if(pull.hasEncryptedVault || pull.cloudRecordHint>0){
                                                if(records.isNotEmpty()){
                                                    // 本机有号：用密码重新加密上传覆盖旧密文
                                                    showCloudMsg("密码无法匹配旧密文。本机有 ${records.size} 个号码，正在用密码重新同步到云端…")
                                                    cloudPost(ns,"/api/sync",cloudEncryptedPayload(records,ns)){ok3,msg3->
                                                        showCloudMsg(if(ok3) "已用密码重写云端：${records.size} 个号码，之后换机可直接恢复" else msg3)
                                                        if(ok3) loadCloudOverview()
                                                    }
                                                }else{
                                                    showCloudMsg("登录成功。云端有统计但密码解不开密文：请确认密码正确；或在有本地号码的设备登录后点「同步到云端」。")
                                                }
                                            }else{
                                                showCloudMsg("登录成功 · 云端暂无号码")
                                            }
                                        }
                                    }
                                }else showCloudMsg(msg)
                            }
                        }
                    }
                },shape=RoundedCornerShape(14.dp),modifier=Modifier.weight(1f)){Text(if(cloudSignedIn) "测试账户" else if(resetPasswordMode) "重置密码" else "登录账户")}
                Button({
                    if(cloudSignedIn){
                        // 退出只清会话；下次用账号+密码登录会重新派生密钥并自动恢复
                        val ns=st.copyMut{ cloudToken="" }
                        st=ns; on(ns); showCloudMsg("已退出登录。下次用账号密码登录即可自动恢复")
                    }else if(resetPasswordMode){
                        resetPasswordMode=false; privateKeyInput=""
                    }else{
                        val username=registerUsername.trim()
                        val password=registerPassword
                        when{
                            username.length<3 -> showCloudMsg("账号至少 3 位")
                            password.length<8 -> showCloudMsg("密码至少 8 位")
                            password!=registerPasswordAgain -> showCloudMsg("两次密码不一致")
                            else -> {
                                registerBusy=true
                                val body=JSONObject().put("username",username).put("password",password).put("source","app").toString()
                                cloudPost(st,"/api/account/register",body){ok,msg->
                                    registerBusy=false
                                    if(ok){
                                        val obj=runCatching{ JSONObject(msg) }.getOrNull()
                                        val token=obj?.optString("token","") ?: ""
                                        val privateKey=cleanCloudApiKey(obj?.optString("privateKey","") ?: "")
                                        val pwdSecret=runCatching{ deriveSimjCloudSecret(username,password) }.getOrDefault("")
                                        if(token.isBlank()) showCloudMsg("注册成功但服务器未返回令牌")
                                        else if(pwdSecret.isBlank()) showCloudMsg("注册成功但无法派生数据密钥")
                                        else {
                                            // 数据密钥=密码派生；私钥仅展示一次，用于忘记密码
                                            val ns=st.copyMut{ cloudUrl=cleanBundledCloudUrl(st.cloudUrl); cloudUsername=username; cloudToken=token; cloudApiKey=pwdSecret; cloudDeviceId=cloudDeviceId.ifBlank{UUID.randomUUID().toString()}; cloudEnabled=true; cloudAutoSync=true }
                                            st=ns; on(ns); registerPassword=""; registerPasswordAgain=""; privateKeyInput=""
                                            if(isValidCloudApiKey(privateKey)) showPrivateKeyOnce=privateKey
                                            showCloudMsg("注册成功。日常登录只需账号密码即可自动恢复；私钥仅用于找回密码，请另存。")
                                            if(records.isNotEmpty()) cloudPost(ns,"/api/sync",cloudEncryptedPayload(records,ns)){ok2,msg2-> showCloudMsg(if(ok2) "已同步到云端：${records.size} 个号码" else msg2); if(ok2) loadCloudOverview() } else loadCloudOverview()
                                        }
                                    }else showCloudMsg(msg)
                                }
                            }
                        }
                    }
                },shape=RoundedCornerShape(14.dp),modifier=Modifier.weight(1f)){Text(if(cloudSignedIn) "退出此设备" else "注册账户")}
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button({
                    if(!cloudSignedIn){ showCloudMsg("请先登录云同步账户"); return@Button }
                    cloudGet(st,"/api/sync"){ok,msg->
                        if(ok){
                            val pull=analyzeCloudSyncResponse(msg,st)
                            if(pull.records.isNotEmpty()){
                                if(records.isEmpty()) showCloudMsg("云端已有 ${pull.records.size} 个号码，请先从云端恢复") else cloudSyncChoice=Pair(pull.records,pull.settings)
                            }else if(pull.hasEncryptedVault || pull.cloudRecordHint>0){
                                val countText=if(pull.cloudRecordHint>0) "${pull.cloudRecordHint} 个号码" else "加密数据"
                                if(records.isEmpty()){
                                    showCloudMsg("云端有$countText，但当前密码密钥解不开。请退出后用正确密码登录，或在有本地号码的设备重新同步。")
                                }else{
                                    showCloudMsg("云端有$countText，但当前密码密钥解不开。如确认本机数据最新，可选择覆盖云端。")
                                    cloudOverwriteConfirm=true
                                }
                            }else{
                                if(records.isEmpty()) showCloudMsg("本地暂无号码") else cloudPost(st,"/api/sync",cloudEncryptedPayload(records,st)){ok2,msg2-> showCloudMsg(if(ok2) S("同步成功") else msg2); if(ok2){ loadCloudOverview() } }
                            }
                        }else if(msg.contains("404") || msg.contains("暂无") || msg.contains("no cloud data",true)){
                            if(records.isEmpty()) showCloudMsg("本地暂无号码") else cloudPost(st,"/api/sync",cloudEncryptedPayload(records,st)){ok2,msg2-> showCloudMsg(if(ok2) S("同步成功") else msg2); if(ok2){ loadCloudOverview() } }
                        }else showCloudMsg(msg)
                    }
                },shape=RoundedCornerShape(14.dp),modifier=Modifier.weight(1f)){Text(S("同步到云端"))}
                Button({
                    if(!cloudSignedIn){ showCloudMsg("请先登录云同步账户"); return@Button }
                    if(cleanCloudApiKey(st.cloudApiKey).isBlank()){
                        showCloudMsg("缺少数据密钥，请退出后用账号密码重新登录（将自动恢复）")
                        return@Button
                    }
                    showCloudMsg("正在用密码解密并恢复…")
                    cloudGet(st,"/api/sync"){ok,msg->
                        if(ok){
                            val pull=analyzeCloudSyncResponse(msg,st)
                            if(pull.records.isNotEmpty()){
                                applyPulledCloud(pull,st,autoMigrate=true)
                            }else if(pull.hasEncryptedVault || pull.cloudRecordHint>0){
                                if(records.isNotEmpty()){
                                    showCloudMsg("密文与当前密码不匹配。本机有号码，点「同步到云端」用密码重写即可")
                                }else{
                                    showCloudMsg("当前密码解不开云端密文。请退出后用正确密码重新登录；或在有本地号码的设备上同步一次。")
                                }
                            }else showCloudMsg("云端没有可恢复的号码数据")
                        }else showCloudMsg(msg)
                    }
                },shape=RoundedCornerShape(14.dp),modifier=Modifier.weight(1f)){Text(S("从云端恢复"))}
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                IOSSwitchRow(S("云端 Telegram"),st.cloudTelegramEnabled){ st=st.copyMut{cloudTelegramEnabled=it}; on(st) }
            }
            if(st.cloudTelegramEnabled){
                IOSSwitchRow(S("启用 TG 配置"),st.tgEnabled){ st=st.copyMut{tgEnabled=it}; on(st) }
                PlainInput("Bot Token",st.botToken){ st=st.copyMut{botToken=it}; on(st) }
                PlainInput("Chat ID",st.chatId){ st=st.copyMut{chatId=it}; on(st) }
                Text(S("TG配置说明"),fontSize=11.sp,color=Color(0xFF8A94A6),lineHeight=16.sp)
            }
            IOSDividerLine()
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                IOSSwitchRow(S("云端邮件"),st.cloudEmailEnabled){ st=st.copyMut{cloudEmailEnabled=it}; on(st) }
            }
            if(st.cloudEmailEnabled){
                IOSSwitchRow(S("SMTP 自动发邮件"),st.smtpEnabled){ st=st.copyMut{smtpEnabled=it}; on(st) }
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    Box(Modifier.weight(1f)){ PlainInput(S("SMTP 服务器"),st.smtpHost){ st=st.copyMut{smtpHost=it}; on(st) } }
                    Box(Modifier.weight(.45f)){ PlainInput(S("端口"),st.smtpPort.toString()){ st=st.copyMut{smtpPort=it.toIntOrNull()?:465}; on(st) } }
                }
                PlainInput(S("邮箱账号"),st.smtpUser){ st=st.copyMut{smtpUser=it}; on(st) }
                PlainInput(S("授权码"),st.smtpPass){ st=st.copyMut{smtpPass=it}; on(st) }
                PlainInput(S("发件邮箱"),st.smtpFrom){ st=st.copyMut{smtpFrom=it}; on(st) }
                PlainInput(S("收件邮箱"),st.smtpTo){ st=st.copyMut{smtpTo=it}; on(st) }
                Text(S("SMTP授权码说明"),fontSize=11.sp,color=Color(0xFF8A94A6),lineHeight=16.sp)
            }
            IOSDividerLine()
            IOSSwitchRow(S("本地通知提醒"),st.notificationEnabled){ st=st.copyMut{notificationEnabled=it}; on(st) }
            IOSSwitchRow(S("通知一键发邮件"),st.emailQuickEnabled){ st=st.copyMut{emailQuickEnabled=it}; on(st) }
            Text(S("本地通知说明"),fontSize=11.sp,color=Color(0xFF8A94A6),lineHeight=16.sp)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Button({ showCloudMsg("端到端加密后，服务器不再读取号码或提醒配置；这些测试保留在本机执行。") },shape=RoundedCornerShape(14.dp),modifier=Modifier.weight(1f)){Text(S("测试TG"))}
                Button({ showCloudMsg("端到端加密后，服务器不再读取号码或提醒配置；这些测试保留在本机执行。") },shape=RoundedCornerShape(14.dp),modifier=Modifier.weight(1f)){Text(S("测试邮件"))}
            }
            Button({ showCloudMsg("号码已端到端加密，云端不会解密检查；到期提醒由本机通知继续负责。") },shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()){Text(S("立即检查到期"))}
            Text(S("云端服务说明"),fontSize=12.sp,color=Color(0xFF8A94A6),lineHeight=17.sp)
            if(cloudSignedIn){
                Text("云端同步：仅保存密文包和地图覆盖统计",fontSize=12.sp,color=Color(0xFF374151),lineHeight=17.sp)
                Text("网页地图会显示 ${cloudCoverage(records).optInt("countryCount",0)} 个国家/地区覆盖",fontSize=12.sp,color=Color(0xFF374151),lineHeight=17.sp)
            }
            if(cloudMsg.isNotBlank()) Text(cloudMsg,fontSize=12.sp,color=Color(0xFF007AFF),lineHeight=17.sp)
        }


        SettingsSection("云端数据与备份"){
            if(!cloudSignedIn){
                Text("未登录云同步账户，登录后会显示加密包状态和 Web 地图覆盖。",fontSize=12.sp,color=Color(0xFF8A94A6),lineHeight=17.sp)
            }else{
                Text("当前账户：${st.cloudUsername.ifBlank { "..." }}",fontSize=12.sp,color=Color(0xFF374151))
                Text("云端号码：${if(cloudOverviewKeyRecords>=0) cloudOverviewKeyRecords.toString() else "-"}",fontSize=13.sp,color=Color(0xFF374151))
                Text("云端加密包：${if(cloudOverviewHasSettings) "已同步" else "未同步"}",fontSize=13.sp,color=Color(0xFF374151))
                Text("上次同步：${if(cloudOverviewUpdatedAt>0) java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(cloudOverviewUpdatedAt)) else "-"}",fontSize=13.sp,color=Color(0xFF374151))
                if(cloudMsg.isNotBlank()) Text(cloudMsg,fontSize=12.sp,color=Color(0xFF007AFF),lineHeight=17.sp)
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    Button({ loadCloudOverview() },shape=RoundedCornerShape(14.dp),modifier=Modifier.weight(1f)){ Text("刷新状态") }
                    Button({ runCatching{ ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(cleanBundledCloudUrl(st.cloudUrl)))) } },shape=RoundedCornerShape(14.dp),modifier=Modifier.weight(1f)){ Text("打开 Web 地图") }
                    if(cloudBackupsTotal>cloudBackups.size){
                        Button({ loadCloudBackups(cloudBackupsLimit+20) },shape=RoundedCornerShape(14.dp),modifier=Modifier.weight(1f)){ Text("加载更多") }
                    }
                }
                if(cloudBackupsTotal>0){
                    Text("云端备份共 ${cloudBackupsTotal} 条，当前显示 ${cloudBackups.size} 条",fontSize=12.sp,color=Color(0xFF8A94A6),lineHeight=17.sp)
                }
                if(cloudBackups.isNotEmpty()){
                    Text("最近云端备份",fontSize=13.sp,color=Color(0xFF8A94A6))
                    cloudBackups.forEach{ item ->
                        val rid=item.optInt("id",0)
                        val reason=item.optString("reason","")
                        val recordsCount=item.optInt("records_count",0)
                        val ts=item.optLong("created_at",0)*1000L
                        val timeText=if(ts>0) java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ts)) else "-"
                        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).border(0.6.dp,Color(0xFFE5E7EB),RoundedCornerShape(12.dp)).motionClickable(pressedScale=.985f){ showCloudBackupRestoreConfirm=item }.padding(horizontal=12.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
                            Column(Modifier.weight(1f)){
                                Text("$reason  ·  ${recordsCount}条",fontSize=14.sp,fontWeight=FontWeight.SemiBold,color=Color(0xFF111827),maxLines=1,overflow=TextOverflow.Ellipsis)
                                Text("$timeText  ·  ID $rid",fontSize=12.sp,color=Color(0xFF8A94A6))
                            }
                            Row(horizontalArrangement=Arrangement.spacedBy(10.dp),verticalAlignment=Alignment.CenterVertically){
                                Text("详情",fontSize=13.sp,fontWeight=FontWeight.SemiBold,color=Color(0xFF007AFF),modifier=Modifier.motionClickable{ loadCloudBackupDetail(rid) })
                                Text("恢复",fontSize=13.sp,fontWeight=FontWeight.SemiBold,color=Color(0xFF007AFF))
                            }
                        }
                    }
                }else if(cloudBackupLoading.not()){
                    Text("端到端加密模式下，服务器只保存当前密文包；具体号码不会在后台备份明细中展开。",fontSize=12.sp,color=Color(0xFF8A94A6),lineHeight=17.sp)
                }
Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    Button({ showCloudMsg("端到端同步不展示服务器端号码备份明细。") },shape=RoundedCornerShape(14.dp),modifier=Modifier.weight(1f)){ Text("备份策略") }
                }
            }
        }
        SettingsSection(L("工具")){
            var pickTraffic by remember { mutableStateOf(false) }
            var pickDial by remember { mutableStateOf(false) }
            var importDlg by remember { mutableStateOf(false) }
            var importText by remember { mutableStateOf("") }
            ToolRow("traffic",L("刷流量"),L("选择一个号码执行真实下载流量测试")){ pickTraffic=true }
            ToolRow("dial",L("拨号测试"),L("选择号码并打开系统拨号器")){ pickDial=true }
            ToolRow("export_json",L("导出 JSON"),L("生成完整 JSON 备份文本")){ onExportJson() }
            ToolRow("export_csv",L("导出 CSV"),L("生成 CSV 表格文本")){ onExportCsv() }
            ToolRow("import",L("导入数据"),L("粘贴 JSON 或 CSV 恢复号码列表")){ importDlg=true }
            var exportSimHub by remember{ mutableStateOf(false) }
            var importSimHub by remember{ mutableStateOf(false) }
            ToolRow("export_json",L("导出 SimHub"),L("导出为 SimHub JSON 兼容格式")){ exportSimHub=true }
            ToolRow("import",L("导入 SimHub"),L("从 SimHub JSON 文件导入号码")){ importSimHub=true }
            if(pickTraffic) NumberPickerDialog(L("选择刷流量号码"),records,{pickTraffic=false}){ pickTraffic=false; onTraffic(it) }
            if(pickDial) NumberPickerDialog(L("选择拨号号码"),records,{pickDial=false}){ pickDial=false; onDial(it) }
            if(importDlg) IOSImportDialog(importText,{importText=it},{importDlg=false},{ onImportText(importText); importDlg=false },ctx)
            if(exportSimHub){
                val json = com.sansim.app.util.SimHubCompat.exportToJson(records)
                val exportTitle=L("导出 SimHub")
                AlertDialog(onDismissRequest={exportSimHub=false},
                    title={Text(L("导出 SimHub JSON"))},
                    text={Text(L("已生成")+" ${records.size} "+L("个号码的 SimHub 兼容 JSON"))},
                    confirmButton={
                        Row{
                            Button({shareExportFile(ctx,"simj-simhub-export.json","application/json",json,exportTitle)},colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF007AFF))){Text(L("分享"))}
                            Spacer(Modifier.width(8.dp))
                            Button({exportSimHub=false},colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF8E8E93))){Text(L("关闭"))}
                        }
                    })
            }
            val simHubImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){ uri->
                if(uri!=null){
                    val imported = com.sansim.app.util.SimHubCompat.importFromJson(ctx,uri)
                    if(imported.isNotEmpty()){
                        onImportSimHub(imported)
                    }
                }
            }
            LaunchedEffect(importSimHub){ if(importSimHub){ simHubImportLauncher.launch("application/json"); importSimHub=false } }
        }
        SettingsSection(L("语言 / Language")){
            Text(L("当前语言：")+st.language,fontSize=13.sp,color=Color(0xFF8A94A6))
            FlowRow(horizontalArrangement=Arrangement.spacedBy(7.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){
                listOf("简体中文","繁体中文","English","日本語","阿拉伯语").forEach{ lang -> IOSChip(lang,st.language==lang){ st=st.copyMut{language=lang}; on(st) } }
            }
            Text(if(st.language=="阿拉伯语") L("已启用 RTL 右到左布局") else L("支持实时切换，主要页面会立即刷新。"),fontSize=12.sp,color=Color(0xFF8A94A6))
        }
        SettingsSection(L("关于")){
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){
                Box(Modifier.size(34.dp).clip(RoundedCornerShape(17.dp)).background(Color(0xFF007AFF)),contentAlignment=Alignment.Center){Text("i",color=Color.White,fontWeight=FontWeight.Bold)}
                Spacer(Modifier.width(10.dp))
                Text("DsimJ v"+currentVersion+"\n"+L("开发者")+"：爱用AI的Doro\n基于 SIMJ 项目进行二次开发（二改）\n"+L("本地数据存储"),fontSize=13.sp,color=Color(0xFF4B5563),lineHeight=20.sp)
            }
            Spacer(Modifier.height(8.dp))
            var checking by remember { mutableStateOf(false) }
            LaunchedEffect(checking) { if(checking) { kotlinx.coroutines.delay(2000); checking=false } }
            Button(
                onClick = {
                    checking = true
                    onUpdateCheck?.invoke()
                },
                enabled = !checking,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (checking) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (checking) "检查中..." else "检查更新")
            }
        }
        Spacer(Modifier.height(20.dp))
    }
    if(keyGenerateConfirm){
        AlertDialog(
            onDismissRequest={ if(!registerBusy) keyGenerateConfirm=false },
            title={ Text("注册端到端同步账户") },
            text={
                Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
                    Text("服务地址：${cleanBundledCloudUrl(st.cloudUrl)}",fontSize=12.sp,color=Color(0xFF6B7280),lineHeight=17.sp)
                    Text("日常：账号+密码登录后会自动从云端恢复数据。注册时显示的私钥只用于忘记密码时重置，日常不需要填写。",fontSize=12.sp,color=Color(0xFF6B7280),lineHeight=17.sp)
                    Text("私钥只显示一次，请另存；丢失后仍可正常登录同步，但无法再用私钥重置密码。",fontSize=12.sp,color=Color(0xFFC2410C),lineHeight=17.sp)
                    OutlinedTextField(
                        value=registerUsername,
                        onValueChange={ registerUsername=it.trim().take(64) },
                        modifier=Modifier.fillMaxWidth(),
                        singleLine=true,
                        label={ Text("用户名") }
                    )
                    OutlinedTextField(
                        value=registerPassword,
                        onValueChange={ registerPassword=it },
                        modifier=Modifier.fillMaxWidth(),
                        singleLine=true,
                        visualTransformation=PasswordVisualTransformation(),
                        label={ Text("密码，至少 8 位") }
                    )
                    OutlinedTextField(
                        value=registerPasswordAgain,
                        onValueChange={ registerPasswordAgain=it },
                        modifier=Modifier.fillMaxWidth(),
                        singleLine=true,
                        visualTransformation=PasswordVisualTransformation(),
                        label={ Text("再次输入密码") }
                    )
                }
            },
            confirmButton={
                Button(
                    enabled=!registerBusy,
                    onClick={
                        val username=registerUsername.trim()
                        val password=registerPassword
                        when{
                            username.length<3 -> showCloudMsg("用户名至少 3 位")
                            password.length<8 -> showCloudMsg("密码至少 8 位")
                            password!=registerPasswordAgain -> showCloudMsg("两次密码不一致")
                            else -> {
                                registerBusy=true
                                val body=JSONObject().put("username",username).put("password",password).put("source","app").toString()
                                cloudPost(st,"/api/account/register",body){ok,msg->
                                    registerBusy=false
                                    if(ok){
                                        val obj=runCatching{ JSONObject(msg) }.getOrNull()
                                        val token=obj?.optString("token","") ?: ""
                                        val privateKey=cleanCloudApiKey(obj?.optString("privateKey","") ?: "")
                                        val pwdSecret=runCatching{ deriveSimjCloudSecret(username,password) }.getOrDefault("")
                                        if(token.isBlank()){
                                            showCloudMsg("注册成功但未返回令牌")
                                        }else if(pwdSecret.isBlank()){
                                            showCloudMsg("注册成功但无法派生数据密钥")
                                        }else{
                                            // 数据密钥=密码派生；私钥仅展示一次，用于忘记密码
                                            val ns=st.copyMut{ cloudUrl=cleanBundledCloudUrl(st.cloudUrl); cloudUsername=username; cloudToken=token; cloudApiKey=pwdSecret; cloudDeviceId=cloudDeviceId.ifBlank{UUID.randomUUID().toString()}; cloudEnabled=true; cloudAutoSync=true }
                                            st=ns
                                            on(ns)
                                            keyGenerateConfirm=false
                                            registerUsername=""
                                            registerPassword=""
                                            registerPasswordAgain=""
                                            if(isValidCloudApiKey(privateKey)) showPrivateKeyOnce=privateKey
                                            showCloudMsg("注册成功。日常登录只需账号密码即可自动恢复；私钥仅用于找回密码。")
                                            if(records.isNotEmpty()){
                                                cloudPost(ns,"/api/sync",cloudEncryptedPayload(records,ns)){ok2,msg2->
                                                    showCloudMsg(if(ok2) "已同步到新账户：${records.size} 个号码" else msg2)
                                                    if(ok2){ loadCloudOverview(); loadCloudReminderStatus() }
                                                }
                                            }else{
                                                loadCloudOverview()
                                            }
                                        }
                                    }else{
                                        showCloudMsg(msg)
                                    }
                                }
                            }
                        }
                    }
                ){ Text(if(registerBusy) "注册中..." else "注册账户") }
            },
            dismissButton={
                TextButton(enabled=!registerBusy,onClick={keyGenerateConfirm=false}){ Text("取消") }
            }
        )
    }
    showPrivateKeyOnce?.let{ pkey ->
        AlertDialog(
            onDismissRequest={ /* 必须主动确认，避免误关丢钥 */ },
            title={ Text("请保存找回密码私钥") },
            text={
                Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
                    Text("私钥只用于忘记密码时重置，日常登录与云端恢复只需账号+密码。服务器不保存此私钥。",fontSize=13.sp,color=Color(0xFF6B7280),lineHeight=18.sp)
                    Text("请立即复制并另存；丢失后仍可正常登录同步，但无法再用私钥重置密码。",fontSize=13.sp,color=Color(0xFFC2410C),lineHeight=18.sp)
                    Surface(shape=RoundedCornerShape(12.dp),color=Color(0xFF111827),modifier=Modifier.fillMaxWidth()){
                        Text(pkey,modifier=Modifier.padding(12.dp),fontSize=12.sp,color=Color(0xFFFFE4E6),lineHeight=17.sp)
                    }
                }
            },
            confirmButton={
                Button(onClick={
                    val cm=ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    cm.setPrimaryClip(android.content.ClipData.newPlainText("simj-private-key",pkey))
                    showCloudMsg("私钥已复制")
                }){ Text("复制私钥") }
            },
            dismissButton={
                TextButton(onClick={ showPrivateKeyOnce=null }){ Text("我已保存") }
            }
        )
    }
    cloudSyncChoice?.let{ data->
        val cloudRecords=data.first; val cloudSettings=data.second
        CloudDataChoiceDialog(
            title="云端已有数据",
            message="云端已有 ${cloudRecords.size} 个号码，本地有 ${records.size} 个号码。建议使用合并同步，避免覆盖其他设备的数据。",
            primary="合并同步",
            secondary="覆盖云端",
            dangerSecondary=true,
            onDismiss={cloudSyncChoice=null},
            onPrimary={
                cloudSyncChoice=null
                val merged=mergeRecords(cloudRecords,records)
                val ns=mergeCloudSettings(st,cloudSettings)
                st=ns
                cloudPost(ns,"/api/sync",cloudEncryptedPayload(merged,ns)){ok,msg-> showCloudMsg(if(ok) "合并同步成功：${merged.size} 个号码" else msg); if(ok) loadCloudOverview() }
            },
            onSecondary={ cloudSyncChoice=null; cloudOverwriteConfirm=true }
        )
    }
    if(cloudOverwriteConfirm){
        IOSConfirmDialog("确认覆盖云端？","将用当前手机的 ${records.size} 个号码替换云端数据。新版服务会在覆盖前自动备份旧云端数据；如果服务尚未升级，建议优先使用合并同步。",true,{cloudOverwriteConfirm=false},{
            cloudOverwriteConfirm=false
            if(records.isEmpty()){
                showCloudMsg("本地暂无号码")
            }else{
                cloudPost(st,"/api/sync",cloudEncryptedPayload(records,st)){ok,msg-> showCloudMsg(if(ok) "覆盖云端完成：${records.size} 个号码" else msg); if(ok) loadCloudOverview() }
            }
        })
    }
    showCloudBackupRestoreConfirm?.let{ item ->
        val bid=item.optInt("id",0)
        val reason=item.optString("reason","")
        val cnt=item.optInt("records_count",0)
        IOSConfirmDialog("恢复该备份？","将使用备份 $bid（$reason，${cnt}条）恢复当前云端数据。恢复前会自动备份当前数据。",false,{showCloudBackupRestoreConfirm=null},{
            showCloudBackupRestoreConfirm=null
            restoreCloudBackup(item)
        })
    }
    showCloudBackupDetailId?.let{ bid ->
        Dialog(onDismissRequest={showCloudBackupDetailId=null; cloudBackupDetailBackup=null; cloudBackupDetailSummary=null}){
            Surface(shape=RoundedCornerShape(24.dp),color=Color(0xFFF2F3F7),tonalElevation=0.dp,modifier=Modifier.fillMaxWidth()){
                Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp),horizontalAlignment=Alignment.CenterHorizontally){
                    Text("备份详情",fontSize=20.sp,fontWeight=FontWeight.Bold,color=Color(0xFF111827),textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                    if(cloudBackupDetailLoading){
                        Text("加载中...",fontSize=13.sp,color=Color(0xFF8A94A6))
                    }else{
                        val b=cloudBackupDetailBackup
                        val sum=cloudBackupDetailSummary
                        if(b==null){
                            Text("未读取到备份信息",fontSize=13.sp,color=Color(0xFF8A94A6))
                        }else{
                            val reason=b.optString("reason","")
                            val cnt=b.optInt("records_count",0)
                            val ts=b.optLong("created_at",0)*1000L
                            val timeText=if(ts>0) java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ts)) else "-"
                            Text("ID：${b.optInt("id",0)}",fontSize=13.sp,color=Color(0xFF374151))
                            Text("类型：$reason",fontSize=13.sp,color=Color(0xFF374151))
                            Text("号码数：$cnt",fontSize=13.sp,color=Color(0xFF374151))
                            Text("时间：$timeText",fontSize=13.sp,color=Color(0xFF374151))
                            if(sum!=null){
                                val keys=sum.optJSONArray("settingsKeys")
                                if(keys!=null && keys.length()>0){
                                    Text("配置字段",fontSize=13.sp,color=Color(0xFF8A94A6))
                                    Text((0 until keys.length()).joinToString(", "){ keys.optString(it) },fontSize=12.sp,color=Color(0xFF374151),lineHeight=17.sp)
                                }
                                val samples=sum.optJSONArray("recordSamples")
                                if(samples!=null && samples.length()>0){
                                    Text("号码摘要",fontSize=13.sp,color=Color(0xFF8A94A6))
                                    for(i in 0 until samples.length()){
                                        val r=samples.getJSONObject(i)
                                        Text("${r.optString("countryCode","")} ${"*".repeat(0)}${r.optString("number","")}  ${r.optString("operator","")}  ${r.optString("expireDate","")}",fontSize=12.sp,color=Color(0xFF374151))
                                    }
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
                        Box(Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).motionClickable{showCloudBackupDetailId=null; cloudBackupDetailBackup=null; cloudBackupDetailSummary=null},contentAlignment=Alignment.Center){Text("关闭",fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=Color(0xFF007AFF))}
                        Box(Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF007AFF)).motionClickable{
                            showCloudBackupDetailId=null
                            cloudBackupDetailBackup?.let{ showCloudBackupRestoreConfirm=it }
                        },contentAlignment=Alignment.Center){Text("恢复该备份",fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=Color.White)}
                    }
                }
            }
        }
    }
    if(showCloudCleanDialog){
        Dialog(onDismissRequest={showCloudCleanDialog=false}){
            Surface(shape=RoundedCornerShape(24.dp),color=Color(0xFFF2F3F7),tonalElevation=0.dp,modifier=Modifier.fillMaxWidth()){
                Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp),horizontalAlignment=Alignment.CenterHorizontally){
                    Text("清理旧备份",fontSize=20.sp,fontWeight=FontWeight.Bold,color=Color(0xFF111827),textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                    Text("只保留最近 N 条备份。设置为 0 将清空当前账户的全部备份。",fontSize=14.sp,color=Color(0xFF6B7280),lineHeight=20.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                    OutlinedTextField(value=cloudCleanKeepText,onValueChange={cloudCleanKeepText=it.filter{ch->ch.isDigit()}},modifier=Modifier.fillMaxWidth(),singleLine=true,label={Text("保留条数")})
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
                        Box(Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(16.dp)).background(Color.White).motionClickable{showCloudCleanDialog=false},contentAlignment=Alignment.Center){Text("取消",fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=Color(0xFF007AFF))}
                        Box(Modifier.weight(1f).height(48.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF007AFF)).motionClickable{
                            showCloudCleanDialog=false
                            val keep=cloudCleanKeepText.toIntOrNull()?:20
                            cloudPost(st,"/api/backups/clear",JSONObject().put("keep",keep).toString()){ok,msg->
                                if(ok){
                                    showCloudMsg("已清理旧备份")
                                    loadCloudBackups()
                                }else showCloudMsg(msg)
                            }
                        },contentAlignment=Alignment.Center){Text("确认清理",fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=Color.White)}
                    }
                }
            }
        }
    }
    cloudRestoreChoice?.let{ data->
        val cloudRecords=data.first; val cloudSettings=data.second
        CloudDataChoiceDialog(
            title="从云端恢复",
            message="云端有 ${cloudRecords.size} 个号码，本地有 ${records.size} 个号码。建议合并恢复，号码和 TG/邮件/通知配置会一起迁移。",
            primary="合并恢复",
            secondary="清空本地后恢复",
            dangerSecondary=true,
            onDismiss={cloudRestoreChoice=null},
            onPrimary={
                cloudRestoreChoice=null
                val merged=mergeRecords(cloudRecords,records)
                val ns=mergeCloudSettings(st,cloudSettings)
                st=ns
                onCloudRestore(merged,ns)
                cloudPost(ns,"/api/sync",cloudEncryptedPayload(merged,ns)){_,_->}
                showCloudMsg("合并恢复完成：${merged.size} 个号码，配置已同步")
            },
            onSecondary={
                cloudRestoreChoice=null
                val ns=mergeCloudSettings(st,cloudSettings)
                st=ns
                onCloudRestore(cloudRecords,ns)
                showCloudMsg("云端恢复成功：${cloudRecords.size} 个号码，配置已同步")
            }
        )
    }
}


@Composable fun IOSSection(title:String,content:@Composable ColumnScope.()->Unit){
    val scheme=MaterialTheme.colorScheme
    Column(verticalArrangement=Arrangement.spacedBy(6.dp)){
        Text(title,fontSize=13.sp,color=scheme.onSurfaceVariant,modifier=Modifier.padding(start=4.dp))
        Card(shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=scheme.surfaceContainerHigh),elevation=CardDefaults.cardElevation(1.dp),modifier=Modifier.fillMaxWidth().border(.7.dp,scheme.outlineVariant.copy(alpha=.65f),RoundedCornerShape(22.dp))){
            Column(Modifier.padding(12.dp),verticalArrangement=Arrangement.spacedBy(9.dp)){content()}
        }
    }
}

@Composable fun ModernSettingsSection(title:String,content:@Composable ColumnScope.()->Unit){
    var expanded by remember(title){ mutableStateOf(false) }
    val scheme=MaterialTheme.colorScheme
    val shape=RoundedCornerShape(22.dp)
    val arrowRotation by animateFloatAsState(
        targetValue=if(expanded) 180f else 0f,
        animationSpec=tween(180),
        label="sectionArrowRotation"
    )
    Surface(
        shape=shape,
        color=scheme.surfaceContainerLowest,
        tonalElevation=1.dp,
        shadowElevation=0.dp,
        modifier=Modifier
            .fillMaxWidth()
            .border(.9.dp,scheme.outlineVariant.copy(alpha=.5f),shape)
    ){
        Column{
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .motionClickable(pressedScale=.985f){expanded=!expanded}
                    .padding(start=18.dp,end=12.dp),
                verticalAlignment=Alignment.CenterVertically
            ){
                Text(
                    title,
                    fontSize=17.sp,
                    lineHeight=21.sp,
                    fontWeight=FontWeight.ExtraBold,
                    color=scheme.onSurface,
                    modifier=Modifier.weight(1f)
                )
                Box(
                    Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if(expanded) scheme.primaryContainer else scheme.surfaceContainerHighest),
                    contentAlignment=Alignment.Center
                ){
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription=null,
                        tint=if(expanded) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                        modifier=Modifier
                            .size(25.dp)
                            .graphicsLayer(rotationZ=arrowRotation)
                    )
                }
                if(false) Surface(shape=RoundedCornerShape(14.dp),color=scheme.surfaceContainerHighest,contentColor=scheme.onSurfaceVariant){
                    Text(
                        if(expanded) "⌃" else "⌄",
                        fontSize=18.sp,
                        lineHeight=18.sp,
                        fontWeight=FontWeight.Bold,
                        modifier=Modifier.padding(horizontal=9.dp,vertical=5.dp)
                    )
                }
            }
            if(expanded){
                Box(Modifier.fillMaxWidth().height(1.dp).background(scheme.outlineVariant.copy(alpha=.42f)))
                Column(
                    Modifier.padding(start=18.dp,end=18.dp,top=14.dp,bottom=16.dp),
                    verticalArrangement=Arrangement.spacedBy(13.dp)
                ){ content() }
            }
        }
    }
}

@Composable fun ModernIOSValueRow(title:String,value:String,onClick:()->Unit){
    val scheme=MaterialTheme.colorScheme
    val shape=RoundedCornerShape(18.dp)
    Surface(
        shape=shape,
        color=scheme.surfaceContainerHighest.copy(alpha=.72f),
        tonalElevation=1.dp,
        modifier=Modifier
            .fillMaxWidth()
            .border(.8.dp,scheme.outlineVariant.copy(alpha=.54f),shape)
            .motionClickable(pressedScale=.985f){onClick()}
    ){
        Row(
            Modifier.padding(horizontal=14.dp,vertical=12.dp),
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(12.dp)
        ){
            Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(3.dp)){
                Text(title,fontSize=12.sp,lineHeight=15.sp,fontWeight=FontWeight.Bold,color=scheme.primary,maxLines=1,overflow=TextOverflow.Ellipsis)
                Text(value,fontSize=15.sp,lineHeight=20.sp,fontWeight=FontWeight.Medium,color=scheme.onSurface,maxLines=1,overflow=TextOverflow.Ellipsis)
            }
            Text("›",fontSize=24.sp,lineHeight=24.sp,fontWeight=FontWeight.SemiBold,color=scheme.onSurfaceVariant)
        }
    }
}

@Composable fun ModernIOSField(label:String,value:String,onValue:(String)->Unit,placeholder:String,singleLine:Boolean=true,minLines:Int=1){
    val scheme=MaterialTheme.colorScheme
    val shape=RoundedCornerShape(18.dp)
    Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
        Text(
            label,
            fontSize=13.sp,
            lineHeight=16.sp,
            fontWeight=FontWeight.Bold,
            color=scheme.onSurfaceVariant,
            modifier=Modifier.padding(start=2.dp)
        )
        TextField(
            value=value,
            onValueChange=onValue,
            modifier=Modifier
                .fillMaxWidth()
                .heightIn(min=58.dp)
                .border(1.dp,scheme.outlineVariant.copy(alpha=.46f),shape)
                .clip(shape),
            singleLine=singleLine,
            minLines=minLines,
            placeholder={
                Text(
                    placeholder,
                    fontSize=14.sp,
                    lineHeight=18.sp,
                    color=scheme.onSurfaceVariant.copy(alpha=.58f),
                    maxLines=1,
                    overflow=TextOverflow.Ellipsis
                )
            },
            colors=TextFieldDefaults.colors(
                focusedContainerColor=scheme.surface,
                unfocusedContainerColor=scheme.surface,
                focusedIndicatorColor=Color.Transparent,
                unfocusedIndicatorColor=Color.Transparent,
                cursorColor=scheme.primary,
                focusedTextColor=scheme.onSurface,
                unfocusedTextColor=scheme.onSurface
            ),
            textStyle=TextStyle(fontSize=16.sp,lineHeight=22.sp,fontWeight=FontWeight.Medium,color=scheme.onSurface)
        )
    }
}

@Composable fun ModernIOSChip(text:String,selected:Boolean,m:Modifier=Modifier,onClick:()->Unit){
    val scheme=MaterialTheme.colorScheme
    val shape=RoundedCornerShape(18.dp)
    val container=if(selected) scheme.primaryContainer.copy(alpha=.92f) else scheme.surfaceContainerHighest
    val content=if(selected) scheme.onPrimaryContainer else scheme.onSurfaceVariant
    val border=if(selected) scheme.primary.copy(alpha=.7f) else scheme.outlineVariant.copy(alpha=.7f)
    Row(
        m
            .height(38.dp)
            .defaultMinSize(minWidth=66.dp)
            .clip(shape)
            .background(container)
            .border(1.dp,border,shape)
            .motionClickable(pressedScale=.96f){onClick()}
            .padding(horizontal=13.dp),
        verticalAlignment=Alignment.CenterVertically,
        horizontalArrangement=Arrangement.Center
    ){
        Text(text,fontSize=13.sp,lineHeight=16.sp,fontWeight=FontWeight.Bold,color=content,maxLines=1,overflow=TextOverflow.Ellipsis)
    }
}

@Composable fun SettingsSection(title:String,content:@Composable ColumnScope.()->Unit){
    ModernSettingsSection(title,content)
    return
    var expanded by remember(title){ mutableStateOf(false) }
    val scheme=MaterialTheme.colorScheme
    Column(verticalArrangement=Arrangement.spacedBy(0.dp)){
        Surface(shape=RoundedCornerShape(if(expanded) 26.dp else 22.dp),color=scheme.surfaceContainerHigh,tonalElevation=1.dp,modifier=Modifier.fillMaxWidth().border(.8.dp,scheme.outlineVariant.copy(alpha=.65f),RoundedCornerShape(if(expanded) 26.dp else 22.dp))){
            Column{
                Row(Modifier.fillMaxWidth().height(56.dp).motionClickable(pressedScale=.985f){expanded=!expanded}.padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically){
                    Text(title,fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=scheme.onSurface,modifier=Modifier.weight(1f))
                    Text(if(expanded) "⌃" else "›",fontSize=22.sp,color=scheme.onSurfaceVariant,fontWeight=FontWeight.SemiBold)
                }
                if(expanded){
                    IOSDividerLine()
                    Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){content()}
                }
            }
        }
    }
}
@Composable fun IOSSwitchRow(title:String,checked:Boolean,onChecked:(Boolean)->Unit){
    val scheme=MaterialTheme.colorScheme
    val shape=RoundedCornerShape(18.dp)
    Surface(
        shape=shape,
        color=scheme.surfaceContainerHighest.copy(alpha=.72f),
        modifier=Modifier
            .fillMaxWidth()
            .border(.8.dp,scheme.outlineVariant.copy(alpha=.54f),shape)
    ){
        Row(
            Modifier
                .fillMaxWidth()
                .height(58.dp)
                .padding(horizontal=14.dp),
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.SpaceBetween
        ){
            Text(title,fontSize=16.sp,lineHeight=20.sp,fontWeight=FontWeight.SemiBold,color=scheme.onSurface)
            Switch(
                checked=checked,
                onCheckedChange=onChecked,
                colors=SwitchDefaults.colors(
                    checkedThumbColor=scheme.onPrimary,
                    checkedTrackColor=scheme.primary,
                    uncheckedThumbColor=scheme.onSurfaceVariant,
                    uncheckedTrackColor=scheme.surfaceContainerHighest
                )
            )
        }
    }
    return
    val oldScheme=MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){
        Text(title,fontSize=16.sp,color=oldScheme.onSurface); Switch(checked,onChecked)
    }
}

fun App设置.mutableState()= mutableStateOf(this)

@Composable fun CloudDataChoiceDialog(title:String,message:String,primary:String,secondary:String,dangerSecondary:Boolean=false,onDismiss:()->Unit,onPrimary:()->Unit,onSecondary:()->Unit){
    val scheme=MaterialTheme.colorScheme
    Dialog(onDismissRequest=onDismiss){
        Surface(shape=RoundedCornerShape(28.dp),color=scheme.surfaceContainerHigh,tonalElevation=3.dp,modifier=Modifier.fillMaxWidth()){
            Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp),horizontalAlignment=Alignment.CenterHorizontally){
                Text(title,fontSize=20.sp,fontWeight=FontWeight.Bold,color=scheme.onSurface,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                Text(message,fontSize=14.sp,color=scheme.onSurfaceVariant,lineHeight=20.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                Button(onPrimary,modifier=Modifier.fillMaxWidth().height(50.dp),shape=RoundedCornerShape(18.dp)){Text(primary,fontSize=16.sp,fontWeight=FontWeight.SemiBold)}
                Button(onSecondary,modifier=Modifier.fillMaxWidth().height(48.dp),shape=RoundedCornerShape(18.dp),colors=ButtonDefaults.buttonColors(containerColor=if(dangerSecondary) scheme.error else scheme.secondaryContainer,contentColor=if(dangerSecondary) scheme.onError else scheme.onSecondaryContainer)){Text(secondary,fontSize=15.sp,fontWeight=FontWeight.SemiBold)}
                TextButton(onDismiss){Text(L("取消"),color=scheme.primary)}
            }
        }
    }
}

@Composable fun TrafficInterfaceSettings(st:App设置,onChange:(App设置)->Unit){
    SettingsSection(L("流量接口")){
        PlainInput(label=L("流量接口 URL"),value=st.trafficUrl,onValue={ onChange(st.copyMut{trafficUrl=it}) })
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){
            listOf(
                "Cloudflare" to "https://speed.cloudflare.com/__down?bytes=10485760",
                "Hetzner" to "https://speed.hetzner.de/10MB.bin",
                "ThinkBroadband" to "https://ipv4.download.thinkbroadband.com/10MB.zip"
            ).forEach{ item-> Text(item.first,fontSize=13.sp,fontWeight=FontWeight.SemiBold,color=Color(0xFF00A7D9),modifier=Modifier.clip(RoundedCornerShape(8.dp)).motionClickable{onChange(st.copyMut{trafficUrl=item.second})}.padding(horizontal=6.dp,vertical=4.dp)) }
        }
        PlainInput(label=L("默认流量 KB"),value=st.trafficKb.toString(),onValue={ onChange(st.copyMut{trafficKb=it.toDoubleOrNull()?:st.trafficKb}) })
    }
}

@Composable fun PlainInput(label:String,value:String,onValue:(String)->Unit){
    val scheme=MaterialTheme.colorScheme
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)){
        Text(label,fontSize=13.sp,color=scheme.onSurfaceVariant)
        OutlinedTextField(value=value,onValueChange=onValue,modifier=Modifier.fillMaxWidth().heightIn(min=56.dp),singleLine=true,shape=RoundedCornerShape(18.dp),colors=OutlinedTextFieldDefaults.colors(focusedBorderColor=scheme.primary,unfocusedBorderColor=scheme.outlineVariant,focusedContainerColor=scheme.surfaceContainerHighest,unfocusedContainerColor=scheme.surfaceContainerHighest,focusedTextColor=scheme.onSurface,unfocusedTextColor=scheme.onSurface))
    }
}

fun App设置.copyMut(block:App设置.()->Unit):App设置{ val n=this.copy(); n.block(); return n }
fun App设置.copy()=App设置(dark,remind天,trafficUrl,trafficKb,tgEnabled,botToken,chatId,keepCycle,backgroundUri,backgroundAlpha,reminderEnabled,notificationEnabled,remindHour,remindMinute,language,emailQuickEnabled,smtpEnabled,smtpHost,smtpPort,smtpUser,smtpPass,smtpFrom,smtpTo,cloudEnabled,cloudUrl,cloudApiKey,cloudTelegramEnabled,cloudEmailEnabled,cloudAutoSync,showFlag,bankCardStyle,cloudToken,cloudUsername,cloudDeviceId)
@Composable fun Presets(on:(String)->Unit){
    Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){
        mapOf(
            "Cloudflare" to "https://speed.cloudflare.com/__down?bytes=10485760",
            "Hetzner" to "https://speed.hetzner.de/10MB.bin",
            "ThinkBroadband" to "https://ipv4.download.thinkbroadband.com/10MB.zip",
            "Google204" to "https://www.google.com/generate_204"
        ).forEach{TextButton({on(it.value)}){Text(it.key)}}
    }
}


@Composable fun IOSConfirmDialog(title:String,message:String,danger:Boolean=false,onCancel:()->Unit,onConfirm:()->Unit){
    val scheme=MaterialTheme.colorScheme
    Dialog(onDismissRequest=onCancel){
        Surface(shape=RoundedCornerShape(28.dp),color=scheme.surfaceContainerHigh,tonalElevation=3.dp,modifier=Modifier.fillMaxWidth()){
            Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp),horizontalAlignment=Alignment.CenterHorizontally){
                Text(title,fontSize=20.sp,fontWeight=FontWeight.Bold,color=scheme.onSurface,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                Text(message,fontSize=14.sp,color=scheme.onSurfaceVariant,lineHeight=20.sp,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
                    Box(Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(18.dp)).background(scheme.secondaryContainer).motionClickable{onCancel()},contentAlignment=Alignment.Center){Text(L("取消"),fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=scheme.onSecondaryContainer)}
                    Box(Modifier.weight(1f).height(50.dp).clip(RoundedCornerShape(18.dp)).background(if(danger) scheme.error else scheme.primary).motionClickable{onConfirm()},contentAlignment=Alignment.Center){Text(L("确认"),fontSize=16.sp,fontWeight=FontWeight.SemiBold,color=if(danger) scheme.onError else scheme.onPrimary)}
                }
            }
        }
    }
}

@Composable fun TrafficDialog(ctx:Context,r:PhoneNumberRecord,s:App设置,onDismiss:()->Unit){
    var url by remember{mutableStateOf(if(s.trafficUrl.contains("generate_204")) "https://speed.cloudflare.com/__down?bytes=10485760" else s.trafficUrl)}
    var amount by remember{mutableStateOf(if(s.trafficKb>1.0) "${s.trafficKb.roundToInt()}KB" else "1MB")}
    var confirm by remember{mutableStateOf(false)}
    var result by remember{mutableStateOf<String?>(null)}
    val lang = LocalAppLanguage.current
    val scheme=MaterialTheme.colorScheme
    Dialog(onDismissRequest=onDismiss){
        Surface(shape=RoundedCornerShape(30.dp),color=scheme.surfaceContainerHigh,tonalElevation=3.dp,modifier=Modifier.fillMaxWidth()){
            Column(Modifier.padding(18.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){
                Row(verticalAlignment=Alignment.CenterVertically){
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(16.dp)).background(scheme.primaryContainer),contentAlignment=Alignment.Center){Text("▥",fontSize=22.sp,color=scheme.onPrimaryContainer,fontWeight=FontWeight.Bold)}
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)){Text(L("刷流量"),fontSize=22.sp,fontWeight=FontWeight.Bold,color=scheme.onSurface);Text(L("真实下载数据测试"),fontSize=12.sp,color=scheme.onSurfaceVariant)}
                    TextButton(onDismiss){Text(L("关闭"),color=scheme.primary)}
                }
                IOSSection(L("号码")){
                    Row(verticalAlignment=Alignment.CenterVertically){Text(r.flag,fontSize=24.sp);Spacer(Modifier.width(8.dp));Column{Text(r.operator.ifBlank{r.countryName},fontWeight=FontWeight.SemiBold,color=scheme.onSurface);Text("${r.countryCode} ${formatNumber(r.number)}",fontSize=13.sp,color=scheme.onSurfaceVariant)}}
                }
                IOSSection(L("下载测试接口")){
                    PlainInput(label="URL",value=url,onValue={url=it})
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){
                        listOf(
                            "Cloudflare" to "https://speed.cloudflare.com/__down?bytes=10485760",
                            "Hetzner" to "https://speed.hetzner.de/10MB.bin",
                            "Think" to "https://ipv4.download.thinkbroadband.com/10MB.zip"
                        ).forEach{ item-> IOSChip(item.first, url==item.second, Modifier.weight(1f)){url=item.second} }
                    }
                }
                IOSSection(L("目标流量")){
                    PlainInput(label=L("例：100KB / 1MB / 50MB"),value=amount,onValue={amount=it})
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){
                        listOf("100KB","1MB","5MB","10MB").forEach{ IOSChip(it, amount==it, Modifier.weight(1f)){amount=it} }
                    }
                    Text(L("204 / 空响应接口不能真正消耗流量，建议使用 Cloudflare 或 Hetzner。"),fontSize=12.sp,color=scheme.onSurfaceVariant,lineHeight=17.sp)
                }
                result?.let{
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(scheme.surfaceContainerHighest).padding(12.dp)){Text(it,fontSize=13.sp,color=scheme.onSurface)}
                }
                Button(onClick={confirm=true},modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(18.dp)){Text(L("开始刷流量"),fontSize=16.sp,fontWeight=FontWeight.SemiBold)}
            }
        }
    }
    if(confirm) IOSConfirmDialog(L("确认刷流量？"),L("将实际下载约")+" ${amount} "+L("目标流量")+"。\n"+L("确认后会真实消耗当前网络流量。"),false,{confirm=false},{
        confirm=false
        val targetKb=parseTrafficKb(amount).coerceIn(1.0,1024.0*500.0)
        result=tr(lang,"请求中…")
        consumeTraffic(url,targetKb,lang){msg->result=msg; if(s.tgEnabled) sendTelegram(s.botToken,s.chatId,"📶 DsimJ ${tr(lang,"刷流量")}\n${r.countryCode} ${formatNumber(r.number)}\n$msg")}
    })
}

@Composable fun IOSChip(text:String,selected:Boolean,m:Modifier=Modifier,onClick:()->Unit){
    ModernIOSChip(text,selected,m,onClick)
    return
    val scheme=MaterialTheme.colorScheme
    Box(m.height(36.dp).clip(RoundedCornerShape(18.dp)).background(if(selected) scheme.primaryContainer else scheme.surfaceContainerHighest).border(.8.dp,if(selected) scheme.primary else scheme.outlineVariant,RoundedCornerShape(18.dp)).motionClickable{onClick()},contentAlignment=Alignment.Center){Text(text,fontSize=12.sp,fontWeight=FontWeight.SemiBold,color=if(selected) scheme.onPrimaryContainer else scheme.primary,maxLines=1,overflow=TextOverflow.Ellipsis)}
}



fun csvEscape(v:String)= if(v.any{ it==',' || it=='"' || it=='\n' || it=='\r' }) "\""+v.replace("\"","\"\"")+"\"" else v
fun csvLine(values:List<String>)=values.joinToString(","){csvEscape(it)}
fun recordFields(r:PhoneNumberRecord)=listOf(r.id,r.countryCode,r.countryName,r.flag,r.number,r.operator,r.expireDate,r.note,r.balance,r.eid,r.smdp,r.activationCode,r.startDate,r.createdAt,r.activatedAt,r.longTerm.toString(),r.cycleDays.toString(),r.signalStatus,r.cardType,r.cardBackgroundAssetName,r.cyclePaymentMinorUnits.toString(),r.currencyCode,r.sortOrder.toString())
val recordHeader=listOf("id","countryCode","countryName","flag","number","operator","expireDate","note","balance","eid","smdp","activationCode","startDate","createdAt","activatedAt","longTerm","cycleDays","signalStatus","cardType","cardBackgroundAssetName","cyclePaymentMinorUnits","currencyCode","sortOrder")

fun settingsToJson(s:App设置):JSONObject = JSONObject().put("dark",s.dark).put("remind天",s.remind天).put("trafficUrl",s.trafficUrl).put("trafficKb",s.trafficKb).put("tgEnabled",s.tgEnabled).put("botToken",s.botToken).put("chatId",s.chatId).put("keepCycle",s.keepCycle).put("backgroundUri",s.backgroundUri).put("backgroundAlpha",s.backgroundAlpha.toDouble()).put("reminderEnabled",s.reminderEnabled).put("notificationEnabled",s.notificationEnabled).put("remindHour",s.remindHour).put("remindMinute",s.remindMinute).put("language",s.language).put("emailQuickEnabled",s.emailQuickEnabled).put("smtpEnabled",s.smtpEnabled).put("smtpHost",s.smtpHost).put("smtpPort",s.smtpPort).put("smtpUser",s.smtpUser).put("smtpPass",s.smtpPass).put("smtpFrom",s.smtpFrom).put("smtpTo",s.smtpTo).put("cloudEnabled",s.cloudEnabled).put("cloudUrl",cleanBundledCloudUrl(s.cloudUrl)).put("cloudApiKey",s.cloudApiKey).put("cloudTelegramEnabled",s.cloudTelegramEnabled).put("cloudEmailEnabled",s.cloudEmailEnabled).put("cloudAutoSync",s.cloudAutoSync).put("showFlag",s.showFlag).put("bankCardStyle",s.bankCardStyle).put("cloudToken",s.cloudToken).put("cloudUsername",s.cloudUsername).put("cloudDeviceId",s.cloudDeviceId)

fun settingsFromJson(o:JSONObject):App设置 = App设置(dark=o.optBoolean("dark",false),remind天=o.optInt("remind天",7),trafficUrl=o.optString("trafficUrl","https://speed.cloudflare.com/__down?bytes=10485760"),trafficKb=o.optDouble("trafficKb",1.0),tgEnabled=o.optBoolean("tgEnabled",false),botToken=o.optString("botToken",""),chatId=o.optString("chatId",""),keepCycle=o.optString("keepCycle","月"),backgroundUri=o.optString("backgroundUri",""),backgroundAlpha=o.optDouble("backgroundAlpha",0.72).toFloat(),reminderEnabled=o.optBoolean("reminderEnabled",true),notificationEnabled=o.optBoolean("notificationEnabled",true),remindHour=o.optInt("remindHour",9),remindMinute=o.optInt("remindMinute",0),language=o.optString("language","简体中文"),emailQuickEnabled=o.optBoolean("emailQuickEnabled",true),smtpEnabled=o.optBoolean("smtpEnabled",false),smtpHost=o.optString("smtpHost",""),smtpPort=o.optInt("smtpPort",465),smtpUser=o.optString("smtpUser",""),smtpPass=o.optString("smtpPass",""),smtpFrom=o.optString("smtpFrom",""),smtpTo=o.optString("smtpTo",""),cloudEnabled=o.optBoolean("cloudEnabled",false),cloudUrl=cleanBundledCloudUrl(o.optString("cloudUrl","")),cloudApiKey=o.optString("cloudApiKey",""),cloudTelegramEnabled=o.optBoolean("cloudTelegramEnabled",true),cloudEmailEnabled=o.optBoolean("cloudEmailEnabled",true),cloudAutoSync=o.optBoolean("cloudAutoSync",false),showFlag=o.optBoolean("showFlag",true),bankCardStyle=o.optBoolean("bankCardStyle",false),cloudToken=o.optString("cloudToken",""),cloudUsername=o.optString("cloudUsername",""),cloudDeviceId=o.optString("cloudDeviceId",""))

fun exportRecordsJson(records:List<PhoneNumberRecord>,settings:App设置):String{
    val root=JSONObject()
    val arr=JSONArray()
    records.forEach{ r-> arr.put(DataStore.recordJson(r)) }
    root.put("type","san-sim-full-backup").put("version",3).put("count",records.size).put("records",arr).put("settings",settingsToJson(settings))
    return root.toString(2)
}

fun exportRecordsCsv(records:List<PhoneNumberRecord>):String{
    return buildString{
        appendLine(csvLine(recordHeader))
        records.forEach{ appendLine(csvLine(recordFields(it))) }
    }
}

fun splitCsvLine(line:String):List<String>{
    val out=mutableListOf<String>(); val sb=StringBuilder(); var q=false; var i=0
    while(i<line.length){ val ch=line[i]; when{
        q && ch=='"' && i+1<line.length && line[i+1]=='"' -> { sb.append('"'); i++ }
        ch=='"' -> q=!q
        ch==',' && !q -> { out.add(sb.toString()); sb.clear() }
        else -> sb.append(ch)
    }; i++ }
    out.add(sb.toString()); return out
}

fun parseRecordObject(o:JSONObject)=PhoneNumberRecord(
    id=o.optString("id",UUID.randomUUID().toString()), countryCode=o.optString("countryCode","+86"), countryName=o.optString("countryName","中国"), flag=o.optString("flag","🇨🇳"), number=o.optString("number"), operator=o.optString("operator"), expireDate=o.optString("expireDate",LocalDate.now().plusDays(30).toString()), note=o.optString("note"),
    balance=o.optString("balance"), eid=o.optString("eid"), smdp=o.optString("smdp"), activationCode=o.optString("activationCode"), startDate=o.optString("startDate",LocalDate.now().toString()), createdAt=o.optString("createdAt",LocalDate.now().toString()), activatedAt=o.optString("activatedAt"), longTerm=o.optBoolean("longTerm",false), cycleDays=o.optInt("cycleDays",30), signalStatus=o.optString("signalStatus","在线"), tags=o.optString("tags",""), transactionNotes=o.optString("transactionNotes",""), customPrompt=o.optString("customPrompt",""), websiteURL=o.optString("websiteURL",""), cyclePaymentMinorUnits=o.optInt("cyclePaymentMinorUnits",0), currencyCode=o.optString("currencyCode",""), cardBackgroundAssetName=o.optString("cardBackgroundAssetName",""), cardColorHex=o.optString("cardColorHex",""), cardType=o.optString("cardType","prepaid"), sortOrder=o.optInt("sortOrder",0)
)
fun hasRecordPayload(o:JSONObject):Boolean = listOf("id","number","phoneNumber","operator","carrier","countryCode","countryName","flag","eid","smdp","smdpAddress","activationCode","expireDate","expiryDate","note","balance","cardType","cardBackgroundAssetName").any{ o.optString(it).isNotBlank() }
fun parseRecordArray(arr:JSONArray):List<PhoneNumberRecord> = (0 until arr.length()).mapNotNull{ idx->
    val o=arr.optJSONObject(idx) ?: return@mapNotNull null
    if(hasRecordPayload(o)) parseRecordObject(o) else null
}
fun parseRecordsJson(text:String):Pair<List<PhoneNumberRecord>,App设置?>{
    return runCatching{
        val trimmed=text.trim()
        if(trimmed.startsWith("[")) Pair(parseRecordArray(JSONArray(trimmed)),null)
        else { val obj=JSONObject(trimmed); val arr=obj.getJSONArray("records"); val s=if(obj.has("settings")) settingsFromJson(obj.getJSONObject("settings")) else null; Pair(parseRecordArray(arr),s) }
    }.getOrElse{ Pair(emptyList(),null) }
}

fun parseRecordsCsv(text:String):List<PhoneNumberRecord>{
    return runCatching{
        val lines=text.lines().filter{it.isNotBlank()}
        if(lines.size<2) return@runCatching emptyList<PhoneNumberRecord>()
        val header=splitCsvLine(lines.first()).map{it.trim()}
        lines.drop(1).mapNotNull{ line->
            val vals=splitCsvLine(line); val map=header.mapIndexedNotNull{ i,k-> vals.getOrNull(i)?.let{k to it} }.toMap()
            val o=JSONObject(); map.forEach{(k,v)-> when(k){"longTerm"->o.put(k,v.toBoolean());"cycleDays"->o.put(k,v.toIntOrNull()?:30);"cyclePaymentMinorUnits"->o.put(k,v.toIntOrNull()?:0);"sortOrder"->o.put(k,v.toIntOrNull()?:0);else->o.put(k,v)} }
            parseRecordObject(o).takeIf{hasRecordPayload(o)}
        }
    }.getOrElse{ emptyList() }
}

fun parseRecordsAny(text:String):List<PhoneNumberRecord> = parseRecordsJson(text).first.ifEmpty{ parseRecordsCsv(text) }
fun parseRecordsAndSettings(text:String):Pair<List<PhoneNumberRecord>,App设置?> = parseRecordsJson(text).let{ if(it.first.isEmpty()) Pair(parseRecordsCsv(text),null) else it }

fun parseTrafficKb(text:String):Double{
    val t=text.trim().uppercase().replace(" ","")
    val num=Regex("""[0-9]+(\.[0-9]+)?""").find(t)?.value?.toDoubleOrNull() ?: 1.0
    return when{
        t.contains("GB") || t.endsWith("G") -> num*1024.0*1024.0
        t.contains("MB") || t.endsWith("M") -> num*1024.0
        else -> num
    }
}

fun consumeTraffic(url:String,kb:Double,lang:String,cb:(String)->Unit){
    thread{
        val want=(kb*1024).roundToInt().coerceAtLeast(1)
        val res=runCatching{
            var total=0
            val started=System.currentTimeMillis()
            var round=0
            while(total<want && round<30){
                round++
                val sep=if(url.contains("?")) "&" else "?"
                val u=if(url.contains("speed.cloudflare.com/__down")) url.replace(Regex("""bytes=\d+"""),"bytes=${want-total}") else url+sep+"_san="+System.nanoTime()
                val c=(URL(u).openConnection() as HttpURLConnection)
                c.connectTimeout=15000
                c.readTimeout=30000
                c.instanceFollowRedirects=true
                c.useCaches=false
                c.setRequestProperty("Cache-Control","no-cache")
                c.setRequestProperty("Pragma","no-cache")
                c.setRequestProperty("User-Agent","SanSIM/1.5.6")
                val buf=ByteArray(8192)
                c.inputStream.use{ input->
                    while(total<want){
                        val n=input.read(buf,0,minOf(buf.size,want-total))
                        if(n<=0) break
                        total+=n
                    }
                }
                c.disconnect()
            }
            val sec=maxOf(0.001,(System.currentTimeMillis()-started)/1000.0)
            val speed=total/1024.0/sec
            tr(lang,"成功")+"："+tr(lang,"实际读取")+" ${"%.2f".format(total/1024.0)}KB / "+tr(lang,"目标")+" ${"%.2f".format(want/1024.0)}KB，"+tr(lang,"耗时")+" ${"%.1f".format(sec)} "+tr(lang,"秒")+"，"+tr(lang,"约")+" ${"%.1f".format(speed)}KB/s"
        }.getOrElse{tr(lang,"失败")+"：${it.javaClass.simpleName}: ${it.message}"}
        cb(res)
    }
}
fun dial(ctx:Context,r:PhoneNumberRecord){ ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${r.countryCode}${r.number}"))) }
fun formatNumber(n:String)=n.chunked(4).joinToString(" ")
fun guessOperator(n:String,iso:String):String{
    val x=n.filter{it.isDigit()}
    val p3=x.take(3); val p4=x.take(4)
    return when{
        iso=="CN" && (p4 in listOf("1340","1341","1342","1343","1344","1345","1346","1347","1348") || p3 in listOf("135","136","137","138","139","147","148","150","151","152","157","158","159","172","178","182","183","184","187","188","195","197","198"))->"中国移动"
        iso=="CN" && (p3 in listOf("130","131","132","145","146","155","156","166","175","176","185","186","196"))->"中国联通"
        iso=="CN" && (p3 in listOf("133","149","153","173","174","177","180","181","189","190","191","193","199"))->"中国电信"
        iso=="CN" && p3=="192"->"中国广电"
        iso=="CN" && (p3 in listOf("162","165","167","170","171"))->"虚拟运营商"
        iso=="HK"->"3HK"
        iso=="US"||iso=="CA"->OperatorDatabase.firstNameFor(iso)
        iso=="TH"->"AIS"
        iso=="JP"->"NTT Docomo"
        else->OperatorDatabase.firstNameFor(iso)
    }
}
