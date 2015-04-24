package com.github.setuga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MusicPlayer implements Runnable
{

    static protected Logger logger = LogManager.getLogger(MusicPlayer.class);

    protected Thread musicThread = null;
    protected Object object;
    protected AudioInputStream musicAudioInputStream;
    protected AudioInputStream decodedAudioInputStream;
    protected AudioFileFormat musicAudioFileFormat;

    protected SourceDataLine sourceDataLine;

    public static final int UNKNOWN = -1;
    public static final int PLAYING = 0;
    public static final int PAUSED = 1;
    public static final int STOPPED = 2;

    private int status = UNKNOWN;

    public MusicPlayer()
    {
        object = null;
        reset();
    }

    public int getStatus()
    {
        return status;
    }

    public void open(String path)
    {
        logger.info("open(" + path + ")");
        File file = new File(path);
        if (file != null)
        {
            object = file;
        }
    }

    public void open(File file)
    {
        logger.info("open(" + file + ")");
        if (file != null)
        {
            object = file;
        }
    }

    public void open(InputStream inputStream)
    {
        logger.info("open(" + inputStream + ")");
        if (inputStream != null)
        {
            object = inputStream;
        }
    }

    public void open(URL url)
    {
        logger.info("open(" + url + ")");
        if (url != null)
        {
            object = url;
        }
    }

    protected void reset()
    {
        status = UNKNOWN;
        if (musicAudioInputStream != null)
        {
            synchronized (musicAudioInputStream)
            {
                closeStream();
            }
        }
        musicAudioInputStream = null;
        musicAudioFileFormat = null;
    }

    protected void initialization()
    {
        if (object instanceof File)
        {
            initialization((File) object);
        }
        else if (object instanceof InputStream)
        {
            initialization((InputStream) object);
        }
        else if (object instanceof URL)
        {
            initialization((URL) object);
        }
        createLine();
    }

    protected void initialization(File file)
    {
        try
        {
            musicAudioInputStream = AudioSystem.getAudioInputStream(file);
            musicAudioFileFormat = AudioSystem.getAudioFileFormat(file);
        }
        catch (UnsupportedAudioFileException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected void initialization(InputStream inputStream)
    {
        try
        {
            musicAudioInputStream = AudioSystem.getAudioInputStream(inputStream);
            musicAudioFileFormat = AudioSystem.getAudioFileFormat(inputStream);
        }
        catch (UnsupportedAudioFileException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected void initialization(URL url)
    {
        try
        {
            musicAudioInputStream = AudioSystem.getAudioInputStream(url);
            musicAudioFileFormat = AudioSystem.getAudioFileFormat(url);
        }
        catch (UnsupportedAudioFileException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    protected void createLine()
    {
        logger.info("Create Line");
        AudioFormat baseAudioFormat = musicAudioFileFormat.getFormat();
        int sampleSizeInBits = baseAudioFormat.getSampleSizeInBits();
        if (sampleSizeInBits <= 0) sampleSizeInBits = 16;
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseAudioFormat.getSampleRate(),
                sampleSizeInBits, baseAudioFormat.getChannels(), baseAudioFormat.getChannels() * 2, baseAudioFormat.getSampleRate(), false);
        decodedAudioInputStream = AudioSystem.getAudioInputStream(audioFormat, musicAudioInputStream);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try
        {
            sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
        }
        catch (LineUnavailableException e)
        {
            e.printStackTrace();
        }
        logger.info("Line : " + sourceDataLine.toString());
        logger.info("Line Info : " + info.toString());
        logger.info("Line Format : " + audioFormat);
    }

    protected void openLine()
    {
        if (sourceDataLine != null)
        {
            AudioFormat audioFormat = decodedAudioInputStream.getFormat();
            int bufferSize = sourceDataLine.getBufferSize();
            try
            {
                sourceDataLine.open(audioFormat, bufferSize);
            }
            catch (LineUnavailableException e)
            {
                e.printStackTrace();
            }
            logger.info("Open Line BufferSize : " + bufferSize);
        }
    }

    public void run()
    {
        synchronized (decodedAudioInputStream)
        {
            sourceDataLine.start();
            byte[] data = new byte[4096];
            int nBytesRead = 0, nBytesWritten = 0;
            while (nBytesRead != -1)
            {
                try
                {
                    nBytesRead = decodedAudioInputStream.read(data, 0, data.length);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                if (nBytesRead != -1) nBytesWritten = sourceDataLine.write(data, 0, nBytesRead);
            }
            sourceDataLine.drain();
            sourceDataLine.stop();
            sourceDataLine.close();
        }
    }

    protected void play()
    {
        initialization();
        openLine();
        musicThread = new Thread(this, "MusicPlayer");
        musicThread.start();
        status = PLAYING;
    }

    protected void closeStream()
    {
        if (decodedAudioInputStream != null)
        {
            try
            {
                decodedAudioInputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            logger.info("AudioInputStream Closed");
        }
    }

}
