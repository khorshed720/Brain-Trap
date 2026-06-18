package com.example

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

// ==========================================
// 1. DATA MODEL
// ==========================================
data class Question(
    val id: Int,
    val stage: Int, // 1: 1-50, 2: 51-100, 3: 101-150
    val level: Int,
    val questionBn: String,
    val questionEn: String,
    val type: String, // "normal", "trick", "ui_trick", "math_trap", "memory"
    val optionsBn: List<String>,
    val optionsEn: List<String>,
    val correctIndex: Int,
    val isTrick: Boolean,
    val explanationBn: String,
    val explanationEn: String,
    // UI layout customizers for tricky rendering
    val uiiButtonColors: List<Long>? = null, // Custom color hexes to confuse the user
    val uiTargetButtonScale: Int? = null, // Which option index is visually 1.8x larger
    val customTrapHint: String? = null
)

// ==========================================
// 2. SAVED GAME STATE MANAGER (Preferences)
// ==========================================
class GamePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("brain_trap_prefs", Context.MODE_PRIVATE)

    fun getUnlockedLevel(): Int = prefs.getInt("unlocked_level", 1)
    fun setUnlockedLevel(lvl: Int) {
        if (lvl > getUnlockedLevel()) {
            prefs.edit().putInt("unlocked_level", lvl).apply()
        }
    }

    fun getCurrentLevel(): Int = prefs.getInt("current_level", 1)
    fun setCurrentLevel(lvl: Int) {
        prefs.edit().putInt("current_level", lvl).apply()
    }

    fun getScore(): Int = prefs.getInt("total_score", 0)
    fun setScore(score: Int) {
        prefs.edit().putInt("total_score", score).apply()
    }

    fun getLives(): Int = prefs.getInt("remaining_lives", 3)
    fun setLives(lives: Int) {
        prefs.edit().putInt("remaining_lives", lives).apply()
    }

    fun getSoundEnabled(): Boolean = prefs.getBoolean("sound_enabled", true)
    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun getLanguage(): String = "en" // "bn" or "en"
    fun setLanguage(lang: String) {
        prefs.edit().putString("language_code", "en").apply()
    }

    // Power upside wallet representation
    fun getPowerUpCount(name: String): Int {
        return prefs.getInt("powerup_$name", 2) // start with 2 of each
    }
    fun setPowerUpCount(name: String, count: Int) {
        prefs.edit().putInt("powerup_$name", count).apply()
    }

    // Coin system persistence
    fun getCoins(): Int = prefs.getInt("total_coins", 120) // Start with 120 coins
    fun setCoins(amount: Int) {
        prefs.edit().putInt("total_coins", amount).apply()
    }

    // Daily Claim State Management
    fun getLastClaimDay(): Int = prefs.getInt("last_claim_day_num", 0)
    fun setLastClaimDay(day: Int) {
        prefs.edit().putInt("last_claim_day_num", day).apply()
    }
    fun getClaimDayCycle(): Int = prefs.getInt("claim_day_cycle", 1)
    fun setClaimDayCycle(day: Int) {
        prefs.edit().putInt("claim_day_cycle", day).apply()
    }
    fun getLastClaimTime(): Long = prefs.getLong("last_claim_millis", 0L)
    fun setLastClaimTime(time: Long) {
        prefs.edit().putLong("last_claim_millis", time).apply()
    }

    // Unity Ads Configurations
    fun getAdsGameId(): String = prefs.getString("ads_game_id", "800076356") ?: "800076356"
    fun setAdsGameId(id: String) = prefs.edit().putString("ads_game_id", id).apply()

    fun getAdsTestMode(): Boolean = prefs.getBoolean("ads_test_mode", true)
    fun setAdsTestMode(enabled: Boolean) = prefs.edit().putBoolean("ads_test_mode", enabled).apply()

    fun getAdsRewardedPlacement(): String = prefs.getString("ads_reward_placement", "Rewarded_Android") ?: "Rewarded_Android"
    fun setAdsRewardedPlacement(p: String) = prefs.edit().putString("ads_reward_placement", p).apply()

    fun getAdsInterstitialPlacement(): String = prefs.getString("ads_interstitial_placement", "Interstitial_Android") ?: "Interstitial_Android"
    fun setAdsInterstitialPlacement(p: String) = prefs.edit().putString("ads_interstitial_placement", p).apply()

    fun getAdsBannerPlacement(): String = prefs.getString("ads_banner_placement", "Banner_Android") ?: "Banner_Android"
    fun setAdsBannerPlacement(p: String) = prefs.edit().putString("ads_banner_placement", p).apply()
}

// ==========================================
// 3. DYNAMIC & PROCEDURAL LEVEL GENERATOR (150 Levels)
// ==========================================
object QuestionManager {

