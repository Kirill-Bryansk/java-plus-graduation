package teamfive.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class StatHit {

    private final String app;
    private final String uri;
    private final Long hits;
}
