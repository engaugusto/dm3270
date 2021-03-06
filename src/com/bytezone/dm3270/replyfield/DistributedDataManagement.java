package com.bytezone.dm3270.replyfield;

import com.bytezone.dm3270.application.Utility;

public class DistributedDataManagement extends ReplyField
{
  int flags;
  int limitIn;
  int limitOut;
  int subsets;
  byte ddmSubset;

  public DistributedDataManagement ()
  {
    super (DISTRIBUTED_DATA_MANAGEMENT_REPLY);

    int ptr = createReply (8);
    checkDataLength (ptr + 8);
  }

  public DistributedDataManagement (byte[] buffer)
  {
    super (buffer);
    assert data[1] == DISTRIBUTED_DATA_MANAGEMENT_REPLY;

    flags = Utility.unsignedShort (data, 2);
    limitIn = Utility.unsignedShort (data, 4);
    limitOut = Utility.unsignedShort (data, 6);
    subsets = data[8] & 0xFF;
    ddmSubset = data[9];

    int ptr = 10;
    while (ptr < data.length)
    {
      int len = data[ptr] & 0xFF;
      byte id = data[ptr + 1];
      ptr += len;
    }
  }

  @Override
  public String toString ()
  {
    StringBuilder text = new StringBuilder (super.toString ());

    text.append (String.format ("%n  flags      : %04X", flags));
    text.append (String.format ("%n  limit in   : %s", limitIn));
    text.append (String.format ("%n  limit out  : %s", limitOut));
    text.append (String.format ("%n  subsets    : %s", subsets));
    text.append (String.format ("%n  DDMSS      : %s", ddmSubset));

    return text.toString ();
  }
}