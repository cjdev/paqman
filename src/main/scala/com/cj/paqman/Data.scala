
package com.cj.paqman

import java.io.File

class Data(dataDir:File) extends DataInterface {
    val qualifications = new Database(new File(dataDir, "quals"), classOf[Qual])
    val users = new Database(new File(dataDir, "users"), classOf[UserInfo])
    val sessions = new Database(new File(dataDir, "sessions"), classOf[Session])
}