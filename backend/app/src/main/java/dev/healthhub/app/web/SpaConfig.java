package dev.healthhub.app.web;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Serves the built Vue SPA from the static locations and falls back to index.html for client-side
 * routes (e.g. /trends), so the whole app is same-origin behind one container. API, swagger,
 * actuator and webhook paths are never rewritten to index.html.
 */
@Configuration
public class SpaConfig implements WebMvcConfigurer {

    private final String[] staticLocations;

    public SpaConfig(@Value("${spring.web.resources.static-locations:"
            + "classpath:/META-INF/resources/,classpath:/resources/,classpath:/static/,classpath:/public/}")
            String[] staticLocations) {
        this.staticLocations = staticLocations;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations(staticLocations)
                .resourceChain(true)
                .addResolver(new SpaResourceResolver());
    }

    static class SpaResourceResolver extends PathResourceResolver {
        @Override
        protected Resource getResource(String resourcePath, Resource location) throws IOException {
            var requested = super.getResource(resourcePath, location);
            if (requested != null) {
                return requested;
            }
            if (isServerPath(resourcePath)) {
                return null;
            }
            return super.getResource("index.html", location);
        }

        private static boolean isServerPath(String path) {
            return path.startsWith("api/")
                    || path.startsWith("v3/api-docs")
                    || path.startsWith("swagger")
                    || path.startsWith("actuator/")
                    || path.startsWith("webhooks/")
                    || path.startsWith("connect/")
                    || path.equals("openapi.yaml");
        }
    }
}
