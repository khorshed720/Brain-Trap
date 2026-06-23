package com.example

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

object AdManager {
    private const val TAG = "AdManager"

    // Set this to true to prevent full-screen ads from automatically opening and blocking inputs in the browser's emulator.
    // When you are ready to publish, set this to false to enable full production ads.
    var devBypassFullScreenAds = true

    // Google AdMob Test Ad Unit IDs provided by the user
    private const val APP_OPEN_AD_ID = "ca-app-pub-3940256099942544/9257395921"
    private const val BANNER_AD_ID = "ca-app-pub-3940256099942544/9214589741"
    private const val INTERSTITIAL_AD_ID = "ca-app-pub-3940256099942544/1033173712"
    private const val REWARDED_AD_ID = "ca-app-pub-3940256099942544/5224354917"

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var appOpenAd: AppOpenAd? = null

    private var isInitializing = false
    private var isInitialized = false
    private var lastAppOpenShowTime: Long = 0

    // Cooldown in milliseconds dynamically avoids spamming users on app resumes (60 seconds)
    private const val APP_OPEN_COOLDOWN_MS = 60000

    fun initialize(context: Context) {
        if (isInitialized || isInitializing) return
        isInitializing = true
        Log.d(TAG, "Initializing Google Mobile Ads SDK...")
        try {
            MobileAds.initialize(context) { initializationStatus ->
                isInitialized = true
                isInitializing = false
                Log.d(TAG, "AdMob Initialized Successfully.")
                // Preload ad forms immediately
                loadAllAds(context.applicationContext)
            }
        } catch (e: Exception) {
            isInitializing = false
            Log.e(TAG, "Error initializing Mobile Ads: ", e)
        }
    }

    private fun loadAllAds(context: Context) {
        loadInterstitial(context)
        loadRewarded(context)
        loadAppOpen(context)
    }

    // ==========================================
    // INTERSTITIAL AD LIFECYCLE
    // ==========================================
    fun loadInterstitial(context: Context) {
        try {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                INTERSTITIAL_AD_ID,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitialAd = ad
                        Log.d(TAG, "Interstitial loaded.")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        interstitialAd = null
                        Log.e(TAG, "Interstitial failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed launching InterstitialAd.load", e)
        }
    }

    fun showInterstitial(activity: Activity, onAdClosed: () -> Unit) {
        if (devBypassFullScreenAds) {
            Log.d(TAG, "Bypassing Interstitial Ad show due to devBypassFullScreenAds = true")
            onAdClosed()
            return
        }
        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial dismissed.")
                    interstitialAd = null
                    loadInterstitial(activity.applicationContext) // reload next
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Interstitial failed to show: ${adError.message}")
                    interstitialAd = null
                    loadInterstitial(activity.applicationContext) // reload next
                    onAdClosed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Interstitial showed.")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial not ready yet.")
            loadInterstitial(activity.applicationContext) // trigger reload attempt
            onAdClosed()
        }
    }

    // ==========================================
    // REWARDED VIDEO AD LIFECYCLE
    // ==========================================
    fun loadRewarded(context: Context) {
        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                REWARDED_AD_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        Log.d(TAG, "Rewarded loaded.")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        rewardedAd = null
                        Log.e(TAG, "Rewarded failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed launching RewardedAd.load", e)
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdClosed: () -> Unit
    ) {
        if (devBypassFullScreenAds) {
            Log.d(TAG, "Bypassing Rewarded Ad show due to devBypassFullScreenAds = true - Granting Instant Reward")
            onUserEarnedReward()
            onAdClosed()
            return
        }
        val ad = rewardedAd
        if (ad != null) {
            var isRewarded = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded ad dismissed.")
                    rewardedAd = null
                    loadRewarded(activity.applicationContext) // reload next
                    if (isRewarded) {
                        onUserEarnedReward()
                    }
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                    rewardedAd = null
                    loadRewarded(activity.applicationContext) // reload next
                    onAdClosed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded showed.")
                }
            }
            ad.show(activity) { reward ->
                isRewarded = true
                Log.d(TAG, "User earned reward: ${reward.amount} ${reward.type}")
            }
        } else {
            Log.d(TAG, "Rewarded ad not loaded yet.")
            loadRewarded(activity.applicationContext) // trigger reload attempt
            onAdClosed()
        }
    }

    // ==========================================
    // APP OPEN AD LIFECYCLE
    // ==========================================
    fun loadAppOpen(context: Context) {
        try {
            val adRequest = AdRequest.Builder().build()
            AppOpenAd.load(
                context,
                APP_OPEN_AD_ID,
                adRequest,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        Log.d(TAG, "App Open loaded.")
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        appOpenAd = null
                        Log.e(TAG, "App Open failed to load: ${loadAdError.message}")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed launching AppOpenAd.load", e)
        }
    }

    fun showAppOpenAdIfAvailable(activity: Activity) {
        if (devBypassFullScreenAds) {
            Log.d(TAG, "Bypassing App Open Ad show due to devBypassFullScreenAds = true")
            return
        }
        val now = System.currentTimeMillis()
        if (now - lastAppOpenShowTime < APP_OPEN_COOLDOWN_MS) {
            Log.d(TAG, "App Open ad is on load cooldown.")
            return
        }

        val ad = appOpenAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "App Open dismissed.")
                    appOpenAd = null
                    lastAppOpenShowTime = System.currentTimeMillis()
                    loadAppOpen(activity.applicationContext) // reload next
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "App Open failed to show: ${adError.message}")
                    appOpenAd = null
                    loadAppOpen(activity.applicationContext) // reload next
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "App Open showed.")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "App Open ad not loaded yet.")
            loadAppOpen(activity.applicationContext)
        }
    }
}

// ==========================================
// ANCHORED ADAPTIVE BANNER COMPOSABLE
// ==========================================
@Composable
fun AdBannerView(modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        factory = { ctx ->
            AdView(ctx).apply {
                val displayMetrics = ctx.resources.displayMetrics
                val widthPixels = displayMetrics.widthPixels
                val density = displayMetrics.density
                val adWidth = (widthPixels / density).toInt()

                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(ctx, adWidth))
                // Google standard banner test ID matching ca-app-pub-3940256099942544/9214589741
                adUnitId = "ca-app-pub-3940256099942544/9214589741"
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
