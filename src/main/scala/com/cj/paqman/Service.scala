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
      val isCurrent = user.hasPassed(qual.latest)
      
      
      println(s"user ${user} isCurrent=$isCurrent, wasCurrent=$wasCurrent hasPassedSomeChallenges=$hasPassedSomeChallenges passed $passedVersions")
      
      
      val latestSignificantVersions = qual.latest.hunks.map{latestH=>
          val hunkHistory = qual.history.tail.foldLeft(List[Hunk](latestH)){(hunksSoFar, q) =>
            hunksSoFar.last.replacementInfo match {
              case None=> hunksSoFar
              case Some(r) => {
                hunksSoFar ++ q.hunkOrReplacmentForHunk(r.replacesId)
              }
            }
          }
          
          val lastSignificantEdit = hunkHistory.find{hunk=>
              hunk.replacementInfo match {
                case None => true
                case Some(r) => r.isSignificantEdit
              }
          }
          lastSignificantEdit.get
      }
      
      val challenges = latestSignificantVersions.filter(_.kind == "challenge")
      
      println(s"sigs: $challenges")
      
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