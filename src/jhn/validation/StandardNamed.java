package jhn.validation;

public class StandardNamed extends AbstractNamed {
	private final String name;
	
	public StandardNamed(String name) {
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}

}
