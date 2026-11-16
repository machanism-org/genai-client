package org.machanism.machai.process.tools;

/*@guidance: >>> ${guidances}/def-class-javadoc.md */
/**
 * Signals that the current task has ended without terminating the
 * application.
 * <p>
 * Function tools can throw this exception to gracefully conclude an
 * interactive session or user-driven task. The surrounding application can
 * handle it as a control-flow signal and continue running.
 * </p>
 *
 * @author Viktor Tovstyi
 * @see SpecialException
 */
public class EndTaskException extends ToolException {
	/**
	 * Serialization version for this control-flow exception.
	 *
	 * @serial
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an exception with the specified detail message.
	 *
	 * @param message the detail message describing why the task ended; may be
	 *                {@code null}
	 */
	public EndTaskException(String message) {
		super(message);
	}
}
