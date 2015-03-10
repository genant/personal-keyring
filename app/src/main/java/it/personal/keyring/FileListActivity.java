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

import java.io.File;
import java.io.FileFilter;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Stack;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public abstract class FileListActivity extends Activity {
	private File currentFolder;
	private String filter;
	private String actionBarTitle;
	private ListView mFileList;
	private Stack<String> mHistory;
	private ArrayList<HashMap<String, Object>> mList;
	public String getActionBarTitle() {
		return actionBarTitle;
	}
	public void setActionBarTitle(String navBarTitle) {
		this.actionBarTitle = navBarTitle;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		if(filter != null) {
			filter = filter.toLowerCase(Locale.US);
		}
		this.filter = filter;
	}
	public File getCurrentFolder() {
		return currentFolder;
	}
	public void setCurrentFolder(File mCurrentFolder) {
		this.currentFolder = mCurrentFolder;
	}
	public ListView getListView() {
		return mFileList;
	}
	
	
	public void setListView(ListView mFileList) {
		this.mFileList = mFileList;
		this.mFileList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String path = (String) mList.get(position).get("path");
				File selectedFile = new File(path);
				if(OnFileSelectionChanging(selectedFile)) {
					currentFolder = selectedFile;
					if(selectedFile.isDirectory()) {
						mHistory.push(selectedFile.getPath());
						currentFolder = selectedFile;
						fillListView();
					}
				}
			}
		});
		mHistory = new Stack<String>();
		if(currentFolder == null) {
			currentFolder = Environment
					.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		}
		mHistory.push(currentFolder.getPath());

		fillListView();

	}

	abstract boolean OnFileSelectionChanging(File file);
	
	private void fillListView() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            ActionBar actionBar = getActionBar();
            //actionBar.setTitle(actionBarTitle);
            actionBar.setSubtitle(currentFolder.getName());
        }

        if(mList == null) {
        	mList = new ArrayList<HashMap<String,Object>>();
        } else {
        	mList.clear();
        }
        
        File[] items;
        if(filter == null) {
    		items = currentFolder.listFiles();
        } else {
            FileFilter filter = new FileFilter() {
    			
    			@Override
    			public boolean accept(File pathname) {
    				if(pathname.isDirectory()) return true;
    				if(pathname.getName().toLowerCase(Locale.US).endsWith(FileListActivity.this.filter))
    					return true;
    					
    				return false;
    			}
    		};
    		items = currentFolder.listFiles(filter);
        }
		ArrayList<File> files = new ArrayList<File>(); 
		ArrayList<File> folders = new ArrayList<File>(); 

		for (File file : items) {
			if(file.isDirectory())
				folders.add(file);
			else
				files.add(file);
		}

		for (File file : folders) {
			addItem(file);
		}
		for (File file : files) {
			addItem(file);
		}
		
		String[] from = new String[] { "name", "image", "attr", "size" };
		int[] to = new int[] { 
				R.id.open_file_row_name, 
				R.id.open_file_row_icon, 
				R.id.open_file_row_attributes,
				R.id.open_file_row_size,
				};
		
		ListAdapter adapter = mFileList.getAdapter();
		if(adapter == null) {
			adapter = new SimpleAdapter(this, mList,
				R.layout.open_file_row,
				from, to);
			mFileList.setAdapter(adapter);
		} else { 
			((SimpleAdapter)adapter).notifyDataSetChanged();
		}
	}

	private void addItem(File file) {
		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put("name", file.getName());
		String attr = null;
		if(file.isDirectory()) {
			item.put("image", R.drawable.ic_folder);
			attr = SimpleDateFormat.getDateTimeInstance().format(new Date(file.lastModified()));
		} else {
			item.put("image", R.drawable.ic_xml_file);
			attr = SimpleDateFormat.getDateTimeInstance().format(new Date(file.lastModified()));
			item.put("size", GetFileSize(file.length()));
		}
		item.put("attr", attr);
		item.put("path", file.getPath());
		mList.add(item);
	}

	@SuppressLint("DefaultLocale")
	private static String GetFileSize(long length) {
		if(length < 1024) {
			return String.format("%,d B", length);
		}
		return String.format("%,d KB", length / 1024);
	}
	
	public void goBack() {
    	if(!mHistory.empty()) {
    		mHistory.pop();
    	} 
    	
		if(mHistory.isEmpty()) {
			setResult(RESULT_CANCELED, getIntent());
			finish();
		} else {
    		currentFolder = new File(mHistory.peek());
    		fillListView();
		}
    }

}
