package autoprox

import org.openlca.core.model.Flow

/**
 * Matcher describes the protocol for calculating similarity
 * scores between flows in a database.
 */
interface Matcher {

    /**
     * Sets the flow candidates C. For a given flow
     * f the matcher will calculate a score s_c for each
     * flow c in C.
     */
    fun setCandidates(candidates: Collection<Flow>)

    /**
     * Returns a map { flow ID -> score } that contains
     * the calculated matching score s_c for each flow c
     * of the candidates C related to the given flow f.
     */
    fun getScores(flow: Flow): Map<Long, Double>

}
