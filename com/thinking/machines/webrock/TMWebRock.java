package com.thinking.machines.webrock;
import com.thinking.machines.webrock.pojo.*;
import com.thinking.machines.webrock.model.*;
import com.thinking.machines.webrock.annotations.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import com.google.gson.*;
public class TMWebRock extends HttpServlet
{
private static boolean forward=false;
private ServletContext servletContext;
private HttpSession httpSession;
private int j;
private boolean found=false;
private int q=0;

public void doGet(HttpServletRequest request,HttpServletResponse response)
{
System.out.println("doGet got called");
try
{
String requestURI=request.getRequestURI();
int  index=requestURI.indexOf("webServices",1);
String filteredRequestURI=requestURI.substring(index+11);// basically here we are doing this thing
// like /tmwebrock/webServices/students/add request we are cutting to /students/add
//webServices is required
//System.out.println(filteredRequestURI);
// this is for AutoWiring
String autoWire="";
try
{
index=filteredRequestURI.indexOf("/",1);
autoWire=filteredRequestURI.substring(0,index);
}catch(Exception exception)
{
response.sendError(HttpServletResponse.SC_NOT_FOUND,requestURI);
return;
}
//System.out.println("AutoWire String for Search : "+autoWire);
// here we are creating Application Scope and Session scope
servletContext=getServletContext();
httpSession=request.getSession();
WebRockModel webRockModel=(WebRockModel)servletContext.getAttribute("map");
if(webRockModel==null) 
{
//System.out.println("WebRockModel is empty");//means map is empty
response.sendError(HttpServletResponse.SC_NOT_FOUND);
return;
}
Service service=webRockModel.get(filteredRequestURI);
if(service==null) 
{
System.out.println("Service is not present");
response.sendError(HttpServletResponse.SC_NOT_FOUND);
return;
}
if(!service.getIsGetAllowed())
{
if(forward && service.getIsPostAllowed())
{
forward=false;
doPost(request,response);
return;
}
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}
if(service.getIsMethodValid()==false)
{
System.out.println("Method is Invalid");
response.sendError(HttpServletResponse.SC_BAD_REQUEST);
return;
}

Class c=service.getServiceClass();
Object obj=c.newInstance();

// Checking all Scope Annotation
if(service.getInjectApplicationDirectory())
{
File file=new File(service.getRealPath());
ApplicationDirectory a=new ApplicationDirectory(file);
Class parameter[]=new Class[1];
parameter[0]=ApplicationDirectory.class;// cool features
try
{
Method m=c.getDeclaredMethod("setAttribute",parameter);
m.invoke(obj,a);// here we are checking setAttribute method is exists or not
}catch(NoSuchMethodException  noMethod)
{
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Method setAttribute(ApplicationDirectory) not exists");
return;
}
}
if(service.getInjectSessionScope())
{
SessionScope s=new SessionScope();
s.setAttribute(httpSession);
Class parameter[]=new Class[1];
parameter[0]=SessionScope.class;// cool features
try
{
Method m=c.getDeclaredMethod("setAttribute",parameter);
m.invoke(obj,s);// here we are checking setAttribute method is exists or not
}catch(NoSuchMethodException  noMethod)
{
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Method setAttribute(SessionScope) not exists");
return;
}
}
if(service.getInjectRequestScope())
{
RequestScope r=new RequestScope();
r.setAttribute(request);
Class parameter[]=new Class[1];
parameter[0]=RequestScope.class;// cool features
try
{
Method m=c.getDeclaredMethod("setAttribute",parameter);
m.invoke(obj,r);// here we are checking setAttribute method is exists or not
}catch(NoSuchMethodException  noMethod)
{
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Method setAttribute(RequestScope) not exists");
return;
}
}
if(service.getInjectApplicationScope())
{
ApplicationScope a=new ApplicationScope();
a.setAttribute(servletContext);
Class parameter[]=new Class[1];
parameter[0]=ApplicationScope.class;// cool features
try
{
Method m=c.getDeclaredMethod("setAttribute",parameter);
m.invoke(obj,a);// here we are checking setAttribute method is exists or not
}catch(NoSuchMethodException  noMethod)
{
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Method setAttribute(ApplicationScope) not exists");
return;
}
}

// implementing AutoWired features 
HashMap<String,Field> autoWireMap=webRockModel.getFromAutoWired(autoWire);
if(autoWireMap!=null)
{
autoWireMap.forEach((k,v)->{
Field f=v;
Class fieldType=f.getType();
String fieldName=f.getName();
String methodName="set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1);
//System.out.println(k+"     METHOD NAME : "+methodName+"  Field Name : "+fieldName+"  TYPE : "+fieldType.getName());
Object object=servletContext.getAttribute(k);
if(object!=null)
{
//System.out.println("Servlet Context Autowired");

if(fieldType.isAssignableFrom(object.getClass())){ 
Class parameter[]=new Class[1];
parameter[0]=fieldType;
try
{
Method m=c.getDeclaredMethod(methodName,parameter);
m.invoke(obj,object);// here we are checking  method is exists or not according to fieldName
// for Example if field name is student then we search for setStudent(Student student) method
}catch(NoSuchMethodException  noMethod)
{

 try {
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Method for (AutoWired) is not exists"+methodName);
return;
            } catch (IOException e) {
// do nothing 
            }
}catch(Exception exception)
{
System.out.println(exception.getMessage());
 try {
response.sendError(HttpServletResponse.SC_FORBIDDEN,exception.getMessage());
return;
            } catch (IOException e) {
// do nothing 
            }
}
}else{

 try {
response.sendError(HttpServletResponse.SC_FORBIDDEN,"Invalid parameter  for (AutoWired)  : "+k);
return;
            } catch (IOException e) {
// do nothing 
            }
}

}else {
object=httpSession.getAttribute(k);
if(object!=null)
{
if(fieldType.isAssignableFrom(object.getClass())){ // is  a way to compare in Reflection API
Class parameter[]=new Class[1];
parameter[0]=fieldType;
try
{
Method m=c.getDeclaredMethod(methodName,parameter);
m.invoke(obj,object);// here we are checking  method is exists or not according to fieldName
// for Example if field name is student then we search for setStudent(Student student) method
}catch(NoSuchMethodException  noMethod)
{
 try {
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Method for (AutoWired) is not exists"+methodName);
return;
            } catch (IOException e) {
// do nothing 
            }
}catch(Exception exception)
{
System.out.println(exception.getMessage());
 try {
response.sendError(HttpServletResponse.SC_FORBIDDEN,exception.getMessage());
return;
            } catch (IOException e) {
// do nothing 
            }
}
}else{
 try {
response.sendError(HttpServletResponse.SC_FORBIDDEN,"Invalid parameter  for (AutoWired)  : "+k);
return;
            } catch (IOException e) {
// do nothing 
            }

}
}else
{
object=request.getAttribute(k);
if(object!=null)
{
if(fieldType.isAssignableFrom(object.getClass())){ // is  a way to compare in Reflection API
Class parameter[]=new Class[1];
parameter[0]=fieldType;
try
{
Method m=c.getDeclaredMethod(methodName,parameter);
m.invoke(obj,object);// here we are checking  method is exists or not according to fieldName
// for Example if field name is student then we search for setStudent(Student student) method
}catch(NoSuchMethodException  noMethod)
{
try {
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Method for (AutoWired) is not exists"+methodName);
return;
            } catch (IOException e) {
// do nothing 
            }


}catch(Exception exception)
{

 try {
response.sendError(HttpServletResponse.SC_FORBIDDEN,exception.getMessage());
return;
            } catch (IOException e) {
// do nothing 
            }

}
}else{

 try {
response.sendError(HttpServletResponse.SC_FORBIDDEN,"Invalid parameter  for (AutoWired)  : "+k);
return;
            } catch (IOException e) {
// do nothing 
            }
}

}
}
}
//System.out.printf("Key : %s value : %s\n",k,f.getName());


});
}


if(service.getPath().equals(filteredRequestURI))// comparing value of path and requestURI 
{
// here we are implementing SecurityGuard starts here // Pending some Exception work
if(service.isSecured())
{
Class para[]=new Class[4];
para[0]=String.class;
para[1]=ServletContext.class;
para[2]=HttpSession.class;
para[3]=HttpServletRequest.class;
Object args[]=new Object[4];
args[0]=service.getPath();
args[1]=servletContext;
args[2]=httpSession;
args[3]=request;
Class clz=service.getSecuredGuardClass();
Object securedObject=clz.newInstance();
Method sm=clz.getDeclaredMethod("allow",para);
Boolean allow=(Boolean)sm.invoke(securedObject,args);
if(!allow)// allow==false means some Problem then we check the value of getActionType forward or divertTo
{
Method getActionType=clz.getDeclaredMethod("getActionType");
String returnActionType=(String)getActionType.invoke(securedObject);
if(returnActionType.equalsIgnoreCase("forward")) 
{
Method forwardMethod=clz.getDeclaredMethod("divertTo",para);
TMForward tmf=(TMForward)forwardMethod.invoke(securedObject,args);
String forwardTo=tmf.getForwardString();
service=webRockModel.get(forwardTo);// here we are checking is Get Type Request because
if(!(forwardTo.endsWith(".jsp") || forwardTo.endsWith(".html")))forwardTo="/webServices"+forwardTo;
if(service!=null) if(service.getIsPostAllowed()) forward=true;
RequestDispatcher requestDispatcher=request.getRequestDispatcher(forwardTo);
requestDispatcher.forward(request,response);
return;
}else if(returnActionType.equalsIgnoreCase("redirect"))
{
Method redirectMethod=clz.getDeclaredMethod("redirectTo",para);
TMRedirect tmr=(TMRedirect)redirectMethod.invoke(securedObject,args);
String redirectTo=tmr.getRedirectString();
service=webRockModel.get(redirectTo);
if(!(redirectTo.endsWith(".jsp") || redirectTo.endsWith(".html")))redirectTo="/webServices"+redirectTo;
if(service!=null) if(service.getIsPostAllowed()) forward=true;
response.sendRedirect(request.getContextPath()+redirectTo);
return;
} else
{
response.sendError(HttpServletResponse.SC_BAD_REQUEST,"In SecurityGuard getActionType method you should return (redirect or forward)");
return;
}
}
}
// here we are implementing SecurityGuard ends here 

Map<String,Class> maps=service.getRequestParametersMap();
Class parameters[]=new Class[maps.size()];
j=0;
Object arguments[]=new Object[maps.size()];
String pathName=service.getRealPath();
if(service.isJSON())
{
maps.forEach((k,v)->{
Class json=v;// v is a reference of Class Type object
if(j==0){
try
{
BufferedReader br=request.getReader();
StringBuffer b=new StringBuffer();
String d;
while(true)
{
d=br.readLine();
if(d==null)break;
b.append(d);
}
String rowData=b.toString();
System.out.println(rowData);
Gson gson=new Gson();
Object classObject=gson.fromJson(rowData,json);
arguments[j]=classObject;
}catch(Exception exception)
{
System.out.println(exception.getMessage());
try
{
response.sendError(HttpServletResponse.SC_BAD_REQUEST,exception.getMessage());
return;
}catch(Exception io)
{
//do nothing
}
}
}else{ // if j==0 first is JSON TYPE
String className=v.getName();// v is a reference of Class Type object
if(className.equals("com.thinking.machines.webrock.ApplicationScope")){
ApplicationScope a=new ApplicationScope();
a.setAttribute(servletContext);
arguments[j]=a;
} else if(className.equals("com.thinking.machines.webrock.RequestScope")){
RequestScope r=new RequestScope();
r.setAttribute(request);
arguments[j]=r;
} else if(className.equals("com.thinking.machines.webrock.SessionScope")){
SessionScope s=new SessionScope();
s.setAttribute(httpSession);
arguments[j]=s;
} else if(className.equals("com.thinking.machines.webrock.ApplicationDirectory")){
File file=new File(pathName);
ApplicationDirectory a=new ApplicationDirectory(file);
arguments[j]=a;
}else
{
 try {
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Only JsonObject && ApplicationScope,SessionScope,RequestScope,ApplicationDirectory is allowed");
return;

            } catch (IOException e) {
// do nothing 
            }


return;
}

}
j++;
});
}else// it is not json Type method 
{
found=false;
q=0;
maps.forEach((k,v)->{// here we add neccessary things for SessionScope and RequestScope and more
if(k.equals("P"+q)==true && found==false) found=true;
q++;
String className=v.getName();// v is a reference of Class Type object
if(found)
{
if(className.equals("com.thinking.machines.webrock.ApplicationScope")){
ApplicationScope a=new ApplicationScope();
a.setAttribute(servletContext);
arguments[j]=a;
}else if(className.equals("com.thinking.machines.webrock.RequestScope")){
RequestScope r=new RequestScope();
r.setAttribute(request);
arguments[j]=r;
}else if(className.equals("com.thinking.machines.webrock.SessionScope")){
SessionScope s=new SessionScope();
s.setAttribute(httpSession);
arguments[j]=s;
}else if(className.equals("com.thinking.machines.webrock.ApplicationDirectory")){
File file=new File(pathName);
ApplicationDirectory a=new ApplicationDirectory(file);
arguments[j]=a;
} else{
 try {
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Only @RequestParameter() && ApplicationScope,SessionScope,RequestScope,ApplicationDirectory is allowed");
return;

            } catch (IOException e) {
// do nothing 
            }

return;
}
} else{// found else part starts here
String reqs=request.getParameter(k);
if(reqs!=null)
{
if(className.equals("java.lang.Long") || className.equals("long"))
{
try
{
arguments[j]=Long.parseLong(reqs);
}catch(NumberFormatException nfe)
{
arguments[j]=0;
}
} else if(className.equals("java.lang.Integer") || className.equals("int"))
{
try
{
arguments[j]=Integer.parseInt(reqs);
}catch(NumberFormatException nfe)
{
arguments[j]=0;
}
} else if(className.equals("java.lang.Short") || className.equals("short")){
try
{
arguments[j]=Short.parseShort(reqs);
}catch(NumberFormatException nfe)
{
arguments[j]=0;
}
} else if(className.equals("java.lang.Byte") || className.equals("byte")){ 
try
{
arguments[j]=Byte.parseByte(reqs);
}catch(NumberFormatException nfe)
{
arguments[j]=0;
}
} else if(className.equals("java.lang.Double") || className.equals("double")){ 
try
{
arguments[j]=Double.parseDouble(reqs);
}catch(NumberFormatException nfe)
{
arguments[j]=0.0;
}
} else if(className.equals("java.lang.Float") || className.equals("float")){ 
try
{
arguments[j]=Float.parseFloat(reqs);
}catch(NumberFormatException nfe)
{
arguments[j]=0.0f;
}
} else if(className.equals("java.lang.Boolean") || className.equals("boolean")){
arguments[j]=Boolean.parseBoolean(reqs);
} else if(className.equals("java.lang.Character") || className.equals("char")){
arguments[j]=reqs.charAt(0);
} else if(className.equals("java.lang.String") ){ 
arguments[j]=reqs;
}

}else// else part in request.getParameter not found then we set default value
{
if(className.equals("java.lang.Long") || className.equals("long"))
{
arguments[j]=0;
} else if(className.equals("java.lang.Integer") || className.equals("int")){ 
arguments[j]=0;
} else if(className.equals("java.lang.Short") || className.equals("short")){
 arguments[j]=0;
} else if(className.equals("java.lang.Byte") || className.equals("byte")){ arguments[j]=0;}
else if(className.equals("java.lang.Double") || className.equals("double")){ arguments[j]=0.0;}
else if(className.equals("java.lang.Float") || className.equals("float")){ arguments[j]=0.0f;}
else if(className.equals("java.lang.Boolean") || className.equals("boolean")){
 arguments[j]=false;
} else if(className.equals("java.lang.Character") || className.equals("char")){
arguments[j]=' ';}
else if(className.equals("java.lang.String") ){
arguments[j]="\"\"";}

}
}// found==false if ends here
j++;
});// forEach ends here for requestParameter annotation
}// else part ends here if it is not JSON 

Method method=service.getService();

if(service.isReturning()==false)
{
method.invoke(obj,arguments); // if return type void then just call
if(service.isResponseHeader())// here we are implementing ResponseHeader new new new new
{// we to add on PostMethod to 
Class para[]=new Class[3];
para[0]=ServletContext.class;// cool features
para[1]=HttpSession.class;
para[2]=HttpServletRequest.class;
Object args[]=new Object[3];
args[0]=servletContext;
args[1]=httpSession;
args[2]=request;
Class myClass =service.getResponseHeaderClass();// here we have to implement exception
Object responseObject=myClass.newInstance();
Method mr=myClass.getDeclaredMethod("getCustomHttpHeaders",para);
HashMap<String,String> hashMap=(HashMap)mr.invoke(responseObject,args);
hashMap.forEach((k,v)->{
response.setHeader(k,v);
});
}
}else// if return type is Object type then make it Return object and use Gson and send to client side
{
ReturnPojo ret;
ret=new ReturnPojo();
try
{
Object result=method.invoke(obj,arguments);
ret.setSuccess(true);
ret.setIsException(false);
ret.setIsReturnSomething(true);
ret.setResult(result);
ret.setException(null);
if(service.isResponseHeader())// here we are implementing ResponseHeader
{
Class para[]=new Class[3];
para[0]=ServletContext.class;// cool features
para[1]=HttpSession.class;
para[2]=HttpServletRequest.class;
Object args[]=new Object[3];
args[0]=servletContext;
args[1]=httpSession;
args[2]=request;
Class myClass =service.getResponseHeaderClass();// here we have to implement exception
Object responseObject=myClass.newInstance();
Method mr=myClass.getDeclaredMethod("getCustomHttpHeaders",para);
HashMap<String,String> hashMap=(HashMap)mr.invoke(responseObject,args);
hashMap.forEach((k,v)->{
response.setHeader(k,v);
});
}
}catch(InvocationTargetException ite)
{
Throwable cause=ite.getCause();
ret.setSuccess(false);
ret.setIsException(true);
ret.setIsReturnSomething(false);
if(cause!=null) ret.setException(cause.toString());
else ret.setException("Server Error");
}catch(Exception exc)
{
response.sendError(HttpServletResponse.SC_BAD_REQUEST,exc.getMessage());
return;
}
response.setContentType("application/json");
response.setCharacterEncoding("utf-8");
PrintWriter pw=response.getWriter();
Gson gson=new Gson();
String jsonString=gson.toJson(ret);
pw.print(jsonString);
pw.flush();
}
}
String forwardTo=service.getForwardTo();
String f=forwardTo;
if(forwardTo.length()!=0)
{
if(!(forwardTo.endsWith(".jsp") || forwardTo.endsWith(".html"))) forwardTo="/webServices"+forwardTo;
service=webRockModel.get(f);// here we are checking is Get Type Request because
// when we forward using RequestDispatcher it will redirect for only Post type request thats why we are 
// using this technic and top we are checking the value of forward if it is true then we will redirec to doGet method
if(service!=null) if(service.getIsPostAllowed()) forward=true;
RequestDispatcher requestDispatcher=request.getRequestDispatcher(forwardTo);
requestDispatcher.forward(request,response);
}

System.out.println("doGet Ends here");
}catch(Exception e)
{
System.out.println(e);
try {
response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage());
return;

            } catch (IOException t) {
// do nothing done
            }

}
}


