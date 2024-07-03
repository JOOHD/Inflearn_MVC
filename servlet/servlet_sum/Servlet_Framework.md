# 본문

### FrontController pattern 소개
![frontB](../servlet_img/front%20_before.png)

![frontA](../servlet_img/front_after.png)

### FrontController pattern 특징
    1.프론트 컨트롤러 서블릿 하나로 클라이언트의 요청을 받음
    2.프론트 컨트롤러가 요청에 맞는 컨트롤러 차자서 호출
    3.입구를 하나로!
    4.공통 처리 가능
    5.프론트 컨트롤러를 제외한 나머지 컨트롤러는 서블릿을 사용하지 않아도 됨

### FrontController V1 구조
![V1](../servlet_img/front_V1.png)    

### ControllerV1 interface
    public interface ControllerV1 {

        void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    }

    서블릿과 비슷한 모양의 컨트롤러 인터페이스를 도입한다. 
    각 컨트롤러들은 이 인터페이스를 구현하면 된다.
    프론트 컨트롤러는 이 인터페이스를 호출해서 구현과 관계없이 로직의 일관성을 가져갈 수 있다.

### MemberFormControllerV1 - 회원 등록 컨트롤러
    public class MemberFormControllerV1 implements ControllerV1 {
        @Override
        public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            String viewPath = "/WEB-INF/views/new-form.jsp";
            Requestdispatcher dispatcher = request.getRequestdispatcher(viewPath);
            dispatcher.forward(request, response);
        }
    }   

### FrontControllerServletV1 - V1 프론트 컨트롤러
    @WebServlet(name = "frontControllerServletV1", urlPatterns = "/frontcontroller/v1/*")
    public class FrontControllerServletV1 extends HttpServlet {

        private Map<String, ControllerV1> controllerMap = new HashMap<>();

        public FrontControllerServletV1() {
            ControllerMap.put("/front-controller/v1/members/new-form", new MemberFormControllerV1());
            ControllerMap.put("/front-controller/v1/members/save", new MemberSaveControllerV1());
            ControllerMap.put("/front-controller/v1/members", new MemberListControllerV1());
        }

        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

            String requestURI = request.getRequestURI();

            ControllerV1 controller = controllerMap.get(requestURI);
            if(controller == null) {
                response.setStatus(HttpSerlvetResponse.SC_NOT_FOUND);
                return;
            }

            controller.process(request, response);
        }
    }

    ● urlPatterns
    "/front-controller/v1/*" 하위 모든 요청은 서블릿이 받아들인다.
    ex) /front-controller/v1, /front-controller/v1/a, /front-controller/v1/a/b

    ● service() 
    requestURI를 조회해서 실제 호출할 컨트롤러를 controllerMap에서 찾는다.
    만약 없다면 404(SC_NOT_FOUND) 상태 코드를 반환한다.
    컨트롤러를 찾고 controller.process(request, response)을 호출해서 해당 컨트롤러르를 실행한다.

### FrontControllerServletV2 - V2 (View 분리)     
    String viewPath = "/WEB-INF/views/new-form.jsp";
    Requestdispatcher dispatcher = request.getRequestdispatcher(viewPath);
    dispatcher.forward(request, response);

### FrontController V2 구조
![V2](../servlet_img/front_V2.png)

### V2 = MyView 
    public class MyView {

        private String viewPath;

        public MyView(String viewPath) {
            this.viewPath = viewPath;
        }

        public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            Requestdispatcher dispatcher = request.getRequestdispatcher(viewPath);
            dispatcher.forward(request, response);
        }
    }

### MemberFormControllerV2 - 회원 등록 폼    
    public class MemberFormControllerV2 implements ControllerV2 {

        @Override
        public MyView process(HttpSerlvetRequest request, HttpServletResponse response) throws ServletException, IOException {
            return new MyView("/WEB-INF/views/new-form.jsp");
        }
    }

    이제 각 컨트롤러에서 dispatcher.forward()를 직접 생성해서 호출하지 않아도 된다. 단순히 MyView 객체를 생성하고 거기에 view 이름만 넣고 반환.

    String viewPath = "/WEB-INF/views/new-form.jsp";
    Requestdispatcher dispatcher = request.getRequestdispatcher(viewPath);
    dispatcher.forward(request, response);

    위에 코드가 return new MyView("/WEB-INF/views/new-form.jsp"); 축약 되었다.

