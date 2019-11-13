package autoprox

import org.openlca.core.model.Flow
import kotlin.math.exp

class InfoContentMatcher : Matcher {

    val alpha = 0.1
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
        val maxScore = flowWords.fold(.0, { s, word ->
            s + (word.length.toDouble() * exp(-alpha))
        })

        return candidateWords.entries.fold(
            mutableMapOf(), { map, (candidateID, candidateWords) ->
                var candidateScore = .0
                for (flowWord in flowWords) {
                    var wordScore = .0
                    for (candWord in candidateWords) {
                        if (flowWord == candWord) {
                            wordScore = infoContent(candWord)
                            break
                        }
                    }
                    candidateScore += wordScore
                }
                if (candidateScore > 0.0) {
                    map[candidateID] = candidateScore / maxScore
                }
                map
            })
    }

    private fun infoContent(word: String): Double {
        val freq = wordFrequencies[word] ?: 0.0
        if (freq == .0)
            return .0
        return word.length * exp(-alpha * freq)
    }
}