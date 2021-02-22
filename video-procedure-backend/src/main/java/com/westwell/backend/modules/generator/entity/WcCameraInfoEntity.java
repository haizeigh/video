package com.westwell.backend.modules.generator.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 摄像来源信息
 *
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2021-02-18 14:46:17
 */
@Data
@TableName("wc_camera_info")
public class WcCameraInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 */
	@TableId
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
