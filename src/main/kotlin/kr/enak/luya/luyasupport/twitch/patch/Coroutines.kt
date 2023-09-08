package kr.enak.luya.luyasupport.twitch.patch

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object Coroutines {
    var ioScope = CoroutineScope(Dispatchers.IO)
}
