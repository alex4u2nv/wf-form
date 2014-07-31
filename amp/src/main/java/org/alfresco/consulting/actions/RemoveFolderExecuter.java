package org.alfresco.consulting.actions;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.log4j.Logger;
/**
 * This ActionExecuter will remove a folder.
 * Additional parameters are included to 
 * 		* force removal if not empty,
 *		* do nothing and silently exist if not empty
 *		* fail and throw an exception if not empty
 * @author Alexander Mahabir<alex.mahabir@alfresco.com>
 *
 */
public class RemoveFolderExecuter extends ActionExecuterAbstractBase{
	public static final String NAME = "rmdir";
	
	public static final String PARAM_FORCE_DELETION="force-deletion";
	public static final String PARAM_THROW_EXCEPTION_ON_FAILURE="exception-failure";
	public static final String PARAM_EXCEPTION_LIST="exception-list";
	private FileFolderService fileFolderService;
	private NodeService nodeService;
	Logger logger = Logger.getLogger(RemoveFolderExecuter.class);
	
	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		
		
		Boolean forceDeletion = (Boolean) action.getParameterValue(PARAM_FORCE_DELETION);
		Boolean exceptionFailure = (Boolean) action.getParameterValue(PARAM_THROW_EXCEPTION_ON_FAILURE);
		String exceptionList = (String) action.getParameterValue(PARAM_EXCEPTION_LIST);
		if (exceptionList == null
				|| exceptionList.indexOf(actionedUponNodeRef.toString()) < 0) {
			if (forceDeletion != null && forceDeletion)
				fileFolderService.delete(actionedUponNodeRef);
			else {
				int childrenCount = nodeService.countChildAssocs(
						actionedUponNodeRef, true);
				if (childrenCount > 0) {
					String msg = "Deletion failed.Folder {"
							+ actionedUponNodeRef + "} is not empty ";
					if (exceptionFailure != null && !exceptionFailure)
						throw new RemoveFolderException(msg);
					logger.debug(msg);
				} else {
					fileFolderService.delete(actionedUponNodeRef);
				}
			}
		} else {
			logger.debug("Skipping the deletion of " + actionedUponNodeRef);
		}
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_FORCE_DELETION, DataTypeDefinition.BOOLEAN, true, getParamDisplayLabel(PARAM_FORCE_DELETION)));
		paramList.add(new ParameterDefinitionImpl(PARAM_THROW_EXCEPTION_ON_FAILURE, DataTypeDefinition.BOOLEAN, true, getParamDisplayLabel(PARAM_THROW_EXCEPTION_ON_FAILURE)));
	}

}
