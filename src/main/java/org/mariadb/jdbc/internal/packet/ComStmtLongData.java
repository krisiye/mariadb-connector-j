/*
MariaDB Client for Java

Copyright (c) 2012-2014 Monty Program Ab.
Copyright (c) 2015-2016 MariaDB Ab.

This library is free software; you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the Free
Software Foundation; either version 2.1 of the License, or (at your option)
any later version.

This library is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
for more details.

You should have received a copy of the GNU Lesser General Public License along
with this library; if not, write to Monty Program Ab info@montyprogram.com.

This particular MariaDB Client for Java file is work
derived from a Drizzle-JDBC. Drizzle-JDBC file which is covered by subject to
the following copyright and notice provisions:

Copyright (c) 2009-2011, Marcus Eriksson

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:
Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of the driver nor the names of its contributors may not be
used to endorse or promote products derived from this software without specific
prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS  AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
OF SUCH DAMAGE.
*/

package org.mariadb.jdbc.internal.packet;

import org.mariadb.jdbc.internal.packet.dao.parameters.ParameterHolder;
import org.mariadb.jdbc.internal.stream.PacketOutputStream;
import org.mariadb.jdbc.internal.util.BulkStatus;

import java.io.IOException;


public class ComStmtLongData {

    public ComStmtLongData() { }

    /**
     * Send long data packet.
     *
     * @param writer output stream
     * @param statementId statement id
     * @param parameterId parameter id
     * @param param parameter
     * @throws IOException if socket orror occur
     */
    public void send(PacketOutputStream writer, int statementId, short parameterId, ParameterHolder param) throws IOException {
        writer.startPacket(0);
        writer.buffer.put(Packet.COM_STMT_SEND_LONG_DATA);
        writer.buffer.putInt(statementId);
        writer.buffer.putShort(parameterId);
        param.writeBinary(writer);
        writer.finishPacketWithoutRelease();
    }

    /**
     * Send COM_MULTI long data sub-command .
     *
     * @param writer output stream
     * @param statementId statement id
     * @param parameterId parameter id
     * @param param parameter
     * @param status bulk status
     * @return current buffer position
     * @throws IOException if socket orror occur
     */
    public static int sendComMulti(PacketOutputStream writer, int statementId, short parameterId, ParameterHolder param, BulkStatus status)
            throws IOException {
        status.subCmdInitialPosition = writer.buffer.position();
        writer.assureBufferCapacity(3);
        writer.buffer.position(status.subCmdInitialPosition + 3);

        //add execute sub command
        writer.buffer.put(Packet.COM_STMT_SEND_LONG_DATA);
        writer.buffer.putInt(statementId);
        writer.buffer.putShort(parameterId);
        param.writeBinary(writer);

        //write subCommand length
        int subCmdEndPosition = writer.buffer.position();
        writer.buffer.position(status.subCmdInitialPosition);
        writer.buffer.put((byte) ((subCmdEndPosition - (status.subCmdInitialPosition + 3)) & 0xff));
        writer.buffer.put((byte) ((subCmdEndPosition - (status.subCmdInitialPosition + 3)) >>> 8));
        writer.buffer.put((byte) ((subCmdEndPosition - (status.subCmdInitialPosition + 3)) >>> 16));
        writer.buffer.position(subCmdEndPosition);
        return subCmdEndPosition;
    }

}