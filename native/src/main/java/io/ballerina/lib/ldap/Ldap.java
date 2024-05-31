/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.lib.ldap;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.Modification;
import com.unboundid.ldap.sdk.ModificationType;
import com.unboundid.ldap.sdk.ModifyRequest;
import com.unboundid.ldap.sdk.SearchResultEntry;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.ValueUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;

import java.util.ArrayList;
import java.util.List;

public class Ldap {
    private Ldap() {
    }

    public static void generateLdapClient(BObject ldapClient, BMap<BString, Object> config) {
        String hostName = ((BString) config.get(ModuleUtils.HOST_NAME)).getValue();
        int port = Math.toIntExact(config.getIntValue(ModuleUtils.PORT));
        String domainName = ((BString) config.get(ModuleUtils.DOMAIN_NAME)).getValue();
        String password = ((BString) config.get(ModuleUtils.PASSWORD)).getValue();
        try {
            LDAPConnection ldapConnection = new LDAPConnection(hostName, port, domainName, password);
            ldapClient.addNativeData(ModuleUtils.NATIVE_CLIENT, ldapConnection);
        } catch (LDAPException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object modify(BObject ldapClient, BString distinguishedName, BMap<BString, BString> user) {
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(ModuleUtils.NATIVE_CLIENT);
            List<Modification> modificationList = new ArrayList<>();
            for (BString key: user.getKeys()) {
                modificationList
                        .add(new Modification(ModificationType.REPLACE, key.getValue(), user.get(key).getValue()));
            }
            ModifyRequest modifyRequest = new ModifyRequest(distinguishedName.getValue(), modificationList);
            ldapConnection.modify(modifyRequest);
            return null;
        } catch (LDAPException e) {
            return Utils.createError(e.getMessage(), e);
        }
    }

    public static Object getEntry(BObject ldapClient, BString distinguishedName, BTypedesc typeParam) {
        BMap entry = ValueCreator.createMapValue();
        try {
            LDAPConnection ldapConnection = (LDAPConnection) ldapClient.getNativeData(ModuleUtils.NATIVE_CLIENT);
            SearchResultEntry userEntry = ldapConnection.getEntry(distinguishedName.getValue());
            for (Attribute attribute: userEntry.getAttributes()) {
                entry.put(StringUtils.fromString(attribute.getName()), StringUtils.fromString(attribute.getValue()));
            }
            return ValueUtils.convert(entry, typeParam.getDescribingType());
        } catch (LDAPException e) {
            return Utils.createError(e.getMessage(), e);
        }
    }
}