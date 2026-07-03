package com.sansim.app.util

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext

object CardBackgroundLoader {
    // 区号到背景图片文件名的映射 (auto-generated from SIMKit classification)
    private val dialCodeToFile = mapOf(
        "+1" to "card-background-us-3-lighttrail.jpg",
        "+7" to "card-background-kazakhstan-lighttrail.jpg",
        "+30" to "card-background-greece-2-lighttrail.jpg",
        "+31" to "card-background-netherlands-2-lighttrail.jpg",
        "+33" to "card-background-france-4-lighttrail.jpg",
        "+34" to "card-background-spain-lighttrail.jpg",
        "+36" to "card-background-hungary-lighttrail.jpg",
        "+39" to "card-background-italy-lighttrail.jpg",
        "+40" to "card-background-romania-lighttrail.jpg",
        "+41" to "card-background-switzerland-lighttrail.jpg",
        "+43" to "card-background-austria-2-lighttrail.jpg",
        "+44" to "card-background-uk-3-lighttrail.jpg",
        "+45" to "card-background-denmark-2-lighttrail.jpg",
        "+47" to "card-background-norway-lighttrail.jpg",
        "+49" to "card-background-germany-lighttrail.jpg",
        "+52" to "card-background-mexico-lighttrail.jpg",
        "+53" to "card-background-cuba-lighttrail.jpg",
        "+54" to "card-background-argentina-lighttrail.jpg",
        "+55" to "card-background-brazil-lighttrail.jpg",
        "+56" to "card-background-chile-lighttrail.jpg",
        "+57" to "card-background-colombia-lighttrail.jpg",
        "+60" to "card-background-malaysia-lighttrail.jpg",
        "+61" to "card-background-australia-2-lighttrail.jpg",
        "+62" to "card-background-indonesia-lighttrail.jpg",
        "+64" to "card-background-new-zealand-lighttrail.jpg",
        "+66" to "card-background-thailand-2-lighttrail.jpg",
        "+81" to "card-background-japan-lighttrail.jpg",
        "+84" to "card-background-vietnam-lighttrail.jpg",
        "+86" to "card-background-china-2-lighttrail.jpg",
        "+91" to "card-background-india-2-lighttrail.jpg",
        "+93" to "card-background-afghanistan-lighttrail.jpg",
        "+95" to "card-background-myanmar-lighttrail.jpg",
        "+98" to "card-background-iran-lighttrail.jpg",
        "+212" to "card-background-morocco-lighttrail.jpg",
        "+213" to "card-background-algeria-lighttrail.jpg",
        "+218" to "card-background-libya-lighttrail.jpg",
        "+222" to "card-background-mauritania-lighttrail.jpg",
        "+223" to "card-background-mali-lighttrail.jpg",
        "+224" to "card-background-guinea-lighttrail.jpg",
        "+226" to "card-background-burkina-faso-lighttrail.jpg",
        "+228" to "card-background-togo-lighttrail.jpg",
        "+229" to "card-background-benin-lighttrail.jpg",
        "+230" to "card-background-mauritius-lighttrail.jpg",
        "+233" to "card-background-ghana-2-lighttrail.jpg",
        "+236" to "card-background-central-african-republic-lighttrail.jpg",
        "+240" to "card-background-equatorial-guinea-lighttrail.jpg",
        "+244" to "card-background-angola-lighttrail.jpg",
        "+250" to "card-background-rwanda-lighttrail.jpg",
        "+251" to "card-background-ethiopia-lighttrail.jpg",
        "+253" to "card-background-djibouti-2-lighttrail.jpg",
        "+254" to "card-background-kenya-lighttrail.jpg",
        "+258" to "card-background-mozambique-lighttrail.jpg",
        "+261" to "card-background-madagascar-lighttrail.jpg",
        "+265" to "card-background-malawi-lighttrail.jpg",
        "+266" to "card-background-lesotho-lighttrail.jpg",
        "+267" to "card-background-botswana-lighttrail.jpg",
        "+268" to "card-background-eswatini-lighttrail.jpg",
        "+269" to "card-background-comoros-lighttrail.jpg",
        "+291" to "card-background-eritrea-lighttrail.jpg",
        "+299" to "card-background-greenland-lighttrail.jpg",
        "+350" to "card-background-gibraltar-lighttrail.jpg",
        "+353" to "card-background-ireland-2-lighttrail.jpg",
        "+354" to "card-background-iceland-2-lighttrail.jpg",
        "+355" to "card-background-albania-lighttrail.jpg",
        "+356" to "card-background-malta-lighttrail.jpg",
        "+357" to "card-background-cyprus-lighttrail.jpg",
        "+358" to "card-background-finland-lighttrail.jpg",
        "+359" to "card-background-bulgaria-lighttrail.jpg",
        "+371" to "card-background-latvia-lighttrail.jpg",
        "+372" to "card-background-estonia-lighttrail.jpg",
        "+373" to "card-background-moldova-lighttrail.jpg",
        "+374" to "card-background-armenia-lighttrail.jpg",
        "+375" to "card-background-belarus-lighttrail.jpg",
        "+376" to "card-background-andorra-lighttrail.jpg",
        "+382" to "card-background-montenegro-lighttrail.jpg",
        "+385" to "card-background-croatia-lighttrail.jpg",
        "+387" to "card-background-bosnia-herzegovina-lighttrail.jpg",
        "+420" to "card-background-czechia-lighttrail.jpg",
        "+423" to "card-background-liechtenstein-lighttrail.jpg",
        "+502" to "card-background-guatemala-lighttrail.jpg",
        "+503" to "card-background-el-salvador-lighttrail.jpg",
        "+504" to "card-background-honduras-lighttrail.jpg",
        "+509" to "card-background-haiti-lighttrail.jpg",
        "+590" to "card-background-guadeloupe-lighttrail.jpg",
        "+591" to "card-background-bolivia-lighttrail.jpg",
        "+592" to "card-background-guyana-lighttrail.jpg",
        "+593" to "card-background-ecuador-lighttrail.jpg",
        "+598" to "card-background-uruguay-lighttrail.jpg",
        "+679" to "card-background-fiji-3-lighttrail.jpg",
        "+686" to "card-background-kiribati-lighttrail.jpg",
        "+688" to "card-background-tuvalu-lighttrail.jpg",
        "+691" to "card-background-micronesia-lighttrail.jpg",
        "+692" to "card-background-marshall-islands-lighttrail.jpg",
        "+852" to "card-background-hong-kong-lighttrail.jpg",
        "+856" to "card-background-laos-lighttrail.jpg",
        "+880" to "card-background-bangladesh-lighttrail.jpg",
        "+886" to "card-background-taiwan-lighttrail.jpg",
        "+960" to "card-background-maldives-lighttrail.jpg",
        "+961" to "card-background-lebanon-lighttrail.jpg",
        "+962" to "card-background-jordan-lighttrail.jpg",
        "+964" to "card-background-iraq-lighttrail.jpg",
        "+965" to "card-background-kuwait-lighttrail.jpg",
        "+972" to "card-background-israel-lighttrail.jpg",
        "+973" to "card-background-bahrain-lighttrail.jpg",
        "+975" to "card-background-bhutan-lighttrail.jpg",
        "+976" to "card-background-mongolia-lighttrail.jpg",
        "+994" to "card-background-azerbaijan-lighttrail.jpg",
        "+995" to "card-background-georgia-lighttrail.jpg",
        "+996" to "card-background-kyrgyzstan-lighttrail.jpg",
        "+1242" to "card-background-bahamas-lighttrail.jpg",
        "+1246" to "card-background-barbados-lighttrail.jpg",
        "+1268" to "card-background-antigua-barbuda-lighttrail.jpg",
        "+1441" to "card-background-bermuda-lighttrail.jpg",
        "+1767" to "card-background-dominica-lighttrail.jpg",
        "+1809" to "card-background-dominican-republic-3-lighttrail.jpg",
    )
    
    fun getCardBackgroundFileName(dialCode: String): String? {
        return dialCodeToFile[dialCode]
    }
    
    fun loadCardBackground(context: Context, dialCode: String): ImageBitmap? {
        val fileName = getCardBackgroundFileName(dialCode) ?: return null
        return try {
            val inputStream = context.assets.open("card_backgrounds/$fileName")
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun rememberCardBackground(dialCode: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(dialCode) {
        CardBackgroundLoader.loadCardBackground(context, dialCode)
    }
}