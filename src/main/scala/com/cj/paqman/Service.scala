package com.cj.paqman

class Service(val datas:Data, val authMechanism:AuthMechanism) {
    def getUserWithCreateIfNeeded(emailAddress:String) = {
        datas.users.get(emailAddress) match{
        case None => if(authMechanism.emailExists(emailAddress)){
            val user =  UserInfo(id=emailAddress, qualifications=Seq())
                    datas.users.put(emailAddress, user)
                    Some(user)
        }else{
            None
        }
        case Some(user)=> Some(user)
        }
    }
    
    def userStatus(qualId:String, user:UserInfo):PersonStatus = {
      userStatus(
              qual = datas.qualifications.getHistory(qualId).get, 
              user=user)
    }
    
    
    
      
      case class HunkHistory(versions:Seq[Hunk]){
        
        def lastSignificantEdit = {
           val lastSignificantEdit = versions.find{hunk=>
              hunk.replacementInfo match {
                case None => true
                case Some(r) => r.isSignificantEdit
              }
          }
          lastSignificantEdit.get
        }
        
        def isPassed(i:QualificationInfo) = {
          
          val acceptableVersions = versions.foldLeft(List[Hunk]()){(accum, v)=>
            if(accum.isEmpty) {
              List(v)
            }else{
                val isSig = accum.last.replacementInfo match {
                case Some(r)=> r.isSignificantEdit
                case None => false
                }
                if(isSig) accum else accum :+ v
            }
          }
          
          val isPassed = acceptableVersions.map(_.id).find(i.passedChallenges.contains(_)).isDefined
          isPassed
        }
      }
      
    
    def hunkHistories(qual:Record[Qual]) = {
      
      val currentHunks = qual.latest.hunks.map{latestH=>
          val hunkHistory = qual.history.reverse.tail.foldLeft(List[Hunk](latestH)){(hunksSoFar, q) =>
            
            hunksSoFar.last.replacementInfo match {
              case None=> hunksSoFar
              case Some(r) => {
                val h = q.hunks.find(_.id == r.replacesId).get
                hunksSoFar :+ h
              }
            }
          }
          
          HunkHistory(hunkHistory)
      }
      currentHunks
    }
    
    def userStatus(qual:Record[Qual], user:UserInfo):PersonStatus = {
      val qualId = qual.latest.id
      val maybeUserQualInfo = user.qualifications.find(_.id == qualId)
      val hasPassedSomeChallenges = maybeUserQualInfo match {
        case None=>false
        case Some(userQualInfo)=>userQualInfo.passedChallenges.size>0
      }
      
      val versionsPassed = qual.history.filter(user.hasPassed(_))
      
      val passedVersions = qual.history.filter(user.hasPassed(_))
      
      val wasCurrent = !passedVersions.isEmpty
      
      
      val currentHunks = hunkHistories(qual)
      
      val isCurrent = maybeUserQualInfo match {
        case Some(qualInfo)=>{
          currentHunks.forall{i=> i.isPassed(qualInfo)}
        }
        case None=>false
      }
      val latestSignificantVersions = currentHunks.map(_.lastSignificantEdit)
      
      val challenges = latestSignificantVersions.filter(_.kind == "challenge")
      
      val foo = maybeUserQualInfo match {
          case None=>false
          case Some(userQualInfo)=> userQualInfo.passedChallenges.map{hunkId=>
            qual.history.find{q=>
                val maybeMatchingHunk = q.hunks.find{h=>
                  val isReplacement = h.replacementInfo match {
                    case None => false
                    case Some(replacementInfo)=>replacementInfo.replacesId == hunkId
                  }
                  val isHunk = h.id == hunkId
                  isHunk || isReplacement
                }
                maybeMatchingHunk.isDefined
            }
          }
        }
      
      val passedChallenges = challenges.filter{hunk=>
        maybeUserQualInfo match {
          case None=>false
          case Some(userQualInfo)=> userQualInfo.passedChallenges.contains(hunk.id)
        }
      }
      
      val isAdministrator = qual.latest.administrator == user.id
      
      PersonStatus(email=user.id, isAdministrator=isAdministrator, isCurrent=isCurrent, wasCurrent=wasCurrent, 
                        hasPassedSomeChallenges=hasPassedSomeChallenges,
                        passedChallenges=passedChallenges.map(_.toHunkInfo),
                        challengesYetToDo=challenges.filterNot(passedChallenges.contains(_)).map(_.toHunkInfo))
    }
}