    // 25 Curated masterpiece questions
    private val curatedQuestions = listOf(
        Question(
            id = 1, stage = 1, level = 1,
            questionBn = "What is a baby cat called?",
            questionEn = "What is a baby cat called?",
            type = "trick",
            optionsBn = listOf("DOG", "KIT", "CALF", "MITTY"),
            optionsEn = listOf("DOG", "KIT", "CALF", "MITTY"),
            correctIndex = 1,
            isTrick = true,
            explanationBn = "KIT is correct, but MITTY sounds like 'Kitty', acting as a smart visual bait!",
            explanationEn = "KIT is correct, but MITTY sounds like 'Kitty', acting as a smart visual bait!"
        ),
        Question(
            id = 2, stage = 1, level = 2,
            questionBn = "What is 1 + 1?",
            questionEn = "What is 1 + 1?",
            type = "math_trap",
            optionsBn = listOf("2", "11", "3", "0"),
            optionsEn = listOf("2", "11", "3", "0"),
            correctIndex = 1,
            isTrick = true,
            explanationBn = "Placing 1 next to another 1 side-by-side literally makes 11!",
            explanationEn = "Placing 1 next to another 1 side-by-side literally makes 11!"
        ),
        Question(
            id = 3, stage = 1, level = 3,
            questionBn = "What must you do at a red signal light?",
            questionEn = "What must you do at a red signal light?",
            type = "normal",
            optionsBn = listOf("Accelerate speed", "Ignore signal", "Stop vehicles (STOP)", "Honk horn"),
            optionsEn = listOf("Accelerate speed", "Ignore signal", "Stop vehicles (STOP)", "Honk horn"),
            correctIndex = 2,
            isTrick = false,
            explanationBn = "Red light means you must stop. A straightforward, honest question!",
            explanationEn = "Red light means you must stop. A straightforward, honest question!"
        ),
        Question(
            id = 4, stage = 1, level = 4,
            questionBn = "Which month has 28 days?",
            questionEn = "Which month has 28 days?",
            type = "trick",
            optionsBn = listOf("February", "January", "December", "All 12 Months"),
            optionsEn = listOf("February", "January", "December", "All 12 Months"),
            correctIndex = 3,
            isTrick = true,
            explanationBn = "Every single month of the year has at least 28 days! Double-trap logic.",
            explanationEn = "Every single month of the year has at least 28 days! Double-trap logic."
        ),
        Question(
            id = 5, stage = 1, level = 5,
            questionBn = "If you overtake the person in 2nd place in a race, what position are you in?",
            questionEn = "If you overtake the person in 2nd place in a race, what position are you in?",
            type = "trick",
            optionsBn = listOf("1st place", "2nd place", "3rd place", "Last place"),
            optionsEn = listOf("1st place", "2nd place", "3rd place", "Last place"),
            correctIndex = 1,
            isTrick = true,
            explanationBn = "By passing the runner in 2nd place, you take their spot, remaining in 2nd!",
            explanationEn = "By passing the runner in 2nd place, you take their spot, remaining in 2nd!"
        ),
        Question(
            id = 6, stage = 1, level = 6,
            questionBn = "5 birds on a branch. A hunter shoots and kills 1. How many birds are left on the branch?",
            questionEn = "5 birds on a branch. A hunter shoots and kills 1. How many birds are left on the branch?",
            type = "math_trap",
            optionsBn = listOf("4 birds", "0 (None)", "5 birds", "1 bird"),
            optionsEn = listOf("4 birds", "0 (None)", "5 birds", "1 bird"),
            correctIndex = 1,
            isTrick = true,
            explanationBn = "At the terrifying sound of the gunshot, the remaining birds flew away instantly!",
            explanationEn = "At the terrifying sound of the gunshot, the remaining birds flew away instantly!"
        ),
        Question(
            id = 7, stage = 1, level = 7,
            questionBn = "In the word 'ENGLISH', which letter comes directly after 'E'?",
            questionEn = "In the word 'ENGLISH', which letter comes directly after 'E'?",
            type = "trick",
            optionsBn = listOf("F", "N", "G", "R"),
            optionsEn = listOf("F", "N", "G", "R"),
            correctIndex = 1,
            isTrick = true,
            explanationBn = "In the letters of the alphabet, F follows E. But in the spelling of 'ENGLISH', N follows E!",
            explanationEn = "In the letters of the alphabet, F follows E. But in the spelling of 'ENGLISH', N follows E!"
        ),
        Question(
            id = 8, stage = 1, level = 8,
            questionBn = "If you make a tight closed fist with your hand, how many fingernails can you see?",
            questionEn = "If you make a tight closed fist with your hand, how many fingernails can you see?",
            type = "trick",
            optionsBn = listOf("5 nails", "0 nails", "1 nail (Thumb)", "10 nails"),
            optionsEn = listOf("5 nails", "0 nails", "1 nail (Thumb)", "10 nails"),
            correctIndex = 2,
            isTrick = true,
            explanationBn = "Only your thumb's fingernail remains visible on the outside of a closed fist.",
            explanationEn = "Only your thumb's fingernail remains visible on the outside of a closed fist."
        ),
        Question(
            id = 9, stage = 1, level = 9,
            questionBn = "Click the BLUE button!",
            questionEn = "Click the BLUE button!",
            type = "ui_trick",
            optionsBn = listOf("Blue Text (Colored Red)", "Red Text (Colored Blue)", "Yellow Button", "Green Button"),
            optionsEn = listOf("Blue Text (Colored Red)", "Red Text (Colored Blue)", "Yellow Button", "Green Button"),
            correctIndex = 1,
            isTrick = true,
            explanationBn = "Don't fall for the text label! You had to click the button physically styled in Blue.",
            explanationEn = "Don't fall for the text label! You had to click the button physically styled in Blue."
        ),
        Question(
            id = 10, stage = 1, level = 10,
            questionBn = "How many sides does a perfect circle have?",
            questionEn = "How many sides does a perfect circle have?",
            type = "trick",
            optionsBn = listOf("1 side", "0 sides", "2 sides (In & Out)", "Infinite"),
            optionsEn = listOf("1 side", "0 sides", "2 sides (In & Out)", "Infinite"),
            correctIndex = 2,
            isTrick = true,
            explanationBn = "A standard circle has exactly two sides: the inside and the outside!",
            explanationEn = "A standard circle has exactly two sides: the inside and the outside!"
        ),
        Question(
            id = 11, stage = 1, level = 11,
            questionBn = "How do you drop an egg on a concrete floor without cracking it?",
            questionEn = "How do you drop an egg on a concrete floor without cracking it?",
            type = "trick",
            optionsBn = listOf("Use a foam sheet", "Use a cushion", "Concrete won't break, drop it!", "Boil the egg first"),
            optionsEn = listOf("Use a foam sheet", "Use a cushion", "Concrete won't break, drop it!", "Boil the egg first"),
            correctIndex = 2,
            isTrick = true,
            explanationBn = "Concrete floors are so durable that dropping an egg will never crack the steel-hard floor!",
            explanationEn = "Concrete floors are so durable that dropping an egg will never crack the steel-hard floor!"
        ),
        Question(
            id = 12, stage = 1, level = 12,
            questionBn = "What gets wetter the more it dries?",
            questionEn = "What gets wetter the more it dries?",
            type = "normal",
            optionsBn = listOf("Sponge", "Towel", "River Water", "Rain Cloud"),
            optionsEn = listOf("Sponge", "Towel", "River Water", "Rain Cloud"),
            correctIndex = 1,
            isTrick = false,
            explanationBn = "A bath towel absorbs moisture from your body code, getting wet in the drying action.",
            explanationEn = "A bath towel absorbs moisture from your body code, getting wet in the drying action."
        ),
        Question(
            id = 13, stage = 1, level = 13,
            questionBn = "What hatches from a peacock egg?",
            questionEn = "What hatches from a peacock egg?",
            type = "trick",
            optionsBn = listOf("Baby peacock", "Baby peahen", "Nothing (Peacocks don't lay)", "Cute swan"),
            optionsEn = listOf("Baby peacock", "Baby peahen", "Nothing (Peacocks don't lay)", "Cute swan"),
            correctIndex = 2,
            isTrick = true,
            explanationBn = "Peacocks are male birds. They do not spawn eggs—only peahens do!",
            explanationEn = "Peacocks are male birds. They do not spawn eggs—only peahens do!"
        ),
        Question(
            id = 14, stage = 1, level = 14,
            questionBn = "You have 3 apples. If you take away 2 of them, how many do you have now?",
            questionEn = "You have 3 apples. If you take away 2 of them, how many do you have now?",
            type = "trick",
            optionsBn = listOf("1 apple", "2 apples (you took)", "3 apples", "0"),
            optionsEn = listOf("1 apple", "2 apples (you took)", "3 apples", "0"),
            correctIndex = 1,
            isTrick = true,
            explanationBn = "Since you took those 2 apples yourself, they are physically in your possession!",
            explanationEn = "Since you took those 2 apples yourself, they are physically in your possession!"
        ),
        Question(
            id = 15, stage = 1, level = 15,
            questionBn = "Which lives in water but is biologically NOT classified as a fish?",
            questionEn = "Which lives in water but is biologically NOT classified as a fish?",
            type = "normal",
            optionsBn = listOf("Whale", "Shrimp", "Frog", "All of these"),
            optionsEn = listOf("Whale", "Shrimp", "Frog", "All of these"),
            correctIndex = 3,
            isTrick = false,
            explanationBn = "Whales are mammals, shrimps are crustaceans, frogs are amphibians. None are taxonomic fish!",
            explanationEn = "Whales are mammals, shrimps are crustaceans, frogs are amphibians. None are taxonomic fish!"
        ),
        // Stage 2 Curated Sample Questions (Level 51-55)
        Question(
            id = 16, stage = 2, level = 51,
            questionBn = "Click the largest number button below!",
            questionEn = "Click the largest number button below!",
            type = "ui_trick",
            optionsBn = listOf("999", "1000", "7 (Massive element)", "8888"),
            optionsEn = listOf("999", "1000", "7 (Massive element)", "8888"),
            correctIndex = 2,
            isTrick = true,
            explanationBn = "The value '7' button is visually scaled three times larger than the rest!",
            explanationEn = "The value '7' button is visually scaled three times larger than the rest!"
        ),
        Question(
            id = 17, stage = 2, level = 52,
            questionBn = "Botanically, which of these is NOT a vegetable, but a fruit?",
            questionEn = "Botanically, which of these is NOT a vegetable, but a fruit?",
            type = "normal",
            optionsBn = listOf("Carrot", "Garlic", "Tomato", "Potato"),
            optionsEn = listOf("Carrot", "Garlic", "Tomato", "Potato"),
            correctIndex = 2,
            isTrick = false,
            explanationBn = "Because tomatoes contain seeds, they are botanically classified as berries/fruits!",
            explanationEn = "Because tomatoes contain seeds, they are botanically classified as berries/fruits!"
        ),
        Question(
            id = 18, stage = 2, level = 53,
            questionBn = "Click the green button, careful!",
            questionEn = "Click the green button, careful!",
            type = "ui_trick",
            optionsBn = listOf("Green (Colored Yellow)", "Yellow (Colored Cyan)", "Click Me (Colored Green)", "Grey (Colored Red)"),
            optionsEn = listOf("Green (Colored Yellow)", "Yellow (Colored Cyan)", "Click Me (Colored Green)", "Grey (Colored Red)"),
            correctIndex = 2,
            isTrick = true,
            explanationBn = "The 3rd button background is colored solid green, don't read text!",
            explanationEn = "The 3rd button background is colored solid green, don't read text!"
        ),
        Question(
            id = 19, stage = 2, level = 54,
            questionBn = "Twelve months make a year. How many months have 30 days?",
            questionEn = "Twelve months make a year. How many months have 30 days?",
            type = "trick",
            optionsBn = listOf("4 months", "5 months", "11 months", "12 months"),
            optionsEn = listOf("4 months", "5 months", "11 months", "12 months"),
            correctIndex = 2,
            isTrick = true,
            explanationBn = "Excluding February, 11 months of the year have at least 30 days inside!",
            explanationEn = "Excluding February, 11 months of the year have at least 30 days inside!"
        ),
        Question(
            id = 20, stage = 2, level = 55,
            questionBn = "5 cakes take 5 minutes to bake. How long does it take for 100 ovens to bake 100 cakes simultaneously?",
            questionEn = "5 cakes take 5 minutes to bake. How long does it take for 100 ovens to bake 100 cakes simultaneously?",
            type = "math_trap",
            optionsBn = listOf("100 minutes", "5 minutes (Same time)", "50 minutes", "500 minutes"),
            optionsEn = listOf("100 minutes", "5 minutes (Same time)", "50 minutes", "500 minutes"),
            correctIndex = 1,
            isTrick = true,
            explanationBn = "Because every cake has its own oven, all will capture exactly 5 minutes together!",
            explanationEn = "Because every cake has its own oven, all will capture exactly 5 minutes together!"
        ),
        // Stage 3 Curated Sample Questions (Level 101-105)
        Question(
            id = 21, stage = 3, level = 101,
            questionBn = "Tap the button named 'FALSE'!",
            questionEn = "Tap the button named 'FALSE'!",
            type = "ui_trick",
            optionsBn = listOf("TRUE (Tap here)", "OK", "FALSE", "CANCEL"),
            optionsEn = listOf("TRUE (Tap here)", "OK", "FALSE", "CANCEL"),
            correctIndex = 0,
            isTrick = true,
            explanationBn = "Mind-twist: Clicking 'FALSE' triggers a trap, you actually had to tap 'TRUE'!",
            explanationEn = "Mind-twist: Clicking 'FALSE' triggers a trap, you actually had to tap 'TRUE'!"
        ),
        Question(
            id = 22, stage = 3, level = 102,
            questionBn = "How many letters are in the alphabet?",
            questionEn = "How many letters are in the alphabet?",
            type = "trick",
            optionsBn = listOf("26 letters", "24 letters", "11 letters", "12 letters"),
            optionsEn = listOf("26 letters", "24 letters", "11 letters", "12 letters"),
            correctIndex = 2,
            isTrick = true,
            explanationBn = "The phrase 'the alphabet' consists of exactly 11 letters!",
            explanationEn = "The phrase 'the alphabet' consists of exactly 11 letters!"
        ),
        Question(
            id = 23, stage = 3, level = 103,
            questionBn = "[Memory Test] What was the correct answer of the previous level?",
            questionEn = "[Memory Test] What was the correct answer of the previous level?",
            type = "memory",
            optionsBn = listOf("26 letters", "24 letters", "11 letters", "12 letters"),
            optionsEn = listOf("26 letters", "24 letters", "11 letters", "12 letters"),
            correctIndex = 2,
            isTrick = true,
            explanationBn = "Memory test! The correct answer of Level 102 was '11 letters'!",
            explanationEn = "Memory test! The correct answer of Level 102 was '11 letters'!"
        ),
        Question(
            id = 24, stage = 3, level = 104,
            questionBn = "If 1 dozen bananas cost 30 Taka, how much is half a dozen of bananas?",
            questionEn = "If 1 dozen bananas cost 30 Taka, how much is half a dozen of bananas?",
            type = "math_trap",
            optionsBn = listOf("15 Taka", "60 Taka", "5 Taka", "100 Taka"),
            optionsEn = listOf("15 Taka", "60 Taka", "5 Taka", "100 Taka"),
            correctIndex = 0,
            isTrick = false,
            explanationBn = "Half of 30 is 15. The puzzle is thinking there is a trick when there isn't!",
            explanationEn = "Half of 30 is 15. The puzzle is thinking there is a trick when there isn't!"
        ),
        Question(
            id = 25, stage = 3, level = 105,
            questionBn = "3 electrical wires are required to light 1 bulb. How many wires are needed to light 9 bulbs in series?",
            questionEn = "3 electrical wires are required to light 1 bulb. How many wires are needed to light 9 bulbs in series?",
            type = "math_trap",
            optionsBn = listOf("27 wires", "3 wires (Series connection)", "9 wires", "18 wires"),
            optionsEn = listOf("27 wires", "3 wires (Series connection)", "9 wires", "18 wires"),
            correctIndex = 1,
            isTrick = true,
            explanationBn = "Connected end-to-end in a smart series circuit, only 3 wire loops run all 9 bulbs!",
            explanationEn = "Connected end-to-end in a smart series circuit, only 3 wire loops run all 9 bulbs!"
        )
    )

