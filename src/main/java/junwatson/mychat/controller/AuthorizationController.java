package junwatson.mychat.controller;

import junwatson.mychat.dto.TokenDto;
import junwatson.mychat.service.GoogleLoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthorizationController {

    private final GoogleLoginService googleLoginService;

    @GetMapping("/authorization/google")
    public TokenDto googleCallback(@RequestParam(name = "code") String code) {
        log.info("AuthorizationController.googleCallback()");

        String googleAccessToken = googleLoginService.getGoogleAccessToken(code);
        return googleLoginService.loginOrSignUp(googleAccessToken);
    }
}
