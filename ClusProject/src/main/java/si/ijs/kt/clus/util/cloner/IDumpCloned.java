
package si.ijs.kt.clus.util.cloner;

import java.lang.reflect.Field;


/**
 * @author: kostas.kougios
 *          Date: 06/08/13
 */
public interface IDumpCloned {

    void startCloning(Class<?> clz);


    void cloning(Field field, Class<?> clz);
}