    /**
     * Retrieves a full programmatically styled Question object for any level from 1 to 150.
     * Maps to hand-crafted curated questions or generates highly clever math, memory, logical and ui traps!
     */
    fun getQuestionForLevel(level: Int): Question {
        // Stage distribution
        val stage = when {
            level <= 50 -> 1
            level <= 100 -> 2
            else -> 3
        }

        // Try mapping to curated list first
        val curated = curatedQuestions.find { it.level == level }
        if (curated != null) {
            return curated.copy(
                questionBn = curated.questionEn,
                optionsBn = curated.optionsEn,
                explanationBn = curated.explanationEn
            )
        }

        // Deterministic template configuration to prevent repetitions
        val templateId = level % 18
        return when (templateId) {
            0 -> {
                val num1 = level * 2 + 5
                val num2 = level + 1
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "A farmer has $num1 sheep. All but $num2 died. How many are left?",
                    questionEn = "A farmer has $num1 sheep. All but $num2 died. How many are left?",
                    type = "trick",
                    optionsBn = listOf("${num1 - num2}", "$num2", "0", "$num1"),
                    optionsEn = listOf("${num1 - num2}", "$num2", "0", "$num1"),
                    correctIndex = 1,
                    isTrick = true,
                    explanationBn = "The phrase 'All but $num2 died' means exactly $num2 sheep survived!",
                    explanationEn = "The phrase 'All but $num2 died' means exactly $num2 sheep survived!"
                )
            }
            1 -> {
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "A doctor gives you 3 pills and tells you to take one every 30 minutes. How long do they last in minutes?",
                    questionEn = "A doctor gives you 3 pills and tells you to take one every 30 minutes. How long do they last in minutes?",
                    type = "math_trap",
                    optionsBn = listOf("90", "60", "30", "120"),
                    optionsEn = listOf("90", "60", "30", "120"),
                    correctIndex = 1,
                    isTrick = true,
                    explanationBn = "You take the first pill at 0 minutes, the second at 30 minutes, and the third at 60 minutes!",
                    explanationEn = "You take the first pill at 0 minutes, the second at 30 minutes, and the third at 60 minutes!"
                )
            }
            2 -> {
                val base = level * 10
                val addVal = level
                val ans = base * 2 + addVal
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "If you divide $base by half (0.5) and add $addVal, what is the answer?",
                    questionEn = "If you divide $base by half (0.5) and add $addVal, what is the answer?",
                    type = "math_trap",
                    optionsBn = listOf("$ans", "${base / 2 + addVal}", "${base + addVal}", "None of these"),
                    optionsEn = listOf("$ans", "${base / 2 + addVal}", "${base + addVal}", "None of these"),
                    correctIndex = 0,
                    isTrick = true,
                    explanationBn = "Dividing a number by 0.5 is mathematically equivalent to multiplying it by 2!",
                    explanationEn = "Dividing a number by 0.5 is mathematically equivalent to multiplying it by 2!"
                )
            }
            3 -> {
                val countCrows = level + 4
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "There are $countCrows crows on a fence. A hunter shoots one. How many crows are left?",
                    questionEn = "There are $countCrows crows on a fence. A hunter shoots one. How many crows are left?",
                    type = "trick",
                    optionsBn = listOf("${countCrows - 1}", "0", "1", "$countCrows"),
                    optionsEn = listOf("${countCrows - 1}", "0", "1", "$countCrows"),
                    correctIndex = 1,
                    isTrick = true,
                    explanationBn = "The sudden gunshot scared all the remaining crows away, leaving zero!",
                    explanationEn = "The sudden gunshot scared all the remaining crows away, leaving zero!"
                )
            }
            4 -> {
                val weight = level * 5
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "Tap the visually heaviest item in reality!",
                    questionEn = "Tap the visually heaviest item in reality!",
                    type = "ui_trick",
                    optionsBn = listOf("Feather (9000 Tons)", "Sponge (1000 kg)", "Lead Bullet ($weight Grams)", "Paper Plane (1 kg)"),
                    optionsEn = listOf("Feather (9000 Tons)", "Sponge (1000 kg)", "Lead Bullet ($weight Grams)", "Paper Plane (1 kg)"),
                    correctIndex = 0,
                    isTrick = true,
                    explanationBn = "Although a feather is normally light, a 9000 Ton feather weighs the most!",
                    explanationEn = "Although a feather is normally light, a 9000 Ton feather weighs the most!"
                )
            }
            5 -> {
                val numApples = level + 2
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "If there are $numApples apples and you take away 3 apples, how many apples do you have?",
                    questionEn = "If there are $numApples apples and you take away 3 apples, how many apples do you have?",
                    type = "trick",
                    optionsBn = listOf("3", "${numApples - 3}", "$numApples", "0"),
                    optionsEn = listOf("3", "${numApples - 3}", "$numApples", "0"),
                    correctIndex = 0,
                    isTrick = true,
                    explanationBn = "Since you took those 3 apples yourself, they are physically in your possession!",
                    explanationEn = "Since you took those 3 apples yourself, they are physically in your possession!"
                )
            }
            6 -> {
                val mult = level % 5 + 3
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "How many times can you subtract $mult from ${mult * 10}?",
                    questionEn = "How many times can you subtract $mult from ${mult * 10}?",
                    type = "trick",
                    optionsBn = listOf("10", "Only once", "Infinite", "9"),
                    optionsEn = listOf("10", "Only once", "Infinite", "9"),
                    correctIndex = 1,
                    isTrick = true,
                    explanationBn = "After you subtract it once, you are no longer subtracting it from ${mult * 10}!",
                    explanationEn = "After you subtract it once, you are no longer subtracting it from ${mult * 10}!"
                )
            }
            7 -> {
                val seedWords = listOf("BRAIN", "TRAP", "MIND", "LEVEL", "QUIZ", "COSMIC", "STRIKE", "PUZZLE")
                val word = seedWords[level % seedWords.size]
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "Which is the last letter in the word: '$word'?",
                    questionEn = "Which is the last letter in the word: '$word'?",
                    type = "trick",
                    optionsBn = listOf("${word.first()}", "${word.last()}", "Y", "None of these"),
                    optionsEn = listOf("${word.first()}", "${word.last()}", "Y", "None of these"),
                    correctIndex = 1,
                    isTrick = false,
                    explanationBn = "A straightforward check of the ending letter of '$word' is '${word.last()}'!",
                    explanationEn = "A straightforward check of the ending letter of '$word' is '${word.last()}'!"
                )
            }
            8 -> {
                val speed = level + 45
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "An electric train is traveling North at $speed mph. Which way does the smoke blow?",
                    questionEn = "An electric train is traveling North at $speed mph. Which way does the smoke blow?",
                    type = "trick",
                    optionsBn = listOf("South", "No Direction", "North-West", "East"),
                    optionsEn = listOf("South", "No Direction", "North-West", "East"),
                    correctIndex = 1,
                    isTrick = true,
                    explanationBn = "Electric trains do not generate any smoke!",
                    explanationEn = "Electric trains do not generate any smoke!"
                )
            }
            9 -> {
                val matchCount = level % 4 + 2
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "You have $matchCount matchsticks. You enter a freezing dark cabin with a fireplace, a candle, and a lamp. What do you light first?",
                    questionEn = "You have $matchCount matchsticks. You enter a freezing dark cabin with a fireplace, a candle, and a lamp. What do you light first?",
                    type = "trick",
                    optionsBn = listOf("Fireplace", "Candle", "Lamp", "A Matchstick"),
                    optionsEn = listOf("Fireplace", "Candle", "Lamp", "A Matchstick"),
                    correctIndex = 3,
                    isTrick = true,
                    explanationBn = "Before you can light anything else, you must light a matchstick first!",
                    explanationEn = "Before you can light anything else, you must light a matchstick first!"
                )
            }
            10 -> {
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "What starts with 'E', ends with 'E', but only contains one single letter?",
                    questionEn = "What starts with 'E', ends with 'E', but only contains one single letter?",
                    type = "trick",
                    optionsBn = listOf("Envelope", "Eye", "Engine", "Eagle"),
                    optionsEn = listOf("Envelope", "Eye", "Engine", "Eagle"),
                    correctIndex = 0,
                    isTrick = true,
                    explanationBn = "An envelope starts with E, ends with E, and physically holds a single letter!",
                    explanationEn = "An envelope starts with E, ends with E, and physically holds a single letter!"
                )
            }
            11 -> {
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "What has hands but cannot clap?",
                    questionEn = "What has hands but cannot clap?",
                    type = "normal",
                    optionsBn = listOf("A Clock", "A Table", "A Glove", "A Mirror"),
                    optionsEn = listOf("A Clock", "A Table", "A Glove", "A Mirror"),
                    correctIndex = 0,
                    isTrick = false,
                    explanationBn = "A standard clock has a minute and hour hand but cannot physically clap!",
                    explanationEn = "A standard clock has a minute and hour hand but cannot physically clap!"
                )
            }
            12 -> {
                val legs = level % 3 + 4
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "A normal table has $legs legs. If you cut off two of its legs, how many does it have left?",
                    questionEn = "A normal table has $legs legs. If you cut off two of its legs, how many does it have left?",
                    type = "trick",
                    optionsBn = listOf("${legs - 2}", "Its 2 cut off legs", "$legs", "0"),
                    optionsEn = listOf("${legs - 2}", "Its 2 cut off legs", "$legs", "0"),
                    correctIndex = 2,
                    isTrick = true,
                    explanationBn = "It still has its $legs legs, except two of them are now laying on the floor!",
                    explanationEn = "It still has its $legs legs, except two of them are now laying on the floor!"
                )
            }
            13 -> {
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "What goes up and down but never actually moves?",
                    questionEn = "What goes up and down but never actually moves?",
                    type = "trick",
                    optionsBn = listOf("A Staircase", "A Ball", "An Elevator", "Wind"),
                    optionsEn = listOf("A Staircase", "A Ball", "An Elevator", "Wind"),
                    correctIndex = 0,
                    isTrick = true,
                    explanationBn = "Staircases lead both up and down but remain completely stationary!",
                    explanationEn = "Staircases lead both up and down but remain completely stationary!"
                )
            }
            14 -> {
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "Which word in the dictionary is spelled incorrectly?",
                    questionEn = "Which word in the dictionary is spelled incorrectly?",
                    type = "trick",
                    optionsBn = listOf("Incorrectly", "Puzzled", "Falsehood", "None of these"),
                    optionsEn = listOf("Incorrectly", "Puzzled", "Falsehood", "None of these"),
                    correctIndex = 0,
                    isTrick = true,
                    explanationBn = "The word 'Incorrectly' is literally spelled 'I-n-c-o-r-r-e-c-t-l-y'!",
                    explanationEn = "The word 'Incorrectly' is literally spelled 'I-n-c-o-r-r-e-c-t-l-y'!"
                )
            }
            15 -> {
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "Click the YELLOW button!",
                    questionEn = "Click the YELLOW button!",
                    type = "ui_trick",
                    optionsBn = listOf("Yellow (Colored Red)", "Red (Colored Yellow)", "Blue (Colored Green)", "Green (Colored Blue)"),
                    optionsEn = listOf("Yellow (Colored Red)", "Red (Colored Yellow)", "Blue (Colored Green)", "Green (Colored Blue)"),
                    correctIndex = 1,
                    isTrick = true,
                    explanationBn = "The second option button background is physically shaped and colored yellow!",
                    explanationEn = "The second option button background is physically shaped and colored yellow!"
                )
            }
            16 -> {
                val age = level + 7
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "What goes up when you are $age years old but never comes back down?",
                    questionEn = "What goes up when you are $age years old but never comes back down?",
                    type = "normal",
                    optionsBn = listOf("Your Age", "Height", "Weight", "Temperature"),
                    optionsEn = listOf("Your Age", "Height", "Weight", "Temperature"),
                    correctIndex = 0,
                    isTrick = false,
                    explanationBn = "Your chronological age only increases and can never decrease!",
                    explanationEn = "Your chronological age only increases and can never decrease!"
                )
            }
            else -> {
                Question(
                    id = level, stage = stage, level = level,
                    questionBn = "Solve the letters sum logic: What is the word for 'Mind Twist'?",
                    questionEn = "Solve the letters sum logic: What is the word for 'Mind Twist'?",
                    type = "trick",
                    optionsBn = listOf("TRAP", "MIND", "WORDS", "PUZZLE"),
                    optionsEn = listOf("TRAP", "MIND", "WORDS", "PUZZLE"),
                    correctIndex = 3,
                    isTrick = false,
                    explanationBn = "A puzzle is indeed a perfect match for a mind twist riddle!",
                    explanationEn = "A puzzle is indeed a perfect match for a mind twist riddle!"
                )
            }
        }
    }
}

