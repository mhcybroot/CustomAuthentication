package root.cyb.mhr.CustomAuthentication.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import root.cyb.mhr.CustomAuthentication.dto.AuthResponse;
import root.cyb.mhr.CustomAuthentication.dto.LoginRequest;
import root.cyb.mhr.CustomAuthentication.dto.RegisterRequest;
import root.cyb.mhr.CustomAuthentication.entity.User;
import root.cyb.mhr.CustomAuthentication.entity.VerificationToken;
import root.cyb.mhr.CustomAuthentication.repository.UserRepository;
import root.cyb.mhr.CustomAuthentication.repository.VerificationTokenRepository;
import root.cyb.mhr.CustomAuthentication.util.JwtUtil;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VerificationTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setVerified(false);
        user.setLastEmailSentAt(LocalDateTime.now());

        userRepository.save(user);

        VerificationToken token = new VerificationToken(user);
        tokenRepository.save(token);

        emailService.sendVerificationEmail(user.getEmail(), token.getToken());

        return new AuthResponse("Registration successful. Please check your email to verify your account.");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!user.isVerified()) {
            LocalDateTime lastEmailSent = user.getLastEmailSentAt();

            if (lastEmailSent != null) {
                long minutesSinceLastEmail = ChronoUnit.MINUTES.between(lastEmailSent, LocalDateTime.now());

                if (minutesSinceLastEmail < 5) {
                    long remainingMinutes = 5 - minutesSinceLastEmail;
                    throw new RuntimeException("Account not verified. Please wait " + remainingMinutes +
                            " more minute(s) before requesting a new verification email.");
                }
            }

            tokenRepository.findByUser(user).ifPresent(oldToken -> {
                oldToken.setUsed(true);
                tokenRepository.save(oldToken);
            });

            VerificationToken newToken = new VerificationToken(user);
            tokenRepository.save(newToken);

            user.setLastEmailSentAt(LocalDateTime.now());
            userRepository.save(user);

            emailService.sendVerificationEmail(user.getEmail(), newToken.getToken());

            throw new RuntimeException(
                    "Account not verified. A new verification email has been sent to your email address.");
        }

        String jwtToken = jwtUtil.generateToken(user.getEmail());

        return new AuthResponse("Login successful", jwtToken, user.getEmail());
    }

    @Transactional
    public AuthResponse verifyAccount(String tokenString) {

        VerificationToken token = tokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new RuntimeException(
                        "Invalid or expired verification token. Please request a new one by attempting to login."));

        User user = token.getUser();

        if (user.isVerified()) {
            return new AuthResponse(
                    "Your email is already verified. Please proceed to login.",
                    null,
                    user.getEmail());
        }

        if (token.isUsed()) {
            throw new RuntimeException(
                    "This verification link has already been used. Your email is verified. Please login to continue.");
        }
        if (token.isExpired()) {
            throw new RuntimeException(
                    "Verification link expired (10 min limit). Please login to receive a new verification email.");
        }

        user.setVerified(true);
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);

        return new AuthResponse(
                "Email verified successfully. You can now login.",
                null,
                user.getEmail());
    }
}
