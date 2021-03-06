package com.bytezone.dm3270.attributes;

import com.bytezone.dm3270.application.ScreenPosition;

public class ResetAttribute extends Attribute
{
  public ResetAttribute (byte value)
  {
    super (AttributeType.RESET, XA_RESET, value);
  }

  @Override
  public void process (ScreenPosition screenPosition)
  {
    // this should probably deal with the field as the SP has to revert to a
    // previous value
    screenPosition.resetAttribute ();
  }
}