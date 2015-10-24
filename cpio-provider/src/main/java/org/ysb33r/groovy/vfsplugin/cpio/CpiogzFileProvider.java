/**
 * ============================================================================
 * (C) Copyright Schalk W. Cronje 2013-2015
 *
 * This software is licensed under the Apache License 2.0
 * See http://www.apache.org/licenses/LICENSE-2.0 for license details
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 *
 * ============================================================================
 */

// ----------------------------------------------------------------------------
//  Much of this code is a modficiation or a port of original Java code from the
//  Apache Commons VFS project and therefore the following license may still apply
// ----------------------------------------------------------------------------
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ysb33r.groovy.vfsplugin.cpio;

import java.util.Collection;

import org.apache.commons.vfs2.Capability;
import org.apache.commons.vfs2.provider.CompositeFileProvider;

/**
 * A file system provider for Tar files.  Provides read-only file systems.
 */
public class CpioGzFileProvider extends CompositeFileProvider
{
    /** The provider's capabilities */
    protected static final Collection<Capability> capabilities = CpioFileProvider.capabilities;

    private static final String[] SCHEMES = new String[]
    {
        "gz",
        "cpio"
    };

    public CpioGzFileProvider()
    {
        super();
    }

    @Override
    protected String[] getSchemes()
    {
        return SCHEMES;
    }

    @Override
    public Collection<Capability> getCapabilities()
    {
        return capabilities;
    }
}
