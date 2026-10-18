package com.mycompany.quanlyphongkham.common;

import java.util.Collections;
import java.util.List;

/**
 * Lớp chuẩn hóa kết quả phân trang (Pagination Wrapper)
 * Dùng cho các API danh sách có khối lượng dữ liệu lớn
 *
 * @param <T> Kiểu dữ liệu của phần tử trong danh sách
 */
public class PageResponse<T> {

    private List<T> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;

    public PageResponse() {
        this.items = Collections.emptyList();
        this.page = 1;
        this.size = 10;
        this.totalElements = 0;
        this.totalPages = 0;
    }

    public PageResponse(List<T> items, int page, int size, long totalElements) {
        this.items = items != null ? items : Collections.emptyList();
        this.page = Math.max(page, 1);
        this.size = Math.max(size, 1);
        this.totalElements = Math.max(totalElements, 0);
        this.totalPages = (int) Math.ceil((double) this.totalElements / this.size);
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
