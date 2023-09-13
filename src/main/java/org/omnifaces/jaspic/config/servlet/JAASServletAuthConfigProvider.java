/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.omnifaces.jaspic.config.servlet;

import java.util.Map;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.omnifaces.jaspic.config.delegate.MessagePolicyDelegate;
import org.omnifaces.jaspic.config.helper.AuthContextHelper;
import org.omnifaces.jaspic.config.jaas.JAASAuthConfigProvider;

/**
 *
 * @author Ron Monzillo
 */
public class JAASServletAuthConfigProvider extends JAASAuthConfigProvider {

    private static final String HTTP_SERVLET_LAYER = "HttpServlet";
    private static final String MANDATORY_KEY = "javax.security.auth.message.MessagePolicy.isMandatory";
    private static final String MANDATORY_AUTH_CONTEXT_ID = "mandatory";
    private static final String OPTIONAL_AUTH_CONTEXT_ID = "optional";
    private static final Class[] moduleTypes = new Class[] { ServerAuthModule.class };
    private static final Class[] messageTypes = new Class[] { HttpServletRequest.class, HttpServletResponse.class };
    final static MessagePolicy mandatoryPolicy = new MessagePolicy(
            new MessagePolicy.TargetPolicy[] { new MessagePolicy.TargetPolicy((MessagePolicy.Target[]) null, new MessagePolicy.ProtectionPolicy() {

                @Override
                public String getID() {
                    return MessagePolicy.ProtectionPolicy.AUTHENTICATE_SENDER;
                }
            }) }, true);
    final static MessagePolicy optionalPolicy = new MessagePolicy(
            new MessagePolicy.TargetPolicy[] { new MessagePolicy.TargetPolicy((MessagePolicy.Target[]) null, new MessagePolicy.ProtectionPolicy() {

                @Override
                public String getID() {
                    return MessagePolicy.ProtectionPolicy.AUTHENTICATE_SENDER;
                }
            }) }, false);

    public JAASServletAuthConfigProvider(Map properties, AuthConfigFactory factory) {
        super(properties, factory);
    }

    @Override
    public MessagePolicyDelegate getMessagePolicyDelegate(String appContext) throws AuthException {

        return new MessagePolicyDelegate() {

            @Override
            public MessagePolicy getRequestPolicy(String authContextID, Map properties) {
                MessagePolicy rvalue;
                if (MANDATORY_AUTH_CONTEXT_ID.equals(authContextID)) {
                    rvalue = mandatoryPolicy;
                } else {
                    rvalue = optionalPolicy;
                }
                return rvalue;
            }

            @Override
            public MessagePolicy getResponsePolicy(String authContextID, Map properties) {
                return null;
            }

            @Override
            public Class[] getMessageTypes() {
                return messageTypes;
            }

            @Override
            public String getAuthContextID(MessageInfo messageInfo) {
                String rvalue;
                if (messageInfo.getMap().containsKey(MANDATORY_KEY)) {
                    rvalue = MANDATORY_AUTH_CONTEXT_ID;
                } else {
                    rvalue = OPTIONAL_AUTH_CONTEXT_ID;
                }
                return rvalue;
            }

            @Override
            public boolean isProtected() {
                return true;
            }

        };
    }

    @Override
    protected Class[] getModuleTypes() {
        return moduleTypes;
    }

    @Override
    protected String getLayer() {
        return HTTP_SERVLET_LAYER;
    }

    @Override
    public AuthContextHelper getAuthContextHelper(String appContext, boolean returnNullContexts) throws AuthException {
        // overrides returnNullContexts to false (as required by Servlet Profile)
        return super.getAuthContextHelper(appContext, false);
    }
}