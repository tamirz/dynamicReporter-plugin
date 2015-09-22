package org.jenkinsci.dynamicreporter.server.messages;

import java.io.DataInput;
import java.io.IOException;

public interface ITestMessagesHandler {

	public abstract void processTestMessages(DataInput dataInput) throws IOException;

}