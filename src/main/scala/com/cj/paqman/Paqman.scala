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
  
    def main(args: Array[String]) {
        val port = 43280
        val configFilePath = new File("config.json")
        val datas = new Data(new File("data"))
        
        val authMechanism = loadAuthMechanism(configFilePath)
        val service = new Service(datas=datas, authMechanism=authMechanism)
        
        HttpObjectsJettyHandler.launchServer(port,
            new SessionFactoryResource(datas=datas, authMechanism=authMechanism, service=service),
            new SessionResource(data=datas),
            new QualificationsResource(datas=datas, service=service),
            new QualResource(datas=datas),
            new ChallengePeopleResource(data=datas, service=service),
            new HunksResource(datas=datas),
            new HunkResource(datas=datas),
            new QualPeopleResource(data=datas, service=service),
            new ClasspathResourceObject("/", "/content/index.html", getClass()),
            new ClasspathResourcesObject("/{resource*}", getClass(), "/content"),
            new QualUIResource(data=datas)
        );
        
        println("paqman is alive and listening on port " + port);
    }
    
    def loadAuthMechanism(configFilePath:File) = if(configFilePath.exists()){
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
}

class QualUIResource (data:Data) extends ClasspathResourceObject("/{name}", "/content/qual.html", getClass()){
  override def get(r:Request) = {
    val name = URLDecoder.decode(r.path().valueFor("name"))
    val maybeQual = data.qualifications.toSeq().map{_.latest}.find(_.name == name);
    maybeQual match {
      case Some(qual) => super.get(r);
      case None => null
    }
  }
}