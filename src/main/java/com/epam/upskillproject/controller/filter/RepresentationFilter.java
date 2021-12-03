package com.epam.upskillproject.controller.filter;

import com.epam.upskillproject.util.init.PropertiesKeeper;
import jakarta.inject.Inject;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

@WebFilter("*")
public class RepresentationFilter implements Filter {

    // Encoding and content-type properties
    private static final String CHARACTER_ENCODING_PROP = "representation.encoding.default";
    private static final String CONTENT_TYPE_PROP = "representation.contentType.default";
    // Encoding and content-type default values
    private static final String DEFAULT_CHARACTER_ENCODING = "UTF-8";
    private static final String DEFAULT_CONTENT_TYPE = "text/html;charset=UTF-8";

    private String characterEncoding;
    private String contentType;

    @Inject
    private PropertiesKeeper propertiesKeeper;

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
            ServletException {
        // Sets default request character encoding
        if (req.getCharacterEncoding() == null || !characterEncoding.equalsIgnoreCase(req.getCharacterEncoding())) {
            req.setCharacterEncoding(characterEncoding);
        }
        // Sets default response character encoding
        if (resp.getCharacterEncoding() == null || !characterEncoding.equalsIgnoreCase(resp.getCharacterEncoding())) {
            resp.setCharacterEncoding(characterEncoding);
        }
        // Sets default response content type
        if (resp.getContentType() == null || !contentType.equalsIgnoreCase(resp.getContentType())) {
            resp.setContentType(contentType);
        }
        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        characterEncoding = propertiesKeeper.getStringOrDefault(CHARACTER_ENCODING_PROP, DEFAULT_CHARACTER_ENCODING);
        contentType = propertiesKeeper.getStringOrDefault(CONTENT_TYPE_PROP, DEFAULT_CONTENT_TYPE);
    }

}
