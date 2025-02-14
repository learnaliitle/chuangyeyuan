package com.ruoyi.common.utils;

import lombok.extern.log4j.Log4j2;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Properties;

/**
 * @author TracyYang
 * @Description: 邮件工具类
 * @date 2023/7/22 14:15
 */
@Log4j2
public class MailUtils {

    // 邮件协议
    private static final String emailProtocol = "smtp";
    // 发件人的SMTP服务器地址（企业邮箱）
    private static final String emailSMTPHost = "smtp.qq.com";
    // 端口
    private static final String emailPort = "465";

    /**
     * 发送邮件
     *
     * @param email  目标邮件地址
     * @param title   邮件标题
     * @param content 邮件内容
     */
    public static boolean sendEmail(String email, String title, String content) {
        // 获得当前登录用户邮箱
        String emailAccount = SecurityUtils.getEmail();
        // 获得当前登录用户邮箱授权码
        String emailPassword = SecurityUtils.getEmailAuthorizationCode();

        // 检查邮箱授权码
        if (emailPassword == null || emailPassword.isEmpty()) {
            log.error("邮箱授权码为空，无法发送邮件");
            return false;
        }

        // 未传收件人邮箱地址则直接返回
        if (email == null || email.isEmpty()) return false;

        try {
            // 1. 创建参数配置, 用于连接邮件服务器的参数配置
            Properties props = new Properties();
            props.put("mail.smtp.host", emailSMTPHost);
            props.put("mail.smtp.port", emailPort);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", "true"); // 启用SSL
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.socketFactory.fallback", "false");

            // 2. 尝试连接并认证
            Session session = Session.getInstance(props);
            session.setDebug(false); // 生产环境关闭调试模式
            Transport transport = session.getTransport(emailProtocol);
            try {
                transport.connect(emailSMTPHost, Integer.parseInt(emailPort), emailAccount, emailPassword);
            } catch (Exception e) {
                log.error("邮箱授权码无效或连接失败", e);
                return false;
            }

            // 3. 创建邮件消息
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailAccount));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
            message.setSubject(title);
            message.setText(content);
            message.setSentDate(new Date());

            // 4. 发送邮件
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();

            return true;
        } catch (Exception e) {
            log.error("发送邮件失败,系统错误！", e);
            return false;
        }
    }
}
