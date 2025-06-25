package bob.growingmdal.entity.response;

public class ErrorResponseBean<T> extends ResponseBean<T>{
	public ErrorResponseBean(String message) {
		super.setCode(400);
		super.setMessage(message);
		super.setData(null);
	}
	public ErrorResponseBean(int code,String message) {
		super.setCode(code);
		super.setMessage(message);
		super.setData(null);
	}
	public ErrorResponseBean(int code,String message,T data) {
		super.setCode(code);
		super.setMessage(message);
		super.setData(data);
	}
}
