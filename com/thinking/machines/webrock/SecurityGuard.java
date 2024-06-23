package com.thinking.machines.webrock;
import javax.servlet.http.*;
import javax.servlet.*;
public interface SecurityGuard
{// which class this securityGuard called
public boolean allow(String str,ServletContext servletContext,HttpSession httpSession,HttpServletRequest httpServletRequest);
public TMForward divertTo(String str,ServletContext servletContext,HttpSession httpSession,HttpServletRequest httpServletRequest);
public String getActionType();// Return forward or divertTo 
public TMRedirect redirectTo(String str,ServletContext servletContext,HttpSession httpSession,HttpServletRequest httpServletRequest);
}