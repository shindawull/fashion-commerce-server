package com.ccommit.fashionserver.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdditionalInfoRequest {
    private String address;
    private String phoneNumber;
}
