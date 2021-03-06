package com.bytezone.dm3270.session;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.bytezone.dm3270.application.Utility;
import com.bytezone.dm3270.streams.TelnetSocket.Source;

public class SessionReader
{
  private final String name;
  private final String firstLetter;
  private List<String> lines;
  private int nextLine;

  private boolean genuine;
  private boolean returnGenuine;

  private LocalDateTime dateTime;
  private LocalDateTime returnDateTime;

  private String label;
  private String returnLabel;

  public byte[] buffer = new byte[4096];

  public SessionReader (Source source, Path path)
  {
    this.name = source == Source.CLIENT ? "Client" : "Server";
    firstLetter = name.substring (0, 1);

    try
    {
      lines = Files.readAllLines (path);
    }
    catch (IOException e)
    {
      e.printStackTrace ();
      if (lines == null)
        lines = new ArrayList<> ();
    }
    skipToNext ();
  }

  public int next ()
  {
    if (nextLine >= lines.size ())
      return 0;

    int bytesWritten = 0;
    returnGenuine = genuine;
    returnDateTime = dateTime;
    returnLabel = label;

    for (String line : getBufferLines ())
    {
      while (!line.isEmpty ())
      {
        String b = line.substring (0, 2).trim ();     // get hex value
        line = line.substring (3);                    // remove it from the line
        if (b.isEmpty ())
          break;
        buffer[bytesWritten++] = (byte) Integer.parseInt (b, 16);
      }
    }
    return bytesWritten;
  }

  public byte[] nextBuffer ()
  {
    int bytesRead = next ();
    byte[] data = new byte[bytesRead];
    System.arraycopy (buffer, 0, data, 0, bytesRead);
    return data;
  }

  public boolean isGenuine ()
  {
    return returnGenuine;
  }

  public LocalDateTime getDateTime ()
  {
    return returnDateTime;
  }

  public String getLabel ()
  {
    return returnLabel;
  }

  // returns a List of lines contained in a single buffer
  private List<String> getBufferLines ()
  {
    List<String> list = new ArrayList<> ();
    while (nextLine < lines.size ())
    {
      String line = lines.get (nextLine);

      if (!line.startsWith ("0"))
        break;

      line = line.substring (8);
      if (line.length () > 48)
        line = line.substring (0, 48);      // leave a space on the end
      list.add (line);

      nextLine++;
    }

    skipToNext ();
    return list;
  }

  private void skipToNext ()
  {
    while (nextLine < lines.size ())
    {
      String line = lines.get (nextLine++);
      // skip to next line with our name
      if (line.startsWith (firstLetter))
      {
        if (line.length () > 7)
          genuine = line.charAt (7) != '*';
        else
          genuine = true;
        if (line.length () > 9)
          dateTime = LocalDateTime.parse (line.substring (9).trim ());
        else
          dateTime = null;

        while (nextLine < lines.size ())    // skip all comments and blank lines
        {
          if (lines.get (nextLine).startsWith ("0"))      // first buffer line
            return;
          nextLine++;
        }
        return;
      }
      else if (line.startsWith ("##"))
        label = line.substring (3);
    }

    nextLine++;         // make it greater than lines.size()
  }

  public int nextLineNo ()
  {
    return nextLine;
  }

  public static void main (String[] args) throws IOException
  {
    String base = "/Users/denismolony/Dropbox/Mainframe documentation/";
    String filename = String.format ("%smf.txt", base);
    int mode = 2;

    SessionReader server = new SessionReader (Source.SERVER, Paths.get (filename));
    SessionReader client = new SessionReader (Source.CLIENT, Paths.get (filename));

    while (client.nextLineNo () != server.nextLineNo ())    // both finished
    {
      if (client.nextLineNo () < server.nextLineNo ())
      {
        System.out.println ("-----------------< Client >--------------------");
        while (client.nextLineNo () < server.nextLineNo ())
          if (mode == 1)
            print (client.getBufferLines ());
          else
            print (client.nextBuffer ());
      }
      else
      {
        System.out.println ("-----------------< Server >--------------------");
        while (server.nextLineNo () < client.nextLineNo ())
          if (mode == 1)
            print (server.getBufferLines ());
          else
            print (server.nextBuffer ());
      }
    }
  }

  private static void print (byte[] buffer)
  {
    System.out.println (Utility.toHex (buffer));
    if (buffer[buffer.length - 1] == (byte) 0xEF)
      System.out.println ();
  }

  private static void print (List<String> list)
  {
    for (String line : list)
      System.out.println (line);
    System.out.println ();
  }
}