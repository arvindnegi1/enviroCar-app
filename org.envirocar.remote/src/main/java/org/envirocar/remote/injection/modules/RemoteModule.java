/**
 * Copyright (C) 2013 - 2019 the enviroCar community
 *
 * This file is part of the enviroCar app.
 *
 * The enviroCar app is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The enviroCar app is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with the enviroCar app. If not, see http://www.gnu.org/licenses/.
 */
package org.envirocar.remote.injection.modules;

import android.content.Context;

import org.envirocar.core.ContextInternetAccessProvider;
import org.envirocar.core.InternetAccessProvider;
import org.envirocar.core.injection.InjectApplicationScope;
import org.envirocar.remote.service.EnviroCarService;
import org.envirocar.remote.util.AuthenticationInterceptor;
import org.envirocar.remote.util.JsonContentTypeInterceptor;
import org.envirocar.remote.util.LanguageHeaderInterceptor;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

/**
 * TODO JavaDoc
 *
 * @author dewall
 */
@Module(includes = {RetrofitModule.class, SerializationModule.class})
public class RemoteModule {
    public static HttpUrl URL_ENVIROCAR_BASE = HttpUrl.parse(EnviroCarService.BASE_URL);
    public static HttpUrl URL_ENVIROCAR_BASE_NEW = HttpUrl.parse(EnviroCarService.BASE_URL_NEW);

    /**
     * Provides the InternetAccessProivder.
     *
     * @return the provider for internet access.
     */
    @Provides
    @Singleton
    public InternetAccessProvider provideInternetAccessProvider(
            @InjectApplicationScope Context context) {
        return new ContextInternetAccessProvider(context);
    }

    @Provides
    @Singleton
    protected HttpUrl provideBaseUrl() {
        return URL_ENVIROCAR_BASE;
    }

    @Provides
    @Singleton
    protected OkHttpClient provideOkHttpClient(AuthenticationInterceptor authInterceptor,
                                               JsonContentTypeInterceptor jsonInterceptor,
                                               LanguageHeaderInterceptor languageInterceptor) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.SECONDS)  // connect timeout
                .writeTimeout(300, TimeUnit.SECONDS)    // write timeout
                .readTimeout(300, TimeUnit.SECONDS)     // socket timeout
                .addInterceptor(authInterceptor)
                .addInterceptor(jsonInterceptor)
                .addInterceptor(languageInterceptor)
                .build();
        return client;
    }

}
