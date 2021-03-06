/*******************************************************************************
 * Copyright (c) 2013 hangum.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     hangum - initial API and implementation
 ******************************************************************************/
package com.hangum.tadpole.importdb.core.dialog.importdb.mongodb;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.Job;

import com.hangum.tadpole.dao.system.UserDBDAO;

/**
 * ? to mongodb database import
 * 
 * @author hangum
 *
 */
public abstract class DBImport {
	
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(DBImport.class);
	
	protected UserDBDAO importUserDB;
	protected UserDBDAO exportUserDB;
	
	public DBImport(UserDBDAO importUserDB, UserDBDAO exportUserDB) {
		this.importUserDB = importUserDB;
		this.exportUserDB = exportUserDB;
	}

	public abstract Job workTableImport();
}
