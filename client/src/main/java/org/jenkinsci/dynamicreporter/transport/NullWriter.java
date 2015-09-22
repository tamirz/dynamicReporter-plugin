package org.jenkinsci.dynamicreporter.transport;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *  A Writer than does nothing.
 */
public class NullWriter extends DataOutputStream {

	public NullWriter(OutputStream out) {
		super(out);
	}

	@Override
	public void flush() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}

}
