
package si.ijs.kt.clus.util.exception;

import java.util.Arrays;
import java.util.List;

import si.ijs.kt.clus.main.settings.Settings;


public class ClusInvalidSettingsException extends ClusException {

    private static final long serialVersionUID = Settings.SERIAL_VERSION_ID;

    private static final String m_Msg = "Invalid settings configuration";


    public ClusInvalidSettingsException(String msg) {
        this(Arrays.asList(msg));
    }


    public ClusInvalidSettingsException(List<String> invalid) {
        super(invalid == null ? m_Msg : m_Msg + System.lineSeparator() + String.join(System.lineSeparator(), invalid));
    }
}
