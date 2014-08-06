package org.alfresco.consulting.actions;

import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
import org.apache.log4j.Logger;

/**
 * This ActionExecuter will remove a folder. Additional parameters are included
 * to * force removal if not empty, * do nothing and silently exist if not empty
 * * fail and throw an exception if not empty
 * 
 * @author Alexander Mahabir<alex.mahabir@alfresco.com>
 * 
 */
public class RemoveFolderExecuter extends ActionExecuterAbstractBase {
	public static final String NAME = "rmdir";

	public static final String PARAM_FORCE_DELETION = "force-deletion";
	public static final String PARAM_THROW_EXCEPTION_ON_FAILURE = "exception-failure";
	/**
	 * Used to exclude directories from where this action will execute. such as
	 * template folder.
	 */
	public static final String PARAM_EXCEPTION_LIST = "exception-list";
	private FileFolderService fileFolderService;
	private NodeService nodeService;
	private RuleService ruleService;
	Logger logger = Logger.getLogger(RemoveFolderExecuter.class);

	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {

		Boolean forceDeletion = (Boolean) action
				.getParameterValue(PARAM_FORCE_DELETION);
		Boolean exceptionFailure = (Boolean) action
				.getParameterValue(PARAM_THROW_EXCEPTION_ON_FAILURE);
		String exceptionList = (String) action
				.getParameterValue(PARAM_EXCEPTION_LIST);
		if (logger.isDebugEnabled()) {
			logger.debug("Actioned Upon: " + actionedUponNodeRef);
			logger.debug("Force Deletion: " + forceDeletion);
			logger.debug("exceptionFailure: " + exceptionFailure);
			logger.debug("exceptionList: " + exceptionList);
		}

		NodeRef owningNodeRef = ruleService.getOwningNodeRef(action);

		if (exceptionList == null
				|| exceptionList.indexOf(owningNodeRef.toString()) < 0) {
			if (forceDeletion != null && forceDeletion)
				fileFolderService.delete(owningNodeRef);
			else {
				if (logger.isDebugEnabled()) {
					for (FileInfo child : fileFolderService
							.listFiles(owningNodeRef)) {
						logger.debug("Children: " + child.getNodeRef() + " - "
								+ child.getName());
					}
				}
				int childrenCount = fileFolderService.listFiles(owningNodeRef)
						.size();
				logger.debug("Children Count for: " + owningNodeRef + " : "
						+ childrenCount);
				if (childrenCount > 0) {
					String msg = "Deletion failed.Folder {" + owningNodeRef
							+ "} is not empty ";
					if (exceptionFailure != null && exceptionFailure)
						throw new RemoveFolderException(msg);
					logger.debug(msg);
				} else {
					fileFolderService.delete(owningNodeRef);
				}
			}
		} else {
			logger.debug("Skipping the deletion of " + owningNodeRef);
		}
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(new ParameterDefinitionImpl(PARAM_FORCE_DELETION,
				DataTypeDefinition.BOOLEAN, true,
				getParamDisplayLabel(PARAM_FORCE_DELETION)));
		paramList.add(new ParameterDefinitionImpl(
				PARAM_THROW_EXCEPTION_ON_FAILURE, DataTypeDefinition.BOOLEAN,
				true, getParamDisplayLabel(PARAM_THROW_EXCEPTION_ON_FAILURE)));
	}

	public FileFolderService getFileFolderService() {
		return fileFolderService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public NodeService getNodeService() {
		return nodeService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public RuleService getRuleService() {
		return ruleService;
	}

	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

}
