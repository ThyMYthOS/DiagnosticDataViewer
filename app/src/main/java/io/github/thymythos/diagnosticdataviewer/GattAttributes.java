/*
 * Copyright (C) 2013 The Android Open Source Project
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

package io.github.thymythos.diagnosticdataviewer;

import java.util.HashMap;

/**
 * This class includes the GATT attributes.
 */
public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap<>();
    public static String DataLoggerService = "0f6ee9d0-c7fd-11e7-abc4-cec278b6b50a";
    public static String DataCharacteristic = "5cd84f08-c89e-11e7-abc4-cec278b6b50a";

    static {
        // Services
        attributes.put(DataLoggerService, "DataLogger Service");
        // Characteristics
        attributes.put(DataCharacteristic, "DataLogger");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
