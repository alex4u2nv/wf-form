/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * WorkflowUploadApplication tool component.
 * 
 * @namespace Alfresco
 * @class Alfresco.WorkflowUploadApplication
 */

require(["dojo/ready"], function(ready){
  ready(function(){
	  	dojo.declare("Alfresco.consulting.WorkflowUpload", null, {
		application: null,
		htmlid: null,
		picker: null,
		
		constructor: function(args) {
			dojo.safeMixin(this,args);
			
			
		},
		
		onUpload : function(e,p) {
			   if (!this.fileUpload)
	            {
	               this.fileUpload = Alfresco.getFileUploadInstance();
	            }
	            
	            // Show uploader for single file select - override the upload URL to use appropriate upload service
	            var uploadConfig =
	            {
	               flashUploadURL:"/consulting/services/workflow/upload",
	               htmlUploadURL: "/consulting/services/workflow/upload",
	               //mode: this.fileUpload.MODE_SINGLE_UPLOAD,
	               onFileUploadComplete:
	               {
	                  fn: this.onFileUploadComplete,
	                  scope: this
	               }
	            };	            
	            this.fileUpload.show(uploadConfig);
	            YAHOO.util.Event.preventDefault(e); //prevent default event I.E Form submission 
	            
		},
		/**
         * File Upload complete event handler
         *
         * @method onFileUploadComplete
         * @param complete {object} Object literal containing details of successful and failed uploads
         */
        onFileUploadComplete: function onFileUploadComplete(complete)
        {
           var success = complete.successful.length;
           if (success != 0)
           {
              var noderef = complete.successful[0].nodeRef;
              
              var records = this.picker.widgets.dataTable.getRecordSet().getRecords(),
              i = 0,
              il = records.length;
              var obj = {
            		  item : {
            			description: "Newly Upload Workflow File"
            			,displayPath: "/Shared/Workflow/Uploads"
            			,isContainer: false
            			,modified: new Date()
            			,modifier:  Alfresco.constants.USERNAME
            			,name: complete.successful[0].fileName
            			,nodeRef: complete.successful[0].nodeRef
            			,parentType: "cm:cmobject"
            			,selectable: true
            			,title: ""
            			,type: "cm:content"
            		  }
              }
          
          
        	   this.picker.widgets.dataTable.addRow(obj.item);
        	   this.picker.selectedItems[obj.item.nodeRef] = obj.item;
        	   this.picker.singleSelectedItem = obj.item;

              if (obj.highlight)
              {
                 // Make sure we scroll to the bottom of the list and highlight the new item
                 var dataTableEl = this.picker.widgets.dataTable.get("element");
                 dataTableEl.scrollTop = dataTableEl.scrollHeight;
                 Alfresco.util.Anim.pulse(this.picker.widgets.dataTable.getLastTrEl());
              }
          
              YAHOO.Bubbling.fire("renderCurrentValue",
            	         {
            	            eventGroup: this.picker
            	         })
              
           }
        }
		
	});

  });
});
  
