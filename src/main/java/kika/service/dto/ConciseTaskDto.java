package kika.service.dto;

import java.util.List;

public record ConciseTaskDto (
    long id,
    String name,
    List<ConciseTaskDto> children
) {}
