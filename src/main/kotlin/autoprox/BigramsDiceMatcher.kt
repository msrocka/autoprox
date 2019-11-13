package autoprox

import org.openlca.core.model.Flow

/**
 * Calculates matching scores based on the Dice coefficient of the bigrams
 * of flow names.
 */
class BigramsDiceMatcher : Matcher {

    private val candidateBigrams = mutableMapOf<Long, List<String>>()

    override fun setCandidates(candidates: Collection<Flow>) {
        candidateBigrams.clear()
        for (flow in candidates) {
            candidateBigrams[flow.id] = getBigrams(flow)
        }
    }

    override fun getScores(flow: Flow): Map<Long, Double> {
        val flowBigrams = getBigrams(flow)
        return candidateBigrams.entries.fold(mutableMapOf(),
            { map, (candidateID, candidateBigrams) ->
                map[candidateID] = dice(flowBigrams, candidateBigrams)
                map
            })
    }

    private fun getBigrams(flow: Flow): List<String> {
        val words = words(flow.name)
        return words.fold(mutableListOf(), { allBigrams, word ->
            allBigrams.addAll(bigrams(word))
            allBigrams
        })
    }
}
