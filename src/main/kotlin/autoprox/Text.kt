package autoprox

/**
 * Extracts the words from the given string where we define
 * a word to be a sequence of letters, digits, or characters
 * like `-`. The single words are all in lower case.
 */
fun words(s: String?): List<String> {
    if (s == null || s.isEmpty())
        return emptyList()

    var buf = StringBuilder()
    val words = ArrayList<String>()
    for (c in s.toLowerCase().toCharArray()) {
        if (Character.isLetterOrDigit(c) || c == '-') {
            buf.append(c)
        } else if (buf.isNotEmpty()) {
            val word = buf.toString()
            buf = StringBuilder()
            words.add(word)
        }
    }
    if (buf.isNotEmpty()) {
        val word = buf.toString()
        words.add(word)
    }
    return words
}

/**
 * Returns the bigrams of the given string.
 */
fun bigrams(s: String?): List<String> {
    return ngrams(s, 2)
}

fun ngrams(s: String?, n: Int): List<String> {
    if (s == null || s.length < n)
        return emptyList()
    val ngrams = mutableListOf<String>()
    var buf = StringBuilder()
    for (c in s.toCharArray()) {
        buf.append(c)
        if (buf.length == n) {
            ngrams.add(buf.toString())
            buf.deleteCharAt(0)
        }
    }
    return ngrams
}

/** Computes the dice coefficient for the given lists of strings.*/
fun dice(a: List<String>, b: List<String>): Double {
    if (a.isEmpty() || b.isEmpty())
        return 0.0
    val remaining = mutableListOf<String>()
    remaining.addAll(b)
    var matched = 0.0
    for (ai in a) {
        if (ai in remaining) {
            matched += 1.0
            remaining.remove(ai)
        }
    }
    val total = a.size + b.size
    return 2 * matched / total.toDouble()
}
