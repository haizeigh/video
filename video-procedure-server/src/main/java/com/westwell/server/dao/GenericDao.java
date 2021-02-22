package com.westwell.server.dao;

import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;
import tk.mybatis.mapper.common.MySqlMapper;

/**
 * 查询基础能力接口
 *
 * @author yanzy
 * @date 2020-03-18 11:32
 */
public interface GenericDao<T> extends Mapper<T>, MySqlMapper<T>, IdsMapper<T> {

}
