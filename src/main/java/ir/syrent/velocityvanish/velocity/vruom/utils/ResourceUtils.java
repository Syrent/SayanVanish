package ir.syrent.velocityvanish.velocity.vruom.utils;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ResourceUtils {
    public static InputStream getResource(String resourceFileName) {
        return ResourceUtils.class.getClassLoader().getResourceAsStream(resourceFileName);
    }

    public static File copyResource(String resourceFileName, File file) {
        InputStream inputStream = getResource(resourceFileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream, outputStream);

            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

}