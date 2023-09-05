package test.java.servlet.domain.member;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

public class MemberRepositoryTest {

    MemberRepository memberRepository = MemberRepository.getInstatnce();

    @AfterEach // test 끝날 때 마다 초기화
    void AfterEach() {
        memberRepository.clearStore();// 설정 안되있을 시, 순서를 정해주지 않기에 에러가 발생
    }

    @Test
    void save() {
        // given 이런걸 주어줬을 때,
        Member member = new Member("hello", 20);

        // when 이런걸 실행햇을 때,
        Member saveMember = memberRepository.save(member);

        // then 이런 결과가 나와야 돼
        Member findMember = memberRepository.findById(saveMember.getId());
        Assertions.assertThat(findMember).isEqualsTo(saveMember);// 찾아온 member는 저장된 member와 같아야 된다.
    }

    @Test
    void findAll() {
        // given
        Member member1 = new Member("member1", 20);
        Member member2 = new Member("member2", 30);

        memberRepository.save(member1);
        memberRepository.save(member2);

        // when
        List<Member> result = memberRepository.findAll();

        // then
        assertThat(result.size()).isEqualTo(2);
        assertThat(result).contains(member1, member2);
    }

}