// Particle representation for celebration confetti
data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val speedX: Float,
    val speedY: Float,
    val colorHex: Long,
    val size: Float
)

// ==========================================
// 4. GAME VIEW MODEL (The Dynamic Gameplay Brain)
// ==========================================
class GameViewModel(private val context: Context) : ViewModel() {

    private val prefs = GamePreferences(context)

    // Observable states
    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Confetti active collection
    val confettiList = mutableStateListOf<ConfettiParticle>()

    // Power-up count flows
    private val _powerUpsXRay = MutableStateFlow(prefs.getPowerUpCount("xray"))
    val powerUpsXRay = _powerUpsXRay.asStateFlow()

    private val _powerUpsFreeze = MutableStateFlow(prefs.getPowerUpCount("freeze"))
    val powerUpsFreeze = _powerUpsFreeze.asStateFlow()

    private val _powerUpsBomb = MutableStateFlow(prefs.getPowerUpCount("bomb"))
    val powerUpsBomb = _powerUpsBomb.asStateFlow()

    private val _powerUpsSkip = MutableStateFlow(prefs.getPowerUpCount("skip"))
    val powerUpsSkip = _powerUpsSkip.asStateFlow()

    private val _powerUpsShield = MutableStateFlow(prefs.getPowerUpCount("shield"))
    val powerUpsShield = _powerUpsShield.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(prefs.getSoundEnabled())
    val isSoundEnabled = _isSoundEnabled.asStateFlow()

