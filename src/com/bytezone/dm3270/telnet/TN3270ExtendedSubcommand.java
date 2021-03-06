package com.bytezone.dm3270.telnet;

import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.TelnetState;

public class TN3270ExtendedSubcommand extends TelnetSubcommand
{
  protected static final byte EXT_DEVICE_TYPE = 2;
  protected static final byte EXT_FUNCTIONS = 3;

  protected static final byte EXT_IS = 4;
  protected static final byte EXT_REQUEST = 7;
  protected static final byte EXT_SEND = 8;

  private SubType subType;
  private String connect;
  private List<Function> functions;
  private String functionsList = "";

  public enum SubType
  {
    IS, REQUEST, DEVICE_TYPE
  }

  enum Function
  {
    BIND_IMAGE, RESPONSES, SYSREQ
  }

  public TN3270ExtendedSubcommand (byte[] buffer, int offset, int length,
      TelnetState telnetState)
  {
    super (buffer, offset, length, telnetState);

    switch (buffer[3])
    {
      case EXT_SEND:
        type = SubcommandType.SEND;
        if (buffer[4] == EXT_DEVICE_TYPE)
          subType = SubType.DEVICE_TYPE;
        break;

      case EXT_DEVICE_TYPE:
        type = SubcommandType.DEVICE_TYPE;
        if (buffer[4] == EXT_REQUEST)
        {
          subType = SubType.REQUEST;
          value = new String (buffer, 5, length - 7);
        }
        else if (buffer[4] == EXT_IS)
        {
          subType = SubType.IS;
          for (int ptr = 6; ptr < length; ptr++)
          {
            if (buffer[ptr] == 1)
            {
              value = new String (buffer, 5, ptr - 5);    // value before the ptr
              connect = new String (buffer, ptr + 1, length - ptr - 3);   // after
              break;
            }
          }
          if (value == null)            // don't know if this can happen
            value = new String (buffer, 5, length - 5);
        }
        break;

      case EXT_FUNCTIONS:
        type = SubcommandType.FUNCTIONS;
        if (buffer[4] == EXT_REQUEST)
        {
          subType = SubType.REQUEST;
          setFunctions (buffer, length);
        }
        else if (buffer[4] == EXT_IS)
        {
          subType = SubType.IS;
          setFunctions (buffer, length);
        }
        break;

      default:
        throw new InvalidParameterException (String.format ("Unknown Extended: %02X",
                                                            buffer[3]));
    }
  }

  private void setFunctions (byte[] buffer, int length)
  {
    StringBuilder funcList = new StringBuilder ();
    functions = new ArrayList<> ();

    for (int ptr = 5, max = length - 2; ptr < max; ptr++)
    {
      if (buffer[ptr] == 0)
      {
        functions.add (Function.BIND_IMAGE);
        funcList.append ("BIND, ");
      }
      else if (buffer[ptr] == 2)
      {
        functions.add (Function.RESPONSES);
        funcList.append ("RESPONSES, ");
      }
      else if (buffer[ptr] == 4)
      {
        functions.add (Function.SYSREQ);
        funcList.append ("SYSREQ, ");
      }
      else
        throw new InvalidParameterException (String.format ("Unknown function: %02X%n",
                                                            buffer[ptr]));
    }

    if (funcList.length () > 0)
    {
      funcList.deleteCharAt (funcList.length () - 1);
      funcList.deleteCharAt (funcList.length () - 1);
    }
    functionsList = funcList.toString ();
  }

  public SubType getSubtype ()
  {
    return subType;
  }

  @Override
  public void process ()
  {
    //    if (replies.size () > 0)
    //      return;

    if (type == SubcommandType.SEND && subType == SubType.DEVICE_TYPE)
    {
      try
      {
        byte[] header =
            { TelnetCommand.IAC, TelnetCommand.SB, TERMINAL_TYPE, EXT_DEVICE_TYPE,
             EXT_REQUEST };
        byte[] terminal = "IBM-3278-2-E".getBytes ("ASCII");
        byte[] reply = new byte[header.length + terminal.length + 2];

        System.arraycopy (header, 0, reply, 0, header.length);
        System.arraycopy (terminal, 0, reply, header.length, terminal.length);
        reply[reply.length - 2] = TelnetCommand.IAC;
        reply[reply.length - 1] = TelnetCommand.SE;

        this.reply = new TN3270ExtendedSubcommand (reply, 0, reply.length, telnetState);
      }
      catch (UnsupportedEncodingException e)
      {
        e.printStackTrace ();
      }
    }

    if (type == SubcommandType.DEVICE_TYPE && subType == SubType.IS)
    {
      byte[] reply =
          { TelnetCommand.IAC, TelnetCommand.SB, TN3270E, EXT_FUNCTIONS, EXT_REQUEST,
           0x00, 0x02, 0x04, TelnetCommand.IAC, TelnetCommand.SE };
      this.reply = new TN3270ExtendedSubcommand (reply, 0, reply.length, telnetState);
    }
  }

  public boolean doesFunction (Function function)
  {
    return functions.contains (function);
  }

  public String getConnect ()
  {
    return connect;
  }

  @Override
  public String toString ()
  {
    switch (type)
    {
      case SEND:
        return type + " " + subType;
      case FUNCTIONS:
        return type + " " + subType + " : " + functionsList;
      case DEVICE_TYPE:
        return type + " " + subType + " " + value;
      default:
        return "SUB: " + "Unknown";
    }
  }
}