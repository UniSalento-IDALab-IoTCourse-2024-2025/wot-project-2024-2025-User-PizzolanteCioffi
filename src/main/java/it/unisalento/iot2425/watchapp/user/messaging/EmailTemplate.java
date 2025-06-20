/*
package it.unisalento.iot2425.watchapp.user.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EmailTemplate {
    public static String loadEmailTemplate(InputStream templateStream, String templatePath, String... args) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(templatePath)));
        return String.format(content, (Object[]) args);
    }
}
*/

package it.unisalento.iot2425.watchapp.user.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class EmailTemplate {
    public static String loadEmailTemplate(InputStream templateStream, String... args) throws IOException {
        if (templateStream == null) {
            throw new IOException("Template stream is null");
        }
        String content = new String(templateStream.readAllBytes(), StandardCharsets.UTF_8);
        return String.format(content, (Object[]) args);
    }
}