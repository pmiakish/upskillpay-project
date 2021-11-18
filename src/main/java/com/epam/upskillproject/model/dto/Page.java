package com.epam.upskillproject.model.dto;

import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.SortType;
import java.util.List;

public class Page<T> {
    private final List<T> entries;
    private final int pageNumber;
    private final int pageSize;
    private final int total;
    private final SortType sortType;

    public Page(List<T> entries, int pageNumber, int pageSize, int total, SortType sortType) {
        this.entries = entries;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.total = total;
        this.sortType = sortType;
    }

    public List<T> getEntries() {
        return entries;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotal() {
        return total;
    }

    public String getSort() {
        return sortType.getName();
    }
}
