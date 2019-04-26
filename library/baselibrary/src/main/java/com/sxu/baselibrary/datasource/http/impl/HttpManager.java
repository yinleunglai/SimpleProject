package com.sxu.baselibrary.datasource.http.impl;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sxu.baselibrary.commonutils.BaseContentProvider;
import com.sxu.baselibrary.datasource.http.bean.ResponseBean;
import com.sxu.baselibrary.datasource.http.impl.interceptor.BaseInterceptor;
import com.sxu.baselibrary.datasource.http.impl.interceptor.CookieInterceptor;
import com.sxu.baselibrary.datasource.http.impl.interceptor.HttpCacheInterceptor;

import java.util.List;
import java.util.WeakHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import okhttp3.Cache;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/******************************************************************************
 * Description: 网络组件的封装
 *
 * Author: Freeman
 *
 * Date: 2018/1/16
 *
 * Copyright: all rights reserved by Freeman.
 *******************************************************************************/
public class HttpManager {

	private static String mBaseUrl;
	private static Context mContext;
	private Retrofit retrofit;
	private WeakHashMap<Integer, Subscription> subscriptionHashMap = new WeakHashMap<>();

	public static void init(String baseUrl) {
		mContext = BaseContentProvider.context;
		mBaseUrl = baseUrl;
	}

	public static void init(Context context, String baseUrl) {
		mContext = context.getApplicationContext();
		mBaseUrl = baseUrl;
	}

	private HttpManager() {
		OkHttpClient httpClient = new OkHttpClient.Builder()
				.addInterceptor(new BaseInterceptor(mContext))
				.addInterceptor(new CookieInterceptor())
				.addNetworkInterceptor(new HttpCacheInterceptor(mContext))
				.cache(new Cache(mContext.getCacheDir(), 20 * 1024 * 1024))
				.cookieJar(new CookieJar() {
					@Override
					public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {

					}

					@Override
					public List<Cookie> loadForRequest(HttpUrl url) {
						return null;
					}
				})
				.hostnameVerifier(new HostnameVerifier() {
					@Override
					public boolean verify(String hostname, SSLSession session) {
						if (!TextUtils.isEmpty(mBaseUrl) && mBaseUrl.contains(hostname)) {
							return true;
						} else {
							HostnameVerifier hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
							return hostnameVerifier.verify(hostname, session);
						}
					}
				})
				.build();
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(Integer.class, new WrapTypeAdapters.IntegerAdapter())
				.registerTypeAdapter(String.class, new WrapTypeAdapters.StringAdapter())
				.serializeNulls()
				.create();
		retrofit = new Retrofit.Builder()
			.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
			.addConverterFactory(GsonConverterFactory.create(gson))
			.client(httpClient)
			.baseUrl(mBaseUrl)
			.build();
	}

	public <T> T getApiService(Class<T> classT) {
		return retrofit.create(classT);
	}

	/**
	 * 执行网络请求
	 * @param observable
	 * @param listener
	 * @param <T>
	 */
	public <T> void executeRequest(final rx.Observable<ResponseBean<T>> observable,
	                               final RequestListener listener) {
		if (observable == null) {
			throw new IllegalArgumentException("observable can't be null");
		}

		final Subscription subscription = observable.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new Subscriber<ResponseBean>() {
					@Override
					public void onCompleted() {
						Subscription subscript = subscriptionHashMap.get(observable.hashCode());
						if (subscript != null) {
							if (subscript.isUnsubscribed()) {
								subscript.unsubscribe();
							}
						}
						subscriptionHashMap.remove(observable.hashCode());
					}

					@Override
					public void onError(Throwable e) {
						ResponseProcessor.process(null, listener);
						Subscription subscript = subscriptionHashMap.get(observable.hashCode());
						if (subscript != null) {
							if (subscript.isUnsubscribed()) {
								subscript.unsubscribe();
							}
						}
						subscriptionHashMap.remove(observable.hashCode());
					}

					@Override
					public void onNext(ResponseBean result) {
						ResponseProcessor.process(result, listener);
					}
				});
		subscriptionHashMap.put(observable.hashCode(), subscription);
	}

	public static HttpManager getInstance() {
		return SingletonHolder.INSTANCE;
	}

	public static class SingletonHolder {
		public final static HttpManager INSTANCE = new HttpManager();
	}
}
