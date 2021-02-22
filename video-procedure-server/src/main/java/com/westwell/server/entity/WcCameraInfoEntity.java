package com.westwell.server.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 摄像来源信息
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-18 14:46:17
 */

@Data
@Table(name = "wc_camera_info")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WcCameraInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@Id
	@GeneratedValue(generator = "JDBC")
	private Integer id;
	/**
	 * 摄像头编号
	 */
	private Integer cameraNo;
	/**
	 * 01表示主码流，02是子码流
	 */
	private String streamNo;
	/**
	 * nvr的ip
	 */
	private String nvrIp;
	/**
	 * nvr的开放端口
	 */
	private Integer nvrPort;
	/**
	 * 用户名
	 */
	private String userName;
	/**
	 * 密码
	 */
	private String passwd;

}
