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
package com.hangum.tadpole.session.manager;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.rap.rwt.RWT;

import com.hangum.tadpold.commons.libs.core.define.PublicTadpoleDefine;
import com.hangum.tadpole.dao.system.UserDAO;
import com.hangum.tadpole.dao.system.UserInfoDataDAO;
import com.hangum.tadpole.dao.system.UserRoleDAO;
import com.hangum.tadpole.system.TadpoleSystem_UserInfoData;
import com.hangum.tadpole.system.TadpoleSystem_UserRole;

/**
 * tadpole의 session manager입니다
 * 
 * @author hangum
 *
 */
public class SessionManager {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(SessionManager.class);

	/**
	 * <pre>
	 * 		MANAGER_SEQ는 그룹의 manager 권한 사용자의 seq 입니다.  seq로  그룹의 db list를 얻기위해 미리 가져옵니다.
	 * </pre>
	 * 
	 * @author hangum
	 */
	public static enum SESSEION_NAME {	/* 자신의 메니저 그룹 */		GROUP_SEQ, 
										/* 자신이 속한 그룹종류 */	GROUP_SEQS, 
										/* 자신의 유저 seq */		USER_SEQ, 
																LOGIN_EMAIL, 
																LOGIN_PASSWORD, 
																LOGIN_NAME, 
										/* 대표적인 권한 타입 */		REPRESENT_ROLE_TYPE, 
										/* 자신의 모든 롤 타입 */	ROLE_TYPE, 
																USER_INFO_DATA}
	
	/**
	 * 사용자를 session에 등록
	 * 
	 * @param loginUserDao
	 */
	public static void addSession(UserDAO loginUserDao) {
		HttpSession sStore = RWT.getRequest().getSession();
		
//		 user의 대표 role과 전체 role을 세션에 저장합니다. 
//		 이것은 user role이 manager일 경우만 디비의 등록 추가 수정이 가능하여 자신이 속한 그룹의 role을 찾는 것입니다.
		String strRepresentRole 	= PublicTadpoleDefine.USER_TYPE.USER.toString();
		String tmpStrRepAdminRole 	= "";
		String tmpStrRepManagerRole = "";
		String tmpStrRepDBARole 	= "";
		String tmpStrRepUserRole 	= "";
		
		// 내가 속한 모든 그룹 순번이고, 이것은 사용할수 있는 디비를 조회하는 용도로 사용하기 위해 세션에 입력합니다.
		String strGroupSeqs = "";
		
		try {
			Map<Integer, String> mapUserRole = new HashMap<Integer, String>();
			for (UserRoleDAO userRoleDAO : TadpoleSystem_UserRole.findUserRole(loginUserDao)) {
				mapUserRole.put(userRoleDAO.getGroup_seq(), userRoleDAO.getRole_type());
				
				if(PublicTadpoleDefine.USER_TYPE.ADMIN.toString().equals(userRoleDAO.getRole_type())) {
					tmpStrRepAdminRole = PublicTadpoleDefine.USER_TYPE.ADMIN.toString();
				} else if(PublicTadpoleDefine.USER_TYPE.MANAGER.toString().equals(userRoleDAO.getRole_type())) {
					tmpStrRepManagerRole = PublicTadpoleDefine.USER_TYPE.MANAGER.toString();
					
					sStore.setAttribute(SESSEION_NAME.GROUP_SEQ.toString(), userRoleDAO.getSeq());
				} else if(PublicTadpoleDefine.USER_TYPE.DBA.toString().equals(userRoleDAO.getRole_type())) {
					tmpStrRepDBARole = PublicTadpoleDefine.USER_TYPE.DBA.toString();
				} else if(PublicTadpoleDefine.USER_TYPE.USER.toString().equals(userRoleDAO.getRole_type())) {
					tmpStrRepUserRole = PublicTadpoleDefine.USER_TYPE.USER.toString();
				}
				
				strGroupSeqs += userRoleDAO.getGroup_seq() + ",";
			}
			strGroupSeqs = StringUtils.removeEnd(strGroupSeqs, ",");
			sStore.setAttribute(SESSEION_NAME.GROUP_SEQS.toString(), strGroupSeqs);
			
			// 대표 role을 찾는다.
			if(!"".equals(tmpStrRepAdminRole)) strRepresentRole = tmpStrRepAdminRole;
			else if(!"".equals(tmpStrRepManagerRole)) strRepresentRole = tmpStrRepManagerRole;
			else if(!"".equals(tmpStrRepDBARole)) strRepresentRole = tmpStrRepDBARole;
			else if(!"".equals(tmpStrRepUserRole)) strRepresentRole = tmpStrRepUserRole;
			
			// session 에 등록.
			sStore.setAttribute(SESSEION_NAME.REPRESENT_ROLE_TYPE.toString(), strRepresentRole);
			sStore.setAttribute(SESSEION_NAME.ROLE_TYPE.toString(), mapUserRole);
			
		} catch(Exception e) {
			logger.error("find user rold", e);
		}
		
//		UserDAO groupManagerUser =  TadpoleSystem_UserQuery.getGroupManager(loginUserDao.getGroup_seq());
//		String groupName = "";
//		try {
//			groupName = TadpoleSystem_UserGroupQuery.findGroupName(groupSeq);
//		} catch(Exception e) {
//			logger.error("Session group name", e);
//		}
		
//		sStore.setAttribute(SESSEION_NAME.GROUP_SEQ.toString(), groupSeq);		
		sStore.setAttribute(SESSEION_NAME.USER_SEQ.toString(), loginUserDao.getSeq());
//		sStore.setAttribute(SESSEION_NAME.GROUP_NAME.toString(), groupName);
		sStore.setAttribute(SESSEION_NAME.LOGIN_EMAIL.toString(), loginUserDao.getEmail());
		sStore.setAttribute(SESSEION_NAME.LOGIN_PASSWORD.toString(), loginUserDao.getPasswd());
		sStore.setAttribute(SESSEION_NAME.LOGIN_NAME.toString(), loginUserDao.getName());
	}
	
