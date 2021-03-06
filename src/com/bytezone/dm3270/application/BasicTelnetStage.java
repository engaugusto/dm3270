package com.bytezone.dm3270.application;

import com.bytezone.dm3270.attributes.Attribute;
import com.bytezone.dm3270.commands.Command;
import com.bytezone.dm3270.structuredfields.StructuredField;
import com.bytezone.dm3270.telnet.TelnetCommand;

public class BasicTelnetStage extends BasicStage
{
  boolean direct = true;

  protected byte[] createReadBufferCommand (byte command)
  {
    return direct ? createDirectReadBufferCommand (command)
        : createIndirectReadBufferCommand (command);
  }

  private byte[] createDirectReadBufferCommand (byte command)
  {
    int ptr = 0;
    byte[] buffer = new byte[3];

    buffer[ptr++] = command;

    buffer[ptr++] = TelnetCommand.IAC;
    buffer[ptr++] = TelnetCommand.EOR;

    assert ptr == buffer.length;

    return buffer;
  }

  private byte[] createIndirectReadBufferCommand (byte command)
  {
    int ptr = 0;
    byte[] buffer = new byte[8];

    buffer[ptr++] = Command.WRITE_STRUCTURED_FIELD_F3;

    buffer[ptr++] = 0x00;
    buffer[ptr++] = 0x05;                               // length
    buffer[ptr++] = StructuredField.READ_PARTITION;     // 0x01
    buffer[ptr++] = 0x00;                               // partition 0
    buffer[ptr++] = command;                            // RB-F2/RM-F6/RMA-6E

    buffer[ptr++] = TelnetCommand.IAC;
    buffer[ptr++] = TelnetCommand.EOR;

    assert ptr == buffer.length;

    return buffer;
  }

  protected byte[] createSetReplyModeCommand (byte mode)
  {
    int ptr = 0;
    byte[] buffer = new byte[mode == 2 ? 13 : 8];

    buffer[ptr++] = Command.WRITE_STRUCTURED_FIELD_F3;

    buffer[ptr++] = 0x00;
    buffer[ptr++] = (byte) (mode == 2 ? 0x0A : 0x05);   // length
    buffer[ptr++] = StructuredField.SET_REPLY_MODE;
    buffer[ptr++] = 0x00;                               // partition 0
    buffer[ptr++] = mode;                               // reply mode

    if (mode == 2)
    {
      buffer[ptr++] = Attribute.XA_HIGHLIGHTING;
      buffer[ptr++] = Attribute.XA_FGCOLOR;
      buffer[ptr++] = Attribute.XA_CHARSET;
      buffer[ptr++] = Attribute.XA_BGCOLOR;
      buffer[ptr++] = Attribute.XA_TRANSPARENCY;
    }

    buffer[ptr++] = TelnetCommand.IAC;
    buffer[ptr++] = TelnetCommand.EOR;

    assert ptr == buffer.length;

    return buffer;
  }
}