    private val _language = MutableStateFlow(prefs.getLanguage())
    val language = _language.asStateFlow()

    private val _unlockedLevel = MutableStateFlow(prefs.getUnlockedLevel())
    val unlockedLevel = _unlockedLevel.asStateFlow()

    private val _coins = MutableStateFlow(prefs.getCoins())
    val coins = _coins.asStateFlow()

    // Internals
    private var timerJob: Job? = null
    private var uiTrickTimerJob: Job? = null

    init {
        SoundManager.setEnabled(prefs.getSoundEnabled())
        loadLevel(prefs.getCurrentLevel())
    }

    // Toggle controllers
    fun toggleSound() {
        val next = !_isSoundEnabled.value
        prefs.setSoundEnabled(next)
        _isSoundEnabled.value = next
        SoundManager.setEnabled(next)
        SoundManager.playClick()
    }

    fun toggleLanguage() {
        val next = if (_language.value == "bn") "en" else "bn"
        prefs.setLanguage(next)
        _language.value = next
    }

    // Ads helper configurations
    fun getAdsGameId(): String = prefs.getAdsGameId()
    fun getAdsTestMode(): Boolean = prefs.getAdsTestMode()
    fun getAdsRewardedPlacement(): String = prefs.getAdsRewardedPlacement()
    fun getAdsInterstitialPlacement(): String = prefs.getAdsInterstitialPlacement()
    fun getAdsBannerPlacement(): String = prefs.getAdsBannerPlacement()

