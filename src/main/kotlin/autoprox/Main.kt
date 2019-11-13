package autoprox

import org.openlca.core.database.derby.DerbyDatabase
import java.io.File

fun main() {
    val dbPath = "C:/Users/ms/openLCA-data-1.4/databases/z_autoprox_test"
    val procID = "16cb496c-497f-3595-ba7d-df4e255c4b6c"
    val db = DerbyDatabase(File(dbPath))
    val gen = Generator(db, WordNetPathMatcher())
    gen.epsilon = 0.1
    gen.doIt(procID)
}
