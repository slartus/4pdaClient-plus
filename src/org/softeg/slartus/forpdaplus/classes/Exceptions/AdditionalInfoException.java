package org.softeg.slartus.forpdaplus.classes.Exceptions;

import android.os.Bundle;

/**
 * User: slinkin
 * Date: 16.03.12
 * Time: 8:54
 */
public class AdditionalInfoException extends Exception {
    public static final String ARG_ATTACH_BODY="ArgAttachBody";
    private Bundle m_Args;

    public AdditionalInfoException(String message, String key, String value){
        super(message);

        m_Args=new Bundle();
        m_Args.putString(key,value);
    }
    
    public AdditionalInfoException(String message, Bundle args){
        super(message);
        m_Args=args;
    }
    
    public Bundle getArgs(){
        return m_Args;
    }
}
