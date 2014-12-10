package com.cj.paqman

import java.net.URLDecoder
import org.httpobjects.Request
import org.httpobjects.jetty.HttpObjectsJettyHandler
import org.httpobjects.util.ClasspathResourceObject
import org.httpobjects.util.ClasspathResourcesObject
import com.cj.paqman.http._
import java.io.File
import org.httpobjects.HttpObject

case class AuthRequest(email:String, password:String)
case class SessionInfo(email:String, qualifications:Seq[QualPersonStatus])
case class QualSummary(id:String, name:String, description:String, administrator:String, ref:String)
case class Session(email:String)
case class QualDto(id:String , name:String, description:String, hunks:Seq[HunkVersion], administrator:String, proctors:Seq[String]){
  def this(q:Qual, proctors:Seq[String]) = this(q.id,q.name,q.description,q.hunks,q.administrator,proctors)
}
case class HunkInfo(id:String, name:String)
case class QualPersonStatus(
            qualId:String,
			email:String, 
			isAdministrator:Boolean,
            isCurrent:Boolean, 
            wasCurrent:Boolean, 
            challenges:Seq[ChallengeStatus])
case class ChallengeStatus(challengeId:String, name:String, hasPassed:Boolean, isCurrent:Boolean)
  
object Paqman {
  
    def main(args: Array[String]) {
        val port = 43280
        val configFilePath = new File("config.json")
        val datas = new Data(new File("data"))
        
        val authMechanism = loadAuthMechanism(configFilePath)
        val service = new Service(datas=datas, authMechanism=authMechanism)
        
        HttpObjectsJettyHandler.launchServer(port,
            new SessionFactoryResource(datas=datas, authMechanism=authMechanism, service=service),
            new SessionResource(data=datas, service=service),
            new QualificationsResource(datas=datas, service=service),
            new QualResource(datas=datas, service=service),
            new ChallengePeopleResource(data=datas, service=service),
            new HunksResource(datas=datas),
            new HunkResource(datas=datas),
            new QualPeopleResource(data=datas, service=service),
            new ClasspathResourceObject("/", "/content/index.html", getClass),
            new ClasspathResourcesObject("/{resource*}", getClass, "/content"),
            new QualUIResource(data=datas)
        )
        
        println("paqman is alive and listening on port " + port)
    }
    
    def loadAuthMechanism(configFilePath:File) = if(configFilePath.exists()){
      val config = ConfigFile.read(configFilePath)
          new LdapTool(
              url=config.ldapUrl, 
              ldapUser = config.ldapUser, 
              ldapPassword = config.ldapPassword)
    }else{
      new AuthMechanism(){
        def authenticateEmail(email:String, password:String) = Option(AuthDetailsPlaceholder)
            def emailExists(email:String) = true
      }
    }
}

class QualUIResource (data:Data) extends ClasspathResourceObject("/{name}", "/content/qual.html", getClass){
  override def get(r:Request) = {
    val name = URLDecoder.decode(r.path().valueFor("name"), "utf-8")
    val maybeQual = data.qualifications.toSeq().map{_.latest}.find(_.name == name)
    maybeQual match {
      case Some(qual) => super.get(r);
      case None => null
    }
  }
}