package com.cts.mfrp.petz.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.io.IOException;
import java.io.InputStream;

/**
 * Loads XML test-data fixtures from the classpath (typically src/test/resources/testdata/*.xml)
 * into typed POJOs via Jackson XML.
 *
 * <pre>
 *   RegisterCases data = XmlDataLoader.load("testdata/auth-register.xml", RegisterCases.class);
 * </pre>
 */
public final class XmlDataLoader {

    private static final XmlMapper MAPPER = buildMapper();

    private static XmlMapper buildMapper() {
        XmlMapper m = new XmlMapper();
        m.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return m;
    }

    public static <T> T load(String classpathResource, Class<T> type) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        try (InputStream in = cl.getResourceAsStream(classpathResource)) {
            if (in == null) {
                throw new IllegalStateException(
                        "XML test-data resource not found on classpath: " + classpathResource);
            }
            return MAPPER.readValue(in, type);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Failed to deserialize XML test-data " + classpathResource + " into " + type.getName(), e);
        }
    }

    private XmlDataLoader() {}
}
