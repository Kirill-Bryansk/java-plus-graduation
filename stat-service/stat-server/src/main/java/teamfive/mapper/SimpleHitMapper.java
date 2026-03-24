package teamfive.mapper;

import dto.InputHitDto;
import dto.OutHitDto;
import dto.StatDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import teamfive.model.Hit;
import teamfive.model.StatHit;

@Mapper(componentModel = "spring")
public interface SimpleHitMapper {

    @Mapping(target = "id", ignore = true)
    Hit dtoToHit(InputHitDto inputHitDto);

    OutHitDto hitToDto(Hit hit);

    StatDto statHitToStatDto(StatHit statHit);
}
