package com.likelion.mindiary.domain.account.service;

import com.likelion.mindiary.domain.account.model.Account;
import com.likelion.mindiary.domain.account.repository.AccountRepository;
import com.likelion.mindiary.domain.refreshToken.Repository.RefreshTokenRepository;
import com.likelion.mindiary.domain.refreshToken.model.RefreshToken;
import com.likelion.mindiary.global.config.JWT.JWTUtil;
import com.likelion.mindiary.global.config.Security.CustomOauth2UserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final JWTUtil jwtUtil;
    private final AccountRepository accountRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    public ResponseEntity<String> login(CustomOauth2UserDetails userDetails){

        Account member = accountRepository.findByLoginId(userDetails.getProvidedId());

        String accessToken = jwtUtil.createAccessToken(member.getLoginId(), userDetails.getRole(), 1000*100L); //10초
        String refreshToken = jwtUtil.createRefreshToken(1000*60*60*24*7L ); // 1달

        RefreshToken refreshToken1 = new RefreshToken();
        refreshToken1.setAccount(member);
        refreshToken1.setToken(refreshToken);

        refreshTokenRepository.save(refreshToken1);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header("RefreshToken", "Bearer " + refreshToken)
                .build();
    }

    public ResponseEntity<String> logout(String accessToken){
        String loginId = jwtUtil.getLoginId(accessToken);
        Account member = accountRepository.findByLoginId(loginId);
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByAccount(member);
        if(!refreshToken.isPresent()){
            return ResponseEntity.badRequest().body("유효하지 않은 토큰");
        }
        refreshTokenRepository.delete(refreshToken.get());
        return ResponseEntity.ok("refreshToken 삭제");
    }

}