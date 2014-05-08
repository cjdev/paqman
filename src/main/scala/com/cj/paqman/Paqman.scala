package com.cj.paqman

import java.io.ByteArrayOutputStream
import java.net.URLDecoder
import java.util.UUID
import scala.reflect.BeanProperty
import org.httpobjects.DSL._
import org.httpobjects.HttpObject
import org.httpobjects.Representation
import org.httpobjects.Request
import org.httpobjects.jetty.HttpObjectsJettyHandler
import org.httpobjects.util.ClasspathResourceObject
import org.httpobjects.util.ClasspathResourcesObject
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import java.io.File
import org.apache.commons.io.FileUtils
import com.cj.paqman.http._

case class AuthRequest(val email:String, val password:String)
case class UserQualStatus(val id:String, val isQualified:Boolean, val challengesMet:Set[String])
case class SessionInfo(val email:String, val qualifications:Seq[UserQualStatus])
case class QualSummary(val id:String, val name:String, val description:String, val ref:String)
case class Session(val email:String)
case class QualDto(val id:String , val name:String, val description:String, val hunks:Seq[Hunk], val administrator:String, val proctors:Seq[String]){
  def this(q:Qual, proctors:Seq[String]) = this(id = q.id, name=q.name, description = q.description, hunks = q.hunks, administrator = q.administrator, proctors=proctors)
}
case class HunkInfo(id:String, name:String)
case class PersonStatus(email:String, isAdministrator:Boolean,
            isCurrent:Boolean, wasCurrent:Boolean, 
            hasPassedSomeChallenges:Boolean, 
            passedChallenges:Seq[HunkInfo],
            challengesYetToDo:Seq[HunkInfo])
  
object Paqman {
  
  private def replace[T](current:T, replacement:T, seq:Seq[T]) = {
    val idx = seq.indexOf(current)
    val buffer = seq.toBuffer
    buffer.update(idx, replacement)
    buffer.toSeq
  }
  
  val jackson = new ObjectMapper() with ScalaObjectMapper
  jackson.registerModule(DefaultScalaModule)
  
  def readJson[T](r:Representation)(implicit mf:Manifest[T]):T = {
    val bytes = new ByteArrayOutputStream()
    r.write(bytes);
    bytes.close();
    
    jackson.readValue(bytes.toByteArray());
  }

  def generate(o:AnyRef) = jackson.writeValueAsString(o);
  
    def main(args: Array[String]) {
        val configFilePath = new File("config.json")
        val port = 43280
        val datas = new Data(new File("data"))
        
        val authMechanism = if(configFilePath.exists()){
          val config = ConfigFile.read(configFilePath)
              new LdapTool(
                  url=config.ldapUrl, 
                  ldapUser = config.ldapUser, 
                  ldapPassword = config.ldapPassword)
        }else{
          new AuthMechanism(){
            def authenticateEmail(email:String, password:String) = Option(AuthDetails())
                def emailExists(email:String) = true
          }
        }
        val service = new Service(datas=datas, authMechanism=authMechanism)
        val sessionFactoryResource = new SessionFactoryResource(datas=datas, authMechanism=authMechanism, service=service)
        
        
        val sessionResource = new HttpObject("/api/sessions/{id}"){
            override def get(r:Request)={
              val id = r.path().valueFor("id")
              datas.sessions.get(id) match {
                case None => NOT_FOUND()
                case Some(session) => {
                  datas.users.get(session.email) match {
                        case Some(user) => {
                          val myQuals = user.qualifications.map{q=>
                          val qual = datas.qualifications.get(q.id).get
                            if(user.id == qual.administrator){
                              UserQualStatus(
                                id=q.id,
                                isQualified = true,
                                challengesMet = qual.hunks.filter(_.kind == "challenge").map(_.id).toSet)
                            }else{
                              UserQualStatus(
                                id=q.id, 
                                isQualified=q.hasPassed(datas.qualifications), 
                                challengesMet=q.passedChallenges)
                            }
                          }
                          OK(Json(generate(SessionInfo(email = session.email, qualifications = myQuals))))
                        }
                        case None => INTERNAL_SERVER_ERROR
                    }
                  }
              }
                          
            }
        }
        
        val qualificationsResource = new HttpObject("/api/quals"){
          override def get(r:Request) = {
            
            val summaries = datas.qualifications.toSeq().map(_.latest).map{qual=>
              QualSummary(id = qual.id, name=qual.name, description = qual.description, ref= "/api/quals/" + qual.id)
            }
            OK(Json(generate(summaries)))
          }
          override def post(r:Request) = {
            val input = readJson[Qual](r.representation())
            val aQualExistsWithThisNameAlready = datas.qualifications.toSeq.map(_.latest).find(_.name == input.name).isDefined
            
            if(aQualExistsWithThisNameAlready || !authMechanism.emailExists(input.administrator)){
              BAD_REQUEST
            }else{
              val qual = Qual(
                          id = UUID.randomUUID().toString(), 
                          name=input.name, 
                          description = input.description, 
                          administrator = input.administrator, 
                          hunks = Seq())
                          
              datas.qualifications.put(qual.id, qual);
              CREATED(Location("/api/quals/" + qual.id))
            }
          }
        }
        
        val qualificationResource = new HttpObject("/api/quals/{id}"){
          override def get(r:Request) = {
            val id = r.path().valueFor("id")
            val maybeQual = datas.qualifications.get(id)
            maybeQual match {
              case Some(qual) => {
                val proctors = datas.users.toSeq.map(_.latest).filter(_.hasPassed(qual)).map(_.id) :+ qual.administrator
                val dto = new QualDto(qual, proctors)
                OK(Json(generate(dto)))
              }
              case None => NOT_FOUND
            }
          }
        }
        
        val hunksResource = new HttpObject("/api/quals/{id}/hunks"){
          
          override def post(r:Request) = {
            val id = r.path().valueFor("id")
            val maybeQual = datas.qualifications.get(id)
            maybeQual match {
              case Some(qual) => {
                val newHunk = readJson[Hunk](r.representation()).copy(id=UUID.randomUUID().toString())
                
                val updatedQual = qual.copy(hunks = (qual.hunks :+ newHunk))
                datas.qualifications.put(id, updatedQual);
                CREATED(Location(s"/api/quals/${id}/hunks/${newHunk.id}"))
              }
              case None => NOT_FOUND
            }
          }
        }
        
        val hunkResource = new HttpObject("/api/quals/{id}/hunks/{hunkId}"){
          
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
            case Some(qualHunk) => {
              OK(Json(generate(qualHunk.hunk)))
            }
            case None=> NOT_FOUND
          }
          
          private def sameByReference(a:Any, b:Any): Boolean = a.asInstanceOf[AnyRef].eq(b.asInstanceOf[AnyRef])
          
          private def referenceReplacer[T](from:T, to:T):(T)=>T = {candidate:T =>  
            if(sameByReference(candidate, from)) to else candidate
          }
          override def delete(r:Request) = findHunk(r) match {
            case Some(qualHunk) => {
              val qual = qualHunk.qual
              val updatedQual = qual.copy(
                                      hunks = qual.hunks.filterNot(qualHunk.hunk.id == _.id))
                                      
              datas.qualifications.put(qual.id, updatedQual);
              OK(Text(""))
            }
            case None=> NOT_FOUND
          }
          override def put(r:Request) = findHunk(r) match {
            case Some(qualHunk) => {
              val existingHunk = qualHunk.hunk
              val qual = qualHunk.qual
              val updatedHunk = existingHunk.updateFrom(readJson[Hunk](r.representation()), now = System.currentTimeMillis())
              val updatedQual = qual.copy(
                                      hunks = qual.hunks.map(referenceReplacer(
                                                      from=existingHunk, 
                                                      to=updatedHunk)))
                                      
              datas.qualifications.put(qual.id, updatedQual);
              OK(Json(generate(updatedHunk)))
            }
            case None=> NOT_FOUND
          }
        }

