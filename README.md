Alfresco Workflow Upload Form
=========

Alfresco Activiti workflow form control override with upload functionality that makes use of the following concepts:

  - [Custom Action Executer] [1]
  - [Space Templates] [2]
  - [Modified / Extended Server side javascript webscript] [3]
  - [OnBootstrap Initializer][4]
  - [Surf widget extension][5]
  - [Surf components override][5]
  - [Surf components widget reuse][5]
  - [Alfresco Form Controls][6]

Dependencies
--------------
  - Alfresco Enterprise 4.2.2
  - Maven 3.x
  - JDK 7.x




Installation
--------------

```sh
git clone [git-repo-url] alfresco-workflow-upload
cd alfresco-workflow-upload
mvn install -Prun
```

Usage
--------------
  - Log into [Alfresco Share] with username admin:admin
  - Start a new workflow under My Tasks
  - Select [New Task] from the dropdown
  - Fill in appropriate fields
  - Upload a file
  - Start Workflow

Additional Information
--------------
  - Files uploaded directly from the workflow form, will be uploaded into /Shared/Workflow/Uploads/[username]-Date
  - When the uploaded file is moved out of the uploaded folder, into another another location, the tmp folder will be deleted
  - While workflow is active, the associated files need to be in a directory that is accessible to the user assigned to the workflow. Otherwise they won't see the file.

Known Issues
--------------
  - OnBootstrap functionality does not work well with Solr. 



[Alfresco Share]:http://localhost:8080/share
[1]:https://wiki.alfresco.com/wiki/Custom_Actions
[2]:http://docs.alfresco.com/4.2/tasks/tuh-spaces-create-template.html
[3]:http://docs.alfresco.com/4.2/concepts/API-JS-intro.html
[4]:http://swazzy.com/docs/springsurf/d1/d2b/a00018.php
[5]:http://docs.alfresco.com/4.2/concepts/dev-extensions-share.html
[6]:http://docs.alfresco.com/4.2/tasks/forms-formcontrol-config.html

