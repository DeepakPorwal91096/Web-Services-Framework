package com.thinking.machines.webrock.pojo;
import java.lang.reflect.*;
import java.util.*;
public class Service implements java.io.Serializable
{
private Class serviceClass;
private String path;
private Method service;
private boolean isGetAllowed;
private boolean isPostAllowed;
private String forwardTo;
private boolean injectSessionScope;
private boolean injectApplicationScope;
private boolean injectRequestScope;
private boolean injectApplicationDirectory;
private HashMap<String,Object> autoWires;
private LinkedHashMap<String,Class> requestParameters;
private String realPath;
private boolean isMethodValid;
private boolean isJSON;
private boolean isReturning;
private boolean isResponseHeader;
private boolean isSecured;
private Class responseHeaderClass;
private Class securedGuardClass;
public Service()
{
this.responseHeaderClass=null;
this.securedGuardClass=null;
this.isResponseHeader=false;
this.isSecured=false;
this.isReturning=false;
this.isJSON=false;
this.isMethodValid=true;
this.realPath=null;
this.serviceClass=null;
this.path="";
this.service=null;
this.isGetAllowed=false;
this.isPostAllowed=false;
this.forwardTo="";
this.injectSessionScope=false;
this.injectApplicationScope=false;
this.injectRequestScope=false;
this.injectApplicationDirectory=false;
autoWires=new HashMap<>();
requestParameters=new LinkedHashMap<>();
}

public void setResponseHeaderClass(Class clz)
{
this.responseHeaderClass=clz;
}
public Class getResponseHeaderClass()
{
return this.responseHeaderClass;
}
public void setSecuredGuardClass(Class securedGuardClass)
{
this.securedGuardClass=securedGuardClass;
}
public Class getSecuredGuardClass()
{
return this.securedGuardClass;
}

public void isResponseHeader(boolean t)
{
this.isResponseHeader=t;
}
public boolean isResponseHeader()
{
return this.isResponseHeader;
}
public void  isSecured(boolean t)
{
this.isSecured=t;
}
public boolean isSecured()
{
return this.isSecured;
}

public void isReturning(boolean t)
{
this.isReturning=t;
}
public boolean isReturning()
{
return this.isReturning;
}
public void isJSON(boolean t)
{
this.isJSON=t;
}
public boolean isJSON()
{
return this.isJSON;
}
public void setIsMethodValid(boolean t)
{
this.isMethodValid=t;
}
public boolean getIsMethodValid()
{
return this.isMethodValid;
}
public void setRequestParameters(String key,Class value)
{
this.requestParameters.put(key,value);
}
public Class getRequestParameters(String key)
{
return this.requestParameters.get(key);
}
public void clearRequestParameters()
{
this.requestParameters.clear();
}
public LinkedHashMap<String,Class> getRequestParametersMap()
{
return this.requestParameters;
}
public void setServiceClass(java.lang.Class serviceClass)
{
this.serviceClass=serviceClass;
}
public java.lang.Class getServiceClass()
{
return this.serviceClass;
}
public void setPath(java.lang.String path)
{
this.path=path;
}
public java.lang.String getPath()
{
return this.path;
}
public void setService(java.lang.reflect.Method service)
{
this.service=service;
}
public java.lang.reflect.Method getService()
{
return this.service;
}
public void setIsGetAllowed(boolean isGetAllowed)
{
this.isGetAllowed=isGetAllowed;
}
public boolean getIsGetAllowed()
{
return this.isGetAllowed;
}
public void setIsPostAllowed(boolean isPostAllowed)
{
this.isPostAllowed=isPostAllowed;
}
public boolean getIsPostAllowed()
{
return this.isPostAllowed;
}
public void setForwardTo(String forwardTo)
{
this.forwardTo=forwardTo;
}
public String getForwardTo()
{
return this.forwardTo;
}

public void setInjectSessionScope(boolean injectSessionScope)
{
this.injectSessionScope=injectSessionScope;
}
public boolean getInjectSessionScope()
{
return this.injectSessionScope;
}
public void setInjectApplicationScope(boolean injectApplicationScope)
{
this.injectApplicationScope=injectApplicationScope;
}
public boolean getInjectApplicationScope(){
return this.injectApplicationScope;
}
public void setInjectRequestScope(boolean injectRequestScope)
{
this.injectRequestScope=injectRequestScope;
}
public boolean getInjectRequestScope()
{
return this.injectRequestScope;
}
public void setInjectApplicationDirectory(boolean injectApplicationDirectory)
{
this.injectApplicationDirectory=injectApplicationDirectory;
}
public boolean getInjectApplicationDirectory()
{
return this.injectApplicationDirectory;
}
public void setAutoWires(String key,Object value)
{
this.autoWires.put(key,value);
}
public Object getAutoWires(String key)
{
return this.autoWires.get(key);
}
public void setRealPath(String realPath)
{
this.realPath=realPath;
}
public String getRealPath()
{
return this.realPath;
}
}