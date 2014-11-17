package com.cj.paqman.http

import org.httpobjects._
import org.httpobjects.DSL._
import com.cj.paqman.Data
import com.cj.paqman.Jackson
import com.cj.paqman.Qual
import com.cj.paqman.HunkVersion
import com.cj.paqman.api.HunkDto

class HunkResource (datas:Data) extends HttpObject("/api/quals/{id}/hunks/{hunkId}"){
  
  override def get(r:Request) = findHunk(r) match {
    case Some(qualHunk) =>
      OK(Json(Jackson.generate(qualHunk.hunk)))
    case None=> NOT_FOUND
  }
  
  override def delete(r:Request) = findHunk(r) match {
    case Some(qualHunk) =>
      val qual = qualHunk.qual
      val updatedQual = qual.copy(
                              hunks = qual.hunks.filterNot(qualHunk.hunk.hunkId == _.hunkId))
                              
      datas.qualifications.put(qual.id, updatedQual)
      OK(Text(""))
    case None=> NOT_FOUND
  }
  
  override def put(r:Request) = findHunk(r) match {
    case Some(qualHunk) =>
      val dto = Jackson.readJson[HunkDto](r.representation())
      val existingHunk = qualHunk.hunk
      val qual = qualHunk.qual
      val updatedHunk = existingHunk.withUpdatesFromDto(dto, now = System.currentTimeMillis())
      val updatedQual = qual.copy(
                              hunks = qual.hunks.map{existing=>if(existing.hunkId==updatedHunk.hunkId) updatedHunk else existing})
                              
      datas.qualifications.put(qual.id, updatedQual)
      OK(Json(Jackson.generate(updatedHunk)))
    case None=> NOT_FOUND
  }
  
  
  case class QualHunk(qual:Qual, hunk:HunkVersion)
  
  private def findHunk(r:Request) = {
    val id = r.path().valueFor("id")
    val hunkId = r.path().valueFor("hunkId")
    
    datas.qualifications.get(id) match {
      case Some(qual) => qual.hunks.find(_.hunkId == hunkId) match {
        case Some(hunk)=> Some(QualHunk(qual, hunk))
        case None=> None
      }
      case None => None
    }
  }
}
