package autoprox

import org.openlca.core.model.Flow
import kotlin.math.exp

class InfoContentMatcher : Matcher {

    val alpha = 0.1
    val substringFactor = 0.01

    private val wordFrequencies = mutableMapOf<String, Double>()
    private val candidateWords = mutableMapOf<Long, List<String>>()

    override fun setCandidates(candidates: Collection<Flow>) {
        for (candidate in candidates) {
            val words = words(candidate.name)
            candidateWords[candidate.id] = words
            for (word in words) {
                val freq = wordFrequencies.getOrDefault(word, .0)
                wordFrequencies[word] = 1.0 + freq
            }
        }
    }

    override fun getScores(flow: Flow): Map<Long, Double> {
        val flowWords = words(flow.name)
        if (flowWords.isEmpty())
            return emptyMap()
        val maxScore = flowWords.fold(0.0, { s, flowWord ->
            s + flowWord.length.toDouble() * exp(-alpha)
        })
        return candidateWords.entries.fold(
            mutableMapOf(), { map, (candidateID, candidateWords) ->
                var totalScore = .0
                for (flowWord in flowWords) {
                    var wordScore = .0
                    for (candWord in candidateWords) {
                        if (flowWord == candWord) {
                            wordScore = candWord.length.toDouble() * exp(
                                -alpha * wordFrequencies.getOrDefault(candWord, 1.0)
                            )
                            break
                        }
                        var (smaller, larger) = if (candWord.length < flowWord.length) {
                            Pair(candWord, flowWord)
                        } else {
                            Pair(flowWord, candWord)
                        }
                        if (larger.contains(smaller)) {
                            // substring match
                            val s = substringFactor * smaller.length.toDouble() * exp(
                                -alpha * wordFrequencies.getOrDefault(candWord, 1.0)
                            )
                            if (s > wordScore) {
                                wordScore = s
                            }
                        }
                    }
                    totalScore += wordScore
                }
                if (totalScore > 0.0) {
                    map[candidateID] = totalScore / maxScore
                }
                map
            })
    }
}