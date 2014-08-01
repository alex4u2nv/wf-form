function extractMetadata(file)
{
   // Extract metadata - via repository action for now.
   // This should use the MetadataExtracter API to fetch properties, allowing for possible failures.
   var emAction = actions.create("extract-metadata");
   if (emAction != null)
   {
      // Call using readOnly = false, newTransaction = false
      emAction.execute(file, false, false);
   }
}

function exitUpload(statusCode, statusMsg)
{
   status.code = statusCode;
   status.message = statusMsg;
   status.redirect = true;
}
/**
 * Generate a unique name for saving the folder into shared directory.
 * @returns
 */
function generateUniqueName() {
	return person.properties.userName+"-"+utils.toISO8601(new Date()).replace(".","_").replace(":","_").replace("T", " ");
}

function main()
{
   try
   {
      var filename = null,
         content = null,
         mimetype = null,
         siteId = null, site = null,
         containerId = null, container = null,
         destination = null,
         destNode = null,
         thumbnailNames = null,
         i;

      // Upload specific
      var uploadDirectory = null,
         contentType = null,
         aspects = [],
         overwrite = true; // If a filename clashes for a versionable file

      // Update specific
      var updateNodeRef = null,
         majorVersion = false,
         description = "";
      
      // Prevents Flash- and IE8-sourced "null" values being set for those parameters where they are invalid.
      // Note: DON'T use a "!==" comparison for "null" here.
      var fnFieldValue = function(p_field)
      {
         return p_field.value.length() > 0 && p_field.value != "null" ? p_field.value : null;
      };

      // allow the locale to be set via an argument
      if (args["lang"] != null)
      {
         utils.setLocale(args["lang"]);
      }

      // Parse file attributes
      for each (field in formdata.fields)
      {
         switch (String(field.name).toLowerCase())
         {
            case "filename":
               filename = fnFieldValue(field);
               break;
            
            case "filedata":
               if (field.isFile)
               {
                  filename = filename ? filename : field.filename;
                  content = field.content;
                  mimetype = field.mimetype;
               }
               break;

            case "siteid":
               siteId = fnFieldValue(field);
               break;

            case "containerid":
               containerId = fnFieldValue(field);
               break;

            case "destination":
               destination = fnFieldValue(field);
               break;

            case "uploaddirectory":
               uploadDirectory = fnFieldValue(field);
               if ((uploadDirectory !== null) && (uploadDirectory.length() > 0))
               {
                  if (uploadDirectory.charAt(uploadDirectory.length() - 1) != "/")
                  {
                     uploadDirectory = uploadDirectory + "/";
                  }
                  // Remove any leading "/" from the uploadDirectory
                  if (uploadDirectory.charAt(0) == "/")
                  {
                     uploadDirectory = uploadDirectory.substr(1);
                  }
               }
               break;

            case "updatenoderef":
               updateNodeRef = fnFieldValue(field);
               break;

            case "description":
               description = field.value;
               break;

            case "contenttype":
               contentType = field.value;
               break;

            case "aspects":
               aspects = field.value != "-" ? field.value.split(",") : [];
               break;

            case "majorversion":
               majorVersion = field.value == "true";
               break;

            case "overwrite":
               overwrite = field.value == "true";
               break;

            case "thumbnails":
               thumbnailNames = field.value;
               break;
         }
      }

      //MNT-7213 When alf_data runs out of disk space, Share uploads result in a success message, but the files do not appear
      if (formdata.fields.length == 0)
      {
         exitUpload(404, " No disk space available");
         return;
      }
	  
     
         /**
          * Non-Site mode.
          * Need valid destination nodeRef.
          */
    	  destNode = search.findNode("path", ["workspace", "SpacesStore", "Company Home", "Shared", "Workflow", "Uploads"]);
          var tplNode = search.findNode("path", ["workspace", "SpacesStore", "Company Home", "Data Dictionary", "Space Templates", "temp-upload"]);
    	 
         if (destNode === null)
         {
            exitUpload(404, "Destination (" + destination + ") not found.");
            return;
         }
         if (tplNode===null) {
        	 exitUpload(404, "Template Node does not exist");
         }
         
         destNode=tplNode.copy(destNode);
         destNode.name=generateUniqueName();
         

     
         /**
          * Create a new file.
          */
         var newFile;
         if (contentType !== null)
         {
            newFile = destNode.createFile(filename, contentType);
         }
         else
         {
            newFile = destNode.createFile(filename);
         }
         // Use the appropriate write() method so that the mimetype already guessed from the original filename is
         // maintained - as upload may have been via Flash - which always sends binary mimetype and would overwrite it.
         // Also perform the encoding guess step in the write() method to save an additional Writer operation.
         newFile.properties.content.write(content, false, true);
         newFile.save();
         
         // TODO (THOR-175) - review
         // Ensure the file is versionable (autoVersion = true, autoVersionProps = false)
         newFile.ensureVersioningEnabled(true, false);

         // NOTE: Removal of first request for thumbnails to improve upload performance
         //       Thumbnails are still requested by Share on first render of the doclist image.

         // Additional aspects?
         if (aspects.length > 0)
         {
            for (i = 0; i < aspects.length; i++)
            {
               newFile.addAspect(aspects[i]);
            }
         }

         // Extract the metadata
         extractMetadata(newFile);

         // Record the file details ready for generating the response
         model.document = newFile;
      
      // MNT-8745 fix: Do not clean formdata temp files to allow for retries. Temp files will be deleted later when GC call DiskFileItem#finalize() method or by temp file cleaner.
   }
   catch (e)
   {
      // NOTE: Do not clean formdata temp files to allow for retries. It's possible for a temp file
      //       to remain if max retry attempts are made, but this is rare, so leave to usual temp
      //       file cleanup.
      
      // capture exception, annotate it accordingly and re-throw
      if (e.message && e.message.indexOf("AccessDeniedException") != -1)
      {
         e.code = 403;
      }
      else if (e.message && e.message.indexOf("org.alfresco.service.cmr.usage.ContentQuotaException") == 0)
      {
         e.code = 413;
      }
      else if (e.message && e.message.indexOf("org.alfresco.repo.content.ContentLimitViolationException") == 0)
      {
         e.code = 409;
      }
      else
      {
         e.code = 500;
         e.message = "Unexpected error occurred during upload of new content.";    
         
      }
      throw e;
   }
}

main();
