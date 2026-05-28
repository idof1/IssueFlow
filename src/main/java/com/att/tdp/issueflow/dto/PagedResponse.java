package com.att.tdp.issueflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> data;
    private long total;
    private int page;
    private int pageSize;
}
