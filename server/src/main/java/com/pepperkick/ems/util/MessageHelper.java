package com.pepperkick.ems.util;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class MessageHelper implements MessageSourceAware  {
    private MessageSource messageSource;

    public String getMessage(String tag) {
        return getMessage(tag, null);
    }

    public String getMessage(String tag, Object p1) {
        return getMessage(tag, new Object[] { p1 });
    }

    public String getMessage(String tag, Object p1, Object p2) {
        return getMessage(tag, new Object[] { p1, p2 });
    }

    public String getMessage(String tag, Object p1, Object p2, Object p3) {
        return getMessage(tag, new Object[] { p1, p2, p3 });
    }

    public String getMessage(String tag, Object[] params) {
        return messageSource.getMessage(tag, params, Locale.US);
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
}
