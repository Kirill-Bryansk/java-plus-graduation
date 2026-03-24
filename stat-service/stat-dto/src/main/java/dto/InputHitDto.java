package dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InputHitDto {

    @NotNull(message = "не заполнен app")
    private String app;

    @NotNull(message = "не заполнен uri")
    private String uri;

    @NotNull(message = "не заполнен ip")
    private String ip;

    @NotNull(message = "не заполнена дата запроса")
    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
}