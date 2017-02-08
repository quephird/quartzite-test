package quartzitetest;

/**
 * Created by danielle on 2/6/17.
 */
import clojure.lang.DynamicClassLoader;
import org.quartz.spi.ClassLoadHelper;

public class MyDynamicClassLoader extends DynamicClassLoader implements ClassLoadHelper {
    public void initialize() {}

    @SuppressWarnings("unchecked")
    public <T> Class<? extends T> loadClass(String name, Class<T> clazz)
            throws ClassNotFoundException {
        return (Class<? extends T>) loadClass(name);
    }

    public ClassLoader getClassLoader() {
        return this;
    }
}