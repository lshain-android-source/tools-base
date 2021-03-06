/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.sdklib.internal.repository.updater;

import com.android.sdklib.SdkManager;
import com.android.sdklib.internal.avd.AvdManager;
import com.android.sdklib.internal.repository.DownloadCache;
import com.android.sdklib.internal.repository.ITaskFactory;
import com.android.utils.ILogger;


/**
 * Interface used to retrieve some parameters from an {@link UpdaterData} instance.
 * Useful mostly for unit tests purposes.
 */
public interface IUpdaterData {

    public abstract ITaskFactory getTaskFactory();

    public abstract ILogger getSdkLog();

    public abstract DownloadCache getDownloadCache();

    public abstract SdkManager getSdkManager();

    public abstract AvdManager getAvdManager();

    public abstract SettingsController getSettingsController();

}
