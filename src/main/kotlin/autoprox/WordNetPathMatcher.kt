package autoprox

import edu.cmu.lti.ws4j.WS4J
import org.openlca.core.model.Flow
import kotlin.math.exp

class WordNetPathMatcher : Matcher {

    val alpha = 0.1
    private val candidateWords = mutableMapOf<Long, List<String>>()
    private val wordFrequencies = mutableMapOf<String, Double>()

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
        val maxScore = flowWords.size.toDouble() * exp(-alpha)

        return candidateWords.entries.fold(
            mutableMapOf(), { map, (candidateID, candidateWords) ->
                var candidateScore = .0
                for (flowWord in flowWords) {
                    var wordScore = .0
                    for (candWord in candidateWords) {
                        if (flowWord == candWord) {
                            wordScore = exp(-alpha)
                            break
                        }
                        val s = getScore(flowWord, candWord)
                        if (s > wordScore) {
                            wordScore = s
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

    private fun getScore(flowWord: String, candWord: String): Double {
        val freq = wordFrequencies[candWord] ?: 0.0
        if (freq == .0)
            return .0
        val pathScore = WS4J.runPATH(flowWord, candWord)
        if (pathScore <= .0)
            return .0
        return pathScore * exp(-alpha * freq)
    }
}