package hello.login.domain.member;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import java.util.*;
/**
 * 동시성 문제가 고려되어 있지 않음, 실무에서는 ConcurrentHashMap, AtomicLong 사용 고려
 */
@Slf4j
@Repository
public class MemberRepository {
    private static Map<Long, Member> store = new HashMap<>(); //static 사용
    private static long sequence = 0L; //static 사용

    public Member save(Member member) {
        member.setId(++sequence);
        log.info("save: member={}", member);
        store.put(member.getId(), member); // 찾은 id를 member에 집어 넣는다.
        return member;
    }

    public Optional<Member> findByLongId(String loginId) {
//        List<Member> all = findAll();
//        for (Member m : all) {
//            if (m.getLoginId().equals(loginId)) {
//                return Optional.of(m);
//            }
//        }
//        return Optional.empty();

        return findAll().stream() // List = Stream roof 가 돈다고 생각
                .filter(m -> m.getLoginId().equals(loginId)) // 조건에 만족하는 값만 넘어간다.
                .findFirst(); // 먼저 나오는 값만 반환하는 것.
    }

    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    public Member findById(Long id) {
        return store.get(id);
    }

    public void clearStore() {
        store.clear();
    }
}
