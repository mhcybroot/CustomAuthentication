Assignment on module 23
Title: Custom Authentication


Summary:
Build a simple Spring Boot project where users can create an account using email & password. Email of the user must be verified before the user can login.

For verifying the user, you should send the user a link to the user's email. If the user clicks on the link within 10 minutes, the user's account should be verified.

No UI is required — this is a backend-only, API-based project.

Requirements:
1. Use JWT for the authentication system.

2. Use a unique verification link. (Hint: Link can be a simple GET api endpoint with domain name)

3. Send the link to the email in proper format. (Hint: You can use JavaMailSender to send email. You just need an email address with an app password). Read here: https://medium.com/@AlexanderObregon/sending-emails-from-a-spring-boot-application-3cba9b051dbd.

4. Users should be able to login only after verification.

5. If users try to login without verification, you should send a verification link to the user’s mail again. (Obviously new link), But

6. Make sure not to send email before 5 minutes after the last email sent. You must return an error in the api response.

7. If you send a new verification link, the old one(if any) shouldn't work anymore.

8. Use any database. But, No in-memory-db is allowed.

9. Add validation as you need.
