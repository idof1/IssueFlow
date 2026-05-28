package com.att.tdp.issueflow.dto;

import lombok.Data;
import java.util.List;

@Data
public class ImportResult {
    private int created;
    private int failed;
    private List<String> errors;
}
