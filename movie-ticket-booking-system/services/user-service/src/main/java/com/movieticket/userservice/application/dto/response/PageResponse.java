package com.movieticket.userservice.application.dto.response;




import lombok.Builder;
import lombok.Getter;

import java.util.List;


@Getter
@Builder
public class PageResponse<T> {


    private List<T> content;


    private int page;


    private int size;


    private long totalElements;


    private int totalPages;

}