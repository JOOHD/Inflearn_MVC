package hello.springmvc.basic.requestmapping;

import org.springframework.web.bind.annotation.*;

/**
 Postman으로 테스트
 회원 목록 조회: GET /mapping/users
 회원 등록: POST /mapping/users
 회원 조회: GET /mapping/users/id1
 회원 수정: PATCH /mapping/users/id1
 회원 삭제: DELETE /mapping/users/id1
 **/

@RestController
@RequestMapping("/mapping/users")
public class MappingClassController {

    @GetMapping
    public String user() {
        return "get users";
    }

    @PostMapping
    public String addUser() {
        return "get user";
    }

    @GetMapping("/{userId}")
    public String findUser(@PathVariable String userId) {
        return "get userId=" + userId;
    }

    @PatchMapping("/{userId")
    public String updateUser(@PathVariable String userId) {
        return "update userid=" + userId;
    }

    @DeleteMapping("/{userId")
    public String deleteUser(@PathVariable String userId) {
        return "delete userId=" + userId;
    }
}
