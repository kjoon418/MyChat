package junwatson.mychat.controller;

import jakarta.servlet.http.HttpServletRequest;
import junwatson.mychat.dto.TokenDto;
import junwatson.mychat.dto.response.ReissueAccessTokenResponseDto;
import junwatson.mychat.service.GoogleLoginService;
import junwatson.mychat.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthorizationController {

    private final GoogleLoginService googleLoginService;
    private final MemberService memberService;

    @GetMapping("/authorization/google")
    public TokenDto googleCallback(@RequestParam(name = "code") String code) {
        log.info("AuthorizationController.googleCallback() called");

        String googleAccessToken = googleLoginService.getGoogleAccessToken(code);
        return googleLoginService.loginOrSignUp(googleAccessToken);
    }

    @GetMapping("/authorization/reissue")
    public ResponseEntity<ReissueAccessTokenResponseDto> reissueAccessToken(HttpServletRequest request) {
        log.info("AuthorizationController.reissueAccessToken() called");

        ReissueAccessTokenResponseDto responseDto = memberService.reissueAccessToken(request);

        return ResponseEntity.ok(responseDto);
    }
}
