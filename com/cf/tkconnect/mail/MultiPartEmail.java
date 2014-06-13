/*
 */
package com.cf.tkconnect.mail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePart;

import com.cf.tkconnect.mail.Email;
import com.cf.tkconnect.mail.EmailAttachment;
import com.cf.tkconnect.mail.EmailException;
import com.cf.tkconnect.mail.EmailUtils;
import com.cf.tkconnect.mail.MultiPartEmail;
/**
 * A multipart email.
 *
 * <p>This class is used to send multi-part internet email like
 * messages with attachments.
 *
 * <p>To create a multi-part email, call the default constructor and
 * then you can call setMsg() to set the message and call the
 * different attach() methods.
 *
 */
public class MultiPartEmail extends Email
{
	static com.cf.tkconnect.log.Log logger = com.cf.tkconnect.log.LogSource
	.getInstance(MultiPartEmail.class);

    /** Body portion of the email. */
    private MimeMultipart container;

    /** The message container. */
    private BodyPart primaryBodyPart = null;

    /** The MIME subtype. */
    private String subType;

    /** Indicates if the message has been initialized */
    private boolean initialized;

    /** Indicates if attachments have been added to the message */
    private boolean boolHasAttachments;

    /**
     * Set the MIME subtype of the email.
     *
     * @param aSubType MIME subtype of the email
     * 
     */
    public void setSubType(String aSubType)
    {
        this.subType = aSubType;
    }

    /**
     * Get the MIME subtype of the email.
     *
     * @return MIME subtype of the email
     */
    public String getSubType()
    {
        return subType;
    }

    /**
     * Add a new part to the email.
     *
     * @param partContent The content.
     * @param partContentType The content type.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     */
    public Email addPart(String partContent, String partContentType)
        throws EmailException
    {
            BodyPart bodyPart = createBodyPart();
        try
        {
            bodyPart.setContent(partContent, partContentType);
            getContainer().addBodyPart(bodyPart);
        }
        catch (MessagingException me)
        {
            throw new EmailException(me);
        }

        return this;
    }

    /**
     * Add a new part to the email.
     *
     * @param multipart The MimeMultipart.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     *  
     */
    public Email addPart(MimeMultipart multipart) throws EmailException
    {
        try
        {
            return addPart(multipart, getContainer().getCount());
        }
        catch (MessagingException me)
        {
            throw new EmailException(me);
        }
    }

    /**
     * Add a new part to the email.
     *
     * @param multipart The part to add.
     * @param index The index to add at.
     * @return The email.
     * @throws EmailException An error occured while adding the part.
     */
    public Email addPart(MimeMultipart multipart, int index) throws EmailException
    {
            BodyPart bodyPart = createBodyPart();
        try
        {
            bodyPart.setContent(multipart);
            getContainer().addBodyPart(bodyPart, index);
        }
        catch (MessagingException me)
        {
            throw new EmailException(me);
        }

        return this;
    }

    /**
     * Initialize the multipart email.
     * 
     */
    protected void init()
    {
        if (initialized)
        {
            throw new IllegalStateException("Already initialized");
        }

        container = createMimeMultipart();
        super.setContent(container);

        initialized = true;
    }

    /**
     * Set the message of the email.
     *
     * @param msg A String.
     * @return An Email.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * 
     */
    public Email setMsg(String msg) throws EmailException
    {
        // throw exception on null message
        if (EmailUtils.isEmpty(msg))
        {
            throw new EmailException("Invalid message supplied");
        }
        try
        {
            BodyPart primary = getPrimaryBodyPart();

            if ((primary instanceof MimePart) && EmailUtils.isNotEmpty(charset))
            {
                ((MimePart) primary).setText(msg, charset);
            }
            else
            {
                primary.setText(msg);
            }
        }
        catch (MessagingException me)
        {
            throw new EmailException(me);
        }
        return this;
    }

    /**
     * Builds the actual MimeMessage
     *
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * 
     */
    public void buildMimeMessage() throws EmailException
    {
        try
        {
            if (primaryBodyPart != null)
            {
                // before a multipart message can be sent, we must make sure that
                // the content for the main body part was actually set.  If not,
                // an IOException will be thrown during super.send().

                   BodyPart body = this.getPrimaryBodyPart();
                try
                {
                    body.getContent();
                }
                catch (IOException e)
                {
                    // do nothing here.  content will be set to an empty string
                    // as a result.
                }
            }

            if (subType != null)
            {
                getContainer().setSubType(subType);
            }

            super.buildMimeMessage();
        }
        catch (MessagingException me)
        {
            throw new EmailException(me);
        }
    }

    /**
     * Attach an EmailAttachment.
     *
     * @param attachment An EmailAttachment.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * 
     */
    public MultiPartEmail attach(List<Map> attachments)
    	throws EmailException
    {
    	for(Map attachment:attachments)
    		attach( attachment);
    	return this;
    }

    public MultiPartEmail attach(Map attachment)
	    throws EmailException
	{
        if (attachment == null)
        {
            throw new EmailException("Invalid attachment supplied");
        }
		   String filename = (String) attachment.get("file_name");
		   String filepathwithname = (String) attachment.get("file_path");
		   String title = (String)attachment.get("file_title");
		   return attach(filename,filepathwithname, title);
	}

