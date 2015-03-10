/*
 * Copyright 2014 Antonello Genuario
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

package it.personal.keyring.common;

import it.personal.keyring.model.PersonalKey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;
import org.xmlpull.v1.XmlSerializer;

import android.text.TextUtils;
import android.util.Xml;

public class KeyXmlParser {
 
    private String encryptPassword;
    private boolean enableEncrypting;
    public KeyXmlParser() {
    	encryptPassword = null;
    	enableEncrypting = false;
    }
    public String getEncryptPassword() {
		return encryptPassword;
	}

	public void setEncryptPassword(String encryptPassword) {
		this.encryptPassword = encryptPassword;
		if(encryptPassword != null && !TextUtils.isEmpty(encryptPassword)) {
			enableEncrypting = true;
		} else {
			enableEncrypting = false;
		}
	}

	protected InputStream getInputStream(String fileName) {
        try {
            return new FileInputStream(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // names of the XML tags
	static final String KEYS_NODE = "keys";
	static final String VERSION_ATTR = "version";
	static final String ALGORITHM_ATTR = "algorithm";
	static final String KEY = "key";
    static final String KEY_NAME = "keyName";
    static final  String USERNAME = "username";
    static final  String PASSWORD = "password";
    static final  String NOTES = "notes";

	public void export(List<PersonalKey> items, String fileName) {
        XmlSerializer serializer = Xml.newSerializer();
        try {
	        // create a file on the sdcard to export the
			// database contents to
			File myFile = new File( fileName );
			myFile.createNewFile();
			
			FileOutputStream fOut =  new FileOutputStream(myFile);

            serializer.setOutput(fOut, "UTF-8");
            serializer.startDocument("UTF-8", true);
            serializer.startTag("", KEYS_NODE);
        	serializer.attribute("", VERSION_ATTR, "2.5");
            if(enableEncrypting) {
            	serializer.attribute("", ALGORITHM_ATTR, "AES");
            }
            

            for(PersonalKey item : items) {
                serializer.startTag("", KEY);
                
				serializer.attribute("", KEY_NAME, item.Name);
				serializer.attribute("", USERNAME, item.Username);
				if(enableEncrypting) {
					serializer.attribute("", PASSWORD, SimpleCrypto.encrypt(encryptPassword, item.Password));
				} else {
					serializer.attribute("", PASSWORD, item.Password);
				}
				serializer.attribute("", NOTES, item.Notes);
                serializer.endTag("", KEY);
            }
            serializer.endTag("", KEYS_NODE);
            serializer.endDocument();
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
		
	}	

    public List<PersonalKey> parse(String fileName) throws PasswordProtectedException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        List<PersonalKey> keys = new ArrayList<PersonalKey>();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse(this.getInputStream(fileName));
            Element root = dom.getDocumentElement();
            
        	boolean shouldDecrypt = (root.hasAttribute(ALGORITHM_ATTR));
    		if(shouldDecrypt && !enableEncrypting) {
    			throw new PasswordProtectedException("The selected file is password protected, please supply a valid password");
    		}
            NodeList items = root.getElementsByTagName(KEY);
            for (int i=0;i<items.getLength();i++){
                PersonalKey key = new PersonalKey();
                Node item = items.item(i);
                NamedNodeMap properties = item.getAttributes();
                for (int j=0;j<properties.getLength();j++){
                    Node property = properties.item(j);
                    String name = property.getNodeName();
                    if (name.equalsIgnoreCase(KEY_NAME)){
                        key.Name = property.getNodeValue();
                    } else if (name.equalsIgnoreCase(USERNAME)){
                        key.Username = property.getNodeValue();
                    } else if (name.equalsIgnoreCase(PASSWORD)){
                    	String pwd;
                    	if(shouldDecrypt) {
                    		pwd = SimpleCrypto.decrypt(encryptPassword, property.getNodeValue());
                    	} else {
                    		pwd = property.getNodeValue();
                    	}
                    	key.Password = pwd;
                    } else if (name.equalsIgnoreCase(NOTES)){
                        key.Notes = property.getNodeValue();
                    }
                }
                keys.add(key);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
        return keys;
    }
	public boolean isProtected(String fileName) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
	        DocumentBuilder builder = factory.newDocumentBuilder();
	        Document dom = builder.parse(this.getInputStream(fileName));
	        Element root = dom.getDocumentElement();
        
    		return (root.hasAttribute(ALGORITHM_ATTR));
        } catch (Exception e) {
        	return false;
        }
	}
}
