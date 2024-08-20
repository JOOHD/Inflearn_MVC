## 본문

### springMVC - paging(MyBatis 적용)

### 1.페이징(Paging)이란?
    - 페이징은 사용자에게 데이터를 제공할 때, 전체 데이터 중의 일부를 보여주는 방식이다.
  
### 2.페이징 파라미터 관리용 클래스 추가하기
    page : 현재 페이지 번호를 의미, 페이지 정보 계산에 사용된다.
    recordSize : 페이지당 출력할 데이터 개수를 의미하며, page와 함께 페이지 정보 계산에 사용된다.
    pageSize : 화면 하단에 출력할 페이지의 크기를 의미하며, 5로 지정하면 1~5까지, 10으로 지정하면 1~10까지의 페이지가 보이게 된다.
    keyword : 검색 키워드를 의미하며, MyBatis의 동적(Dynamic) SQL 처리에 사용된다.
    searchType : 검색 유형을 의미하며, keyword와 함께 검색처리에 사용된다.

    - 페이징과 검색만 해도, view에서 받을 파라미터가 5개나 됩니다.
    이처럼 적지 않은 수의 파라미터는 클래스로 관리하는 게 효율적입니다.

    ● SearchDto
    @Data
    public class SearchDto {

        private int page;             // 현재 페이지 번호
        private int recordSize;       // 페이지당 출력할 데이터 개수
        private int pageSize;         // 화면 하단에 출력할 페이지 사이즈
        private String keyword;       // 검색 키워드
        private String searchType;    // 검색 유형

        public SearchDto() {
            this.page = 1;
            this.recordSize = 10;
            this.pageSize = 10;
        }

        public int getOffset() {
            return (page - 1) * recordSize;
        }

    }
    - 생성자 : 객체가 생성되는 시점에 현재 페이지 번호는 1로, 페이지당 출력할 데이터 개수와 하단에 출력할 페이지 개수를 10으로 초기화합니다.
    - getOffset() : MariaDB에서 LIMIT 구문의 시작 부분에 사용되는 메서드입니다. SQL 쿼리를 작성한 후 해당 메서드가 어떻게 사용되는지 알아보자.

### 추가 : PagingVO (페이징 로직을 컨트롤러, 서비스가 아닌 VO에서 처리)
    public class PagingVO {

        // 현재 페이지, 시작 페이지, 끝 페이지, 게시글 총 갯수, 페이지당 글 갯수, 마지막 페이지, SQL쿼리에 쓸 start, end
        private int nowPage, startPage, endPage, total, cntPerPage, lastPage, start, end;
	    private int cntPage = 5;

        public PagingVO() {            
        }

        public PagingVO(int total, int nowPage, int cntPerPage) {
            setNowPage(nowPage);
            setCntPerPage(cntPerPage);
            setTotal(total);
            calcLastPage(getTotal(), getCntPerPage());
            calcStartEndPage(getNowPage(), cntPage);
            calcStartEnd(getNowPage(), getCntPerPage());
        }
        // 제일 마지막 페이지 계산
        public void calcLastPage(int total, int cntPerPage) {
            setLastPage((int) Math.ceil((double)total / (double)cntPerPage));
        }
        // 시작, 끝 페이지 계산
        public void calcStartEndPage(int nowPage, int cntPage) {
            setEndPage(((int)Math.ceil((double)nowPage / (double)cntPage)) * cntPage);
            if (getLastPage() < getEndPage()) {
                setEndPage(getLastPage());
            }
            setStartPage(getEndPage() - cntPage + 1);
            if (getStartPage() < 1) {
                setStartPage(1);
            }
        }
        // DB 쿼리에서 사용할 start, end값 계산
        public void calcStartEnd(int nowPage, int cntPerPage) {
            setEnd(nowPage * cntPerPage);
            setStart(getEnd() - cntPerPage + 1);
        }
    }

### 3.Mapper와 XML Mapper 수정하기
    ● Mapper 
    // 게시글 리스트 조회
    List<PostResponse> findAll(SearchDto params);

    // 게시글 수 카운팅
    int count(SearchDto params);

    ● Mapper.xml
    <!-- 게시글 리스트 조회 -->
    <select id="findAll" parameterType="com.study.common.dto.SearchDto" 
    resultType="com.study.domain.post.PostResponse">
        SELECT
            <include refid="pstColumns" />
        FROM
            tb_post
        WHERE
            delete_yn = 0
        ORDER BY
            id DESC
        LIMIT #{offset}, #{recordSize}
    </select>

    <!-- 게시글 수 카운팅 -->
    <select id="count" parameterType="com.study.common.dto.SearchDto" resultType="int">
        SELECT 
            COUNT(8)
        FROM
            tb_post
        WHERE
            delete_yn = 0
    </select>

    ● LIMIT
    LIMIT 구문은 SELECT 쿼리와 함께 사용되며, 반환되는 데이터의 개수를 지정할 수 있다.
    - offset : MyBaits에서 쿼리의 parameterType이 클래스의 객체인 경우, XML Mapper의 #{parameter}는 맴버 변수의 getter에 해당됩니다.
    쉽게 말해, get() 메서드를 이용해서 쿼리에 파라미터를 바인딩하는 개념입니다.
    
    findAll 쿼리에서 offset은 SearchDto 클래스의 getOffset() 메서드가 리턴하는, (page - 1) * recordSize를 계산한 값입니다.

        ex) page=3, recordSize를 10으로 가정해보면 (3-1) * 10 = 20입니다. 
        즉, 현재 페이지 번호가 3이라면 "LIMIT 20,10"으로 쿼리가 실행되며, 결론적으로 offset은 조회할 데이터의 시작 위치(몇 번째 데이터부터 조회할 것인지)를 의미.
    
    - recordSize : 시작 위치(offset)를 기준으로 조회할 데이터의 개수를 의미합니다.
  
### 4.Service 수정하기
    public List<PostResponse> findAllPost(final SearchDto params) {
        return postMapper.findAll(params);
    }

### 5.Controller 수정하기
    // 게시글 리스트 페이지
    @GetMapping("/post/list.do")
    public String openPostList(@ModelAttribute("params") final SearchDto params, Model model) {
        List<PostResponse> posts = postService.findAllPost(params);
        model.addAttribute("posts", posts);
        return "post/list";
    }