package com.thinking.machines.webrock;
import javax.servlet.http.*;
public class RequestScope implements java.io.Serializable
{
private HttpServletRequest httpServletRequest;
public RequestScope()
{
this.httpServletRequest=null;
}
public void setAttribute(HttpServletRequest httpServletRequest)
{
this.httpServletRequest=httpServletRequest;
}
public HttpServletRequest getAttribute()
{
return this.httpServletRequest;
}
}