package tests;

import java.io.UnsupportedEncodingException;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class SendEmail
{
	public static void sentEmail(String from, String to, String text)
	{
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);

		String msgBody = "Testing....";

		try
		{
			Message msg = new MimeMessage(session);
			msg.setFrom(new InternetAddress(from,
					from));
			msg.addRecipient(Message.RecipientType.TO, new InternetAddress(
					to, to));
			msg.setSubject("test message");
			msg.setText(msgBody);
			Transport.send(msg);

		}
		catch (AddressException e)
		{
			// ...
		}
		catch (MessagingException e)
		{
			// ...
		}
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}