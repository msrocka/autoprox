package autoprox

import org.openlca.core.database.CategoryDao
import org.openlca.core.database.FlowDao
import org.openlca.core.database.IDatabase
import org.openlca.core.database.ProcessDao
import org.openlca.core.database.derby.DerbyDatabase
import org.openlca.core.matrix.ProcessProduct
import org.openlca.core.matrix.cache.ProcessTable
import org.openlca.core.model.*
import java.io.File
import java.util.*
import kotlin.math.max

fun main() {
    val dbPath = "C:/Users/Besitzer/openLCA-data-1.4/databases/zauto_bridge_test"
    val procID = "16cb496c-497f-3595-ba7d-df4e255c4b6c"
    val db = DerbyDatabase(File(dbPath))

    val process = ProcessDao(db).getForRefId(procID)
    if (process == null) {
        println("Could not find process with id = ${procID} in database")
        return
    }
    println("Try to generate proxy processes for: ${process.name}")

    // flowProviders contains the products (or waste flows) for
    // which there is a provider (or treatment process) in the database
    // in a map { flowID -> provider }
    val flowProviders = ProcessTable.create(db).providers.fold(
        mutableMapOf<Long, ProcessProduct>(), { map, processProduct ->
            map[processProduct.flowId()] = processProduct
            map
        })
    val products = FlowDao(db).getForIds(flowProviders.keys)
    println("There are ${products.size} products in the database")
    if (products.size == 0)
        return

    for (e in process.exchanges) {
        if (!isLinkable(e))
            continue
        val provider = flowProviders[e.flow.id]
        if (provider != null) {
            // there is already a provider for the given flow
            continue
        }
        generateProxy(e.flow, products, db)
    }
}

/** An exchange is linkable if it is a product input or waste output.*/
fun isLinkable(e: Exchange): Boolean {
    val flowType = e.flow?.flowType ?: return false
    if (e.isInput && flowType == FlowType.PRODUCT_FLOW)
        return true
    if (!e.isInput && flowType == FlowType.WASTE_FLOW)
        return true
    return false
}

fun generateProxy(flow: Flow, products: List<Flow>, db: IDatabase) {
    val candidates = products.filter { p ->
        p.flowType == flow.flowType
                && Objects.equals(
            p.referenceFlowProperty,
            flow.referenceFlowProperty
        )
    }
    if (candidates.isEmpty()) {
        println("No matching flows with type and unit found for: ${flow.name}")
        return
    }

    val allBigrams = { s: String ->
        val bgs = mutableListOf<String>()
        for (word in words(s)) {
            bgs.addAll(bigrams(word))
        }
        bgs
    }
    val flowBigrams = allBigrams(flow.name)
    var matchFactors: Map<Long, Double> = candidates.fold(
        mutableMapOf(), { map, candidate ->
            val candBigrams = allBigrams(candidate.name)
            map[candidate.id] = dice(flowBigrams, candBigrams)
            map
        })

    val maxFactor = matchFactors.values.fold(.0, { a, b -> max(a, b) })
    if (maxFactor == .0) {
        println("No match factor > 0 found for: ${flow.name}")
        return
    }
    matchFactors = matchFactors.filter { (_, factor) ->
        factor / maxFactor > 0.1
    }
    val factorsTotal = matchFactors.values.fold(.0, { a, b -> a + b })
    val proxy = initProxy(flow, db)

    for (candidate in candidates) {
        val factor = matchFactors[candidate.id] ?: continue
        val exchange = proxy.exchange(candidate)
        exchange.isInput = flow.flowType == FlowType.PRODUCT_FLOW
        exchange.amount = factor / factorsTotal
    }

    ProcessDao(db).insert(proxy)
    println("Created proxy process ${proxy.name}")
}

fun initProxy(flow: Flow, db: IDatabase): Process{
    val p = Process()
    p.category = CategoryDao(db).sync(ModelType.PROCESS, "_proxies")
    p.refId = UUID.randomUUID().toString()
    p.name = "_proxy: ${flow.name}"
    p.processType = ProcessType.UNIT_PROCESS
    val qref = p.exchange(flow)
    qref.isInput = flow.flowType == FlowType.WASTE_FLOW
    p.quantitativeReference = qref
    return p
}