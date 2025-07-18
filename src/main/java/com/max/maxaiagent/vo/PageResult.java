package com.max.maxaiagent.vo;

import lombok.Data;
import java.util.List;

/**
 * 分页结果封装类
 * @author xtwang
 */
@Data
public class PageResult<T> {
    
    /**
     * 当前页码
     */
    private Integer pageNum;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 总页数
     */
    private Integer totalPages;
    
    /**
     * 数据列表
     */
    private List<T> records;
    
    /**
     * 是否有下一页
     */
    private Boolean hasNext;
    
    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;
    
    public PageResult() {}
    
    public PageResult(Integer pageNum, Integer pageSize, Long total, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.records = records;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
        this.hasNext = pageNum < totalPages;
        this.hasPrevious = pageNum > 1;
    }
    
    /**
     * 创建分页结果
     */
    public static <T> PageResult<T> of(Integer pageNum, Integer pageSize, Long total, List<T> records) {
        return new PageResult<>(pageNum, pageSize, total, records);
    }
} 