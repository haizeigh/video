package com.westwell.server.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 作业管理记录表
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-03-02 17:05:19
 */

@Data
@Table(name = "wc_task_manager")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcTaskManagerEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@Id
	@GeneratedValue(generator = "JDBC")
	private Integer id;
	/**
	 * 作业的开始时间
	 */
	private Date startTime;
	/**
	 * 作业的结束时间
	 */
	private Date endTime;
	/**
	 * 任务状态
	 */
	private String status;
	/**
	 * 任务备注
	 */
	private String context;

}
