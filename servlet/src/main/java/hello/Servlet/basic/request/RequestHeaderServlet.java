package main.java.Servlet.basic.request;

import javax.serlvet.annotation.WebServlet;
import javax.serlvet.http.HttpServlet;

//localhost:8080/request-header
@WebServlet(name = "requestHeaderServlet", urlPatterns = "/request-header")
public class RequestHeaderServlet extends HttpServlet {

   @Override
   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException throws ServletException, IOException {
         
      printStartLine(request);
      printHeaders(request);
      printHeaderUtils(request);
      printEtc(request);
      response.getWriter().write("ok");
   }

   private void prinltStartLine(HttpServletRequest request) {
      System.out.println("--- REQUEST-LINE - start ---");
      System.out.println("request.getMethod() = " + request.getMethod());         //GET
      System.out.println("request.getProtocol() = " + request.getProtocol());     //HTTP/1.1
      System.out.println("request.getScheme() = " + request.getScheme());         //http                                                               
      System.out.println("request.getRequestURL() = " + request.getRequestURL()); // http://localhost:8080/request-header
      System.out.println("request.getRequestURI() = " + request.getRequestURI()); // /request-header
      System.out.println("request.getQueryString() = " +request.getQueryString());//username=hi
      System.out.println("request.isSecure() = " + request.isSecure());           //https 사용 유무
      System.out.println("--- REQUEST-LINE - end ---");
      System.out.println();

      /*
      --- REQUEST-LINE - start ---
      request.getMethod() = GET
      request.getProtocol() = HTTP/1.1
      request.getScheme() = http
      request.getRequestURL() = http://localhost:8080/request-header
      request.getRequestURI() = /request-header
      request.getQueryString() = username=hello
      request.isSecure() = false
      --- REQUEST-LINE - end ---
      */

   }

   //Header 모든 정보
private void printHeaders(HttpServletRequest request) {
   System.out.println("--- Headers - start ---");
  /*
   Enumeration<String> headerNames = requset.getHeaderNames();
   while (headerNames.hasMoreElements()) {
         String headerName = headerNames.nextElement();
         System.out.println(headerNmae + ": " + headerName);
   }  
  */
   request.getHeaderNames().asIterator()
            .forEachRemaining(headerName -> System.out.println(headerName + ": " + headerName));
   System.out.println("--- Headers - end ---");
   System.out.println();
  }
   
}