package mapview


internal class LruCache<K, V>(
    private var capacity: Int,
) {
    private val cache = linkedMapOf<K, V>()

    operator fun set(key: K, value: V) {
        if (cache.size >= capacity) {
            cache.remove(cache.iterator().next().key)
        }
        cache[key] = value
    }

    operator fun get(key: K): V? {
        val value = cache[key]
        if (value != null) {
            cache.remove(key)
            cache[key] = value
        }
        return value
    }

    fun remove(key: K) {
        cache.remove(key)
    }

    fun reset(newCapacity: Int? = null) {
        cache.clear()
        capacity = newCapacity ?: capacity
    }

}
