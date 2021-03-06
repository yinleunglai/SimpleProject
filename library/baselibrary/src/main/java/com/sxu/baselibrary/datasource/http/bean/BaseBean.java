package com.sxu.baselibrary.datasource.http.bean;

import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.reflect.Type;

/*******************************************************************************
 * Description: APP相关的信息
 *
 * Author: Freeman
 *
 * Date: 2017/6/21
 *
 * Copyright: all rights reserved by Freeman.
 *******************************************************************************/
public abstract class BaseBean implements Serializable {

	public String toJson() {
		return toJson(this);
	}

	public static String toJson(BaseBean bean) {
		if (bean != null) {
			return new Gson().toJson(bean);
		}

		return null;
	}

	public static <T extends BaseBean> T fromJson(String jsonStr, Type subClass) {
		try {
			BaseBean newObj = new Gson().fromJson(jsonStr,
					subClass);
			return (T) newObj;
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

		return null;
	}

	@Override
	public String toString() {
		return toJson();
	}
}
