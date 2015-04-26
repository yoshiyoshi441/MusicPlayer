package com.github.setuga;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MusicPlayer implements Runnable
{

    public static final int UNKNOWN = -1;
    public static final int PLAYING = 0;
    public static final int PAUSED = 1;
    public static final int STOPPED = 2;

    protected Logger logger = LogManager.getLogger(MusicPlayer.class);
    protected Thread musicThread = null;
    protected Object object;
    protected AudioInputStream musicAudioInputStream;
    protected AudioInputStream decodedAudioInputStream;
    protected AudioFileFormat musicAudioFileFormat;
    protected SourceDataLine sourceDataLine;

    private Map map = new HashMap();
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

    public Object getProperties(String key)
    {
        return map.get(key);
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
        Map properties = null;
        if (musicAudioFileFormat instanceof TAudioFileFormat)
        {
            properties = ((TAudioFileFormat) musicAudioFileFormat).properties();
            if (properties != null)
            {
                map.put("duration", properties.get("duration"));
                map.put("title", properties.get("title"));
                map.put("author", properties.get("author"));
                map.put("album", properties.get("album"));
                map.put("date", properties.get("date"));
                map.put("copyright", properties.get("copyright"));
                map.put("mp3.version.mpeg", properties.get("mp3.version.mpeg"));
                map.put("mp3.version.layer", properties.get("mp3.version.layer"));
                map.put("mp3.version.encoding", properties.get("mp3.version.encoding"));
                map.put("mp3.channels", properties.get("mp3.channels"));
                map.put("mp3.frequency.hz", properties.get("mp3.frequency.hz"));
                map.put("mp3.bitrate.nominal.bps", properties.get("mp3.bitrate.nominal.bps"));
                map.put("mp3.length.bytes", properties.get("mp3.length.bytes"));
                map.put("mp3.length.frames", properties.get("mp3.length.frames"));
                map.put("mp3.framesize.bytes", properties.get("mp3.framesize.bytes"));
                map.put("mp3.framerate.fps", properties.get("mp3.framerate.fps"));
                map.put("mp3.header.pos", properties.get("mp3.header.pos"));
                map.put("mp3.vbr", properties.get("mp3.vbr"));
                map.put("mp3.vbr.scale", properties.get("mp3.vbr.scale"));
                map.put("mp3.crc", properties.get("mp3.crc"));
                map.put("mp3.original", properties.get("mp3.original"));
                map.put("mp3.copyright", properties.get("mp3.copyright"));
                map.put("mp3.padding", properties.get("mp3.padding"));
                map.put("mp3.mode", properties.get("mp3.mode"));
                map.put("mp3.id3tag.genre", properties.get("mp3.id3tag.genre"));
                map.put("mp3.id3tag.track", properties.get("mp3.id3tag.track"));
                map.put("mp3.id3tag.v2", properties.get("mp3.id3tag.v2"));
            }
        }
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
            try
            {
                sourceDataLine.start();
                byte[] data = new byte[4096 * 10];
                int nBytesRead = 0, nBytesWritten = 0;
                while (nBytesRead != -1 && status != STOPPED && status != UNKNOWN)
                {
                    if (status == PLAYING)
                    {
                        nBytesRead = decodedAudioInputStream.read(data, 0, data.length);
                        if (nBytesRead != -1) nBytesWritten = sourceDataLine.write(data, 0, nBytesRead);
                    }
                    else if (status == PAUSED)
                    {
                        Thread.sleep(1000);
                    }
                }
                sourceDataLine.drain();
                sourceDataLine.stop();
                sourceDataLine.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void play()
    {
        initialization();
        openLine();
        musicThread = new Thread(this, "MusicPlayer");
        musicThread.start();
        logger.info("Music Start");
        status = PLAYING;
    }

    public void pause()
    {
        if (sourceDataLine != null)
        {
            if (status == PLAYING)
            {
                logger.info("Music Paused");
                status = PAUSED;
            }
        }
    }

    public void resume()
    {
        if (sourceDataLine != null)
        {
            if (status == PAUSED)
            {
                logger.info("Music Resume");
                status = PLAYING;
            }
        }
    }

    public void stop()
    {
        if (sourceDataLine != null)
        {
            if (status == PLAYING || status == PAUSED)
            {
                sourceDataLine.stop();
                status = STOPPED;
                logger.info("Music Stopped");
                synchronized (musicAudioInputStream)
                {
                    closeStream();
                }
            }
        }
    }

    protected void closeStream()
    {
        if (musicAudioInputStream != null)
        {
            try
            {
                musicAudioInputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            logger.info("AudioInputStream Closed");
        }
    }

}
