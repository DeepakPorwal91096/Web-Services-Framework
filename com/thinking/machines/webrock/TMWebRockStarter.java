package com.thinking.machines.webrock;
import com.thinking.machines.webrock.pojo.*;
import com.thinking.machines.webrock.annotations.*;
import com.thinking.machines.webrock.model.*;
import com.thinking.machines.webrock.exception.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.stream.Stream;
import java.io.*;
public class TMWebRockStarter  implements ServletContextListener
{
private WebRockModel webRockModel=new WebRockModel();
public void contextInitialized(ServletContextEvent event)
{
Service service;
try
{
System.out.println("Servlet got calledTMWebRockStarter got called");
ServletContext sc=event.getServletContext();
String servletClass=sc.getInitParameter("SERVICE_PACKAGE_PREFIX");
servletClass=servletClass.replace(".",File.separator);
// extracting path of web-inf to find real path
        String webXmlPath = sc.getRealPath("/WEB-INF");
        String realPath=webXmlPath+File.separator+"classes"+File.separator+servletClass;
// starting  scanning part start here

java.nio.file.Path folderPath =java.nio.file.Paths.get(realPath);
List<String> list=new LinkedList<>();
try (Stream<java.nio.file.Path> paths = Files.walk(folderPath)) {
 // Filter files with ".class" extension
int i=0;
paths.filter(path -> path.toString().endsWith(".class")).forEach(path->{
list.add(path.toString());
}); 

} catch (IOException e) {
e.printStackTrace();
return;
}// Scanning part ends here


Path path;
for(int index=0;index<list.size();index++)
{
String g=list.get(index);
int ii=g.indexOf(".class");
String newString=g.substring(0,ii);
ii=g.indexOf(servletClass);
newString=newString.substring(ii);
newString=newString.replace(File.separator,".");
Class serviceClass;
try
{
serviceClass=Class.forName(newString);
}catch(Exception exception)
{
System.out.println("Unable to process given class : "+exception.getMessage());//done
continue;
}
if(serviceClass.isAnnotationPresent(Path.class)){
Annotation annotation=serviceClass.getAnnotation(Path.class);
path=(Path)annotation;// we can also right directly Path path=serviceClass.getAnnotation(Path.class);
// method part starts here
Method methods[]=serviceClass.getDeclaredMethods();
Field fields[]=serviceClass.getDeclaredFields();
Annotation classGetAnnotation=serviceClass.getAnnotation(Get.class);// here we are doing to identify Get & Post annotation is not applied on class level
Annotation classPostAnnotation=serviceClass.getAnnotation(Post.class);
boolean isGet=false;
boolean isPost=false;
if(classGetAnnotation!=null) isGet=true;
if(classPostAnnotation!=null) isPost=true;
if(isGet && isPost)
{
System.out.println("Unable to process (Get or Post) annotation at one time.. Please specify one at a time");
throw new WebException("Unable to process (Get or Post) annotation at one time.. Please specify one at a time");
}
TreeMap<Integer,List<Method>> onStartupMap=new TreeMap<>();
boolean found=false;
//AutoWiring Features implementing
HashMap<String,Field> autoWireMap=new HashMap<>();
for(int i=0;i<fields.length;i++)
{
AutoWired autoWired=fields[i].getAnnotation(AutoWired.class);
if(autoWired!=null)
{
System.out.printf("AutoWired Name %s, property name : %s\n",autoWired.name(),fields[i].getName());// will be removed after testing
autoWireMap.put(autoWired.name(),fields[i]);
}
}


for(int i=0;i<methods.length;i++)// methods loop starts here
{

if(methods[i].isAnnotationPresent(OnStartup.class))// here we are implementing OnStartup
{
OnStartup onStartup=methods[i].getAnnotation(OnStartup.class);
int priority=onStartup.priority();
Class parameters[]=methods[i].getParameterTypes();
if(parameters.length>1) 
{
throw new WebException("In ONStartup (Annotation) Parameter should be zero or ApplicationScope   (methodName) : "+methods[i].getName());
}// here we have to check return type is void ?????
String m=methods[i].getReturnType().getName();
if(!m.equalsIgnoreCase("void"))
{
throw new WebException("In ONStartup (Annotation) Return Type should be void   (methodName) : "+methods[i].getName());
}
if(parameters.length==1)
{
String parameterName=parameters[0].getName();
if(parameterName.equals("com.thinking.machines.webrock.ApplicationScope")==false)
{
throw new WebException("In ONStartup (Annotation) Parameter should be zero or com.thinking.machines.webrock.ApplicationScope   (methodName) : "+methods[i].getName());
}
}
List<Method> lists=onStartupMap.get(priority);// here we are adding on TreeMap according to priority factor
if(lists==null)
{
lists=new LinkedList<>();
lists.add(methods[i]);
onStartupMap.put(priority,lists);
}else
{
lists.add(methods[i]);
}
}// OnStartup ends here



// here we are implementing Path Factor if path factor exits then we check Get or Post Factor
// and set on Service Object
Annotation a=methods[i].getAnnotation(Path.class);
if(a!=null)
{
service=new Service();

// here we are implementing ResponseHeader 
if(methods[i].isAnnotationPresent(ResponseHeader.class))
{
ResponseHeader responseHeader=methods[i].getAnnotation(ResponseHeader.class);
Class c=responseHeader.provider();
Object obj=c.newInstance();
Class clz=CustomHttpHeadersProvider.class;
boolean valid= clz.isInstance(obj);
if(valid==false) throw new WebException("Error: The class " + responseHeader.provider().getName() + " does not implement the required interface " + clz.getName());
service.isResponseHeader(true);
service.setResponseHeaderClass(c);
}
// here we are implementing ResponseHeader  ends here

// here we are implementing SecuredGuard
if(methods[i].isAnnotationPresent(Secured.class))
{
Secured secured=methods[i].getAnnotation(Secured.class);
Class c=secured.guard();
Object obj=c.newInstance();
Class clz=SecurityGuard.class;
boolean valid= clz.isInstance(obj);
if(valid==false) throw new WebException("Error: The class " + secured.guard().getName() + " does not implement the required interface " + clz.getName());
service.isSecured(true);
service.setSecuredGuardClass(c);
}
// here we are implementing ResponseHeader  ends here







// Return Type implementation starts here 
String m=methods[i].getReturnType().getName();
if(!m.equalsIgnoreCase("void"))
{
if(m.equalsIgnoreCase("java.lang.Object")) service.isReturning(true);
else{
throw new WebException("Path (Annotation) Method return type should be Object or void");
}
}
// Return Type implementation ends here 


// Parameter implementation
Class[] parameterTypes=methods[i].getParameterTypes();
// here we are implementing for Json String or Json Object
if(parameterTypes.length>0){
String s=parameterTypes[0].getName();
boolean b=getPrimitiveDataType(s);// if it is return false means it is primitive data type 
if(b==true){
service.isJSON(true);
service.setRequestParameters("json",parameterTypes[0]);	
for(int z=1;z<parameterTypes.length;z++)
{
String ss=parameterTypes[z].getName();
b=getParameterScopeType(ss);// if it is return true then parameter is scope types(ApplicationScope,Session,Request,Directory)
if(b==true)
{
service.setRequestParameters("P"+z,parameterTypes[z]);
}else
{
service.setIsMethodValid(false);
service.clearRequestParameters();// pending Exception generation we have to apply Exception 
throw new WebException("method parameter is invalid (example : method(json,ApplicationDirectory,SessionScope,....)etc )"+methods[i].getName());
}
}// loop ends here
} else{
// here we are implementing RequestParameter Annotation 
service.isJSON(false);
Annotation[][] parameterAnnotations=methods[i].getParameterAnnotations();
parameterTypes=methods[i].getParameterTypes();

for(int z=0;z<parameterTypes.length;z++)
{
try
{
Annotation requestParameterAnnotation;
try
{
// this is because of we are introducing new features we can also use Scopes like this
//public void  get(@RequestParameter("pqr") String t,@RequestParameter("lmn") int i,ApplicationScope tsd,SessionScope st,RequestScope ts)
requestParameterAnnotation=parameterAnnotations[z][0];
}catch(Exception exception)
{// here we are just doing things like this we doing things like this we exception will generated then we checking
// Scopes are available or not  
for(;z<parameterTypes.length;z++){
String ss=parameterTypes[z].getName();
b=getParameterScopeType(ss);// if it is return true then parameter is scope type
if(b==true)
{
service.setRequestParameters("P"+z,parameterTypes[z]);
}else
{
service.setIsMethodValid(false);
service.clearRequestParameters();// pending Exception generation we have to apply Exception 
throw new WebException("method parameter is invalid (example : method(@RequestParameter('pqr') String t,@RequestParameter('lmn') int i,.......ApplicationDirectory,SessionScope,....)etc ) "+methods[i].getName());
}
}// loop ends here
break;
}

if(requestParameterAnnotation instanceof RequestParameter)
{
RequestParameter requestParameter= (RequestParameter) requestParameterAnnotation;
service.setRequestParameters(requestParameter.value(),parameterTypes[z]);	
}else
{
service.setIsMethodValid(false);
service.clearRequestParameters();
throw new WebException("method parameter is invalid (example : method(@RequestParameter('pqr') String t,@RequestParameter('lmn') int i,.......ApplicationDirectory,SessionScope,....)etc )"+methods[i].getName());
}

}catch(Exception exception)
{
service.clearRequestParameters();// pending Exception generation we have to apply Exception 
service.setIsMethodValid(false);
throw new WebException("method parameter is invalid "+methods[i].getName()+"    "+exception.getMessage());
}

}// loop ends here
}// else part ends here of identifying it is primitive or not

}// parameterTypes length is greater then zero


Annotation methodPostAnnotation=null;
if(!(isGet || isPost))
{
found=true;
Annotation methodGetAnnotation=methods[i].getAnnotation(Get.class);
methodPostAnnotation=methods[i].getAnnotation(Post.class);
}

Path p=(Path)a;

String servicePath=path.value()+p.value();
service.setServiceClass(serviceClass);
service.setService(methods[i]);
service.setPath(servicePath);
// here we are adding real path in service object for ApplicationDirectoryScope;
service.setRealPath(realPath);
if(found)
{
if(methodPostAnnotation!=null) service.setIsPostAllowed(true);
else service.setIsGetAllowed(true);
}else{
if(isGet) service.setIsGetAllowed(true);
if(isPost) service.setIsPostAllowed(true);
}

// here we are implementing Forward features
if(methods[i].isAnnotationPresent(Forward.class))
{
if(service.isReturning())
{
throw new WebException("In Forward Annotation return type should be void");
}
String forwardPath=methods[i].getAnnotation(Forward.class).value();
service.setForwardTo(forwardPath);
}
// here we are checking for isSessionScope RequestScope,ApplicationScope etc.
if(methods[i].isAnnotationPresent(InjectRequestScope.class))
{
service.setInjectRequestScope(true);
}
if(methods[i].isAnnotationPresent(InjectSessionScope.class))
{
service.setInjectSessionScope(true);
}
if(methods[i].isAnnotationPresent(InjectApplicationScope.class))
{
service.setInjectApplicationScope(true);
}
if(methods[i].isAnnotationPresent(InjectApplicationDirectory.class))
{
service.setInjectApplicationDirectory(true);
}

Service exists=webRockModel.get(servicePath);
if(exists!=null)
{
Class serviceC=exists.getServiceClass();
throw new WebException("Method already exists in Class: "+serviceC.getName());
}
webRockModel.add(servicePath,service);// added on model


}
}// method loop ends here

// here we are checking priority factor of OnStartup and calling the methods
Object obj=serviceClass.newInstance();
int u=0;
boolean z;
onStartupMap.forEach((k,v)->{
List<Method> lists=v;
lists.forEach((m)->{
try
{

Class parameters[]=m.getParameterTypes();
if(parameters.length==1)
{
Object args[]=new Object[1];
ApplicationScope applicationScope=new ApplicationScope();
applicationScope.setAttribute(sc);
args[0]=applicationScope;
m.invoke(obj,args);
}
else m.invoke(obj);
}catch(Exception exception)
{
throw new RuntimeException(exception.getMessage());
}
});
});

if(autoWireMap.size()>=1) 
{
webRockModel.addToAutoWired(path.value(),autoWireMap);
}

}
}// loop ends here main loop ends here List (List) 
sc.setAttribute("map",webRockModel);
}catch(Exception ie)
{
System.out.println("Exception : -----"+ie.getMessage());
webRockModel.clear();
}
}
public void contextDestroyed(ServletContextEvent event)
{
ServletContext sc=event.getServletContext();
sc.removeAttribute("map");
}
public boolean getPrimitiveDataType(String d) 
{// if this method return false it means it data type is primitive
if(d.equals("java.lang.Long") || d.equals("long"))
{
return false;
} else if(d.equals("java.lang.Integer") || d.equals("int")){ 
return false;
} else if(d.equals("java.lang.Short") || d.equals("short")){
return false;
} else if(d.equals("java.lang.Byte") || d.equals("byte")){ return false;}
else if(d.equals("java.lang.Double") || d.equals("double")){return false;}
else if(d.equals("java.lang.Float") || d.equals("float")){return false;}
else if(d.equals("java.lang.Boolean") || d.equals("boolean")){
return false;
} else if(d.equals("java.lang.Character") || d.equals("char")){
return false;}
else if(d.equals("java.lang.String") ){
return false;
}
if(getParameterScopeType(d)) return false;
return true;
}
public boolean getParameterScopeType(String d)// if it is return true then parameter is present is 4 scope
{
if(d.equals("com.thinking.machines.webrock.ApplicationScope")) return true;
if(d.equals("com.thinking.machines.webrock.RequestScope")) return true;
if(d.equals("com.thinking.machines.webrock.SessionScope")) return true;
if(d.equals("com.thinking.machines.webrock.ApplicationDirectory")) return true;
return false;
}

}