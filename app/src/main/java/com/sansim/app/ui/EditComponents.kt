package com.sansim.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sansim.app.LocalIsDark

@Composable
private fun dk(dark: Color, light: Color): Color =
    if (LocalIsDark.current) dark else light

/**
 * Tag selector with preset chips + custom tag input.
 * Tags are stored as comma-separated string.
 */
@Composable
fun TagSelector(
    selectedTags: String,
    onTagsChanged: (String) -> Unit
) {
    val presetTags = listOf("保号卡", "旅行备用", "测试卡", "中国卡")
    val currentList = selectedTags.split(",").map { it.trim() }.filter { it.isNotBlank() }
    var customTagInput by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Preset tag chips - row 1
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            presetTags.take(2).forEach { tag ->
                val isSelected = tag in currentList
                TagChip(tag, isSelected, Modifier.weight(1f)) {
                    onTagsChanged(toggleTag(selectedTags, tag))
                }
            }
        }
        // Preset tag chips - row 2
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            presetTags.drop(2).forEach { tag ->
                val isSelected = tag in currentList
                TagChip(tag, isSelected, Modifier.weight(1f)) {
                    onTagsChanged(toggleTag(selectedTags, tag))
                }
            }
            repeat(2 - presetTags.drop(2).size) {
                Spacer(Modifier.weight(1f))
            }
        }

        // Custom tags displayed as chips
        val customTags = currentList.filter { it !in presetTags }
        if (customTags.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                customTags.forEach { tag ->
                    TagChip(tag, true) {
                        onTagsChanged(toggleTag(selectedTags, tag))
                    }
                }
            }
        }

        // Add custom tag input
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BasicTextField(
                value = customTagInput,
                onValueChange = { customTagInput = it },
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(dk(Color(0xFF1C1C1E), Color(0xFFF4F5F8)))
                    .border(0.7.dp, dk(Color(0xFF38383A), Color(0xFFE5E7EB)), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = dk(Color(0xFFE5E5E7), Color(0xFF111827))
                ),
                cursorBrush = SolidColor(Color(0xFF007AFF)),
                decorationBox = { inner ->
                    Box {
                        if (customTagInput.isEmpty()) {
                            Text(
                                "添加自定义标签",
                                fontSize = 13.sp,
                                color = Color(0xFF8A94A6)
                            )
                        }
                        inner()
                    }
                }
            )
            Box(
                Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF007AFF))
                    .clickable {
                        val trimmed = customTagInput.trim()
                        if (trimmed.isNotBlank() && trimmed !in currentList) {
                            val newTags = if (selectedTags.isBlank()) trimmed
                            else "$selectedTags,$trimmed"
                            onTagsChanged(newTags)
                            customTagInput = ""
                        }
                    }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // Show current tags summary
        if (currentList.isNotEmpty()) {
            Text(
                "当前标签：${currentList.joinToString(", ")}",
                fontSize = 11.sp,
                color = Color(0xFF8A94A6)
            )
        }
    }
}

