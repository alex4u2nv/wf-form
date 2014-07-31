package org.alfresco.consulting.services.workflow;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.consulting.actions.RemoveFolderExecuter;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.evaluator.IsSubTypeEvaluator;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileFolderUtil;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationEvent;
import org.springframework.extensions.surf.util.AbstractLifecycleBean;
import org.alfresco.service.cmr.rule.Rule;
import org.alfresco.service.cmr.rule.RuleType;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionCondition;
import org.alfresco.service.cmr.action.CompositeAction;
/**
 * Initialize rules to handle cleanup up Shared/workflow upload folder.
 * @author alexmahabir
 *
 */
public class InitializeWorkflow extends AbstractLifecycleBean {
	Logger logger = Logger.getLogger(InitializeWorkflow.class);
	final StoreRef STORE_REF = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
	final boolean REQUIRED=true;
	final boolean NOT_REQUIRED=false;
	private TransactionService transactionService;
	private NodeService nodeService;
	private RuleService ruleService;
	private ActionService actionService;
	private FileFolderService fileFolderService;
	private SearchService searchService;
	@Override
	protected void onBootstrap(ApplicationEvent event) {
		setup();
		
	}

	@Override
	protected void onShutdown(ApplicationEvent event) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * System Initializer
	 * @throws Exception
	 */
	private void initialize() throws Exception {
		NodeRef sharedFolder = getSharedFolder();
		if (sharedFolder== null)
			throw new InitializeWorkflowException("Unable to initialize Workflow Upload Module");
		
		NodeRef uploadFolder = getUploadFolder();
		
		if (uploadFolder==null){ //If upload folder exists, then the system may have already been initialized.
			uploadFolder = mkdir(sharedFolder, "Workflow/Uploads",REQUIRED);
			NodeRef uploadTpl = mkdir(getTemplateFolder(), "temp-upload", REQUIRED);
			setMainRule(uploadTpl);
		}	
	}
	/**
	 * Setup rule
	 * @param uploadRef
	 */
	private void setMainRule(NodeRef uploadRef) { 
		Rule rule = new Rule();
		//create and set rules.
		final String RULE_TITLE = "Workflow Upload Directory Template";
		rule.setTitle(RULE_TITLE);
		rule.setExecuteAsynchronously(true);
		rule.applyToChildren(false);
		rule.setRuleDisabled(false);
		rule.setRuleType(RuleType.OUTBOUND);
		CompositeAction compositeAction = actionService.createCompositeAction();
		rule.setAction(compositeAction);

		//create and set action conditions.
		ActionCondition actionCondition = actionService
				.createActionCondition(IsSubTypeEvaluator.NAME);

		Map<String, Serializable> conditionParameters = new HashMap<String, Serializable>(
				1);
		conditionParameters.put(IsSubTypeEvaluator.PARAM_TYPE,
				ContentModel.TYPE_CONTENT); //setting subtypes to FOLDER
		actionCondition.setParameterValues(conditionParameters);
		compositeAction.addActionCondition(actionCondition);
		
		//create and set action proprties.
		Action action = actionService.createAction(RemoveFolderExecuter.NAME);
		action.setTitle("Workflow Upload Cleanup");
		action.setExecuteAsynchronously(true);
		Map<String, Serializable> actionPropsMap = compositeAction.getParameterValues();
		actionPropsMap.put(RemoveFolderExecuter.PARAM_FORCE_DELETION, false);
		actionPropsMap.put(RemoveFolderExecuter.PARAM_THROW_EXCEPTION_ON_FAILURE, false);
		actionPropsMap.put(RemoveFolderExecuter.PARAM_EXCEPTION_LIST, uploadRef);
		action.setParameterValues(actionPropsMap);
		
		compositeAction.addAction(action);
		ruleService.saveRule(uploadRef, rule);

	}
	
	/**
	 * Get the NodeRef of the Shared Folder
	 * @return NodeRef
	 */
	private NodeRef getSharedFolder() {
		final String PATH = "/app:company_home/app:shared";
		return getFolder(PATH,REQUIRED);
	}
	
	/**
	 * Get the NodeRef Spaces Template Folder
	 * @return NodeRef
	 */
	private NodeRef getTemplateFolder() {
		final String PATH = "/app:company_home/app:dictionary/app:space_templates";
		return getFolder(PATH,REQUIRED);
	}
	
	/**
	 * 
	 * @param xpath -- xpath of folder to find.
	 * @return
	 */
	private NodeRef getFolder(final String xpath) {
		return this.getFolder(xpath, NOT_REQUIRED);
	}
	
	/**
	 * 
	 * @param xpath -- xpath of folder to find.
	 * @param required -- if a noderef must be found; if it doesn't a fatal error will be reported.
	 * @return
	 */
	private NodeRef getFolder(final String xpath, final boolean required) {
		ResultSet resultSet = this.searchService.query(STORE_REF, SearchService.LANGUAGE_XPATH,xpath );
		
		try {
			if (resultSet.length()!=0) {
				return resultSet.getNodeRef(0);
			}
		} catch (Exception e){
			if (required) {
				logger.fatal("Could not find " + xpath ,e);
				throw e;
			} else {
				logger.debug("Could nod find " + xpath,e);
			}
		} finally {
			resultSet.close();
		}
		
		return null;
	}
	
	/**
	 * Get the NodeRef of the Upload Folder
	 * @return NodeRef
	 */
	private NodeRef getUploadFolder() {
		final String PATH="/app:company_home/app:shared/cm:Workflow/cm:Upload";
		return getFolder(PATH);
	}
	/**
	 * Make Directory
	 * @param parent
	 * @param path
	 * @return
	 */
	public NodeRef mkdir(final NodeRef parent,final String path) {
		return mkdir(parent, path,NOT_REQUIRED);
	}
	
	/**
	 * Make Directory
	 * @param parent
	 * @param path
	 * @param required
	 * @return
	 */
	public NodeRef mkdir(final NodeRef parent,final String path, final boolean required) {
		List<String> pathElements = Arrays.asList(path.split(""));
		
		FileInfo fileInfo = FileFolderUtil.makeFolders(fileFolderService,
				parent, pathElements, ContentModel.TYPE_FOLDER);
		
		if (fileInfo == null) {
			String msg = "Unable to create {" + parent + "}" + path;
			if (required) 
				throw new InitializeWorkflowException(msg);
			logger.warn(msg);
			return null;
		}
		return fileInfo.getNodeRef();
	}
	/**
	 * Initialize in a Retrying Transaction Helper,
	 * and as system user.
	 */
	private void setup() {
		final RetryingTransactionHelper txnHelper = transactionService
				.getRetryingTransactionHelper();
		final RetryingTransactionCallback<Object> callback = new RetryingTransactionCallback<Object>() {

			@Override
			public Object execute() throws Throwable {
				 
				
				initialize();
				
				return null;
			}
		};
		
		AuthenticationUtil.runAs(new AuthenticationUtil.RunAsWork<Object>() {

			@Override
			public Object doWork() throws Exception {
				txnHelper.doInTransaction(callback,false,true);
				return null;
			}
		}, AuthenticationUtil.getSystemUserName());
		
	}

	public TransactionService getTransactionService() {
		return transactionService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
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

	public ActionService getActionService() {
		return actionService;
	}

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	public FileFolderService getFileFolderService() {
		return fileFolderService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	public SearchService getSearchService() {
		return searchService;
	}

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	
}
