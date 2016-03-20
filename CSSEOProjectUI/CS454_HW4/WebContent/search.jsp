<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="HW4.*"%>
<%@ page import="org.json.*" %>
<%
String text = request.getParameter("text");
boolean isCSEnabled = Boolean.parseBoolean(request.getParameter("isCSEnabled"));
double pTfidf = Double.parseDouble(request.getParameter("pTfidf"));
double pTitle = Double.parseDouble(request.getParameter("pTitle"));
double pRanking = Double.parseDouble(request.getParameter("pRanking"));

SearchEngine se = new SearchEngine();
String json = se.search(text.toLowerCase(),isCSEnabled,new double[]{pTfidf,pRanking,pTitle});

response.setContentType("json");

JSONArray array = new JSONArray(json);
array.write(response.getWriter());
%>