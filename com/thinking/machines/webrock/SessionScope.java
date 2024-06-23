package com.thinking.machines.webrock;
import javax.servlet.http.*;
public class SessionScope implements java.io.Serializable
{
private HttpSession httpSession;
public SessionScope()
{
this.httpSession=null;
}
public void setAttribute(HttpSession httpSession)
{
this.httpSession=httpSession;
}
public HttpSession getAttribute()
{
return this.httpSession;
}
}