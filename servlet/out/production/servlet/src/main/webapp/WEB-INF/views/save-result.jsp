<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <html>

    <head>
        <meta charset="UTF-8">
    </head>

    <body>
        성공
        <ul>
            <!--원래는: id=<%=((Member)request.getAttribute("member")).getId()%>-->
            <!--원래는: username=<%=((Member)request.getAttribute("member")).getUsername()%>-->
            <!--원래는: age=<%=((Member)request.getAttribute("member")).getAge()%>-->
            <li>id=${member.id}</li>
            <li>username=${member.username}</li>
            <li>age=${member.age}</li>
        </ul>
        <a href="/index.html">메인</a>
    </body>

    </html>