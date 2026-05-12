package com.sunyard.module.system.api;

import javax.annotation.Resource;
import com.sunyard.framework.licence.task.service.ServerAuthService;
import org.springframework.web.bind.annotation.RestController;
import com.sunyard.framework.common.result.Result;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class ServerAuthApiImpl implements ServerAuthApi {

    @Resource
    private ServerAuthService serverAuthService;

    @Override
    public Result<Boolean> verifyServerAuth(String onlyFrontDate,String onlyDate){
        return serverAuthService.verifyServerAuth(onlyFrontDate,onlyDate);
    }

}
