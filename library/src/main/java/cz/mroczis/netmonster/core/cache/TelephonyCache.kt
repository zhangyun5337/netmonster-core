package cz.mroczis.netmonster.core.cache

import android.telephony.TelephonyManager
import cz.mroczis.netmonster.core.Milliseconds
import cz.mroczis.netmonster.core.SubscriptionId
import cz.mroczis.netmonster.core.cache.TelephonyCache.LIFETIME
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Telephony cache helps us reduce calls to [TelephonyManager.listen] and speed up processing
 * Fetched results are stored for [LIFETIME] milliseconds.
 * 电话缓存帮助我们减少对[TelephonyManager.listen]的调用，加快处理速度
 * 获取的结果将存储 [LIFETIME] 毫秒。
 */
internal object TelephonyCache {

    private const val LIFETIME: Milliseconds = 1_000

    private val keys = ConcurrentLinkedQueue<Key>()
    private val cache = ConcurrentHashMap<Key, Value>()

    /**
     * Attempts to get stored value from a cache, if it is not present or it is already expired
     * invokes [update] to get fresh value and caches it.
     *
     * This method is safe to invoke from any thread.
     *
     * [subId] - subscription id
     * [event] - one of PhoneStateListener.LISTEN_* constants
     * 如果缓存中不存在或已过期，则尝试从缓存中获取存储值
     * 调用 [update] 获取新值并缓存。
     *
     * 此方法可以从任何线程安全调用。
     *
     * [subId] - 订阅 id
     * [event] - PhoneStateListener.LISTEN_* 常量之一
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getOrUpdate(subId: SubscriptionId?, event: Event, update: () -> T?): T? {
        // Make sure that there will be only one instance of a key
        val key = synchronized(this) {
            val modelKey = Key(subId = subId, event = event)
            keys.find { it == modelKey } ?: run {
                keys += modelKey
                modelKey
            }
        }

        // Try to get cached one without synchronised access
        // 尝试获取缓存，而无需同步访问
        val value = cache[key]?.takeIf { it.valid }
        if (value != null) {
            return value.any as? T
        }

        return synchronized(key) {
            // Let's try grab cached value once again since we are in a critical section
            // 既然我们处于临界区，让我们再次尝试获取缓存值
            val syncedValue = cache[key]?.takeIf { it.valid }
            if (syncedValue != null) {
                syncedValue.any as? T
            } else {
                cache.remove(key)
                update().let {
                    val newValue = Value(created = System.currentTimeMillis(), any = it)
                    cache[key] = newValue
                    it
                }
            }
        }
    }

    /**
     * Key here serves as a set of unique identifiers required to perform data update
     * 这里的Key是一组执行数据更新所需的唯一标识符
     */
    private data class Key(
        val subId: SubscriptionId?,
        val event: Event,
    )

    /**
     * Cached value, could be literally [any].
     * 缓存值，可以字面意思是[任何]。
     */
    private data class Value(
        val created: Milliseconds,
        val any: Any?,
    ) {

        val valid
            get() = created + LIFETIME >= System.currentTimeMillis()

    }

    enum class Event {
        CELL_LOCATION,
        DISPLAY_INFO,
        PHYSICAL_CHANNEL,
        SERVICE_STATE,
        SIGNAL_STRENGTHS,
        ;
    }
}
