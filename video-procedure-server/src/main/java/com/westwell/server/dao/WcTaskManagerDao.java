package com.westwell.server.dao;

import com.westwell.server.entity.WcTaskManagerEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * 作业管理记录表
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-03-02 17:05:19
 */
@Mapper
@Repository
public interface WcTaskManagerDao extends GenericDao<WcTaskManagerEntity>  {

}
