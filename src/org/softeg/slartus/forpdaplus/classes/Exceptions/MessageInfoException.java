package org.softeg.slartus.forpdaplus.classes.Exceptions;

/**
 * Created by IntelliJ IDEA.
 * User: slinkin
 * Date: 25.10.12
 * Time: 8:56
 * To change this template use File | Settings | File Templates.
 */
public class MessageInfoException extends Exception {
    public String Title;
    public String Text;

    public MessageInfoException(String title, String text){

        this.Title = title;
        this.Text = text;
    }
}
