package junwatson.mychat.controller;

import jakarta.servlet.http.HttpServletRequest;
import junwatson.mychat.dto.request.MemberSignInRequestDto;
import junwatson.mychat.dto.response.TokenDto;
import junwatson.mychat.dto.request.MemberSignUpRequestDto;
import junwatson.mychat.dto.response.ReissueAccessTokenResponseDto;
import junwatson.mychat.service.GoogleLoginService;
import junwatson.mychat.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/authorization")
@Slf4j
public class AuthorizationController {

    private final GoogleLoginService googleLoginService;
    private final MemberService memberService;

    @GetMapping("/google")
    public TokenDto googleCallback(@RequestParam(name = "code") String code) {
        log.info("AuthorizationController.googleCallback() called");

        String googleAccessToken = googleLoginService.getGoogleAccessToken(code);
        return googleLoginService.loginOrSignUp(googleAccessToken);
    }

    @GetMapping("/reissue")
    public ResponseEntity<ReissueAccessTokenResponseDto> reissueAccessToken(HttpServletRequest request) {
        log.info("AuthorizationController.reissueAccessToken() called");

        ReissueAccessTokenResponseDto responseDto = memberService.reissueAccessToken(request);

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping
    public ResponseEntity<TokenDto> signUp(@RequestBody MemberSignUpRequestDto requestDto) {
        log.info("AuthorizationController.signUp() called");

        TokenDto responseDto = memberService.signUp(requestDto);

        return ResponseEntity.status(CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<TokenDto> signIn(@RequestBody MemberSignInRequestDto requestDto) {
        log.info("AuthorizationController.signIn() called");

        TokenDto responseDto = memberService.signIn(requestDto);

        return ResponseEntity.status(CREATED).body(responseDto);
    }
}
