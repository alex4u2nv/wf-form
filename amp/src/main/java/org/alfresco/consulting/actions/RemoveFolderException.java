package org.alfresco.consulting.actions;

import org.alfresco.error.AlfrescoRuntimeException;
/**
 * 
 * @author Alexander Mahabir
 *
 */
public class RemoveFolderException extends AlfrescoRuntimeException{

	public RemoveFolderException(String msgId, Object[] msgParams,
			Throwable cause) {
		super(msgId, msgParams, cause);
		// TODO Auto-generated constructor stub
	}

	public RemoveFolderException(String msgId, Object[] msgParams) {
		super(msgId, msgParams);
		// TODO Auto-generated constructor stub
	}

	public RemoveFolderException(String msgId, Throwable cause) {
		super(msgId, cause);
		// TODO Auto-generated constructor stub
	}

	public RemoveFolderException(String msgId) {
		super(msgId);
		// TODO Auto-generated constructor stub
	}

}
