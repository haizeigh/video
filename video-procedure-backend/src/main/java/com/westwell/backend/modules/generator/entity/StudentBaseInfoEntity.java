package com.westwell.backend.modules.generator.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 学生基本信息
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-08 10:15:59
 */
@Data
@TableName("wc_student_base_info")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StudentBaseInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@TableId
	private Integer id;
	/**
	 * 学生编号
	 */
	private String studentNum;
	/**
	 * 姓名
	 */
	private String studentName;
	/**
	 * 头像地址
	 */
	private String picsUrl;
	/**
	 * 视频地址
	 */
	private String videoUrl;
	/**
	 * 备注信息
	 */
	private String context;
	/**
	 * 创建时间
	 */
	private Date creatTime;
	/**
	 * 更新时间
	 */
	private Date updateTime;

}
