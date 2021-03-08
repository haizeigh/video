package com.westwell.server.service.base;

import com.westwell.server.common.configs.DataConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class RpcBaseInspectService {

    public void checkInterface(String interfaceName, int code, String msg){

        if (code != DataConfig.SUCCESS_CODE){
            //todo 测试
//            throw new VPException( "接口["+interfaceName+"]调用失败，状态码="+code + ", msg=" +msg);
        }

    }
}
