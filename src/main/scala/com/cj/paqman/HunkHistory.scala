package com.cj.paqman

case class HunkHistory(versions:Seq[HunkVersion]){
    val hunkId = latestVersion.hunkId
    val name = latestVersion.name
  
    def latestVersion = versions.last
    def lastSignificantEdit = certifiableVersions.last
    
    def certifiableVersions = {
       val maybeLastSignificantEdit = versions.reverse.find{hunk=>
          hunk.isSignificantEdit
      }
      
      maybeLastSignificantEdit match {
        case None => Seq()
        case Some(lastSignificantEdit) => versions.filter(_.whenAdded >= lastSignificantEdit.whenAdded)
      }
      
    }

    def isCurrent(user:UserInfo):Boolean = {
      val maybePassedVersion = certifiableVersions.find{hunkVersion=>
        val passedVersions = user.events.filter(hunkId == _.challengeId).map(_.challengeVersion)
        passedVersions.contains(hunkVersion.versionId)
      }
      maybePassedVersion.isDefined
    }
    
    def hasPassed(user:UserInfo):Boolean = user.events.exists{e=>e.challengeId ==hunkId}
}