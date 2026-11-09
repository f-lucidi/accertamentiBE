package it.sogei.tabacchi.accertamenti.business.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ErrorMessageDto {

    private final String message;
}