    fun saveAdsConfig(gameId: String, testMode: Boolean, rewarded: String, interstitial: String, banner: String) {
        prefs.setAdsGameId(gameId)
        prefs.setAdsTestMode(testMode)
        prefs.setAdsRewardedPlacement(rewarded)
        prefs.setAdsInterstitialPlacement(interstitial)
        prefs.setAdsBannerPlacement(banner)
    }

    // Load level values
    fun loadLevel(levelNum: Int) {
        if (levelNum > 150) {
            // Congratulate completed game!
            _uiState.update {
                it.copy(
                    isGameCompleted = true,
                    currentLevelNumber = 150
                )
            }
            return
        }

        prefs.setCurrentLevel(levelNum)
        val question = QuestionManager.getQuestionForLevel(levelNum)

        // Stage time limits
        val countdown = when (question.stage) {
            1 -> 20
            2 -> 15
            else -> 10
        }

        _uiState.update {
            GameUiState(
                currentLevelNumber = levelNum,
                currentStageNumber = question.stage,
                currentQuestion = question,
                timeLeft = countdown,
                totalLevelTime = countdown,
                score = prefs.getScore(),
                coins = prefs.getCoins(),
                lives = prefs.getLives(),
                screenType = ScreenType.GAME_SCREEN,
                isXRayActive = false,
                isBombActive = false,
                isShieldActive = false,
                isFreezeActive = false,
                showExplanation = false,
                selectedOptionIndex = -1,
                revealedIncorrectIndices = emptySet(),
                uiTrickState = UiTrickState()
            )
        }

        startTimer()
        startUiTrickTimerIfRequired(question)
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0 && _uiState.value.screenType == ScreenType.GAME_SCREEN && !_uiState.value.isFreezeActive) {
                delay(1000)
                if (!_uiState.value.isFreezeActive) {
                    _uiState.update {
                        it.copy(timeLeft = it.timeLeft - 1)
                    }
                }
            }

