/*
 * This class represent a path's component representing a step made
 * of an axis and a test (axis::test)
 */
package fr.upsud.lri.pathExtractor;

/**
 * The Class StepItem.
 */
public class StepItem extends PathItem {
	
	/** The axis. */
	private String axis;
	
	/** The test. */
	private String test;
	
	/**
	 * Instantiates a new step item.
	 *
	 * @param axis the axis
	 * @param test the test
	 */
	public StepItem(String axis, String test){
		this.axis = axis.isEmpty() ? "child" : axis;
		this.test = test;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		//boolean slashOrSlashslash = isSlash() || isSlashSlash();
		//return slashOrSlashslash ? axis : axis + "::" + test;
		return test.isEmpty() ? axis : axis + "::" + test;
	}

	/**
	 * Sets the axis.
	 *
	 * @param axis the new axis
	 */
	public void setAxis(String axis) {
		this.axis = axis;
	}

	/**
	 * Gets the axis.
	 *
	 * @return the axis
	 */
	public String getAxis() {
		return axis;
	}

	/**
	 * Sets the test.
	 *
	 * @param test the new test
	 */
	public void setTest(String test) {
		this.test = test;
	}

	/**
	 * Gets the test.
	 *
	 * @return the test
	 */
	public String getTest() {
		return test;
	}

	/**
	 * Tests if the element represents a slash or not.
	 *
	 * @return true if the element is a slash, false otherwise
	 */
	public boolean isSlash() {
		return axis.compareToIgnoreCase("/") == 0;
	}
	
	/**
	 * Tests if the element represents a slashslash or not.
	 *
	 * @return true if the element is a slashslash, false otherwise
	 */
	public boolean isSlashSlash() {
		return axis.compareToIgnoreCase("//") == 0;
	}
	
	/**
	 * Tests if the element represents a doc.
	 *
	 * @return true if the element is a doc, false otherwise
	 */
	public boolean isDoc() {
		return axis.startsWith("doc(\"") && axis.endsWith("\")");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((axis == null) ? 0 : axis.hashCode());
		result = prime * result + ((test == null) ? 0 : test.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StepItem other = (StepItem) obj;
		if (axis == null) {
			if (other.axis != null)
				return false;
		} else if (!axis.equalsIgnoreCase(other.axis))
			return false;
		if (test == null) {
			if (other.test != null)
				return false;
		} else if (!test.equalsIgnoreCase(other.test))
			return false;
		return true;
	}
}
