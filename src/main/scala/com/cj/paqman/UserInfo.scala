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


case class QualificationFoo(qualId:String, challengesMissing:Seq[String]){
  def hasPassed = challengesMissing.isEmpty
}

// has passed before
// has met one or more challenges
// nothing

case class UserInfo (id:String, qualifications:Seq[QualificationInfo]){
  def hasPassed(q:Qual) = qualifications.exists(q.hasPassed)
}
