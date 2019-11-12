package autoprox

import org.openlca.core.database.FlowDao
import org.openlca.core.database.ProcessDao
import org.openlca.core.database.derby.DerbyDatabase
import org.openlca.core.matrix.ProcessProduct
import org.openlca.core.matrix.cache.ProcessTable
import org.openlca.core.model.Exchange
import org.openlca.core.model.Flow
import org.openlca.core.model.FlowType
import java.io.File
import java.util.*

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
        generateProxy(e.flow, products)
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

fun generateProxy(flow: Flow, products: List<Flow>) {
    val isWaste = flow.flowType == FlowType.WASTE_FLOW

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



}