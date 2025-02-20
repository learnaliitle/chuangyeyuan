package com.ruoyi.system.domain.DTO;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(description = "通过申请的请求参数")
public class PassApplicationDTO {
    private Integer applicationId;
    private String reason;
}