	/**
	 * 사용자 그룹 seqs를 보내줍니다.
	 * @return
	 */
	public static String getGroupSeqs() {
		HttpSession sStore = RWT.getRequest().getSession();
		return (String)sStore.getAttribute(SESSEION_NAME.GROUP_SEQS.toString());
	}
	
	public static int getGroupSeq() {
		HttpSession sStore = RWT.getRequest().getSession();
		return (Integer)sStore.getAttribute(SESSEION_NAME.GROUP_SEQ.toString());
	}
	
	public static int getSeq() {
		HttpSession sStore = RWT.getRequest().getSession();
		Object obj = sStore.getAttribute(SESSEION_NAME.USER_SEQ.toString());
		
		if(obj == null) return 0;
		else return (Integer)obj;
	}
	
//	public static String getGroupName() {
//		HttpSession sStore = RWT.getRequest().getSession();
//		return (String)sStore.getAttribute(SESSEION_NAME.GROUP_NAME.toString());
//	}
	
	public static String getEMAIL() {
		HttpSession sStore = RWT.getRequest().getSession();
		return (String)sStore.getAttribute(SESSEION_NAME.LOGIN_EMAIL.toString());
	}
	
	public static String getPassword() {
		HttpSession sStore = RWT.getRequest().getSession();
		return (String)sStore.getAttribute(SESSEION_NAME.LOGIN_PASSWORD.toString());
	}
	
	public static String getName() {
		HttpSession sStore = RWT.getRequest().getSession();
		return (String)sStore.getAttribute(SESSEION_NAME.LOGIN_NAME.toString());
	}
	