        val qualPeopleResource = new HttpObject("/api/quals/{id}/people"){
          override def get(r:Request) = {
            val qualId = r.path().valueFor("id")
            datas.qualifications.getHistory(qualId) match {
              case None =>BAD_REQUEST
              case Some(qual)=>{
                val result = datas.users.toSeq.map(_.latest).flatMap{user=>
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
                  
                  val challenges = qual.latest.hunks.filter(_.kind == "challenge")
                  val passedChallenges = challenges.filter{hunk=>
                    maybeUserQualInfo match {
                      case None=>false
                      case Some(userQualInfo)=> userQualInfo.passedChallenges.contains(hunk.id)
                    }
                  }
                  
                  val isAdministrator = qual.latest.administrator == user.id
                  
                  if(hasPassedSomeChallenges || isAdministrator){
                    Some(PersonStatus(email=user.id, isAdministrator=isAdministrator, isCurrent=isCurrent, wasCurrent=wasCurrent, 
                        hasPassedSomeChallenges=hasPassedSomeChallenges,
                        passedChallenges=passedChallenges.map(_.toHunkInfo),
                        challengesYetToDo=challenges.filterNot(passedChallenges.contains(_)).map(_.toHunkInfo)))
                  }else{
                      None
                  }
                }
                OK(Json(generate(result)))
              }
            }
          }
        }
                
        val userQualificationsResource = new HttpObject("/api/quals/{id}/challenges/{challengeId}/people"){
          
          override def post(r:Request) = {
            val qualId = r.path().valueFor("id")
            val challengeId = r.path().valueFor("challengeId")
            val emailAddress = readJson[String](r.representation())
            datas.qualifications.get(qualId) match {
              case None =>BAD_REQUEST()
              case Some(qual)=>{
                val user = service.getUserWithCreateIfNeeded(emailAddress).get
                def isThis(q:QualificationInfo) = q.id == qualId
                
                val qualInfo:QualificationInfo = user.qualifications.find(isThis).getOrElse(new QualificationInfo(id=qualId))
                val otherQuals = user.qualifications.filterNot(isThis)
                
                val passedChallenges = qualInfo.passedChallenges + challengeId
                val updatedQual = qualInfo.copy(passedChallenges = passedChallenges)
                datas.users.put(emailAddress, user.copy(qualifications=otherQuals :+ updatedQual))
                OK(Text("yo"))
              }
            }
          }
        }
        
        val qualUiResource = new ClasspathResourceObject("/{name}", "/content/qual.html", getClass()){
          override def get(r:Request) = {
            val name = URLDecoder.decode(r.path().valueFor("name"))
            val maybeQual = datas.qualifications.toSeq().map{_.latest}.find(_.name == name);
            maybeQual match {
              case Some(qual) => super.get(r);
              case None => null
            }
          }
        }
        
        HttpObjectsJettyHandler.launchServer(port,
            sessionFactoryResource,
            sessionResource,
            qualificationsResource,
            qualificationResource,
            userQualificationsResource,
            hunksResource,
            hunkResource,
            qualPeopleResource,
            new ClasspathResourceObject("/", "/content/index.html", getClass()),
            new ClasspathResourcesObject("/{resource*}", getClass(), "/content"),
            qualUiResource
        );
        
        println("paqman is alive and listening on port " + port);
    }
    
}