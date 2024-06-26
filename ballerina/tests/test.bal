// Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com)
//
// WSO2 LLC. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/test;

configurable string hostName = ?;
configurable int port = ?;
configurable string domainName = ?;
configurable string password = ?;
configurable string userDN = ?;

Client ldapClient = check new ({
    hostName: hostName,
    port: port,
    domainName: domainName,
    password: password
});

@test:Config {}
public function testUpdateUser() returns error? {
    record {} user = {
        "sn": "User",
        "givenName": "Updated User",
        "displayName": "Updated User"
    };
    LDAPResponse val = check ldapClient->modify(userDN, user);
    test:assertEquals(val.resultStatus, SUCCESS);
}

@test:Config {
    dependsOn: [testUpdateUser]
}
public function testGetUser() returns error? {
    UserConfig value = check ldapClient->getEntry(userDN);
    test:assertEquals(value?.givenName, "Updated User");
}

@test:Config {}
public function testInvalidClient() returns error? {
    Client|Error ldapClient = new ({
        hostName: "111.111.11.111",
        port: port,
        domainName: domainName,
        password: password
    });
    test:assertTrue(ldapClient is Error);
}

@test:Config {}
public function testInvalidDomainInClient() returns error? {
    Client|Error ldapClient = new ({
        hostName: hostName,
        port: port,
        domainName: "invalid@ad.invalid",
        password: password
    });
    test:assertTrue(ldapClient is Error);
}

@test:Config {}
public function testGetInvalidUser() returns error? {
    UserConfig|Error value = ldapClient->getEntry("CN=Invalid User,OU=People,DC=ad,DC=windows");
    test:assertTrue(value is Error);
}

@test:Config {}
public function testUpdateUserWithNullValues() returns error? {
    string distinguishedName = "CN=John Doe,OU=People,DC=ad,DC=windows";
    record {} user = {
        "employeeId":"30896","givenName":"John","sn":"Doe","company":"Grocery Co. USA","co":null,"streetAddress":null,"mobile":null,"displayName":"John Doe","middleName":null,"mail":null,"l":null,"telephoneNumber":null,"department":"Produce","st":null,"title":"Clerk"
    };
    LDAPResponse val = check ldapClient->modify(distinguishedName, user);
    test:assertEquals(val.resultStatus, SUCCESS);
}
