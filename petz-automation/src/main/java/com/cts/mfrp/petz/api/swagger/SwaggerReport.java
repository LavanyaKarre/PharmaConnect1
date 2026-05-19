package com.cts.mfrp.petz.api.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Writes the OpenAPI spec and a self-contained Swagger UI HTML page, then opens
 * the page in the default browser.
 *
 * <p>The HTML embeds the spec inline as a JS object (no fetch, no CORS, no
 * embedded HTTP server), so it works as a plain {@code file://} URL.
 */
public final class SwaggerReport {

    private static final Logger log = LoggerFactory.getLogger(SwaggerReport.class);

    private static final ObjectMapper JSON = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private static final String HTML_TEMPLATE = """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8" />
              <title>PETZ API - observed</title>
              <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css" />
              <style> body { margin: 0; } .swagger-ui .topbar { display: none; } </style>
            </head>
            <body>
              <div id="ui"></div>
              <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
              <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-standalone-preset.js"></script>
              <script>
                window.onload = function () {
                  window.ui = SwaggerUIBundle({
                    dom_id: '#ui',
                    spec: __SPEC__,
                    deepLinking: true,
                    layout: 'StandaloneLayout',
                    presets: [
                      SwaggerUIBundle.presets.apis,
                      SwaggerUIStandalonePreset
                    ],
                    plugins: [
                      SwaggerUIBundle.plugins.DownloadUrl
                    ],
                    tryItOutEnabled: true,
                    requestSnippetsEnabled: true,
                    persistAuthorization: true,
                    displayRequestDuration: true,
                    filter: true,
                    docExpansion: 'list'
                  });
                };
              </script>
            </body>
            </html>
            """;

    private SwaggerReport() {}

    /**
     * @param captures  the recorded HTTP exchanges
     * @param serverUrl base URL to show under "servers" in the UI
     * @param outputDir directory to drop openapi.json + index.html into
     * @param openInBrowser  if true, attempt Desktop.browse on the generated HTML
     */
    public static void write(List<Capture> captures, String serverUrl,
                             Path outputDir, boolean openInBrowser) {
        if (captures.isEmpty()) {
            log.warn("No captures recorded - skipping Swagger report.");
            return;
        }

        try {
            Files.createDirectories(outputDir);
            Map<String, Object> spec = OpenApiBuilder.build(captures, serverUrl);
            String specJson = JSON.writeValueAsString(spec);

            Path jsonFile = outputDir.resolve("openapi.json");
            Path htmlFile = outputDir.resolve("index.html");
            Files.writeString(jsonFile, specJson);
            Files.writeString(htmlFile, HTML_TEMPLATE.replace("__SPEC__", specJson));

            log.info("Swagger spec written: {}", jsonFile.toAbsolutePath());
            log.info("Swagger UI written:   {}", htmlFile.toAbsolutePath());

            if (openInBrowser) openInBrowser(htmlFile);
        } catch (IOException e) {
            log.error("Failed to write Swagger report", e);
        }
    }

    private static void openInBrowser(Path htmlFile) {
        try {
            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(htmlFile.toUri());
                log.info("Swagger UI opened in default browser.");
            } else {
                log.warn("Desktop browse unsupported - open manually: {}",
                        htmlFile.toAbsolutePath());
            }
        } catch (Exception e) {
            log.warn("Could not auto-open browser ({}) - open manually: {}",
                    e.getMessage(), htmlFile.toAbsolutePath());
        }
    }
}
