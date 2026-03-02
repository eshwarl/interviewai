package com.interviewai.service;

import com.interviewai.model.Interview;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Async("messageBrokerTaskScheduler")
    public void sendInterviewScheduledEmail(Interview interview) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(interview.getCandidate().getEmail());
            helper.setSubject("Interview Scheduled: " + interview.getTitle());
            helper.setText(buildEmailHtml(interview), true);

            mailSender.send(message);
            log.info("Interview email sent to {}", interview.getCandidate().getEmail());

        } catch (Exception e) {
            log.error("Failed to send interview email: {}", e.getMessage());
        }
    }

    private String buildEmailHtml(Interview interview) {
        String candidateName = interview.getCandidate().getName();
        String title         = interview.getTitle();
        String passkey       = interview.getPasskey();
        String scheduledTime = interview.getScheduledTime()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        int duration         = interview.getDurationMinutes();

        String joinFrom  = interview.getScheduledTime().minusMinutes(15)
                .format(DateTimeFormatter.ofPattern("hh:mm a"));
        String joinUntil = interview.getScheduledTime().plusMinutes(duration)
                .format(DateTimeFormatter.ofPattern("hh:mm a"));

        // 🔥 No String.formatted() — use concat to avoid % issues
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'/></head>" +
                "<body style='margin:0;padding:0;background:#020817;font-family:Segoe UI,Arial,sans-serif;'>" +
                "<div style='max-width:580px;margin:40px auto;background:#0f172a;border:1px solid rgba(255,255,255,0.08);border-radius:20px;overflow:hidden;'>" +

                // HEADER
                "<div style='background:linear-gradient(135deg,#1d4ed8,#7c3aed);padding:36px 40px;text-align:center;'>" +
                "<div style='font-size:36px;margin-bottom:12px;'>&#9889;</div>" +
                "<h1 style='color:white;margin:0;font-size:24px;font-weight:800;'>InterviewAI</h1>" +
                "<p style='color:rgba(255,255,255,0.75);margin:6px 0 0 0;font-size:14px;'>Your interview has been scheduled</p>" +
                "</div>" +

                // BODY
                "<div style='padding:36px 40px;'>" +
                "<p style='color:#94a3b8;font-size:15px;margin:0 0 24px 0;'>Hi <strong style='color:#f1f5f9;'>" + candidateName + "</strong>,<br><br>Your interview has been scheduled. Please find the details below.</p>" +

                // DETAILS
                "<div style='background:rgba(255,255,255,0.03);border:1px solid rgba(255,255,255,0.07);border-radius:14px;padding:24px;margin-bottom:24px;'>" +
                "<table style='width:100%;border-collapse:collapse;'>" +
                "<tr><td style='padding:10px 0;border-bottom:1px solid rgba(255,255,255,0.05);color:#64748b;font-size:12px;font-weight:600;width:40%;'>INTERVIEW</td>" +
                "<td style='padding:10px 0;border-bottom:1px solid rgba(255,255,255,0.05);color:#f1f5f9;font-size:14px;font-weight:600;'>" + title + "</td></tr>" +

                "<tr><td style='padding:10px 0;border-bottom:1px solid rgba(255,255,255,0.05);color:#64748b;font-size:12px;font-weight:600;'>DATE &amp; TIME</td>" +
                "<td style='padding:10px 0;border-bottom:1px solid rgba(255,255,255,0.05);color:#f1f5f9;font-size:14px;font-weight:600;'>" + scheduledTime + "</td></tr>" +

                "<tr><td style='padding:10px 0;border-bottom:1px solid rgba(255,255,255,0.05);color:#64748b;font-size:12px;font-weight:600;'>DURATION</td>" +
                "<td style='padding:10px 0;border-bottom:1px solid rgba(255,255,255,0.05);color:#f1f5f9;font-size:14px;font-weight:600;'>" + duration + " minutes</td></tr>" +

                "<tr><td style='padding:10px 0;color:#64748b;font-size:12px;font-weight:600;'>JOIN WINDOW</td>" +
                "<td style='padding:10px 0;color:#f1f5f9;font-size:14px;font-weight:600;'>" + joinFrom + " &mdash; " + joinUntil + "</td></tr>" +
                "</table></div>" +

                // PASSKEY
                "<div style='background:rgba(59,130,246,0.06);border:1px solid rgba(59,130,246,0.2);border-radius:14px;padding:24px;text-align:center;margin-bottom:24px;'>" +
                "<p style='color:#64748b;font-size:11px;font-weight:600;letter-spacing:0.08em;margin:0 0 10px 0;'>YOUR PASSKEY</p>" +
                "<div style='font-size:36px;font-weight:800;color:#60a5fa;font-family:monospace;letter-spacing:0.12em;margin-bottom:10px;'>" + passkey + "</div>" +
                "<p style='color:#475569;font-size:12px;margin:0;'>Use this passkey to join your interview session</p>" +
                "</div>" +

                // INSTRUCTIONS
                "<div style='background:rgba(16,185,129,0.05);border:1px solid rgba(16,185,129,0.15);border-radius:12px;padding:18px 20px;margin-bottom:28px;'>" +
                "<p style='color:#10b981;font-size:12px;font-weight:700;margin:0 0 10px 0;'>HOW TO JOIN</p>" +
                "<ol style='color:#94a3b8;font-size:13px;margin:0;padding-left:18px;line-height:1.8;'>" +
                "<li>Visit <strong style='color:#f1f5f9;'>interviewai.com</strong> and log in</li>" +
                "<li>Go to your Dashboard</li>" +
                "<li>Enter the passkey above in the Join box</li>" +
                "<li>Join button activates <strong style='color:#f1f5f9;'>15 minutes before</strong> your scheduled time</li>" +
                "<li>Click Start Interview when ready</li>" +
                "</ol></div>" +

                "<p style='color:#475569;font-size:12px;line-height:1.6;margin:0;'>If you have any issues joining, please contact your interviewer.<br>Make sure you are in a quiet environment with a stable internet connection.</p>" +
                "</div>" +

                // FOOTER
                "<div style='padding:20px 40px;border-top:1px solid rgba(255,255,255,0.05);text-align:center;'>" +
                "<p style='color:#334155;font-size:12px;margin:0;'>&#169; 2025 InterviewAI &middot; This is an automated message</p>" +
                "</div></div></body></html>";
    }
}