@Composable
private fun TagChip(
    text: String,
    selected: Boolean,
    m: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        m
            .height(34.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) Color(0xFF007AFF) else Color(0xFFF4F5F8))
            .border(
                0.7.dp,
                if (selected) Color(0xFF007AFF) else Color(0xFFE5E7EB),
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (selected) Color.White else Color(0xFF007AFF),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

private fun toggleTag(current: String, tag: String): String {
    val list = current.split(",").map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
    if (tag in list) list.remove(tag) else list.add(tag)
    return list.joinToString(",")
}


    private val bankCardCountryRemarkMap = mapOf(
        "bank_card_006.jpg" to "葡萄牙",
        "bank_card_010.jpg" to "荷兰",
        "bank_card_015.jpg" to "韩国",
        "bank_card_019.jpg" to "丹麦",
        "bank_card_024.jpg" to "阿根廷",
        "bank_card_027.jpg" to "塞尔维亚",
        "bank_card_028.jpg" to "芬兰",
        "bank_card_034.jpg" to "加拿大",
        "bank_card_037.jpg" to "黑山",
        "bank_card_038.jpg" to "美国",
        "bank_card_043.jpg" to "新加坡",
        "bank_card_058.jpg" to "匈牙利",
        "bank_card_060.jpg" to "哈萨克斯坦",
        "bank_card_061.jpg" to "爱沙尼亚",
        "bank_card_064.jpg" to "尼日利亚",
        "bank_card_065.jpg" to "缅甸",
        "bank_card_066.jpg" to "斯洛文尼亚",
        "bank_card_068.jpg" to "卡塔尔",
        "bank_card_082.jpg" to "印度",
        "bank_card_083.jpg" to "智利",
        "bank_card_084.jpg" to "中国",
        "bank_card_087.jpg" to "爱尔兰",
        "bank_card_090.jpg" to "巴西",
        "bank_card_094.jpg" to "文莱",
        "bank_card_105.jpg" to "阿曼",
        "bank_card_107.jpg" to "希腊",
        "bank_card_109.jpg" to "瑞典",
        "bank_card_110.jpg" to "挪威",
        "bank_card_111.jpg" to "孟加拉国",
        "bank_card_115.jpg" to "巴基斯坦",
        "bank_card_122.jpg" to "埃及",
        "bank_card_123.jpg" to "肯尼亚",
        "bank_card_124.jpg" to "法国",
        "bank_card_134.jpg" to "香港",
        "bank_card_137.jpg" to "马来西亚",
        "bank_card_139.jpg" to "保加利亚",
        "bank_card_144.jpg" to "日本",
        "bank_card_160.jpg" to "North Korea",
        "bank_card_165.jpg" to "奥地利",
        "bank_card_167.jpg" to "印尼",
        "bank_card_168.jpg" to "菲律宾",
        "bank_card_174.jpg" to "澳门",
        "bank_card_176.jpg" to "阿联酋",
        "bank_card_178.jpg" to "墨西哥",
        "bank_card_180.jpg" to "克罗地亚",
        "bank_card_183.jpg" to "哥伦比亚",
        "bank_card_184.jpg" to "罗马尼亚",
        "bank_card_188.jpg" to "瑞士",
        "bank_card_189.jpg" to "拉脱维亚",
        "bank_card_191.jpg" to "沙特阿拉伯",
        "bank_card_195.jpg" to "意大利",
        "bank_card_201.jpg" to "西班牙",
        "bank_card_203.jpg" to "斯洛伐克",
        "bank_card_204.jpg" to "英国",
        "bank_card_206.jpg" to "捷克",
        "bank_card_209.jpg" to "土耳其",
        "bank_card_212.jpg" to "科威特",
        "bank_card_213.jpg" to "以色列",
        "bank_card_214.jpg" to "冰岛",
        "bank_card_216.jpg" to "柬埔寨",
        "bank_card_220.jpg" to "斯里兰卡",
        "bank_card_221.jpg" to "Russia",
        "bank_card_222.jpg" to "乌克兰",
        "bank_card_223.jpg" to "泰国",
        "bank_card_225.jpg" to "秘鲁",
        "bank_card_226.jpg" to "老挝",
        "bank_card_228.jpg" to "波兰",
        "bank_card_229.jpg" to "约旦",
        "bank_card_232.jpg" to "波黑",
        "bank_card_234.jpg" to "澳大利亚",
        "bank_card_238.jpg" to "马尔代夫",
        "bank_card_244.jpg" to "Nepal",
        "bank_card_245.jpg" to "南非",
        "bank_card_254.jpg" to "比利时",
        "bank_card_257.jpg" to "越南",
        "bank_card_260.jpg" to "德国",
        "bank_card_262.jpg" to "新西兰",
        "bank_card_265.jpg" to "立陶宛",
        "bank_card_268.jpg" to "阿尔巴尼亚",
        "bank_card_269.jpg" to "摩洛哥"
    )

/**
 * Card background picker - shows preview + country grid from assets/flag_backgrounds/
 */
@Composable
fun CardBackgroundPicker(
    currentAssetName: String,
    onSelect: (String) -> Unit
) {
    val ctx = LocalContext.current
    
    // Load light trail backgrounds from card_backgrounds/
    val lightTrailBackgrounds = remember {
        runCatching { 
            ctx.assets.list("card_backgrounds")
                ?.filter { it.endsWith("-lighttrail.jpg") }
                ?.sorted()
                .orEmpty() 
        }.getOrDefault(emptyList())
    }
    
    // Load bank card backgrounds from bank_card_backgrounds/
    val bankCardBackgrounds = remember {
        runCatching { 
            ctx.assets.list("bank_card_backgrounds")
                ?.filter { it.endsWith(".jpg") }
                ?.sorted()
                .orEmpty() 
        }.getOrDefault(emptyList())
    }
    
    // Extract country name from light trail filename
    fun lightTrailCountryName(fileName: String): String {
        val slug = fileName
            .removePrefix("card-background-")
            .removeSuffix("-lighttrail.jpg")
            .replace(Regex("-\\d+$"), "")
        val slugToChinese = mapOf(
            "afghanistan" to "阿富汗", "aland" to "奥兰群岛", "albania" to "阿尔巴尼亚",
            "algeria" to "阿尔及利亚", "andorra" to "安道尔", "angola" to "安哥拉",
            "antigua-barbuda" to "安提瓜和巴布达", "argentina" to "阿根廷", "armenia" to "亚美尼亚",
            "australia" to "澳大利亚", "austria" to "奥地利", "azerbaijan" to "阿塞拜疆",
            "bahamas" to "巴哈马", "bahrain" to "巴林", "bangladesh" to "孟加拉国",
            "barbados" to "巴巴多斯", "belarus" to "白俄罗斯", "belgium" to "比利时",
            "benin" to "贝宁", "bermuda" to "百慕大", "bhutan" to "不丹",
            "bolivia" to "玻利维亚", "bosnia-herzegovina" to "波黑", "botswana" to "博茨瓦纳",
            "brazil" to "巴西", "brunei" to "文莱", "bulgaria" to "保加利亚",
            "burkina-faso" to "布基纳法索", "cambodia" to "柬埔寨", "canada" to "加拿大",
            "central-african-republic" to "中非", "chile" to "智利", "china" to "中国",
            "colombia" to "哥伦比亚", "comoros" to "科摩罗", "croatia" to "克罗地亚",
            "cuba" to "古巴", "curacao" to "库拉索", "cyprus" to "塞浦路斯",
            "czechia" to "捷克", "denmark" to "丹麦", "djibouti" to "吉布提",
            "dominica" to "多米尼克", "dominican-republic" to "多米尼加", "ecuador" to "厄瓜多尔",
            "egypt" to "埃及", "el-salvador" to "萨尔瓦多", "equatorial-guinea" to "赤道几内亚",
            "eritrea" to "厄立特里亚", "estonia" to "爱沙尼亚", "eswatini" to "斯威士兰",
            "ethiopia" to "埃塞俄比亚", "european-union" to "欧盟", "falkland-islands" to "福克兰群岛",
            "fiji" to "斐济", "finland" to "芬兰", "france" to "法国",
            "french-polynesia" to "法属波利尼西亚", "georgia" to "格鲁吉亚", "germany" to "德国",
            "ghana" to "加纳", "gibraltar" to "直布罗陀", "greece" to "希腊",
            "greenland" to "格陵兰", "guadeloupe" to "瓜德罗普", "guatemala" to "危地马拉",
            "guinea" to "几内亚", "guyana" to "圭亚那", "haiti" to "海地",
            "honduras" to "洪都拉斯", "hong-kong" to "香港", "hungary" to "匈牙利",
            "iceland" to "冰岛", "india" to "印度", "indonesia" to "印尼",
            "iran" to "伊朗", "iraq" to "伊拉克", "ireland" to "爱尔兰",
            "israel" to "以色列", "italy" to "意大利", "jamaica" to "牙买加",
            "japan" to "日本", "jordan" to "约旦", "kazakhstan" to "哈萨克斯坦",
            "kenya" to "肯尼亚", "kiribati" to "基里巴斯", "kosovo" to "科索沃",
            "kuwait" to "科威特", "kyrgyzstan" to "吉尔吉斯斯坦", "laos" to "老挝",
            "latvia" to "拉脱维亚", "lebanon" to "黎巴嫩", "lesotho" to "莱索托",
            "libya" to "利比亚", "liechtenstein" to "列支敦士登", "lithuania" to "立陶宛",
            "luxembourg" to "卢森堡", "macau" to "澳门", "madagascar" to "马达加斯加",
            "malawi" to "马拉维", "malaysia" to "马来西亚", "maldives" to "马尔代夫",
            "mali" to "马里", "malta" to "马耳他", "marshall-islands" to "马绍尔群岛",
            "mauritania" to "毛里塔尼亚", "mauritius" to "毛里求斯", "mexico" to "墨西哥",
            "micronesia" to "密克罗尼西亚", "moldova" to "摩尔多瓦", "mongolia" to "蒙古",
            "montenegro" to "黑山", "morocco" to "摩洛哥", "mozambique" to "莫桑比克",
            "myanmar" to "缅甸", "netherlands" to "荷兰", "new-zealand" to "新西兰",
            "nepal" to "尼泊尔", "nigeria" to "尼日利亚", "northern-ireland" to "北爱尔兰",
            "norway" to "挪威", "oman" to "阿曼", "pakistan" to "巴基斯坦",
            "panama" to "巴拿马", "peru" to "秘鲁", "philippines" to "菲律宾",
            "poland" to "波兰", "portugal" to "葡萄牙", "qatar" to "卡塔尔",
            "republic-of-the-congo" to "刚果(布)", "romania" to "罗马尼亚", "rwanda" to "卢旺达",
            "saint-lucia" to "圣卢西亚", "saint-vincent-grenadines" to "圣文森特和格林纳丁斯",
            "saudi-arabia" to "沙特阿拉伯", "serbia" to "塞尔维亚", "singapore" to "新加坡",
            "slovakia" to "斯洛伐克", "slovenia" to "斯洛文尼亚", "south-africa" to "南非",
            "south-korea" to "韩国", "spain" to "西班牙", "sri-lanka" to "斯里兰卡",
            "sweden" to "瑞典", "switzerland" to "瑞士", "taiwan" to "台湾",
            "thailand" to "泰国", "togo" to "多哥", "tunisia" to "突尼斯",
            "turkey" to "土耳其", "turks-caicos" to "特克斯和凯科斯群岛", "tuvalu" to "图瓦卢",
            "uganda" to "乌干达", "ukraine" to "乌克兰", "united-arab-emirates" to "阿联酋",
            "united-kingdom" to "英国", "united-states" to "美国", "uruguay" to "乌拉圭",
            "uzbekistan" to "乌兹别克斯坦", "vanuatu" to "瓦努阿图", "venezuela" to "委内瑞拉",
            "vietnam" to "越南", "zambia" to "赞比亚", "zimbabwe" to "津巴布韦"
        )
        return slugToChinese[slug] ?: slug.replace("-", " ").split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
    }
    
    // Determine current background type
    val isCurrentBankCard = currentAssetName.startsWith("bank_card_")
    val isCurrentLightTrail = currentAssetName.endsWith("-lighttrail.jpg")
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Current selection preview
        if (currentAssetName.isNotBlank()) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .size(width = 80.dp, height = 50.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF007AFF), RoundedCornerShape(10.dp))
                ) {
                    val assetDir = when {
                        isCurrentBankCard -> "bank_card_backgrounds"
                        isCurrentLightTrail -> "card_backgrounds"
                        else -> "flag_backgrounds"
                    }
                    AsyncImage(
                        model = "file:///android_asset/$assetDir/$currentAssetName",
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Column {
                    val displayName = when {
                        isCurrentBankCard -> bankCardCountryRemarkMap[currentAssetName] ?: "银行卡"
                        isCurrentLightTrail -> lightTrailCountryName(currentAssetName)
                        else -> currentAssetName.removeSuffix(".jpg")
                    }
                    Text(
                        displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = dk(Color(0xFFE5E5E7), Color(0xFF111827))
                    )
                    Text(
                        if (isCurrentBankCard) "银行卡风格" else "光轨国旗",
                        fontSize = 11.sp,
                        color = Color(0xFF8A94A6)
                    )
                }
            }
        }

        // ========== Light Trail Section ==========
        Text("光轨国（区）旗", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280))
        Text("体育场灯光 + 国旗飘扬效果，共${lightTrailBackgrounds.size}张", fontSize = 11.sp, color = Color(0xFF8A94A6))
        lightTrailBackgrounds.chunked(4).forEach { rowItems ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                rowItems.forEach { fileName ->
                    val isSelected = currentAssetName == fileName
                    val countryName = lightTrailCountryName(fileName)
                    Box(
                        Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                if (isSelected) 2.dp else 0.7.dp,
                                if (isSelected) Color(0xFF007AFF) else dk(Color(0xFF38383A), Color(0xFFE5E7EB)),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { onSelect(fileName) }
                    ) {
                        AsyncImage(
                            model = "file:///android_asset/card_backgrounds/$fileName",
                            contentDescription = countryName,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            Modifier.align(Alignment.BottomStart)
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                countryName,
                                fontSize = 8.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (isSelected) {
                            Box(
                                Modifier.fillMaxSize()
                                    .background(Color(0xFF007AFF).copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("\u2713", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
                repeat(4 - rowItems.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        // ========== Bank Card Section ==========
        if (bankCardBackgrounds.isNotEmpty()) {
            Text("银行卡风格", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280))
            Text("金色边框 + 地标建筑 + 国名，共${bankCardBackgrounds.size}张", fontSize = 11.sp, color = Color(0xFF8A94A6))
            bankCardBackgrounds.chunked(3).forEach { rowItems ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    rowItems.forEach { fileName ->
                        val isSelected = currentAssetName == fileName
                        Box(
                            Modifier.weight(1f).height(54.dp).clip(RoundedCornerShape(10.dp))
                                .border(if(isSelected) 2.dp else 0.7.dp, if(isSelected) Color(0xFF007AFF) else dk(Color(0xFF38383A), Color(0xFFE5E7EB)), RoundedCornerShape(10.dp))
                                .clickable { onSelect(fileName) }
                        ) {
                            AsyncImage(model = "file:///android_asset/bank_card_backgrounds/$fileName", contentDescription = fileName, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            Box(Modifier.align(Alignment.BottomStart).background(Color.Black.copy(alpha=.42f)).padding(horizontal=6.dp,vertical=2.dp)){
                                Text(bankCardCountryRemarkMap[fileName] ?: "银行卡", fontSize=10.sp, fontWeight=FontWeight.SemiBold, color=Color.White, maxLines=1, overflow=TextOverflow.Ellipsis)
                            }
                            if(isSelected){ Box(Modifier.fillMaxSize().background(Color(0xFF007AFF).copy(alpha=.28f)), contentAlignment = Alignment.Center){ Text("\u2713", fontSize=18.sp, fontWeight=FontWeight.Bold, color=Color.White) } }
                        }
                    }
                    repeat(3 - rowItems.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        // Clear button
        if (currentAssetName.isNotBlank()) {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable { onSelect("") }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("清除背景", fontSize = 13.sp, color = Color(0xFF8A94A6))
            }
        }
    }
}

/**
 * Color picker row - 8 preset color circles with checkmark on selected.
 * Returns hex color string like "#3A3A3C"
 */
@Composable
fun ColorPickerRow(
    currentColor: String,
    onColorChanged: (String) -> Unit
) {
    val colors = listOf(
        "#3A3A3C" to "深灰",
        "#007AFF" to "蓝色",
        "#64D2FF" to "青色",
        "#30B0C7" to "蓝绿",
        "#A8DB10" to "青柠",
        "#8CC63F" to "黄绿",
        "#FF9500" to "橙色",
        "#FF3B30" to "红橙"
    )

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        colors.forEach { (hex, label) ->
            val isSelected = hex.equals(currentColor, ignoreCase = true) ||
                    (currentColor.isBlank() && hex == "#3A3A3C")
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(android.graphics.Color.parseColor(hex)))
                        .border(
                            if (isSelected) 2.5.dp else 1.dp,
                            if (isSelected) Color.White else Color(0xFF38383A).copy(alpha = 0.3f),
                            CircleShape
                        )
                        .clickable { onColorChanged(hex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Text(
                            "\u2713",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Text(
                    label,
                    fontSize = 9.sp,
                    color = Color(0xFF8A94A6)
                )
            }
        }
    }
}
