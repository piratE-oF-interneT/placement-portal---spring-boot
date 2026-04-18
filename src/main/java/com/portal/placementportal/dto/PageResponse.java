package com.portal.placementportal.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Transport-friendly wrapper around a Spring Data {@link Page}. We do not
 * serialize {@code Page} directly because its JSON shape is considered
 * unstable by the Spring team and depends on rendering configuration.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    /** Map page contents element-by-element. */
    public static <E, R> PageResponse<R> of(Page<E> page, Function<E, R> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    /**
     * Carry paging metadata from {@code page} but use externally-prepared
     * content (useful when content was built with a bulk join rather than
     * a per-row mapper).
     */
    public static <T> PageResponse<T> ofMapped(Page<?> page, List<T> mappedContent) {
        return new PageResponse<>(
                mappedContent,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
