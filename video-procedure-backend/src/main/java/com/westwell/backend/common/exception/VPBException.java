

package com.westwell.backend.common.exception;


public class VPBException extends RuntimeException {
	private static final long serialVersionUID = 1L;

    private String msg;
    private int code = 501;

    public VPBException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public VPBException(String msg, Throwable e) {
		super(msg, e);
		this.msg = msg;
	}

	public VPBException(String msg, int code) {
		super(msg);
		this.msg = msg;
		this.code = code;
	}

	public VPBException(String msg, int code, Throwable e) {
		super(msg, e);
		this.msg = msg;
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}


}