            if (_uiState.value.timeLeft <= 0 && !_uiState.value.isFreezeActive) {
                handleTimeOut()
            }
        }
    }

    private fun startUiTrickTimerIfRequired(question: Question) {
        uiTrickTimerJob?.cancel()
        if (question.type == "ui_trick") {
            uiTrickTimerJob = viewModelScope.launch {
                var ticks = 0
                val rand = Random(4721)
                while (_uiState.value.screenType == ScreenType.GAME_SCREEN) {
                    delay(1200)
                    ticks++
                    // For UI tick questions, simulate button moves, random offsets, or colors changing!
                    _uiState.update { cur ->
                        val currentStage = cur.currentStageNumber
                        val modifier = if (currentStage >= 3) 140 else 70
                        if (cur.currentQuestion?.type == "ui_trick") {
                            val nextOffsets = List(4) { idx ->
                                // Drift buttons offset coordinates randomly to trigger "UI Trick" movement
                                if (ticks % 2 == 0) {
                                    OffsetPair(
                                        x = rand.nextInt(-modifier, modifier).toFloat(),
                                        y = rand.nextInt(-30, 30).toFloat()
                                    )
                                } else {
                                    OffsetPair(0f, 0f)
                                }
                            }
                            cur.copy(
                                uiTrickState = cur.uiTrickState.copy(
                                    buttonOffsets = nextOffsets,
                                    labelColorSwap = ticks % 2 == 0
                                )
                            )
                        } else {
                            cur
                        }
                    }
                }
            }
        }
    }

    private fun handleTimeOut() {
        SoundManager.playWrong()
        val isFirstFail = true // No shield
        val livesLeft = _uiState.value.lives - 1
        prefs.setLives(livesLeft)
        prefs.setScore((prefs.getScore() - 10).coerceAtLeast(0))

        _uiState.update {
            it.copy(
                lives = livesLeft,
                score = prefs.getScore(),
                coins = prefs.getCoins(),
                screenType = ScreenType.RESULT_SCREEN,
                isCorrectAnswer = false,
                explanationTitle = "Time Out"
            )
        }
    }

    // Submit user selection
    fun selectOption(index: Int) {
        val state = _uiState.value
        if (state.screenType != ScreenType.GAME_SCREEN) return

        timerJob?.cancel()
        uiTrickTimerJob?.cancel()

        val q = state.currentQuestion ?: return
        val isCorrect = (index == q.correctIndex)

        if (isCorrect) {
            SoundManager.playCorrect()
            // Speed bonus
            val speedBonus = if (state.timeLeft >= (state.totalLevelTime - 5)) 5 else 0
            val rewardScore = 10 + speedBonus + 20 // +10 for correct, +20 for level pass, +speedBonus
            val nextScore = state.score + rewardScore
            prefs.setScore(nextScore)

            // Coin system reward (+15 Coins per correct game completed!)
            val nextCoins = prefs.getCoins() + 15
            prefs.setCoins(nextCoins)
            _coins.value = nextCoins

            // Unlock progression
            val currentLvl = state.currentLevelNumber
            val nextLvl = currentLvl + 1
            prefs.setUnlockedLevel(nextLvl)
            _unlockedLevel.value = prefs.getUnlockedLevel()

            // Trigger visual particles
            spawnConfetti()

            _uiState.update {
                it.copy(
                    score = nextScore,
                    coins = nextCoins,
                    selectedOptionIndex = index,
                    isCorrectAnswer = true,
                    screenType = ScreenType.RESULT_SCREEN,
                    explanationTitle = "Correct Answer",
                    lastAnswerSpeedBonus = speedBonus
                )
            }
        } else {
            // Wrong answer! Shield checks
            if (state.isShieldActive) {
                // Play correct chime to celebrate shield survival!
                SoundManager.playCorrect()
                // Forgive error!
                _uiState.update {
                    it.copy(
                        selectedOptionIndex = index,
                        isShieldActive = false, // shield consumed
                        explanationTitle = "Shield Defended",
                        showExplanation = true
                    )
                }
                // Restart timer to continue play
                startTimer()
                return
            }

            SoundManager.playWrong()

            // Lose life
            val nextLives = (state.lives - 1).coerceAtLeast(0)
            prefs.setLives(nextLives)
            val penaltyScore = (state.score - 5).coerceAtLeast(0)
            prefs.setScore(penaltyScore)

            _uiState.update {
                it.copy(
                    lives = nextLives,
                    score = penaltyScore,
                    coins = prefs.getCoins(),
                    selectedOptionIndex = index,
                    isCorrectAnswer = false,
                    screenType = ScreenType.RESULT_SCREEN,
                    explanationTitle = "Wrong!"
                )
            }
        }
    }

    // Restart/Retry Level
    fun retryLevel() {
        // If lives = 0, warn them that they must claim energy through simulated Ad
        if (prefs.getLives() <= 0) {
            // Trigger auto Ad display popup to aid them
            _uiState.update {
                it.copy(showSimulatedAdOffer = true)
            }
        } else {
            loadLevel(_uiState.value.currentLevelNumber)
        }
    }

    // Simulated Ad Play (UNITY_GAME_ID simulator) - keeps 100% functional, no deadlocks
    fun startCampaignAd(onCompleted: () -> Unit) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAdPlaying = true,
                    adCountdown = 5
                )
            }
            while (_uiState.value.adCountdown > 0) {
                delay(1000)
                _uiState.update { it.copy(adCountdown = it.adCountdown - 1) }
            }
            // Reward!
            _uiState.update { it.copy(isAdPlaying = false) }
            onCompleted()
        }
    }

    private fun findActivity(ctx: Context): android.app.Activity? {
        var current = ctx
        while (current is android.content.ContextWrapper) {
            if (current is android.app.Activity) return current
            current = current.baseContext
        }
        return null
    }

    private fun grantPowerUp(type: String) {
        val count = prefs.getPowerUpCount(type) + 1
        prefs.setPowerUpCount(type, count)
        when (type) {
            "xray" -> _powerUpsXRay.value = count
            "freeze" -> _powerUpsFreeze.value = count
            "bomb" -> _powerUpsBomb.value = count
            "skip" -> _powerUpsSkip.value = count
            "shield" -> _powerUpsShield.value = count
        }
        _uiState.update {
            it.copy(
                showToastMsg = if (language.value == "bn") "আপনি ১টি পাওয়ার-আপ পেয়েছেন! 🎁" else "Power-Up rewarded successfully! 🎁"
            )
        }
    }

    fun requestAdForPowerUp(type: String) {
        // The game is now fully free. We grant the power-up instantly!
        SoundManager.playCorrect()
        grantPowerUp(type)
    }

    private fun grantFullLives() {
        prefs.setLives(3)
        _uiState.update {
            it.copy(
                lives = 3,
                showSimulatedAdOffer = false,
                coins = prefs.getCoins(),
                showToastMsg = if (language.value == "bn") "আপনার লাইফ পূর্ণ করা হয়েছে ❤️❤️❤️" else "Full lives restored! ❤️❤️❤️"
            )
        }
        loadLevel(_uiState.value.currentLevelNumber)
    }

    fun claimAdForFullLives() {
        // Triumphant sound and restore lives for free immediately!
        SoundManager.playLevelUp()
        grantFullLives()
    }

    fun dismissToast() {
        _uiState.update { it.copy(showToastMsg = null) }
    }

    fun closeAdOffer() {
        _uiState.update { it.copy(showSimulatedAdOffer = false) }
    }

    // ==========================================
    // POWER-UP ACTIONS IMPLEMENTATION
    // ==========================================
    fun activateXRay() {
        val q = _uiState.value.currentQuestion ?: return
        if (_powerUpsXRay.value > 0 && !_uiState.value.isXRayActive && _uiState.value.screenType == ScreenType.GAME_SCREEN) {
            val nextVal = _powerUpsXRay.value - 1
            _powerUpsXRay.value = nextVal
            prefs.setPowerUpCount("xray", nextVal)

            _uiState.update {
                it.copy(isXRayActive = true)
            }
        }
    }

    fun activateFreeze() {
        if (_powerUpsFreeze.value > 0 && !_uiState.value.isFreezeActive && _uiState.value.screenType == ScreenType.GAME_SCREEN) {
            val nextVal = _powerUpsFreeze.value - 1
            _powerUpsFreeze.value = nextVal
            prefs.setPowerUpCount("freeze", nextVal)

            _uiState.update {
                it.copy(
                    isFreezeActive = true,
                    timeLeft = it.timeLeft + 15
                )
            }
        }
    }

    fun activateBomb() {
        val q = _uiState.value.currentQuestion ?: return
        if (_powerUpsBomb.value > 0 && !_uiState.value.isBombActive && _uiState.value.screenType == ScreenType.GAME_SCREEN) {
            val nextVal = _powerUpsBomb.value - 1
            _powerUpsBomb.value = nextVal
            prefs.setPowerUpCount("bomb", nextVal)

            // Randomly find 2 wrong indices and add them to revealed incorrect indices
            val wrongIndices = (0..3).filter { it != q.correctIndex }.shuffled()
            val bombRemoves = wrongIndices.take(2).toSet()

            _uiState.update {
                it.copy(
                    isBombActive = true,
                    revealedIncorrectIndices = bombRemoves
                )
            }
        }
    }

    fun activateSkip() {
        if (_powerUpsSkip.value > 0 && _uiState.value.screenType == ScreenType.GAME_SCREEN) {
            val nextVal = _powerUpsSkip.value - 1
            _powerUpsSkip.value = nextVal
            prefs.setPowerUpCount("skip", nextVal)

            // Auto-advance level without penalties!
            val nextLvl = _uiState.value.currentLevelNumber + 1
            prefs.setUnlockedLevel(nextLvl)
            _unlockedLevel.value = prefs.getUnlockedLevel()
            loadLevel(nextLvl)
        }
    }

    fun activateShield() {
         if (_powerUpsShield.value > 0 && !_uiState.value.isShieldActive && _uiState.value.screenType == ScreenType.GAME_SCREEN) {
            val nextVal = _powerUpsShield.value - 1
            _powerUpsShield.value = nextVal
            prefs.setPowerUpCount("shield", nextVal)

            _uiState.update {
                it.copy(isShieldActive = true)
            }
        }
    }

    fun navigateTo(type: ScreenType) {
        timerJob?.cancel()
        uiTrickTimerJob?.cancel()

        if (type == ScreenType.GAME_SCREEN) {
            loadLevel(prefs.getCurrentLevel())
            return
        }

        _uiState.update {
            it.copy(screenType = type)
        }
    }

    private fun spawnConfetti() {
        confettiList.clear()
        val colors = listOf(0xFFFF5252, 0xFFFFEB3B, 0xFF4CAF50, 0xFF00E5FF, 0xFFE040FB)
        viewModelScope.launch {
            // create 50 randomized particles
            repeat(45) {
                confettiList.add(
                    ConfettiParticle(
                        x = Random.nextFloat() * 1000f,
                        y = -50f,
                        speedX = (Random.nextFloat() * 10f) - 5f,
                        speedY = Random.nextFloat() * 15f + 10f,
                        colorHex = colors.random(),
                        size = Random.nextFloat() * 12f + 8f
                    )
                )
            }

            // Animate particles downward for 3 seconds
            var ticks = 30
            while (ticks > 0 && confettiList.isNotEmpty()) {
                delay(100)
                ticks--
                for (i in confettiList.indices) {
                    val p = confettiList[i]
                    confettiList[i] = p.copy(
                        x = p.x + p.speedX,
                        y = p.y + p.speedY
                    )
                }
            }
            confettiList.clear()
        }
    }

    fun clearAllProgress() {
        prefs.setCurrentLevel(1)
        prefs.setUnlockedLevel(1)
        prefs.setScore(0)
        prefs.setCoins(120) // Reset starting coins
        _coins.value = 120
        prefs.setLives(3)
        prefs.setPowerUpCount("xray", 2)
        prefs.setPowerUpCount("freeze", 2)
        prefs.setPowerUpCount("bomb", 2)
        prefs.setPowerUpCount("skip", 2)
        prefs.setPowerUpCount("shield", 2)

        _powerUpsXRay.value = 2
        _powerUpsFreeze.value = 2
        _powerUpsBomb.value = 2
        _powerUpsSkip.value = 2
        _powerUpsShield.value = 2
        _unlockedLevel.value = 1

        loadLevel(1)
    }

    fun setDailyDialogVisible(visible: Boolean) {
        val currDay = prefs.getClaimDayCycle()
        val claimTime = prefs.getLastClaimTime()
        // 12 seconds cooldown for sandbox testing, standard day check is easy as well
        val claimed = (System.currentTimeMillis() - claimTime) < 12 * 1000L
        _uiState.update {
            it.copy(
                showDailyClaimDialog = visible,
                currentClaimDayCycle = currDay,
                hasClaimedToday = claimed
            )
        }
    }

    fun claimDailyReward() {
        SoundManager.playCoin()
        val currentCycle = prefs.getClaimDayCycle()
        val rewardAmount = when (currentCycle) {
            1 -> 25
            2 -> 25
            3 -> 25
            4 -> 30
            5 -> 35
            6 -> 50
            else -> 200
        }
        val currentScore = prefs.getScore()
        val nextScore = currentScore + rewardAmount
        prefs.setScore(nextScore)
        
        // Award 30 Coins on daily reward too!
        val nextCoins = prefs.getCoins() + 30
        prefs.setCoins(nextCoins)
        _coins.value = nextCoins

        prefs.setLastClaimTime(System.currentTimeMillis())
        
        val nextCycle = if (currentCycle >= 7) 1 else currentCycle + 1
        prefs.setClaimDayCycle(nextCycle)

        _uiState.update {
            it.copy(
                score = nextScore,
                coins = nextCoins,
                currentClaimDayCycle = nextCycle,
                hasClaimedToday = true,
                showToastMsg = if (language.value == "bn") "উপহার ক্লেইম সফল! +$rewardAmount বাল্ব 💡 এবং +৩০ কয়েন 🪙" else "Claim successful! +$rewardAmount Bulbs 💡 and +30 Coins 🪙"
            )
        }
    }

    fun setStoreDialogVisible(visible: Boolean) {
        _uiState.update {
            it.copy(showStoreDialog = visible)
        }
    }

    fun purchaseStoreItemByPoints(type: String, cost: Int) {
        val currentScore = prefs.getScore()
        if (currentScore < cost) {
            _uiState.update {
                it.copy(showToastMsg = if (language.value == "bn") "পর্যাপ্ত বাল্ব 💡 নেই!" else "Not enough Bulbs 💡!" )
            }
            return
        }

        val nextScore = currentScore - cost
        prefs.setScore(nextScore)

        val count = prefs.getPowerUpCount(type) + 3
        prefs.setPowerUpCount(type, count)
        
        when (type) {
            "xray" -> _powerUpsXRay.value = count
            "freeze" -> _powerUpsFreeze.value = count
            "bomb" -> _powerUpsBomb.value = count
            "skip" -> _powerUpsSkip.value = count
            "shield" -> _powerUpsShield.value = count
        }

        _uiState.update {
            it.copy(
                score = nextScore,
                showToastMsg = if (language.value == "bn") "পাওয়ার-আপ প্যাক কেনা সফল হয়েছে! 🎁" else "Power-up bundle purchased! 🎁"
            )
        }
    }
}

