package com.cj.paqman


case class QualificationInfo(id:String, passedChallenges:Set[String] = Set()){
  def hasPassed(db:Database[Qual]) = {
    db.get(id) match {
      case None=> false
      case Some(qual) => {
        qual.hasPassed(this)
      }
    }
    
  }
}


case class QualificationFoo(val qualId:String, val challengesMissing:Seq[String]){
  def hasPassed = challengesMissing.isEmpty
}

// has passed before
// has met one or more challenges
// nothing

case class UserInfo (val id:String, val qualifications:Seq[QualificationInfo]){
  def hasPassed(q:Qual) = qualifications.find(q.hasPassed(_)).isDefined
}