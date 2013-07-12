package jp.dobes.umvalidator.preference;

import jp.dobes.umvalidator.Activator;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * ê›íËèâä˙âª
 * 
 * @author ikaruga
 *
 */
public class Initializer extends AbstractPreferenceInitializer {

	public static String IS_OUT_CONSOLE            = "IS_OUT_CONSOLE";
	public static String IS_DETECT_MBSPACE         = "IS_DETECT_MBSPACE";
	public static String IS_DETECT_TAB             = "IS_DETECT_TAB";
	public static String IS_DETECT_VARNAME_CAMEL   = "IS_DETECT_VARNAME_CAMEL";
	public static String IS_DETECT_VARNAME_USCORE  = "IS_DETECT_VARNAME_USCORE";
	public static String IS_DETECT_CRLF            = "IS_DETECT_CRLF";
	public static String IS_DETECT_START_BRACKETS  = "IS_DETECT_START_BRACKETS";
	public static String IS_DETECT_AFTER_COMMA     = "IS_DETECT_AFTER_COMMA";
	public static String IS_DETECT_FUNCTION_RETURN = "IS_DETECT_FUNCTION_RETURN";
	
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		store.setDefault(Initializer.IS_OUT_CONSOLE, "true");
		store.setDefault(Initializer.IS_DETECT_MBSPACE, "true");
		store.setDefault(Initializer.IS_DETECT_TAB, "false");
		store.setDefault(Initializer.IS_DETECT_VARNAME_CAMEL, "false");
		store.setDefault(Initializer.IS_DETECT_VARNAME_USCORE, "false");
		store.setDefault(Initializer.IS_DETECT_CRLF, "true");
		store.setDefault(Initializer.IS_DETECT_START_BRACKETS, "false");
		store.setDefault(Initializer.IS_DETECT_AFTER_COMMA, "true");
		store.setDefault(Initializer.IS_DETECT_FUNCTION_RETURN, "true");
	}

}
