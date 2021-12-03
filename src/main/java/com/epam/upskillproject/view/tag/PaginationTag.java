package com.epam.upskillproject.view.tag;

import com.epam.upskillproject.util.init.PropertiesKeeper;
import com.epam.upskillproject.model.dto.Page;
import jakarta.inject.Inject;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.io.IOException;

public class PaginationTag<T> extends SimpleTagSupport {

    // MAX_PAGES_DISPLAYED may not be less than 3
    private static final int MAX_PAGES_DISPLAYED = 5;
    private static final String START_NAV_TAG_PROP = "pag.tag.startNav";
    private static final String END_NAV_TAG_PROP = "pag.tag.endNav";
    private static final String START_UL_TAG_PROP = "pag.tag.startUl";
    private static final String END_UL_TAG_PROP = "pag.tag.endUl";
    private static final String START_LI_TAG_PROP = "pag.tag.startLi";
    private static final String START_LI_ACTIVE_TAG_PROP = "pag.tag.startLiActive";
    private static final String START_LI_DISABLED_TAG_PROP = "pag.tag.startLiDisabled";
    private static final String END_LI_TAG_PROP = "pag.tag.endLi";
    private static final String SPAN_DOTS_TAG_PROP = "pag.tag.spanDots";
    private static final String SPAN_ACTIVE_PAGE_TAG_PATTERN_PROP = "pag.tag.spanActivePage";
    private static final String PAGE_LINK_TAG_PATTERN_PROP = "pag.tag.pageLink";

    @Inject
    private PropertiesKeeper propertiesKeeper;

    private Page<T> page;
    private String endpoint;

    @Override
    public void doTag() throws IOException {
        int currentPageNumber = page.getPageNumber();
        int entriesTotal = page.getTotal();
        int pageSize = page.getPageSize();
        String sort = page.getSort();

        int pagesTotal;
        if (entriesTotal <= pageSize) {
            pagesTotal = 1;
        } else {
            pagesTotal = (entriesTotal % pageSize == 0) ? entriesTotal / pageSize : entriesTotal / pageSize + 1;
        }
        int middleCell = MAX_PAGES_DISPLAYED / 2 + 1;

        // an array with numbers of pages to display in a pagination block
        int[] displayedPages = null;

        // in this case all the page numbers will be displayed
        if (pagesTotal <= MAX_PAGES_DISPLAYED && pagesTotal > 1) {
            displayedPages = new int[pagesTotal];
            for (int i = 0; i < displayedPages.length; i++) {
                displayedPages[i] = i + 1;
            }
        // in this case will be displayed the first page number, the last page number and (MAX_PAGES_DISPLAYED - 2)
        // other page numbers
        } else if (pagesTotal > 1) {
            displayedPages = new int[MAX_PAGES_DISPLAYED];
            // the first page number
            displayedPages[0] = 1;
            // the last page number
            displayedPages[MAX_PAGES_DISPLAYED - 1] = pagesTotal;
            // other numbers
            if (currentPageNumber <= middleCell) {
                for (int i = 1; i < displayedPages.length - 1; i++) {
                    displayedPages[i] = i + 1;
                }
            } else if (currentPageNumber > pagesTotal - middleCell) {
                int pageNumber = pagesTotal;
                for (int i = displayedPages.length - 2; i >= 1; i--) {
                    displayedPages[i] = --pageNumber;
                }
            } else {
                int pageNumber = currentPageNumber - MAX_PAGES_DISPLAYED / 2;
                for (int i = 1; i < displayedPages.length - 1; i++) {
                    displayedPages[i] = ++pageNumber;
                }
            }
        }

        if (displayedPages != null) {
            JspWriter writer = getJspContext().getOut();
            writer.println(propertiesKeeper.getString(START_NAV_TAG_PROP));
            writer.println(propertiesKeeper.getString(START_UL_TAG_PROP));

            for (int i = 0; i < displayedPages.length; i++) {
                // if a 'three-dots' placeholder is needed
                if ((i == 1 && displayedPages[i] - 1 != displayedPages[0]) ||
                        (i == displayedPages.length - 1 && displayedPages[i] - 1 != displayedPages[i - 1])) {
                    writer.println(propertiesKeeper.getString(START_LI_DISABLED_TAG_PROP));
                    writer.println(propertiesKeeper.getString(SPAN_DOTS_TAG_PROP));
                    writer.println(propertiesKeeper.getString(END_LI_TAG_PROP));
                }
                // an active page number highlighting
                writer.println((displayedPages[i] == currentPageNumber) ?
                        propertiesKeeper.getString(START_LI_ACTIVE_TAG_PROP) :
                        propertiesKeeper.getString(START_LI_TAG_PROP));
                writer.println((displayedPages[i] == currentPageNumber) ?
                        String.format(propertiesKeeper.getString(SPAN_ACTIVE_PAGE_TAG_PATTERN_PROP), displayedPages[i]) :
                        String.format(propertiesKeeper.getString(PAGE_LINK_TAG_PATTERN_PROP),
                                endpoint, displayedPages[i], pageSize, sort, displayedPages[i]));
                writer.println(propertiesKeeper.getString(END_LI_TAG_PROP));
            }
            writer.println(propertiesKeeper.getString(END_UL_TAG_PROP));
            writer.println(propertiesKeeper.getString(END_NAV_TAG_PROP));
        }
    }

    public void setPage(Page<T> page) {
        this.page = page;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
