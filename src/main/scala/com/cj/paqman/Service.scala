package com.cj.paqman

import org.joda.time.DateTime

object Service {
  
    def hunkHistories(versionsEarliestToLatest:Seq[Qual]) = {
      
      val headsOfEachHunkStreamEver = versionsEarliestToLatest.foldLeft(List[HunkVersion]()){(accum, qual)=>
        val hunksNotInAccum = qual.hunks.filter{h=> !accum.map(_.hunkId).contains(h.hunkId)}
//        println(hunksNotInAccum.size + " new hunks in " + qual.id + " - " + qual.name +": " + hunksNotInAccum.map{h=>h.name + " added " +  new DateTime(h.whenAdded) + " " + h.hunkId  + " " + h.isSignificantEdit  })
        accum ::: hunksNotInAccum.toList
      }
      val hunkHistories = headsOfEachHunkStreamEver.map{firstVersion=>
          val hunkHistory = versionsEarliestToLatest.foldLeft(List[HunkVersion](firstVersion)){(hunksSoFar, nextQualVersion) =>
            if(hunksSoFar.isEmpty) hunksSoFar
            else {
            	val latestSoFar = hunksSoFar.last
    			val maybeReplacement = nextQualVersion.hunks.find{h=> h.hunkId == latestSoFar.hunkId}
            	
            	maybeReplacement match {
            	case Some(replacement) => {
            		hunksSoFar :+ replacement
            	}
            	case None => hunksSoFar
            	}
            }
          }
          HunkHistory(hunkHistory.distinct)
      }
      
      hunkHistories
    }
}

class Service(val datas:DataInterface, val authMechanism:AuthMechanism) {
  
  def getUserWithCreateIfNeeded(emailAddress:String) = {
      datas.users.get(emailAddress) match{
      case None => if(authMechanism.emailExists(emailAddress)){
          val user =  UserInfo(id=emailAddress, events=List())
                  datas.users.put(emailAddress, user)
                  Some(user)
      }else{
          None
      }
      case Some(user)=> Some(user)
      }
  }
  
  
  
  def getHunkHistory(qualId:String, h:HunkVersion): HunkHistory = getHunkHistory(qualId, h.hunkId)
  
  def getHunkHistory(qualId:String, hunkId:String): HunkHistory = {
    val qual = datas.qualifications.getHistory(qualId).get
    
    val histories = Service.hunkHistories(versionsEarliestToLatest = qual.history)
    histories.find(_.latestVersion.hunkId ==hunkId).get
  }
  
  
  def userIsCurrent(user:UserInfo, record:Record[Qual]) = new QualWithHistory(record).isCurrent(user)
  
}
