package com.thinking.machines.webrock;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
public interface CustomHttpHeadersProvider
{
public HashMap<String,String> getCustomHttpHeaders(ServletContext sc,HttpSession ht,HttpServletRequest hsr);
}