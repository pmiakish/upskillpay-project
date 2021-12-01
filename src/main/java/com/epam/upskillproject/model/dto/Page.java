package com.epam.upskillproject.model.dto;

import com.epam.upskillproject.model.dao.queryhandlers.sqlorder.sort.SortType;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Page<?> page = (Page<?>) o;
        return pageNumber == page.pageNumber &&
                pageSize == page.pageSize &&
                total == page.total &&
                Objects.equals(entries, page.entries) &&
                Objects.equals(sortType, page.sortType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entries, pageNumber, pageSize, total, sortType);
    }

    @Override
    public String toString() {
        return "Page{" +
                "entries=" + entries +
                ", pageNumber=" + pageNumber +
                ", pageSize=" + pageSize +
                ", total=" + total +
                ", sortType=" + sortType +
                '}';
    }
}
