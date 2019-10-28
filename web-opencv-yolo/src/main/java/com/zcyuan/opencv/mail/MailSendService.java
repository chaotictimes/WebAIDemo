package com.zcyuan.opencv.mail;

import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

public class MailSendService {
	private ThreadPoolTaskExecutor taskExecutor = taskExecutor();

	public ThreadPoolTaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor poolExecutor = new ThreadPoolTaskExecutor();
		poolExecutor.initialize();
		// 核心线程数
		poolExecutor.setCorePoolSize(5);
		// 最大线程数
		poolExecutor.setMaxPoolSize(15);
		// 队列大小
		poolExecutor.setQueueCapacity(100);
		// 线程最大空闲时间
		poolExecutor.setKeepAliveSeconds(300);
		// 拒绝策略
		poolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		// 线程名称前缀
		poolExecutor.setThreadNamePrefix("my-pool-");

		return poolExecutor;
	}

	public void sendMail(String mailTo, String subject, String content) {
		JavaMailSenderImpl sender = new JavaMailSenderImpl();
		sender.setHost("smtp.163.com");
		sender.setPort(25); // 默认就是25
		sender.setUsername("work_services");
		sender.setPassword("************");

		// 配置文件对象
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true"); // 是否进行验证
		Session session = Session.getInstance(props);
		sender.setSession(session); // 为发送器指定会话

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(mailTo); // 发送给谁
		mail.setSubject(subject); // 标题
		mail.setFrom("work_services@163.com"); // 来自
		// 邮件内容
		mail.setText(content);

		sender.send(mail); // 发送
	}

	public void sendMailWithImg(String mailTo, String subject, String content, String filePath, String fname) {
		Properties properties = new Properties();
		properties.put("mail.smtp.host", "smtp.163.com");// 设置smtp主机
		properties.put("mail.smtp.auth", "true");// 使用smtp身份验证
		try {
			final MimeMessage message = new MimeMessage(Session.getInstance(properties, new Authenticator() {
				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(// 设置发送帐号密码
							"work_services@163.com", "************");
				}
			}));
			// 设置邮件的属性
			// 设置邮件的发件人
			message.setFrom(new InternetAddress("work_services@163.com"));
			// 设置邮件的收件人 cc表示抄送 bcc 表示暗送
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(mailTo));
			// 设置邮件的主题
			message.setSubject(subject);
			// 创建邮件的正文
			MimeBodyPart text = new MimeBodyPart();
			// setContent(“邮件的正文内容”,”设置邮件内容的编码方式”)
			text.setContent(content, "text/html;charset=gb2312");

			// 点到点的发送
			// 一对多发送只要改一个地方如下：

			// // 构建一个群发地址数组
			// InternetAddress[] adr=new InternetAddress[toMore.length];
			// for(int i=0;i<toMore.length;i++){ adr[i]=new
			// InternetAddress(toMore[i]); }
			// // Message的setRecipients方法支持群发。。注意:setRecipients方法是复数和点 到点不一样
			// message.setRecipients(Message.RecipientType.TO,adr);
			// 创建图片
			MimeBodyPart img = new MimeBodyPart();
			DataHandler dh = new DataHandler(new FileDataSource(filePath));// 图片路径
			img.setDataHandler(dh);
			img.setFileName(fname);
			// 创建图片的一个表示用于显示在邮件中显示
			img.setContentID("attach image");

			// 创建附件
			// MimeBodyPart attch = new MimeBodyPart();
			// DataHandler dh1 = new DataHandler(new FileDataSource("src//b.jpg"));
			// attch.setDataHandler(dh1);
			// String filename1 = dh1.getName();
			// MimeUtility 是一个工具类，encodeText（）用于处理附件字，防止中文乱码问题
			// attch.setFileName(MimeUtility.encodeText(filename1));
			// 关系 正文和图片的
			MimeMultipart mm = new MimeMultipart();
			mm.addBodyPart(text);
			mm.addBodyPart(img);
			mm.setSubType("related");// 设置正文与图片之间的关系
			// 图片与正文的 body
			MimeBodyPart all = new MimeBodyPart();
			all.setContent(mm);

			message.setContent(mm);
			message.saveChanges(); // 保存修改

			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						Transport.send(message);
					} catch (MessagingException e) {
						e.printStackTrace();
					}
				}
			});
			System.out.println("邮件发送成功");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}