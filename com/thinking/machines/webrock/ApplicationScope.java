package com.thinking.machines.webrock;
import javax.servlet.*;
public class ApplicationScope implements java.io.Serializable
{
private ServletContext servletContext;
public ApplicationScope()
{
this.servletContext=null;
}
public void setAttribute(ServletContext servletContext)
{
this.servletContext=servletContext;
}
public ServletContext getAttribute()
{
return this.servletContext;
}
}