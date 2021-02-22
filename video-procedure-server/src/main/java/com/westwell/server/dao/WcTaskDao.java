package com.westwell.server.dao;

import com.westwell.server.entity.WcTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;


/**
 * 任务记录表
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-18 14:46:17
 */
@Mapper
@Repository
public interface WcTaskDao extends GenericDao<WcTaskEntity> {

}
