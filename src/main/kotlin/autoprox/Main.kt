package autoprox

import org.openlca.core.database.ProcessDao
import org.openlca.core.database.derby.DerbyDatabase
import org.openlca.core.matrix.cache.ProcessTable
import java.io.File

fun main() {
    val dbPath = "C:/Users/Besitzer/openLCA-data-1.4/databases/zauto_bridge_test"
    val procID = "16cb496c-497f-3595-ba7d-df4e255c4b6c"
    val db = DerbyDatabase(File(dbPath))

    val providers = ProcessTable.create(db).providers
    val process = ProcessDao(db).getForRefId(procID)


    print(process.name)
}


