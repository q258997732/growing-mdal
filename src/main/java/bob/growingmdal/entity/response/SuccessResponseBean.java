package bob.growingmdal.entity.response;

public class SuccessResponseBean<T> extends ResponseBean<T> {
	public SuccessResponseBean(T data) {
		super.setCode(200);
		super.setMessage("Success");
		super.setData(data);
	}
	public SuccessResponseBean(String message,T data) {
		super.setCode(200);
		super.setMessage(message);
		super.setData(data);
	}
}
