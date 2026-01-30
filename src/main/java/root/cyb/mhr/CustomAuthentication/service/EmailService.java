package root.cyb.mhr.CustomAuthentication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.base.url}")
    private String baseUrl;

    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String verificationLink = baseUrl + "/auth/verify?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Email Verification - Custom Authentication");
            message.setText("Hello,\n\n" +
                    "Thank you for registering with us.\n\n" +
                    "Please click on the link below to verify your email address:\n" +
                    verificationLink + "\n\n" +
                    "This link will expire in 10 minutes.\n\n" +
                    "If you did not create an account, please ignore this email.\n\n" +
                    "Best regards,\n" +
                    "Custom Authentication Team");

            mailSender.send(message);
            System.out.println("Verification email sent to: " + toEmail);
            System.out.println("Verification link: " + verificationLink);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            System.out.println("Verification link (Console): " + baseUrl + "/auth/verify?token=" + token);
        }
    }
}
