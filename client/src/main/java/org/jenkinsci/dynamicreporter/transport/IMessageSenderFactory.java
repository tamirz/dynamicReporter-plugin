package org.jenkinsci.dynamicreporter.transport;

/**
 * Factory for {@link MessageSender}
 */
public interface IMessageSenderFactory {

	public MessageSender getMessageSender();

}