	/**
	 * db에 해당하는 자신의 role을 가지고 옵니다.
	 * 
	 * @param groupSeq
	 * @return
	 */
	public static String getRoleType(int groupSeq) {
		HttpSession sStore = RWT.getRequest().getSession();
		Map<Integer, String> mapUserRole = (Map)sStore.getAttribute(SESSEION_NAME.ROLE_TYPE.toString());
		
		return mapUserRole.get(groupSeq);
	}
	
	/**
	 * 자신이 대표 권한을 리턴합니다.
	 * 
	 * <pre>
	 * 권한 중복일 경우
	 * admin이면서 manager일수는 없습니다. 
	 * 	1) admin
	 *  2) manager 
	 *  3) dba 
	 *  4) user
	 * 
	 * group당 manager권한은 반듯이 하나입니다.
	 * manager권한이 정지되면 그룹을 수정 못하는 것으로.
	 * </pre>
	 * 
	 * @return
	 */
	public static String representRole() {
		HttpSession sStore = RWT.getRequest().getSession();
		
		return (String)sStore.getAttribute(SESSEION_NAME.REPRESENT_ROLE_TYPE.toString());
	}
	
	/**
	 * 사용자의 모든 role type을 리턴합니다.
	 * @return
	 */
	public static Map<Integer, String> getAllRoleType() {
		HttpSession sStore = RWT.getRequest().getSession();
		Map<Integer, String> mapUserRole = (Map)sStore.getAttribute(SESSEION_NAME.ROLE_TYPE.toString());
		
		return mapUserRole;
	}
	
//	public static int getManagerSeq() {
//		HttpSession sStore = RWT.getRequest().getSession();
//		return (Integer)sStore.getAttribute(SESSEION_NAME.MANAGER_SEQ.toString());
//	}

	/**
	 * 초기 접속시 프리퍼런스 정보를 로드합니다.
	 */
	public static void setUserInfos(Map<String, Object> mapUserInfo) {
		HttpSession sStore = RWT.getRequest().getSession();
		sStore.setAttribute(SESSEION_NAME.USER_INFO_DATA.toString(), mapUserInfo);		
	}
	
	/**
	 * 기존 세션 정보를 추가합니다. 
	 * @param key
	 * @param obj
	 */
	public static void setUserInfo(String key, String obj) {
		HttpSession sStore = RWT.getRequest().getSession();
		Map<String, Object> mapUserInfoData = (Map<String, Object>)sStore.getAttribute(SESSEION_NAME.USER_INFO_DATA.toString());
		UserInfoDataDAO userInfoDataDAO = (UserInfoDataDAO)mapUserInfoData.get(key);
		if(userInfoDataDAO == null) {
			userInfoDataDAO = new UserInfoDataDAO();
			userInfoDataDAO.setName(key);
			userInfoDataDAO.setUser_seq(SessionManager.getSeq());
			userInfoDataDAO.setValue0(obj);
		
			try {
				TadpoleSystem_UserInfoData.insertUserInfoData(userInfoDataDAO);
			} catch(Exception e) {
				logger.error("User data save exception [key]" + key + "[value]" + obj, e);
			}
		} else {
			userInfoDataDAO.setValue0(obj);
		}
			
		mapUserInfoData.put(key, userInfoDataDAO);
		
		sStore.setAttribute(SESSEION_NAME.USER_INFO_DATA.toString(), mapUserInfoData);
	}
	
	/**
	 * 사용자 User 정보 .
	 * 
	 * @param key
	 * @return
	 */
	public static UserInfoDataDAO getUserInfo(String key) {
		HttpSession sStore = RWT.getRequest().getSession();
		Map<String, Object> mapUserInfoData = (Map<String, Object>)sStore.getAttribute(SESSEION_NAME.USER_INFO_DATA.toString());
		
		return (UserInfoDataDAO)mapUserInfoData.get(key);
	}
	
	/**
	 * logout 처리를 합니다.
	 */
	public static void logout() {
		try {
			HttpSession sStore = RWT.getRequest().getSession();
			sStore.invalidate();
		} catch(Exception e) {
			// ignor exception
		}
	}
}
