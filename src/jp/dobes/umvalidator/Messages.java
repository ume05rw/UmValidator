package jp.dobes.umvalidator;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "jp.dobes.umvalidator.messages";

    private  Messages(){

    }

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    public static String PREF_IS_OUT_CONSOLE;
    public static String PREF_IS_DETECT_MBSPACE;
    public static String PREF_IS_DETECT_TAB;
    public static String PREF_IS_DETECT_VARNAME_CAMEL;
    public static String PREF_IS_DETECT_VARNAME_USCORE;
    public static String PREF_IS_DETECT_CRLF;
    public static String PREF_IS_DETECT_START_BRACKETS;
    public static String PREF_IS_DETECT_AFTER_COMMA;
    public static String PREF_IS_DETECT_FUNCTION_RETURN;

    public static String ERRMSG_DETECT_MB_SPACE;
    public static String ERRMSG_DETECT_TAB;
    public static String ERRMSG_DETECT_VARNAME_CAMEL;
    public static String ERRMSG_DETECT_VARNAME_USCORE;
    public static String ERRMSG_DETECT_CRLF;
    public static String ERRMSG_STRING_AFTER_START_BRACKETS;
    public static String ERRMSG_LAST_ELEMENTS_AFTER_COMMA;
    public static String ERRMSG_FUNCTION_CLOSE_WITHOUT_RETURN;
}