public void doPost(HttpServletRequest request,HttpServletResponse response)
{
System.out.println("doPost got called");
try
{
String requestURI=request.getRequestURI();
int  index=requestURI.indexOf("webServices",1);
String filteredRequestURI=requestURI.substring(index+11);// basically here we are doing this thing
// like /tmwebrock/webServices/students/add request we are cutting to /students/add
//webServices is required
//System.out.println(filteredRequestURI);
// this is for AutoWiring
String autoWire="";
try
{
index=filteredRequestURI.indexOf("/",1);
autoWire=filteredRequestURI.substring(0,index);
}catch(Exception exception)
{
response.sendError(HttpServletResponse.SC_NOT_FOUND,requestURI);
return;
}
//System.out.println("AutoWire String for Search : "+autoWire);
// here we are creating Application Scope and Session Scope
servletContext=getServletContext();
httpSession=request.getSession();
WebRockModel webRockModel=(WebRockModel)servletContext.getAttribute("map");
if(webRockModel==null) 
{
//System.out.println("WebRockModel is empty");//means map is empty
response.sendError(HttpServletResponse.SC_NOT_FOUND);
return;
}
Service service=webRockModel.get(filteredRequestURI);
if(service==null) 
{
System.out.println("Service is not present");
response.sendError(HttpServletResponse.SC_NOT_FOUND);
return;
}
if(!service.getIsPostAllowed())
{
if(forward && service.getIsGetAllowed())
{
forward=false;
doGet(request,response);
return;
}
response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
return;
}
if(service.getIsMethodValid()==false)
{
System.out.println("Method is Invalid");
response.sendError(HttpServletResponse.SC_BAD_REQUEST);
return;
}

Class c=service.getServiceClass();
Object obj=c.newInstance();

// Checking all Scope Annotation
if(service.getInjectApplicationDirectory())
{
File file=new File(service.getRealPath());
ApplicationDirectory a=new ApplicationDirectory(file);
Class parameter[]=new Class[1];
parameter[0]=ApplicationDirectory.class;// cool features
try
{
Method m=c.getDeclaredMethod("setAttribute",parameter);
m.invoke(obj,a);// here we are checking setAttribute method is exists or not
}catch(NoSuchMethodException  noMethod)
{
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Method setAttribute(ApplicationDirectory) not exists");
return;
}
}
if(service.getInjectSessionScope())
{
SessionScope s=new SessionScope();
s.setAttribute(httpSession);
Class parameter[]=new Class[1];
parameter[0]=SessionScope.class;// cool features
try
{
Method m=c.getDeclaredMethod("setAttribute",parameter);
m.invoke(obj,s);// here we are checking setAttribute method is exists or not
}catch(NoSuchMethodException  noMethod)
{
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Method setAttribute(SessionScope) not exists");
return;
}
}
if(service.getInjectRequestScope())
{
RequestScope r=new RequestScope();
r.setAttribute(request);
Class parameter[]=new Class[1];
parameter[0]=RequestScope.class;// cool features
try
{
Method m=c.getDeclaredMethod("setAttribute",parameter);
m.invoke(obj,r);// here we are checking setAttribute method is exists or not
}catch(NoSuchMethodException  noMethod)
{
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Method setAttribute(RequestScope) not exists");
return;
}
}
if(service.getInjectApplicationScope())
{
ApplicationScope a=new ApplicationScope();
a.setAttribute(servletContext);
Class parameter[]=new Class[1];
parameter[0]=ApplicationScope.class;// cool features
try
{
Method m=c.getDeclaredMethod("setAttribute",parameter);
m.invoke(obj,a);// here we are checking setAttribute method is exists or not
}catch(NoSuchMethodException  noMethod)
{
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Method setAttribute(ApplicationScope) not exists");
return;
}
}

// implementing AutoWired features 
HashMap<String,Field> autoWireMap=webRockModel.getFromAutoWired(autoWire);
if(autoWireMap!=null)
{
autoWireMap.forEach((k,v)->{
Field f=v;
Class fieldType=f.getType();
String fieldName=f.getName();
String methodName="set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1);
//System.out.println(k+"     METHOD NAME : "+methodName+"  Field Name : "+fieldName+"  TYPE : "+fieldType.getName());
Object object=servletContext.getAttribute(k);
if(object!=null)
{
//System.out.println("Servlet Context Autowired");

if(fieldType.isAssignableFrom(object.getClass())){ 
Class parameter[]=new Class[1];
parameter[0]=fieldType;
try
{
Method m=c.getDeclaredMethod(methodName,parameter);
m.invoke(obj,object);// here we are checking  method is exists or not according to fieldName
// for Example if field name is student then we search for setStudent(Student student) method
}catch(NoSuchMethodException  noMethod)
{

 try {
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Method for (AutoWired) is not exists"+methodName);
return;
            } catch (IOException e) {
// do nothing 
            }
}catch(Exception exception)
{
System.out.println(exception.getMessage());
 try {
response.sendError(HttpServletResponse.SC_FORBIDDEN,exception.getMessage());
return;
            } catch (IOException e) {
// do nothing 
            }
}
}else{

 try {
response.sendError(HttpServletResponse.SC_FORBIDDEN,"Invalid parameter  for (AutoWired)  : "+k);
return;
            } catch (IOException e) {
// do nothing 
            }
}

}else {
object=httpSession.getAttribute(k);
if(object!=null)
{
if(fieldType.isAssignableFrom(object.getClass())){ // is  a way to compare in Reflection API
Class parameter[]=new Class[1];
parameter[0]=fieldType;
try
{
Method m=c.getDeclaredMethod(methodName,parameter);
m.invoke(obj,object);// here we are checking  method is exists or not according to fieldName
// for Example if field name is student then we search for setStudent(Student student) method
}catch(NoSuchMethodException  noMethod)
{
 try {
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Method for (AutoWired) is not exists"+methodName);
return;
            } catch (IOException e) {
// do nothing 
            }
}catch(Exception exception)
{
System.out.println(exception.getMessage());
 try {
response.sendError(HttpServletResponse.SC_FORBIDDEN,exception.getMessage());
return;
            } catch (IOException e) {
// do nothing 
            }
}
}else{
 try {
response.sendError(HttpServletResponse.SC_FORBIDDEN,"Invalid parameter  for (AutoWired)  : "+k);
return;
            } catch (IOException e) {
// do nothing 
            }

}
}else
{
object=request.getAttribute(k);
if(object!=null)
{
if(fieldType.isAssignableFrom(object.getClass())){ // is  a way to compare in Reflection API
Class parameter[]=new Class[1];
parameter[0]=fieldType;
try
{
Method m=c.getDeclaredMethod(methodName,parameter);
m.invoke(obj,object);// here we are checking  method is exists or not according to fieldName
// for Example if field name is student then we search for setStudent(Student student) method
}catch(NoSuchMethodException  noMethod)
{
try {
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Method for (AutoWired) is not exists"+methodName);
return;
            } catch (IOException e) {
// do nothing 
            }


}catch(Exception exception)
{

 try {
response.sendError(HttpServletResponse.SC_FORBIDDEN,exception.getMessage());
return;
            } catch (IOException e) {
// do nothing 
            }

}
}else{

 try {
response.sendError(HttpServletResponse.SC_FORBIDDEN,"Invalid parameter  for (AutoWired)  : "+k);
return;
            } catch (IOException e) {
// do nothing 
            }
}

}
}
}
//System.out.printf("Key : %s value : %s\n",k,f.getName());


});
}


if(service.getPath().equals(filteredRequestURI))// comparing value of path and requestURI 
{
// here we are implementing SecurityGuard starts here // Pending some Exception work
if(service.isSecured())
{
Class para[]=new Class[4];
para[0]=String.class;
para[1]=ServletContext.class;
para[2]=HttpSession.class;
para[3]=HttpServletRequest.class;
Object args[]=new Object[4];
args[0]=service.getPath();
args[1]=servletContext;
args[2]=httpSession;
args[3]=request;
Class clz=service.getSecuredGuardClass();
Object securedObject=clz.newInstance();
Method sm=clz.getDeclaredMethod("allow",para);
Boolean allow=(Boolean)sm.invoke(securedObject,args);
if(!allow)// allow==false means some Problem then we check the value of getActionType forward or divertTo
{
Method getActionType=clz.getDeclaredMethod("getActionType");
String returnActionType=(String)getActionType.invoke(securedObject);
if(returnActionType.equalsIgnoreCase("forward")) 
{
Method forwardMethod=clz.getDeclaredMethod("divertTo",para);
TMForward tmf=(TMForward)forwardMethod.invoke(securedObject,args);
String forwardTo=tmf.getForwardString();
service=webRockModel.get(forwardTo);// here we are checking is Get Type Request because
if(!(forwardTo.endsWith(".jsp") || forwardTo.endsWith(".html")))forwardTo="/webServices"+forwardTo;
if(service!=null) if(service.getIsPostAllowed()) forward=true;
RequestDispatcher requestDispatcher=request.getRequestDispatcher(forwardTo);
requestDispatcher.forward(request,response);
return;
}else if(returnActionType.equalsIgnoreCase("redirect"))
{
Method redirectMethod=clz.getDeclaredMethod("redirectTo",para);
TMRedirect tmr=(TMRedirect)redirectMethod.invoke(securedObject,args);
String redirectTo=tmr.getRedirectString();
service=webRockModel.get(redirectTo);
if(!(redirectTo.endsWith(".jsp") || redirectTo.endsWith(".html")))redirectTo="/webServices"+redirectTo;
if(service!=null) if(service.getIsPostAllowed()) forward=true;
response.sendRedirect(request.getContextPath()+redirectTo);
return;
} else
{
response.sendError(HttpServletResponse.SC_BAD_REQUEST,"In SecurityGuard getActionType method you should return (redirect or forward)");
return;
}
}
}
// here we are implementing SecurityGuard ends here 

Map<String,Class> maps=service.getRequestParametersMap();
Class parameters[]=new Class[maps.size()];
j=0;
Object arguments[]=new Object[maps.size()];
String pathName=service.getRealPath();
if(service.isJSON())
{
maps.forEach((k,v)->{
Class json=v;// v is a reference of Class Type object
if(j==0){
try
{
BufferedReader br=request.getReader();
StringBuffer b=new StringBuffer();
String d;
while(true)
{
d=br.readLine();
if(d==null)break;
b.append(d);
}
String rowData=b.toString();
System.out.println(rowData);
Gson gson=new Gson();
Object classObject=gson.fromJson(rowData,json);
arguments[j]=classObject;
}catch(Exception exception)
{
System.out.println(exception.getMessage());
try
{
response.sendError(HttpServletResponse.SC_BAD_REQUEST,exception.getMessage());
return;
}catch(Exception io)
{
//do nothing
}
}
}else{ // if j==0 first is JSON TYPE
String className=v.getName();// v is a reference of Class Type object
if(className.equals("com.thinking.machines.webrock.ApplicationScope")){
ApplicationScope a=new ApplicationScope();
a.setAttribute(servletContext);
arguments[j]=a;
} else if(className.equals("com.thinking.machines.webrock.RequestScope")){
RequestScope r=new RequestScope();
r.setAttribute(request);
arguments[j]=r;
} else if(className.equals("com.thinking.machines.webrock.SessionScope")){
SessionScope s=new SessionScope();
s.setAttribute(httpSession);
arguments[j]=s;
} else if(className.equals("com.thinking.machines.webrock.ApplicationDirectory")){
File file=new File(pathName);
ApplicationDirectory a=new ApplicationDirectory(file);
arguments[j]=a;
}else
{
 try {
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Only JsonObject && ApplicationScope,SessionScope,RequestScope,ApplicationDirectory is allowed");
return;

            } catch (IOException e) {
// do nothing 
            }


return;
}

}
j++;
});
}else// it is not json Type method 
{
found=false;
q=0;
maps.forEach((k,v)->{// here we add neccessary things for SessionScope and RequestScope and more
if(k.equals("P"+q)==true && found==false) found=true;
q++;
String className=v.getName();// v is a reference of Class Type object
if(found)
{
if(className.equals("com.thinking.machines.webrock.ApplicationScope")){
ApplicationScope a=new ApplicationScope();
a.setAttribute(servletContext);
arguments[j]=a;
}else if(className.equals("com.thinking.machines.webrock.RequestScope")){
RequestScope r=new RequestScope();
r.setAttribute(request);
arguments[j]=r;
}else if(className.equals("com.thinking.machines.webrock.SessionScope")){
SessionScope s=new SessionScope();
s.setAttribute(httpSession);
arguments[j]=s;
}else if(className.equals("com.thinking.machines.webrock.ApplicationDirectory")){
File file=new File(pathName);
ApplicationDirectory a=new ApplicationDirectory(file);
arguments[j]=a;
} else{
 try {
response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,"Only @RequestParameter() && ApplicationScope,SessionScope,RequestScope,ApplicationDirectory is allowed");
return;

            } catch (IOException e) {
// do nothing 
            }

return;
}
} else{// found else part starts here
String reqs=request.getParameter(k);
if(reqs!=null)
{
if(className.equals("java.lang.Long") || className.equals("long"))
{
try
{
arguments[j]=Long.parseLong(reqs);
}catch(NumberFormatException nfe)
{
arguments[j]=0;
}
} else if(className.equals("java.lang.Integer") || className.equals("int"))
{
try
{
arguments[j]=Integer.parseInt(reqs);
}catch(NumberFormatException nfe)
{
arguments[j]=0;
}
} else if(className.equals("java.lang.Short") || className.equals("short")){
try
{
arguments[j]=Short.parseShort(reqs);
}catch(NumberFormatException nfe)
{
arguments[j]=0;
}
} else if(className.equals("java.lang.Byte") || className.equals("byte")){ 
try
{
arguments[j]=Byte.parseByte(reqs);
}catch(NumberFormatException nfe)
{
arguments[j]=0;
}
} else if(className.equals("java.lang.Double") || className.equals("double")){ 
try
{
arguments[j]=Double.parseDouble(reqs);
}catch(NumberFormatException nfe)
{
arguments[j]=0.0;
}
} else if(className.equals("java.lang.Float") || className.equals("float")){ 
try
{
arguments[j]=Float.parseFloat(reqs);
}catch(NumberFormatException nfe)
{
arguments[j]=0.0f;
}
} else if(className.equals("java.lang.Boolean") || className.equals("boolean")){
arguments[j]=Boolean.parseBoolean(reqs);
} else if(className.equals("java.lang.Character") || className.equals("char")){
arguments[j]=reqs.charAt(0);
} else if(className.equals("java.lang.String") ){ 
arguments[j]=reqs;
}

}else// else part in request.getParameter not found then we set default value
{
if(className.equals("java.lang.Long") || className.equals("long"))
{
arguments[j]=0;
} else if(className.equals("java.lang.Integer") || className.equals("int")){ 
arguments[j]=0;
} else if(className.equals("java.lang.Short") || className.equals("short")){
 arguments[j]=0;
} else if(className.equals("java.lang.Byte") || className.equals("byte")){ arguments[j]=0;}
else if(className.equals("java.lang.Double") || className.equals("double")){ arguments[j]=0.0;}
else if(className.equals("java.lang.Float") || className.equals("float")){ arguments[j]=0.0f;}
else if(className.equals("java.lang.Boolean") || className.equals("boolean")){
 arguments[j]=false;
} else if(className.equals("java.lang.Character") || className.equals("char")){
arguments[j]=' ';}
else if(className.equals("java.lang.String") ){
arguments[j]="\"\"";}

}
}// found==false if ends here
j++;
});// forEach ends here for requestParameter annotation
}// else part ends here if it is not JSON 

Method method=service.getService();

if(service.isReturning()==false)
{
method.invoke(obj,arguments); // if return type void then just call
if(service.isResponseHeader())// here we are implementing ResponseHeader new new new new
{// we to add on PostMethod to 
Class para[]=new Class[3];
para[0]=ServletContext.class;// cool features
para[1]=HttpSession.class;
para[2]=HttpServletRequest.class;
Object args[]=new Object[3];
args[0]=servletContext;
args[1]=httpSession;
args[2]=request;
Class myClass =service.getResponseHeaderClass();// here we have to implement exception
Object responseObject=myClass.newInstance();
Method mr=myClass.getDeclaredMethod("getCustomHttpHeaders",para);
HashMap<String,String> hashMap=(HashMap)mr.invoke(responseObject,args);
hashMap.forEach((k,v)->{
response.setHeader(k,v);
});
}
}else// if return type is Object type then make it Return object and use Gson and send to client side
{
ReturnPojo ret;
ret=new ReturnPojo();
try
{
Object result=method.invoke(obj,arguments);
ret.setSuccess(true);
ret.setIsException(false);
ret.setIsReturnSomething(true);
ret.setResult(result);
ret.setException(null);
if(service.isResponseHeader())// here we are implementing ResponseHeader
{
Class para[]=new Class[3];
para[0]=ServletContext.class;// cool features
para[1]=HttpSession.class;
para[2]=HttpServletRequest.class;
Object args[]=new Object[3];
args[0]=servletContext;
args[1]=httpSession;
args[2]=request;
Class myClass =service.getResponseHeaderClass();// here we have to implement exception
Object responseObject=myClass.newInstance();
Method mr=myClass.getDeclaredMethod("getCustomHttpHeaders",para);
HashMap<String,String> hashMap=(HashMap)mr.invoke(responseObject,args);
hashMap.forEach((k,v)->{
response.setHeader(k,v);
});
}
}catch(InvocationTargetException ite)
{
Throwable cause=ite.getCause();
ret.setSuccess(false);
ret.setIsException(true);
ret.setIsReturnSomething(false);
if(cause!=null) ret.setException(cause.toString());
else ret.setException("Server Error");
}catch(Exception exc)
{
response.sendError(HttpServletResponse.SC_BAD_REQUEST,exc.getMessage());
return;
}
response.setContentType("application/json");
response.setCharacterEncoding("utf-8");
PrintWriter pw=response.getWriter();
Gson gson=new Gson();
String jsonString=gson.toJson(ret);
pw.print(jsonString);
pw.flush();
}
}
String forwardTo=service.getForwardTo();
String f=forwardTo;
if(forwardTo.length()!=0)
{
if(!(forwardTo.endsWith(".jsp") || forwardTo.endsWith(".html"))) forwardTo="/webServices"+forwardTo;
service=webRockModel.get(f);// here we are checking is Get Type Request because
// when we forward using RequestDispatcher it will redirect for only Post type request thats why we are 
// using this technic and top we are checking the value of forward if it is true then we will redirec to doGet method
if(service!=null) if(service.getIsGetAllowed()) forward=true;
RequestDispatcher requestDispatcher=request.getRequestDispatcher(forwardTo);
requestDispatcher.forward(request,response);
}

System.out.println("doPost Ends here");
}catch(Exception e)
{
System.out.println(e);
try {
response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage());
return;

            } catch (IOException t) {
// do nothing done
            }

}
}




 
}