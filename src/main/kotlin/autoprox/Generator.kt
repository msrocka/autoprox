package autoprox

import org.openlca.core.database.CategoryDao
import org.openlca.core.database.FlowDao
import org.openlca.core.database.IDatabase
import org.openlca.core.database.ProcessDao
import org.openlca.core.matrix.ProcessProduct
import org.openlca.core.matrix.cache.ProcessTable
import org.openlca.core.model.*
import org.slf4j.LoggerFactory
import java.util.*
import kotlin.math.max
import kotlin.math.pow

/**
 * Generates the bridge processes using a given flow matcher.
 */
class Generator(
    private val db: IDatabase,
    private val matcher: Matcher
) {

    private val log = LoggerFactory.getLogger(Generator::class.java)

    fun doIt(processID: String) {
        val process = ProcessDao(db).getForRefId(processID)
        if (process == null) {
            log.error(
                "Could not find process with " +
                        "id='{}' in database", processID
            )
            return
        }

        // flowProviders contains the products (or waste flows) for
        // which there is a provider (or treatment process) in the database
        // in a map { flowID -> provider }
        val flowProviders = ProcessTable.create(db).providers.fold(
            mutableMapOf<Long, ProcessProduct>(), { map, processProduct ->
                map[processProduct.flowId()] = processProduct
                map
            })
        // collect the product / waste flows that have a provider
        val candidates = FlowDao(db).getForIds(flowProviders.keys)
        if (candidates.size == 0) {
            log.error(
                "There are no products with " +
                        "providers in the databases"
            )
            return
        }
        log.info(
            "There are {} products with providers " +
                    "in the database", candidates.size
        )
        matcher.setCandidates(candidates)

        // generate the bridge processes for linkable flows
        for (e in process.exchanges) {
            if (!isLinkable(e))
                continue
            val provider = flowProviders[e.flow.id]
            if (provider != null) {
                // there is already a provider for the given flow
                log.info(
                    "flow '{}' already has a provider '{}'",
                    e.flow.name, provider.process?.name
                )
                continue
            }
            log.info(
                "try to generate provider for flow '{}'",
                e.flow.name
            )
            makeBridge(e.flow, candidates)
        }

    }

    /** An exchange is linkable if it is a product input or waste output.*/
    private fun isLinkable(e: Exchange): Boolean {
        val flowType = e.flow?.flowType ?: return false
        if (e.isInput && flowType == FlowType.PRODUCT_FLOW)
            return true
        if (!e.isInput && flowType == FlowType.WASTE_FLOW)
            return true
        return false
    }

    private fun makeBridge(flow: Flow, allCandidates: List<Flow>) {
        // we take flows as candidates that have the
        // same reference flow property. this is currently
        // a bit restrictive as we just need to make sure that
        // both flows have common flow property. However, as
        // flows with multiple flow properties are not that
        // common in current databases this probably is fine.
        val candidates = allCandidates.filter { p ->
            p.flowType == flow.flowType
                    && Objects.equals(
                p.referenceFlowProperty,
                flow.referenceFlowProperty
            )
        }
        if (candidates.isEmpty()) {
            log.warn(
                "No matching flows with type and " +
                        "unit found for: ${flow.name}"
            )
            return
        }

        var scores = matcher.getScores(flow)

        val maxScore = scores.values.reduce { a, b -> max(a, b) }
        if (maxScore == .0) {
            log.warn(
                "No matching score > 0 found for: {}",
                flow.name
            )
            return
        }
        scores = scores.filter { (_, factor) ->
            factor / maxScore > 0.5
        }
        val scoresTotal = scores.values.reduce { a, b -> a + b }
        val bridge = initBridge(flow, db)

        for (candidate in candidates) {
            val score = scores[candidate.id] ?: continue
            val exchange = bridge.exchange(candidate)
            exchange.isInput = flow.flowType == FlowType.PRODUCT_FLOW
            exchange.amount = score.pow(2.0) / (scoresTotal * maxScore)
        }

        ProcessDao(db).insert(bridge)
        log.info("Created bridge process {}", bridge.name)
    }

    private fun initBridge(flow: Flow, db: IDatabase): Process {
        val p = Process()
        p.category = CategoryDao(db).sync(ModelType.PROCESS, "_bridge")
        p.refId = UUID.randomUUID().toString()
        p.name = "_bridge: ${flow.name}"
        p.processType = ProcessType.UNIT_PROCESS
        val qref = p.exchange(flow)
        qref.isInput = flow.flowType == FlowType.WASTE_FLOW
        p.quantitativeReference = qref
        return p
    }
}

