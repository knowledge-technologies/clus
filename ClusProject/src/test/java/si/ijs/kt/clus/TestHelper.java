package si.ijs.kt.clus;

import java.io.IOException;

import si.ijs.kt.clus.data.rows.RowData;
import si.ijs.kt.clus.main.settings.Settings;
import si.ijs.kt.clus.util.exception.ClusException;
import si.ijs.kt.clus.util.jeans.util.cmdline.CMDLineArgs;

public class TestHelper {

  /**
   * Loads the data set that is specified in the {@code settingsFile} under the section [Data],
   * field 'File' and returns a RowData object.
   *
   * @param settingsFile name of the settings file
   * @return RowData object
   * @throws IOException
   * @throws ClusException
   */
  public static RowData getRowData(String settingsFile) {
    RowData data = null;
    Clus clus;

    try {
      clus = new Clus();
      Settings sett = clus.getSettings();
      CMDLineArgs cargs = new CMDLineArgs(clus);
      cargs.process(new String[] {"-silent", settingsFile});
      sett.getGeneric().setAppName(cargs.getMainArg(0));

      clus.initSettings(cargs);
      clus.initialize(cargs);

      data = clus.getData();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      clus = null;
    }

    return data;
  }
}
