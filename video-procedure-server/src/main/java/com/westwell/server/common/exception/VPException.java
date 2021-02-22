

package com.westwell.server.common.exception;


public class VPException extends RuntimeException {
	private static final long serialVersionUID = 1L;

    private String msg;
    private int code = 501;

    public VPException(String msg) {
		super(msg);
		this.msg = msg;
	}

	public VPException(String msg, Throwable e) {
		super(msg, e);
		this.msg = msg;
	}

	public VPException(String msg, int code) {
		super(msg);
		this.msg = msg;
		this.code = code;
	}

	public VPException(String msg, int code, Throwable e) {
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
