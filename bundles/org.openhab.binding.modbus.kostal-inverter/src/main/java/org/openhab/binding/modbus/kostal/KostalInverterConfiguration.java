/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.modbus.kostal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link KostalInverterConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Andreas Lanz - Initial contribution
 */
@NonNullByDefault
public class KostalInverterConfiguration {

    /**
     * Refresh interval in milliseconds
     */
    public int pollInterval;

    /**
     * Maximum number of tries, for reading data
     */
    public int maxTries = 3;

    public boolean littleEndian = true;
}
