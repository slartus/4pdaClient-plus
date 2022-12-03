package org.softeg.slartus.forpdaplus.common.data.repositories.emotics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import ru.softeg.slartus.common.api.*
import ru.softeg.slartus.common.api.models.Emotic
import javax.inject.Inject

class LocalEmoticsDataSource @Inject constructor(
    private val settings: Settings
) {
    suspend fun getEmotics(): List<Emotic> = withContext(Dispatchers.Default) {
        listOf(
            ":happy:" to "happy.gif",
            ";)" to "wink.gif",
            ":P" to "tongue.gif",
            ":-D" to "biggrin.gif",
            ":lol:" to "laugh.gif",
            ":rolleyes:" to "rolleyes.gif",
            ":)" to "smile_good.gif",
            ":beee:" to "beee.gif",
            ":rofl:" to "rofl.gif",
            ":sveta:" to "sveta.gif",
            ":thank_you:" to "thank_you.gif",
            "}-)" to "devil.gif",
            ":girl_cray:" to "girl_cray.gif",
            ":D" to "biggrin.gif",
            "o.O" to "blink.gif",
            ":blush:" to "blush.gif",
            ":yes2:" to "yes.gif",
            ":mellow:" to "mellow.gif",
            ":huh:" to "huh.gif",
            ":o" to "ohmy.gif",
            "B)" to "cool.gif",
            "-_-" to "sleep.gif",
            "&lt;_&lt;" to "dry.gif",
            ":wub:" to "wub.gif",
            ":angry:" to "angry.gif",
            ":(" to "sad.gif",
            ":unsure:" to "unsure.gif",
            ":wacko:" to "wacko.gif",
            ":blink:" to "blink.gif",
            ":ph34r:" to "ph34r.gif",
            ":banned:" to "banned.gif",
            ":antifeminism:" to "antifeminism.gif",
            ":beta:" to "beta.gif",
            ":boy_girl:" to "boy_girl.gif",
            ":butcher:" to "butcher.gif",
            ":bubble:" to "bubble.gif",
            ":censored:" to "censored.gif",
            ":clap:" to "clap.gif",
            ":close_tema:" to "close_tema.gif",
            ":clapping:" to "clapping.gif",
            ":coldly:" to "coldly.gif",
            ":comando:" to "comando.gif",
            ":congratulate:" to "congratulate.gif",
            ":dance:" to "dance.gif",
            ":daisy:" to "daisy.gif",
            ":dancer:" to "dancer.gif",
            ":derisive:" to "derisive.gif",
            ":dinamo:" to "dinamo.gif",
            ":dirol:" to "dirol.gif",
            ":diver:" to "diver.gif",
            ":drag:" to "drag.gif",
            ":download:" to "download.gif",
            ":drinks:" to "drinks.gif",
            ":first_move:" to "first_move.gif",
            ":feminist:" to "feminist.gif",
            ":flood:" to "flood.gif",
            ":fool:" to "fool.gif",
            ":friends:" to "friends.gif",
            ":foto:" to "foto.gif",
            ":girl_blum:" to "girl_blum.gif",
            ":girl_crazy:" to "girl_crazy.gif",
            ":girl_curtsey:" to "girl_curtsey.gif",
            ":girl_dance:" to "girl_dance.gif",
            ":girl_flirt:" to "girl_flirt.gif",
            ":girl_hospital:" to "girl_hospital.gif",
            ":girl_hysterics:" to "girl_hysterics.gif",
            ":girl_in_love:" to "girl_in_love.gif",
            ":girl_kiss:" to "girl_kiss.gif",
            ":girl_pinkglassesf:" to "girl_pinkglassesf.gif",
            ":girl_parting:" to "girl_parting.gif",
            ":girl_prepare_fish:" to "girl_prepare_fish.gif",
            ":good:" to "good.gif",
            ":girl_spruce_up:" to "girl_spruce_up.gif",
            ":girl_tear:" to "girl_tear.gif",
            ":girl_tender:" to "girl_tender.gif",
            ":girl_teddy:" to "girl_teddy.gif",
            ":girl_to_babruysk:" to "girl_to_babruysk.gif",
            ":girl_to_take_umbrage:" to "girl_to_take_umbrage.gif",
            ":girl_triniti:" to "girl_triniti.gif",
            ":girl_tongue:" to "girl_tongue.gif",
            ":girl_wacko:" to "girl_wacko.gif",
            ":girl_werewolf:" to "girl_werewolf.gif",
            ":girl_witch:" to "girl_witch.gif",
            ":grabli:" to "grabli.gif",
            ":good_luck:" to "good_luck.gif",
            ":guess:" to "guess.gif",
            ":hang:" to "hang.gif",
            ":heart:" to "heart.gif",
            ":help:" to "help.gif",
            ":helpsmilie:" to "helpsmilie.gif",
            ":hemp:" to "hemp.gif",
            ":heppy_dancing:" to "heppy_dancing.gif",
            ":hysterics:" to "hysterics.gif",
            ":indeec:" to "indeec.gif",
            ":i-m_so_happy:" to "i-m_so_happy.gif",
            ":kindness:" to "kindness.gif",
            ":king:" to "king.gif",
            ":laugh_wild:" to "laugh_wild.gif",
            ":4PDA:" to "love_4PDA.gif",
            ":nea:" to "nea.gif",
            ":moil:" to "moil.gif",
            ":no:" to "no.gif",
            ":nono:" to "nono.gif",
            ":offtopic:" to "offtopic.gif",
            ":ok:" to "ok.gif",
            ":papuas:" to "papuas.gif",
            ":party:" to "party.gif",
            ":pioneer_smoke:" to "pioneer_smoke.gif",
            ":pipiska:" to "pipiska.gif",
            ":protest:" to "protest.gif",
            ":popcorm:" to "popcorm.gif",
            ":rabbi:" to "rabbi.gif",
            ":resent:" to "resent.gif",
            ":roll:" to "roll.gif",
            ":rtfm:" to "rtfm.gif",
            ":russian_garmoshka:" to "russian_garmoshka.gif",
            ":russian:" to "russian.gif",
            ":russian_ru:" to "russian_ru.gif",
            ":scratch_one-s_head:" to "scratch_one-s_head.gif",
            ":scare:" to "scare.gif",
            ":search:" to "search.gif",
            ":secret:" to "secret.gif",
            ":skull:" to "skull.gif",
            ":shok:" to "shok.gif",
            ":sorry:" to "sorry.gif",
            ":smoke:" to "smoke.gif",
            ":spiteful:" to "spiteful.gif",
            ":stop_flood:" to "stop_flood.gif",
            ":suicide:" to "suicide.gif",
            ":stop_holywar:" to "stop_holywar.gif",
            ":superman:" to "superman.gif",
            ":superstition:" to "superstition.gif",
            ":tablet_za:" to "tablet_protiv.gif",
            ":tablet_protiv:" to "tablet_za.gif",
            ":this:" to "this.gif",
            ":tomato:" to "tomato.gif",
            ":to_clue:" to "to_clue.gif",
            ":tommy:" to "tommy.gif",
            ":tongue3:" to "tongue3.gif",
            ":umnik:" to "umnik.gif",
            ":victory:" to "victory.gif",
            ":vinsent:" to "vinsent.gif",
            ":wallbash:" to "wallbash.gif",
            ":whistle:" to "whistle.gif",
            ":wink_kind:" to "wink_kind.gif",
            ":yahoo:" to "yahoo.gif",
            ":yes:" to "yes.gif",
            ":&#91;" to "confusion.gif",
            "&#93;-:{" to "girl_devil.gif",
            ":*" to "kiss.gif",
            "@}-'-,-" to "give_rose.gif",
            ":'(" to "cry.gif",
            ":-{" to "mad.gif",
            "=^.^=" to "kitten.gif",
            "(-=" to "girl_hide.gif",
            "(-;" to "girl_wink.gif",
            ")-:{" to "girl_angry.gif",
            "*-:" to "girl_chmok.gif",
            ")-:" to "girl_sad.gif",
            ":girl_mad:" to "girl_mad.gif",
            "(-:" to "girl_smile.gif",
            ":acute:" to "acute.gif",
            ":aggressive:" to "aggressive.gif",
            ":air_kiss:" to "air_kiss.gif",
            "o_O" to "blink.gif",
            ":-&#91;" to "confusion.gif",
            ":'-(" to "cry.gif",
            ":lol_girl:" to "girl_haha.gif",
            ")-':" to "girl_cray.gif",
            "(;" to "girl_wink.gif",
            ":-*" to "kiss.gif",
            ":laugh:" to "laugh.gif",
            ":ohmy:" to "ohmy.gif",
            ":-(" to "sad.gif",
            "8-)" to "rolleyes.gif",
            ":-)" to "smile.gif",
            ":smile:" to "smile.gif",
            ":-P" to "tongue.gif",
            ";-)" to "wink.gif",
        )
    }

    suspend fun getFavoriteEmotics(): List<Emotic> {
        return settings.getList<SerializableEmotic>(KEY_FAVORITE_EMOTICS, emptyList()).orEmpty()
            .map { it.to() }
    }

    suspend fun addFavoriteEmotic(emotic: Emotic) {
        val emotics = getFavoriteEmotics()
        val newFavorites = listOf(emotic) + emotics
        settings.putList(
            KEY_FAVORITE_EMOTICS,
            newFavorites.take(FAVORITE_COUNT).map(SerializableEmotic::of)
        )
    }

    private companion object {
        const val FAVORITE_COUNT = 10
        const val KEY_FAVORITE_EMOTICS = "emotics.favorite"
    }
}

@Serializable
private data class SerializableEmotic(val code: String, val image: String) {
    fun to(): Emotic = Emotic(code, image)

    companion object {
        fun of(emotic: Emotic) = SerializableEmotic(code = emotic.code, image = emotic.image)
    }
}


private infix fun String.to(that: String): Emotic = Emotic(this, that)
