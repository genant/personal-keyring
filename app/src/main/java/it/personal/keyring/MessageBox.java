/*
 * Copyright (C) 2014 Antonello Genuario
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.personal.keyring;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class MessageBox {
	public static void show(Context context, int titleId, int messageId)
	{
		show(context, context.getString(titleId), context.getString(messageId));
	}
	
	public static void show(Context context, String title, String message)
	{
	    AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);                      
	    dlgAlert.setTitle(title); 
	    dlgAlert.setMessage(message); 
	    dlgAlert.setPositiveButton("OK",new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	             dialog.dismiss();
	        }
	   });
	    dlgAlert.setCancelable(true);
	    dlgAlert.create().show();
	}
}
