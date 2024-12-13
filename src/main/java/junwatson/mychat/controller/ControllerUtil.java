package junwatson.mychat.controller;

import junwatson.mychat.domain.Member;
import junwatson.mychat.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ControllerUtil {

    private final MemberService memberService;

    public Member findMemberByPrincipal(Principal principal) {
        log.info("ControllerUtil.findMemberByPrincipal() called");

        Long memberId = Long.parseLong(principal.getName());

        return memberService.findById(memberId);
    }
}
