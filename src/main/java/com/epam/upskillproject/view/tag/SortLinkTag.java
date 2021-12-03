package com.epam.upskillproject.view.tag;

import com.epam.upskillproject.util.init.PropertiesKeeper;
import com.epam.upskillproject.model.dto.Page;
import jakarta.inject.Inject;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

public class SortLinkTag<T> extends SimpleTagSupport {

    private static final String ARROW_UP = " &#9651;";
    private static final String ARROW_DOWN = " &#9661;";
    private static final String DESC_SUFFIX = "_desc";
    private static final String DEFAULT_STYLE_TAG_ATTR_PROP = "sort.tag.attr.linkStyle.default";
    private static final String ACTIVE_STYLE_TAG_ATTR_PROP = "sort.tag.attr.linkStyle.active";
    private static final String SORT_LINK_TAG_PATTERN_PROP = "sort.tag.sortLink";

    @Inject
    private PropertiesKeeper propertiesKeeper;

    private Page<T> page;
    private String endpoint;
    private String target;
    private String description;

    @Override
    public void doTag() throws IOException {
        int pageSize = page.getPageSize();
        String currentSort = page.getSort();
        String columnName = (description != null && description.length() > 0) ? description : target;
        if (currentSort.equals(target.toLowerCase())) {
            columnName = columnName.concat(ARROW_UP);
        } else if (currentSort.equals(target.toLowerCase().concat(DESC_SUFFIX))) {
            columnName = columnName.concat(ARROW_DOWN);
        }
        String sortParam = (currentSort.equals(target.toLowerCase())) ? target.concat(DESC_SUFFIX) : target;
        String linkStyle = (currentSort.matches(target.toLowerCase().concat(".*"))) ?
                propertiesKeeper.getString(ACTIVE_STYLE_TAG_ATTR_PROP) :
                propertiesKeeper.getString(DEFAULT_STYLE_TAG_ATTR_PROP);
        JspWriter writer = getJspContext().getOut();
        writer.println(
                String.format(propertiesKeeper.getString(SORT_LINK_TAG_PATTERN_PROP),
                        linkStyle, endpoint, pageSize, sortParam, columnName)
        );

    }

    public void setPage(Page<T> page) {
        this.page = page;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