### FrontControllerServletV2 - 프론트 컨트롤러    
    @WebServlet(name = "frontControllerServletV2", urlPatterns = "/front-
    controller/v2/*")
    public class FrontControllerServletV2 extends HttpServlet {

        private Map<String, ControllerV2> controllerMap = new HashMap<>();

        public FrontControllerServletV2() {
            controllerMap.put("/front-controller/v2/members/new-form", new MemberFormControllerV2());
            controllerMap.put("/front-controller/v2/members/save", new MemberSaveControllerV2());
            controllerMap.put("/front-controller/v2/members", new MemberListControllerV2());
        }

        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            String requestURI = request.getRequestURi();

            ControllerV2 Controller = controllerMap.get(requestURI);
            if(controller == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            NyView view = controller.process(request, response);
            view.render(request, response)
        }
    }

    ControllerV2의 반환 타입이 MyView이므로 프론트 컨트롤러의 호출 결과로
    MyView를 반환 받는다. 그리고 view.render()를 호출하면 forward 로직을 수행해서 JSP가 실행된다.

### MyView.render()
    public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Requestdispatcher dispatcher = request.getRequestdispatcher(viewPath);
        dispatcher.forward(request, response);
    }

    프론트 컨트롤러의 도입으로 MyView 객체의 render()를 호출하는 부분을 모두 일관되게 처리할 수 있다. 각각의 컨트롤러는 MyView 객체를 생성만 해서 반환하면 된다.

### FrontControllerServletV3 - V3 (Model 추가)
    ● 서블릿 종속성 제거
    Controller에서 HttpServletRequest, HttpServletResponse 는 불필요한 코드이다.
    요청 파라미터 정보는 자바의 Map으로 대신 넘기도록 하면 지금 구조에서는 컨트롤러가 서블릿 기술을 몰라도 동작할 수 있다.
    request 객체를 Model로 사용하는 대신에 별도의 Model 객체를 만들어서 반환하면 된다.

    V3부터는 서블릿 기술을 전혀 사용하지 않도록 변경.

    ● View 이름 중복 제거
    /WEB-INF/views/new-form.jsp -> new-form
    /WEB-INF/views/save-result.jsp -> save-result
    /WEB-INF/views/members.jsp -> members

### FrontController V3 구조    
![V3](../servlet_img/front_V3.png)

### ModelView
    지금까지 컨트롤러에서 서블릿에 종속적인 HttpServletRequest를 사용.
    Model도 request.setAttribute()를 통헤 데이터를 저장하고 view에 전달했다.
    HttpServletRequest를 사용하지 않고, setAttribute()를 호출할 수 없다.
    그래서 Model을 사용하여 view 이름까지 전달하는 객체를 만들어 보자.

    public class ModelView {

        //mmdel은 (key, value)로 구성 되어 있어서 필요한 데이터를 넣는다.
        private String viewName;
        private Map<String, Object> model = new HashMap<>();
    }

### MemberFormControllerV3 - 회원 등록 폼
    public class MemberFormControllerV3 implements ControllerV3 {
        @Override
        public ModelView process(Map<String, String> paramMap) {
            return new ModelView("new-form");
        }
    }
    paramMap에 담아서 호출해주고, 응답 결과로 view 이름과 view에 전달할 Model 데이터를 포함하는 ModelView 객체를 반환하면 된다.

### FrontControllerServletV3
    @WebServlet(name = "frontControllerServletV3", urlPatterns = "/front
    controller/v3/*")        
    public class FrontControllerServletV3() {
        controllerMap.put("/front-controller/v3/members/new-form", new 
        MemberFormControllerV3());
        controllerMap.put("/front-controller/v3/members/save", new 
        MemberSaveControllerV3());
        controllerMap.put("/front-controller/v3/members", new 
        MemberListControllerV3());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestURI = request.getRequestURI();

        ControllerV3 controller = controllerMap.get(requestURI);
        if(controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Map<String, String> paramMap = createParamMap(request);
        ModelView mv = controller.process(paramMap);

        String viewName = mv.getViewName();

        MyView view = viewResolver(viewName);
        view.render(mv.getModel(), request, response);

        private Map<String, String> createParamMap(HttpServletRequest request) {

            Map<String, String> paramMap = new HashMap<>();

            request.getParameterNames().asIterator().forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
            return paramMap;
        }

        private MyView viewResolver(String viewName) {
            return new MyView("/WEB-INF/views/" + viewName + ".jsp");
        }
    }

    ● viewResolver
    컨트롤러가 반환한 = 논리 뷰, 이름 = 실제 물리뷰, 그리고 실제 물리 경로가 있는 MyView 객체를 반환한다.
        논리 뷰 : members
        물리 뷰 : /WEB-INF/views/members.jsp

        view.render(mv.getModel(), request, response)
        -뷰 객체를 통해서 HTML 화면을 랜더링 한다.
        -뷰 객체의 render()는 모델 정보도 함께 받는다.
        -JSP는 request.getAttribute()로 데이터를 조회하기 때문에, 모델의 데이터를 꺼내서 request.setAttribute()로 담아둔다.
        -JSP로 포워드 해서 JSP를 렌더링 한다. 

### V3 = MyView 
    public class MyView {
        private String viewPath;;

        public MyView(String viewPath) {
            this.viewPath = viewPath;
        }

        public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
            Reqeustdispatcher dispatcher = request.getRequestdispatcher(viewPath);
            dispatcher.forward(request, response);
        }

        pucblic void render(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            modelToRequestAttribute(model, request);
            Requestdispatcher dispatcher = request.getRequestdispatcher(viewPath);
            dispatcher.forward(reqeust, response);
        }

        private void modelToReqeustAttribute(Map<String, Object> mdoel, HttpServletRequest request) {
            model.forEach((key, value) -> request.setAttribute(key, value));
        }
    }

### FrontControllerServletV4 - V4 (단순하고 실용적인 컨트롤러)
    앞서 만든 V3 컨트롤러는 서블릿 종속성을 제거하고 뷰 경로의 중복을 제거하는 등, 잘 설계된 컨트롤러이다.
    그런데 실제 컨트롤러의 인터페이스를 구현하는 개발자 입장에서는 항상 ModelView 객체를 생성하고 반환해야 하는 부분이 조금은 번거롭다.

    V3를 조금 변경해서 실제 구현하는 개발자들이 매우 편리하게 개발 가능한 V4를 구현해보자.

### FrontController V4 구조    
![V4](../servlet_img/front_V4.png)    

    기본적인 구조는 V3와 같다. 대신에 컨트롤러가 ModelView를 반환하지 않고, viewName만 반환한다.

### ControllerV4 interface
    public interface ControllerV4 {
        /**
         * @param paramMap
         * @param model
         * @return viewName
         */
        String process(Map<String, String> paramMap, Map<Stirng, Object> model);
    }

    이번 버전은 인터페이스에 ModelView가 없다. model 객체는 파라미터로 전달되기 때문에 그냥 사용, 결과로 view의 이름만 반환해주면 된다.

