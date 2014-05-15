package perfclipse.perforations;

public class PerforationException extends Exception {

	public PerforationException(String message) {
		super(message);
	}

	public PerforationException(Exception e) {
		super(e);
	}
}
