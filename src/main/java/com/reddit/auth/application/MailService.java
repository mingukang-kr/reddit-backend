package com.reddit.auth.application;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.reddit.auth.domain.NotificationEmail;
import com.reddit.common.exception.SpringRedditException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Async
	public void sendMail(NotificationEmail notificationEmail) {
    	
        MimeMessagePreparator messagePreparator = mimeMessage -> {
        	
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            
            messageHelper.setFrom("master@email.com");
            messageHelper.setTo(notificationEmail.getRecipient());
            messageHelper.setSubject(notificationEmail.getSubject());
            messageHelper.setText(notificationEmail.getBody());
        };
        
        try {
            mailSender.send(messagePreparator);
            log.info("메일이 성공적으로 발송되었습니다.");
        } catch (MailException e) {
        	// 사용자 정의 에러를 만들어서 내부 기술 에러를 노출시키지 않도록 한다.
            throw new SpringRedditException("메일을 발송하던 중 오류가 발생했습니다. 받는 사용자는 "
            		+ notificationEmail.getRecipient() + " 입니다. 상세 에러 : " + e);
        }
    }
}