    public MultiPartEmail attach(String filename, String filepathwithname, String title)
        throws EmailException
    {
        MultiPartEmail result = null;

        try
        {
            File file = new File(filepathwithname);
            if (!file.exists())
            {
                throw new IOException(
                    "\"" + filepathwithname + "\" does not exist");
            }
            result =
                attach(
                    new FileDataSource(file),
                    filename,
                    title,
                    EmailAttachment.ATTACHMENT);
        }
        catch (Exception e)
        {
            throw new EmailException(
                "Cannot attach file \"" + filepathwithname + "\"",
                e);
        }
        return result;
    }

    /**
     * Attach a file located by its URL.  The disposition of the file
     * is set to mixed.
     *
     * @param url The URL of the file (may be any valid URL).
     * @param name The name field for the attachment.
     * @param description A description for the attachment.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * 
     */
    public MultiPartEmail attach(URL url, String name, String description)
        throws EmailException
    {
        return attach(url, name, description, EmailAttachment.ATTACHMENT);
    }

    /**
     * Attach a file located by its URL.
     *
     * @param url The URL of the file (may be any valid URL).
     * @param name The name field for the attachment.
     * @param description A description for the attachment.
     * @param disposition Either mixed or inline.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * 
     */
    public MultiPartEmail attach(
        URL url,
        String name,
        String description,
        String disposition)
        throws EmailException
    {
        // verify that the URL is valid
       try
       {
           InputStream is = url.openStream();
           is.close();
       }
       catch (IOException e)
       {
           throw new EmailException("Invalid URL set");
       }

       return attach(new URLDataSource(url), name, description, disposition);
    }

    /**
     * Attach a file specified as a DataSource interface.
     *
     * @param ds A DataSource interface for the file.
     * @param name The name field for the attachment.
     * @param description A description for the attachment.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * 
     */
    public MultiPartEmail attach(
        DataSource ds,
        String name,
        String description)
        throws EmailException
    {
        // verify that the DataSource is valid
        try
        {
            if (ds == null || ds.getInputStream() == null)
            {
                throw new EmailException("Invalid Datasource");
            }
        }
        catch (IOException e)
        {
            throw new EmailException("Invalid Datasource");
        }

        return attach(ds, name, description, EmailAttachment.ATTACHMENT);
    }

    /**
     * Attach a file specified as a DataSource interface.
     *
     * @param ds A DataSource interface for the file.
     * @param name The name field for the attachment.
     * @param description A description for the attachment.
     * @param disposition Either mixed or inline.
     * @return A MultiPartEmail.
     * @throws EmailException see javax.mail.internet.MimeBodyPart
     *  for definitions
     * 
     */
    public MultiPartEmail attach(
        DataSource ds,
        String name,
        String description,
        String disposition)
        throws EmailException
    {
        if (EmailUtils.isEmpty(name))
        {
            name = ds.getName();
        }
        BodyPart bodyPart = createBodyPart();
        try
        {
            getContainer().addBodyPart(bodyPart);

            bodyPart.setDisposition(disposition);
            bodyPart.setFileName(name);
            bodyPart.setDescription(description);
            bodyPart.setDataHandler(new DataHandler(ds));
        }
        catch (MessagingException me)
        {
            throw new EmailException(me);
        }
        setBoolHasAttachments(true);

        return this;
    }

    /**
     * Gets first body part of the message.
     *
     * @return The primary body part.
     * @throws MessagingException An error occured while getting the primary body part.
     * 
     */
    protected BodyPart getPrimaryBodyPart() throws MessagingException
    {
        if (!initialized)
        {
            init();
        }

        // Add the first body part to the message.  The fist body part must be
        if (this.primaryBodyPart == null)
        {
            primaryBodyPart = createBodyPart();
            getContainer().addBodyPart(primaryBodyPart, 0);
        }

        return primaryBodyPart;
    }

    /**
     * Gets the message container.
     *
     * @return The message container.
     * 
     */
    protected MimeMultipart getContainer()
    {
        if (!initialized)
        {
            init();
        }
        return container;
    }

    /**
     * Method that can be overridden if you don't
     * want to create a MimeBodyPart.
     * @return
     */
    protected BodyPart createBodyPart()
    {
        BodyPart bodyPart = new MimeBodyPart();
        return bodyPart;
    }
    /**
     *
     * @return
     */
    protected MimeMultipart createMimeMultipart()
    {
        MimeMultipart mmp = new MimeMultipart();
        return mmp;
    }

    /**
     * @return boolHasAttachments
     * 
     */
    public boolean isBoolHasAttachments()
    {
        return boolHasAttachments;
    }

    /**
     * @param b boolHasAttachments
     * 
     */
    public void setBoolHasAttachments(boolean b)
    {
        boolHasAttachments = b;
    }

    /**
     *
     * @return
     */
    protected boolean isInitialized()
    {
        return initialized;
    }

    /**
     *
     * @param b
     */
    protected void setInitialized(boolean b)
    {
        initialized = b;
    }

}
