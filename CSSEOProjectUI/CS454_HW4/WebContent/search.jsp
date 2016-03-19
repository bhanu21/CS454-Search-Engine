<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<%@page import="HW4.*"%>
<%@ page import="org.json.*" %>
<%
String text = request.getParameter("text");

SearchEngine se = new SearchEngine();
String json = se.search(text.toLowerCase());

response.setContentType("json");

JSONArray array = new JSONArray(json);
array.write(response.getWriter());
%>