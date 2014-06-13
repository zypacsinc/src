/*
 */
package com.cf.tkconnect.mail;

import com.cf.tkconnect.mail.Email;
import com.cf.tkconnect.mail.EmailException;
import com.cf.tkconnect.mail.EmailUtils;

/**
 * This class is used to send simple internet email messages without
 * attachments.
 *
*/
 public class SimpleEmail extends Email
	{
    /**
     * Set the content of the mail
     *
     * @param msg A String.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     */
    public Email setMsg(String msg) throws EmailException
    {
        if (EmailUtils.isEmpty(msg))
        {
            throw new EmailException("Invalid message supplied");
        }

        setContent(msg, TEXT_PLAIN);
        return this;
    }
}
