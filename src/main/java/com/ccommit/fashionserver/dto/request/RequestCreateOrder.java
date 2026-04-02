package com.ccommit.fashionserver.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RequestCreateOrder {
    List<Integer> orderProductIdList;
}
