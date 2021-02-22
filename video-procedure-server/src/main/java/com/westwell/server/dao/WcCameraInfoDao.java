package com.westwell.server.dao;

import com.westwell.server.entity.WcCameraInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface WcCameraInfoDao extends GenericDao<WcCameraInfoEntity> {

}