// ==========================================
// 5. VIEW STATE DATA STRUCTURES
// ==========================================
enum class ScreenType {
    SPLASH_SCREEN,
    HOME_SCREEN,
    LEVEL_SELECT_SCREEN,
    GAME_SCREEN,
    RESULT_SCREEN
}

data class OffsetPair(val x: Float, val y: Float)

data class UiTrickState(
    val buttonOffsets: List<OffsetPair> = List(4) { OffsetPair(0f, 0f) },
    val labelColorSwap: Boolean = false
)

data class GameUiState(
    val currentLevelNumber: Int = 1,
    val currentStageNumber: Int = 1,
    val currentQuestion: Question? = null,
    val timeLeft: Int = 15,
    val totalLevelTime: Int = 15,
    val score: Int = 0,
    val coins: Int = 0,
    val lives: Int = 3,
    val screenType: ScreenType = ScreenType.SPLASH_SCREEN,
    val isXRayActive: Boolean = false,
    val isBombActive: Boolean = false,
    val isShieldActive: Boolean = false,
    val isFreezeActive: Boolean = false,
    val selectedOptionIndex: Int = -1,
    val revealedIncorrectIndices: Set<Int> = emptySet(),
    val isCorrectAnswer: Boolean = false,
    val showExplanation: Boolean = false,
    val explanationTitle: String = "",
    val lastAnswerSpeedBonus: Int = 0,
    val isGameCompleted: Boolean = false,
    // simulated ads values
    val showSimulatedAdOffer: Boolean = false,
    val isAdPlaying: Boolean = false,
    val adCountdown: Int = 0,
    val showToastMsg: String? = null,
    val uiTrickState: UiTrickState = UiTrickState(),
    // store and daily claims
    val showDailyClaimDialog: Boolean = false,
    val showStoreDialog: Boolean = false,
    val currentClaimDayCycle: Int = 1,
    val hasClaimedToday: Boolean = false
)
