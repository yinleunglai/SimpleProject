package com.sxu.basecomponent.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/*******************************************************************************
 * Description: 不需要网络请求的Fragment
 *
 * Author: Freeman
 *
 * Date: 2018/7/27
 *
 * Copyright: all rights reserved by Freeman.
 *******************************************************************************/
public abstract class BaseFragment extends CommonFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getLayoutResId() != 0) {
            contentView = inflater.inflate(getLayoutResId(), null);
            contentView.setClickable(true);
            getViews();
            initFragment();
        }

        return contentView;
    }
}
