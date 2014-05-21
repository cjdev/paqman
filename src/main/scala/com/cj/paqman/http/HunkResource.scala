package com.cj.paqman.http

import org.httpobjects._
import org.httpobjects.DSL._
import com.cj.paqman.Data
import com.cj.paqman.Jackson
import com.cj.paqman.Qual
import com.cj.paqman.Hunk

class HunkResource (datas:Data) extends HttpObject("/api/quals/{id}/hunks/{hunkId}"){
  
  case class QualHunk(qual:Qual, hunk:Hunk)
  
  private def findHunk(r:Request) = {
    val id = r.path().valueFor("id")
    val hunkId = r.path().valueFor("hunkId")
    
    datas.qualifications.get(id) match {
      case Some(qual) => qual.hunks.find(_.id == hunkId) match {
        case Some(hunk)=> Some(QualHunk(qual, hunk))
        case None=> None
      }
      case None => None
    }
  }
  
  override def get(r:Request) = findHunk(r) match {
    case Some(qualHunk) =>
      OK(Json(Jackson.generate(qualHunk.hunk)))
    case None=> NOT_FOUND
  }
  
  private def sameByReference(a:Any, b:Any): Boolean = a.asInstanceOf[AnyRef].eq(b.asInstanceOf[AnyRef])
  
  private def referenceReplacer[T](from:T, to:T):(T)=>T = {candidate:T =>  
    if(sameByReference(candidate, from)) to else candidate
  }
  override def delete(r:Request) = findHunk(r) match {
    case Some(qualHunk) =>
      val qual = qualHunk.qual
      val updatedQual = qual.copy(
                              hunks = qual.hunks.filterNot(qualHunk.hunk.id == _.id))
                              
      datas.qualifications.put(qual.id, updatedQual)
      OK(Text(""))
    case None=> NOT_FOUND
  }
  override def put(r:Request) = findHunk(r) match {
    case Some(qualHunk) =>
      val existingHunk = qualHunk.hunk
      val qual = qualHunk.qual
      val updatedHunk = existingHunk.updateFrom(Jackson.readJson[Hunk](r.representation()), now = System.currentTimeMillis())
      val updatedQual = qual.copy(
                              hunks = qual.hunks.map(referenceReplacer(
                                              from=existingHunk, 
                                              to=updatedHunk)))
                              
      datas.qualifications.put(qual.id, updatedQual)
      OK(Json(Jackson.generate(updatedHunk)))
    case None=> NOT_FOUND
  }
}