### MemberFormControllerV4
    public class MemberFormControllerV4 implements ControllerV4 {
        @Override
        public String process(Map<String, Stirng> paramMap, Map<String, Object> model) {
            return "new-form";
        }
    }

### FrontControllerServletV4
    @WebServlet(name = "frontControllerServletV4", urlPatterns = "/front-
    controller/v4/*")    
    public class FrontControllerServletV4 extends HttpServlet {

        private Map<Stirng, ControllerV4> controllerMap = new HashMap<>();

        public FrontControllerServletV4() {
        controllerMap.put("/front-controller/v4/members/new-form", new 
        MemberFormControllerV4());
        controllerMap.put("/front-controller/v4/members/save", new 
        MemberSaveControllerV4());
        controllerMap.put("/front-controller/v4/members", new 
        MemberListControllerV4());
        }

        @Override
        protected void service(HttpServletRequest request, HttpServletReponse response) throws ServletException, IOException {

            String requestURI = request.getRequestURI();

            ControllerV4 controller = controllerMap.get(requestURI);
            if(controller == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Map<String, String> paramMap = createParamMap(request);
            Map<String, Object> model = new HashMap<>(); // 추가

            String viewName = controller.process(paramMap, model);

            MyView view = viewResolver(viewName);
            view.render(model, request, response);
        }

        private Map<String, String> createParamMap(HttpServletRequest request) {

            Map<String, String> paramMap = new HashMap<>();

            request.getParameterNames().asIterator().forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
            return paramMap;
        }

        private MyView viewResolver(String viewName) {
            return new MyView("/WEB-INF/views/" + viewName + ".jsp");
        }
    }

    ● 정리
    이번 버전의 컨트롤러는 매우 단순하고 실용적이다. 기존 구조에서 모델을 파라미터로 넘기고, 뷰의 논리 이름을 반환한다.

### FrontControllerServletV5 - V5 (어댑터 패턴)    
    지금까지 우리가 개발한 프론트 컨트롤러는 한가지 방식의 컨트롤러 인터페이스만 사용할 수 있다.
    ControllerV3, ControllerV4는 완전히 다른 인터페이스이다. 
    따라서 호환이 불가능하다. 마치 V3는 110V이고, V4는 220V 전기 콘셉트 같은 것이다. 이럴 때 사용하는 기능이 어뎁터이다.
    
### FrontController V5 구조    
![V5](../servlet_img/front_V5.png)

    ● 핸들러 어댑터 : 중간에 어댑터 역할을 하는 어댑터가 추가되었는데, 이름이 핸들러 어댑터이다. 여기서 어댑터 역할을 해주는 덕분에 다양한 종류의 컨트롤러를 호출할 수 있다.

    ● 핸들러 : 컨트롤러의 이름을 더 넓은 범위인 핸들러로 변경했다. 그 이유는 이제 어댑터가 있기 때문에 꼭 컨트롤러의 개념 뿐만 아니라 어떠한 것이든 해당하는 종류의 어댑터만 있으면 다 처리할 수 있기 때문이다.

### MyHandlerAdater interface
    public interface MyHandlerAdapter {

        boolean supports(Object Handler);

        ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws ServletException, IOException;
    }   

    ● boolean supports(Object handler) 
        -handler는 컨트롤러를 말한다.
        -어댑터가 해당 컨트롤러를 처리할 수 있는지 판단하는 메서드다.
    ● ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler)    
        -어댑터는 실제 컨트롤러를 호출하고, 그 결과로 ModelView를 반환해야 한다.
        -실제 컨트롤러가 ModelView를 반환하지 못하면, 어댑터가 ModelView를 직접 생성해서라도 반환해야 한다.
        -이전에는 프론트 컨트롤러가 실제 컨트롤러를 호출했지만 이제는 어댑터를 통해서 실제 컨트롤러가 호출된다.

### ControllerV3HandlerAdapter
    public class ControllerV3HandlerAdapter implements MyHandlerAdapter {

        @Override
        public boolean supports(Object handler) { //ControllerV3 어댑터
            return (handler instanceof ControllerV3);
        }

        @Override
        public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler);

        Map<String, String> paramMap = createParamMap(request);

        ModelView mv = controller.process(paramMap);
        return mv;

        private Map<String, String> createParamMap(HttpServletRequest request) {

            Map<String, String> paramMap = new HashMap<>();

            request.getParameterNames().asIterator().forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));

            return paramMap;
        }
    }         

### FrontControllerServletV5
    @WebServlet(name = "frontControllerServletV5", urlPatterns = "/front
    controller/v5/*")
    public class FrontControllerServletV5 extends HttpServlet {

        private final Map<String, Object> handlerMappingMap = new HashMap<>(); //ControllerV3/V4 같은 interface 아무 값이나 받을 수 있는 Object 변경.
        private final List<MyHandlerAdapter> handlerAdapters = new ArrayList<>();

        public FrontControllerServletV5(){
            initHandlerMappingMap(); //핸들러 매핑 초기화
            initHandlerAdapters();   //어댑터 매핑 초기화
        }

        private void initHandlerMappingMap() {
            //V3
            handlerMappingMap.put("/front-controller/v5/v3/members/new-form", new MemberFormControllerV3());
            handlerMappingMap.put("/front-controller/v5/v3/members/save", new MemberSaveControllerV3());
            handlerMappingMap.put("/front-controller/v5/v3/members", new MemberListControllerV3());

             //V4 추가
            handlerMappingMap.put("/front-controller/v5/v4/members/new-form", new MemberFormControllerV4());
            handlerMappingMap.put("/front-controller/v5/v4/members/save", new MemberSaveControllerV4());
            handlerMappingMap.put("/front-controller/v5/v4/members", new MemberListControllerV4());
        }

        private void initHandlerAdapters() {
            handlerAdapters.add(new ControllerV3HandlerAdapter());
            handlerAdapters.add(new ControllerV4HandlerAdapter()); //V4 추가.
        }

        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

            Object handler = getHandler(request);
            if(handler == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            MyHandlerAdapter adapter = getHandlerAdapter(handler);
            ModelView mv = adapter.handle(request, response, handler);

            MyView view = viewResolver(mv.getViewName());
            view.render(mv.getModel(), request, response);
        }

        //handlerMappingMap에서 URL에 매핑된 핸들러(컨트롤러) 객체를 찾아서 반환한다.
        private Object getHandler(HttpServletRequest request) {
            String requestURI = request.getRequestURI();
            return handlerMappingMap.get(requestURI);
        }

        private MyHandlerAdapter getHandlerAdapter(Object handler) {
            for(MyHandlerAdapter adapter : handlerAdapters) {
                if(adapter.supports(handler)) { 
                    return adapter;
                }
            }
            throws new IllegalArgumentException("handler adapter를 찾을 수 없습니다. handler=" + handler);
        }

        private MyView viewResolver(String viewNam) {
            return new MyView("/WEB-INF/views/" + viewName + ".jsp");
        }
    }

### ControllerV4HandlerAdapter
    handlerMappingMap에 ControllerV4를 사용하는 컨트롤러 추가,
    해당 컨트롤러를 처리할 수 있는 어댑터인 ControllerV4HandlerAdapter추가. 

    public class ControllerV4HandlerAdapter implements MyHandlerAdapter {

        @Override
        public boolean supports(Object handler) {
            return (handler instanceof ControllerV4);
        }

        @Override
        public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) {

            ControllerV4 controller = (ControllerV4) handler;

            Map<String, String> paramMap = createParamMap(request);
            Map<String, Object> model = new HashMap<>();

            String viewName = controller.process(paramMap, model);

            ModelView mv = new ModelView(viewName);
            mv.setModel(model);

            return mv;
        }

        private Map<String, String> createParamMap(HttpServletRequest request) {
            Map<String, String> paramMap = new HashMap<>();
            request.getParameterNames().asIterator().forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
            return paramMap;
        }
    }

### 어댑터 변환
    ModelView mv = new ModelView(viewName);
    mv.setModel(model);

    return mv;
    어댑터가 호출하는 ControllerV4는 뷰의 이름을 반환한다.
    그런데 어댑터는 뷰의 이름이 아니라 ModelView를 만들어서 반환해야 한다.
    여기서 어댑터가 꼭 필요한 이유,
    ControllerV4는 뷰의 이름을 반환했지만, 어댑터는 이것을 ModelView로 만들어서 형식을 맞추어 반환한다.

### adapter ControllerV4
    public interface ControllerV4 {
        String process(Map<String, String> paramMap<String, Object> model);
    }    

    public interfacee MyHandlerAdapter {
        ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) thorws
    }
    
### 정리
    ● V1 : FrontController 도입    
        기존 구조를 최대한 유지하면서 프론트 컨트롤러를 도입
    ● v2 : View 분류
        단순 반복 되는 뷰 로직 분리
    ● v3 : Model 추가
        서블릿 종속성 제거
        뷰 이름 중복 제거
    ● V4 : 단순하고 실용적인 컨트롤러
        v3와 거의 비슷
        ModelView를 직접 생성해서 반환하지 않도록 편리한 인터페이스 제공
    ● V5 : 유연한 컨트롤러
        어댑터 도입
        어댑터를 추가해서 프레임워크를 유연하고 확장성 있게 설계

    여기에 어노테이션을 사용해서 컨트롤러를 더 편리하게 발전 가능하다.
    어노테이션을 사용해서 컨트롤러의 편리하게 사용할 수 있게 하려면, 어노테이션을 지원하는 어댑터를 추가해서 구현.